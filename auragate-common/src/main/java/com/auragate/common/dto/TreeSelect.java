package com.auragate.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 树结构实体类
 */
@Data
public class TreeSelect {
    private Long id;
    private String label;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelect> children;

    public TreeSelect() {}

    public TreeSelect(Long id, String label, List<TreeSelect> children) {
        this.id = id;
        this.label = label;
        this.children = children;
    }
}
