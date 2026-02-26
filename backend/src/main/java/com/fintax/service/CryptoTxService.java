package com.fintax.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.fintax.entity.CryptoTx;
import com.fintax.mapper.CryptoTxMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.fintax.entity.table.CryptoTxTableDef.CRYPTO_TX;
import static com.mybatisflex.core.query.QueryMethods.max;

@Service
@RequiredArgsConstructor
public class CryptoTxService {

    private final CryptoTxMapper cryptoTxMapper;

    /**
     * 按条件分页查询交易
     */
    public Page<CryptoTx> page(int pageNumber, int pageSize,
                                Long mainAccountId, Long subAccountId,
                                String txHash, String direction,
                                LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper query = buildFilterQuery(mainAccountId, subAccountId, txHash, direction, startTime, endTime);
        query.orderBy(CRYPTO_TX.TRADE_TIME.desc());
        return cryptoTxMapper.paginate(Page.of(pageNumber, pageSize), query);
    }

    /**
     * 按 tx_hash 查询明细列表
     */
    public List<CryptoTx> listByTxHash(String txHash) {
        QueryWrapper query = QueryWrapper.create()
                .where(CRYPTO_TX.TX_HASH.eq(txHash))
                .and(CRYPTO_TX.IS_DELETED.eq(false))
                .orderBy(CRYPTO_TX.TRADE_TIME.asc());
        return cryptoTxMapper.selectListByQuery(query);
    }

    /**
     * 按 tx_hash + sub_account_id 查询明细
     */
    public List<CryptoTx> listByTxHashAndSubAccount(String txHash, Long subAccountId) {
        QueryWrapper query = QueryWrapper.create()
                .where(CRYPTO_TX.TX_HASH.eq(txHash))
                .and(CRYPTO_TX.SUB_ACCOUNT_ID.eq(subAccountId))
                .and(CRYPTO_TX.IS_DELETED.eq(false))
                .orderBy(CRYPTO_TX.TRADE_TIME.asc());
        return cryptoTxMapper.selectListByQuery(query);
    }

    /**
     * 按 tx_hash + main_account_id 查询明细
     */
    public List<CryptoTx> listByTxHashAndMainAccount(String txHash, Long mainAccountId) {
        QueryWrapper query = QueryWrapper.create()
                .where(CRYPTO_TX.TX_HASH.eq(txHash))
                .and(CRYPTO_TX.MAIN_ACCOUNT_ID.eq(mainAccountId))
                .and(CRYPTO_TX.IS_DELETED.eq(false))
                .orderBy(CRYPTO_TX.TRADE_TIME.asc());
        return cryptoTxMapper.selectListByQuery(query);
    }

    /**
     * 检查某主账户下是否存在未删除的交易
     */
    public boolean existsByMainAccountId(Long mainAccountId) {
        QueryWrapper query = QueryWrapper.create()
                .where(CRYPTO_TX.MAIN_ACCOUNT_ID.eq(mainAccountId))
                .and(CRYPTO_TX.IS_DELETED.eq(false));
        return cryptoTxMapper.selectCountByQuery(query) > 0;
    }

    /**
     * 批量插入交易记录
     */
    public void batchInsert(List<CryptoTx> txList) {
        cryptoTxMapper.insertBatch(txList);
    }

    /**
     * 按 sub_account_id 和 currency 查询（用于对账计算）
     */
    public List<CryptoTx> listBySubAccountAndCurrency(Long subAccountId, String currency) {
        QueryWrapper query = QueryWrapper.create()
                .where(CRYPTO_TX.SUB_ACCOUNT_ID.eq(subAccountId))
                .and(CRYPTO_TX.CURRENCY.eq(currency))
                .and(CRYPTO_TX.IS_DELETED.eq(false));
        return cryptoTxMapper.selectListByQuery(query);
    }

