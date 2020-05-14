package com.wuxi.tools.es;

import com.google.gson.Gson;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EsClient implements Closeable {

    private RestHighLevelClient client;

    private Gson gson = new Gson();

    private RequestOptions DEFAULT_OPTIONS = RequestOptions.DEFAULT.toBuilder().build();

    @Override
    public void close(){
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public EsClient(String ip, int port){
        this.client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(ip, port, "http")));
    }

    public void createIndex(String index, String type, Map<String, Object> mapping){
        Settings settings = Settings.builder()
                .put("index.number_of_shards", 5)
                .put("index.number_of_replicas", 0)
                .build();
        CreateIndexRequest request = new CreateIndexRequest(index);
        request.settings(settings);
        request.mapping(type, mapping);
        try {
            client.indices().create(request, DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void index(List<Object> data, String index, String type){
        if(ObjectUtils.isEmpty(data)){
            System.out.println("empty data!!!");
            return;
        }
        BulkRequest bulkRequest = new BulkRequest();
        data.forEach(e -> {
            IndexRequest request = new IndexRequest().index(index).type(type).source(gson.toJson(e), XContentType.JSON);
            bulkRequest.add(request);
        });
        bulk(bulkRequest, data.size());
    }

    public void deleteIndex(String index){
        DeleteIndexRequest request = new DeleteIndexRequest(index);
        try {
            AcknowledgedResponse response = client.indices().delete(request);
            System.out.println(gson.toJson(response));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteByDocIds(String index, String type, List<String> ids){
        if(ObjectUtils.isEmpty(ids)){
            System.out.println("empty ids!!!");
            return;
        }
        BulkRequest bulkRequest = new BulkRequest();
        ids.forEach(e -> {
            DeleteRequest request = new DeleteRequest().index(index).type(type).id(e);
            bulkRequest.add(request);
        });
        bulk(bulkRequest, ids.size());
    }

    private void bulk(BulkRequest bulkRequest, int size) {
        try {
            BulkResponse responses = client.bulk(bulkRequest, DEFAULT_OPTIONS);
            if(responses.hasFailures()){
                System.out.println("warn! some request may be failure!");
            } else {
                System.out.println("bulk success! size: " + size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> query(String index, BoolQueryBuilder boolQueryBuilder, int size, String name, SortOrder order){
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.size(size);
        sourceBuilder.sort(name, order);
        sourceBuilder.query(boolQueryBuilder);
        SearchRequest request = new SearchRequest(index);
        request.source(sourceBuilder);
        try {
            List<Map<String, Object>> result = new LinkedList<>();
            SearchResponse rp = client.search(request, DEFAULT_OPTIONS);
            if(rp.getHits().getHits().length > 0){
                for (SearchHit hit : rp.getHits().getHits()) {
                    result.add(hit.getSourceAsMap());
                }
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static void main(String[] args){
        Gson gson = new Gson();
        EsClient client = new EsClient("prophet2.dg.163.org", 8200);

        client.close();
    }
}
