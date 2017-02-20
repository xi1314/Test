package com.ruziniu.phonelive.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.BaseFragment;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.ui.AllActivity;
import com.ruziniu.phonelive.ui.NearbyActivity;
import com.ruziniu.phonelive.ui.RegionActivity;
import com.ruziniu.phonelive.ui.VideoBackActivity;
import com.ruziniu.phonelive.ui.listener.AutoLoadListener;
import com.ruziniu.phonelive.utils.LiveUtils;
import com.ruziniu.phonelive.utils.StringUtils;
import com.ruziniu.phonelive.utils.UIHelper;
import com.ruziniu.phonelive.viewpagerfragment.IndexPagerFragment;
import com.ruziniu.phonelive.widget.AvatarView;
import com.ruziniu.phonelive.widget.LoadUrlImageView;
import com.ruziniu.phonelive.widget.WPSwipeRefreshLayout;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.Call;
import rx.Subscription;

import static com.ruziniu.phonelive.AppContext.address;

/**
 * @author 魏鹏
 * @dw 首页热门
 */
public class HotFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    @InjectView(R.id.lv_live_room)
    ListView mListUserRoom;
    @InjectView(R.id.refreshLayout)
    WPSwipeRefreshLayout mSwipeRefreshLayout;
    private LayoutInflater inflater;
    private HotUserListAdapter mHotUserListAdapter;
    private Subscription mSubscription;
    private Button mRegion;
    private String city;
    private UserBean mLiveRecordBean;
    private String area = "";
    private String list1 = "";
    private String list2 = "";
    private String city1;
    private UserBean mUser;
    private int selectPositon;
    //分类加载
    private int classposition = 0;
    private int selectItenposition;
    private List<UserBean> mUserList = new ArrayList<>();
    private boolean config = false;
    private Handler handler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_index_hot, null);
        ButterKnife.inject(this, view);
        this.inflater = inflater;
        initView();
        initData();
        return view;
    }

    private void initView() {
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.global));
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mListUserRoom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectPositon = position;
                //跳转直播间
                joinRoom(position);
            }
        });

        //添加自动读页的事件
        AutoLoadListener autoLoadListener = new AutoLoadListener(new AutoLoadListener.AutoLoadCallBack() {
            @Override
            public void execute() {
                if (config){
                    //showWaitDialog("正在加载更多...");
                    getData();
                }
            }
        });
        mListUserRoom.setOnScrollListener(autoLoadListener);
    }

    private void joinRoom(int position) {
        mLiveRecordBean = mUserList.get(position - 1);
        PhoneLiveApi.isLive(mLiveRecordBean.getId(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                String res = ApiUtils.checkIsSuccess(response);
                try {
                    JSONObject jsonObject = new JSONObject(res);
                    String islive = jsonObject.get("islive").toString();
                    mLiveRecordBean.setIslive(islive);
                    if (StringUtils.toInt(mLiveRecordBean.getIslive()) == 1) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("USER_INFO", mLiveRecordBean);
                        UIHelper.showLookLiveActivity(getActivity(), bundle);
                    } else {
                        String url = mLiveRecordBean.getVideo_url();
                        if (url.equals("")) {
                            AppContext.showToastAppMsg(getActivity(), "无直播记录");
                        } else {
                            showWaitDialog("正在获取回放...");
                            PhoneLiveApi.getLiveRecordById(mLiveRecordBean.getVid() + "", new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {
                                    hideWaitDialog();
                                    AppContext.showToastAppMsg(getActivity(), "获取录播失败");
                                }

                                @Override
                                public void onResponse(String response) {
                                    hideWaitDialog();
                                    String res = ApiUtils.checkIsSuccess(response);
                                    if (res != null) {
                                        mLiveRecordBean.setVideo_url(res.trim());
                                        PhoneLiveApi.livehit(mLiveRecordBean.getVid(), new StringCallback() {
                                            @Override
                                            public void onError(Call call, Exception e) {
                                                hideWaitDialog();
                                                mLiveRecordBean.setHit((StringUtils.toInt(mLiveRecordBean.getHit()) + 1) + "");
                                                mLiveRecordBean.setUid(mLiveRecordBean.getVid());
                                                VideoBackActivity.startVideoBack(getActivity(), mLiveRecordBean);
                                            }

                                            @Override
                                            public void onResponse(String response) {
                                                hideWaitDialog();
                                                String res = ApiUtils.checkIsSuccess(response);
                                                if (res != null) {
                                                    try {
                                                        JSONObject json = new JSONObject(res);
                                                        mLiveRecordBean.setHit(json.get("hit").toString().trim());
                                                        mLiveRecordBean.setId(mLiveRecordBean.getVid());
                                                        VideoBackActivity.startVideoBack(getActivity(), mLiveRecordBean);
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
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void initData() {
        handler = new Handler();
        //2016.09.06 无数据不显示轮播修改 wp
        mUser = AppContext.getInstance().getLoginUser();
        mHotUserListAdapter = new HotUserListAdapter();
        View view = inflater.inflate(R.layout.view_hot_rollpic, null);
        view.findViewById(R.id.view).setVisibility(View.GONE);
        LinearLayout mFenLei = (LinearLayout) view.findViewById(R.id.ll_fenlei);
        mFenLei.setVisibility(View.GONE);
        mRegion = (Button) view.findViewById(R.id.btn_region);
        Button mAll = (Button) view.findViewById(R.id.btn_all);
        Button mNearby = (Button) view.findViewById(R.id.btn_nearby);
        mListUserRoom.addHeaderView(view);
        mListUserRoom.setAdapter(mHotUserListAdapter);
        city = address;
        city1 = city;
        String c = city.substring(0, 2);
        mRegion.setText(c);
        mRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    //摄像头权限检测
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                                    != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                        //进行权限请求
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.ACCESS_COARSE_LOCATION},
                                5);
                        return;
                    }
                }
                Intent intent = new Intent(getContext(), RegionActivity.class);
                intent.putExtra("region", city);
                startActivityForResult(intent, 0);
            }
        });
        mAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AllActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        mNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NearbyActivity.class);
                intent.putExtra("nearby", city);
                startActivityForResult(intent, 2);
            }
        });
        getData();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 5: {
                // 判断权限请求是否通过
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults.length > 0 && grantResults[2] != PackageManager.PERMISSION_GRANTED && grantResults.length > 0 && grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(getContext(), RegionActivity.class);
                    intent.putExtra("region", city);
                    startActivityForResult(intent, 0);
                } else if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    AppContext.showToast("您已拒绝使用摄像头权限,将无法正常直播,请去设置中修改");
                } else if (grantResults.length > 0 && grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    AppContext.showToast("您已拒绝使用录音权限,将无法正常直播,请去设置中修改");
                } else if (grantResults.length > 0 && grantResults[2] != PackageManager.PERMISSION_GRANTED || grantResults.length > 0 && grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                    AppContext.showToast("您没有同意使用读写文件权限,无法正常直播,请去设置中修改");
                } else if (grantResults.length > 0 && grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                    AppContext.showToast("定位权限未打开");
                }
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bundle = data.getExtras();
        if (0 == resultCode) {
            if (!bundle.getString("region").equals("")) {
                mRegion.setText(bundle.getString("region").substring(0, 2));
            }
            city = bundle.getString("region");
            if (city.equals("全国")) {
                city1 = "";
            } else {
                city1 = city;
            }
            list1 = "";
            list2 = "";
            area = "";
            refresh.run();
        } else if (1 == resultCode) {
            list1 = bundle.getString("list1");
            list2 = bundle.getString("list2");
            area = "";
            refresh.run();
        } else if (2 == resultCode) {
            area = bundle.getString("nearby");
            city1 = city;
            list1 = "";
            list2 = "";
            refresh.run();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Runnable refresh = new Runnable() {
        @Override
        public void run() {
            //获取首页热门
            getData();
        }
    };

    private void fillUI() {

        mListUserRoom.setVisibility(View.VISIBLE);

        if (mSwipeRefreshLayout.isRefreshing()) {
            mHotUserListAdapter.notifyDataSetChanged();
        } else {
            mListUserRoom.setAdapter(mHotUserListAdapter);
        }
        if (selectPositon != 1) {
            mListUserRoom.setSelection(selectPositon);
        }
        mListUserRoom.setSelection(selectItenposition);
    }

    private StringCallback callback = new StringCallback() {

        @Override
        public void onError(Call call, Exception e) {
            //hideWaitDialog();
        }

        @Override
        public void onResponse(String s) {
            //hideWaitDialog();
            mSwipeRefreshLayout.setRefreshing(false);
            JSONArray res = ApiUtils.arrayCheckIsSuccess(s);
            if (res != null) {
                config = true;
                try {
                    if (classposition == 0) {
                        mUserList  = new ArrayList<>();
                    }
                    selectItenposition = mUserList.size();
                    for (int i = 0; i < res.length(); i++) {
                        JSONObject resJa = res.getJSONObject(i);
                        if (resJa != null) {
                            UserBean user = new Gson().fromJson(resJa.toString(), UserBean.class);
                            mUserList.add(user);
                        }
                    }
                    if (isAdded()) {
                        //获取集合中最后一个数据的ID
                        if (mUserList.size() > 0) {
                            classposition = mUserList.get(mUserList.size() - 1).getVid();
                        }
                        fillUI();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    @Override
    public void onResume() {
        super.onResume();

        if (IndexPagerFragment.mSex != 0 || !IndexPagerFragment.mArea.equals("")) {
            selectTermsScreen(IndexPagerFragment.mSex, IndexPagerFragment.mArea);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //关闭定时刷新
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHotUserListAdapter = null;
        if (mUserList.size() > 0) {
            mUserList.clear();
        }
        mUserList = null;
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }

        OkHttpUtils.getInstance().cancelTag("getNew");
    }

    public void selectTermsScreen(int sex, String area) {
        //PhoneLiveApi.selectTermsScreen(sex,area,callback);
    }

    //下拉刷新
    @Override
    public void onRefresh() {
        classposition = 0;
        handler.postDelayed(runnable,3);
        getData();

        //PhoneLiveApi.selectTermsScreen(IndexPagerFragment.mSex,IndexPagerFragment.mArea,callback);
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };

    private void getData() {
        //PhoneLiveApi.getNew(city, "", "", "", callback);
        config = false;
        PhoneLiveApi.getNew(classposition, mUser.getId(), "", area, list1, list2, callback);
    }

    private class HotUserListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mUserList.size();
        }

        @Override
        public Object getItem(int position) {
            return mUserList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_hot_user, null);
                viewHolder = new ViewHolder();
                viewHolder.mUserNick = (TextView) convertView.findViewById(R.id.tv_live_nick);
                viewHolder.mUserLocal = (TextView) convertView.findViewById(R.id.tv_live_local);
                viewHolder.mUserNums = (TextView) convertView.findViewById(R.id.tv_live_usernum);
                viewHolder.mUserHead = (AvatarView) convertView.findViewById(R.id.iv_live_user_head);
                viewHolder.mUserPic = (LoadUrlImageView) convertView.findViewById(R.id.iv_live_user_pic);
                viewHolder.mRoomTitle = (TextView) convertView.findViewById(R.id.tv_hot_room_title);
                viewHolder.mLive = (TextView) convertView.findViewById(R.id.tv_live);
                viewHolder.mIsLive = (LinearLayout) convertView.findViewById(R.id.tv_islive);
                viewHolder.mLocation = (LinearLayout) convertView.findViewById(R.id.ll_location);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final UserBean user = mUserList.get(position);
            viewHolder.mUserHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UIHelper.showHomePageActivity(getActivity(), user.getId());
                }
            });
            viewHolder.mLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UIHelper.showHomePageActivity(getActivity(), user.getId());
                }
            });
            if (user.getIslive().equals("0")) {
                viewHolder.mIsLive.setVisibility(View.GONE);
            }
            viewHolder.mUserNick.setText(user.getUser_nicename());
            viewHolder.mUserLocal.setText(user.getCity() + " " + user.getAddress());
            //viewHolder.mUserPic.setImageLoadUrl(user.getAvatar());
            //用于加载图片可以平滑滚动
            Glide
                    .with(HotFragment.this)
                    .load(user.getAvatar())
                    .dontAnimate()
                    .placeholder(R.drawable.bigmoren)
                    .into(viewHolder.mUserPic);
            viewHolder.mUserHead.setAvatarUrl(user.getAvatar_thumb());
            viewHolder.mUserNums.setText(String.valueOf(user.getNums()));
            /*if(null !=user.getTitle()){
                viewHolder.mRoomTitle.setVisibility(View.VISIBLE);
                viewHolder.mRoomTitle.setText(user.getTitle());
            }*/
            return convertView;
        }
    }

    private class ViewHolder {
        public TextView mUserNick, mUserLocal, mUserNums, mRoomTitle, mLive;
        public LoadUrlImageView mUserPic;
        public AvatarView mUserHead;
        public LinearLayout mIsLive, mLocation;
    }

}
