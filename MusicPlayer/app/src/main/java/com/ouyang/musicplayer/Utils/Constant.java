package com.ouyang.musicplayer.Utils;

/**
 * Created by ASUS-PC on 2016/7/5.
 */
public class Constant {
    public static final String SP_NAME="PlayerMusic";//私有属性文件名
    public static final String DB_NAME="MusicList.db";//数据库名
    public static final int PLAY_RECORD_MAX_NUM=5;//最近播放列表显示的最大歌曲数

    //百度音乐地址
    public static final String BAIDU_URL="http://music.baidu.com/";
    //热歌榜
    public static final String BAIDU_DAYHOT="top/dayhot/?pst=shouyeTop";
    //搜索歌曲
    public static final String BAIDU_SEARCH="/search/song";

    //用户代理
    public static final String USER_AGENT="Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36 QIHU 360SE";

    public static final int SUCCESS=1;//成功标记
    public static final int FAILED=2;//失败标记

    //定义放歌曲和歌词的目录
    public static final String DIR_MUSIC="/MusicPlayer_music/music";
    public static final String DIR_LRC="/MusicPlayer_music/lrc";

}
