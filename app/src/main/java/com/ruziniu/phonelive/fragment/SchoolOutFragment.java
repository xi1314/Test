package com.ruziniu.phonelive.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.adapter.LiveUserAdapter;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.BaseFragment;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.ui.AllActivity;
import com.ruziniu.phonelive.ui.NearbyActivity;
import com.ruziniu.phonelive.ui.RegionActivity;
import com.ruziniu.phonelive.ui.VideoBackActivity;
import com.ruziniu.phonelive.ui.VideoPlayerActivity;
import com.ruziniu.phonelive.ui.listener.AutoLoadListener;
import com.ruziniu.phonelive.utils.StringUtils;
import com.ruziniu.phonelive.utils.UIHelper;
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
 * 首页左边关注
 */
public class SchoolOutFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    @InjectView(R.id.lv_attentions)
    ListView mLvAttentions;

    @InjectView(R.id.mSwipeRefreshLayout)
    SwipeRefreshLayout mRefresh;

    private View view;
    private LiveUserAdapter mAdapter;
    private UserBean mLiveRecordBean;
    private LayoutInflater inflater;
    private View viewhead;
    private Button mRegion;
    private String city;
    private String city1;
    private String list1 = "";
    private String list2 = "";
    private String area = "";
    private UserBean mUser;
    private int classposition = 0;
    private int selectItenposition;
    private List<UserBean> mUserList = new ArrayList<>();
    private boolean config = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_schoolout, null);
        ButterKnife.inject(this, view);
        this.inflater = inflater;
        initView(view);
        initData();
        return view;
    }

    @Override
    public void initView(View view) {
        mRefresh.setColorSchemeColors(getResources().getColor(R.color.global));
        mRefresh.setOnRefreshListener(this);
        viewhead = inflater.inflate(R.layout.view_hot_rollpic, null);
        mRegion = (Button) viewhead.findViewById(R.id.btn_region);
        Button mAll = (Button) viewhead.findViewById(R.id.btn_all);
        Button mNearby = (Button) viewhead.findViewById(R.id.btn_nearby);
        mRegion.setOnClickListener(this);
        mAll.setOnClickListener(this);
        mNearby.setOnClickListener(this);
    }

    @Override
    public void initData() {
        mUser = AppContext.getInstance().getLoginUser();
        mLvAttentions.addHeaderView(viewhead);

        city = "";
        city1 = city;
        //String c = city.substring(0, 2);
        mRegion.setText("全国");
        getDate();
    }

    private void getDate() {
        config = false;
        PhoneLiveApi.outschool(classposition, mUser.getId(), city1, area, list1, list2, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                //hideWaitDialog();
                mRefresh.setRefreshing(false);
            }

            @Override
            public void onResponse(String response) {
                //hideWaitDialog();
                mRefresh.setRefreshing(false);
                JSONArray res = ApiUtils.arrayCheckIsSuccess(response);

                if (null != res) {
                    config = true;
                    try {
                        if (classposition == 0) {
                            mUserList = new ArrayList<>();
                        }
                        selectItenposition = mUserList.size();
                        Gson g = new Gson();
                        for (int i = 0; i < res.length(); i++) {
                            mUserList.add(g.fromJson(res.getString(i), UserBean.class));
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_region:
                intoRegion();
                break;
            case R.id.btn_all:
                intoAll();
                break;
            case R.id.btn_nearby:
                intoNearby();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle bundle = data.getExtras();
        if (bundle != null) {
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
                refresh.run();
            } else if (1 == resultCode) {
                list1 = bundle.getString("list1");
                list2 = bundle.getString("list2");
                //Toast.makeText(getActivity(), list1 + list2, Toast.LENGTH_SHORT).show();
                area = "";
                classposition = 0;
                mUserList  = new ArrayList<>();
                refresh.run();
            } else if (2 == resultCode) {
                area = bundle.getString("nearby");
                //Toast.makeText(getActivity(), area, Toast.LENGTH_SHORT).show();
                city1 = city;
                list1 = "";
                list2 = "";
                classposition = 0;
                mUserList  = new ArrayList<>();
                refresh.run();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void fillUI() {
        mAdapter = new LiveUserAdapter(getActivity(), getActivity().getLayoutInflater(), mUserList);
        mLvAttentions.setAdapter(mAdapter);
        mLvAttentions.setSelection(selectItenposition);
        mLvAttentions.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
                                bundle.putSerializable(VideoPlayerActivity.USER_INFO, mLiveRecordBean);
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


        //添加自动读页的事件   下拉加载
        AutoLoadListener autoLoadListener = new AutoLoadListener(new AutoLoadListener.AutoLoadCallBack() {
            @Override
            public void execute() {
                if (config) {
                    //showWaitDialog("正在加载更多...");
                    getDate();
                }
            }
        });
        mLvAttentions.setOnScrollListener(autoLoadListener);
    }


    private Runnable refresh = new Runnable() {
        @Override
        public void run() {
            getDate();
        }
    };

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
    public void onDestroy() {
        super.onDestroy();
        mAdapter = null;
        mUserList = null;
        OkHttpUtils.getInstance().cancelTag("outschool");
    }

    @Override
    public void onRefresh() {
        classposition = 0;
        getDate();
    }
}