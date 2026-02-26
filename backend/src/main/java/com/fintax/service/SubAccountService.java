package com.fintax.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.fintax.entity.SubAccount;
import com.fintax.mapper.SubAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.fintax.entity.table.SubAccountTableDef.SUB_ACCOUNT;

@Service
@RequiredArgsConstructor
public class SubAccountService {

    private final SubAccountMapper subAccountMapper;

    /**
     * 创建子账户
     */
    @Transactional
    public SubAccount create(SubAccount subAccount) {
        subAccount.setIsDeleted(false);
        subAccount.setCreatedAt(LocalDateTime.now());
        subAccount.setUpdatedAt(LocalDateTime.now());
        subAccountMapper.insert(subAccount);
        return subAccount;
    }

    /**
     * 更新子账户
     */
    @Transactional
    public SubAccount update(SubAccount subAccount) {
        subAccount.setUpdatedAt(LocalDateTime.now());
        subAccountMapper.update(subAccount);
        return subAccount;
    }

    /**
     * 软删除子账户
     */
    @Transactional
    public void delete(Long id) {
        SubAccount subAccount = subAccountMapper.selectOneById(id);
        if (subAccount != null) {
            subAccount.setIsDeleted(true);
            subAccount.setUpdatedAt(LocalDateTime.now());
            subAccountMapper.update(subAccount);
        }
    }

    /**
     * 根据 ID 查询（排除已删除）
     */
    public SubAccount getById(Long id) {
        QueryWrapper query = QueryWrapper.create()
                .where(SUB_ACCOUNT.ID.eq(id))
                .and(SUB_ACCOUNT.IS_DELETED.eq(false));
        return subAccountMapper.selectOneByQuery(query);
    }

    /**
     * 按 main_account_id 查询子账户列表（排除已删除）
     */
    public List<SubAccount> listByMainAccountId(Long mainAccountId) {
        QueryWrapper query = QueryWrapper.create()
                .where(SUB_ACCOUNT.MAIN_ACCOUNT_ID.eq(mainAccountId))
                .and(SUB_ACCOUNT.IS_DELETED.eq(false))
                .orderBy(SUB_ACCOUNT.CREATED_AT.desc());
        return subAccountMapper.selectListByQuery(query);
    }

    /**
     * 分页查询（排除已删除）
     */
    public Page<SubAccount> page(int pageNumber, int pageSize, Long mainAccountId) {
        QueryWrapper query = QueryWrapper.create()
                .where(SUB_ACCOUNT.IS_DELETED.eq(false));
        if (mainAccountId != null) {
            query.and(SUB_ACCOUNT.MAIN_ACCOUNT_ID.eq(mainAccountId));
        }
        query.orderBy(SUB_ACCOUNT.CREATED_AT.desc());
        return subAccountMapper.paginate(Page.of(pageNumber, pageSize), query);
    }

    /**
     * 检查某主账户下是否存在未删除的子账户
     */
    public boolean existsByMainAccountId(Long mainAccountId) {
        QueryWrapper query = QueryWrapper.create()
                .where(SUB_ACCOUNT.MAIN_ACCOUNT_ID.eq(mainAccountId))
                .and(SUB_ACCOUNT.IS_DELETED.eq(false));
        return subAccountMapper.selectCountByQuery(query) > 0;
    }
}
