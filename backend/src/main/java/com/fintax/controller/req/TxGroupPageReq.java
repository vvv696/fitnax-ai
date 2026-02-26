package com.fintax.controller.req;

import lombok.Data;

@Data
public class TxGroupPageReq {

    private Long mainAccountId;

    private Long subAccountId;

    private String txHash;

    private String direction;

    private String category;

    private String chainName;

    private String startTime;

    private String endTime;

    private Integer pageNumber = 1;

    private Integer pageSize = 20;
}
