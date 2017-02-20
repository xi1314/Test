package com.ruziniu.phonelive.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
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

/**
 * @author
 * @dw 视频
 */
public class AttentionFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    @InjectView(R.id.Gv_attentions)
    GridView mGridUserRoom;
    @InjectView(R.id.mSwipeRefreshLayou)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @InjectView(R.id.btn_all1)
    Button mAll;
    @InjectView(R.id.btn_nearby1)
    Button mNearby;
    private List<UserBean> mUserList = new ArrayList<>();
    private int classposition = 0;
    private int selectItenposition;
    private boolean config = false;
    private String city1;
    private String city;
    private String list1 = "";
    private String list2 = "";
    private String area = "";
    private Button mRegion;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attention, null);
        ButterKnife.inject(this, view);
        initView(view);
        initData();
        return view;
    }

    public void initView(View view) {
        mRegion = (Button) view.findViewById(R.id.btn_region1);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.global));
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mGridUserRoom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final UserBean user = mUserList.get(i);
                user.setVideo_url(user.getUrl());
                user.setAvatar(user.getAvatar_thumb());
                user.setUpload(2);
                //观看次数
                PhoneLiveApi.videohit(user.getVid(), new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        hideWaitDialog();
                        user.setHit((StringUtils.toInt(user.getHit())+1)+"");
                        VideoBackActivity.startVideoBack(getActivity(), user);
                    }
                    @Override
                    public void onResponse(String response) {
                        hideWaitDialog();
                        String res = ApiUtils.checkIsSuccess(response);
                        if (res!=null){
                            try {
                                JSONObject json = new JSONObject(res);
                                user.setHit(json.get("hit").toString().trim());
                                user.setId(user.getVid());
                                VideoBackActivity.startVideoBack(getActivity(),user);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                //VideoBackActivity.startVideoBack(getActivity(),user);
            }
        });

        //添加自动读页的事件   下拉加载
        AutoLoadListener autoLoadListener =new AutoLoadListener(new AutoLoadListener.AutoLoadCallBack() {
            @Override
            public void execute() {
                if (config){
                    //showWaitDialog("正在加载更多...");
                    getDate();
                }
            }
        });
        mGridUserRoom.setOnScrollListener(autoLoadListener);
        mRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intoRegion();
            }
        });
        mAll.setOnClickListener(this);
        mNearby.setOnClickListener(this);
    }

    @Override
    public void initData() {

        city = "";
        city1 = city;
        mRegion.setText("全国");
        //2016.09.06 无数据不显示轮播修改 wp
        getDate();
    }

    private void getDate() {
        config = false;
        PhoneLiveApi.videomain(classposition,city1,area,list1,list2,new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                //hideWaitDialog();
                config = true;
                mSwipeRefreshLayout.setRefreshing(false);
            }
            @Override
            public void onResponse(String response) {
                //hideWaitDialog();
                mSwipeRefreshLayout.setRefreshing(false);
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (isAdded()) {
                    //获取集合中最后一个数据的ID
                    if (mUserList.size() > 0) {
                        classposition = mUserList.get(mUserList.size() - 1).getVid();
                    }
                    fillUI();
                }
            }
        });
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
            classposition = 0;
            mUserList  = new ArrayList<>();
            getDate();
        } else if (1 == resultCode) {
            list1 = bundle.getString("list1");
            list2 = bundle.getString("list2");
            //Toast.makeText(getActivity(), list1 + list2, Toast.LENGTH_SHORT).show();
            area = "";
            classposition = 0;
            mUserList  = new ArrayList<>();
            getDate();
        } else if (2 == resultCode) {
            area = bundle.getString("nearby");
            //Toast.makeText(getActivity(), area, Toast.LENGTH_SHORT).show();
            city1 = city;
            list1 = "";
            list2 = "";
            classposition = 0;
            mUserList  = new ArrayList<>();
            getDate();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_region1:
                intoRegion();
                break;
            case R.id.btn_all1:
                intoAll();
                break;
            case R.id.btn_nearby1:
                intoNearby();
                break;
        }
    }

    private void fillUI() {
        mGridUserRoom.setAdapter(new NewestAdapter());
        mGridUserRoom.setSelection(selectItenposition-(mUserList.size()-selectItenposition)/2 );
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
                convertView = View.inflate(getActivity(), R.layout.item_newest_shipin, null);
                viewHolder = new ViewHolder();
                viewHolder.mUHead = (LoadUrlImageView) convertView.findViewById(R.id.iv_newest_item_user);
                viewHolder.mUText = (TextView) convertView.findViewById(R.id.tv_nicheng);
                viewHolder.mUnum = (TextView) convertView.findViewById(R.id.tv_people_num);
                viewHolder.mHead = (AvatarView) convertView.findViewById(R.id.av_head);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final  UserBean u = mUserList.get(position);
            viewHolder.mHead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UIHelper.showHomePageActivity(getActivity(),u.getUid());
                }
            });
            viewHolder.mUText.setText(u.getUser_nicename());
            viewHolder.mUnum.setText(u.getNewtime());
            viewHolder.mHead.setAvatarUrl(u.getAvatar_thumb());
            Glide.with(AttentionFragment.this)
                    .load(u.getUrl_img())
                    .dontAnimate()
                    .placeholder(R.drawable.xiaomoren)
                    .into(viewHolder.mUHead);
            return convertView;
        }

        class ViewHolder {
            public LoadUrlImageView mUHead;
            public TextView mUText ,mUnum ;
            public AvatarView mHead;
        }
    }

    //进入附近
    private void intoNearby() {
        Intent intent = new Intent(getContext(), NearbyActivity.class);
        intent.putExtra("nearby", city);
        startActivityForResult(intent, 2);
    }

    //进入全部
    private void intoAll() {
        Intent intent = new Intent(getContext(), AllActivity.class);
        startActivityForResult(intent, 1);
    }

    //进入地图界面
    private void intoRegion() {
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

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUserList = null;
        OkHttpUtils.getInstance().cancelTag("videomain");
    }
    //下拉刷新
    @Override
    public void onRefresh() {
        classposition = 0;
        getDate();
    }
}
