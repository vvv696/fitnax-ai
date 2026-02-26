package com.fintax.service;

import com.fintax.controller.resp.ImportResultResp;
import com.fintax.entity.CryptoTx;
import com.fintax.entity.SubAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Excel 导入服务
 *
 * <p>解析上传的 Excel 文件，自动创建 Sub Account 并批量写入 crypto_tx</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final SubAccountService subAccountService;
    private final CryptoTxService cryptoTxService;

    /**
     * 导入 Excel 文件中的交易记录
     *
     * <p>流程：
     * 1. 生成 import_batch_id (UUID)
     * 2. 创建 Sub Account（import_type=Excel, account_source=Excel）
     * 3. 逐行解析 Excel，映射为 CryptoTx
     * 4. 批量插入（跳过重复行，捕获唯一约束冲突）
     * </p>
     *
     * @param mainAccountId 主账户 ID
     * @param file          上传的 Excel 文件（.xlsx）
     * @return 导入结果（成功条数、失败条数、失败行详情）
     */
    @Transactional
    public ImportResultResp importExcel(Long mainAccountId, MultipartFile file) {
        String importBatchId = UUID.randomUUID().toString().replace("-", "");
        String fileName = file.getOriginalFilename();

        // 创建 Sub Account
        SubAccount subAccount = SubAccount.builder()
                .mainAccountId(mainAccountId)
                .subAccountName("Excel-" + importBatchId.substring(0, 8))
                .category("Import")
                .accountSource("Excel")
                .importType("Excel")
                .importFileName(fileName)
                .importBatchId(importBatchId)
                .status("Active")
                .build();
        SubAccount created = subAccountService.create(subAccount);
        Long subAccountId = created.getId();

        List<ImportResultResp.FailRow> failRows = new ArrayList<>();
        List<CryptoTx> successTxList = new ArrayList<>();
        int rowIndex = 0;

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                failRows.add(ImportResultResp.FailRow.builder()
                        .rowNo(1).reason("Excel 文件无表头行").build());
                return buildResult(0, failRows);
            }

            // 解析表头，建立列名到索引的映射
            int[] colMap = parseHeader(headerRow);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                rowIndex = i + 1; // 用户可见的行号（从1开始，+1因为表头）
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                try {
                    CryptoTx tx = parseRow(row, colMap, mainAccountId, subAccountId, importBatchId, i);
                    successTxList.add(tx);
                } catch (Exception e) {
                    failRows.add(ImportResultResp.FailRow.builder()
                            .rowNo(rowIndex).reason(e.getMessage()).build());
                }
            }

        } catch (Exception e) {
            log.error("Excel 解析异常", e);
            failRows.add(ImportResultResp.FailRow.builder()
                    .rowNo(rowIndex).reason("Excel 解析异常: " + e.getMessage()).build());
            return buildResult(0, failRows);
        }

        // 批量插入，逐条处理以捕获重复
        int successCount = 0;
        for (CryptoTx tx : successTxList) {
            try {
                cryptoTxService.batchInsert(List.of(tx));
                successCount++;
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("uk_tx_dedupe")) {
                    failRows.add(ImportResultResp.FailRow.builder()
                            .rowNo(tx.getRowNo() + 1).reason("重复记录，已跳过").build());
                } else {
                    failRows.add(ImportResultResp.FailRow.builder()
                            .rowNo(tx.getRowNo() + 1).reason("插入失败: " + msg).build());
                }
            }
        }

        return buildResult(successCount, failRows);
    }

    /**
     * 解析表头，返回列索引数组
     * 顺序: [tx_hash, direction, category, currency, amount, from_address, to_address, fee, fee_currency, chain_name, trade_time, description, business_type]
     */
    private int[] parseHeader(Row headerRow) {
        int[] colMap = new int[13];
        java.util.Arrays.fill(colMap, -1);

        String[] expectedHeaders = {
                "tx_hash", "direction", "category", "currency", "amount",
                "from_address", "to_address", "fee", "fee_currency",
                "chain_name", "trade_time", "description", "business_type"
        };
        String[][] aliases = {
                {"tx_hash", "txhash", "hash", "transaction_hash", "txn_id"},
                {"direction", "type", "side"},
                {"category"},
                {"currency", "token", "coin", "asset"},
                {"amount", "quantity", "qty", "value"},
                {"from_address", "from", "sender"},
                {"to_address", "to", "receiver", "recipient"},
                {"fee", "gas_fee", "tx_fee"},
                {"fee_currency", "fee_token"},
                {"chain_name", "chain", "network", "blockchain"},
                {"trade_time", "time", "timestamp", "date", "datetime"},
                {"description", "desc", "memo", "note", "remark"},
                {"business_type", "biz_type"}
        };

        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            if (cell == null) continue;
            String header = getCellStringValue(cell).trim().toLowerCase().replace(" ", "_");
            for (int h = 0; h < aliases.length; h++) {
                for (String alias : aliases[h]) {
                    if (header.equals(alias)) {
                        colMap[h] = c;
                        break;
                    }
                }
            }
        }

        return colMap;
    }

    private CryptoTx parseRow(Row row, int[] colMap, Long mainAccountId, Long subAccountId,
                               String importBatchId, int rowNo) {
        String txHash = getColValue(row, colMap, 0);
        String direction = getColValue(row, colMap, 1);
        String category = getColValue(row, colMap, 2);
        String currency = getColValue(row, colMap, 3);
        BigDecimal amount = getColBigDecimal(row, colMap, 4);
        String fromAddress = getColValue(row, colMap, 5);
        String toAddress = getColValue(row, colMap, 6);
        BigDecimal fee = getColBigDecimal(row, colMap, 7);
        String feeCurrency = getColValue(row, colMap, 8);
        String chainName = getColValue(row, colMap, 9);
        LocalDateTime tradeTime = getColDateTime(row, colMap, 10);
        String description = getColValue(row, colMap, 11);
        String businessType = getColValue(row, colMap, 12);

        if (txHash == null || txHash.isBlank()) {
            throw new IllegalArgumentException("tx_hash 不能为空");
        }
        if (direction == null || direction.isBlank()) {
            throw new IllegalArgumentException("direction 不能为空");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency 不能为空");
        }
        if (amount == null) {
            throw new IllegalArgumentException("amount 不能为空");
        }
        if (tradeTime == null) {
            throw new IllegalArgumentException("trade_time 不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        return CryptoTx.builder()
                .mainAccountId(mainAccountId)
                .subAccountId(subAccountId)
                .txHash(txHash)
                .direction(direction)
                .category(category)
                .businessType(businessType)
                .currency(currency)
                .amount(amount)
                .fromAddress(fromAddress != null ? fromAddress : "")
                .toAddress(toAddress != null ? toAddress : "")
                .fee(fee)
                .feeCurrency(feeCurrency)
                .chainName(chainName)
                .tradeTime(tradeTime)
                .description(description)
                .importBatchId(importBatchId)
                .rowNo(rowNo)
                .isDeleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private String getColValue(Row row, int[] colMap, int index) {
        if (colMap[index] < 0) return null;
        Cell cell = row.getCell(colMap[index]);
        if (cell == null) return null;
        return getCellStringValue(cell);
    }

    private BigDecimal getColBigDecimal(Row row, int[] colMap, int index) {
        if (colMap[index] < 0) return null;
        Cell cell = row.getCell(colMap[index]);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        String val = getCellStringValue(cell).trim();
        if (val.isBlank()) return null;
        return new BigDecimal(val);
    }

    private LocalDateTime getColDateTime(Row row, int[] colMap, int index) {
        if (colMap[index] < 0) return null;
        Cell cell = row.getCell(colMap[index]);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        String val = getCellStringValue(cell).trim();
        if (val.isBlank()) return null;
        return LocalDateTime.parse(val.replace(" ", "T"));
    }

    private String getCellStringValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getStringCellValue();
            default -> "";
        };
    }

    private boolean isEmptyRow(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellStringValue(cell).trim();
                if (!val.isEmpty()) return false;
            }
        }
        return true;
    }

    private ImportResultResp buildResult(int successCount, List<ImportResultResp.FailRow> failRows) {
        return ImportResultResp.builder()
                .successCount(successCount)
                .failCount(failRows.size())
                .failRows(failRows)
                .build();
    }
}
