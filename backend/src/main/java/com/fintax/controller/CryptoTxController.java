package com.fintax.controller;

import com.mybatisflex.core.paginate.Page;
import com.fintax.common.Result;
import com.fintax.controller.req.TxDetailReq;
import com.fintax.controller.req.TxGroupPageReq;
import com.fintax.controller.req.TxUpdateBusinessTypeReq;
import com.fintax.controller.req.TxUpdateDescReq;
import com.fintax.controller.resp.TxDetailResp;
import com.fintax.controller.resp.TxGroupResp;
import com.fintax.entity.CryptoTx;
import com.fintax.entity.MainAccount;
import com.fintax.service.CryptoTxService;
import com.fintax.service.MainAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 交易管理控制器
 *
 * <p>提供 Crypto/Transactions 页面的 Tx Group 聚合查询、明细展开、
 * Description 和 Business Type 编辑接口</p>
 */
@RestController
@RequestMapping("/api/crypto-tx")
@RequiredArgsConstructor
public class CryptoTxController {

    private final CryptoTxService cryptoTxService;
    private final MainAccountService mainAccountService;

    /**
     * Tx Group 分页查询（按 tx_hash 聚合）
     *
     * <p>对 crypto_tx 按 tx_hash 进行 GROUP BY 聚合查询，输出：
     * Category（Send/Receive/Mixed）、Direction（From/To 地址）、Account Name、
     * Trade Time、Inflow、Outflow、Fee、Chain Name、Description</p>
     *
     * @param req 筛选条件，包含 mainAccountId、subAccountId、txHash、direction、category、chainName、startTime、endTime、分页参数
     * @return 分页的 TxGroup 聚合结果
     */
    @GetMapping("/groups")
    public Result<Map<String, Object>> pageGroups(TxGroupPageReq req) {
        LocalDateTime startTime = parseDateTime(req.getStartTime());
        LocalDateTime endTime = parseDateTime(req.getEndTime());

        // 获取去重 tx_hash 的分页结果
        Page<CryptoTx> txHashPage = cryptoTxService.pageDistinctTxHash(
                req.getPageNumber(), req.getPageSize(),
                req.getMainAccountId(), req.getSubAccountId(),
                req.getTxHash(), req.getDirection(),
                req.getCategory(), req.getChainName(),
                startTime, endTime);

        // 对每个 tx_hash 获取明细并聚合
        List<TxGroupResp> groups = new ArrayList<>();
        for (CryptoTx hashRow : txHashPage.getRecords()) {
            String txHash = hashRow.getTxHash();
            List<CryptoTx> details = cryptoTxService.listByTxHash(txHash);
            if (details.isEmpty()) continue;

            groups.add(aggregateGroup(txHash, details));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", groups);
        result.put("totalRow", txHashPage.getTotalRow());
        result.put("pageNumber", txHashPage.getPageNumber());
        result.put("pageSize", txHashPage.getPageSize());
        return Result.success(result);
    }

    /**
     * Tx Details 查询（按 tx_hash 展开明细）
     *
     * <p>根据 tx_hash 和可选的 sub_account_id / main_account_id 返回该交易的所有明细行</p>
     *
     * @param req 查询条件，包含 txHash（必填）、mainAccountId（可选）、subAccountId（可选）
     * @return 交易明细列表
     */
    @GetMapping("/details")
    public Result<List<TxDetailResp>> getDetails(TxDetailReq req) {
        if (req.getTxHash() == null || req.getTxHash().isBlank()) {
            return Result.error(400, "txHash 不能为空");
        }

        List<CryptoTx> details;
        if (req.getSubAccountId() != null) {
            details = cryptoTxService.listByTxHashAndSubAccount(req.getTxHash(), req.getSubAccountId());
        } else if (req.getMainAccountId() != null) {
            details = cryptoTxService.listByTxHashAndMainAccount(req.getTxHash(), req.getMainAccountId());
        } else {
            details = cryptoTxService.listByTxHash(req.getTxHash());
        }

        List<TxDetailResp> records = details.stream()
                .map(this::toDetailResp)
                .collect(Collectors.toList());
        return Result.success(records);
    }

    /**
     * 按 tx_hash 批量更新 Description
     *
     * <p>将指定 tx_hash 下所有未删除交易的 description 更新为新值</p>
     *
     * @param req 包含 txHash 和新的 description
     * @return 更新的记录数
     */
    @PutMapping("/description")
    public Result<Integer> updateDescription(@RequestBody TxUpdateDescReq req) {
        if (req.getTxHash() == null || req.getTxHash().isBlank()) {
            return Result.error(400, "txHash 不能为空");
        }
        int count = cryptoTxService.updateDescriptionByTxHash(req.getTxHash(), req.getDescription());
        return Result.success(count);
    }

    /**
     * 按 tx_hash 批量更新 Business Type
     *
     * <p>将指定 tx_hash 下所有未删除交易的 business_type 更新为新值</p>
     *
     * @param req 包含 txHash 和新的 businessType
     * @return 更新的记录数
     */
    @PutMapping("/business-type")
    public Result<Integer> updateBusinessType(@RequestBody TxUpdateBusinessTypeReq req) {
        if (req.getTxHash() == null || req.getTxHash().isBlank()) {
            return Result.error(400, "txHash 不能为空");
        }
        int count = cryptoTxService.updateBusinessTypeByTxHash(req.getTxHash(), req.getBusinessType());
        return Result.success(count);
    }

    /**
     * 聚合单个 tx_hash 下的所有明细为 TxGroupResp
     */
    private TxGroupResp aggregateGroup(String txHash, List<CryptoTx> details) {
        Set<String> directions = details.stream()
                .map(CryptoTx::getDirection)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        String category;
        if (directions.contains("Send") && directions.contains("Receive")) {
            category = "Mixed";
        } else if (directions.contains("Send")) {
            category = "Send";
        } else if (directions.contains("Receive")) {
            category = "Receive";
        } else {
            category = details.get(0).getCategory();
        }

        // Collect unique from/to addresses
        String fromAddress = details.stream()
                .map(CryptoTx::getFromAddress)
                .filter(a -> a != null && !a.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));

        String toAddress = details.stream()
                .map(CryptoTx::getToAddress)
                .filter(a -> a != null && !a.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));

