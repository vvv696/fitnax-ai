package com.fintax.controller;

import com.mybatisflex.core.paginate.Page;
import com.fintax.common.Result;
import com.fintax.controller.req.SubAccountCreateReq;
import com.fintax.controller.req.SubAccountPageReq;
import com.fintax.controller.req.SubAccountUpdateReq;
import com.fintax.controller.resp.SubAccountResp;
import com.fintax.entity.SubAccount;
import com.fintax.service.SubAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 子账户管理控制器
 *
 * <p>提供 Sub Account 的新建、编辑、删除（软删）、按 main_account_id 列表查询接口</p>
 */
@RestController
@RequestMapping("/api/sub-accounts")
@RequiredArgsConstructor
public class SubAccountController {

    private final SubAccountService subAccountService;

    /**
     * 创建子账户
     *
     * @param req 子账户创建请求，包含 mainAccountId、subAccountName、category、accountSource、importType、address、status
     * @return 创建成功的子账户信息
     */
    @PostMapping
    public Result<SubAccountResp> create(@RequestBody SubAccountCreateReq req) {
        SubAccount subAccount = SubAccount.builder()
                .mainAccountId(req.getMainAccountId())
                .subAccountName(req.getSubAccountName())
                .category(req.getCategory())
                .accountSource(req.getAccountSource())
                .importType(req.getImportType())
                .address(req.getAddress())
                .status(req.getStatus())
                .build();
        SubAccount created = subAccountService.create(subAccount);
        return Result.success(SubAccountResp.fromEntity(created));
    }

    /**
     * 编辑子账户
     *
     * @param id  子账户 ID（路径参数）
     * @param req 子账户更新请求，包含需要修改的字段
     * @return 更新后的子账户信息
     */
    @PutMapping("/{id}")
    public Result<SubAccountResp> update(@PathVariable Long id, @RequestBody SubAccountUpdateReq req) {
        SubAccount existing = subAccountService.getById(id);
        if (existing == null) {
            return Result.error(404, "子账户不存在");
        }
        existing.setSubAccountName(req.getSubAccountName());
        existing.setCategory(req.getCategory());
        existing.setAccountSource(req.getAccountSource());
        existing.setImportType(req.getImportType());
        existing.setAddress(req.getAddress());
        existing.setStatus(req.getStatus());
        SubAccount updated = subAccountService.update(existing);
        return Result.success(SubAccountResp.fromEntity(updated));
    }

    /**
     * 删除子账户（软删除）
     *
     * @param id 子账户 ID（路径参数）
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SubAccount existing = subAccountService.getById(id);
        if (existing == null) {
            return Result.error(404, "子账户不存在");
        }
        subAccountService.delete(id);
        return Result.success();
    }

    /**
     * 根据 ID 查询子账户
     *
     * @param id 子账户 ID（路径参数）
     * @return 子账户详情
     */
    @GetMapping("/{id}")
    public Result<SubAccountResp> getById(@PathVariable Long id) {
        SubAccount subAccount = subAccountService.getById(id);
        if (subAccount == null) {
            return Result.error(404, "子账户不存在");
        }
        return Result.success(SubAccountResp.fromEntity(subAccount));
    }

    /**
     * 按 main_account_id 查询子账户列表
     *
     * @param mainAccountId 主账户 ID（路径参数）
     * @return 该主账户下的子账户列表
     */
    @GetMapping("/by-main-account/{mainAccountId}")
    public Result<List<SubAccountResp>> listByMainAccount(@PathVariable Long mainAccountId) {
        List<SubAccount> list = subAccountService.listByMainAccountId(mainAccountId);
        List<SubAccountResp> records = list.stream()
                .map(SubAccountResp::fromEntity)
                .collect(Collectors.toList());
        return Result.success(records);
    }

    /**
     * 分页查询子账户列表
     *
     * @param req 分页参数，包含 mainAccountId（可选过滤）、pageNumber（默认1）、pageSize（默认20）
     * @return 分页结果，包含 records、totalRow、pageNumber、pageSize
     */
    @GetMapping
    public Result<Map<String, Object>> page(SubAccountPageReq req) {
        Page<SubAccount> page = subAccountService.page(req.getPageNumber(), req.getPageSize(), req.getMainAccountId());
        List<SubAccountResp> records = page.getRecords().stream()
                .map(SubAccountResp::fromEntity)
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("totalRow", page.getTotalRow());
        result.put("pageNumber", page.getPageNumber());
        result.put("pageSize", page.getPageSize());
        return Result.success(result);
    }
}
