package com.ruziniu.phonelive.bean;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;

/**
 * Created by Administrator on 2016/3/16.
 */
public class ChatBean extends UserBean{
    private SpannableStringBuilder userNick;
    private SpannableStringBuilder sendChatMsg;
    private int type;
    private String method;//聊天方法 HHH
    private String content;//聊天纯文本 HHH

    public int getType() {
        return type;
    }



    public void setType(int type) {
        this.type = type;
    }

    public SpannableStringBuilder getUserNick() {
        return userNick;
    }

    public void setUserNick(SpannableStringBuilder userNick) {
        this.userNick = userNick;
    }

    public SpannableStringBuilder getSendChatMsg() {
        return sendChatMsg;
    }

    public void setSendChatMsg(SpannableStringBuilder sendChatMsg) {
        this.sendChatMsg = sendChatMsg;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
