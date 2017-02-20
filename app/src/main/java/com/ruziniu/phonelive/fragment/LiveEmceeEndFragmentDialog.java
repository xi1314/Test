package com.ruziniu.phonelive.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ruziniu.phonelive.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 主播结束 直播 弹窗
 */
public class LiveEmceeEndFragmentDialog extends DialogFragment {
    @InjectView(R.id.rl_livestop)
    protected RelativeLayout mLayoutLiveStop;

    //结束直播收获映票数量
    @InjectView(R.id.tv_live_end_yp_num)
    TextView mTvLiveEndYpNum;


    //直播结束房间人数
    @InjectView(R.id.tv_live_end_num)
    TextView mTvLiveEndUserNum;

    @InjectView(R.id.btn_back_index)
    Button mBtnBackIndex;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_live_emcee_end_dialog,null);
        Dialog dialog = new Dialog(getActivity(),R.style.dialog_test);
        ButterKnife.inject(this,view);
        dialog.setContentView(view);

        initView(view);

        initData();
        return dialog;
    }

    private void initData() {
        String ypNum = getArguments().getString("ypNum");
        String liveNum = getArguments().getString("liveNum");

        mTvLiveEndUserNum.setText(ypNum);
        mTvLiveEndYpNum.setText(liveNum);
    }

    private void initView(View view) {
        mBtnBackIndex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
                dismiss();
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        //getActivity().finish();
    }
}
