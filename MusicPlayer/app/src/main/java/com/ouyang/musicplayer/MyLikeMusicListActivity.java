package com.ouyang.musicplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.ouyang.musicplayer.adapter.MyMusicListAdapter;
import com.ouyang.musicplayer.vo.Mp3Info;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS-PC on 2016/7/7.
 */
public class MyLikeMusicListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView listView_like;
    private PlayerApplication app;
    private ArrayList<Mp3Info> likeMp3Infos;
    private MyMusicListAdapter adapter;//直接利用之前播放列表的adapter.


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_music_list);
        app = (PlayerApplication) getApplication();
        listView_like = (ListView) findViewById(R.id.listView_like);
        listView_like.setOnItemClickListener(this);
        initData();//初始化数据方法
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

    private void initData() {
        try {
            List<Mp3Info> list = app.dbUtils.findAll(Selector.from(Mp3Info.class).where("isLike", "=", 1));
            if (null == list || list.size() == 0) {
                return;
            }
            likeMp3Infos = (ArrayList<Mp3Info>) list;//将收藏的音乐都查找出来。
            adapter = new MyMusicListAdapter(this, likeMp3Infos);
            listView_like.setAdapter(adapter);
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
        if (playService.getChangePlayList() != PlayService.LIKE_MUSIC_LIST) {
            playService.setMp3Infos(likeMp3Infos);
            playService.setChangePlayList(PlayService.LIKE_MUSIC_LIST);
        }
        playService.play(position);

        //保存播放时间
        savePlayRecord();

    }

    //保存播放记录
    private void savePlayRecord() {
        //获取当前正在播放的歌曲
        Mp3Info mp3Info = playService.getMp3Infos().get(playService.getCurrentPosition());
        try {
            Mp3Info playRecordMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getMp3InfoId()));
            if (null == playRecordMp3Info) {
                mp3Info.setPlayTime(System.currentTimeMillis());//设置当前播放时间
                app.dbUtils.save(mp3Info);
            } else {
                mp3Info.setPlayTime(System.currentTimeMillis());
                app.dbUtils.update(playRecordMp3Info, "playTime");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
