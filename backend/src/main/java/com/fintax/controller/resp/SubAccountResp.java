package com.fintax.controller.resp;

import com.fintax.entity.SubAccount;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubAccountResp {

    private Long id;

    private Long mainAccountId;

    private String subAccountName;

    private String category;

    private String accountSource;

    private String importType;

    private String importFileName;

    private String importBatchId;

    private String address;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static SubAccountResp fromEntity(SubAccount entity) {
        SubAccountResp resp = new SubAccountResp();
        resp.setId(entity.getId());
        resp.setMainAccountId(entity.getMainAccountId());
        resp.setSubAccountName(entity.getSubAccountName());
        resp.setCategory(entity.getCategory());
        resp.setAccountSource(entity.getAccountSource());
        resp.setImportType(entity.getImportType());
        resp.setImportFileName(entity.getImportFileName());
        resp.setImportBatchId(entity.getImportBatchId());
        resp.setAddress(entity.getAddress());
        resp.setStatus(entity.getStatus());
        resp.setCreatedAt(entity.getCreatedAt());
        resp.setUpdatedAt(entity.getUpdatedAt());
        return resp;
    }
}
