package com.ouyang.musicplayer;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ouyang.musicplayer.Utils.AppUtils;
import com.ouyang.musicplayer.Utils.Constant;
import com.ouyang.musicplayer.Utils.SearchMusicUtils;
import com.ouyang.musicplayer.adapter.NetMusicAdapter;
import com.ouyang.musicplayer.vo.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ASUS-PC on 2016/7/1.
 */
public class NetMusicListFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private MainActivity mainActivity;
    private ListView listView_net_music;
    private LinearLayout search_btn_container;
    private LinearLayout search_btn;
    private LinearLayout load_layout;
    private ImageButton ib_search_btn;
    private EditText et_search_content;
    private ArrayList<SearchResult> searchResults = new ArrayList<>();
    private NetMusicAdapter netMusicAdapter;
    private int page = 1;//搜索音乐的页码

    /**
     * 创建实例的方法
     *
     * @return
     */
    public static NetMusicListFragment newInstance() {
        NetMusicListFragment net = new NetMusicListFragment();
        return net;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();//调用Context的getActivity方法得到mainActivity对象.
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //初始化UI组件
        View view = inflater.inflate(R.layout.net_music_list, null);//得到view视图
        listView_net_music = (ListView) view.findViewById(R.id.listView_net_music);
        search_btn_container = (LinearLayout) view.findViewById(R.id.search_btn_container);
        search_btn = (LinearLayout) view.findViewById(R.id.search_btn);
        load_layout = (LinearLayout) view.findViewById(R.id.load_layout);
        ib_search_btn = (ImageButton) view.findViewById(R.id.ib_search_btn);
        et_search_content = (EditText) view.findViewById(R.id.et_search_content);

        //注册点击事件
        listView_net_music.setOnItemClickListener(this);
        search_btn_container.setOnClickListener(this);
        ib_search_btn.setOnClickListener(this);

        loadNetData();//加载网络音乐
        return view;

    }

    private void loadNetData() {
        load_layout.setVisibility(View.VISIBLE);//显示布局，就是progressBar

        //执行异步加载网络音乐任务
        new LoadNetDataTask().execute(Constant.BAIDU_URL + Constant.BAIDU_DAYHOT);

    }


    //列表项的单击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if(position>=netMusicAdapter.getSearchResults().size() || position<0)  return;
        showDownloadDialog(position);//调用showDownloadDialog方法
    }

    //显示下载弹框
    private void showDownloadDialog(final int position) {
        DownloadDialogFragment downloadDialogFragment=DownloadDialogFragment.newInstance(searchResults.get(position));//将当前点击下载的歌曲传入DownloadDialogFragment中
        downloadDialogFragment.show(getFragmentManager(),"download");//调用show方法
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_btn_container:
                search_btn_container.setVisibility(View.GONE);
                search_btn.setVisibility(View.VISIBLE);//隐藏search_btn_container视图，将search_btn显示出来。
                break;
            case R.id.ib_search_btn:
                searchMusic();//查询歌曲
                break;
        }

    }

    //搜索音乐
    private void searchMusic() {
        AppUtils.hideInputMethod(et_search_content);//将键盘隐藏
        search_btn_container.setVisibility(View.VISIBLE);
        search_btn.setVisibility(View.GONE);

        String key=et_search_content.getText().toString();//得到输入的关键字

        //判断关键字是否为空.TextUtils.isEmpty(key)可以判断key为null或者空字符串.
        if(TextUtils.isEmpty(key)){
            Toast.makeText(mainActivity,"请输入搜索关键字",Toast.LENGTH_SHORT).show();
            return;
        }

        load_layout.setVisibility(View.VISIBLE);//将load_layout布局显示出来

        SearchMusicUtils.getInstance().setListener(new SearchMusicUtils.OnSearchResultListener() {
            @Override
            public void onSearchResult(ArrayList<SearchResult> searchResults) {
                ArrayList<SearchResult> sr=netMusicAdapter.getSearchResults();//先拿到adapter里面的搜索歌曲，再更新数据源
                sr.clear();//清楚原先的数据
                sr.addAll(searchResults);//将所有搜查到的歌曲都添加进去
                netMusicAdapter.notifyDataSetChanged();//更新数据源
                load_layout.setVisibility(View.GONE);//设为不可见
            }
        }).search(key,page);//调用SearchMusicUtils里面的search方法
    }

    /**
     * 加载网络歌曲异步任务
     */
    class LoadNetDataTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            load_layout.setVisibility(View.VISIBLE);//显示视图
            listView_net_music.setVisibility(View.GONE);//不显示
            searchResults.clear();//清空列表
        }

        @Override
        protected Integer doInBackground(String... params) {

            String url = params[0];//第一个参数是url地址

            try {
                //使用Jsoup组件请求网络并解析音乐数据，要是在web端就不用解析
                Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6 * 1000).get();//timeout（）请求超时方法
                Elements songTitles = doc.select("span.song-title");//得到歌曲列表
                Elements artists = doc.select("span.author_list");//得到作者列表

                //通过for循坏得到所有歌曲信息
                for (int i = 0; i < songTitles.size(); i++) {
                    SearchResult searchResult = new SearchResult();

                    Elements urls = songTitles.get(i).getElementsByTag("a");//得到a链接
                    searchResult.setUrl(urls.get(0).attr("href"));//得到歌曲url地址，但不是源地址，还得通过这个地址去查找源地址
                    searchResult.setMusicName(urls.get(0).text());//得到歌名

                    Elements artistElements = artists.get(i).getElementsByTag("a");//
                    searchResult.setArtist(artistElements.get(0).text());//得到歌曲的作者

                    searchResult.setAlbum("热歌榜");
                    searchResults.add(searchResult);//将歌曲添加到搜索列表里面
                }
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == 1) {
                netMusicAdapter = new NetMusicAdapter(mainActivity, searchResults);//得到adapter对象
                listView_net_music.setAdapter(netMusicAdapter);//将适配器传入list控件中
                listView_net_music.addFooterView(LayoutInflater.from(mainActivity).inflate(R.layout.footview_layout, null));//将footview_layout加载进来
            }
            load_layout.setVisibility(View.GONE);//将视图隐藏
            listView_net_music.setVisibility(View.VISIBLE);//将视图显示出来
        }
    }

}
