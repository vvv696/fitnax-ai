package com.fintax.controller.req;

import lombok.Data;

@Data
public class MainAccountCreateReq {

    private String accountName;

    private String accountType;

    private String accountSource;

    private String address;

    private String status;

    private String organization;
}
