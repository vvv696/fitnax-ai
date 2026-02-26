package com.fintax.controller.req;

import lombok.Data;

@Data
public class SubAccountCreateReq {

    private Long mainAccountId;

    private String subAccountName;

    private String category;

    private String accountSource;

    private String importType;

    private String address;

    private String status;
}
