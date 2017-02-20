package com.ruziniu.phonelive.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.adapter.DisplayVideoAdapter;
import com.ruziniu.phonelive.adapter.DisplayVideoPhoneAdapter;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.utils.StringUtils;
import com.ruziniu.phonelive.utils.UIHelper;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.Call;

/**
 * Created by admin on 2016/10/11.
 */
public class DisplayActivity extends Activity implements View.OnClickListener {

    private ImageView mBack;
    private Button mUpload;
    private TextView mVideo;
    private TextView mPhone;
    private AlertDialog dialog;
    private GridView mGvDisplay;
    private UserBean user;
    private ArrayList<UserBean> mListPhone = new ArrayList();
    private UserBean videobean;
    private boolean config = true;
    private DisplayVideoAdapter videoAdapter;
    private DisplayVideoPhoneAdapter phoneadapter;
    private UserBean phonebean;
    private View mVideoLine;
    private View mPhoneLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        inintView();
        initDate();
    }

    private void initDate() {
        user = AppContext.getInstance().getLoginUser();
        displayVideo();
    }

    private void inintView() {
        mBack = (ImageView) findViewById(R.id.iv_private_chat_back);
        mUpload = (Button) findViewById(R.id.btn_upload);
        mVideo = (TextView) findViewById(R.id.btn_video);
        mPhone = (TextView) findViewById(R.id.btn_phone);
        mGvDisplay = (GridView) findViewById(R.id.gv_display);
        mVideoLine = findViewById(R.id.video_line);
        mPhoneLine = findViewById(R.id.phone_line);
        mBack.setOnClickListener(this);
        mUpload.setOnClickListener(this);
        mVideo.setOnClickListener(this);
        mPhone.setOnClickListener(this);

        mGvDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                final int position  = i;
                if (config) {
                    final UserBean vb = mListPhone.get(i);
                    PhoneLiveApi.videohit(vb.getId(), new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                            vb.setHit((StringUtils.toInt(vb.getHit())+1)+"");
                            vb.setVideo_url(mListPhone.get(i).getUrl());
                            vb.setUpload(2);
                            VideoBackActivity.startVideoBack(DisplayActivity.this, vb);
                        }
                        @Override
                        public void onResponse(String response) {
                            String res = ApiUtils.checkIsSuccess(response);
                            if (res!=null){
                                try {
                                    JSONObject json = new JSONObject(res);
                                    vb.setHit(json.get("hit").toString().trim());
                                    vb.setVideo_url(mListPhone.get(position).getUrl());
                                    vb.setUpload(2);
                                    VideoBackActivity.startVideoBack(DisplayActivity.this, vb);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }else{
                    UserBean phonebean = mListPhone.get(i);
                    Bundle dBundle = new Bundle();
                    dBundle.putString("phone", phonebean.getUrl());
                    UIHelper.showBigPhone(DisplayActivity.this, dBundle);
                }
            }
        });

        mGvDisplay.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                tanchuang(i);
                return true;
            }
        });
    }
    //删除弹窗
    private void tanchuang(final int position) {

        new AlertDialog.Builder(DisplayActivity.this)//设置对话框标题

                .setMessage("确定删除该文件？？")//设置显示的内容

                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
                    @Override

                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件

                        // TODO Auto-generated method stub
                        UserBean mlist = mListPhone.get(position);

                        if (config) {
                            PhoneLiveApi.deletevideo(mlist.getId(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {

                                }

                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject resJson = new JSONObject(response);
                                        if (Integer.parseInt(resJson.getString("ret")) == 200) {
                                            JSONObject dataJson = resJson.getJSONObject("data");
                                            String code = dataJson.getString("code");
                                            if (code.equals("2")) {
                                                mListPhone.remove(position);
                                                videoAdapter.notifyDataSetChanged();
                                            } else {
                                                Toast.makeText(AppContext.getInstance(), dataJson.get("msg").toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            PhoneLiveApi.deleteimg( mlist.getId(), new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e) {

                                }

                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject resJson = new JSONObject(response);
                                        if (Integer.parseInt(resJson.getString("ret")) == 200) {
                                            JSONObject dataJson = resJson.getJSONObject("data");
                                            String code = dataJson.getString("code");
                                            if (code.equals("2")) {
                                                mListPhone.remove(position);
                                                phoneadapter.notifyDataSetChanged();
                                            } else {
                                                Toast.makeText(AppContext.getInstance(), dataJson.get("msg").toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }

                }).setNegativeButton("返回", new DialogInterface.OnClickListener() {//添加返回按钮
            @Override

            public void onClick(DialogInterface dialog, int which) {//响应事件

            }

        }).show();//在按键响应事件中显示此对话框

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_private_chat_back:
                finish();
                break;
            case R.id.btn_upload:
                PhoneLiveApi.powers(user.getId(), new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                    }

                    @Override
                    public void onResponse(String response) {
                        boolean config = ApiUtils.checkIsSuccess2(response);
                        if (config){
                            uploadDialog();
                        }
                    }
                });

                break;
            case R.id.btn_video:
                mVideo.setTextColor(getResources().getColor(R.color.white));
                mPhone.setTextColor(getResources().getColor(R.color.white));
                mVideoLine.setBackgroundColor(getResources().getColor(R.color.white));
                mPhoneLine.setBackgroundColor(getResources().getColor(R.color.global));
                displayVideo();
                config = true;
                break;
            case R.id.btn_phone:
                mVideo.setTextColor(getResources().getColor(R.color.white));
                mPhone.setTextColor(getResources().getColor(R.color.white));
                mVideoLine.setBackgroundColor(getResources().getColor(R.color.global));
                mPhoneLine.setBackgroundColor(getResources().getColor(R.color.white));
                displayPhone();
                config = false;
                break;
        }
    }

    //上传弹窗BBB
    private void uploadDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view1 = View.inflate(DisplayActivity.this, R.layout.displaytanchuang,
                null);
        builder.setView(view1);
        LinearLayout mVideo = (LinearLayout) view1.findViewById(R.id.ll_choosevideo);
        LinearLayout mPhone = (LinearLayout) view1.findViewById(R.id.ll_choosephone);
        mVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHelper.showUpload(DisplayActivity.this, String.valueOf(1));
                dialog.dismiss();
            }
        });
        mPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHelper.showUpload(DisplayActivity.this, String.valueOf(2));
                dialog.dismiss();
            }
        });
        ActivityManager manager = (ActivityManager) getSystemService(DisplayActivity.this.ACTIVITY_SERVICE);
        android.app.ActivityManager.RunningTaskInfo info = manager.getRunningTasks(1).get(0);
        String shortClassName = info.topActivity.getShortClassName(); //类名
        if (shortClassName.equals(".ui.DisplayActivity")) {
            dialog = builder.show();
        }
    }

    //展示视频BBB
    private void displayVideo() {
        PhoneLiveApi.videolist(user.getId(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
            }

            @Override
            public void onResponse(String response) {
                JSONArray jsonArray = ApiUtils.arrayCheckIsSuccess(response);
                mListPhone.clear();
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String jsonObject = jsonArray.getJSONObject(i).toString();
                            videobean = new Gson().fromJson(jsonObject, UserBean.class);
                            mListPhone.add(videobean);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                videoAdapter = new DisplayVideoAdapter(DisplayActivity.this,DisplayActivity.this.getLayoutInflater(), mListPhone);
                mGvDisplay.setAdapter(videoAdapter);
            }
        });
    }

    ////展示图片BBB
    private void displayPhone() {
        PhoneLiveApi.imglist(user.getId(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                AppContext.showToastAppMsg(DisplayActivity.this, "网络请求出错");
            }

            @Override
            public void onResponse(String response) {

                JSONArray jsonArray = ApiUtils.arrayCheckIsSuccess(response);
                mListPhone.clear();
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            String jsonObject = jsonArray.getJSONObject(i).toString();
                            phonebean = new Gson().fromJson(jsonObject, UserBean.class);
                            mListPhone.add(phonebean);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                phoneadapter = new DisplayVideoPhoneAdapter(DisplayActivity.this, DisplayActivity.this.getLayoutInflater(), mListPhone);
                mGvDisplay.setAdapter(phoneadapter);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (config){
            displayVideo();
        }else {
            displayPhone();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag("imglist");
        OkHttpUtils.getInstance().cancelTag("videolist");
        OkHttpUtils.getInstance().cancelTag("deleteimg");
        OkHttpUtils.getInstance().cancelTag("deletevideo");
    }
}
