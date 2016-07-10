package com.ouyang.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ouyang.musicplayer.R;
import com.ouyang.musicplayer.Utils.MediaUtils;
import com.ouyang.musicplayer.vo.Mp3Info;

import java.util.ArrayList;

/**
 * Created by ASUS-PC on 2016/7/1.
 */
public class MyMusicListAdapter extends BaseAdapter {
    private Context ctx;
    private ArrayList<Mp3Info> mp3Infos;
    public MyMusicListAdapter(Context ctx, ArrayList<Mp3Info> mp3Infos){
        this.ctx=ctx;
        this.mp3Infos=mp3Infos;
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    @Override
    public int getCount() {
        return mp3Infos.size();
    }

    @Override
    public Object getItem(int position) {
        return mp3Infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(null==convertView){
           convertView= LayoutInflater.from(ctx).inflate(R.layout.item_music_layout,null);
            vh=new ViewHolder();
            vh.textView1_title= (TextView) convertView.findViewById(R.id.textView1_title);
            vh.textView2_singer= (TextView) convertView.findViewById(R.id.textView2_singer);
            vh.textView3_time= (TextView) convertView.findViewById(R.id.textView3_time);
            convertView.setTag(vh);
        }
        vh= (ViewHolder) convertView.getTag();
        Mp3Info mp3Info=mp3Infos.get(position);
        vh.textView1_title.setText(mp3Info.getTitle());
        vh.textView2_singer.setText(mp3Info.getArtist());
        vh.textView3_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        return convertView;
    }

    static class ViewHolder{
        TextView textView1_title;
        TextView textView2_singer;
        TextView textView3_time;

    }
}
