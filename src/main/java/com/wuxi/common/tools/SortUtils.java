package com.wuxi.common.tools;

import com.google.gson.*;
import com.wuxi.common.beans.person;
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

    public static void main(String[] args){
        List<person> result = new LinkedList<>();
        person roA1 = new person();
        roA1.setName("abc");
        result.add(roA1);

        person roA2 = new person();
        roA2.setName("bcd");
        result.add(roA2);

        person roA3 = new person();
        roA3.setName("xyz");
        result.add(roA3);


        person roD1 = new person();
        roD1.setName("123");
        result.add(roD1);

        person roD2 = new person();
        roD2.setName("231");
        result.add(roD2);

        person roD3 = new person();
        roD3.setName("321");
        result.add(roD3);

        person roC1 = new person();
        roC1.setName("阿里巴巴");
        result.add(roC1);

        person roC2 = new person();
        roC2.setName("腾讯");
        result.add(roC2);

        person roC3 = new person();
        roC3.setName("中国");
        result.add(roC3);

        List<person> parent = new LinkedList<>();
        person pa = new person();
        pa.setName("abc");
        pa.setChildren(result);
        parent.add(pa);

        sort(parent, "name", "children");

        parent.get(0).getChildren().forEach(e -> System.out.println(parser.parse(gson.toJson(e)).getAsJsonObject().get("name").getAsString()));
    }

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
