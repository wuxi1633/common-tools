package com.sparkor.tools.http;

import com.google.gson.Gson;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpClient {
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private static final MediaType FORM_URL_ENCODE = MediaType.parse("application/x-www-form-urlencoded");

    private Gson gson = new Gson();

    private OkHttpClient okHttpClient;

    public HttpClient() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true).sslSocketFactory(getSSLSocketFactory()).build();

    }

    //获取这个SSLSocketFactory
    public static SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, getTrustManager(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //获取TrustManager
    private static TrustManager[] getTrustManager() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
        return trustAllCerts;
    }

    private static void destroy(OkHttpClient okHttpClient) {
        if (okHttpClient != null) {
            ConnectionPool connectionPool = okHttpClient.connectionPool();
            connectionPool.evictAll();
            ExecutorService executorService = okHttpClient.dispatcher().executorService();
            executorService.shutdown();
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        }
    }

    private OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    @PreDestroy
    public void destroy() {
        destroy(okHttpClient);
    }

    public String getForString(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        try {
            response = getOkHttpClient().newCall(request).execute();
            return response.body().string();
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public Response execute(Request request) throws IOException {
        try {
            Response response = getOkHttpClient().newCall(request).execute();
            if (response != null) {
                return response;
            }
            return getOkHttpClient().newCall(request).execute();
        } catch (Exception e) {
            return getOkHttpClient().newCall(request).execute();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getForMap(String url) throws IOException {
        String jsonString = getForString(url);
        if (StringUtils.isNotBlank(jsonString)) {
            return gson.fromJson(jsonString, Map.class);
        }
        return Collections.emptyMap();
    }

    public Response getResponse(String url) throws IOException {
        return getResponse(url, false);
    }

    public Response getResponse(String url, Map<String, String> params) throws IOException {
        if (params != null && params.size() > 0) {
            StringBuilder sb = null;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (sb == null) {
                    sb = new StringBuilder();
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                sb.append(key);
                sb.append("=");
                sb.append(value);
            }
            url += sb.toString();
        }

        return getResponse(url);
    }

    public Response getResponseWithHeaders(String url, Map<String, Object> headers) throws IOException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        for (String header : headers.keySet()) {
            builder.addHeader(header, String.valueOf(headers.get(header)));
        }
        Request request = builder.build();
        return getOkHttpClient().newCall(request).execute();
    }

    public Response getResponse(String url, boolean close) throws IOException {
        Request.Builder builder = new Request.Builder();

        builder.url(url);

        if (close) {
            builder.header("Connection", "close");
        }

        Request request = builder.build();

        return getOkHttpClient().newCall(request).execute();
    }

    public String postForFormUrlEncodeString(String url, String body, Map<String, String> headers) throws IOException {
        Response response = postForFormUrlEncodeResponse(url, body, headers);
        try {
            if (response != null && response.body() != null) {
                return response.body().string();
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return StringUtils.EMPTY;
    }

    public String postForFormUrlEncodeString(String url, String body) throws IOException {
        Response response = postForFormUrlEncodeResponse(url, body);
        try {
            if (response != null && response.body() != null) {
                return response.body().string();
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return StringUtils.EMPTY;
    }

    public Response postForFormUrlEncodeResponse(String url, String body) throws IOException {
        RequestBody requestBody = RequestBody.create(FORM_URL_ENCODE, body);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);
        Request request = requestBuilder.build();
        return getOkHttpClient().newCall(request).execute();
    }

    public Response postForResponseWithParam(String url, String key, String param) throws IOException {
        String newUrl = url + "?" + key + "=" + param;
        RequestBody requestBody = RequestBody.create(null, new byte[0]);
        Request.Builder requestBuilder = new Request.Builder()
                .url(newUrl)
                .post(requestBody);
        Request request = requestBuilder.build();
        return getOkHttpClient().newCall(request).execute();
    }

    public Response postForResponse(String url) throws IOException {
        RequestBody requestBody = RequestBody.create(null, new byte[0]);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("Connection","close")
                .post(requestBody);
        Request request = requestBuilder.build();
        return getOkHttpClient().newCall(request).execute();
    }

    public Response postForFormUrlEncodeResponse(String url, Map<String, String> valueMap, Map<String, String> headers) throws IOException {

        FormBody.Builder builder = new FormBody.Builder();
        for (String key : valueMap.keySet()) {
            builder.add(key, valueMap.get(key));
        }
        RequestBody requestBody = builder.build();
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);
        if (headers != null && headers.size() > 0) {
            for (String header : headers.keySet()) {
                requestBuilder.addHeader(header, headers.get(header));
            }
        }
        Request request = requestBuilder.build();
        return getOkHttpClient().newCall(request).execute();
    }

    public Response postForFormUrlEncodeResponse(String url, Map<String, String> params) throws IOException {
        return postForFormUrlEncodeResponse(url, params, null);
    }

    public Response postForFormUrlEncodeResponse(String url, String body, Map<String, String> headers) throws IOException {
        RequestBody requestBody = RequestBody.create(FORM_URL_ENCODE, body);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);
        for (String header : headers.keySet()) {
            requestBuilder.addHeader(header, headers.get(header));
        }
        Request request = requestBuilder.build();
        return getOkHttpClient().newCall(request).execute();
    }

    public String postForJsonString(String url, String json) throws IOException {
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Response response = getOkHttpClient().newCall(request).execute();
        try {
            if (response.body() != null) {
                return response.body().string();
            }
        } finally {
            response.close();
        }
        return StringUtils.EMPTY;
    }

    public Response postForJsonResponse(String url, String json) throws IOException {
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return getOkHttpClient().newCall(request).execute();
    }

    public String postForJsonStringWithHeaders(String url, String json, Map<String, Object> headers) throws IOException {
        Response response = postForResponseWithHeaders(url, json, headers);
        try {
            if (response != null) {
                return response.body().string();
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return StringUtils.EMPTY;
    }

    public Response postForResponse(String url, String json) throws IOException {
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, json);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);
        return getOkHttpClient().newCall(requestBuilder.build()).execute();
    }

    private void isTrue(boolean condition, String message){
        if(!condition){
            throw new IllegalArgumentException(message);
        }
    }

    public Response postForResponseWithHeaders(String url, String json, Map<String, Object> headers) throws IOException {
        isTrue(!headers.isEmpty(), "Headers must not be empty !");
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_JSON, json);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);
        if(null != headers && headers.size() > 0){
            for(Map.Entry<String, Object> entry: headers.entrySet()){
                requestBuilder.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return getOkHttpClient().newCall(requestBuilder.build()).execute();
    }

    public Response getForResponseWithRetry(String url, int retry) throws IOException {
        try {
            Response response = getResponse(url);
            if (response != null && response.code() >= 200 && response.code() < 400) {
                return response;
            }
            if (retry > 0) {
                return getForResponseWithRetry(url, retry - 1);
            }

        } catch (IOException e) {
            if (retry > 0) {
                return getForResponseWithRetry(url, retry - 1);
            }
            throw e;
        }
        return null;
    }

    public String getForStringWithRetry(String url, int retry) throws IOException {
        try {
            Response response = getForResponseWithRetry(url, retry);
            try {
                if (response != null && response.body() != null) {
                    return response.body().string();
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
            return StringUtils.EMPTY;
        } catch (IOException e) {
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getForMapWithRetry(String url, int retry) throws IOException {
        try {
            String jsonString = getForStringWithRetry(url, retry);
            if (StringUtils.isNotBlank(jsonString)) {
                return gson.fromJson(jsonString, Map.class);
            }
            return Collections.emptyMap();
        } catch (IOException e) {
            throw e;
        }
    }

}
