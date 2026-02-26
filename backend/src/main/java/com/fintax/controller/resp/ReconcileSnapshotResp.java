package com.fintax.controller.resp;

import com.fintax.entity.AssetReconcileSnapshot;
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
public class ReconcileSnapshotResp {

    private Long id;

    private Long mainAccountId;

    private Long subAccountId;

    private String subAccountName;

    private String currency;

    private BigDecimal systemTotal;

    private BigDecimal onchainBalance;

    private BigDecimal diff;

    private BigDecimal diffAbs;

    private String diffStatus;

    private LocalDateTime snapshotTime;

    private LocalDateTime createdAt;

    public static ReconcileSnapshotResp fromEntity(AssetReconcileSnapshot snapshot) {
        return ReconcileSnapshotResp.builder()
                .id(snapshot.getId())
                .mainAccountId(snapshot.getMainAccountId())
                .subAccountId(snapshot.getSubAccountId())
                .currency(snapshot.getCurrency())
                .systemTotal(snapshot.getSystemTotal())
                .onchainBalance(snapshot.getOnchainBalance())
                .diff(snapshot.getDiff())
                .diffAbs(snapshot.getDiffAbs())
                .diffStatus(snapshot.getDiffStatus())
                .snapshotTime(snapshot.getSnapshotTime())
                .createdAt(snapshot.getCreatedAt())
                .build();
    }
}
