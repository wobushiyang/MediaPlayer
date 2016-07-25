package com.ouyang.musicplayer.Utils;

import android.os.Environment;

import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;

import com.ouyang.musicplayer.vo.SearchResult;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ASUS-PC on 2016/7/22.
 */
public class DownloadUtils {

    private static final String DOWNLOAD_URL = "/download?__o=%2Fsearch%2Fsong";
    public static final int SUCCESS_LRC=1;//下载歌词成功
    public static final int FAILED_LRC=2;//下载歌词失败
    public static final int SUCCESS_MP3=3;//下载MP3成功
    public static final int FAILED_MP3=4;//下载MP3失败
    public static final int GET_MP3_URL=5;//下载MP3 URL成功
    public static final int GET_FAILED_MP3_URL=6;//下载MP3 URL失败
    public static final int MUSIC_EXISTS=7;//音乐已存在

    private static DownloadUtils sInstance;
    private OnDownloadListener mListener;

    private ExecutorService mThreadPool;//线程池

    //设置回调的监听器对象
    public DownloadUtils setListener(OnDownloadListener mListener){
        this.mListener=mListener;
        return this;
    }

    //获取下载工具的实例
    public synchronized static DownloadUtils getInstance(){
        if(null==sInstance){
            try {
                sInstance=new DownloadUtils();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }

    private DownloadUtils() throws ParserConfigurationException{
        mThreadPool= Executors.newSingleThreadExecutor();//单线程
    }

    //下载的具体业务方法
    public void download(final SearchResult searchResult){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case SUCCESS_LRC:
                        if(mListener!=null)   mListener.onDownload("歌词下载成功");
                        break;
                    case FAILED_LRC:
                        if(mListener!=null)  mListener.onFailed("歌词下载失败");
                        break;
                    case GET_MP3_URL:
                        downloadMusic(searchResult,(String)msg.obj,this);//下载歌曲
                        break;
                    case GET_FAILED_MP3_URL:
                        if(mListener!=null)  mListener.onFailed("下载失败，该歌曲为收费VIP类型或不存在");
                        break;
                    case SUCCESS_MP3:
                        if(mListener!=null)  mListener.onDownload(searchResult.getMusicName()+"已下载");
                        String url=Constant.BAIDU_URL+searchResult.getUrl();
                        downloadLRC(url,searchResult.getMusicName(),this);
                        break;
                    case FAILED_MP3:
                        if(mListener!=null) mListener.onFailed(searchResult.getMusicName()+"下载失败");
                        break;
                    case MUSIC_EXISTS:
                        if(mListener!=null) mListener.onFailed("歌曲已经存在");
                        break;
                    default:
                        break;
                }
            }
        };

        getDownloadMusicURL(searchResult,handler);//由于上面得到的mp3的URL并不是下载需要的URL,调用getDownloadMusicURL得到下载所需的
    }

    //下载歌词的方法
    public void downloadLRC(final String url,final String musicName, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc=Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6000).get();
                    Elements lrcTag = doc.select("div.lyric-content");
                    String lrcUrl = lrcTag.attr("data-lrclink");
                   // System.out.println("歌词URL地址："+lrcUrl);
                    File lrcDirFile = new File(Environment.getExternalStorageDirectory()+Constant.DIR_LRC);//创建目录
                    //判断目录是否存在
                    if (!lrcDirFile.exists()){


                        lrcDirFile.mkdirs();
                    }
                    lrcUrl = Constant.BAIDU_URL+lrcUrl;
                    String target = lrcDirFile+"/"+musicName+".lrc";

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(lrcUrl).build();//创建个请求
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()){
                        PrintStream printStream = new PrintStream(new File(target));
                        byte[] bytes = response.body().bytes();
                        printStream.write(bytes, 0,bytes.length);
                        printStream.close();
                        handler.obtainMessage(SUCCESS_LRC).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    //下载MP3歌曲
    private void downloadMusic(final SearchResult searchResult, final String url, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                File musicDirFile=new File(Environment.getExternalStorageDirectory()+Constant.DIR_MUSIC);//创建目录
                if(!musicDirFile.exists()){
                    musicDirFile.mkdirs();
                }
                String mp3url=Constant.BAIDU_URL+url;//最终下载的路径
                String target=musicDirFile+"/"+searchResult.getMusicName()+".mp3";//保存的具体路径

                File targetFile=new File(target);//创建目录
                if(targetFile.exists()){
                    handler.obtainMessage(MUSIC_EXISTS).sendToTarget();
                    return;
                }else{
                    //使用OkHttpClient三方组件
                    OkHttpClient client=new OkHttpClient();
                    Request request=new Request.Builder().url(mp3url).build();//请求
                    try {
                        Response response=client.newCall(request).execute();
                        if (response.isSuccessful()){
                            PrintStream ps = new PrintStream(targetFile);
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes, 0,bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_MP3).sendToTarget();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.obtainMessage(FAILED_MP3).sendToTarget();
                    }
                }
            }
        });
    }

    //获取下载音乐的url
    private void getDownloadMusicURL(final SearchResult searchResult, final Handler handler) {
        mThreadPool.execute(new Runnable(){
            @Override
            public void run() {
                //下载歌曲的URL
                String url = Constant.BAIDU_URL + "/song/" + searchResult.getUrl().substring(searchResult.getUrl().lastIndexOf("/") + 1) + DOWNLOAD_URL;//获取下载歌曲额URL，拼接起来
                try {
                    //使用Jsoup组件请求网络并解析音乐数据
                    Document doc= Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6000).get();
                    Elements targetElements = doc.select("a[data-btndata]");

                    //解析下载页面出现异常
                    if (targetElements.size() <= 0) {
                        handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                        return;
                    }
                    //循环查找
                    for (Element e : targetElements) {
                        if (e.attr("href").contains(".mp3")) {
                            String result = e.attr("href");
                            Message m = handler.obtainMessage(GET_MP3_URL, result);
                            m.sendToTarget();
                            return;
                        }
                        //如果是以vip开头的，就把歌曲移除
                        if (e.attr("href").startsWith("/vip")) {
                            targetElements.remove(e);
                        }
                    }

                    //上面在判断是否可以下载，循环判断后，看是否有歌曲可以下载
                    if (targetElements.size() <= 0) {
                        handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                        return;
                    }

                    //如果有，就从第一首开始下载
                    String results=targetElements.get(0).attr("href");
                    Message msg= handler.obtainMessage(GET_MP3_URL,results);
                    msg.sendToTarget();

                } catch (IOException e) {
                    e.printStackTrace();
                    handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();//出现异常，就发送获取URL失败
                }
            }
        });
    }

    //自定义下载事件监听器
    public interface OnDownloadListener{
        public void onDownload(String mp3Url);
        public void onFailed(String error);
    }
}
