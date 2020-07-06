package com.sparkor.beans;


import lombok.Data;

import java.util.List;

@Data
public class Person {
    private long id;

    private String name;

    private String code;

    private List<Person> children;
}
