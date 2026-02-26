package com.fintax.controller.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResp {

    private int successCount;

    private int failCount;

    private List<FailRow> failRows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailRow {
        private int rowNo;
        private String reason;
    }
}
