package com.fintax.controller.resp;

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
public class TxDetailResp {

    private Long id;

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

    private Long mainAccountId;

    private Long subAccountId;
}
