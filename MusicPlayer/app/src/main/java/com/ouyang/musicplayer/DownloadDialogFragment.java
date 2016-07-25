package com.ouyang.musicplayer;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.ouyang.musicplayer.Utils.DownloadUtils;
import com.ouyang.musicplayer.vo.SearchResult;

import java.io.File;

/**
 * Created by ASUS-PC on 2016/7/14.
 */
public class DownloadDialogFragment extends DialogFragment {

    private SearchResult searchResult;//当前要下载的歌曲对象
    private MainActivity mainActivity;

    private String[] items;

    public static DownloadDialogFragment newInstance(SearchResult searchResult){
        DownloadDialogFragment downloadDialogFragment=new DownloadDialogFragment();
        downloadDialogFragment.searchResult=searchResult;
        return downloadDialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity= (MainActivity) getActivity();//将Activity拿过来
        items=new String[]{getString(R.string.download),getString(R.string.cancel)};
    }


    //创建对话框的事件方法
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder=new AlertDialog.Builder(mainActivity);
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        //执行下载
                        downloadMusic();
                        break;
                    case 1:
                        //取消
                        dialog.dismiss();
                        break;
                }
            }
        });
        return builder.show();
    }

    //下载音乐的方法
    private void downloadMusic() {
        Toast.makeText(mainActivity,"正在下载"+searchResult.getMusicName(),Toast.LENGTH_SHORT).show();//提示正在下载某首歌曲

        DownloadUtils.getInstance().setListener(new DownloadUtils.OnDownloadListener() {

            //下载成功
            @Override
            public void onDownload(String mp3Url) {
                Toast.makeText(mainActivity,mp3Url,Toast.LENGTH_SHORT).show();

//                //扫描新下载的歌曲
//                Uri contentUri=Uri.fromFile(new File(mp3Url));
//                Intent mediaScanIntent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
//                getContext().sendBroadcast(mediaScanIntent);
            }

            //下载失败
            @Override
            public void onFailed(String error) {
                Toast.makeText(mainActivity,error,Toast.LENGTH_SHORT).show();
            }
        }).download(searchResult);
    }
}
