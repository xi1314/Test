package com.ruziniu.phonelive.bean;

import java.util.List;

/**
 * Created by weipeng on 16/8/23.
 */
public class SendSocketMessageBean {


    /**
     * msg : [{"_method_":"SendMsg","action":"0","ct":"我点亮了","msgtype":"2","timestamp":"2016-08-23 10:01:35","tougood":"","touname":"","heart":3,"touid":0,"ugood":"","city":"泰安市","level":1,"uid":1790,"sex":0,"uname":"叶雨梧桐","uhead":"http://wx.qlogo.cn/mmopen/dxKEsjQBP4T2PrLym74cks5kROnK1nRfotcHBVwPjiahVcibPwZiaAXYVsRVrdoq4wMmxGWcNucUvgZgSt2Zf4nofP3BoZuHMgg/0","usign":"这家伙很懒，什么都没留下"}]
     * retcode : 000000
     * retmsg : ok
     */

    private String retcode;
    private String retmsg;
    /**
     * _method_ : SendMsg
     * action : 0
     * ct : 我点亮了
     * msgtype : 2
     * timestamp : 2016-08-23 10:01:35
     * tougood :
     * touname :
     * heart : 3
     * touid : 0
     * ugood :
     * city : 泰安市
     * level : 1
     * uid : 1790
     * sex : 0
     * uname : 叶雨梧桐
     * uhead : http://wx.qlogo.cn/mmopen/dxKEsjQBP4T2PrLym74cks5kROnK1nRfotcHBVwPjiahVcibPwZiaAXYVsRVrdoq4wMmxGWcNucUvgZgSt2Zf4nofP3BoZuHMgg/0
     * usign : 这家伙很懒，什么都没留下
     */

    private List<MsgBean> msg;

    public String getRetcode() {
        return retcode;
    }

    public void setRetcode(String retcode) {
        this.retcode = retcode;
    }

    public String getRetmsg() {
        return retmsg;
    }

    public void setRetmsg(String retmsg) {
        this.retmsg = retmsg;
    }

    public List<MsgBean> getMsg() {
        return msg;
    }

    public void setMsg(List<MsgBean> msg) {
        this.msg = msg;
    }

    public static class MsgBean {
        private String _method_;
        private String action;
        private String ct;
        private String msgtype;
        private String timestamp;
        private String tougood;
        private String touname;
        private int touid;
        private String ugood;
        private String city;
        private int level;
        private int uid;
        private int sex;
        private String uname;
        private String uhead;
        private String usign;
        private String evensend;

        public String getEvensend() {
            return evensend;
        }

        public void setEvensend(String evensend) {
            this.evensend = evensend;
        }

        public String get_method_() {
            return _method_;
        }

        public void set_method_(String _method_) {
            this._method_ = _method_;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getCt() {
            return ct;
        }

        public void setCt(String ct) {
            this.ct = ct;
        }

        public String getMsgtype() {
            return msgtype;
        }

        public void setMsgtype(String msgtype) {
            this.msgtype = msgtype;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getTougood() {
            return tougood;
        }

        public void setTougood(String tougood) {
            this.tougood = tougood;
        }

        public String getTouname() {
            return touname;
        }

        public void setTouname(String touname) {
            this.touname = touname;
        }

        public int getTouid() {
            return touid;
        }

        public void setTouid(int touid) {
            this.touid = touid;
        }

        public String getUgood() {
            return ugood;
        }

        public void setUgood(String ugood) {
            this.ugood = ugood;
        }

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

        public int getUid() {
            return uid;
        }

        public void setUid(int uid) {
            this.uid = uid;
        }

        public int getSex() {
            return sex;
        }

        public void setSex(int sex) {
            this.sex = sex;
        }

        public String getUname() {
            return uname;
        }

        public void setUname(String uname) {
            this.uname = uname;
        }

        public String getUhead() {
            return uhead;
        }

        public void setUhead(String uhead) {
            this.uhead = uhead;
        }

        public String getUsign() {
            return usign;
        }

        public void setUsign(String usign) {
            this.usign = usign;
        }
    }
}
