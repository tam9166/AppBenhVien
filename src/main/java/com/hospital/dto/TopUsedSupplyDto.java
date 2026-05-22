package com.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO biểu diễn vật tư được dùng nhiều trong báo cáo.
 */
@Getter
@AllArgsConstructor
public class TopUsedSupplyDto {
    private String supplyName;
    private Long totalQuantity;
}
