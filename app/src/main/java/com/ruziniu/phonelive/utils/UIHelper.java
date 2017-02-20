package com.ruziniu.phonelive.utils;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.bean.PrivateChatUserBean;
import com.ruziniu.phonelive.bean.SimpleBackPage;
import com.ruziniu.phonelive.em.ChangInfo;
import com.ruziniu.phonelive.fragment.ManageListDialogFragment;
import com.ruziniu.phonelive.ui.ActionBarSimpleBackActivity;
import com.ruziniu.phonelive.ui.AllActivity;
import com.ruziniu.phonelive.ui.AttentionActivity;
import com.ruziniu.phonelive.ui.BigPhoneActivity;
import com.ruziniu.phonelive.ui.DedicateOrderActivity;
import com.ruziniu.phonelive.ui.EditInfoActivity;
import com.ruziniu.phonelive.ui.FansActivity;
import com.ruziniu.phonelive.ui.HomePageActivity;
import com.ruziniu.phonelive.ui.LiveLoginSelectActivity;
import com.ruziniu.phonelive.ui.LiveRecordActivity;
import com.ruziniu.phonelive.ui.MainActivity;
import com.ruziniu.phonelive.ui.NearbyActivity;
import com.ruziniu.phonelive.ui.PhoneLoginActivity;
import com.ruziniu.phonelive.ui.ReadyStartLiveActivity;
import com.ruziniu.phonelive.ui.RegionActivity;
import com.ruziniu.phonelive.ui.SettingActivity;
import com.ruziniu.phonelive.ui.SimpleBackActivity;
import com.ruziniu.phonelive.ui.UploadActivity;
import com.ruziniu.phonelive.ui.UserChangeSexActivity;
import com.ruziniu.phonelive.ui.UserDiamondsActivity;
import com.ruziniu.phonelive.ui.UserInfoDetailActivity;
import com.ruziniu.phonelive.ui.UserLevelActivity;
import com.ruziniu.phonelive.ui.UserProfitActivity;
import com.ruziniu.phonelive.ui.UserSelectAvatarActivity;
import com.ruziniu.phonelive.ui.VideoPlayerActivity;
import com.ruziniu.phonelive.ui.WebViewActivity;

/**
 * 界面帮助类
 * 
 * @author FireAnt（http://my.oschina.net/LittleDY）
 * @version 创建时间：2014年10月10日 下午3:33:36
 * 
 */

public class UIHelper {
    /**
     * 发送通知广播
     *
     * @param context
     */
    public static void sendBroadcastForNotice(Context context) {
        /*Intent intent = new Intent(NoticeService.INTENT_ACTION_BROADCAST);
        context.sendBroadcast(intent);*/
    }
    /**
     * 手机登录
     *
     * @param context
     */

    public static void showMobilLogin(Context context) {
        Intent intent = new Intent(context, PhoneLoginActivity.class);
        context.startActivity(intent);
    }
    /**
     * 登陆选择
     *
     * @param context
     */
    public static void showLoginSelectActivity(Context context) {
        Intent intent = new Intent(context, LiveLoginSelectActivity.class);
        context.startActivity(intent);

    }

    /**
     * 首页
     *
     * @param context
     */
    public static void showMainActivity(Context context) {
        Intent intent = new Intent(context,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }
    /**
     * 我的详细资料
     *
     * @param context
     */
    public static void showMyInfoDetailActivity(Context context) {
        Intent intent = new Intent(context, UserInfoDetailActivity.class);
        context.startActivity(intent);
    }
    /**
     * 编辑资料
     *
     * @param context
     */
    public static void showEditInfoActivity(UserInfoDetailActivity context, String action,
                                            String prompt, String defaultStr, ChangInfo changInfo) {
        Intent intent = new Intent(context, EditInfoActivity.class);
        intent.putExtra(EditInfoActivity.EDITACTION,action);
        intent.putExtra(EditInfoActivity.EDITDEFAULT,defaultStr);
        intent.putExtra(EditInfoActivity.EDITPROMP,prompt);
        intent.putExtra(EditInfoActivity.EDITKEY, changInfo.getAction());
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.activity_open_start, 0);
    }

