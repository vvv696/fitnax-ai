package com.fintax.controller.req;

import lombok.Data;

@Data
public class SubAccountPageReq {

    private Long mainAccountId;

    private Integer pageNumber = 1;

    private Integer pageSize = 20;
}
