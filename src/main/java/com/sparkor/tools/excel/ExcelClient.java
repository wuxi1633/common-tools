package com.sparkor.tools.excel;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelClient {

    private static final String SEPARATOR = "->";

    private static int targetPart = -1;

    public static int EXCEL_SIZE = 50000;

    /**
     * Source file content must be json list with out "[" or "]", and each line must be a json object that not end with "," !
     * @param timeField the key name of timeField
     * @param rowTitle the key is the fileName or jsonObject, the value is the target row title in excel
     */
    public void trans(String index, List<Map<String, Object>> jsonList, String targetDirPath, String timeField, Map<String, String> rowTitle){
        targetPart++;
        File file = new File(targetDirPath);
        if(!file.exists()){
            file.mkdirs();
        }
        String targetPath = targetDirPath + (targetDirPath.endsWith(File.separator) ? "":File.separator) + index + "-0" + targetPart + ".xlsx";
        System.out.println("start gen file: " + targetPath);
        System.out.println("please wait!");
        writeData(jsonList, targetPath, timeField, rowTitle);
    }

    private static void writeData(List<Map<String, Object>> jsonList, String targetPath, String timeField, Map<String, String> rowTitle){
        if(CollectionUtils.isEmpty(jsonList)){
            return;
        }

        final JsonParser parser = new JsonParser();
        final Gson gson = new Gson();
        List<JsonObject> jsonObjectList = jsonList.stream().map(e -> parser.parse(gson.toJson(e)).getAsJsonObject()).collect(Collectors.toList());
        writeExcel(jsonObjectList, targetPath, timeField, rowTitle);
        System.out.println("success write file: " + targetPath + "! count: " + jsonList.size());
        System.out.println();
    }

    private static XSSFWorkbook genBook(List<JsonObject> datas, List<String> keyList, String timeField, Map<String, String> rowTitle){
        XSSFWorkbook result = new XSSFWorkbook();
        if (CollectionUtils.isNotEmpty(datas)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            XSSFSheet sheet = result.createSheet();
            // 创建表头
            XSSFRow rowHeader = sheet.createRow(0);
            for (int i = 0; i < keyList.size(); i++) {
                XSSFCell cell = rowHeader.createCell(i);
                String value = keyList.get(i);
                if(null != rowTitle && rowTitle.size() > 0){
                    String title = rowTitle.get(value);
                    if(StringUtils.isNotBlank(title)){
                        value = title;
                    }
                }
                cell.setCellValue(value);
            }
            for (int i = 0; i < datas.size(); i++) {
                XSSFRow row = sheet.createRow(i + 1);
                JsonObject json = datas.get(i);
                for (int j = 0; j < keyList.size(); j++) {
                    String key = keyList.get(j);
                    String value = "";
                    if (StringUtils.isNotBlank(timeField) && timeField.equalsIgnoreCase(key)) {
                        JsonElement e = json.get(key);
                        try{
                            if(null != e){
                                long time = e.getAsLong();
                                if (time < 32525372800L) {
                                    time = time * 1000;
                                }
                                value = format.format(new Date(time));
                            }
                        }catch (NumberFormatException ex){
                            value = e.getAsString();
                        }
                    } else {
                        String[] keyParts = key.split(SEPARATOR);
                        if(keyParts.length == 1){
                            JsonElement e = json.get(key);
                            if(null != e){
                                if(e.isJsonObject()){
                                    value = e.getAsJsonObject().toString();
                                } else if(e.isJsonArray()){
                                    value = e.getAsJsonArray().toString();
                                } else {
                                    value = e.getAsString();
                                }

                            }
                        } else if(keyParts.length == 2){
                            JsonElement e = json.get(keyParts[0]);
                            if(null != e){
                                if(!e.isJsonObject()){
                                    System.out.println("json element can not match key, key: " + key + ", json: " + e.toString());
                                    throw new IllegalArgumentException();
                                }
                                JsonElement subEle = e.getAsJsonObject().get(keyParts[1]);
                                if(null != subEle){
                                    value = subEle.getAsString();
                                }
                            }
                        }

                    }
                    row.createCell(j).setCellValue(value);
                }
            }

        } else {
            XSSFSheet sheet = result.createSheet();
            // 创建表头
            XSSFRow rowHeader = sheet.createRow(0);
            XSSFCell cell = rowHeader.createCell(0);
            cell.setCellValue("无数据");
        }
        return result;
    }

    /**
     * @param datas json data list
     * @param targetPath target excel file path
     */
    private static void writeExcel(List<JsonObject> datas, String targetPath, String timeField, Map<String, String> rowTitle) {
        List<String> keyList = genKeyList(datas);

        XSSFWorkbook excel = genBook(datas, keyList, timeField, rowTitle);

        try(FileOutputStream out = new FileOutputStream(targetPath)){
            excel.write(out);
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static List<String> genKeyList(List<JsonObject> datas) {
        if(CollectionUtils.isEmpty(datas)){
            new LinkedList<>();
        }
        Gson gson = new Gson();
        // 一级
        List<Map> maps = datas.stream().map(e -> gson.fromJson(e, Map.class)).collect(Collectors.toList());
        Set<String> keys = new HashSet<>();
        maps.forEach(e -> keys.addAll(e.keySet()));

        // 二级
        Set<String> subKeySet = new HashSet<>();
        datas.forEach(data -> {
            for (Iterator<String> iterator1 = keys.iterator(); iterator1.hasNext(); ) {
                String key = iterator1.next();
                JsonElement element = data.get(key);
                if (null != element && element.isJsonObject()) {
                    Map map = gson.fromJson(element.getAsJsonObject(), Map.class);
                    map.keySet().forEach(subKey -> {
                        subKeySet.add(key + SEPARATOR + subKey);
                    });
                    iterator1.remove();
                }
            }
        });
        keys.addAll(subKeySet);

        keys.removeIf(Objects::isNull);

        return new ArrayList<>(keys);
    }


    public List<JsonObject> readSheet(String path, int indexOfSheet, int length){
        XSSFWorkbook excel = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(path);
            excel = new XSSFWorkbook(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != fileInputStream){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return getExcelData(excel, indexOfSheet, length);
    }

    public List<JsonObject> getExcelData(XSSFWorkbook excel, int indexOfSheet, int length){
        List<JsonObject> result = new LinkedList<>();
        if(null == excel){
            System.out.println("excel is null!");
            return result;
        }

        XSSFSheet sheet = excel.getSheetAt(indexOfSheet);

        if(null == sheet){
            System.out.println("sheet is null at index: " + indexOfSheet + "!");
            return result;
        }

        List<String> headers = new LinkedList<>();
        int startRowNum = sheet.getFirstRowNum();
        XSSFRow header = sheet.getRow(startRowNum);
        for (int i = 0; i < length; i++) {
            XSSFCell cell = header.getCell(i);
            if(cell != null){
                headers.add(cell.getStringCellValue());
            }
        }

        int index = startRowNum + 1;
        while (true){
            JsonObject object = new JsonObject();
            XSSFRow row = sheet.getRow(index);
            boolean allEmtpy = true;
            for (int i = 0; i < length; i++) {
                XSSFCell cell = row.getCell(i);
                if(cell != null){
                    String value =  cell.getStringCellValue();
                    if(StringUtils.isNotBlank(value)){
                        allEmtpy = false;
                        object.addProperty(headers.get(i), value);
                    }
                }
            }
            index++;
            result.add(object);
            if(allEmtpy){
                break;
            }
        }

        return result;
    }
}
