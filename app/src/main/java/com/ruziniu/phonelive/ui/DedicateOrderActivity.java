package com.ruziniu.phonelive.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.adapter.OrderAdapter;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.bean.OrderBean;
import com.ruziniu.phonelive.utils.UIHelper;
import com.ruziniu.phonelive.widget.CircleImageView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import butterknife.InjectView;
import okhttp3.Call;

/**
 * 贡献排行榜
 */
public class DedicateOrderActivity extends ToolBarBaseActivity {
    private ArrayList<OrderBean> mOrderList = new ArrayList<>();
    @InjectView(R.id.lv_order)
    ListView mOrderListView;
    @InjectView(R.id.tv_order_total)
    TextView mOrderTotal;
    private String mTotal;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_order;
    }

    @Override
    public void initView() {

    }
    @Override
    public void initData() {
        setActionBarTitle(getString(R.string.yporder));
        StringCallback callback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                String res = ApiUtils.checkIsSuccess(response);
                if(null != res){
                    try {
                        JSONObject resJsonObj = new JSONObject(res);
                        Gson g = new Gson();
                        JSONArray uLsit = resJsonObj.getJSONArray("list");
                        mTotal = resJsonObj.getString("total");
                        for(int i = 0;i<uLsit.length(); i++){
                            OrderBean orderBean = g.fromJson(uLsit.getString(i), OrderBean.class);
                            orderBean.setOrderNo(String.valueOf(i));
                            mOrderList.add(orderBean);

                        }

                        fillUI();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        PhoneLiveApi.getYpOrder(getIntent().getIntExtra("uid", 0), callback);

    }

    private void fillUI() {
        mOrderTotal.setText(mTotal + getString(R.string.yingpiao));
        if(0 < mOrderList.size()){
            mOrderListView.setAdapter(new OrderAdapter(mOrderList,getLayoutInflater(),this));
            mOrderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    UIHelper.showHomePageActivity(DedicateOrderActivity.this,mOrderList.get(position).getUid());
                }
            });
        }

    }

    @Override
    public void onClick(View v) {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag("getYpOrder");//BBB
    }
}
