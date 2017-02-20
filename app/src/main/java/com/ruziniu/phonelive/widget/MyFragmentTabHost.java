package com.ruziniu.phonelive.widget;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentTabHost;
import android.util.AttributeSet;

import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.ui.MainActivity;
import com.ruziniu.phonelive.ui.SearchActivity;
import com.ruziniu.phonelive.utils.UIHelper;

/**
 * tabhost
 * 
 */

public class MyFragmentTabHost extends FragmentTabHost {
	
	private String mCurrentTag;
	
	private String mNoTabChangedTag = "2";
	private Context context;

	
	public MyFragmentTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}
	
	@Override
	public void onTabChanged(String tag) {

		if (tag.equals(mNoTabChangedTag)) {
			setCurrentTabByTag(mCurrentTag);
			((MainActivity)context).startLive();

		}else if (tag.equals("1")){
			context.startActivity(new Intent(context, SearchActivity.class));
		}else if (tag.equals("3")){
			UserBean userBean = AppContext.getInstance().getLoginUser();
			UIHelper.showPrivateChatSimple(context,userBean.getId());
		}else {
			super.onTabChanged(tag);
			mCurrentTag = tag;
		}
	}
	
	public void setNoTabChangedTag(String tag) {
		this.mNoTabChangedTag = tag;
	}
}
