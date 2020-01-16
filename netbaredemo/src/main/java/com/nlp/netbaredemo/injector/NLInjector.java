package com.nlp.netbaredemo.injector;

import android.support.annotation.NonNull;
import android.util.Log;
import com.github.megatronking.netbare.NetBareUtils;
import com.github.megatronking.netbare.http.HttpBody;
import com.github.megatronking.netbare.http.HttpMethod;
import com.github.megatronking.netbare.http.HttpRequest;
import com.github.megatronking.netbare.http.HttpRequestHeaderPart;
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
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Content-Encoding: gzip       //压缩传输,GZIPInputStream
 * Transfer-Encoding: chunked   //分块传输
 *
 *require('net').createServer(function(sock) {
 *     sock.on('data', function(data) {
 *         sock.write('HTTP/1.1 200 OK\r\n');
 *         sock.write('Transfer-Encoding: chunked\r\n');
 *         sock.write('\r\n');
 *
 *         sock.write('b\r\n');  //长度
 *         sock.write('01234567890\r\n'); //实际内容
 *
 *         sock.write('5\r\n');
 *         sock.write('12345\r\n');
 *
 *         sock.write('0\r\n');
 *         sock.write('\r\n');
 *     });
 * }).listen(9090, '127.0.0.1');
 *
 *
 * 方法1、先压缩再分块，逆向处理要先处理分块，再解压缩
 * 方法2、请求头 mHttpRequestHeaderPart.newBuilder().replaceHeader("Accept-Encoding", "None").build()
 *    响应头 mHttpResponseHeaderPart.newBuilder().replaceHeader("Accept-Encoding","None").build();
 * Created by zdd on 2019/11/5
 */
public class NLInjector extends SimpleHttpInjector {
    private static final String TAG = "NLInjector";
    private HttpRequestHeaderPart mHttpRequestHeaderPart;
    private HttpResponseHeaderPart mHttpResponseHeaderPart;

    @Override
    public boolean sniffRequest(HttpRequest request) {
//        // 请求url匹配时才进行注入
        boolean shouldInject = request.url().startsWith("https://app.newland.com.cn") || request.url().startsWith("https://dapp.newland.com.cn");

        return shouldInject&&HttpMethod.POST.equals(request.method());

    }

    @Override
    public boolean sniffResponse(HttpResponse response) {
        // 请求url匹配时才进行注入
        boolean shouldInject = response.url().startsWith("https://app.newland.com.cn") || response.url().startsWith("https://dapp.newland.com.cn");
        return shouldInject&&HttpMethod.POST.equals(response.method());
    }

    @Override
    public void onRequestInject(@NonNull HttpRequestHeaderPart header,
                                @NonNull InjectorCallback callback) throws IOException {
        mHttpRequestHeaderPart = header;
        Log.d(TAG, "请求url=" + header.uri());
        Log.d(TAG,"请求头\n" + new String(header.toBuffer().array(), "UTF-8"));
    }


    @Override
    public void onRequestInject(@NonNull HttpRequest request, @NonNull HttpBody body,
                                @NonNull InjectorCallback callback) throws IOException {
        Log.d(TAG, "请求体=" + new String(body.toBuffer().array(), "UTF-8"));
        if (mHttpRequestHeaderPart == null) {
            // 一般不会发生
            return;
        }

        HttpBodyInputStream his;
        Reader reader;
        ByteArrayOutputStream bos;

        his = new HttpBodyInputStream(body);
        reader = new InputStreamReader(his);
        bos = new ByteArrayOutputStream();

        try {
            JsonElement element = new JsonParser().parse(reader);
            if (element == null || !element.isJsonObject()) {
                return;
            }

            JsonObject location = element.getAsJsonObject();

//            if (location.has("coordinate")) {
//                location.addProperty("coordinate", "119.411478,26.018372");
//                Log.d(TAG, "修改后新大陆=" + element.toString());
//            }


            bos.write(element.toString().getBytes());
            bos.flush();
            byte[] injectBodyData = bos.toByteArray();
            // 更新header的content-length
            HttpRequestHeaderPart injectHeader = mHttpRequestHeaderPart.newBuilder()
                    .replaceHeader("Accept-Encoding","None")
                    .replaceHeader("Content-Length", String.valueOf(injectBodyData.length))
                    .build();

            if(!mHttpRequestHeaderPart.uri().toString().contains("behavior-logs")){
                // 先将header发射出去
                callback.onFinished(injectHeader);
                // 再将响应体发射出去
                callback.onFinished(new ByteStream(injectBodyData));
                Log.i(TAG, "Inject NLInjector location completed!");
            }


        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        } finally {
            NetBareUtils.closeQuietly(his);
            NetBareUtils.closeQuietly(reader);
            NetBareUtils.closeQuietly(bos);
        }

    }

    @Override
    public void onResponseInject(HttpResponseHeaderPart header, InjectorCallback callback) {
        // 由于响应体大小不确定，这里先hold住header（需要在后面修改content-length）
        try {
            mHttpResponseHeaderPart = header;
            Log.d(TAG, "响应头\n" + new String(header.toBuffer().array(), "UTF-8"));
            HttpResponseHeaderPart injectHeader = mHttpResponseHeaderPart.newBuilder()
                    .replaceHeader("Accept-Encoding","None")
                    .build();
            callback.onFinished(injectHeader);
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void onResponseInject(HttpResponse response, HttpBody body, InjectorCallback callback) {
        if (mHttpResponseHeaderPart == null)
            return;

        // 由于响应体大小不确定，这里先hold住header（需要在后面修改content-length）
        try {
            Log.d(TAG, "响应体\n" + new String(body.toBuffer().array()));

//            if (isGzip(mHttpResponseHeaderPart)) {
//                HttpBodyInputStream his = null;
//                Reader reader = null;
//                try {
//                    his = new HttpBodyInputStream(body);
//
//                    reader = new InputStreamReader(new GZIPInputStream(his));
//                    JsonElement element = new JsonParser().parse(reader);
//                    Log.d(TAG, "响应体\n" + element.toString());
//                } catch (Exception e) {
//                    Log.e(TAG,e.getMessage());
//                    e.printStackTrace();
//                } finally {
//                    NetBareUtils.closeQuietly(his);
//                    NetBareUtils.closeQuietly(reader);
//                }
//
//            }

            callback.onFinished(body);
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }
    }


    public boolean isGzip(HttpResponseHeaderPart httpResponseHeaderPart) {
        for (Map.Entry<String, List<String>> entry : httpResponseHeaderPart.headers().entrySet()) {
            for (String value : entry.getValue()) {
                if ("Content-Encoding".equals(entry.getKey()) && "gzip".equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isZlib(HttpResponseHeaderPart httpResponseHeaderPart) {
        for (Map.Entry<String, List<String>> entry : httpResponseHeaderPart.headers().entrySet()) {
            for (String value : entry.getValue()) {
                if ("Content-Encoding".equals(entry.getKey()) && "deflate".equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }
}
