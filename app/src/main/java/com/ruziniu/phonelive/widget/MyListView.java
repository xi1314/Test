package com.ruziniu.phonelive.widget;

import android.view.MotionEvent;
import android.widget.GridView;
import android.widget.ListView;

public class MyListView extends GridView {
	public MyListView(android.content.Context context,
					  android.util.AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		getParent().requestDisallowInterceptTouchEvent(true);
		return super.dispatchTouchEvent(ev);
	}

}