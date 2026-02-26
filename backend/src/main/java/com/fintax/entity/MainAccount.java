package com.fintax.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("main_account")
public class MainAccount {

    @Id(keyType = KeyType.Auto)
    private Long id;

    private String accountName;

    private String accountType;

    private String accountSource;

    private String address;

    private BigDecimal marketValue;

    private String status;

    private String organization;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
