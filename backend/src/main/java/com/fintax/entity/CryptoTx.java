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
@Table("crypto_tx")
public class CryptoTx {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long mainAccountId;

    private Long subAccountId;

    private String txHash;

    private String direction;

    private String category;

    private String businessType;

    private String currency;

    private BigDecimal amount;

    private String fromAddress;

    private String toAddress;

    private BigDecimal fee;

    private String feeCurrency;

    private String chainName;

    private LocalDateTime tradeTime;

    private String description;

    private String importBatchId;

    private Integer rowNo;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
