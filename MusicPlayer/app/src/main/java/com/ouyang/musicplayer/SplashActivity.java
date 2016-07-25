package com.ouyang.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

/**
 * 功能：定义闪屏页，开始服务。
 *
 */
public class SplashActivity extends Activity {

    private static final int START_ACTIVITY =0x1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        Intent intent=new Intent(this,PlayService.class);
        startService(intent);//开始服务

        handler.sendEmptyMessageDelayed(START_ACTIVITY,3000);

    }
        private Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case START_ACTIVITY:
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                        break;
                }
            }
        };
}
