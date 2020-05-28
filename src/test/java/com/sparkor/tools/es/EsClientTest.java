package com.sparkor.tools.es;

import com.google.gson.Gson;
import com.sparkor.beans.Person;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ml.job.results.Bucket;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;

public class EsClientTest {

    private EsClient client;
    private Gson gson = new Gson();
    @Before
    public void init(){
        client = new EsClient("demo", 7000);
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
    public void testAggs(){
        String index = "position_survey_user_event";
        BoolQueryBuilder boolQueryBuilder =  QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("event", 1));
//        boolQueryBuilder.must(QueryBuilders.rangeQuery("createTime").gte(1580894040000L).lte(1580894160000L));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        AggregationBuilder aggBuilder =  AggregationBuilders.dateHistogram("time").field("createTime").dateHistogramInterval(DateHistogramInterval.MINUTE);

        SearchResponse response = client.aggs(index, boolQueryBuilder, aggBuilder);

        ParsedDateHistogram times = response.getAggregations().get("time");

        times.getBuckets().sort((Comparator<Histogram.Bucket>) (o1, o2) -> {
            if(o1.getDocCount() > o2.getDocCount()){
                return 1;
            } else if(o1.getDocCount() < o2.getDocCount()){
                return -1;
            }
            return 0;
        });

        times.getBuckets().forEach(e -> {
            System.out.println(e.getKeyAsString() + "\ttime: " + sdf.format(Long.valueOf(e.getKeyAsString())) + "\tcount: " + e.getDocCount());
        });

        System.out.println(times.getBuckets().size());

        int[] countHours = new int[25];
        times.getBuckets().forEach(e -> {
            countHours[new Date(Long.valueOf(e.getKeyAsString())).getHours()] += e.getDocCount();
        });

        for (int i = 0; i < countHours.length; i++) {
            System.out.println("hour: " + i + ", count: " + countHours[i]);
        }
    }

    @Test
    public void query() {
        BoolQueryBuilder boolQueryBuilder =  QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("event", 1));
        List<Map<String, Object>> maps = client.query("position_survey_user_event", boolQueryBuilder, 3, "createTime", SortOrder.DESC);
        if(null != maps){
            maps.forEach(e -> System.out.println(gson.toJson(e)));
        }
    }

    @Test
    public void queryToExcel() {
        BoolQueryBuilder boolQueryBuilder =  QueryBuilders.boolQuery();
        client.queryToExcel("userpathde", boolQueryBuilder, "/Users/liwuxi/Desktop/target/data", "");
    }
}
