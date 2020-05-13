package com.wuxi.tools.es;

import com.google.gson.Gson;
import com.wuxi.beans.Person;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class EsClientTest {

    private EsClient client;
    private Gson gson = new Gson();
    @Before
    public void init(){
        client = new EsClient("prophet45.dg.163.org", 84500);
    }

    @After
    public void after(){
        client.close();
    }

    @Test
    public void index() {
        List<Object> list = new ArrayList<>();
        Person person =  new Person();
        person.setId(1001L);
        person.setName("张三");
        list.add(person);

        Person person2 =  new Person();
        person2.setId(1002L);
        person2.setName("李四");
        list.add(person2);

        Person person3 =  new Person();
        person3.setId(1003L);
        person3.setName("王麻子");
        list.add(person3);

        client.index(list, "es-client-test-123", "position");
    }

    @Test
    public void deleteIndex() {
        client.deleteIndex("es-client-test-123");
    }

    @Test
    public void deleteByDocIds() {
        List<String> ids = new LinkedList<>();
        ids.add("AW5tObs2wxgx8wJXb96V");
        client.deleteByDocIds("es-client-test-123", "position", ids);
    }

    @Test
    public void query() {
        BoolQueryBuilder boolQueryBuilder =  QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("age", 13));
        client.query("es-client-test-123", boolQueryBuilder, 3, "name", SortOrder.DESC);
    }
}
