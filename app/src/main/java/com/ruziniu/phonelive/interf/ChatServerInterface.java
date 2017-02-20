package com.ruziniu.phonelive.interf;

import android.util.SparseArray;

import com.ruziniu.phonelive.bean.ChatBean;
import com.ruziniu.phonelive.bean.SendGiftBean;
import com.ruziniu.phonelive.bean.UserBean;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2016/3/17.
 */
public interface ChatServerInterface {
    void onMessageListen(int type,ChatBean chatBean);
    void onConnect(boolean res);
    void onUserList(List<UserBean> uList, String votes);
    void onUserStateChange(UserBean user, boolean upordown);
    void onSystemNot(int code);
    void onShowSendGift(SendGiftBean contentJson,ChatBean chatBean);
    void setManage(JSONObject contentJson,ChatBean chatBean);
    void onPrivilegeAction(ChatBean c, JSONObject contentJson);
    void onLit();
    void onAddZombieFans(String ct);
    void onError();
}
