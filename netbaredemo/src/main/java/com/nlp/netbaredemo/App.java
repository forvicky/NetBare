package com.nlp.netbaredemo;

import android.app.Application;

import com.github.megatronking.netbare.NetBare;
import com.github.megatronking.netbare.ssl.JKS;

/**
 * Created by zdd on 2019/11/5
 */
public class App extends Application {
    public static JKS mJKS;
    public static final String JSK_ALIAS="NetBareDemo";

    @Override
    public void onCreate() {
        super.onCreate();

        this.mJKS = new JKS(this, JSK_ALIAS,JSK_ALIAS.toCharArray(), JSK_ALIAS, JSK_ALIAS, JSK_ALIAS, JSK_ALIAS, JSK_ALIAS);
        NetBare.get().attachApplication(this, BuildConfig.DEBUG);
    }


}
