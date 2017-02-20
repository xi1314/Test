package com.ruziniu.phonelive.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.utils.UIHelper;
import com.ruziniu.phonelive.widget.AvatarView;
import com.ruziniu.phonelive.widget.LoadUrlImageView;

import java.util.List;

//热门主播
public class LiveUserAdapter extends BaseAdapter {
    private List<UserBean> mUserList;
    private LayoutInflater inflater;
    private Context context;

    public LiveUserAdapter(Context context, LayoutInflater inflater, List<UserBean> mUserList) {
        this.mUserList = mUserList;
        this.inflater = inflater;
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
            convertView = inflater.inflate(R.layout.item_hot_user,null);
            viewHolder = new ViewHolder();
            viewHolder.mUserNick = (TextView) convertView.findViewById(R.id.tv_live_nick);
            viewHolder.mUserLocal = (TextView) convertView.findViewById(R.id.tv_live_local);
            viewHolder.mUserNums = (TextView) convertView.findViewById(R.id.tv_live_usernum);
            viewHolder.mUserHead = (AvatarView) convertView.findViewById(R.id.iv_live_user_head);
            viewHolder.mUserPic = (LoadUrlImageView) convertView.findViewById(R.id.iv_live_user_pic);
            viewHolder.mRoomTitle = (TextView) convertView.findViewById(R.id.tv_hot_room_title);
            viewHolder.mIslive = (LinearLayout) convertView.findViewById(R.id.tv_islive);
            viewHolder.mLocation = (LinearLayout) convertView.findViewById(R.id.ll_location);
            convertView.setTag(viewHolder);
        }
        final  UserBean user = mUserList.get(position);
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.mUserHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIHelper.showHomePageActivity(context,user.getId());
            }
        });
        if (user.getIslive().equals("0")){
            viewHolder.mIslive.setVisibility(View.GONE);
        }
        viewHolder.mUserNick.setText(user.getUser_nicename());
        viewHolder.mUserLocal.setText(user.getCity()+" "+user.getAddress());
        //viewHolder.mUserPic.setImageLoadUrl(user.getAvatar());
        viewHolder.mUserHead.setAvatarUrl(user.getAvatar_thumb());
        viewHolder.mUserNums.setText(String.valueOf(user.getNums()));
        viewHolder.mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UIHelper.showHomePageActivity(context,user.getId());
            }
        });
        //用于平滑加载图片
        Glide
                .with(AppContext.getInstance())
                .load(user.getAvatar())
                .dontAnimate()
                .placeholder(R.drawable.bigmoren)
                .into(viewHolder.mUserPic);

        /*if(null !=user.getTitle()){
            viewHolder.mRoomTitle.setVisibility(View.VISIBLE);
            viewHolder.mRoomTitle.setText(user.getTitle());
        }*/
        return convertView;
    }
    private class ViewHolder{
        public TextView mUserNick,mUserLocal,mUserNums,mRoomTitle;
        public LoadUrlImageView mUserPic;
        public AvatarView mUserHead;
        public LinearLayout mIslive ,mLocation;
    }
}


