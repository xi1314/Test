package com.ruziniu.phonelive.fragment;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.base.PrivateChatPageBase;
import com.ruziniu.phonelive.adapter.UserBaseInfoPrivateChatAdapter;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.bean.PrivateChatUserBean;
import com.ruziniu.phonelive.bean.PrivateMessage;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.utils.TLog;
import com.ruziniu.phonelive.utils.UIHelper;
import com.zhy.http.okhttp.callback.StringCallback;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Map;
import okhttp3.Call;

//私信已关注会话列表
public class FollowPrivateChatFragment extends PrivateChatPageBase {

    @Override
    public void initView(View view) {
        super.initView(view);
    }

    @Override
    protected void initCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    }

    @Override
    protected void onNewMessage(final EMMessage message) {
        //收到消息
        try {
            if((message.getIntAttribute("isfollow") != 1)) return;
            addMessage(message);

        } catch (HyphenateException e) {
            //没有传送是否关注标记
            TLog.log("关注[没有传送是否关注标记]");
            //未传递标记请求服务端判断
            PhoneLiveApi.getPmUserInfo(Integer.parseInt(message.getFrom()),AppContext.getInstance().getLoginUid(), new StringCallback() {
                @Override
                public void onError(Call call, Exception e) {

                }

                @Override
                public void onResponse(String response) {
                    String res = ApiUtils.checkIsSuccess(response);
                    if(null != res){
                        PrivateChatUserBean privateChatUserBean = new Gson().fromJson(res,PrivateChatUserBean.class);
                        if(privateChatUserBean.getIsattention2() == 1){
                            addMessage(message);
                        }
                    }

                }
            });
            e.printStackTrace();
        }

    }

    private void addMessage(EMMessage message){
        //判断是否在列表中,如果在更新最后一条信息,如果没在添加一条item
        if(!emConversationMap.containsKey(message.getFrom())){
            TLog.log("已关注[不存会话列表]");
            inConversationMapAddItem(message);

        }else{
            if(mPrivateChatListData == null)return;
            TLog.log("已关注[存在会话列表]");
            updataLastMessage(message);

        }
    }


    @Override
    public void initData() {
        mUser = AppContext.getInstance().getLoginUser();
        updatePrivateChatList();
        initConversationList(1);

    }





}
