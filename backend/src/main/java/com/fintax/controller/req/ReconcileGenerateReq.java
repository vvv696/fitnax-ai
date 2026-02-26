package com.fintax.controller.req;

import lombok.Data;

@Data
public class ReconcileGenerateReq {

    private Long mainAccountId;

    private Long subAccountId;

    private String snapshotTime;
}
