package com.ruziniu.phonelive.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.WxPay.WChatPay;
import com.ruziniu.phonelive.alipay.AliPay;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.bean.RechargeBean;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import okhttp3.Call;

/**
 * 我的钻石
 */
public class UserDiamondsActivity extends ToolBarBaseActivity {

    private RelativeLayout mWxPay;

    private RelativeLayout mAliPay;
    private List<RechargeBean> rechanList;
    @InjectView(R.id.lv_select_num_list)
    ListView mSelectNumListItem;
    private TextView mPayName;
    private TextView mCoin;
    private final int WXPAY = 1;
    private final int ALIPAY = 2;
    private int PAYMODE = WXPAY;
    private View mHeadView;
    private AliPay mAliPayUtils;
    private int[] price;
    private int[] diamondsNum;
    private WChatPay mWChatPay;
    //是否是第一次充值
    private boolean isFirstCharge = false;

    private String explain[] = {"", "", "", "赠送10钻石", "赠送90钻石", "赠送300钻石", "赠送1120钻石"};
    private TextView mTvWeiXin;
    private TextView mTvAlipay;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_diamonds;
    }

    @Override
    public void initView() {
        mHeadView = getLayoutInflater().inflate(R.layout.view_diamonds_head, null);
        mWxPay = (RelativeLayout) mHeadView.findViewById(R.id.rl_wxpay);
        mAliPay = (RelativeLayout) mHeadView.findViewById(R.id.rl_alipay);
        mPayName = (TextView) mHeadView.findViewById(R.id.tv_payname);
        mCoin = (TextView) mHeadView.findViewById(R.id.tv_coin);
        mTvWeiXin = (TextView) mHeadView.findViewById(R.id.tv_diamond_weixin);
        mTvAlipay = (TextView) mHeadView.findViewById(R.id.tv_diamond_alipay);
        mSelectNumListItem.addHeaderView(mHeadView);

        getImageView(mWxPay, View.VISIBLE);
        getImageView(mAliPay, View.GONE);
        selected(mWxPay);
        //微信支付
        mWxPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PAYMODE = WXPAY;
                selected(mWxPay);
                mTvWeiXin.setTextColor(getResources().getColor(R.color.white));
                mTvAlipay.setTextColor(getResources().getColor(R.color.black));
            }
        });
        //支付宝
        mAliPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvWeiXin.setTextColor(getResources().getColor(R.color.black));
                mTvAlipay.setTextColor(getResources().getColor(R.color.white));
                PAYMODE = ALIPAY;
                selected(mAliPay);
            }
        });
        mSelectNumListItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (PAYMODE == WXPAY) {
//                    showToast2("微信支付暂未开放...");
//                    //WChatPay wxpay = new WChatPay(MyDiamondsActivity.this);
//                    //wxpay.initPay(String.valueOf(price[position-1]),String.valueOf(diamondsNum[position-1]));
//                    return;
//                }
                if (PAYMODE == ALIPAY)
                    mAliPayUtils.initPay(String.valueOf(price[position - 1]), String.valueOf(diamondsNum[position - 1]));
                else
                    mWChatPay.initPay(String.valueOf(price[position - 1]), String.valueOf(diamondsNum[position - 1]));

            }
        });

    }


    @Override
    public void initData() {
        requestData();
        mAliPayUtils = new AliPay(this);
        mWChatPay = new WChatPay(this);
        setActionBarTitle(getString(R.string.mydiamonds));
        diamondsNum = new int[]{20, 60, 300, 980, 2980, 5880, 15980};

        price = new int[]{1, 6, 30, 98, 298, 588, 1598};
        rechanList = new ArrayList<>();

    }

    private void getUserDiamondsNum() {
        PhoneLiveApi.getUserDiamondsNum(AppContext.getInstance().getLoginUid(),
                AppContext.getInstance().getToken(),
                new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        String res = ApiUtils.checkIsSuccess(response);
                        if (res == null) return;
                        fillUI(res);
                    }
                });
    }

    private void requestData() {
        getUserDiamondsNum();

        //判断是否是第一充值
        PhoneLiveApi.getCharge(AppContext.getInstance().getLoginUid(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {

                String res = ApiUtils.checkIsSuccess(response);
                if (res != null) {
                    try {
                        JSONObject object = new JSONObject(res);

                        for (int i = 0; i < price.length; i++) {
                            rechanList.add(new RechargeBean(price[i], explain[i], diamondsNum[i], price[i] + ".00"));
                        }
                        mSelectNumListItem.setAdapter(new RechangeAdapter());

                        if (object.getString("isnew").equals("1")) {
                            isFirstCharge = true;
                        } else {
                            isFirstCharge = false;
                        }
                        mCoin.setText(object.getString("coin"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private void fillUI(String res) {
        try {
            JSONObject jsonObj = new JSONObject(res);
            mCoin.setText(jsonObj.getString("coin"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

    }

    private void selected(RelativeLayout rl) {
        rl.setBackgroundColor(getResources().getColor(R.color.global));
        rl.getChildAt(1).setVisibility(View.VISIBLE);
        mPayName.setText(getString(R.string.paymode) + (PAYMODE == WXPAY ? getString(R.string.wxpay) : getString(R.string.alipay)));
        if (PAYMODE == WXPAY) {
            mAliPay.setBackgroundColor(getResources().getColor(R.color.white));
        } else {
            mWxPay.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    private ImageView getImageView(RelativeLayout rl, int displayMode) {
        ImageView imageView = new ImageView(this);
        rl.addView(imageView);
        imageView.setVisibility(displayMode);
        imageView.setImageResource(R.drawable.pay_choose);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        params.width = 60;
        params.height = 60;
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        imageView.setLayoutParams(params);
        return imageView;
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    //充值结果
    public void rechargeResult(boolean isOk, String rechargeMoney) {
        if (isOk) {
            //mCoin.setText((Integer.parseInt(mCoin.getText().toString()) + Integer.parseInt(rechargeMoney) + ""));
            requestData();
        }
    }

    private class RechangeAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return rechanList.size();
        }

        @Override
        public Object getItem(int position) {
            return rechanList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RechargeBean rechargeBean = rechanList.get(position);
            ViewHolder holder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_select_num, null);
                holder = new ViewHolder();
                holder.mDiamondsNum = (TextView) convertView.findViewById(R.id.tv_diamondsnum);
                holder.mPrieExplain = (TextView) convertView.findViewById(R.id.tv_price_explain);
                holder.mPriceText = (TextView) convertView.findViewById(R.id.bt_preice_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            //判断是否是第一次充值,如果不是就隐藏20
            if (!isFirstCharge && position == 0) {
                convertView.setVisibility(View.GONE);
            }

            holder.mDiamondsNum.setText(rechargeBean.getRecharDiamondsNum() + "");
            holder.mPrieExplain.setText(rechargeBean.getPriceExplain());
            holder.mPriceText.setText(rechargeBean.getPriceText());
            return convertView;
        }

        private class ViewHolder {
            TextView mDiamondsNum, mPrieExplain;
            TextView mPriceText;
        }
    }


    public void onResume() {
        super.onResume();
        //统计时长
        getUserDiamondsNum();
    }

    public void onPause() {
        super.onPause();
    }
}
