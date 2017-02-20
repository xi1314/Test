package com.ruziniu.phonelive.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.bean.UserBean;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by a on 2016/5/13.
 */
public class MyListViewAdapterRight extends BaseAdapter{
    private HashMap<String,ArrayList<UserBean>>  allData;
    private  Context context;
    private  int selectIndex;

    public MyListViewAdapterRight(HashMap<String, ArrayList<UserBean>> allData, Context context, int selectIndex) {
        this.allData=allData;
        this.context=context;
        this.selectIndex=selectIndex;
    }

    @Override
    public int getCount() {
        return allData.get(String.valueOf(selectIndex)).size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if(convertView==null){
            convertView=View.inflate(context, R.layout.item_listview_right,null);
            vh=new ViewHolder();
            vh.tv= (TextView) convertView.findViewById(R.id.textview);
            convertView.setTag(vh);
        }else {
            vh= (ViewHolder) convertView.getTag();
        }
        vh.tv.setText(allData.get(String.valueOf(selectIndex)).get(position).getTitle());

        return convertView;
    }

    public void setIndex(int index){
        selectIndex=index;
    }

    class ViewHolder{
        TextView tv;
    }
}
