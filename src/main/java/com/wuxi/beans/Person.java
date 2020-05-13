package com.wuxi.beans;

import lombok.Data;

import java.util.List;

@Data
public class Person {
    private long id;

    private String name;

    private List<Person> children;
}
