package com.nlp.netbaredemo.injector;

import android.content.Context;
import android.util.Log;
import com.github.megatronking.netbare.http.HttpBody;
import com.github.megatronking.netbare.http.HttpResponse;
import com.github.megatronking.netbare.http.HttpResponseHeaderPart;
import com.github.megatronking.netbare.injector.InjectorCallback;
import com.github.megatronking.netbare.injector.SimpleHttpInjector;
import com.github.megatronking.netbare.stream.BufferStream;
import com.nlp.netbaredemo.R;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by zdd on 2019/11/5
 */
public class BaiduLogoInjector extends SimpleHttpInjector {
    private static Context mCtx;
    private static final String TAG="BaiduLogoInjector";

    public BaiduLogoInjector(Context context) {
        mCtx=context;
    }

    public boolean sniffResponse(HttpResponse response){
        // 请求url匹配时才进行注入
        boolean shouldInject = "https://m.baidu.com/static/index/plus/plus_logo.png".equals(
                response.url());
        if (shouldInject) {
            Log.i(TAG, "Start Miss. Du logo injection!");
        }
        return shouldInject;
    }

    @Override
    public void onResponseInject(HttpResponseHeaderPart header,InjectorCallback callback) {
        // 响应体大小变化，一定要先更新header中的content-length
        HttpResponseHeaderPart newHeader = header.newBuilder()
                .replaceHeader("content-length", "10764")
                .build();
        try {
            callback.onFinished(newHeader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Inject header completed!");
    }

    @Override
    public void onResponseInject(HttpResponse response,HttpBody body,InjectorCallback callback) {
        try {
            // 替换图片请求响应体
            InputStream injectIOStream = mCtx.getResources().openRawResource(R.raw.baidu_inject_logo);

            if(injectIOStream.available()>0){
                byte [] logos=new byte[injectIOStream.available()];
                injectIOStream.read(logos);

                BufferStream injectStream =new BufferStream(ByteBuffer.wrap(logos));
                injectIOStream.close();
                callback.onFinished(injectStream);
                Log.i(TAG, "Inject body completed!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
