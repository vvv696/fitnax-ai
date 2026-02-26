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
public class TxGroupResp {

    private String txHash;

    /** Send / Receive / Mixed */
    private String category;

    private String businessType;

    private String fromAddress;

    private String toAddress;

    private String accountName;

    private LocalDateTime tradeTime;

    private BigDecimal inflow;

    private BigDecimal outflow;

    private BigDecimal fee;

    private String chainName;

    private String description;

    private int detailCount;
}