    /**
     * 查询去重的 tx_hash 列表（分页），用于 Tx Group 聚合
     */
    public Page<CryptoTx> pageDistinctTxHash(int pageNumber, int pageSize,
                                              Long mainAccountId, Long subAccountId,
                                              String txHash, String direction,
                                              String category, String chainName,
                                              LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper query = QueryWrapper.create()
                .select(CRYPTO_TX.TX_HASH)
                .where(CRYPTO_TX.IS_DELETED.eq(false));
        if (mainAccountId != null) {
            query.and(CRYPTO_TX.MAIN_ACCOUNT_ID.eq(mainAccountId));
        }
        if (subAccountId != null) {
            query.and(CRYPTO_TX.SUB_ACCOUNT_ID.eq(subAccountId));
        }
        if (txHash != null && !txHash.isEmpty()) {
            query.and(CRYPTO_TX.TX_HASH.eq(txHash));
        }
        if (direction != null && !direction.isEmpty()) {
            query.and(CRYPTO_TX.DIRECTION.eq(direction));
        }
        if (category != null && !category.isEmpty()) {
            query.and(CRYPTO_TX.CATEGORY.eq(category));
        }
        if (chainName != null && !chainName.isEmpty()) {
            query.and(CRYPTO_TX.CHAIN_NAME.eq(chainName));
        }
        if (startTime != null) {
            query.and(CRYPTO_TX.TRADE_TIME.ge(startTime));
        }
        if (endTime != null) {
            query.and(CRYPTO_TX.TRADE_TIME.le(endTime));
        }
        query.groupBy(CRYPTO_TX.TX_HASH)
             .orderBy(max(CRYPTO_TX.TRADE_TIME).desc());
        return cryptoTxMapper.paginate(Page.of(pageNumber, pageSize), query);
    }

    /**
     * 按 tx_hash 批量更新 description
     */
    @Transactional
    public int updateDescriptionByTxHash(String txHash, String description) {
        CryptoTx update = new CryptoTx();
        update.setDescription(description);
        update.setUpdatedAt(LocalDateTime.now());
        QueryWrapper query = QueryWrapper.create()
                .where(CRYPTO_TX.TX_HASH.eq(txHash))
                .and(CRYPTO_TX.IS_DELETED.eq(false));
        return cryptoTxMapper.updateByQuery(update, query);
    }

    /**
     * 按 tx_hash 批量更新 business_type
     */
    @Transactional
    public int updateBusinessTypeByTxHash(String txHash, String businessType) {
        CryptoTx update = new CryptoTx();
        update.setBusinessType(businessType);
        update.setUpdatedAt(LocalDateTime.now());
        QueryWrapper query = QueryWrapper.create()
                .where(CRYPTO_TX.TX_HASH.eq(txHash))
                .and(CRYPTO_TX.IS_DELETED.eq(false));
        return cryptoTxMapper.updateByQuery(update, query);
    }

    /**
     * 更新单条交易的 description
     */
    @Transactional
    public int updateDescriptionById(Long id, String description) {
        CryptoTx existing = cryptoTxMapper.selectOneById(id);
        if (existing == null || existing.getIsDeleted()) return 0;
        existing.setDescription(description);
        existing.setUpdatedAt(LocalDateTime.now());
        return cryptoTxMapper.update(existing);
    }

    /**
     * 更新单条交易的 business_type
     */
    @Transactional
    public int updateBusinessTypeById(Long id, String businessType) {
        CryptoTx existing = cryptoTxMapper.selectOneById(id);
        if (existing == null || existing.getIsDeleted()) return 0;
        existing.setBusinessType(businessType);
        existing.setUpdatedAt(LocalDateTime.now());
        return cryptoTxMapper.update(existing);
    }

    private QueryWrapper buildFilterQuery(Long mainAccountId, Long subAccountId,
                                           String txHash, String direction,
                                           LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper query = QueryWrapper.create()
                .where(CRYPTO_TX.IS_DELETED.eq(false));
        if (mainAccountId != null) {
            query.and(CRYPTO_TX.MAIN_ACCOUNT_ID.eq(mainAccountId));
        }
        if (subAccountId != null) {
            query.and(CRYPTO_TX.SUB_ACCOUNT_ID.eq(subAccountId));
        }
        if (txHash != null && !txHash.isEmpty()) {
            query.and(CRYPTO_TX.TX_HASH.eq(txHash));
        }
        if (direction != null && !direction.isEmpty()) {
            query.and(CRYPTO_TX.DIRECTION.eq(direction));
        }
        if (startTime != null) {
            query.and(CRYPTO_TX.TRADE_TIME.ge(startTime));
        }
        if (endTime != null) {
            query.and(CRYPTO_TX.TRADE_TIME.le(endTime));
        }
        return query;
    }
}
