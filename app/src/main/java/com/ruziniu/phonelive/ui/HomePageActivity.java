package com.ruziniu.phonelive.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.ruziniu.phonelive.AppConfig;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.adapter.DisplayVideoAdapter;
import com.ruziniu.phonelive.adapter.DisplayVideoPhoneAdapter;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.bean.OrderBean;
import com.ruziniu.phonelive.bean.PrivateChatUserBean;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.bean.UserHomePageBean;
import com.ruziniu.phonelive.utils.StringUtils;
import com.ruziniu.phonelive.utils.TLog;
import com.ruziniu.phonelive.utils.UIHelper;
import com.ruziniu.phonelive.widget.AvatarView;
import com.ruziniu.phonelive.widget.MyListView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.cybergarage.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 他人信息
 */
public class HomePageActivity extends ToolBarBaseActivity {
    @InjectView(R.id.tv_home_page_send_num)
    TextView mSendNum;//送出钻
    @InjectView(R.id.tv_home_page_uname)
    TextView mUNice;//昵称
    @InjectView(R.id.iv_home_page_sex)
    ImageView mUSex;
    @InjectView(R.id.iv_home_page_level)
    ImageView mULevel;
    @InjectView(R.id.av_home_page_uhead)
    AvatarView mUHead;//头像
    @InjectView(R.id.tv_home_page_follow)
    TextView mUFollowNum;//关注数
    @InjectView(R.id.tv_home_page_fans)
    TextView mUFansNum;//粉丝数
    @InjectView(R.id.tv_home_page_sign)
    TextView mUSign;//个性签名
    @InjectView(R.id.tv_home_page_sign2)
    TextView mUSign2;

    @InjectView(R.id.tv_home_page_num)
    TextView mUNum;
    @InjectView(R.id.ll_default_video)
    LinearLayout mDefaultVideoBg;

    @InjectView(R.id.ll_home_page_index)
    LinearLayout mHomeIndexPage;

    @InjectView(R.id.ll_home_page_video)
    LinearLayout mHomeVideoPage;

    @InjectView(R.id.home_page_uploadvideo)
    LinearLayout mUploadvideo;
    @InjectView(R.id.tv_home_page_index_btn)
    TextView mPageIndexBtn;

    @InjectView(R.id.tv_home_page_video)
    TextView mPageVideo;

    @InjectView(R.id.tv_home_page_phone)
    TextView mPagephone;

    @InjectView(R.id.tv_home_page_video_btn)
    TextView mPageVideoBtn;

    @InjectView(R.id.tv_home_page_menu_follow)
    TextView mFollowState;

    @InjectView(R.id.tv_home_page_black_state)
    TextView mTvBlackState;

    @InjectView(R.id.ll_home_page_bottom_menu)
    LinearLayout mLLBottomMenu;

    @InjectView(R.id.ll_home_page_uploadphone)
    LinearLayout mLLphone;

    @InjectView(R.id.lv_live_record)
    ListView mLiveRecordList;

   @InjectView(R.id.tv_address_information)
    TextView mAddressInfo;

