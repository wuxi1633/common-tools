package com.sparkor.tools.es;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sparkor.beans.Person;
import com.sparkor.tools.excel.ExcelClient;
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
    private ExcelClient excelClient = new ExcelClient();
    private EsClient client;
    private Gson gson = new Gson();
    @Before
    public void init(){

        // online
        client = new EsClient("310.32200.11228.221", 70200);
        // test
//        client = new EsClient("1041.200.3164.2109", 18200);
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
        long phoneStart = 10000000000L;
        long timeStart = System.currentTimeMillis() - 365L * 24 * 3600 * 1000;
        for (int i = 10; i < 13; i++) {
            List<Object> list = new ArrayList<>();
            for (int j = 10000; j < 20000; j++) {
                JsonObject jsonObject =  new JsonObject();
                jsonObject.addProperty("rewardName", "三网话费");
                jsonObject.addProperty("rewardId", "100018");
                jsonObject.addProperty("phone", phoneStart++);
                jsonObject.addProperty("time", timeStart);
                timeStart += 1000;
                jsonObject.addProperty("type", 1);
                jsonObject.addProperty("quantity", 1);
                jsonObject.addProperty("price", 1);
                jsonObject.addProperty("id", "jifen" + i*j);
                jsonObject.addProperty("name", "李武曦" + i*j);
                jsonObject.addProperty("idNumber", timeStart);
                list.add(jsonObject);

            }
            client.index(list, "position_reward_history", "position");
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
        boolQueryBuilder.must(QueryBuilders.termQuery("id",22113));
        boolQueryBuilder.must(QueryBuilders.termQuery("channel","33010"));
//        boolQueryBuilder.must(QueryBuilders.termQuery("event",2));
        client.queryToExcel("position_survey_user_event", boolQueryBuilder, "C:\\Users\\liwuxi\\Desktop\\target\\data", "createTime");
    }

    @Test
    public void queryWithIds() {
        Set<String> ids = new HashSet<>();
        List<JsonObject> sheetData = excelClient.readSheet("C:\\Users\\liwuxi\\Desktop\\酒店项目-携程部分-0207完整版.xlsx", 0, 3);
        sheetData.forEach(e -> {
            JsonElement element = e.get("来源详情");
            if(null != element){
                ids.add(element.getAsString().trim());
            }
        });
        List<JsonObject> jsonObjectList = client.queryInIds("ques_backflow_params_data", "data_params", ids);
        excelClient.writeExcel(jsonObjectList, "C:\\Users\\liwuxi\\Desktop\\meituano2.xlsx", "", new HashMap<>());
    }
}
