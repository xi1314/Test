package com.ruziniu.phonelive.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.utils.StringUtils;

/**
 * 带图片加载的图片控件
 */
public class LoadUrlImageView extends ImageView {
    private Activity aty;
    private int null_drawable = R.drawable.null_blacklist;
    public LoadUrlImageView(Context context) {
        super(context);
        init(context);
    }

    public int getNull_drawable() {
        return null_drawable;
    }

    public void setNull_drawable(int null_drawable) {
        this.null_drawable = null_drawable;
    }

    public LoadUrlImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    private void init(Context context) {
        aty = (Activity) context;

    }
    public LoadUrlImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setImageLoadUrl(String url) {
        if (StringUtils.isEmpty(url)) {
            //setImageResource(R.drawable.wuneirong);
            return;
        }
        // 由于头像地址默认加了一段参数需要去掉
        int end = url.indexOf('?');
        final String headUrl;
        if (end > 0) {
            headUrl = url.substring(0, end);
        } else {
            headUrl = url;
        }
        Glide.with(getContext())
                .load(headUrl)
                .dontAnimate()
                .crossFade()
                .fitCenter()
                .into(this);
    }
}
