package com.fintax.controller.req;

import lombok.Data;

@Data
public class ReconcilePageReq {

    private Long mainAccountId;

    private Long subAccountId;

    private String currency;

    private String startTime;

    private String endTime;

    private Integer pageNumber = 1;

    private Integer pageSize = 20;
}
