package com.sparkor.tools.excel;

import com.google.gson.JsonObject;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ExcelClientTest {

    private ExcelClient excelClient = new ExcelClient();

    @Test
    public void readSheet() {
        List<JsonObject> list = excelClient.readSheet("C:\\Users\\liwuxi\\Desktop\\历史会员信息用户.xlsx", 0, 15);
        System.out.println(list);
    }
}