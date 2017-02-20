package com.ruziniu.phonelive.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.bean.BlackBean;
import com.ruziniu.phonelive.ui.DrawableRes;
import com.ruziniu.phonelive.widget.CircleImageView;

import java.util.List;


/**
 *黑名单
 */
public class BlackInfoAdapter extends BaseAdapter {
    private List<BlackBean> users;
    private Context context;
    public BlackInfoAdapter(List<BlackBean> users, Context application) {
        this.users = users;
        this.context = application;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = View.inflate(AppContext.getInstance(),R.layout.item_black_info,null);
            viewHolder = new ViewHolder();
            viewHolder.mUHead = (CircleImageView) convertView.findViewById(R.id.cv_userHead);
            viewHolder.mUSex  = (ImageView) convertView.findViewById(R.id.tv_item_usex);
            viewHolder.mULevel  = (ImageView) convertView.findViewById(R.id.tv_item_ulevel);
            viewHolder.mUNice = (TextView) convertView.findViewById(R.id.tv_item_uname);
            viewHolder.mUSign = (TextView) convertView.findViewById(R.id.tv_item_usign);

            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BlackBean u = users.get(position);
        Glide.with(context)
                .load(u.getUrl_img())
                .dontAnimate()
                .placeholder(R.drawable.null_blacklist)
                .crossFade()
                .fitCenter()
                .into(viewHolder.mUHead);
        viewHolder.mUSex.setImageResource(u.getSex() == 1 ? R.drawable.global_male : R.drawable.global_female);
        viewHolder.mULevel.setImageResource(DrawableRes.LevelImg[u.getLevel()-1]);
        viewHolder.mUNice.setText(u.getUser_nicename());
        viewHolder.mUSign.setText(u.getSignature());
        return convertView;
    }
    private class ViewHolder{
        public CircleImageView mUHead;
        public ImageView mUSex,mULevel;
        public TextView mUNice,mUSign;
    }
}
