package com.ouyang.musicplayer.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS-PC on 2016/7/6.
 */
public class MyPagerAdapter extends PagerAdapter {

    private ArrayList<View> viewList;//view数组

    public MyPagerAdapter(ArrayList<View> viewList) {
        this.viewList = viewList;//构造方法，参数是我们的页卡，这样比较方便。
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)   {
        container.removeView(viewList.get(position));//删除页卡
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {  //这个方法用来实例化页卡
        container.addView(viewList.get(position), 0);//添加页卡
        return viewList.get(position);
    }

    @Override
    public int getCount() {
        return  viewList.size();//返回页卡的数量
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0==arg1;//官方提示这样写
    }
}
