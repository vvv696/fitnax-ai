package com.fintax.controller;

import com.mybatisflex.core.paginate.Page;
import com.fintax.common.Result;
import com.fintax.controller.req.ReconcileGenerateReq;
import com.fintax.controller.req.ReconcilePageReq;
import com.fintax.controller.resp.ReconcileSnapshotResp;
import com.fintax.entity.AssetReconcileSnapshot;
import com.fintax.entity.CryptoTx;
import com.fintax.entity.SubAccount;
import com.fintax.service.AssetReconcileSnapshotService;
import com.fintax.service.CryptoTxService;
import com.fintax.service.SubAccountService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 对账管理控制器
 *
 * <p>提供对账快照生成、列表查询、导出接口</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/reconcile")
@RequiredArgsConstructor
public class AssetReconcileController {

    private final AssetReconcileSnapshotService snapshotService;
    private final CryptoTxService cryptoTxService;
    private final SubAccountService subAccountService;

    /**
     * 生成对账快照
     *
     * <p>按 Sub Account + Currency 维度计算 system_total（Receive 净额 - Send 净额），
     * onchain_balance 使用 mock 值（固定为 0），计算 diff 和 diff_status</p>
     *
     * @param req 包含 mainAccountId（必填）、subAccountId（可选）、snapshotTime（可选，默认当前时间）
     * @return 生成的对账快照列表
     */
    @PostMapping("/generate")
    public Result<List<ReconcileSnapshotResp>> generate(@RequestBody ReconcileGenerateReq req) {
        if (req.getMainAccountId() == null) {
            return Result.error(400, "mainAccountId 不能为空");
        }

        LocalDateTime snapshotTime = req.getSnapshotTime() != null && !req.getSnapshotTime().isBlank()
                ? LocalDateTime.parse(req.getSnapshotTime().replace(" ", "T"))
                : LocalDateTime.now();

        // 获取要处理的 sub_account 列表
        List<SubAccount> subAccounts;
        if (req.getSubAccountId() != null) {
            SubAccount sa = subAccountService.getById(req.getSubAccountId());
            subAccounts = sa != null ? List.of(sa) : List.of();
        } else {
            subAccounts = subAccountService.listByMainAccountId(req.getMainAccountId());
        }

        List<AssetReconcileSnapshot> snapshots = new ArrayList<>();

        for (SubAccount sa : subAccounts) {
            // 获取该 sub_account 下所有交易，按 currency 分组
            List<CryptoTx> txList = cryptoTxService.page(1, Integer.MAX_VALUE,
                            req.getMainAccountId(), sa.getId(), null, null, null, null)
                    .getRecords();

            Map<String, List<CryptoTx>> byCurrency = txList.stream()
                    .filter(tx -> tx.getCurrency() != null)
                    .collect(Collectors.groupingBy(CryptoTx::getCurrency));

            for (Map.Entry<String, List<CryptoTx>> entry : byCurrency.entrySet()) {
                String currency = entry.getKey();
                List<CryptoTx> currencyTxs = entry.getValue();

                // system_total = Σ(Receive amount) - Σ(Send amount)
                BigDecimal receiveTotal = currencyTxs.stream()
                        .filter(tx -> "Receive".equalsIgnoreCase(tx.getDirection()))
                        .map(CryptoTx::getAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal sendTotal = currencyTxs.stream()
                        .filter(tx -> "Send".equalsIgnoreCase(tx.getDirection()))
                        .map(CryptoTx::getAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal systemTotal = receiveTotal.subtract(sendTotal);

                // Mock onchain_balance (固定值 0，后续替换为真实链上余额)
                BigDecimal onchainBalance = BigDecimal.ZERO;

                BigDecimal diff = systemTotal.subtract(onchainBalance);
                BigDecimal diffAbs = diff.abs();
                String diffStatus = diff.compareTo(BigDecimal.ZERO) == 0 ? "Matched" : "Unmatched";

                AssetReconcileSnapshot snapshot = AssetReconcileSnapshot.builder()
                        .mainAccountId(req.getMainAccountId())
                        .subAccountId(sa.getId())
                        .currency(currency)
                        .systemTotal(systemTotal)
                        .onchainBalance(onchainBalance)
                        .diff(diff)
                        .diffAbs(diffAbs)
                        .diffStatus(diffStatus)
                        .snapshotTime(snapshotTime)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                snapshots.add(snapshot);
            }
        }

        if (!snapshots.isEmpty()) {
            snapshotService.batchSave(snapshots);
        }

        List<ReconcileSnapshotResp> respList = snapshots.stream()
                .map(s -> {
                    ReconcileSnapshotResp resp = ReconcileSnapshotResp.fromEntity(s);
                    SubAccount sa = subAccounts.stream()
                            .filter(a -> a.getId().equals(s.getSubAccountId()))
                            .findFirst().orElse(null);
                    if (sa != null) {
                        resp.setSubAccountName(sa.getSubAccountName());
                    }
                    return resp;
                })
                .collect(Collectors.toList());

        return Result.success(respList);
    }

    /**
     * 分页查询对账快照列表
     *
     * <p>支持按 mainAccountId、subAccountId、currency、时间范围筛选</p>
     *
     * @param req 分页与筛选参数
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(ReconcilePageReq req) {
        LocalDateTime startTime = parseDateTime(req.getStartTime());
        LocalDateTime endTime = parseDateTime(req.getEndTime());

        Page<AssetReconcileSnapshot> page = snapshotService.page(
                req.getPageNumber(), req.getPageSize(),
                req.getMainAccountId(), req.getSubAccountId(),
                req.getCurrency(), startTime, endTime);

        List<ReconcileSnapshotResp> records = page.getRecords().stream()
                .map(s -> {
                    ReconcileSnapshotResp resp = ReconcileSnapshotResp.fromEntity(s);
                    SubAccount sa = subAccountService.getById(s.getSubAccountId());
                    if (sa != null) {
                        resp.setSubAccountName(sa.getSubAccountName());
                    }
                    return resp;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("totalRow", page.getTotalRow());
        result.put("pageNumber", page.getPageNumber());
        result.put("pageSize", page.getPageSize());
        return Result.success(result);
    }

    /**
     * 导出对账数据为 Excel 文件
     *
     * <p>按筛选条件查询全量对账数据并生成 .xlsx 下载</p>
     *
     * @param mainAccountId 主账户 ID（可选）
     * @param subAccountId  子账户 ID（可选）
     * @param currency      币种（可选）
     * @param startTime     起始时间（可选）
     * @param endTime       结束时间（可选）
     * @param response      HTTP 响应
     */
    @GetMapping("/export")
    public void export(@RequestParam(required = false) Long mainAccountId,
                       @RequestParam(required = false) Long subAccountId,
                       @RequestParam(required = false) String currency,
                       @RequestParam(required = false) String startTime,
                       @RequestParam(required = false) String endTime,
                       HttpServletResponse response) {
        LocalDateTime start = parseDateTime(startTime);
        LocalDateTime end = parseDateTime(endTime);

        List<AssetReconcileSnapshot> data = snapshotService.listForExport(
                mainAccountId, subAccountId, currency, start, end);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=assets_reconcile.xlsx");

        try (Workbook workbook = new XSSFWorkbook();
             OutputStream os = response.getOutputStream()) {

            Sheet sheet = workbook.createSheet("Reconcile");
            // Header
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Sub Account ID", "Sub Account Name", "Currency",
                    "System Total", "On-chain Balance", "Diff", "Diff Abs",
                    "Status", "Snapshot Time"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowIdx = 1;
            for (AssetReconcileSnapshot s : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(s.getSubAccountId());
                SubAccount sa = subAccountService.getById(s.getSubAccountId());
                row.createCell(1).setCellValue(sa != null ? sa.getSubAccountName() : "");
                row.createCell(2).setCellValue(s.getCurrency());
                row.createCell(3).setCellValue(s.getSystemTotal() != null ? s.getSystemTotal().doubleValue() : 0);
                row.createCell(4).setCellValue(s.getOnchainBalance() != null ? s.getOnchainBalance().doubleValue() : 0);
                row.createCell(5).setCellValue(s.getDiff() != null ? s.getDiff().doubleValue() : 0);
                row.createCell(6).setCellValue(s.getDiffAbs() != null ? s.getDiffAbs().doubleValue() : 0);
                row.createCell(7).setCellValue(s.getDiffStatus() != null ? s.getDiffStatus() : "");
                row.createCell(8).setCellValue(s.getSnapshotTime() != null ? s.getSnapshotTime().toString() : "");
            }

            workbook.write(os);
        } catch (Exception e) {
            log.error("导出对账数据失败", e);
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) return null;
        return LocalDateTime.parse(dateTimeStr.replace(" ", "T"));
    }
}
