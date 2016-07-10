package com.ouyang.musicplayer.Utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.ouyang.musicplayer.R;
import com.ouyang.musicplayer.vo.Mp3Info;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**import
 * Created by Administrator on 2015/11/17.
 */
public class MediaUtils {
    // 获取专辑封面的Uri
    private static final Uri albumArtUri = Uri.
            parse("content://media/external/audio/albumart");
    /*
    根据歌曲id查询歌曲信息
    @param context
    @param _id
    @return
     */
    // 获取单收歌
    public static Mp3Info getMp3info(Context context, long _id) {

        //  System.out.println(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        //查询数据库
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media._ID + "=" + _id, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        Mp3Info mp3info = null;

        //判断
        if (cursor.moveToNext()) {
            //封装成Mp3对象
            mp3info = new Mp3Info();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));//音乐id

            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));//音乐标题

            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家

            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));//专辑

            long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));//专辑id

            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));//时长

            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));//文件大小

            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));//文件路径

            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐

            //只把音乐添加到集合当中
            if (isMusic != 0) {
                mp3info.setId(id);
                mp3info.setTitle(title);
                mp3info.setArtist(artist);
                mp3info.setAlbum(album);
                mp3info.setAlbumId(albumId);
                mp3info.setDuration(duration);
                mp3info.setSize(size);
                mp3info.setUrl(url);

            }
        }
        cursor.close();
        return mp3info;

    }
    /*
    用于从数据库中查询歌曲的信息，保存在List当中
    @return
     */
//获取多收歌
    public static long[] getMp3infoIds(Context context) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,
                MediaStore.Audio.Media.DURATION + ">=180000", null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        long[] ids =null;
        if (cursor!=null){
            ids = new long[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {
                ids[i] = cursor.getLong(0);
            }
        }
        cursor.close();
        return ids;
    }

    /*
    用于从数据库中查询歌曲的信息，保存在List当中
    @return
     */
    public static ArrayList<Mp3Info> getMp3infos(Context context) {
        //系统打印
        System.out.print(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.DURATION + ">=180000", null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        ArrayList<Mp3Info> mp3infos = new ArrayList<Mp3Info>();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToNext();
            Mp3Info mp3info = new Mp3Info();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));//音乐id

            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));//音乐标题

            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家

            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));//专辑

            long albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));//专辑id

            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));//时长

            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));//文件大小

            String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));//文件路径

            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐

            //只把音乐添加到集合当中
            if (isMusic != 0) {
                mp3info.setId(id);
                mp3info.setTitle(title);
                mp3info.setArtist(artist);
                mp3info.setAlbum(album);
                mp3info.setAlbumId(albumId);
                mp3info.setDuration(duration);
                mp3info.setSize(size);
                mp3info.setUrl(url);
                mp3infos.add(mp3info);

            }
        }
        cursor.close();
        return mp3infos;

    }



    /*
    往List集合中添加Map对象数据，每个Map对下属哪个存放一首音乐的所有属性
    @param mp3infos
    @return
     */

    public static List<HashMap<String, String>> getMusicMaps(List<Mp3Info> mp3infos) {
        List<HashMap<String, String>> mp3list = new ArrayList<>();
        for (Iterator iterator = mp3infos.iterator(); iterator.hasNext(); ) {
            Mp3Info mp3info = (Mp3Info) iterator.next();
            HashMap<String, String> map = new HashMap<>();
            map.put("title", mp3info.getTitle());
            map.put("Artist", mp3info.getArtist());
            map.put("album", mp3info.getAlbum());
            map.put("albumId", String.valueOf(mp3info.getAlbumId()));
            map.put("duration", formatTime(mp3info.getDuration()));
            map.put("size", String.valueOf(mp3info.getSize()));
            map.put("url", mp3info.getUrl());
            mp3list.add(map);
        }
        return mp3list;
    }

    /*

    格式化时间，将毫秒转换为分：秒格式
    @param time
    @return
     */

    public static String formatTime(long time) {
        //time应该为歌曲时间
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        //2到4分钟的歌曲，我的理解
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }

        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }

    /*
    获取默认专辑图片
     @param context
       @return
     */
    public static Bitmap getDefaultArtwork(Context context, boolean small) {

        Options opts=new Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        if (small) {//返回小图片
            return BitmapFactory.decodeStream(context.getResources().openRawResource(R.mipmap.app_logo2), null, opts);
        }
        return BitmapFactory.decodeStream(context.getResources().openRawResource(R.mipmap.app_logo2), null, opts);

    }

    /*
    从文件当中获取专辑封面位图
    @param context
    @param songid
    @param albumid
    @return
     */
    private static Bitmap getActworkFromFile(Context context, long songid, long albumid) {

        Bitmap bm = null;
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException(
                    "Must specify an album or a song id");
        }
        try {
            Options options = new Options();
            FileDescriptor fd = null;
            if (albumid < 0) {
                System.out.println("------------------"+"content://media/external/audio/media"
                        + songid + "albumart");
                Uri uri = Uri.parse("content://media/external/audio/media"
                        + songid + "albumart");
                ParcelFileDescriptor pfd = context.getContentResolver()
                        .openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver()
                        .openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
            options.inSampleSize = 1;
            //只进行大小判断
            options.inJustDecodeBounds = true;
            //调用此方法得到options,得到图片的大小
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            //在800pixel的画面上显示
            //所有需要调用computerSampleSize得到图片的缩放的比例
            options.inSampleSize = 100;
            options.inJustDecodeBounds=false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            //根据options参数，减少所需要的内存
            bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bm;

    }

    /*
    获取专辑封面位图对象
    @param context
    @param song_id
    @param album_id
    @param allowdefalut
    @return
     */

    public static Bitmap getArtwork(Context context, long song_id,
                                    long album_id, boolean allowdefalut, boolean small) {
        if (album_id < 0) {
            if (song_id < 0) {
                Bitmap bm = getActworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowdefalut) {
                //  return getDefaultArtwork(context, small);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                Options options = new Options();
                //先制定原始大小
                options.inSampleSize = 1;
                //只进行大小判断
                options.inJustDecodeBounds = true;
                //调用此方法得到options得到图片的大小
                BitmapFactory.decodeStream(in, null, options);

                if (small) {
                    options.inSampleSize = computeSampleSize(options, 40);
                } else {
                    options.inSampleSize = computeSampleSize(options, 600);
                }
                //我们得到了缩放比例，开始读入Bitmap数据
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, options);

            } catch (FileNotFoundException e) {
                Bitmap bm = getActworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefalut) {
                            return getDefaultArtwork(context, small);
                        }
                    }
                } else if (allowdefalut) {
                    bm = getDefaultArtwork(context, small);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /*
    对图片进行核实的缩放
    @param options
    @param target
    @return
     */

    public static int computeSampleSize(Options options, int target) {
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW, candidateH);
        if (candidate == 0) {
            return 1;
        }
        if (candidate > 1) {
            if ((w > target) && (w / candidate) < target) {
                candidate -= 1;
            }
        }
        if (candidate > 1) {
            if ((h > target) && (h / candidate) < target) {
                candidate -= 1;
            }
        }
        return candidate;
    }


}

