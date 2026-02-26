package com.fintax.controller.resp;

import com.fintax.entity.MainAccount;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MainAccountResp {

    private Long id;

    private String accountName;

    private String accountType;

    private String accountSource;

    private String address;

    private BigDecimal marketValue;

    private String status;

    private String organization;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static MainAccountResp fromEntity(MainAccount entity) {
        MainAccountResp resp = new MainAccountResp();
        resp.setId(entity.getId());
        resp.setAccountName(entity.getAccountName());
        resp.setAccountType(entity.getAccountType());
        resp.setAccountSource(entity.getAccountSource());
        resp.setAddress(entity.getAddress());
        resp.setMarketValue(entity.getMarketValue());
        resp.setStatus(entity.getStatus());
        resp.setOrganization(entity.getOrganization());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setUpdatedAt(entity.getUpdatedAt());
        return resp;
    }
}
