package com.nlp.netbaredemo.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.InflaterInputStream;

/**
 * Created by zdd on 2020/1/15
 */
public class Test {

    public static void main(String[] args) {
        File file=new File("D:\\test.txt");
        InputStream his= null;
        try {
            his = new FileInputStream(file);
            Reader reader =new InputStreamReader(new InflaterInputStream(his));
            JsonElement element =new JsonParser().parse(reader);
            if (element == null || !element.isJsonObject()) {
                return;
            }
            System.out.println("微信="+element.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
