package com.ruziniu.phonelive.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.widget.LoadUrlImageView;

import java.util.ArrayList;

/*展示视频适配器*/
public class DisplayVideoAdapter extends BaseAdapter {
    private ArrayList<UserBean> mUserList;
    private LayoutInflater inflater;
    private Context context;

    public DisplayVideoAdapter(Context context,LayoutInflater inflater, ArrayList<UserBean> mUserList) {
        this.inflater = inflater;
        this.mUserList = mUserList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mUserList.size();
    }

    @Override
    public Object getItem(int position) {
        return mUserList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.item_video,null);
            viewHolder = new ViewHolder();
            viewHolder.mName = (TextView) convertView.findViewById(R.id.tv_nicheng);
            viewHolder.mTime = (TextView) convertView.findViewById(R.id.tv_time);
            viewHolder.mRivImage = (LoadUrlImageView) convertView.findViewById(R.id.iv_newest_item_user);
            convertView.setTag(viewHolder);
        }
        UserBean user = mUserList.get(position);
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.mName.setText(user.getTitle());
        viewHolder.mTime.setText(user.getNewtime());
        Glide.with(context)
                .load(user.getUrl_img())
                .centerCrop()
                .placeholder(R.drawable.xiaomoren)
                .crossFade()
                .fitCenter()
                .into(viewHolder.mRivImage);
        return convertView;
    }
    private class ViewHolder{
        public TextView mName,mTime;
        public LoadUrlImageView mRivImage;
    }

}


