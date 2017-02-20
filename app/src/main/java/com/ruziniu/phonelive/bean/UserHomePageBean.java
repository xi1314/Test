package com.ruziniu.phonelive.bean;

import java.util.List;

/**
 * 他人信息中心数据模型
 */
public class UserHomePageBean {
    private int id;
    private String user_nicename;
    private String avatar;
    private int sex;
    private String signature;
    private String experience;
    private String consumption;
    private String votestotal;
    private String province;
    private String city;
    private int isrecommend;
    private int level;
    private int attentionnum;
    private int fansnum;
    private int liverecordnum;
    private int isattention;
    private List<OrderBean> coinrecord3;
    private int isblack;
    private int isblackto;
    private  String mobile;
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getIsblackto() {
        return isblackto;
    }

    public void setIsblackto(int isblackto) {
        this.isblackto = isblackto;
    }

    public int getIsblack() {
        return isblack;
    }

    public void setIsblack(int isblack) {
        this.isblack = isblack;
    }

    public List<OrderBean> getCoinrecord3() {
        return coinrecord3;
    }

    public void setCoinrecord3(List<OrderBean> coinrecord3) {
        this.coinrecord3 = coinrecord3;
    }

    public int getIsattention() {
        return isattention;
    }

    public void setIsattention(int isattention) {
        this.isattention = isattention;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLiverecordnum() {
        return liverecordnum;
    }

    public void setLiverecordnum(int liverecordnum) {
        this.liverecordnum = liverecordnum;
    }

    public int getFansnum() {
        return fansnum;
    }

    public void setFansnum(int fansnum) {
        this.fansnum = fansnum;
    }

    public int getAttentionnum() {
        return attentionnum;
    }

    public void setAttentionnum(int attentionnum) {
        this.attentionnum = attentionnum;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getIsrecommend() {
        return isrecommend;
    }

    public void setIsrecommend(int isrecommend) {
        this.isrecommend = isrecommend;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getVotestotal() {
        return votestotal;
    }

    public void setVotestotal(String votestotal) {
        this.votestotal = votestotal;
    }

    public String getConsumption() {
        return consumption;
    }

    public void setConsumption(String consumption) {
        this.consumption = consumption;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUser_nicename() {
        return user_nicename;
    }

    public void setUser_nicename(String user_nicename) {
        this.user_nicename = user_nicename;
    }
}
