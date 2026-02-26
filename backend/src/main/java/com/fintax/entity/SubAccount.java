package com.fintax.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("sub_account")
public class SubAccount {

    @Id(keyType = KeyType.Auto)
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

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
