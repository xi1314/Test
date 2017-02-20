package com.ruziniu.phonelive.ui;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.adapter.UserBaseInfoAdapter;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.utils.UIHelper;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import okhttp3.Call;

/**
 * 关注列表
 */
public class AttentionActivity extends ToolBarBaseActivity {
    @InjectView(R.id.lv_attentions)
    ListView mAttentionView;
    @InjectView(R.id.sr_refresh)
    SwipeRefreshLayout mRefreshLayout;
    private List<UserBean> mAttentionList;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_attention;
    }

    @Override
    public void initView() {
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.actionbarbackground));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                initData();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    @Override
    public void initData() {
        setActionBarTitle(getResources().getString(R.string.attention));
        StringCallback callback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(String response) {
                mRefreshLayout.setRefreshing(false);
                String res = ApiUtils.checkIsSuccess(response);
                if(res != null){
                    try {
                        JSONArray fansJsonArr = new JSONArray(res);
                        if(fansJsonArr.length() > 0){
                            Gson gson = new Gson();
                            mAttentionList = new ArrayList<>();
                            for(int i =0;i<fansJsonArr.length(); i++){
                                mAttentionList.add(gson.fromJson(fansJsonArr.getString(i), UserBean.class));
                            }
                            fillUI();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        PhoneLiveApi.getAttentionList(getIntent().getIntExtra("uid", 0), AppContext.getInstance().getLoginUid(), callback);
    }

    private void fillUI() {
        mAttentionView.setAdapter(new UserBaseInfoAdapter(mAttentionList));
        mAttentionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UIHelper.showHomePageActivity(AttentionActivity.this,mAttentionList.get(position).getId());
            }
        });

    }

    @Override
    public void onClick(View v) {

    }


    @Override
    protected boolean hasActionBar() {
        return true;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    public void onResume() {
        super.onResume();
    }
    public void onPause() {
        super.onPause();
    }
}
