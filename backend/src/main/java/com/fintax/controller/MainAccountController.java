package com.fintax.controller;

import com.mybatisflex.core.paginate.Page;
import com.fintax.common.Result;
import com.fintax.controller.req.MainAccountCreateReq;
import com.fintax.controller.req.MainAccountPageReq;
import com.fintax.controller.req.MainAccountUpdateReq;
import com.fintax.controller.resp.MainAccountResp;
import com.fintax.entity.MainAccount;
import com.fintax.service.CryptoTxService;
import com.fintax.service.MainAccountService;
import com.fintax.service.SubAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 主账户管理控制器
 *
 * <p>提供 Main Account 的新建、编辑、删除（软删）、分页列表查询接口</p>
 */
@RestController
@RequestMapping("/api/main-accounts")
@RequiredArgsConstructor
public class MainAccountController {

    private final MainAccountService mainAccountService;
    private final SubAccountService subAccountService;
    private final CryptoTxService cryptoTxService;

    /**
     * 创建主账户
     *
     * @param req 主账户创建请求，包含 accountName、accountType、accountSource、address、status、organization
     * @return 创建成功的主账户信息
     */
    @PostMapping
    public Result<MainAccountResp> create(@RequestBody MainAccountCreateReq req) {
        MainAccount account = MainAccount.builder()
                .accountName(req.getAccountName())
                .accountType(req.getAccountType())
                .accountSource(req.getAccountSource())
                .address(req.getAddress())
                .status(req.getStatus())
                .organization(req.getOrganization())
                .build();
        MainAccount created = mainAccountService.create(account);
        return Result.success(MainAccountResp.fromEntity(created));
    }

    /**
     * 编辑主账户
     *
     * @param id  主账户 ID（路径参数）
     * @param req 主账户更新请求，包含需要修改的字段
     * @return 更新后的主账户信息
     */
    @PutMapping("/{id}")
    public Result<MainAccountResp> update(@PathVariable Long id, @RequestBody MainAccountUpdateReq req) {
        MainAccount existing = mainAccountService.getById(id);
        if (existing == null) {
            return Result.error(404, "主账户不存在");
        }
        existing.setAccountName(req.getAccountName());
        existing.setAccountType(req.getAccountType());
        existing.setAccountSource(req.getAccountSource());
        existing.setAddress(req.getAddress());
        existing.setStatus(req.getStatus());
        existing.setOrganization(req.getOrganization());
        MainAccount updated = mainAccountService.update(existing);
        return Result.success(MainAccountResp.fromEntity(updated));
    }

    /**
     * 删除主账户（软删除）
     *
     * <p>删除前校验：如果该主账户下存在未删除的子账户或交易记录，则不允许删除</p>
     *
     * @param id 主账户 ID（路径参数）
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        MainAccount existing = mainAccountService.getById(id);
        if (existing == null) {
            return Result.error(404, "主账户不存在");
        }
        if (subAccountService.existsByMainAccountId(id)) {
            return Result.error(400, "该主账户下存在子账户，无法删除");
        }
        if (cryptoTxService.existsByMainAccountId(id)) {
            return Result.error(400, "该主账户下存在交易记录，无法删除");
        }
        mainAccountService.delete(id);
        return Result.success();
    }

    /**
     * 根据 ID 查询主账户
     *
     * @param id 主账户 ID（路径参数）
     * @return 主账户详情
     */
    @GetMapping("/{id}")
    public Result<MainAccountResp> getById(@PathVariable Long id) {
        MainAccount account = mainAccountService.getById(id);
        if (account == null) {
            return Result.error(404, "主账户不存在");
        }
        return Result.success(MainAccountResp.fromEntity(account));
    }

    /**
     * 分页查询主账户列表
     *
     * @param req 分页参数，包含 pageNumber（默认1）、pageSize（默认20）
     * @return 分页结果，包含 records、totalRow、pageNumber、pageSize
     */
    @GetMapping
    public Result<Map<String, Object>> page(MainAccountPageReq req) {
        Page<MainAccount> page = mainAccountService.page(req.getPageNumber(), req.getPageSize());
        List<MainAccountResp> records = page.getRecords().stream()
                .map(MainAccountResp::fromEntity)
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("totalRow", page.getTotalRow());
        result.put("pageNumber", page.getPageNumber());
        result.put("pageSize", page.getPageSize());
        return Result.success(result);
    }
}