        // Get account name from main_account
        Long mainAccountId = details.get(0).getMainAccountId();
        String accountName = "";
        if (mainAccountId != null) {
            MainAccount mainAccount = mainAccountService.getById(mainAccountId);
            if (mainAccount != null) {
                accountName = mainAccount.getAccountName();
            }
        }

        // Trade time: use the earliest
        LocalDateTime tradeTime = details.stream()
                .map(CryptoTx::getTradeTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        // Inflow: sum of Receive amounts
        BigDecimal inflow = details.stream()
                .filter(tx -> "Receive".equalsIgnoreCase(tx.getDirection()))
                .map(CryptoTx::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Outflow: sum of Send amounts
        BigDecimal outflow = details.stream()
                .filter(tx -> "Send".equalsIgnoreCase(tx.getDirection()))
                .map(CryptoTx::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Fee: sum of all fees
        BigDecimal fee = details.stream()
                .map(CryptoTx::getFee)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String chainName = details.stream()
                .map(CryptoTx::getChainName)
                .filter(c -> c != null && !c.isBlank())
                .findFirst()
                .orElse("");

        String description = details.stream()
                .map(CryptoTx::getDescription)
                .filter(d -> d != null && !d.isBlank())
                .findFirst()
                .orElse("");

        String businessType = details.stream()
                .map(CryptoTx::getBusinessType)
                .filter(b -> b != null && !b.isBlank())
                .findFirst()
                .orElse("");

        return TxGroupResp.builder()
                .txHash(txHash)
                .category(category)
                .businessType(businessType)
                .fromAddress(fromAddress)
                .toAddress(toAddress)
                .accountName(accountName)
                .tradeTime(tradeTime)
                .inflow(inflow)
                .outflow(outflow)
                .fee(fee)
                .chainName(chainName)
                .description(description)
                .detailCount(details.size())
                .build();
    }

    private TxDetailResp toDetailResp(CryptoTx tx) {
        return TxDetailResp.builder()
                .id(tx.getId())
                .txHash(tx.getTxHash())
                .direction(tx.getDirection())
                .category(tx.getCategory())
                .businessType(tx.getBusinessType())
                .currency(tx.getCurrency())
                .amount(tx.getAmount())
                .fromAddress(tx.getFromAddress())
                .toAddress(tx.getToAddress())
                .fee(tx.getFee())
                .feeCurrency(tx.getFeeCurrency())
                .chainName(tx.getChainName())
                .tradeTime(tx.getTradeTime())
                .description(tx.getDescription())
                .mainAccountId(tx.getMainAccountId())
                .subAccountId(tx.getSubAccountId())
                .build();
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) return null;
        return LocalDateTime.parse(dateTimeStr.replace(" ", "T"));
    }
}
