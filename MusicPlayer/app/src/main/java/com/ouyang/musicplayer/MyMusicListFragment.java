package com.ouyang.musicplayer;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.lidroid.xutils.db.sqlite.Selector;
import com.ouyang.musicplayer.Utils.MediaUtils;
import com.ouyang.musicplayer.adapter.MyMusicListAdapter;
import com.ouyang.musicplayer.vo.Mp3Info;

import java.util.ArrayList;



/**
 * Created by ASUS-PC on 2016/7/1.
 */
public class MyMusicListFragment extends Fragment implements AdapterView.OnItemClickListener,View.OnClickListener {

    private ListView listView_my_music;
    private ImageView imageView_album;
    private TextView textView1_songName,textView2_singer;
    private ImageView imageView2_play_pause,imageView3_next;
    private QuickScroll quickScroll;

    private ArrayList<Mp3Info> mp3Infos;
    private MainActivity mainActivity;
    private MyMusicListAdapter myMusicListAdapter;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity= (MainActivity) context;

    }

    public static MyMusicListFragment newInstance() {
        MyMusicListFragment my = new MyMusicListFragment();
        return my;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.my_music_list_layout,null);
        listView_my_music= (ListView) view.findViewById(R.id.listView_my_music);
        imageView_album= (ImageView) view.findViewById(R.id.imageView_album);
        imageView2_play_pause= (ImageView) view.findViewById(R.id.imageView2_play_pause);
        imageView3_next= (ImageView) view.findViewById(R.id.imageView3_next);
        textView1_songName= (TextView) view.findViewById(R.id.textView1_songName);
        textView2_singer= (TextView) view.findViewById(R.id.textView2_singer);

        quickScroll= (QuickScroll) view.findViewById(R.id.quickscroll);


        listView_my_music.setOnItemClickListener(this);
        imageView2_play_pause.setOnClickListener(this);
        imageView3_next.setOnClickListener(this);
        imageView_album.setOnClickListener(this);
//        loadData();//绑定服务是异步的过程，是在onResume中才绑定服务的，所以不能在这里使用数据.在MainActivity中调用此方法
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //绑定播放服务
        mainActivity.bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        //解除播放服务
        mainActivity.unbindPlayService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //加载本地列表
    public void loadData() {
        mp3Infos= MediaUtils.getMp3infos(mainActivity);//播放列表下的歌单不变，还是这样查找
//        mp3Infos=mainActivity.playService.mp3Infos;
        myMusicListAdapter=new MyMusicListAdapter(mainActivity,mp3Infos);
        listView_my_music.setAdapter(myMusicListAdapter);
        //必须放在adapter之后初始化quickscroll
        initQuickscroll();
    }

    public void initQuickscroll() {

        quickScroll.init(QuickScroll.TYPE_POPUP_WITH_HANDLE,listView_my_music,myMusicListAdapter,QuickScroll.STYLE_HOLO);
        quickScroll.setFixedSize(1);
        quickScroll.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 48);
        quickScroll.setPopupColor(QuickScroll.BLUE_LIGHT,QuickScroll.BLUE_LIGHT_SEMITRANSPARENT,1, Color.WHITE,1);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mainActivity.playService.getChangePlayList()!=PlayService.MY_MUSIC_LIST){
            mainActivity.playService.setMp3Infos(mp3Infos);
            mainActivity.playService.setChangePlayList(PlayService.MY_MUSIC_LIST);
        }
        mainActivity.playService.play(position);

        //保存播放时间
        savePlayRecord();
    }

    //保存播放时间
    private void savePlayRecord() {
        Mp3Info mp3Info=mainActivity.playService.getMp3Infos().get(mainActivity.playService.getCurrentPosition());

        try {
            Mp3Info playRecordMp3Info=mainActivity.app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",mp3Info.getId()));
            if(null==playRecordMp3Info){
                mp3Info.setMp3InfoId(mp3Info.getId());
                mp3Info.setPlayTime(System.currentTimeMillis());//设置当前播放时间
                mainActivity.app.dbUtils.save(mp3Info);
            }else{
                mp3Info.setPlayTime(System.currentTimeMillis());
                mainActivity.app.dbUtils.update(playRecordMp3Info,"playTime");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    // 回调播放状态下的UI设置,改变状态时使用的还是playService里面的mp3Info.
    public void changeUIStatusOnPlay(int position){
        if(position>=0&&position<mainActivity.playService.mp3Infos.size()){
            Mp3Info mp3Info=mainActivity.playService.mp3Infos.get(position);
            textView1_songName.setText(mp3Info.getTitle());
            textView2_singer.setText(mp3Info.getArtist());

            if(mainActivity.playService.isPlaying()){
                imageView2_play_pause.setImageResource(R.mipmap.pause);
            }else{
                imageView2_play_pause.setImageResource(R.mipmap.play);
            }

            Bitmap albumBitmap=MediaUtils.getArtwork(mainActivity,mp3Info.getId(),mp3Info.getAlbumId(),true,true);
            imageView_album.setImageBitmap(albumBitmap);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageView2_play_pause:{
                if(mainActivity.playService.isPlaying()){
                    imageView2_play_pause.setImageResource(R.mipmap.player_btn_play_normal);
                    mainActivity.playService.pause();
                }else{
                    if(mainActivity.playService.isPause()){
                        imageView2_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
                        mainActivity.playService.start();
                    }else{
                        mainActivity.playService.play(mainActivity.playService.getCurrentPosition());
                    }
                }
                break;
            }
            case R.id.imageView3_next:{
                mainActivity.playService.next();
                break;
            }
            case R.id.imageView_album:{
                //点击专辑图片后跳转到播放页面
                Intent intent=new Intent(mainActivity,PlayActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;
        }

    }
}
