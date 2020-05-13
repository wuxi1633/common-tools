package com.wuxi.tools.common;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.wuxi.beans.Person;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class SortUtilsTest {
    private Gson gson = new Gson();
    private JsonParser parser = new JsonParser();

    @Test
    public void sort() {
        List<Person> result = new LinkedList<>();
        Person roA1 = new Person();
        roA1.setName("abc");
        result.add(roA1);

        Person roA2 = new Person();
        roA2.setName("bcd");
        result.add(roA2);

        Person roA3 = new Person();
        roA3.setName("xyz");
        result.add(roA3);


        Person roD1 = new Person();
        roD1.setName("123");
        result.add(roD1);

        Person roD2 = new Person();
        roD2.setName("231");
        result.add(roD2);

        Person roD3 = new Person();
        roD3.setName("321");
        result.add(roD3);

        Person roC1 = new Person();
        roC1.setName("阿里巴巴");
        result.add(roC1);

        Person roC2 = new Person();
        roC2.setName("腾讯");
        result.add(roC2);

        Person roC3 = new Person();
        roC3.setName("中国");
        result.add(roC3);

        List<Person> parent = new LinkedList<>();
        Person pa = new Person();
        pa.setName("abc");
        pa.setChildren(result);
        parent.add(pa);

        SortUtils.sort(parent, "name", "children");

        parent.get(0).getChildren().forEach(e -> System.out.println(parser.parse(gson.toJson(e)).getAsJsonObject().get("name").getAsString()));
    }

    @Test
    public void sort1() {
        List<Person> result = new LinkedList<>();
        Person roA1 = new Person();
        roA1.setName("abc");
        result.add(roA1);

        Person roA2 = new Person();
        roA2.setName("bcd");
        result.add(roA2);

        Person roA3 = new Person();
        roA3.setName("xyz");
        result.add(roA3);


        Person roD1 = new Person();
        roD1.setName("123");
        result.add(roD1);

        Person roD2 = new Person();
        roD2.setName("231");
        result.add(roD2);

        Person roD3 = new Person();
        roD3.setName("321");
        result.add(roD3);

        Person roC1 = new Person();
        roC1.setName("阿里巴巴");
        result.add(roC1);

        Person roC2 = new Person();
        roC2.setName("腾讯");
        result.add(roC2);

        Person roC3 = new Person();
        roC3.setName("中国");
        result.add(roC3);

        SortUtils.sort(result, "name");

        result.forEach(e -> System.out.println(parser.parse(gson.toJson(e)).getAsJsonObject().get("name").getAsString()));
    }
}
