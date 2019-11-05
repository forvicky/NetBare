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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
        if (shouldInject) {
            Log.i(TAG, "Start wechat location injection!");
        }
        return shouldInject;
    }

    @Override
    public void onResponseInject(HttpResponseHeaderPart header,InjectorCallback callback) {
        // 由于响应体大小不确定，这里先hold住header（需要在后面修改content-length）
        mHoldResponseHeader = header;
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
            location.addProperty("latitude", 27.99136f);
            location.addProperty("longitude", 86.88915f);
            String injectedBody = element.toString();
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
