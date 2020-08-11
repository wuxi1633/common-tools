package com.sparkor.tools.excel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sparkor.tools.http.HttpClient;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class ExcelClientTest {

    private ExcelClient excelClient = new ExcelClient();

    private HttpClient httpClient = new HttpClient();

    private static String excelPath = "C:\\Users\\liwuxi\\Desktop\\online-历史会员信息用户.xlsx";
//    private static String excelPath = "C:\\Users\\liwuxi\\Desktop\\历史会员信息用户.xlsx";

    private static int indexOfSheet = 0;
    private static int sheetLength = 16;

    @Test
    public void readSheet() {
        List<JsonObject> list = excelClient.readSheet(excelPath, indexOfSheet, sheetLength);
        System.out.println(list);
    }

    private String trans(JsonObject object){
    /*
         {
          "Q3.请问您的性别？": "女",
          "Q4.请问您的日常居住城市是哪里？:省": "浙江省",
          "Q4.请问您的日常居住城市是哪里？:市": "杭州市",
          "Q5.请问您的出生年月？:年": "1993",
          "Q5.请问您的出生年月？:月": "06",
          "Q5.请问您的出生年月？:日": "14",
          "Q6.请问您当前的学历是？": "本科",
          "Q7.请问您当前的工作是？": "企业普通员工",
          "Q8.请问您当前的个人月收入？（税前）": "8001~10000元",
          "Q17.请问您当前的生活状态？": "单身",
          "Q21.请填写您的手机号，用于未来给您发放有奖问卷。您的手机号我们将严格保密，不会泄露给任何第三方。": "13282808375"
        }
     */
        JsonObject result = new JsonObject();

        result.addProperty("gender", getProperty(object, "Q3.请问您的性别？"));
        result.addProperty("year", getProperty(object, "Q5.请问您的出生年月？:年"));
        result.addProperty("month", getProperty(object, "Q5.请问您的出生年月？:月"));
        result.addProperty("day", getProperty(object, "Q5.请问您的出生年月？:日"));

        String location = getProperty(object, "Q4.请问您的日常居住城市是哪里？:省") + "-" +
                getProperty(object, "Q4.请问您的日常居住城市是哪里？:市");
        result.addProperty("location", location);
        result.addProperty("education", getProperty(object, "Q6.请问您当前的学历是？"));
        result.addProperty("profession", getProperty(object, "Q7.请问您当前的工作是？"));
        result.addProperty("income", getProperty(object, "Q8.请问您当前的个人月收入？（税前）"));
        result.addProperty("married", getProperty(object, "Q17.请问您当前的生活状态？"));
        result.addProperty("children", getProperty(object, "Q18.请问您有几个小孩？"));
        result.addProperty("car", getProperty(object, "Q10.请问您家的汽车品牌和车型是？:汽车品牌"));
        result.addProperty("phone", getProperty(object, "Q21.请填写您的手机号，用于未来给您发放有奖问卷。您的手机号我们将严格保密，不会泄露给任何第三方。"));
        return result.toString();
    }

    private String getProperty(JsonObject object, String key){
        JsonElement element = object.get(key);
        if(null != element){
            return element.getAsString();
        }
        return "";
    }

    @Test
    public void readAndHttp() {
//        String url = "http://10.172.23.42:10022/wechat/user/baseInfo/flush";
        String url = "http://10.201.226.12:9003/wechat/user/baseInfo/flush";
        List<JsonObject> list = excelClient.readSheet(excelPath, indexOfSheet, sheetLength);
        List<JsonObject> errorData = new LinkedList<>();

        int i = 0;
        for (JsonObject e : list) {
            try {
                Response response = httpClient.postForJsonResponse(url, trans(e));
                System.out.println(response.body().string());
            } catch (Exception ex) {
                System.out.println("error send data:" + e.toString());
                ex.printStackTrace();
                errorData.add(e);
            }
            i++;
            if(i % 10 == 0){
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                System.out.println("has finished: " + i);
            }
        }


        if(CollectionUtils.isNotEmpty(errorData)){
            System.out.println("-------------error---------------");
            errorData.forEach(e -> {
                System.out.println(e.toString());
            });
        } else {
            System.out.println("no error data");
            System.out.println("-------------finish---------------");
        }


    }
}