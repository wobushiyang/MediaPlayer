package com.ouyang.musicplayer.Utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.ouyang.musicplayer.PlayerApplication;

/**
 * Created by ASUS-PC on 2016/7/14.
 */
public class AppUtils {

    //隐藏输入的方法，将键盘弄消失
    public static void hideInputMethod(View view){
        InputMethodManager im= (InputMethodManager) PlayerApplication.context.getSystemService(Context.INPUT_METHOD_SERVICE);

        //如果处于活动状态（即键盘显示出来了），将键盘弄没
        if(im.isActive()){
            im.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
