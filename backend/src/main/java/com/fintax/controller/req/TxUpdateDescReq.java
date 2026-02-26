package com.fintax.controller.req;

import lombok.Data;

@Data
public class TxUpdateDescReq {

    private String txHash;

    private String description;
}
