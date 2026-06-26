package com.auragate.common.dto;

import lombok.Data;

import java.util.List;

/**
 * 表格分页数据对象
 */
@Data
public class PageResult {
    private Long total;
    private List<?> rows;
    private int code;
    private String msg;
}
