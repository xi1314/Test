package com.ruziniu.phonelive.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.base.ShowLiveActivityBase;
import com.ruziniu.phonelive.bean.ChatBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播间聊天
 */
public class ChatListAdapter extends BaseAdapter {
    private List<ChatBean> mChats = new ArrayList<>();
    private Context mContext;

    public ChatListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setChats(List<ChatBean> chats){
        this.mChats = chats;
    }
    @Override
    public int getCount() {
        return mChats.size();
    }

    @Override
    public Object getItem(int position) {
        return mChats.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = View.inflate(AppContext.getInstance(),R.layout.item_live_chat,null);
            viewHolder = new ViewHolder();
            viewHolder.mChat1 = (TextView) convertView.findViewById(R.id.tv_chat_1);
            viewHolder.mChat2 = (TextView) convertView.findViewById(R.id.tv_chat_2);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ChatBean c = mChats.get(position);
        if(c.getType() != 13){
            viewHolder.mChat1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ShowLiveActivityBase)mContext).chatListItemClick(mChats.get(position));
                }
            });
        }

        viewHolder.mChat1.setText(c.getUserNick());
        viewHolder.mChat2.setText(c.getSendChatMsg());
        return convertView;
    }
    protected class ViewHolder{
        TextView mChat1,mChat2;
    }
}
