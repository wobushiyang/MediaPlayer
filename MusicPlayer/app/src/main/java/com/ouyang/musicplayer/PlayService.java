package com.ouyang.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.ouyang.musicplayer.Utils.MediaUtils;
import com.ouyang.musicplayer.vo.Mp3Info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 实现自定义功能：
 * 1、播放
 * 2、暂停
 * 3、下一首
 * 4、上一首
 * 5、获取当前播放进度
 * <p/>
 * 使用service的技巧：先开始(在SplashActivity里面开始服务)，再绑定。这样就不会在解绑的时候清除服务。
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer mPlayer;
    private int currentPosition;

    ArrayList<Mp3Info> mp3Infos;

    private MusicUpdateListener musicUpdateListener;

    //创建线程池
    private ExecutorService ex = Executors.newSingleThreadExecutor();

    //播放模式
    public static final int ORDER_PLAY = 1;
    public static final int RANDOM_PLAY = 2;
    public static final int SINGLE_PLAY = 3;
    private int play_mode = ORDER_PLAY;

    private boolean isPause = false;//判断暂停状态的标记。将判断值放在服务里面，供其他界面使用

    // 表示当前播放列表是我的音乐还是我喜爱的喜欢。
    public static final int MY_MUSIC_LIST = 1;//我的音乐播放列表
    public static final int LIKE_MUSIC_LIST = 2;//我喜欢的列表
    public static final int PLAY_RECORD_LIST = 3;//最近播放列表
    private int changePlayList = MY_MUSIC_LIST;

    public int getChangePlayList() {
        return changePlayList;
    }

    public void setChangePlayList(int changePlayList) {
        this.changePlayList = changePlayList;
    }

    public int getPlay_mode() {
        return play_mode;
    }

    /**
     * @param play_mode ORDER_PLAY=1
     *                  RANDOM_PLAY=2
     *                  SINGLE_PLAY=3
     */
    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    public boolean isPause() {
        return isPause;
    }

    public PlayService() {
    }

    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }

    /**
     * 在MyLikeMusicListActivity中可以通过此方法设置mp3Info
     *
     * @param mp3Infos
     */
    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    /**
     * 得到当前所在位置
     *
     * @return
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    private Random random = new Random();

    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (play_mode) {
            case ORDER_PLAY:
                next();
                break;
            case RANDOM_PLAY:
                play(random.nextInt(mp3Infos.size()));
                break;
            case SINGLE_PLAY:
                play(currentPosition);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    //提供得到playService的方法
    class PlayBinder extends Binder {
        public PlayService getPlayService() {
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //得到退出播放时，歌曲的状态值
        PlayerApplication app = (PlayerApplication) getApplication();
        currentPosition = app.sp.getInt("currentPosition", 0);
        play_mode = app.sp.getInt("play_mode", PlayService.ORDER_PLAY);

        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(this);//注册事件
        mPlayer.setOnErrorListener(this);
        mp3Infos = MediaUtils.getMp3infos(this);
        ex.execute(updateStatusRunnable);


    }

    /**
     * 销毁线程
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != ex && !ex.isShutdown()) {
            ex.shutdown();
            ex = null;
        }
    }


    Runnable updateStatusRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (null != musicUpdateListener && null != mPlayer && mPlayer.isPlaying()) {
                    musicUpdateListener.onPublish(getCurrentProgress());//在线程中调用onPublish方法更新状态
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    //播放
    public void play(int position) {
        Mp3Info mp3Info = null;
        if (position < 0 || position >= mp3Infos.size()) {
            position = 0;
        }
        mp3Info = mp3Infos.get(position);
        try {
            mPlayer.reset();
            mPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
            mPlayer.prepare();
            mPlayer.start();
            currentPosition = position;//将在播放的歌曲位置赋给currentPosition
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null != musicUpdateListener) {
            musicUpdateListener.onChange(currentPosition);
        }

    }

    //暂停
    public void pause() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            isPause = true;
        }
    }

    //下一首
    public void next() {
        if (currentPosition + 1 > mp3Infos.size() - 1) {
            currentPosition = 0;
        } else {
            currentPosition++;
        }
        play(currentPosition);
    }

    //上一首
    public void prev() {
        if (currentPosition - 1 < 0) {
            currentPosition = mp3Infos.size() - 1;
        } else {
            currentPosition--;
        }
        play(currentPosition);
    }

    //播放歌曲，暂停后启动
    public void start() {
        if (null != mPlayer && !mPlayer.isPlaying()) {
            mPlayer.start();
        }
    }

    public boolean isPlaying() {
        if (null != mPlayer) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    public int getCurrentProgress() {
        if (null != mPlayer && mPlayer.isPlaying()) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    //得到歌曲时间
    public int getDuration() {
        return mPlayer.getDuration();
    }

    //跳转到某一播放时间
    public void seekTo(int msec) {
        mPlayer.seekTo(msec);
    }


    //更新状态的接口（使用观察者设计模式）
    public interface MusicUpdateListener {
        public void onPublish(int progress);//更新播放状态

        public void onChange(int position);//更换歌曲后的更新方法
    }

    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }
}
