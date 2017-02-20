package com.ruziniu.phonelive.viewpagerfragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.fragment.AttentionFragment;
import com.ruziniu.phonelive.fragment.DoubleAttentionFragment;
import com.ruziniu.phonelive.fragment.SchoolOutFragment;
import com.ruziniu.phonelive.adapter.ViewPageFragmentAdapter;
import com.ruziniu.phonelive.base.BaseFragment;
import com.ruziniu.phonelive.fragment.HotFragment;
import com.ruziniu.phonelive.fragment.NewestFragment;
import com.ruziniu.phonelive.fragment.SchoolInFragment;
import com.ruziniu.phonelive.interf.ListenMessage;
import com.ruziniu.phonelive.ui.other.PhoneLivePrivateChat;
import com.ruziniu.phonelive.utils.TDevice;
import com.ruziniu.phonelive.utils.UIHelper;
import com.ruziniu.phonelive.widget.PagerSlidingTabStrip;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class IndexPagerFragment extends BaseFragment implements ListenMessage{

    private View view;
    @InjectView(R.id.mviewpager)
    ViewPager pager;


    @InjectView(R.id.tabs)
    PagerSlidingTabStrip tabs;


    @InjectView(R.id.iv_hot_new_message)
    ImageView mIvNewMessage;

    private ViewPageFragmentAdapter viewPageFragmentAdapter;

    public static int mSex = 0;

    public static String mArea = "";

    //是否在后台
    private boolean isPause = false;

    private  EMMessageListener mMsgListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_hot,null);
            ButterKnife.inject(this,view);

            initView();
            initData();

        }else{
            mIvNewMessage.setVisibility(View.GONE);
            tabs.setViewPager(pager);
        }

        return view;
    }

    @Override
    public void initData() {
        //获取私信未读数量
        if(PhoneLivePrivateChat.getUnreadMsgsCount() > 0){
            mIvNewMessage.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        //注册私信广播接受
        isPause = false;
        listenMessage();
    }

    @Override
    public void onPause() {
        super.onPause();
        isPause = true;
        unListen();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @OnClick({R.id.iv_hot_private_chat,R.id.iv_hot_search})
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.iv_hot_private_chat:
                int uid = AppContext.getInstance().getLoginUid();
                if(0 < uid){
                    mIvNewMessage.setVisibility(View.GONE);
                    UIHelper.showPrivateChatSimple(getActivity(),uid);
                }

                break;
            case R.id.iv_hot_search:
                UIHelper.showScreen(getActivity());
                break;
        }
    }

    private void initView() {
        mIvNewMessage.setVisibility(View.GONE);
        viewPageFragmentAdapter = new ViewPageFragmentAdapter(getFragmentManager(),pager);
        viewPageFragmentAdapter.addTab(getString(R.string.attention), "gz", DoubleAttentionFragment.class, getBundle());
        viewPageFragmentAdapter.addTab(getString(R.string.hot), "rm", HotFragment.class, getBundle());
        viewPageFragmentAdapter.addTab(getString(R.string.daren), "dr", NewestFragment.class, getBundle());
        viewPageFragmentAdapter.addTab("校内", "xn", SchoolInFragment.class, getBundle());
        viewPageFragmentAdapter.addTab("校外", "xw", SchoolOutFragment.class, getBundle());
        viewPageFragmentAdapter.addTab("视频", "xw", AttentionFragment.class, getBundle());
        pager.setAdapter(viewPageFragmentAdapter);

        pager.setOffscreenPageLimit(1);
        tabs.setViewPager(pager);
        tabs.setUnderlineColor(getResources().getColor(R.color.global));
        tabs.setDividerColor(getResources().getColor(R.color.global));
        tabs.setIndicatorColor(getResources().getColor(R.color.backgroudcolor));
        tabs.setTextColor(getResources().getColor(R.color.white));
        tabs.setTextSize((int) TDevice.dpToPixel(15));
        tabs.setSelectedTextColor(getResources().getColor(R.color.white));
        tabs.setIndicatorHeight(2);
        tabs.setZoomMax(0);
        //tabs.setTypeface(Typeface.defaultFromStyle(getResources().getDimensionPixelSize(R.dimen.text_size_10)),10);
        tabs.setIndicatorColorResource(R.color.white);

        pager.setCurrentItem(1);

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //mRegion.setVisibility(1 == position? View.VISIBLE:View.GONE);
                //tabs.setIndicatorColorResource(1 == position? R.color.global:R.color.white);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    public void listenMessage(){

        mMsgListener = new EMMessageListener() {

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                //收到消息
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIvNewMessage.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                //收到透传消息
            }

            @Override
            public void onMessageReadAckReceived(List<EMMessage> messages) {
                //收到已读回执
            }

            @Override
            public void onMessageDeliveryAckReceived(List<EMMessage> message) {
                //收到已送达回执
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {
                //消息状态变动
            }
        };
        EMClient.getInstance().chatManager().addMessageListener(mMsgListener);

    }
    public void unListen(){
        EMClient.getInstance().chatManager().removeMessageListener(mMsgListener);
    }



    @Override
    public void onDestroy() {
        //注销广播
        super.onDestroy();
        unListen();

    }

    private Bundle getBundle( ) {
       Bundle bundle = new Bundle();

       return bundle;
   }
}
