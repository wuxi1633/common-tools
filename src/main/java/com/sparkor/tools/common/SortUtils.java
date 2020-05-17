package com.sparkor.tools.common;

import com.google.gson.*;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.text.CollationKey;
import java.text.Collator;
import java.util.*;

/**
 * 按照字母序排序
 * 首先是数字，然后是字母，其次是中文的拼音字母序
 * */

@SuppressWarnings("unchecked")
public class SortUtils {
    private static Gson gson = new Gson();
    private static JsonParser parser = new JsonParser();

    public static <T> void sort(List<T> result, String fieldName, String childrenFieldName){
        if(ObjectUtils.isEmpty(result)){
            return;
        }
        Map<String, Object> map = new HashMap<>();
        String names[] = new String[result.size()];
        for (int i = 0, resultSize = result.size(); i < resultSize; i++) {
            Object scene = result.get(i);
            JsonObject object = parser.parse(gson.toJson(scene)).getAsJsonObject();
            JsonElement element = object.get(childrenFieldName);
            if(null != element){
                JsonArray array = element.getAsJsonArray();
                if(null != array && array.size() > 1){
                    List children = new ArrayList(array.size());
                    array.forEach(children::add);
                    sort(children, fieldName, childrenFieldName);
                    object.add(childrenFieldName, parser.parse(gson.toJson(children)));
                }
            }

            String name = object.get(fieldName).getAsString();
            String sceneAlphabet = name.substring(0, 1);
            if (sceneAlphabet.matches("[\\u4e00-\\u9fa5]+")) {
                name = getAlphabet(name) + "&" + name;
                names[i] = name;
            } else {
                names[i]=name;
            }
            map.put(name, scene);
        }

        names = sortArray(names);
        result.clear();

        for(String name : names){
            if(map.containsKey(name))
                result.add((T)map.get(name));
        }
    }

    public static void sort(List result, String fieldName){
        if(ObjectUtils.isEmpty(result)){
            return;
        }
        Map<String, Object> map = new HashMap<>();
        String names[] = new String[result.size()];
        for (int i = 0, resultSize = result.size(); i < resultSize; i++) {
            Object scene = result.get(i);
            String name = parser.parse(gson.toJson(scene)).getAsJsonObject().get(fieldName).getAsString();
            String sceneAlphabet = name.substring(0, 1);
            if (sceneAlphabet.matches("[\\u4e00-\\u9fa5]+")) {
                name = getAlphabet(name) + "&" + name;
                names[i] = name;
            } else {
                names[i]=name;
            }
            map.put(name, scene);
        }

        names = sortArray(names);
        result.clear();

        for(String name : names){
            if(map.containsKey(name))
                result.add(map.get(name));
        }
    }

    private static String[] sortArray(String [] data){
        if(data==null || data.length==0){
            return null;
        }
        Comparator<Object> comparator = Collator.getInstance(Locale.CHINA);
        Arrays.sort(data, comparator);

        Arrays.sort(data, new Collator() {
            @Override
            public int compare(String source, String target) {
                if(source.contains("&") && !target.contains("&")){
                    return 1;
                } else if(!source.contains("&") && target.contains("&")){
                    return -1;
                }
                return 0;
            }

            @Override
            public CollationKey getCollationKey(String source) {
                return null;
            }

            @Override
            public int hashCode() {
                return 0;
            }
        });
        return data;
    }
    /**
     * 调用汉字首字母转化为拼音的根据类
     */
    private static String getAlphabet(String str) {
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        // 输出拼音全部小写
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        // 不带声调
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        String pinyin = null;
        try {
            pinyin = PinyinHelper.toHanyuPinyinStringArray(str.charAt(0), defaultFormat)[0];
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }
        if(StringUtils.isBlank(pinyin)){
            return "0";
        }
        return pinyin.substring(0, 1);
    }
}
