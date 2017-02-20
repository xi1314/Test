package com.ruziniu.phonelive.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.adapter.BlackInfoAdapter;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.BaseFragment;
import com.ruziniu.phonelive.bean.BlackBean;
import com.ruziniu.phonelive.utils.TLog;
import com.zhy.http.okhttp.callback.StringCallback;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.Call;

/**
 * C黑名单
 */
public class BlackListFragment extends BaseFragment {
    private List<BlackBean> mBlackList = new ArrayList<>();
    @InjectView(R.id.lv_black_list)
    ListView mLvBlackList;
    private BlackInfoAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_black_list,null);
        ButterKnife.inject(this,view);
        initView(view);
        initData();
        return view;
    }

    @Override
    public void initView(View view) {

    }

    @Override
    public void initData() {
        AppContext.showToastAppMsg(getActivity(),"长按可解除拉黑");
        mAdapter = new BlackInfoAdapter(mBlackList,getApplication());
        mLvBlackList.setAdapter(mAdapter);
        mLvBlackList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                relieveBlack(position);
                return false;
            }
        });
        requestData(false);
    }

    private void relieveBlack(final int position) {
        try {
            EMClient.getInstance().contactManager().removeUserFromBlackList(String.valueOf(mBlackList.get(position).getId()));

        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        PhoneLiveApi.pullTheBlack(AppContext.getInstance().getLoginUid(),mBlackList.get(position).getId(),
                AppContext.getInstance().getToken(),
                new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        AppContext.showToastAppMsg(getActivity(),"解除拉黑失败");
                    }

                    @Override
                    public void onResponse(String response) {

                        AppContext.showToastAppMsg(getActivity(),"解除拉黑成功");
                        mBlackList.remove(position);
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void requestData(boolean refresh) {
        PhoneLiveApi.getBlackList(AppContext.getInstance().getLoginUid(),callback);
    }
    private StringCallback callback = new StringCallback() {
        @Override
        public void onError(Call call, Exception e) {

        }

        @Override
        public void onResponse(String response) {

            String res = ApiUtils.checkIsSuccess(response);
            if(null == res)return;
            TLog.log(res);
            try {
                JSONArray blackJsonArray = new JSONArray(res);
                if(blackJsonArray.length() > 0){
                    Gson g  = new Gson();
                    for(int i = 0; i< blackJsonArray.length(); i ++){
                        mBlackList.add(g.fromJson(blackJsonArray.getString(i),BlackBean.class));
                    }
                    fillUI();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    private void fillUI() {
        mAdapter.notifyDataSetChanged();
    }
}
