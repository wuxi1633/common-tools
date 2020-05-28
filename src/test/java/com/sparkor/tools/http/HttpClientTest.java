package com.sparkor.tools.http;

import okhttp3.Response;
import org.junit.Test;

import java.io.IOException;

public class HttpClientTest {

    private HttpClient httpClient = new HttpClient();

    private static final String url = "http://10.172.23.40:10008/position-openapi/query?surveyId=9880&batch=op.openapicsl&code=coadf";

    @Test
    public void getForMap() {
        try {
            Response response = httpClient.getResponse(url);
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getResponse() {
        try {
            int count = 0;
            for (int i = 0; i < 10000000; i++) {
                Response response = httpClient.getResponse(url);
                count++;
                if(count % 1000 == 0){
                    System.out.println(response.body().string());
                    System.out.println(count);
                    Thread.sleep(2000L);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getResponse1() {
    }

    @Test
    public void getResponseWithHeaders() {
    }

    @Test
    public void getResponse2() {
    }

    @Test
    public void postForFormUrlEncodeString() {
    }

    @Test
    public void postForFormUrlEncodeString1() {
    }

    @Test
    public void postForFormUrlEncodeResponse() {
    }

    @Test
    public void postForResponseWithParam() {
    }

    @Test
    public void postForResponse() {
    }

    @Test
    public void postForFormUrlEncodeResponse1() {
    }

    @Test
    public void postForFormUrlEncodeResponse2() {
    }

    @Test
    public void postForFormUrlEncodeResponse3() {
    }

    @Test
    public void postForJsonString() {
    }

    @Test
    public void postForJsonResponse() {
    }

    @Test
    public void postForJsonStringWithHeaders() {
    }

    @Test
    public void postForResponse1() {
    }

    @Test
    public void postForResponseWithHeaders() {
    }

    @Test
    public void getForResponseWithRetry() {
    }

    @Test
    public void getForStringWithRetry() {
    }

    @Test
    public void getForMapWithRetry() {
    }
}