    //当前选中的直播记录bean
    private UserBean mLiveRecordBean;
    private int uid;
    AvatarView[] mOrderTopNoThree = new AvatarView[3];
    private UserHomePageBean mUserHomePageBean;
    ArrayList<UserBean> mRecordList = new ArrayList<>();
    private GridView mDisplayVideo;
    private MyListView mDisplayPhone;
    private DisplayVideoAdapter videoAdapter;
    private UserBean videobean;
    private UserBean phonebean;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    public void initView() {
        mOrderTopNoThree[0] = (AvatarView) findViewById(R.id.av_home_page_order1);
        mOrderTopNoThree[1] = (AvatarView) findViewById(R.id.av_home_page_order2);
        mOrderTopNoThree[2] = (AvatarView) findViewById(R.id.av_home_page_order3);
        mLiveRecordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mLiveRecordBean = mRecordList.get(i);
                //开始播放
                showLiveRecord(i);
            }
        });
        mDisplayVideo = (GridView) findViewById(R.id.lv_displayvideo);
        mDisplayPhone = (MyListView) findViewById(R.id.lv_displayphone);
        mDisplayVideo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                videobean.setVideo_url(mRecordList.get(i).getUrl());
                PhoneLiveApi.videohit(mRecordList.get(i).getId(), new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        hideWaitDialog();
                        videobean.setHit((StringUtils.toInt(videobean.getHit())+1)+"");
                        videobean.setUpload(2);
                        VideoBackActivity.startVideoBack(HomePageActivity.this, videobean);
                    }
                    @Override
                    public void onResponse(String response) {
                        hideWaitDialog();
                        String res = ApiUtils.checkIsSuccess(response);
                        if (res!=null){
                            try {
                                JSONObject json = new JSONObject(res);
                                videobean.setHit(json.get("hit").toString().trim());
                                videobean.setUpload(2);
                                VideoBackActivity.startVideoBack(HomePageActivity.this,videobean);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });
            }
        });
        mDisplayPhone.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                phonebean = mRecordList.get(i);
                Bundle dBundle = new Bundle();
                dBundle.putString("phone", phonebean.getUrl());
                UIHelper.showBigPhone(HomePageActivity.this, dBundle);
            }
        });
    }

    @Override
    public void initData() {

        uid = getIntent().getIntExtra("uid", 0);
        UserBean user = AppContext.getInstance().getLoginUser();
        if (uid == user.getId()) {
            mLLBottomMenu.setVisibility(View.GONE);
        }
        //请求用户信息
        PhoneLiveApi.getHomePageUInfo(AppContext.getInstance().getLoginUid(), uid, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                AppContext.showToastAppMsg(HomePageActivity.this, "获取用户信息失败");
            }

            @Override
            public void onResponse(String response) {
                //TLog.log("cityAddress"+response);
                hideWaitDialog();
                String res = ApiUtils.checkIsSuccess(response);
                if (res != null) {
                    mUserHomePageBean = new Gson().fromJson(res, UserHomePageBean.class);
                    fillUI();
                }
            }
        });

    }

    private void fillList() {
        mLiveRecordList.setAdapter(new LiveRecordAdapter());
    }

    private class LiveRecordAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mRecordList.size();
        }

        @Override
        public Object getItem(int position) {
            return mRecordList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                convertView = View.inflate(HomePageActivity.this, R.layout.item_live_record, null);
                viewHolder = new ViewHolder();
                viewHolder.mLiveNum = (TextView) convertView.findViewById(R.id.tv_item_live_record_num);
                viewHolder.mLiveTime = (TextView) convertView.findViewById(R.id.tv_item_live_record_time);
                viewHolder.mLiveTitle = (TextView) convertView.findViewById(R.id.tv_item_live_record_title);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            UserBean l = mRecordList.get(position);
            viewHolder.mLiveNum.setText(l.getNums());
            viewHolder.mLiveTitle.setText(l.getTitle());
            viewHolder.mLiveTime.setText(l.getDatetime());
            return convertView;
        }

        class ViewHolder {
            public TextView mLiveTime, mLiveNum, mLiveTitle;
        }
    }

    private void fillUI() {//ui填充
        mSendNum.setText(getString(R.string.send) + "" + mUserHomePageBean.getConsumption());
        mUHead.setAvatarUrl(mUserHomePageBean.getAvatar());
        mUNice.setText(mUserHomePageBean.getUser_nicename());
        mUSex.setImageResource(mUserHomePageBean.getSex() == 1 ? R.drawable.global_male : R.drawable.global_female);
        mULevel.setImageResource(DrawableRes.LevelImg[(mUserHomePageBean.getLevel() == 0 ? 0 : mUserHomePageBean.getLevel() - 1)]);
        mUFansNum.setText(getString(R.string.fans) + ":" + mUserHomePageBean.getFansnum());
        mUFollowNum.setText(getString(R.string.attention) + ":" + mUserHomePageBean.getAttentionnum());
        mUSign.setText(mUserHomePageBean.getSignature());
        mUSign2.setText(mUserHomePageBean.getSignature());
        mAddressInfo.setText(mUserHomePageBean.getCity()+""+mUserHomePageBean.getAddress());
        mUNum.setText(mUserHomePageBean.getId() + "");
        mFollowState.setText(mUserHomePageBean.getIsattention() == 0 ? getString(R.string.follow2) : getString(R.string.alreadyfollow));
        mTvBlackState.setText(mUserHomePageBean.getIsblack() == 0 ? getString(R.string.pullblack) : getString(R.string.relieveblack));
        List<OrderBean> os = mUserHomePageBean.getCoinrecord3();
        for (int i = 0; i < os.size(); i++) {
            mOrderTopNoThree[i].setAvatarUrl(os.get(i).getAvatar());
        }

    }

    @OnClick({R.id.tv_location, R.id.iv_home_page_phone, R.id.home_page_uploadvideo, R.id.tv_home_page_video, R.id.ll_home_page_uploadphone, R.id.tv_home_page_phone, R.id.ll_home_page_menu_lahei, R.id.ll_home_page_menu_privatechat, R.id.tv_home_page_menu_follow, R.id.rl_home_pager_yi_order, R.id.tv_home_page_follow, R.id.tv_home_page_index_btn, R.id.tv_home_page_video_btn, R.id.iv_home_page_back, R.id.tv_home_page_fans})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_location:
                //定位
                joinAmap();
                break;
            case R.id.iv_home_page_phone:
                //打电话
                callPhone();
                break;
            case R.id.ll_home_page_menu_privatechat:
                openPrivateChat();
                break;
            case R.id.ll_home_page_menu_lahei:
                pullTheBlack();
                break;
            case R.id.tv_home_page_menu_follow:
                followUserOralready();
                break;
            case R.id.tv_home_page_index_btn:
                mHomeIndexPage.setVisibility(View.VISIBLE);
                mHomeVideoPage.setVisibility(View.GONE);
                mUploadvideo.setVisibility(View.GONE);
                mLLphone.setVisibility(View.GONE);
                mPageIndexBtn.setTextColor(getResources().getColor(R.color.black));
                mPageVideoBtn.setTextColor(getResources().getColor(R.color.global));
                mPageVideo.setTextColor(getResources().getColor(R.color.global));
                mPagephone.setTextColor(getResources().getColor(R.color.global));
                break;
            case R.id.tv_home_page_video_btn:
                mHomeIndexPage.setVisibility(View.GONE);
                mHomeVideoPage.setVisibility(View.VISIBLE);
                mUploadvideo.setVisibility(View.GONE);
                mLLphone.setVisibility(View.GONE);
                mPageIndexBtn.setTextColor(getResources().getColor(R.color.global));
                mPageVideoBtn.setTextColor(getResources().getColor(R.color.black));
                mPageVideo.setTextColor(getResources().getColor(R.color.global));
                mPagephone.setTextColor(getResources().getColor(R.color.global));
                mRecordList.clear();
                //直播记录回放
                requestData();
                break;
            case R.id.tv_home_page_video://视频
                mHomeIndexPage.setVisibility(View.GONE);
                mHomeVideoPage.setVisibility(View.GONE);
                mUploadvideo.setVisibility(View.VISIBLE);
                mLLphone.setVisibility(View.GONE);
                mPageIndexBtn.setTextColor(getResources().getColor(R.color.global));
                mPageVideoBtn.setTextColor(getResources().getColor(R.color.global));
                mPageVideo.setTextColor(getResources().getColor(R.color.black));
                mPagephone.setTextColor(getResources().getColor(R.color.global));
                getVideo();
                break;
            case R.id.tv_home_page_phone://图片
                mHomeIndexPage.setVisibility(View.GONE);
                mHomeVideoPage.setVisibility(View.GONE);
                mUploadvideo.setVisibility(View.GONE);
                mLLphone.setVisibility(View.VISIBLE);
                mPageIndexBtn.setTextColor(getResources().getColor(R.color.global));
                mPageVideoBtn.setTextColor(getResources().getColor(R.color.global));
                mPageVideo.setTextColor(getResources().getColor(R.color.global));
                mPagephone.setTextColor(getResources().getColor(R.color.black));
                getPhone();
                break;
            case R.id.iv_home_page_back:
                finish();
                break;
            case R.id.tv_home_page_fans:
                UIHelper.showFansActivity(this, uid);
                break;
            case R.id.tv_home_page_follow:
                UIHelper.showAttentionActivity(this, uid);
                break;
            case R.id.rl_home_pager_yi_order://映票排行榜
                UIHelper.showDedicateOrderActivity(this, uid);
                break;
        }

    }

    private void joinAmap() {
        UserBean userBean = AppContext.getInstance().getLoginUser();
        PhoneLiveApi.getMyUserInfo(userBean.getId(), userBean.getToken(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
            }

            @Override
            public void onResponse(String response) {
                String res = ApiUtils.checkIsSuccess(response);
                if(res == null){
                    UIHelper.showLoginSelectActivity(HomePageActivity.this);
                    finish();
                    return;
                }
                UserBean mInfo = new Gson().fromJson(res,UserBean.class);
                String t =mInfo.getLat_1();
                String g = mInfo.getLng_1();
                if (t != null && g != null&&!t.equals("")&&!g.equals("")) {
                    UIHelper.showWebView(HomePageActivity.this, AppConfig.MAIN_URL2 + "/index.php?g=home&m=Map&a=index&uid=" + mUserHomePageBean.getId() + "&lng=" + g + "&lat=" + t, "路线");
                }else{
                    Toast.makeText(HomePageActivity.this,"请确定定位权限是否打开，在重新登录",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //打电话
    private void callPhone() {
        String mobile = mUserHomePageBean.getMobile();
        if (mobile == null||mobile.equals("")) {
            AppContext.showToastAppMsg(HomePageActivity.this, "电话为空");
        }else {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:"+mobile));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            HomePageActivity.this.startActivity(intent);
        }
    }

    //展示图片
    private void getPhone() {
        PhoneLiveApi.imglist(uid, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                AppContext.showToastAppMsg(HomePageActivity.this, "网络请求出错");
            }

            @Override
            public void onResponse(String response) {

                JSONArray jsonArray = ApiUtils.arrayCheckIsSuccess(response);
                mRecordList.clear();
                if (jsonArray!=null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String jsonObject = jsonArray.getJSONObject(i).toString();
                            phonebean = new Gson().fromJson(jsonObject, UserBean.class);
                            mRecordList.add(phonebean);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                DisplayVideoPhoneAdapter aphonedapter = new DisplayVideoPhoneAdapter(HomePageActivity.this, HomePageActivity.this.getLayoutInflater(), mRecordList);
                mDisplayPhone.setAdapter(aphonedapter);
            }
        });
    }

    //展示视频
   private void getVideo() {
        PhoneLiveApi.videolist(uid, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                AppContext.showToastAppMsg(HomePageActivity.this, "网络请求出错");
            }

            @Override
            public void onResponse(String response) {
                JSONArray jsonArray = ApiUtils.arrayCheckIsSuccess(response);
                mRecordList.clear();
                if (jsonArray!=null){
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String jsonObject = jsonArray.getJSONObject(i).toString();
                            videobean = new Gson().fromJson(jsonObject, UserBean.class);
                             mRecordList.add(videobean);
                            } catch (JSONException e) {
                            e.printStackTrace();
                         }
                    }
                }
                DisplayVideoAdapter videoAdapter =  new DisplayVideoAdapter(HomePageActivity.this,HomePageActivity.this.getLayoutInflater(), mRecordList);
                mDisplayVideo.setAdapter(videoAdapter);
            }
        });
    }

    private void requestData() {
        PhoneLiveApi.getLiveRecord(getIntent().getIntExtra("uid", 0), requestLiveRecordDataCallback);
    }

    private StringCallback requestLiveRecordDataCallback = new StringCallback() {
        @Override
        public void onError(Call call, Exception e) {
            AppContext.showToastAppMsg(HomePageActivity.this, "获取直播纪录失败");
        }

        @Override
        public void onResponse(String response) {
            String res = ApiUtils.checkIsSuccess(response);
            if (null != res) {
                try {
                    JSONObject liveRecordJsonObj = new JSONObject(res);
                    JSONArray liveRecordJsonArray = liveRecordJsonObj.getJSONArray("list");
                    mRecordList.clear();
                    if (0 < liveRecordJsonArray.length()) {
                        Gson g = new Gson();
                        for (int i = 0; i < liveRecordJsonArray.length(); i++) {
                            mRecordList.add(g.fromJson(liveRecordJsonArray.getString(i), UserBean.class));
                        }
                        if (mRecordList.size() > 0) {
                            mDefaultVideoBg.setVisibility(View.GONE);
                        }
                    }
                    fillList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    private void showLiveRecord(final int i) {
        showWaitDialog("正在获取回放...");
        PhoneLiveApi.getLiveRecordById(mLiveRecordBean.getId() + "", new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                hideWaitDialog();
            }

            @Override
            public void onResponse(String response) {
                hideWaitDialog();
                String res = ApiUtils.checkIsSuccess(response);
                if (res != null) {
                    mLiveRecordBean.setVideo_url(res.trim());
                    PhoneLiveApi.livehit(mRecordList.get(i).getId(), new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                            hideWaitDialog();
                            mLiveRecordBean.setHit((StringUtils.toInt(mLiveRecordBean.getHit())+1)+"");
                            VideoBackActivity.startVideoBack(HomePageActivity.this, mLiveRecordBean);
                        }
                        @Override
                        public void onResponse(String response) {
                            hideWaitDialog();
                            String res = ApiUtils.checkIsSuccess(response);
                            if (res!=null){
                                try {
                                    JSONObject json = new JSONObject(res);
                                    mLiveRecordBean.setHit(json.get("hit").toString().trim());
                                    VideoBackActivity.startVideoBack(HomePageActivity.this,mLiveRecordBean);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
                } else {
                    //showToast3("视频暂未生成,请耐心等待",3);
                }
            }
        });
    }


    private void pullTheBlack() {// black list
        PhoneLiveApi.pullTheBlack(AppContext.getInstance().getLoginUid(), uid,
                AppContext.getInstance().getToken(),
                new StringCallback() {

                    @Override
                    public void onError(Call call, Exception e) {
                        AppContext.showToastAppMsg(HomePageActivity.this, "操作失败");
                    }

                    @Override
                    public void onResponse(String response) {
                        String res = ApiUtils.checkIsSuccess(response);
                        if (null == res) return;
                        if (mUserHomePageBean.getIsblack() == 0) {
                            //第二个参数如果为true，则把用户加入到黑名单后双方发消息时对方都收不到；false，则我能给黑名单的中用户发消息，但是对方发给我时我是收不到的
                            try {
                                EMClient.getInstance().contactManager().addUserToBlackList(String.valueOf(mUserHomePageBean.getId()), true);
                            } catch (HyphenateException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                EMClient.getInstance().contactManager().removeUserFromBlackList(String.valueOf(mUserHomePageBean.getId()));
                            } catch (HyphenateException e) {
                                e.printStackTrace();
                            }
                        }
                        AppContext.showToastAppMsg(HomePageActivity.this, mUserHomePageBean.getIsblack() == 0 ? "拉黑成功" : "解除拉黑");
                        mUserHomePageBean.setIsblack(mUserHomePageBean.getIsblack() == 0 ? 1 : 0);
                        mTvBlackState.setText(mUserHomePageBean.getIsblack() == 0 ? getString(R.string.pullblack) : getString(R.string.relieveblack));

                    }
                });
    }

    //私信
    private void openPrivateChat() {
        if (mUserHomePageBean.getIsblackto() == 1) {
            AppContext.showToastAppMsg(this, "你已被对方拉黑无法私信");
            return;
        }
        if (null != mUserHomePageBean) {
            PhoneLiveApi.getPmUserInfo(AppContext.getInstance().getLoginUid(), mUserHomePageBean.getId(), new StringCallback()  {
                @Override
                public void onError(Call call, Exception e) {

                }

                @Override
                public void onResponse(String response) {
                    String res = ApiUtils.checkIsSuccess(response);
                    if (null != res)
                        UIHelper.showPrivateChatMessage(HomePageActivity.this, new Gson().fromJson(res, PrivateChatUserBean.class));
                }
            });
        }
    }

    private void followUserOralready() {
        StringCallback callback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                mUserHomePageBean.setIsattention(mUserHomePageBean.getIsattention() == 0 ? 1 : 0);
                if (mUserHomePageBean.getIsattention() == 0 ){
                     mFollowState.setText(getString(R.string.follow2));
                }else{
                    mFollowState.setText(getString(R.string.alreadyfollow));
                    if (mUserHomePageBean.getIsblack() == 0){
                        return;
                    }else {
                        pullTheBlack();
                    }
                }
            }
        };
        PhoneLiveApi.showFollow(AppContext.getInstance().getLoginUid(), uid, AppContext.getInstance().getToken(), callback);
    }

    @Override
    protected boolean hasActionBar() {
        return false;
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {//BBB
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag("getHomePageUInfo");
        OkHttpUtils.getInstance().cancelTag("getAliCdnRecord");
        OkHttpUtils.getInstance().cancelTag("imglist");
        OkHttpUtils.getInstance().cancelTag("videolist");
        OkHttpUtils.getInstance().cancelTag("getLiveRecord");
        OkHttpUtils.getInstance().cancelTag("livehit");
        OkHttpUtils.getInstance().cancelTag("setBlackList");
    }
}
