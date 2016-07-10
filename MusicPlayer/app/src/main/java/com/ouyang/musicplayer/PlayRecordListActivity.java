package com.ouyang.musicplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.ouyang.musicplayer.BaseActivity;
import com.ouyang.musicplayer.Utils.Constant;
import com.ouyang.musicplayer.adapter.MyMusicListAdapter;
import com.ouyang.musicplayer.vo.Mp3Info;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS-PC on 2016/7/9.
 */
public class PlayRecordListActivity extends BaseActivity implements AdapterView.OnItemClickListener {


    private ListView listView_play_record;
    private TextView textView_no_data;
    private PlayerApplication app;
    private ArrayList<Mp3Info> mp3Infos;
    private MyMusicListAdapter adapter;//直接利用之前播放列表的adapter.

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_record_list);
        app = (PlayerApplication) getApplication();
        listView_play_record = (ListView) findViewById(R.id.listView_play_record);
        textView_no_data = (TextView) findViewById(R.id.textView2_no_data);

        listView_play_record.setOnItemClickListener(this);
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindPlayService();
    }

    //初始化最近播放的数据
    private void initData() {
        try {
            //查询最近播放的记录
            List<Mp3Info> list = app.dbUtils.findAll(Selector.from(Mp3Info.class).where("playTime", "!=", 0).orderBy("playTime", true).limit(Constant.PLAY_RECORD_MAX_NUM));
            if (null == list || list.size() == 0) {
                textView_no_data.setVisibility(View.VISIBLE);//显示视图
                listView_play_record.setVisibility(View.GONE);//将视图隐藏
            } else {
                textView_no_data.setVisibility(View.GONE);
                listView_play_record.setVisibility(View.VISIBLE);
                mp3Infos = (ArrayList<Mp3Info>) list;//将收藏的音乐都查找出来。
                adapter = new MyMusicListAdapter(this, mp3Infos);
                listView_play_record.setAdapter(adapter);
            }

        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (playService.getChangePlayList() != PlayService.PLAY_RECORD_LIST) {
            playService.setMp3Infos(mp3Infos);
            playService.setChangePlayList(PlayService.PLAY_RECORD_LIST);
        }
        playService.play(position);
    }
}
