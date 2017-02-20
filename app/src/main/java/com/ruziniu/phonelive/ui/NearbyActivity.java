package com.ruziniu.phonelive.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.Call;

/**
 * Created by admin on 2016/9/27.
 */
public class NearbyActivity extends Activity {
    private String nearby = "";
    private ArrayList list;
    private ListView mNearby;
    private int softInputStateAlwaysHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(softInputStateAlwaysHidden);
        setContentView(R.layout.activity_nearby);
        initView();
        initDate();
    }

    private void initDate() {
        Intent intent = getIntent();
        String city = intent.getStringExtra("nearby");
        list = new ArrayList();
        PhoneLiveApi.getquyu(city, new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                Toast.makeText(NearbyActivity.this,"网络请求出错",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                JSONArray res = ApiUtils.arrayCheckIsSuccess(response);
                if (res!=null){
                    for (int i= 0;i < res.length();i++){
                        try {
                            JSONObject json = res.getJSONObject(i);
                            String area = json.getString("area");
                            list.add(area);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    fillUi();
                }
            }


        });
    }
    private void fillUi() {
        mNearby.setAdapter(new Myadapter());
    }

    private void initView() {

        ImageView mBack =  (ImageView) findViewById(R.id.tv_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backDate();
                finish();
            }
        });
        mNearby = (ListView) findViewById(R.id.lv_nearby);
        TextView mSh = (TextView) findViewById(R.id.sh);
        mSh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NearbyActivity.this,SearchActivity.class));
            }
        });
        mNearby.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                nearby = list.get(position).toString();
                backDate();
                finish();
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            backDate();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void backDate() {
        Bundle bundle = new Bundle();
        bundle.putString("nearby", nearby);
        setResult(2, this.getIntent().putExtras(bundle));
    }

    @Override
    protected void onDestroy() {
        OkHttpUtils.getInstance().cancelTag("getquyu");
        super.onDestroy();
    }

    class Myadapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if(convertView==null){
                convertView=View.inflate(NearbyActivity.this, R.layout.item_listview_nearby,null);
                vh=new ViewHolder();
                vh.tv= (TextView) convertView.findViewById(R.id.textview);
                convertView.setTag(vh);
            }else {
                vh= (ViewHolder) convertView.getTag();
            }
            vh.tv.setText(list.get(position).toString());
            return convertView;
        }
        class ViewHolder{
            TextView tv;
        }
    }
}

