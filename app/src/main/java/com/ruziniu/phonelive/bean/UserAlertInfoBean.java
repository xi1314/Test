package com.ruziniu.phonelive.bean;

/**
 * 弹窗用户基本信息
 */
public class UserAlertInfoBean {
    private int attention;
    private int fans;
    private UserBean contribute;
    private int consumption;
    private int votestotal;
    private int isblackto;
    private int level;
    private String city;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getIsblackto() {
        return isblackto;
    }

    public void setIsblackto(int isblackto) {
        this.isblackto = isblackto;
    }

    public int getAttention() {
        return attention;
    }

    public void setAttention(int attention) {
        this.attention = attention;
    }

    public int getVotestotal() {
        return votestotal;
    }

    public void setVotestotal(int votestotal) {
        this.votestotal = votestotal;
    }

    public int getConsumption() {
        return consumption;
    }

    public void setConsumption(int consumption) {
        this.consumption = consumption;
    }

    public UserBean getContribute() {
        return contribute;
    }

    public void setContribute(UserBean contribute) {
        this.contribute = contribute;
    }

    public int getFans() {
        return fans;
    }

    public void setFans(int fans) {
        this.fans = fans;
    }
}
