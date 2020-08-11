package com.sparkor.tools.log;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 因为每个微服务都部署在多台机器上，这样查日志的时候多台机器的日志就需要根据时间汇总到一起查看用户的行为
 * 这个工具可以把日志根据时间升序排序，更方面查看日志
 */
public class LogUtil {

    private File file;

    private String logFilePath;

    public LogUtil(String logFilePath) throws FileNotFoundException{
        if(logFilePath == null || logFilePath.length() == 0){
            throw new IllegalArgumentException();
        }
        this.logFilePath = logFilePath;
        this.file = new File(logFilePath);

        if(!file.exists()){
            throw new FileNotFoundException();
        }
    }

    private List<String> readLines(){
        List<String> logs = new LinkedList<>();
        FileInputStream fi = null;
        BufferedReader bufferedReader = null;
        try {
            fi = new FileInputStream(file);
            bufferedReader = new BufferedReader(new InputStreamReader(fi, StandardCharsets.UTF_8));
            String line = null;
            do{
                line = bufferedReader.readLine();
                if(StringUtils.isNotBlank(line)){
                    logs.add(line);
                }
            } while (line != null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != fi){
                try {
                    fi.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return logs;
    }

    private void writeLines(List<LogBean> logBeans){

        String targetPath = file.getParentFile().getAbsolutePath();
        targetPath = targetPath + File.separator + "sorted" + System.currentTimeMillis() + ".txt";
        System.out.println("gen sorted file to: " + targetPath);

        File newfile = new File(targetPath);
        if(!newfile.exists()){
            System.out.println("create new file: " + targetPath);
            try {
                newfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileOutputStream = new FileOutputStream(newfile);
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));

            for (LogBean e : logBeans) {
                System.out.println(e.getLine());
                bufferedWriter.write(e.getLine());
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != bufferedWriter){
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("finish to write, file: " + targetPath);
    }

    private List<LogBean> genSortedBeans(int start, int end, String dataPattern, List<String> logs){

        SimpleDateFormat sdf = new SimpleDateFormat(dataPattern);

        List<LogBean> logBeans = new LinkedList<>();
        logs.forEach(e -> {
            if(e.length() > start && e.length() > end){
                String timeStr = e.substring(start, end);
                try {
                    logBeans.add(new LogBean(sdf.parse(timeStr), e));
                } catch (ParseException parseException) {
                    parseException.printStackTrace();
                }
            }
        });

        Collections.sort(logBeans);
        return logBeans;
    }

    public void sort(int start, int end, String dataPattern) {
        List<String> logs = readLines();

        if(!CollectionUtils.isEmpty(logs)){
            List<LogBean> logBeans = genSortedBeans(start, end, dataPattern, logs);

            writeLines(logBeans);

            System.out.println("finish!");
        }else {
            System.out.println("log file is emtpy!!!");
        }
    }

    @Data
    private static class LogBean implements Comparable{
        private Date time;
        private String line;

        public LogBean(Date time, String line) {
            this.time = time;
            this.line = line;
        }

        @Override
        public int compareTo(Object o) {
            if(o instanceof LogBean){
                LogBean param = (LogBean)o;
                return new Long(this.time.getTime() - param.getTime().getTime()).intValue();
            } else {
                throw new ClassCastException();
            }
        }
    }
}
