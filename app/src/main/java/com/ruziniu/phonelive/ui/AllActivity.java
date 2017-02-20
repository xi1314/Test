package com.ruziniu.phonelive.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.adapter.MyListViewAdapterLeft;
import com.ruziniu.phonelive.adapter.MyListViewAdapterRight;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.bean.UserBean;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;

/**
 * Created by admin on 2016/9/27.
 */
public class AllActivity extends Activity {

    private int selectIndex=0;

    private ListView mListViewleft, mListViewright;
    private MyListViewAdapterLeft adapterleft;
    private MyListViewAdapterRight adapterright;
    private String all;
    private ArrayList<UserBean> mMenus;//左边分类数据
    HashMap<String,ArrayList<UserBean>> allData = new HashMap();
    private ArrayList<UserBean> str;
    private String list2 = "";
    private String list1 = "";


    @Override
    public void onCreate(Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all);
        initView();
        initDate();
    }

    private void initDate() {
        mMenus = new ArrayList();
        // 请求数据
         PhoneLiveApi.getmainlist(new StringCallback() {
             @Override
            public void onError(Call call, Exception e) {
                Toast.makeText(AllActivity.this,"网络请求出错",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(String response) {
                JSONArray res = ApiUtils.arrayCheckIsSuccess(response);
                if (res!=null){
                    for (int i= 0;i < res.length();i++){
                        try {
                            JSONObject json = res.getJSONObject(i);
                            //String title = json.getString("title");
                            Gson gson = new Gson();
                            UserBean userlist1 = gson.fromJson(json.toString(),UserBean.class);
                            mMenus.add(userlist1);
                            JSONArray list = json.getJSONArray("list");
                            str = new ArrayList();
                            if (str.size()!=0){
                                str.clear();
                            }
                            for (int j= 0;j < list.length();j++){
                                JSONObject jso = list.getJSONObject(j);
                                //String t = jso.getString("title");
                                UserBean userlist2 = gson.fromJson(jso.toString(),UserBean.class);
                                str.add(userlist2);
                            }
                            allData.put(String.valueOf(i),str);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    fillUI();
                }
            }
        });
    }

    private void fillUI() {
        adapterleft =new MyListViewAdapterLeft(mMenus,this,selectIndex);
        adapterright =new MyListViewAdapterRight(allData,this,selectIndex);
        mListViewleft.setAdapter(adapterleft);
        mListViewright.setAdapter(adapterright);
    }

    private void initView() {
        TextView mSh = (TextView) findViewById(R.id.sh);
        mSh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AllActivity.this,SearchActivity.class));
            }
        });
        ImageView mBack =  (ImageView) findViewById(R.id.tv_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backDate();
                finish();
            }
        });
        mListViewleft = (ListView) findViewById(R.id.list_item_left);
        mListViewright = (ListView) findViewById(R.id.list_item_right);



        mListViewleft.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selectIndex=position;
                //把下标传过去，然后刷新adapter
                adapterleft.setIndex(position);
                adapterleft.notifyDataSetChanged();
                //当点击某个item的时候让这个item自动滑动到listview的顶部(下面item够多，如果点击的是最后一个就不能到达顶部了)
                mListViewleft.smoothScrollToPositionFromTop(position,0);
                adapterright.setIndex(position);
                mListViewright.setAdapter(adapterright);
            }
        });

        mListViewright.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //ToastUtils.showToast(MainActivity.this,allData[selectIndex][position]);
                all = allData.get(String.valueOf(selectIndex)).get(position).getTitle();
                int i = allData.get(String.valueOf(selectIndex)).get(position).getId();
                int j = mMenus.get(selectIndex).getId();
                list2 = String.valueOf(i);
                list1 = String.valueOf(j);
                backDate();
                finish();
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Bundle bundle = new Bundle();
            bundle.putString("list1", "");
            bundle.putString("list2", "");
            setResult(1, this.getIntent().putExtras(bundle));
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    private void backDate() {
        Bundle bundle = new Bundle();
        bundle.putString("list1", list1);
        bundle.putString("list2", list2);
        setResult(1, this.getIntent().putExtras(bundle));
    }

    @Override
    protected void onDestroy() {
        OkHttpUtils.getInstance().cancelTag("getmainlist");
        super.onDestroy();
    }
}
