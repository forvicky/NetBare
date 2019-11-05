package com.nlp.netbaredemo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.megatronking.netbare.NetBare;
import com.github.megatronking.netbare.NetBareConfig;
import com.github.megatronking.netbare.NetBareListener;
import com.github.megatronking.netbare.http.HttpInjectInterceptor;
import com.github.megatronking.netbare.http.HttpInterceptorFactory;
import com.github.megatronking.netbare.injector.HttpInjector;
import com.github.megatronking.netbare.ssl.JKS;
import com.nlp.netbaredemo.injector.BaiduLogoInjector;
import com.nlp.netbaredemo.injector.HttpUrlPrintInterceptor;
import com.nlp.netbaredemo.injector.WechatLocationInjector;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NetBareListener {
    private NetBare mNetBare;
    private Button mActionButton;
    private static final int REQUEST_CODE_PREPARE = 1;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        mNetBare = NetBare.get();

        mActionButton = this.findViewById(R.id.action);

        mActionButton.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                if (mNetBare.isActive()) {
                    mNetBare.stop();
                } else {
                    MainActivity.this.prepareNetBare();
                }

            }
        });
        mNetBare.registerNetBareListener(this);
    }

    protected void onDestroy() {
        super.onDestroy();

        if(mNetBare!=null){
            mNetBare.unregisterNetBareListener(this);
            mNetBare.stop();
        }

    }

    public void onServiceStarted() {
        this.runOnUiThread((Runnable)(new Runnable() {
            public final void run() {
                mActionButton.setText("暂停服务");
            }
        }));
    }

    public void onServiceStopped() {
        this.runOnUiThread((Runnable)(new Runnable() {
            public final void run() {
                mActionButton.setText("启动服务");
            }
        }));
    }

    private final void prepareNetBare() {
        if (!JKS.isInstalled((Context)this, App.JSK_ALIAS)) {
            try {
                JKS.install((Context)this, App.JSK_ALIAS, App.JSK_ALIAS);
            } catch (IOException var2) {
            }

        } else {
            Intent intent = NetBare.get().prepare();
            if (intent != null) {
                this.startActivityForResult(intent, REQUEST_CODE_PREPARE);
            } else {
                mNetBare.start(NetBareConfig.defaultHttpConfig(App.mJKS, this.interceptorFactories()));
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == 1) {
            this.prepareNetBare();
        }

    }

    private final List interceptorFactories() {
        HttpInterceptorFactory interceptor1 = HttpUrlPrintInterceptor.createFactory();
        HttpInterceptorFactory injector1 = HttpInjectInterceptor.createFactory((HttpInjector)(new BaiduLogoInjector(this)));
        HttpInterceptorFactory injector2 = HttpInjectInterceptor.createFactory((HttpInjector)(new WechatLocationInjector()));
        return Arrays.asList(new HttpInterceptorFactory[]{interceptor1, injector1, injector2});
    }

 
 
}
