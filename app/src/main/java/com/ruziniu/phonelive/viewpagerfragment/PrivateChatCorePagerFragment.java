package com.ruziniu.phonelive.viewpagerfragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.adapter.ViewPageFragmentAdapter;
import com.ruziniu.phonelive.base.BaseViewPagerFragment;
import com.ruziniu.phonelive.fragment.FollowPrivateChatFragment;
import com.ruziniu.phonelive.fragment.NotFollowPrivateChatFragment;

import butterknife.OnClick;


public class PrivateChatCorePagerFragment extends BaseViewPagerFragment {


    @Override
    protected void onSetupTabAdapter(View view,ViewPageFragmentAdapter adapter,ViewPager viewPager) {
        ((ImageView)view.findViewById(R.id.iv_private_chat_back)).setOnClickListener(this);
        initData();
        Bundle b1 = new Bundle();
        b1.putInt("ACTION",1);
        Bundle b2 = new Bundle();
        b2.putInt("ACTION",0);
        adapter.addTab(getString(R.string.friends), "friends", FollowPrivateChatFragment.class, b1);
        adapter.addTab(getString(R.string.nofollow), "nofollow", NotFollowPrivateChatFragment.class, b2);
        viewPager.setCurrentItem(1);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(adapter);

    }

    @Override
    public void initData() {

    }
    @OnClick({R.id.iv_private_chat_back})
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_private_chat_back:
                getActivity().finish();
                break;
        }
    }
}
