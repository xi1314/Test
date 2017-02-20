package com.ruziniu.phonelive.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.utils.StringUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.InjectView;
import okhttp3.Call;

/**
 * 直播记录
 */
public class LiveRecordActivity extends ToolBarBaseActivity{
    @InjectView(R.id.lv_live_record)
    ListView mLiveRecordList;
    ArrayList<UserBean> mRecordList = new ArrayList<>();

    //当前选中的直播记录bean
    private UserBean mLiveRecordBean;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_live_record;
    }

    @Override
    public void initView() {
        mLiveRecordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mLiveRecordBean = mRecordList.get(i);
                /*VideoBackActivity.startVideoBack(LiveRecordActivity.this,mLiveRecordBean);*/
                showLiveRecord(i);
            }
        });
    }

    //打开回放记录
    private void showLiveRecord(final int i) {

        showWaitDialog("正在获取回放...");

        PhoneLiveApi.getLiveRecordById(mLiveRecordBean.getId()+"",new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                hideWaitDialog();
            }

            @Override
            public void onResponse(String response) {
                String res = ApiUtils.checkIsSuccess(response);
                if(res != null){
                    //PhoneLiveApi.getLiveRecordhit();
                    //mLiveRecordBean.setAvatar();
                    //mLiveRecordBean.setNums();
                    mLiveRecordBean.setVideo_url(res.trim());
                    PhoneLiveApi.livehit(mRecordList.get(i).getId(), new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                            hideWaitDialog();
                            mLiveRecordBean.setHit((StringUtils.toInt(mLiveRecordBean.getHit())+1)+"");
                            mLiveRecordBean.setUpload(1);
                            VideoBackActivity.startVideoBack(LiveRecordActivity.this, mLiveRecordBean);
                        }
                        @Override
                        public void onResponse(String response) {
                            hideWaitDialog();
                            String res = ApiUtils.checkIsSuccess(response);
                            if (res!=null){
                                try {
                                    JSONObject json = new JSONObject(res);
                                    mLiveRecordBean.setHit(json.get("hit").toString().trim());
                                    mLiveRecordBean.setUpload(1);
                                    mLiveRecordBean.setUid(mLiveRecordBean.getVid());
                                    VideoBackActivity.startVideoBack(LiveRecordActivity.this,mLiveRecordBean);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                }else{
                    //showToast3("视频暂未生成,请耐心等待",3);
                }
            }
        });

    }
    private StringCallback requestLiveRecordDataCallback = new StringCallback() {
        @Override
        public void onError(Call call, Exception e) {
            AppContext.showToastAppMsg(LiveRecordActivity.this,"获取直播纪录失败");
        }

        @Override
        public void onResponse(String response) {
            String res = ApiUtils.checkIsSuccess(response);
            if(null != res){
                try {
                    JSONObject liveRecordJsonObj = new JSONObject(res);
                    JSONArray liveRecordJsonArray = liveRecordJsonObj.getJSONArray("list");
                    if(0 < liveRecordJsonArray.length()){
                        Gson g = new Gson();
                        for(int i = 0; i < liveRecordJsonArray.length(); i++){
                            mRecordList.add(g.fromJson(liveRecordJsonArray.getString(i),UserBean.class));
                        }
                    }
                    fillUI();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void initData() {
        setActionBarTitle(getString(R.string.liverecord));
        requestData();
    }

    //请求数据
    private void requestData() {

        PhoneLiveApi.getLiveRecord(getIntent().getIntExtra("uid",0),requestLiveRecordDataCallback);
    }

    private void fillUI() {
        mLiveRecordList.setAdapter(new LiveRecordAdapter());
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }
    private class LiveRecordAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mRecordList.size();
        }

        @Override
        public Object getItem(int position) {
            return mRecordList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(null == convertView){
                convertView = View.inflate(LiveRecordActivity.this,R.layout.item_live_record,null);
                viewHolder = new ViewHolder();
                viewHolder.mLiveNum = (TextView) convertView.findViewById(R.id.tv_item_live_record_num);
                viewHolder.mLiveTime = (TextView) convertView.findViewById(R.id.tv_item_live_record_time);
                viewHolder.mLiveTitle = (TextView) convertView.findViewById(R.id.tv_item_live_record_title);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            UserBean l = mRecordList.get(position);
            viewHolder.mLiveNum.setText(l.getNums());
            viewHolder.mLiveTitle.setText(l.getTitle());
            viewHolder.mLiveTime.setText(l.getDatetime());
            return convertView;
        }
        class ViewHolder{
            public TextView mLiveTime,mLiveNum,mLiveTitle;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag("getLiveRecordById");
    }
}
