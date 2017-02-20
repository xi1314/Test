package com.ruziniu.phonelive.bean;

/**
 * Created by weipeng on 16/8/16.
 */
public class RechargeBean {
    public  int price;//人民币
    public  String priceExplain;//充值说明
    public  int recharDiamondsNum;//充值钻石数量
    public  String priceText;//充值人民币组成字符

    public RechargeBean(int price, String priceExplain, int recharDiamondsNum, String priceText) {
        this.price = price;
        this.priceExplain = priceExplain;
        this.recharDiamondsNum = recharDiamondsNum;
        this.priceText = priceText;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getPriceExplain() {
        return priceExplain;
    }

    public void setPriceExplain(String priceExplain) {
        this.priceExplain = priceExplain;
    }

    public int getRecharDiamondsNum() {
        return recharDiamondsNum;
    }

    public void setRecharDiamondsNum(int recharDiamondsNum) {
        this.recharDiamondsNum = recharDiamondsNum;
    }

    public String getPriceText() {
        return priceText;
    }

    public void setPriceText(String priceText) {
        this.priceText = priceText;
    }
}
