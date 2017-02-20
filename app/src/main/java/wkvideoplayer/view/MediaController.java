package wkvideoplayer.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.ui.DisplayActivity;
import com.ruziniu.phonelive.ui.VideoBackActivity;
import com.ruziniu.phonelive.utils.ShareUtils;
import com.ruziniu.phonelive.utils.UIHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import wkvideoplayer.model.Video;
import wkvideoplayer.model.VideoUrl;

/**
 * Created by Ted on 2015/8/4.
 * MediaController
 */
public class MediaController extends FrameLayout implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private ImageView mPlayImg;//播放按钮
    private SeekBar mProgressSeekBar;//播放进度条
    private TextView mTimeTxt;//播放时间
    private ImageView mExpandImg;//最大化播放按钮
    private ImageView mShrinkImg;//缩放播放按钮
    private EasySwitcher mVideoSrcSwitcher;//视频源切换器
    private EasySwitcher mVideoFormatSwitcher;//视频清晰度切换器
    private View mMenuView;
    private View mMenuViewPlaceHolder;

    private MediaControlImpl mMediaControl;
    private ImageView mShare1;
    private static AlertDialog dialog;
    private ImageView mCloseView;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean isFromUser) {
        if (isFromUser)
            mMediaControl.onProgressTurn(ProgressState.DOING, progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mMediaControl.onProgressTurn(ProgressState.START, 0);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mMediaControl.onProgressTurn(ProgressState.STOP, 0);
    }

    private EasySwitcher.EasySwitcherCallbackImpl mSrcSwitcherCallback = new EasySwitcher.EasySwitcherCallbackImpl() {
        @Override
        public void onSelectItem(int position, String name) {
            mMediaControl.onSelectSrc(position);
        }

        @Override
        public void onShowList() {
            mMediaControl.alwaysShowController();
            mVideoFormatSwitcher.closeSwitchList();
        }
    };

    private EasySwitcher.EasySwitcherCallbackImpl mFormatSwitcherCallback = new EasySwitcher.EasySwitcherCallbackImpl() {
        @Override
        public void onSelectItem(int position, String name) {
            mMediaControl.onSelectFormat(position);
        }

        @Override
        public void onShowList() {
            mMediaControl.alwaysShowController();
            mVideoSrcSwitcher.closeSwitchList();
        }
    };

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.pause) {
            mMediaControl.onPlayTurn();
        } else if (view.getId() == R.id.expand) {
            mMediaControl.onPageTurn();
        } else if (view.getId() == R.id.shrink) {
            mMediaControl.onPageTurn();
        } else if (view.getId() == R.id.iv_live_shar) {
            showSharePopWindow(getContext(),view);
        }else if (view.getId() == R.id.video_close_view){
            mMediaControl.onCloseVideo();
        }
    }
    public static void showSharePopWindow(final Context context, View v) {
        View view = LayoutInflater.from(context).inflate(R.layout.pop_view_share1, null);
        PopupWindow p = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        view.measure(MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        p.setBackgroundDrawable(new BitmapDrawable());
        p.setOutsideTouchable(true);
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        p.showAtLocation(v, Gravity.NO_GRAVITY, location[0] + v.getWidth() / 2 - view.getMeasuredWidth() / 2, location[1] - view.getMeasuredHeight());
    }

    public void initVideoList(ArrayList<Video> videoList) {
        ArrayList<String> name = new ArrayList<>();
        for (Video video : videoList) {
            name.add(video.getVideoName());
        }
        mVideoSrcSwitcher.initData(name);
    }

    public void initPlayVideo(Video video) {
        ArrayList<String> format = new ArrayList<>();
        for (VideoUrl url : video.getVideoUrl()) {
            format.add(url.getFormatName());
        }
        mVideoFormatSwitcher.initData(format);
    }

    public void closeAllSwitchList() {
        mVideoFormatSwitcher.closeSwitchList();
        mVideoSrcSwitcher.closeSwitchList();
    }

    /**
     * 初始化精简模式
     */
    public void initTrimmedMode() {
        mMenuView.setVisibility(GONE);
        mMenuViewPlaceHolder.setVisibility(GONE);
        mExpandImg.setVisibility(INVISIBLE);
        mShrinkImg.setVisibility(INVISIBLE);
    }

    /***
     * 强制横屏模式
     */
    public void forceLandscapeMode(){
        mExpandImg.setVisibility(INVISIBLE);
        mShrinkImg.setVisibility(INVISIBLE);
    }


    public void setProgressBar(int progress, int secondProgress) {
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        if (secondProgress < 0) secondProgress = 0;
        if (secondProgress > 100) secondProgress = 100;
        mProgressSeekBar.setProgress(progress);
        mProgressSeekBar.setSecondaryProgress(secondProgress);
    }

    public void setPlayState(PlayState playState) {
        mPlayImg.setImageResource(playState.equals(PlayState.PLAY) ? R.drawable.biz_video_pause : R.drawable.biz_video_play);
    }

    public void setPageType(PageType pageType) {
        mExpandImg.setVisibility(pageType.equals(PageType.EXPAND) ? GONE : VISIBLE);
        mShrinkImg.setVisibility(pageType.equals(PageType.SHRINK) ? GONE : VISIBLE);
    }

    public void setPlayProgressTxt(int nowSecond, int allSecond) {
        mTimeTxt.setText(getPlayTime(nowSecond, allSecond));
    }

    public void playFinish(int allTime) {
        mProgressSeekBar.setProgress(0);
        setPlayProgressTxt(0, allTime);
        setPlayState(PlayState.PAUSE);
    }

    public void setMediaControl(MediaControlImpl mediaControl) {
        mMediaControl = mediaControl;
    }

    public MediaController(Context context) {
        super(context);
        initView(context);
    }

    public MediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public MediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.biz_video_media_controller, this);

        mShare1 = (ImageView) findViewById(R.id.iv_live_shar);
        mPlayImg = (ImageView) findViewById(R.id.pause);
        mProgressSeekBar = (SeekBar) findViewById(R.id.media_controller_progress);
        mVideoSrcSwitcher = (EasySwitcher) findViewById(R.id.video_src_switcher);
        mVideoFormatSwitcher = (EasySwitcher) findViewById(R.id.video_format_switcher);
        mTimeTxt = (TextView) findViewById(R.id.time);
        mCloseView = (ImageView) findViewById(R.id.video_close_view);
        mExpandImg = (ImageView) findViewById(R.id.expand);
        mShrinkImg = (ImageView) findViewById(R.id.shrink);
        mMenuView = findViewById(R.id.view_menu);
        mMenuViewPlaceHolder = findViewById(R.id.view_menu_placeholder);
        initData();
    }

    private void initData() {

        mProgressSeekBar.setOnSeekBarChangeListener(this);
        mPlayImg.setOnClickListener(this);
        mShrinkImg.setOnClickListener(this);
        mExpandImg.setOnClickListener(this);
        mShare1.setOnClickListener(this);
        mCloseView.setOnClickListener(this);
        setPageType(PageType.SHRINK);
        setPlayState(PlayState.PAUSE);
        mVideoFormatSwitcher.setEasySwitcherCallback(mFormatSwitcherCallback);
        mVideoSrcSwitcher.setEasySwitcherCallback(mSrcSwitcherCallback);
    }

    @SuppressLint("SimpleDateFormat")
    private String formatPlayTime(long time) {
        DateFormat formatter = new SimpleDateFormat("mm:ss");
        return formatter.format(new Date(time));
    }

    private String getPlayTime(int playSecond, int allSecond) {
        String playSecondStr = "00:00";
        String allSecondStr = "00:00";
        if (playSecond > 0) {
            playSecondStr = formatPlayTime(playSecond);
        }
        if (allSecond > 0) {
            allSecondStr = formatPlayTime(allSecond);
        }
        return playSecondStr + "/" + allSecondStr;
    }

    /**
     * 播放样式 展开、缩放
     */
    public enum PageType {
        EXPAND, SHRINK
    }

    /**
     * 播放状态 播放 暂停
     */
    public enum PlayState {
        PLAY, PAUSE
    }

    public enum ProgressState {
        START, DOING, STOP
    }


    public interface MediaControlImpl {
        void onPlayTurn();

        void onPageTurn();

        void onProgressTurn(ProgressState state, int progress);

        void onSelectSrc(int position);

        void onSelectFormat(int position);

        void alwaysShowController();

        void onCloseVideo();
    }

}
