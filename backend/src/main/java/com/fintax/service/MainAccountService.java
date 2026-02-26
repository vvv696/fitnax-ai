package com.fintax.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.fintax.entity.MainAccount;
import com.fintax.mapper.MainAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.fintax.entity.table.MainAccountTableDef.MAIN_ACCOUNT;

@Service
@RequiredArgsConstructor
public class MainAccountService {

    private final MainAccountMapper mainAccountMapper;

    /**
     * 创建主账户
     */
    @Transactional
    public MainAccount create(MainAccount account) {
        account.setIsDeleted(false);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        mainAccountMapper.insert(account);
        return account;
    }

    /**
     * 更新主账户
     */
    @Transactional
    public MainAccount update(MainAccount account) {
        account.setUpdatedAt(LocalDateTime.now());
        mainAccountMapper.update(account);
        return account;
    }

    /**
     * 软删除主账户
     */
    @Transactional
    public void delete(Long id) {
        MainAccount account = mainAccountMapper.selectOneById(id);
        if (account != null) {
            account.setIsDeleted(true);
            account.setUpdatedAt(LocalDateTime.now());
            mainAccountMapper.update(account);
        }
    }

    /**
     * 根据 ID 查询（排除已删除）
     */
    public MainAccount getById(Long id) {
        QueryWrapper query = QueryWrapper.create()
                .where(MAIN_ACCOUNT.ID.eq(id))
                .and(MAIN_ACCOUNT.IS_DELETED.eq(false));
        return mainAccountMapper.selectOneByQuery(query);
    }

    /**
     * 分页查询（排除已删除）
     */
    public Page<MainAccount> page(int pageNumber, int pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .where(MAIN_ACCOUNT.IS_DELETED.eq(false))
                .orderBy(MAIN_ACCOUNT.CREATED_AT.desc());
        return mainAccountMapper.paginate(Page.of(pageNumber, pageSize), query);
    }

    /**
     * 查询全部（排除已删除）
     */
    public List<MainAccount> listAll() {
        QueryWrapper query = QueryWrapper.create()
                .where(MAIN_ACCOUNT.IS_DELETED.eq(false))
                .orderBy(MAIN_ACCOUNT.CREATED_AT.desc());
        return mainAccountMapper.selectListByQuery(query);
    }
}
