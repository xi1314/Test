package com.ruziniu.phonelive.em;

import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.fragment.UserInformationFragment;
import com.ruziniu.phonelive.ui.SearchActivity;
import com.ruziniu.phonelive.ui.StartLiveActivity;
import com.ruziniu.phonelive.viewpagerfragment.IndexPagerFragment;
import com.ruziniu.phonelive.viewpagerfragment.PrivateChatCorePagerFragment;

/**
 * Created by Administrator on 2016/3/9.
 */
public enum  MainTab {
    INDEX(0, R.drawable.btn_tab_hot_background,0, IndexPagerFragment.class),
    HOME1(1, R.drawable.btn_tab_search,1, SearchActivity.class),
    LIVE(2, R.drawable.btn_tab_live_background,2, StartLiveActivity.class),
    HOME2(3, R.drawable.btn_tab_chat,3, PrivateChatCorePagerFragment.class),
    HOME(4, R.drawable.btn_tab_home_background,4, UserInformationFragment.class);
    private int idx;
    private int resName;
    private int resIcon;
    private Class<?> clz;

    private MainTab(int idx, int resIcon,int resName, Class<?> clz) {
        this.idx = idx;
        this.resIcon = resIcon;
        this.resName = resName;
        this.clz = clz;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getResName() {
        return resName;
    }

    public void setResName(int resName) {
        this.resName = resName;
    }

    public int getResIcon() {
        return resIcon;
    }

    public void setResIcon(int resIcon) {
        this.resIcon = resIcon;
    }

    public Class<?> getClz() {
        return clz;
    }

    public void setClz(Class<?> clz) {
        this.clz = clz;
    }
}
