package com.ruziniu.phonelive.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.ui.other.ChatServer;
import com.ruziniu.phonelive.utils.ShareUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.Call;

/**
 * Created by weipeng on 16/8/18.
 */
public class LiveEndFragmentDialog extends DialogFragment {

    //直播结束关注btn
    @InjectView(R.id.btn_follow)
    Button mFollowEmcee;
    @InjectView(R.id.btn_back_index)
    Button mBtnBackIndex;
    private int roomnum;
    @InjectView(R.id.tv_liveend_num)
    TextView mNum;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_live_end_dialog,null);
        Dialog dialog = new Dialog(getActivity(),R.style.dialog_test);
        ButterKnife.inject(this,view);
        dialog.setContentView(view);

        initView(view);

        initData();

        return dialog;
    }

    private void initData() {
        roomnum = getArguments().getInt("roomnum");
        mNum.setText(ChatServer.LIVE_USER_NUMS+"");
        showEndIsFollowEmcee();
    }

    private void initView(View view) {
        mFollowEmcee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFollow(AppContext.getInstance().getLoginUid(),roomnum);
            }
        });
        mBtnBackIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
                dismiss();
            }
        });

    }
    /**
     * @dw 关注
     * @param uid 当前用户id
     * @param touid 关注用户id
     * */
    private void showFollow(int uid,int touid){
        StringCallback callback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }
            @Override
            public void onResponse(String response) {
                ApiUtils.checkIsSuccess(response);
                if(mFollowEmcee.getText().toString().equals(getResources().getString(R.string.follow))){
                    mFollowEmcee.setText(getResources().getString(R.string.alreadyfollow));
                }else{
                    mFollowEmcee.setText(getResources().getString(R.string.follow));
                }

            }
        };
        PhoneLiveApi.showFollow(uid,touid, AppContext.getInstance().getToken(),callback);
    }

    //直播结束判断当前主播是否关注过
    private void showEndIsFollowEmcee() {

        StringCallback callback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                String res = ApiUtils.checkIsSuccess(response);//0：未关注1:关注
                if(res != null && isAdded()){
                    if(res.equals("0")){
                        mFollowEmcee.setText(getString(R.string.follow));
                    }else{
                        mFollowEmcee.setText(getString(R.string.alreadyfollow));
                    }
                }
            }
        };
        PhoneLiveApi.getIsFollow(AppContext.getInstance().getLoginUid(),roomnum,callback);//判断当前主播是否已经关注
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        OkHttpUtils.getInstance().cancelTag("getIsFollow");
        if(isAdded()){
            getActivity().finish();
        }
    }


}
