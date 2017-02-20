package com.ruziniu.phonelive.bean;

/**
 * Created by Administrator on 2016/4/13.
 */
public class PrivateChatUserBean extends UserBean {
    private String lastMessage;
    private boolean unreadMessage;
    private int isattention2;

    public int getIsattention2() {
        return isattention2;
    }

    public void setIsattention2(int isattention2) {
        this.isattention2 = isattention2;
    }

    public boolean isUnreadMessage() {
        return unreadMessage;
    }

    public void setUnreadMessage(boolean unreadMessage) {
        this.unreadMessage = unreadMessage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
