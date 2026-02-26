package com.fintax.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("asset_reconcile_snapshot")
public class AssetReconcileSnapshot {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long mainAccountId;

    private Long subAccountId;

    private String currency;

    private BigDecimal systemTotal;

    private BigDecimal onchainBalance;

    private BigDecimal diff;

    private BigDecimal diffAbs;

    private String diffStatus;

    private LocalDateTime snapshotTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
