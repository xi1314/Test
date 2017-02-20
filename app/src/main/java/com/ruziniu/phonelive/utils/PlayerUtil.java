package com.ruziniu.phonelive.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ruziniu.phonelive.widget.VideoSurfaceView;

import java.io.IOException;

/**
 * Created by Administrator on 2016/7/5.
 */
public class PlayerUtil {


    private  String TAG = "PlayerUtil";

    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
             mp.start();
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            long duration = mp.getDuration();
            long progress = duration * percent/100;
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangeListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {

        }
    };

    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompletedListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer mp) {

        }
    };

    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {

        }
    };
    //错误异常监听
    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            switch (what)
            {
                case KSYMediaPlayer.MEDIA_ERROR_UNKNOWN:
                    Log.e(TAG, "OnErrorListener, Error Unknown:" + what + ",extra:" + extra);
                    break;
                default:
                    Log.e(TAG, "OnErrorListener, Error:" + what + ",extra:" + extra);
            }

            return false;
        }
    };

    public IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {

            return false;
        }
    };

    public  KSYMediaPlayer CreatePlayer(Context context,String mrl,VideoSurfaceView surfaceView)
    {
        KSYMediaPlayer ksyMediaPlayer = new KSYMediaPlayer.Builder(context).build();
        /*
        * 参数：OnBufferingUpdateListener
        * 功能：设置Buffering的监听器，当播放器在Buffering时会发出此回调，通知外界Buffering的进度
        * 返回值：无
        * */
        ksyMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        /*
        * 参数：OnCompletionListener
        * 功能：设置Completion的监听器，在视频播放完成后会发出此回调
        * 返回值：无
        * */
        ksyMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        /*
        * 参数：OnPreparedListener
        * 功能：设置Prepared状态的监听器，在调用prepare()/prepareAsync()之后，正常完成解析后会通过此监听器通知外界。
        * 返回值：无
        * */
        ksyMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        /*
        *参数：OnInfoListener
        *功能：设置Info监听器，播放器可通过此回调接口将消息通知开发者
        *返回值：无
        * */
        ksyMediaPlayer.setOnInfoListener(mOnInfoListener);
        /*参数：OnVideoSizeChangedListener
        功能：设置VideoSizeChanged的监听器，当视频的宽度或高度发生变化时会发出次回调，通知外界视频的最新宽度和高度*/
        ksyMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangeListener);
        /*参数：OnErrorListener
        功能：设置Error监听器，当播放器遇到error时，会发出此回调并送出error code
        返回值：无*/
        ksyMediaPlayer.setOnErrorListener(mOnErrorListener);
        /*参数：OnSeekCompleteListener
        功能：设置Seek Complete的监听器，Seek操作完成后会有此回调
        返回值：无*/
        ksyMediaPlayer.setOnSeekCompleteListener(mOnSeekCompletedListener);
        /*参数：screenOn 值为true时，播放时屏幕保持常亮，反之则否
        功能：使用SurfaceHolder控制播放期间屏幕是否保持常亮。须调用接口setDisplay设置SurfaceHolder，此接口才有效
        返回值：无*/
        ksyMediaPlayer.setScreenOnWhilePlaying(true);
        /*参数：直播音频缓存最大值，单位为秒
        功能：设置直播音频缓存上限，由此可控制追赶功能的阈值。该值为负数时，关闭直播追赶功能。此接口只对直播有效
        返回值：无*/
        ksyMediaPlayer.setBufferTimeMax(5);

        try {
            ksyMediaPlayer.setDataSource(mrl);
            ksyMediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return  ksyMediaPlayer;
    }

    public  static VideoSurfaceView CreateVideoSurfaceView(Context context,int width,int height)
    {

        VideoSurfaceView videoView=new VideoSurfaceView(context);
        ViewGroup.LayoutParams lp=new ViewGroup.LayoutParams(80,80);
        videoView.setLayoutParams(lp);
        videoView.setVideoDimension(80,80);
        return  videoView;

    }


}
