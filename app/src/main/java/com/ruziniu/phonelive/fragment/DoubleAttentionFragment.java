package com.ruziniu.phonelive.fragment;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
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

/**
 * 首页最新直播
 */
public class DoubleAttentionFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    @InjectView(R.id.gv_newest)
    GridView mNewestLiveView;
    @InjectView(R.id.sl_newest)
    SwipeRefreshLayout mRefresh;
    private int wh;
    private Subscription mSubscription;
    private UserBean mLiveRecordBean;
    private List<UserBean> mUserList ;

    //默认提示
    @InjectView(R.id.iv_wuneurong)
    ImageView mTvPrompt;

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

    }


    //最新主播数据请求
    private void requestData() {
        PhoneLiveApi.getAttentionLive(AppContext.getInstance().getLoginUid(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                mTvPrompt.setVisibility(View.VISIBLE);
                mNewestLiveView.setVisibility(View.GONE);
            }

            @Override
            public void onResponse(String response) {
                mRefresh.setRefreshing(false);
                String res = ApiUtils.checkIsSuccess(response);
                if (null != res) {
                    try {
                        mUserList = new ArrayList<>() ;
                        JSONArray liveAndAttentionUserJson = new JSONObject(res).getJSONArray("attentionvideo");
                        Gson g = new Gson();
                        for (int i = 0; i < liveAndAttentionUserJson.length(); i++) {
                            mUserList.add(g.fromJson(liveAndAttentionUserJson.getString(i), UserBean.class));
                        }
                        if (mUserList.size() > 0) {
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
                                if (url == null) {
                                    url = "";
                                }
                                if (url.equals("")) {
                                    AppContext.showToastAppMsg(getActivity(), "无直播记录");
                                } else {
                                    PhoneLiveApi.videohit(mLiveRecordBean.getVid(), new StringCallback() {
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
                                                    mLiveRecordBean.setUpload(2);
                                                    mLiveRecordBean.setId(mLiveRecordBean.getVid());
                                                    VideoBackActivity.startVideoBack(getActivity(), mLiveRecordBean);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
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
/*        //添加自动读页的事件
        AutoLoadListener autoLoadListener =new AutoLoadListener(new AutoLoadListener.AutoLoadCallBack() {
            @Override
            public void execute() {
                requestData();
            }
        });
        mNewestLiveView.setOnScrollListener(autoLoadListener);*/
    }

    private Runnable refresh = new Runnable() {
        @Override
        public void run() {
            requestData();
        }
    };

    @Override
    public void onRefresh() {
        requestData();
    }


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
            viewHolder.mUText.setText(u.getUser_nicename());
            if (u.getIslive() == null) {
                u.setIslive("0");
            }
            if (u.getIslive().equals("0")) {
                viewHolder.mUnum.setText(u.getDatetime());
            } else {
                viewHolder.mUnum.setText("正在直播");
            }
            viewHolder.mHead.setAvatarUrl(u.getAvatar_thumb());
            viewHolder.mHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UIHelper.showHomePageActivity(getActivity(), u.getUid());
                }
            });
            Glide.with(DoubleAttentionFragment.this)
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
        requestData();

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
        mUserList = null;
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }

    }
}
