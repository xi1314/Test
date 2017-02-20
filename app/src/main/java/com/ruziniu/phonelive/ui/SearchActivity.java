package com.ruziniu.phonelive.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.adapter.UserBaseInfoAdapter;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.interf.DialogControl;
import com.ruziniu.phonelive.utils.UIHelper;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;
import okhttp3.Call;

/**
 * 用户搜索
 */
public class SearchActivity extends Activity implements View.OnClickListener {
    private List<UserBean> mUserList = new ArrayList<>();
    private EditText mSearchKey;
    private ListView mLvSearch;
    private ImageView mBack;
    private TextView mSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search_index);
        mSearchKey = (EditText) findViewById(R.id.et_search_input);
        mLvSearch = (ListView) findViewById(R.id.lv_search);
        mBack = (ImageView) findViewById(R.id.iv_private_chat_back);
        mSearch = (TextView) findViewById(R.id.tv_search_btn);
        mBack.setOnClickListener(this);
        mSearch.setOnClickListener(this);
        mLvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UIHelper.showHomePageActivity(SearchActivity.this,mUserList.get(position).getId());
            }
        });
    }

    @OnClick({R.id.iv_private_chat_back,R.id.tv_search_btn})
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_private_chat_back:
                finish();
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
    protected void hideWaitDialog() {
        SearchActivity activity = this;
        if (activity instanceof DialogControl) {
            ((DialogControl) activity).hideWaitDialog();
        }
    }

    protected ProgressDialog showWaitDialog() {
        return showWaitDialog(R.string.loading);
    }

    protected ProgressDialog showWaitDialog(int resid) {
        SearchActivity activity = this;
        if (activity instanceof DialogControl) {
            return ((DialogControl) activity).showWaitDialog(resid);
        }
        return null;
    }

    protected ProgressDialog showWaitDialog(String str) {
        SearchActivity activity = this;
        if (activity instanceof DialogControl) {
            return ((DialogControl) activity).showWaitDialog(str);
        }
        return null;
    }

    private void fillUI() {

        mLvSearch.setAdapter(new UserBaseInfoAdapter(mUserList));

    }

    @Override
    protected void onResume() {
        super.onResume();
        search();
    }

    public void onPause() {
        super.onPause();
    }
}
