package com.sparkor.tools.log;

import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class LogUtilTest {

    @Test
    public void sort() throws FileNotFoundException {
        LogUtil logUtil = new LogUtil("C:\\Users\\liwuxi\\Desktop\\log.txt");
        //2020-08-10 22:35:27.151
        logUtil.sort(0, 23, "yyyy-MM-dd HH:mm:ss.SSS");
    }
}