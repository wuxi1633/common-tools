package com.wuxi.common.beans;

import lombok.Data;

import java.util.List;

@Data
public class person {
    private long id;

    private String name;

    private List<person> children;
}
