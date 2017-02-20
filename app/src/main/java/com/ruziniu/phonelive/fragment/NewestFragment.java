package com.ruziniu.phonelive.fragment;

import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.BaseFragment;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.ui.VideoBackActivity;
import com.ruziniu.phonelive.ui.listener.AutoLoadListener;
import com.ruziniu.phonelive.utils.LiveUtils;
import com.ruziniu.phonelive.utils.StringUtils;
import com.ruziniu.phonelive.utils.UIHelper;
import com.ruziniu.phonelive.widget.AvatarView;
import com.ruziniu.phonelive.widget.LoadUrlImageView;
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

import static com.ruziniu.phonelive.AppContext.lat;
import static com.ruziniu.phonelive.AppContext.lng;

/**
 * 首页最新直播
 */
public class NewestFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    List<UserBean> mUserList = new ArrayList<>();
    @InjectView(R.id.gv_newest)
    GridView mNewestLiveView;
    @InjectView(R.id.sl_newest)
    SwipeRefreshLayout mRefresh;

    @InjectView(R.id.tishi)
    LinearLayout mTvPrompt;

    private int wh;
    private Subscription mSubscription;
    private UserBean mLiveRecordBean;
    private int classposition = 0;
    private int selectItenposition;
    private  boolean config = false;
    private Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newest, null);
        ButterKnife.inject(this, view);

        initData();
        initView(view);
        return view;
    }

    @Override
    public void initData() {
        handler = new Handler();
        requestData();
    }


    //最新主播数据请求
    private void requestData() {
        config = false;
        UserBean userBean = AppContext.getInstance().getLoginUser();
        String city_1 = userBean.getCity_1();
        String latb = userBean.getLatb();
        String lngb = userBean.getLngb();
        if (city_1!=null&&latb!=null&&lngb!=null&&!city_1.equals("")&&!latb.equals("")&&!lngb.equals("")){
            getDate(city_1, latb, lngb);
        }else{
            PhoneLiveApi.getMyUserInfo(userBean.getId(), userBean.getToken(), new StringCallback() {
                @Override
                public void onError(Call call, Exception e) {
                    mRefresh.setRefreshing(false);
                }

                @Override
                public void onResponse(String response) {
                    mRefresh.setRefreshing(false);
                    String res = ApiUtils.checkIsSuccess(response);
                    if(res == null){
                        UIHelper.showLoginSelectActivity(getActivity());
                        getActivity().finish();
                        return;
                    }
                    UserBean mInfo = new Gson().fromJson(res,UserBean.class);
                    String city = mInfo.getCity_1();
                    String lat =mInfo.getLat_1();
                    String lng = mInfo.getLng_1();
                    if (city!=null&&lat != null && lng != null&&!city.equals("")&&!lat.equals("")&&!lng.equals("")) {
                        getDate(city, lat, lng);
                    }else{
                        mRefresh.setRefreshing(false);
                        Toast.makeText(getContext(),"请确定定位权限是否打开，在重新登录",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void getDate(String city, String lat, String lng) {
        PhoneLiveApi.samecity(classposition, city, lat, lng, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                //hideWaitDialog();
                mRefresh.setRefreshing(false);
                config = true;
                mTvPrompt.setVisibility(View.VISIBLE);
                mNewestLiveView.setVisibility(View.GONE);
            }

            @Override
            public void onResponse(String response) {
                //hideWaitDialog();
                mRefresh.setRefreshing(false);
                String res = ApiUtils.checkIsSuccess(response);
                if (null != res) {
                    config = true;
                    try {
                        if (classposition == 0) {
                            mUserList  = new ArrayList<>();
                        }
                        selectItenposition = mUserList.size();
                        JSONArray resUserListJsonArr = new JSONArray(res);
                        Gson g = new Gson();
                        for (int i = 0; i < resUserListJsonArr.length(); i++) {
                            mUserList.add(g.fromJson(resUserListJsonArr.getString(i), UserBean.class));
                        }
                        if (mUserList.size() >= 0) {
                            //获取集合中最后一个数据的ID
                            classposition = mUserList.get(mUserList.size() - 1).getVid();
                            if (isAdded()) {
                                fillUI();
                            }
                        } else {
                            mTvPrompt.setVisibility(View.VISIBLE);
                            mNewestLiveView.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void fillUI() {
        mTvPrompt.setVisibility(View.GONE);
        mNewestLiveView.setVisibility(View.VISIBLE);
        if (getActivity() != null) {
            //设置每个主播宽度
            int w = getActivity().getWindowManager().getDefaultDisplay().getWidth();
            wh = w / 2;
            mNewestLiveView.setColumnWidth(wh);
            mNewestLiveView.setAdapter(new NewestAdapter());
            mNewestLiveView.setSelection(selectItenposition-(mUserList.size()-selectItenposition)/2 );
        }
    }

    @Override
    public void initView(View view) {
        mNewestLiveView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mLiveRecordBean = mUserList.get(position);
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
        });
        mRefresh.setColorSchemeColors(getResources().getColor(R.color.global));
        mRefresh.setOnRefreshListener(this);
        //添加自动读页的事件
        AutoLoadListener autoLoadListener = new AutoLoadListener(new AutoLoadListener.AutoLoadCallBack() {
            @Override
            public void execute() {
               if (config){
                   //showWaitDialog("正在加载更多...");
                   requestData();
               }
            }
        });
        mNewestLiveView.setOnScrollListener(autoLoadListener);
    }

    private Runnable refresh = new Runnable() {
        @Override
        public void run() {
            requestData();
        }
    };

    @Override
    public void onRefresh() {
        classposition = 0;

        handler.postDelayed(runnable,4);
        requestData();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mRefresh.setRefreshing(false);
            OkHttpUtils.getInstance().cancelTag("getBaseInfo");
            OkHttpUtils.getInstance().cancelTag("samecity");
        }
    };

    class NewestAdapter extends BaseAdapter {

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
                convertView = View.inflate(getActivity(), R.layout.item_newest_user, null);
                convertView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, wh));

                viewHolder = new ViewHolder();
                viewHolder.mUHead = (LoadUrlImageView) convertView.findViewById(R.id.iv_newest_item_user);
                viewHolder.mUText = (TextView) convertView.findViewById(R.id.tv_nicheng);
                viewHolder.mUnum = (TextView) convertView.findViewById(R.id.tv_people_num);
                viewHolder.mHead = (AvatarView) convertView.findViewById(R.id.av_head);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final UserBean u = mUserList.get(position);
            viewHolder.mHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UIHelper.showHomePageActivity(getActivity(), u.getId());
                }
            });
            viewHolder.mUText.setText(u.getUser_nicename());
            viewHolder.mUnum.setText(u.getDistance());
            viewHolder.mHead.setAvatarUrl(u.getAvatar_thumb());
            Glide.with(NewestFragment.this)
                    .load(u.getAvatar())
                    .dontAnimate()
                    .placeholder(R.drawable.xiaomoren)
                    .into(viewHolder.mUHead);
            return convertView;
        }

        class ViewHolder {
            public LoadUrlImageView mUHead;
            public TextView mUText, mUnum;
            public AvatarView mHead;
        }
    }

    public void onResume() {
        super.onResume();
        //mSubscription = LiveUtils.startInterval(refresh);
    }

    public void onPause() {
        super.onPause();
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }
}