    public static void showSelectAvatar(UserInfoDetailActivity context, String avatar) {
        Intent intent = new Intent(context, UserSelectAvatarActivity.class);
        intent.putExtra("uhead",avatar);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.activity_open_start, 0);
    }

    /**
     * 获取webviewClient对象
     *
     * @return
     */
    public static WebViewClient getWebViewClient() {

        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //showUrlRedirect(view.getContext(), url);
                return true;
            }
        };
    }

    /**
     * 我的等级
     *
     * @return
     */
    public static void showLevel(Context context, int loginUid) {
        Intent intent = new Intent(context, UserLevelActivity.class);
        intent.putExtra("USER_ID",String.valueOf(loginUid));
        context.startActivity(intent);
    }
    /**
     * 我的钻石
     *
     * @return
     */
    public static void showMyDiamonds(Context context, Bundle bundle) {
        Intent intent = new Intent(context, UserDiamondsActivity.class);
        intent.putExtra("USERINFO",bundle);
        context.startActivity(intent);
    }

    /**
     * 图片放大
     *
     * @return
     */
    public static void showBigPhone(Context context, Bundle bundle) {
        Intent intent = new Intent(context, BigPhoneActivity.class);
        intent.putExtra("BIGPHONE",bundle);
        context.startActivity(intent);
    }
    /**
     * 我的收益
     *
     * @return
     */
    public static void showProfitActivity(Context context, Bundle bundle) {
        Intent intent = new Intent(context, UserProfitActivity.class);
        intent.putExtra("USERINFO",bundle);
        context.startActivity(intent);
    }
    /**
     * 设置
     *
     * @return
     */
    public static void showSetting(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }
    /**
     * 看直播
     *
     * @return
     */
    public static void showLookLiveActivity(Context context, Bundle bundle) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.USER_INFO,bundle);
        context.startActivity(intent);
    }
   /* *
     * 地图
     *
     * @return
    public static void showAmapActivity(Context context) {
        Intent intent = new Intent(context, AmapActivity.class);
        context.startActivity(intent);
    }*/

    /**
     * 地区
     *
     * @return
     */
    public static void showRegionActivity(Context context) {
        Intent intent = new Intent(context, RegionActivity.class);
        context.startActivity(intent);
    }

    /**
     * 全部
     *
     * @return
     */
    public static void showAllActivity(Context context) {
        Intent intent = new Intent(context, AllActivity.class);
        context.startActivity(intent);
    }

    /**
     * 附近
     *
     * @return
     */
    public static void showNearbyActivity(Context context) {
        Intent intent = new Intent(context, NearbyActivity.class);
        context.startActivity(intent);
    }
    /**
     * 直播
     *
     * @return
     */
    public static void showStartLiveActivity(Context context) {
        Intent intent = new Intent(context, ReadyStartLiveActivity.class);
        context.startActivity(intent);
    }
    /*
    * 其他用户个人信息
    * */
    public static void showHomePageActivity(Context context,int id) {
        Intent intent = new Intent(context, HomePageActivity.class);
        intent.putExtra("uid",id);
        context.startActivity(intent);
    }
    /*
    * 粉丝列表
    * */
    public static void showFansActivity(Context context, int uid) {
        Intent intent = new Intent(context, FansActivity.class);
        intent.putExtra("uid",uid);
        context.startActivity(intent);
    }
    /*
    * 关注列表
    * */
    public static void showAttentionActivity(Context context, int uid) {
        Intent intent = new Intent(context, AttentionActivity.class);
        intent.putExtra("uid",uid);
        context.startActivity(intent);
    }
    //映票贡献榜
    public static void showDedicateOrderActivity(Context context, int uid) {

        Intent intent = new Intent(context, DedicateOrderActivity.class);
        intent.putExtra("uid",uid);
        context.startActivity(intent);
    }
    //直播记录
    public static void showLiveRecordActivity(Context context, int uid) {
        Intent intent = new Intent(context, LiveRecordActivity.class);
        intent.putExtra("uid",uid);
        context.startActivity(intent);
    }
    //私信页面
    public static void showPrivateChatSimple(Context context, int uid) {
        Intent intent = new Intent(context, SimpleBackActivity.class);
        intent.putExtra("uid",uid);
        intent.putExtra(SimpleBackActivity.BUNDLE_KEY_PAGE, SimpleBackPage.USER_PRIVATECORE.getValue());
        context.startActivity(intent);
    }
    //私信详情
    public static void showPrivateChatMessage(Context context, PrivateChatUserBean user) {
        Intent intent = new Intent(context, SimpleBackActivity.class);
        intent.putExtra("user",user);
        intent.putExtra(SimpleBackActivity.BUNDLE_KEY_PAGE, SimpleBackPage.USER_PRIVATECORE_MESSAGE.getValue());
        context.startActivity(intent);

    }
    //地区选择
    public static void showSelectArea(Context context) {
        Intent intent = new Intent(context,SimpleBackActivity.class);
        intent.putExtra(SimpleBackActivity.BUNDLE_KEY_PAGE,SimpleBackPage.AREA_SELECT.getValue());
        context.startActivity(intent);
    }
    //搜索
    public static void showScreen(Context context) {
        Intent intent = new Intent(context,SimpleBackActivity.class);
        intent.putExtra(SimpleBackActivity.BUNDLE_KEY_PAGE,SimpleBackPage.INDEX_SECREEN.getValue());
        context.startActivity(intent);
    }
    //打开网页
    public static void showWebView(Context context,String url, String title) {
        Intent intent = new Intent(context, WebViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("url",url);
        bundle.putString("title",title);
        intent.putExtra("URL_INFO",bundle);
        context.startActivity(intent);
    }
    //黑名单
    public static void showBlackList(Context context) {
        Intent intent = new Intent(context,ActionBarSimpleBackActivity.class);
        intent.putExtra(ActionBarSimpleBackActivity.BUNDLE_KEY_PAGE,SimpleBackPage.USER_BLACK_LIST.getValue());
        context.startActivity(intent);
    }
    //推送管理
    public static void showPushManage(Context context) {
        Intent intent = new Intent(context,ActionBarSimpleBackActivity.class);
        intent.putExtra(ActionBarSimpleBackActivity.BUNDLE_KEY_PAGE,SimpleBackPage.USER_PUSH_MANAGE.getValue());
        context.startActivity(intent);
    }
    //搜索歌曲
    public static void showSearchMusic(Activity context) {
        Intent intent = new Intent(context,SimpleBackActivity.class);
        intent.putExtra(SimpleBackActivity.BUNDLE_KEY_PAGE,SimpleBackPage.LIVE_START_MUSIC.getValue());
        context.startActivityForResult(intent,1);
    }
    //管理员列表
    public static void shoManageListActivity(Context context) {
        Intent intent = new Intent(context,ManageListDialogFragment.class);
        context.startActivity(intent);
    }

    public static void showChangeSex(Context context, int sex) {
        Intent intent = new Intent(context, UserChangeSexActivity.class);
        intent.putExtra("SEX",sex);
        context.startActivity(intent);

    }

    public static void showUpload(Context context, String choose) {
        Intent intent = new Intent(context, UploadActivity.class);
        intent.putExtra("choose",choose);
        context.startActivity(intent);
    }
}
