package com.ruziniu.phonelive.bean;

/**
 * Created by Administrator on 2016/4/5.
 */
public class ProfitBean {
    private String votes;
    private String canwithdraw;//可提现
    private String withdraw;//今日可提现

    public String getVotes() {
        return votes;
    }

    public void setVotes(String votes) {
        this.votes = votes;
    }

    public String getCanwithdraw() {
        return canwithdraw;
    }

    public void setCanwithdraw(String canwithdraw) {
        this.canwithdraw = canwithdraw;
    }

    public String getWithdraw() {
        return withdraw;
    }

    public void setWithdraw(String withdraw) {
        this.withdraw = withdraw;
    }
}
