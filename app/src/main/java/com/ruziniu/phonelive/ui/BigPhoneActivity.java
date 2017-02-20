package com.ruziniu.phonelive.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.utils.StringUtils;
import com.ruziniu.phonelive.widget.LoadUrlImageView;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import static com.mob.tools.utils.R.getScreenWidth;

public class BigPhoneActivity extends AppCompatActivity {

    private PhotoView mBigPhine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_phone);
        initView();
        initDate();
    }


    private void initView() {
        mBigPhine = (PhotoView) findViewById(R.id.iv_big_phone);
    }


    private void initDate() {

        Bundle bundle = getIntent().getBundleExtra("BIGPHONE");

        Glide.with(this)
                .load(bundle.get("phone"))
                .into(mBigPhine);
        PhotoViewAttacher mAttacher = new PhotoViewAttacher(mBigPhine);
    }
}
