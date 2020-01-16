package com.nlp.netbaredemo.injector;

import android.util.Log;

import com.github.megatronking.netbare.NetBareUtils;
import com.github.megatronking.netbare.http.HttpBody;
import com.github.megatronking.netbare.http.HttpResponse;
import com.github.megatronking.netbare.http.HttpResponseHeaderPart;
import com.github.megatronking.netbare.injector.InjectorCallback;
import com.github.megatronking.netbare.injector.SimpleHttpInjector;
import com.github.megatronking.netbare.io.HttpBodyInputStream;
import com.github.megatronking.netbare.stream.ByteStream;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by zdd on 2019/11/5
 */
public class WechatLocationInjector extends SimpleHttpInjector {
    private static final String TAG = "WechatLocationInjector";


    private HttpResponseHeaderPart mHoldResponseHeader;

    @Override
    public boolean sniffResponse(HttpResponse response){
        // 请求url匹配时才进行注入
        boolean shouldInject = response.url().startsWith("https://lbs.map.qq.com/loc");

//        boolean shouldInject = response.url().contains("qq");


        if (shouldInject) {
            Log.i(TAG, "Start wechat location injection!");
        }
        return shouldInject;
    }

    @Override
    public void onResponseInject(HttpResponseHeaderPart header,InjectorCallback callback) {
        // 由于响应体大小不确定，这里先hold住header（需要在后面修改content-length）
        mHoldResponseHeader = header;

        try {
            Log.d(TAG,"响应url="+header.uri());
            Log.d(TAG,"响应头\n"+new String(header.toBuffer().array(),"UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    @Override
    public void onResponseInject(HttpResponse response,HttpBody body,InjectorCallback callback) {
        if (mHoldResponseHeader == null) {
            // 一般不会发生
            return;
        }
        HttpBodyInputStream his;
        Reader reader;
        DeflaterOutputStream fos;
        ByteArrayOutputStream bos;
        try {
            his =new HttpBodyInputStream(body);
            // 数据使用了zlib编码，这里需要先解码
            reader =new InputStreamReader(new InflaterInputStream(his));
            JsonElement element =new JsonParser().parse(reader);
            if (element == null || !element.isJsonObject()) {
                return;
            }
            Log.d(TAG,"微信="+element.toString());
            JsonElement locationNode = element.getAsJsonObject().get("location");
            if (locationNode == null || !locationNode.isJsonObject()) {
                return;
            }
            // 替换经纬度，这里是珠峰的经纬度，装逼很厉害的地方
            JsonObject location = locationNode.getAsJsonObject();

            Random random=new Random();
            BigDecimal latitudeBD=new BigDecimal("26.0212");
            BigDecimal longitudeBD=new BigDecimal("119.4061");

            latitudeBD=latitudeBD.add(new BigDecimal((random.nextInt(99)+1)/1000000.0));
            longitudeBD=longitudeBD.add(new BigDecimal((random.nextInt(99)+1)/1000000.0));

            location.addProperty("latitude", latitudeBD.doubleValue());
            location.addProperty("longitude", longitudeBD.doubleValue());

//            location.addProperty("latitude", 26.02108f);
//            location.addProperty("longitude", 119.40678f);
            String injectedBody = element.toString();
            Log.d(TAG,"修改后="+injectedBody);
            // 重新使用zlib编码
            bos =new ByteArrayOutputStream();
            fos = new DeflaterOutputStream(bos);
            try {
                fos.write(injectedBody.getBytes());
                fos.finish();
                fos.flush();
                byte [] injectBodyData = bos.toByteArray();
                // 更新header的content-length
                HttpResponseHeaderPart injectHeader = mHoldResponseHeader.newBuilder().replaceHeader("Content-Length", String.valueOf(injectBodyData.length)).build();
                // 先将header发射出去
                callback.onFinished(injectHeader);
                // 再将响应体发射出去
                callback.onFinished(new ByteStream(injectBodyData));
                Log.i(TAG, "Inject wechat location completed!");
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                NetBareUtils.closeQuietly(his);
                NetBareUtils.closeQuietly(reader);
                NetBareUtils.closeQuietly(fos);
                NetBareUtils.closeQuietly(bos);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        mHoldResponseHeader = null;
    }
}
