package com.sparkor.tools.es;

import com.google.gson.Gson;
import com.sparkor.beans.Person;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    /**
     * curl PUT IP:PORT/INDEX
     * {
     *   "mappings":{
     *     "position": {
     *       "properties": {
     *         "name": {
     *           "type": "keyword"
     *         },
     *         "code": {
     *           "type": "keyword"
     *         },
     *         "id": {
     *           "type": "long"
     *         }
     *       }
     *     }
     *   }
     * }
     */
    @Test
    @Deprecated
    public void createIndex(){
        String mapStr = "{\n" +
                "    \"position\": {\n" +
                "      \"properties\": {\n" +
                "        \"app\": {\n" +
                "          \"type\": \"keyword\"\n" +
                "        },\n" +
                "        \"code\": {\n" +
                "          \"type\": \"keyword\"\n" +
                "        },\n" +
                "        \"createTime\": {\n" +
                "          \"type\": \"long\"\n" +
                "        },\n" +
                "        \"endQuestion\": {\n" +
                "          \"type\": \"keyword\"\n" +
                "        },\n" +
                "        \"event\": {\n" +
                "          \"type\": \"long\"\n" +
                "        },\n" +
                "        \"ext\": {\n" +
                "          \"type\": \"keyword\"\n" +
                "        },\n" +
                "        \"id\": {\n" +
                "          \"type\": \"keyword\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }";
        Map<String, Object> mapping = gson.fromJson(mapStr, Map.class);
        client.createIndex("es-client-test-123", "position", mapping);
    }

    @Test
    public void index() {

        // mock 100万数据
        for (int i = 10; i < 20; i++) {
            List<Object> list = new ArrayList<>();
            for (int j = 10000; j < 20000; j++) {

                Person person =  new Person();
                person.setId(i * j);
                person.setName("张三" + person.getId());
                person.setCode(String.valueOf(System.currentTimeMillis()));
                list.add(person);

            }
            client.index(list, "es-client-test-123", "position");
        }

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
