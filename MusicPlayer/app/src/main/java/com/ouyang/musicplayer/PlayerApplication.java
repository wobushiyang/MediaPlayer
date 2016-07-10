package com.ouyang.musicplayer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.lidroid.xutils.DbUtils;
import com.ouyang.musicplayer.Utils.Constant;

/**
 * Created by ASUS-PC on 2016/7/5.
 */
public class PlayerApplication extends Application {

    public static SharedPreferences sp;
    public static DbUtils dbUtils;
    @Override
    public void onCreate() {
        super.onCreate();
        sp=getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        dbUtils=DbUtils.create(getApplicationContext(),Constant.DB_NAME);
    }
}
