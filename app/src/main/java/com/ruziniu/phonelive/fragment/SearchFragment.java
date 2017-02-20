package com.ruziniu.phonelive.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.adapter.UserBaseInfoAdapter;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.BaseFragment;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.utils.UIHelper;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 用户搜索
 */
public class SearchFragment extends BaseFragment {
    @InjectView(R.id.et_search_input)
    EditText mSearchKey;
    @InjectView(R.id.lv_search)
    ListView mLvSearch;
    private List<UserBean> mUserList = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_index,null);
        ButterKnife.inject(this,view);
        initView(view);
        initData();
        return view;
    }

    @Override
    public void initView(View view) {
        mLvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UIHelper.showHomePageActivity(getActivity(),mUserList.get(position).getId());
            }
        });
    }

    @Override
    public void initData() {

    }
    @OnClick({R.id.iv_private_chat_back,R.id.tv_search_btn})
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_private_chat_back:
                getActivity().finish();
            break;
            case R.id.tv_search_btn:
                search();
                break;

        }
    }

    //搜索
    private void search() {
        showWaitDialog();
        String screenKey = mSearchKey.getText().toString().trim();
        if(!screenKey.equals("")){
            StringCallback callback = new StringCallback() {
                @Override
                public void onError(Call call, Exception e) {
                    hideWaitDialog();
                }

                @Override
                public void onResponse(String response) {
                    hideWaitDialog();
                    String res = ApiUtils.checkIsSuccess(response);

                    if(null != res){
                        Gson g = new Gson();
                        try {
                            JSONArray searchUserJsonArray = new JSONArray(res);
                            mUserList.clear();
                            for(int i=0;i<searchUserJsonArray.length();i++){
                                mUserList.add(g.fromJson(searchUserJsonArray.getString(i),UserBean.class));
                            }
                            fillUI();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            };
            PhoneLiveApi.search(screenKey,callback, AppContext.getInstance().getLoginUid());
        }
    }

    private void fillUI() {

        mLvSearch.setAdapter(new UserBaseInfoAdapter(mUserList));

    }

    @Override
    public void onResume() {
        super.onResume();
        search();
    }

    public void onPause() {
        super.onPause();
    }
}
