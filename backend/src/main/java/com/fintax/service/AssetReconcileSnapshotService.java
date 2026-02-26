package com.fintax.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.fintax.entity.AssetReconcileSnapshot;
import com.fintax.mapper.AssetReconcileSnapshotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.fintax.entity.table.AssetReconcileSnapshotTableDef.ASSET_RECONCILE_SNAPSHOT;

@Service
@RequiredArgsConstructor
public class AssetReconcileSnapshotService {

    private final AssetReconcileSnapshotMapper snapshotMapper;

    /**
     * 保存对账快照
     */
    @Transactional
    public AssetReconcileSnapshot save(AssetReconcileSnapshot snapshot) {
        snapshot.setCreatedAt(LocalDateTime.now());
        snapshot.setUpdatedAt(LocalDateTime.now());
        snapshotMapper.insert(snapshot);
        return snapshot;
    }

    /**
     * 批量保存对账快照
     */
    @Transactional
    public void batchSave(List<AssetReconcileSnapshot> snapshots) {
        snapshotMapper.insertBatch(snapshots);
    }

    /**
     * 分页查询对账快照
     */
    public Page<AssetReconcileSnapshot> page(int pageNumber, int pageSize,
                                              Long mainAccountId, Long subAccountId,
                                              String currency,
                                              LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper query = QueryWrapper.create();
        if (mainAccountId != null) {
            query.and(ASSET_RECONCILE_SNAPSHOT.MAIN_ACCOUNT_ID.eq(mainAccountId));
        }
        if (subAccountId != null) {
            query.and(ASSET_RECONCILE_SNAPSHOT.SUB_ACCOUNT_ID.eq(subAccountId));
        }
        if (currency != null && !currency.isEmpty()) {
            query.and(ASSET_RECONCILE_SNAPSHOT.CURRENCY.eq(currency));
        }
        if (startTime != null) {
            query.and(ASSET_RECONCILE_SNAPSHOT.SNAPSHOT_TIME.ge(startTime));
        }
        if (endTime != null) {
            query.and(ASSET_RECONCILE_SNAPSHOT.SNAPSHOT_TIME.le(endTime));
        }
        query.orderBy(ASSET_RECONCILE_SNAPSHOT.SNAPSHOT_TIME.desc());
        return snapshotMapper.paginate(Page.of(pageNumber, pageSize), query);
    }

    /**
     * 导出查询：按条件查询全量数据（不分页）
     */
    public List<AssetReconcileSnapshot> listForExport(Long mainAccountId, Long subAccountId,
                                                       String currency,
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper query = QueryWrapper.create();
        if (mainAccountId != null) {
            query.and(ASSET_RECONCILE_SNAPSHOT.MAIN_ACCOUNT_ID.eq(mainAccountId));
        }
        if (subAccountId != null) {
            query.and(ASSET_RECONCILE_SNAPSHOT.SUB_ACCOUNT_ID.eq(subAccountId));
        }
        if (currency != null && !currency.isEmpty()) {
            query.and(ASSET_RECONCILE_SNAPSHOT.CURRENCY.eq(currency));
        }
        if (startTime != null) {
            query.and(ASSET_RECONCILE_SNAPSHOT.SNAPSHOT_TIME.ge(startTime));
        }
        if (endTime != null) {
            query.and(ASSET_RECONCILE_SNAPSHOT.SNAPSHOT_TIME.le(endTime));
        }
        query.orderBy(ASSET_RECONCILE_SNAPSHOT.SNAPSHOT_TIME.desc());
        return snapshotMapper.selectListByQuery(query);
    }
}
