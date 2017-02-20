package com.ruziniu.phonelive.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.utils.ShareUtils;

import java.util.ArrayList;

import butterknife.InjectView;
import wkvideoplayer.dlna.engine.DLNAContainer;
import wkvideoplayer.dlna.service.DLNAService;
import wkvideoplayer.model.Video;
import wkvideoplayer.model.VideoUrl;
import wkvideoplayer.util.DensityUtil;
import wkvideoplayer.view.MediaController;
import wkvideoplayer.view.SuperVideoPlayer;


//直播回放
public class VideoBackActivity extends ToolBarBaseActivity {

    @InjectView(R.id.video_player)
    SuperVideoPlayer mSuperVideoPlayer;
    private UserBean mLiveRecord;
    @InjectView(R.id.play_btn)
    ImageView mPlayBtnView;
    private ImageView mShare;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_videoback;
    }

    @Override
    public void initView() {
        mShare = (ImageView) findViewById(R.id.iv_live_shar);
        mPlayBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPlayerLive();
            }
        });
    }
    //开始播放视频
    private void startPlayerLive() {
        mPlayBtnView.setVisibility(View.GONE);
        Video video = new Video();
        VideoUrl videoUrl1 = new VideoUrl();
        videoUrl1.setFormatName("贼清楚p");
        //BBB
        video.setUid(String.valueOf(mLiveRecord.getUid()));
        video.setId(mLiveRecord.getVid());
        video.setAvatar(mLiveRecord.getAvatar());
        video.setNum(mLiveRecord.getNums());
        video.setHit(mLiveRecord.getHit());
        video.setUser_nicename(mLiveRecord.getUser_nicename());
        videoUrl1.setAvatar(mLiveRecord.getAvatar());
        videoUrl1.setNum(mLiveRecord.getNums());
        videoUrl1.setFormatUrl(mLiveRecord.getVideo_url());
        ArrayList<VideoUrl> arrayList1 = new ArrayList<>();
        arrayList1.add(videoUrl1);
        video.setVideoName("测试视频一");
        video.setVideoUrl(arrayList1);
        ArrayList<Video> videoArrayList = new ArrayList<>();
        videoArrayList.add(video);
        mSuperVideoPlayer.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().invalidate();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        mSuperVideoPlayer.loadMultipleVideo(videoArrayList,0,0,0);
    }
    //分享操作
    public void share1(View v){
        ShareUtils.share1(this,v.getId(),mLiveRecord);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startPlayerLive();
    }

    @Override
    public void initData() {
        mLiveRecord = (UserBean) getIntent().getSerializableExtra("video");
        //判断视频是否生成
        if(mLiveRecord.getVideo_url() != null && !TextUtils.isEmpty(mLiveRecord.getVideo_url())){
            mSuperVideoPlayer.setVideoPlayCallback(mVideoPlayCallback);
            startDLNAService();
        }else {
            showToast3("回放视频暂未生成", 0);
        }
    }

    @Override
    public void onClick(View view) {

    }

    private SuperVideoPlayer.VideoPlayCallbackImpl mVideoPlayCallback = new SuperVideoPlayer.VideoPlayCallbackImpl() {
        @Override
        public void onCloseVideo() {
            mSuperVideoPlayer.close();
            mPlayBtnView.setVisibility(View.VISIBLE);
            mSuperVideoPlayer.setVisibility(View.GONE);
            resetPageToPortrait();
        }

        @Override
        public void onSwitchPageType() {
            if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mSuperVideoPlayer.setPageType(MediaController.PageType.SHRINK);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mSuperVideoPlayer.setPageType(MediaController.PageType.EXPAND);
            }
        }

        @Override
        public void onPlayFinish() {

        }
    };
    /***
     * 恢复屏幕至竖屏
     */
    private void resetPageToPortrait() {
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mSuperVideoPlayer.setPageType(MediaController.PageType.SHRINK);
        }
    }

    private void startDLNAService() {
        // Clear the device container.
        DLNAContainer.getInstance().clear();
        Intent intent = new Intent(getApplicationContext(), DLNAService.class);
        startService(intent);
    }

    private void stopDLNAService() {
        Intent intent = new Intent(getApplicationContext(), DLNAService.class);
        stopService(intent);
    }
    /***
     * 旋转屏幕之后回调
     *
     * @param newConfig newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (null == mSuperVideoPlayer) return;
        /***
         * 根据屏幕方向重新设置播放器的大小
         */
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().getDecorView().invalidate();
            float height = DensityUtil.getWidthInPx(this);
            float width = DensityUtil.getHeightInPx(this);
            mSuperVideoPlayer.getLayoutParams().height = (int) width;
            mSuperVideoPlayer.getLayoutParams().width = (int) height;
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            final WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            float width = DensityUtil.getWidthInPx(this);
            float height = DensityUtil.dip2px(this, 200.f);
            mSuperVideoPlayer.getLayoutParams().height = (int) height;
            mSuperVideoPlayer.getLayoutParams().width = (int) width;
        }
    }
    @Override
    protected boolean hasActionBar() {
        return false;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDLNAService();
    }

    public static void startVideoBack(Context context, UserBean video){
        Intent intent = new Intent(context,VideoBackActivity.class);
        intent.putExtra("video",video);
        context.startActivity(intent);
    }

}
