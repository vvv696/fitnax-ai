package com.fintax.controller.req;

import lombok.Data;

@Data
public class TxUpdateBusinessTypeReq {

    private String txHash;

    private String businessType;
}
