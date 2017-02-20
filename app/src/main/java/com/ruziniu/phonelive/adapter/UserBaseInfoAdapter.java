package com.ruziniu.phonelive.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.ui.DrawableRes;
import com.ruziniu.phonelive.utils.TLog;
import com.ruziniu.phonelive.widget.CircleImageView;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.List;

import okhttp3.Call;

/**
 *关注粉丝列表
 */
public class UserBaseInfoAdapter extends BaseAdapter {
    private List<UserBean> users;
    public UserBaseInfoAdapter(List<UserBean> users) {
        this.users = users;
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
            convertView = View.inflate(AppContext.getInstance(),R.layout.item_attention_fans,null);
            viewHolder = new ViewHolder();
            viewHolder.mUHead = (CircleImageView) convertView.findViewById(R.id.cv_userHead);
            viewHolder.mUSex  = (ImageView) convertView.findViewById(R.id.tv_item_usex);
            viewHolder.mULevel  = (ImageView) convertView.findViewById(R.id.tv_item_ulevel);
            viewHolder.mUNice = (TextView) convertView.findViewById(R.id.tv_item_uname);
            viewHolder.mUSign = (TextView) convertView.findViewById(R.id.tv_item_usign);
            viewHolder.mIsFollow = (ImageView) convertView.findViewById(R.id.iv_item_attention);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final UserBean u = users.get(position);
        Glide.with(AppContext.getInstance())
                .load(u.getAvatar())
                .dontAnimate()
                .placeholder(R.drawable.null_blacklist)
                .into(viewHolder.mUHead);
        viewHolder.mUSex.setImageResource(u.getSex() == 1 ? R.drawable.global_male : R.drawable.global_female);
        if (u.getId()==AppContext.getInstance().getLoginUser().getId()){
            viewHolder.mIsFollow.setVisibility(View.GONE);
        }else{
            viewHolder.mIsFollow.setVisibility(View.VISIBLE);
        }
        viewHolder.mIsFollow.setImageResource(u.getIsattention() == 1 ? R.drawable.me_following:R.drawable.me_follow);
        viewHolder.mULevel.setImageResource(DrawableRes.LevelImg[u.getLevel() == 0?0:u.getLevel()-1]);
        viewHolder.mUNice.setText(u.getUser_nicename());
        viewHolder.mUSign.setText(u.getSignature());
        viewHolder.mIsFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                PhoneLiveApi.showFollow(AppContext.getInstance().getLoginUid(), u.getId(), AppContext.getInstance().getToken(),new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                    }

                    @Override
                    public void onResponse(String response) {
                        if (u.getIsattention() == 1) {//1 已经关注 0未关注
                            u.setIsattention(0);
                            ((ImageView)v.findViewById(R.id.iv_item_attention)).setImageResource(R.drawable.me_follow);
                        } else {
                            u.setIsattention(1);
                            ((ImageView)v.findViewById(R.id.iv_item_attention)).setImageResource(R.drawable.me_following);
                        }
                    }
                });
            }
        });
        return convertView;
    }
    private class ViewHolder{
        public CircleImageView mUHead;
        public ImageView mUSex,mULevel,mIsFollow;
        public TextView mUNice,mUSign;
    }
}
