package com.ouyang.musicplayer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.ouyang.musicplayer.Utils.MediaUtils;
import com.ouyang.musicplayer.adapter.MyPagerAdapter;
import com.ouyang.musicplayer.vo.Mp3Info;

import java.util.ArrayList;

/**
 * 更新播放状态下的歌曲界面
 */
public class PlayActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private TextView textView3_title;
    private ImageView imageView1_album;
    private SeekBar seekBar1;
    private TextView textView1_start_time;
    private TextView textView2_end_time;
    private ImageView imageView4_play_mode;
    private ImageView imageView3_prev;
    private ImageView imageView2_play_pause;
    private ImageView imageView1_next;

    private ImageView imageView_favorite;

    // private ArrayList<Mp3Info> mp3Infos;

    private static MyHandler myHandler;//声明个MyHandler，然后在onCreate里面赋值

    private static final int UPDATE_TIME = 0x1;//更新播放时间的标记


    private ViewPager viewPager;
    private ArrayList<View> viewList = new ArrayList<>();

    private PlayerApplication app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        app = (PlayerApplication) getApplication();

        textView1_start_time = (TextView) findViewById(R.id.textView1_start_time);
        textView2_end_time = (TextView) findViewById(R.id.textView2_end_time);

        imageView4_play_mode = (ImageView) findViewById(R.id.imageView4_play_mode);
        imageView3_prev = (ImageView) findViewById(R.id.imageView3_prev);
        imageView2_play_pause = (ImageView) findViewById(R.id.imageView2_play_pause);
        imageView1_next = (ImageView) findViewById(R.id.imageView1_next);
        imageView_favorite = (ImageView) findViewById(R.id.imageView_favorite);
        seekBar1 = (SeekBar) findViewById(R.id.seekBar1);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        initViewPager();

        imageView4_play_mode.setOnClickListener(this);
        imageView3_prev.setOnClickListener(this);
        imageView2_play_pause.setOnClickListener(this);
        imageView1_next.setOnClickListener(this);
        imageView_favorite.setOnClickListener(this);
        seekBar1.setOnSeekBarChangeListener(this);



        //mp3Infos = MediaUtils.getMp3infos(this);//取得当前界面的歌曲


        myHandler = new MyHandler(this);

    }

    private void initViewPager() {

        LayoutInflater inflater = getLayoutInflater();
        View album_image_layout = inflater.inflate(R.layout.album_image_layout, null);
        View lrc_layout = inflater.inflate(R.layout.lrc_layout, null);

        textView3_title = (TextView) album_image_layout.findViewById(R.id.textView3_title);
        imageView1_album = (ImageView) album_image_layout.findViewById(R.id.imageView1_album);

        viewList.add(album_image_layout);
        viewList.add(lrc_layout);

        viewPager.setAdapter(new MyPagerAdapter(viewList));

    }


    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();//继承了BaseActivity,调用其方法绑定服务。
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindPlayService();//解除绑定。
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    //进度条快进播放
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            playService.pause();
            playService.seekTo(progress);
            playService.start();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    static class MyHandler extends Handler {
        private PlayActivity playActivity;

        public MyHandler(PlayActivity playActivity) {
            this.playActivity = playActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (null != playActivity) {
                switch (msg.what) {
                    case UPDATE_TIME:
                        playActivity.textView1_start_time.setText(MediaUtils.formatTime(msg.arg1));
                        break;
                }
            }
        }
    }

    /**
     * 更新进度值
     *
     * @param progress
     */
    @Override
    public void publish(int progress) {
        //*textView1_start_time.setText(MediaUtils.formatTime(progress));//不能直接调用这个线程，需要使用Handler
        Message msg = myHandler.obtainMessage(UPDATE_TIME);
        msg.arg1 = progress;//将progress传给msg.arg1
        myHandler.sendMessage(msg);//传送个UPDATE_TIME给handleMessage
        seekBar1.setProgress(progress);//seekbar内部组件做了处理，可以直接调用设置

    }

    /**
     * 当歌曲处于播放状态时，更新界面
     *
     * @param position
     */
    @Override
    public void change(int position) {
        Mp3Info mp3Info = playService.mp3Infos.get(position);
        textView3_title.setText(mp3Info.getTitle());
        Bitmap albumBitmap = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
        imageView1_album.setImageBitmap(albumBitmap);//改变album图片
        textView2_end_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        seekBar1.setProgress(0);
        seekBar1.setMax((int) mp3Info.getDuration());
        if (this.playService.isPlaying()) {
            imageView2_play_pause.setImageResource(R.mipmap.pause);
        } else {
            imageView2_play_pause.setImageResource(R.mipmap.play);
        }

        switch (playService.getPlay_mode()) {
            case PlayService.ORDER_PLAY:
                imageView4_play_mode.setImageResource(R.mipmap.order);
                imageView4_play_mode.setTag(PlayService.ORDER_PLAY);//标记播放模式的值
                break;
            case PlayService.RANDOM_PLAY:
                imageView4_play_mode.setImageResource(R.mipmap.random);
                imageView4_play_mode.setTag(PlayService.RANDOM_PLAY);
                break;
            case PlayService.SINGLE_PLAY:
                imageView4_play_mode.setImageResource(R.mipmap.single);
                imageView4_play_mode.setTag(PlayService.SINGLE_PLAY);
                break;
        }

        //初始化收藏状态
        try {
            Mp3Info likeMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=",mp3Info.getMp3InfoId()));
            if (null != likeMp3Info) {
                imageView_favorite.setImageResource(R.mipmap.xin_hong);
            } else {
                imageView_favorite.setImageResource(R.mipmap.xin_bai);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    private long getId(Mp3Info mp3Info){
        //初始化收藏状态
        long id=0;
        switch (playService.getChangePlayList()){
            case PlayService.MY_MUSIC_LIST:
                id=mp3Info.getId();
                break;
            case PlayService.LIKE_MUSIC_LIST:
                id=mp3Info.getMp3InfoId();
                break;
            default:
                break;
        }
        return id;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView2_play_pause: {
                if (playService.isPlaying()) {
                    imageView2_play_pause.setImageResource(R.mipmap.play);
                    playService.pause();
                } else {
                    if (playService.isPause()) {
                        imageView2_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
                        playService.start();
                    } else {
                        playService.play(playService.getCurrentPosition());
                    }
                }
                break;
            }

            case R.id.imageView1_next: {
                playService.next();
                break;
            }

            case R.id.imageView3_prev: {
                playService.prev();
                break;
            }

            case R.id.imageView4_play_mode: {
                int mode = (int) imageView4_play_mode.getTag();
                switch (mode) {
                    case PlayService.ORDER_PLAY:
                        imageView4_play_mode.setImageResource(R.mipmap.random);
                        imageView4_play_mode.setTag(PlayService.RANDOM_PLAY);//换图标的时候，把播放模式的值也改变
                        playService.setPlay_mode(PlayService.RANDOM_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.random_play), Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.RANDOM_PLAY:
                        imageView4_play_mode.setImageResource(R.mipmap.single);
                        imageView4_play_mode.setTag(PlayService.SINGLE_PLAY);
                        playService.setPlay_mode(PlayService.SINGLE_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.single_play), Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.SINGLE_PLAY:
                        imageView4_play_mode.setImageResource(R.mipmap.order);
                        imageView4_play_mode.setTag(PlayService.ORDER_PLAY);
                        playService.setPlay_mode(PlayService.ORDER_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.order_play), Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
            }

            case R.id.imageView_favorite: {
                Mp3Info mp3Info = playService.mp3Infos.get(playService.getCurrentPosition());
                try {
                    //mp3InfoId就是以前已经保存了的歌曲id
                    Mp3Info likeMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=",getId(mp3Info)));
                    if (null == likeMp3Info) {
                        //保存的时候会把原本的ID给替换掉.先把要保存的ID赋值给mp3InfoId，再保存
                        mp3Info.setMp3InfoId(mp3Info.getId());
                        mp3Info.setIsLike(1);
                        app.dbUtils.save(mp3Info);
                        imageView_favorite.setImageResource(R.mipmap.xin_hong);
                    } else {
                        int isLike=likeMp3Info.getIsLike();
                        if(isLike==1){
                            likeMp3Info.setIsLike(0);
                            imageView_favorite.setImageResource(R.mipmap.xin_bai);
                        }else{
                            likeMp3Info.setIsLike(1);
                            imageView_favorite.setImageResource(R.mipmap.xin_hong);
                        }
                        app.dbUtils.update(likeMp3Info,"isLike");
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }

            default:
                break;
        }

    }
}
