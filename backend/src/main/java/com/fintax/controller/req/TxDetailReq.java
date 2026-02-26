package com.fintax.controller.req;

import lombok.Data;

@Data
public class TxDetailReq {

    private String txHash;

    private Long mainAccountId;

    private Long subAccountId;
}
