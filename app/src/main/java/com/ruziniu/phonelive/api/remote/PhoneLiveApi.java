package com.ruziniu.phonelive.api.remote;

import com.ruziniu.phonelive.AppConfig;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.bean.GiftBean;
import com.ruziniu.phonelive.bean.UserBean;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.concurrent.TimeUnit;

import cn.sharesdk.framework.PlatformDb;

/**
 * 接口获取
 */
public class PhoneLiveApi {



    /**
     * 判断主播是否直播
     */

    public static void isLive(int showid,  StringCallback callback) {
        String url = AppConfig.MAIN_URL;
        OkHttpUtils.get()
                .url(url)
                .addParams("service", "User.isLive")
                .addParams("showid", String.valueOf(showid))
                .tag("isLive")
                .build()
                .execute(callback);

    }
    /**
     * 上传视频判断
     */

    public static void checkuploadvideo(int uid, String time, StringCallback callback) {
        String url = AppConfig.MAIN_URL;
        OkHttpUtils.get()
                .url(url)
                .addParams("service", "User.checkuploadvideo")
                .addParams("uid", String.valueOf(uid))
                .addParams("time", time)
                .tag("checkuploadvideo")
                .build()
                .execute(callback);

    }

    /**
     * 登陆
     *
     * @param phone
     * @param code
     */

    public static void login(String phone, String code, StringCallback callback) {
        String url = AppConfig.MAIN_URL;
        OkHttpUtils.post()
                .url(url)
                .addParams("service", "user.userlogin")
                .addParams("user_login", phone)
                .addParams("code", code)
                .tag("phonelive")
                .build()
                .execute(callback);

    }

    /**
     * 视频
     *
     */

    public static void videomain(int num,String city,String area,String list1,String list2,StringCallback callback) {
        String url = AppConfig.MAIN_URL;
        OkHttpUtils.get()
                .url(url)
                .addParams("service", "User.videomain")
                .addParams("num", String.valueOf(num))
                .addParams("city", city)
                .addParams("area", area)
                .addParams("list1", list1)
                .addParams("list2", list2)
                .tag("videomain")
                .build()
                .execute(callback);

    }
    /**
     * 获取用户信息
     *
     * @param token    appkey
     * @param uid      用户id
     * @param callback 回调
     */
    public static void getMyUserInfo(int uid, String token, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getBaseInfo")
                .addParams("uid", String.valueOf(uid))
                .addParams("token", token)
                .tag("getMyUserInfo")
                .build()
                .execute(callback);

    }

    /**
     * 获取用户信息
     *
     * @param uid      用户id
     * @param callback 回调
     */
    public static void powers(int uid,  StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.powers")
                .addParams("uid", String.valueOf(uid))
                .tag("powers")
                .build()
                .execute(callback);

    }

    /**
     * 获取其他用户信息
     *
     * @param uid      用户id
     * @param callback 回调
     */
    public static void getOtherUserInfo(int uid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getUserInfo")
                .addParams("uid", String.valueOf(uid))
                .tag("phonelive")
                .build()
                .execute(callback);

    }

    /**
     * @param key   参数
     * @param value 修改
     * @dw 修改用户信息
     */
    public static void saveInfo(String key, String value, int uid, String token, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.userUpdate")
                .addParams("field", key)
                .addParams("value", value)
                .addParams("uid", String.valueOf(uid))
                .addParams("token", token)
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @dw 获取热门首页轮播
     */
    public static void getIndexHotRollpic(StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getSlide")
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @dw 获取热门首页主播
     */
    public static void getNew(int classposition,int uid,String city, String area, String list1, String list2, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getNew")
                .addParams("city", city)
                .addParams("area", area)
                .addParams("list1", list1)
                .addParams("list2", list2)
                .addParams("num", String.valueOf(classposition))
                .addParams("uid", String.valueOf(uid))
                .tag("getNew")
                .build()
                .execute(callback);
    }

    /**
     * @param uid    当前用户id
     * @param showId 主播id
     * @param token  token
     * @dw 进入直播间初始化信息
     */
    public static void initRoomInfo(int uid, int showId, String token, String address, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.setNodejsInfo")
                .addParams("uid", String.valueOf(uid))
                .addParams("showid", String.valueOf(showId))
                .addParams("token", token)
                .addParams("city", address)
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @删除图片
     *
     * @dw 进入直播间初始化信息
     */
    public static void deleteimg(int uid,StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.deleteimg")
                .addParams("uid", String.valueOf(uid))
                .tag("deleteimg")
                .build()
                .execute(callback);
    }

    /**
     * @删除视频
     *
     * @dw 进入直播间初始化信息
     */
    public static void deletevideo(int uid,StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.deletevideo")
                .addParams("uid", String.valueOf(uid))
                .tag("deletevideo")
                .build()
                .execute(callback);
    }

    /**
     * @param showid 房间号码
     * @dw 获取用户列表
     */
    public static void getRoomUserList(int showid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getRedislist")
                .addParams("size", "0")
                .addParams("showid", String.valueOf(showid))
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param uid   主播id
     * @param title 开始直播标题
     * @param token
     * @dw 开始直播
     */
    public static void createLive(int uid, String stream, String title, StringCallback callback, String token) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.createRoom")
                .addParams("uid", String.valueOf(uid))
                .addParams("title", title)
                .addParams("stream", stream)
                .addParams("city", AppContext.address)
                .addParams("province", AppContext.province)
                .addParams("token", token)
                .tag("phonelive")
                .build()
                .execute(callback);

    }

    /**
     * @param token 用户的token
     * @dw 关闭直播
     */
    public static void closeLive(int id, String token, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.stopRoom")
                .addParams("uid", String.valueOf(id))
                .addParams("token", token)
                .tag("closeLive")
                .build()
                .execute(callback);
    }

    /**
     * @param callback
     * @dw 获取礼物列表
     */
    public static void getGiftList(StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getGifts")
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param g           赠送礼物信息
     * @param mUser       用户信息
     * @param mNowRoomNum 房间号(主播id)
     * @dw 赠送礼物
     */
    public static void sendGift(UserBean mUser, GiftBean g, int mNowRoomNum, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.sendGift")
                .addParams("token", mUser.getToken())
                .addParams("uid", String.valueOf(mUser.getId()))
                .addParams("touid", String.valueOf(mNowRoomNum))
                .addParams("giftid", String.valueOf(g.getId()))
                .addParams("giftcount", "1")
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param content     弹幕信息
     * @param mUser       用户信息
     * @param mNowRoomNum 房间号(主播id)
     * @dw 发送弹幕 HHH
     */
    public static void sendBarrage(UserBean mUser, String content, int mNowRoomNum, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.sendBarrage")
                .addParams("token", mUser.getToken())
                .addParams("uid", String.valueOf(mUser.getId()))
                .addParams("touid", String.valueOf(mNowRoomNum))
                .addParams("content", content)
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param uid   其他用户id
     * @param ucuid 当前用户自己的id
     * @dw 获取其他用户信息
     */
    public static void getUserInfo(int uid, int ucuid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getPopup")
                .addParams("uid", String.valueOf(uid))
                .addParams("ucuid", String.valueOf(ucuid))
                .tag("getUserInfo")
                .build()
                .execute(callback);
    }

    /**
     * @param touid 当前主播id\
     * @param uid   当前用户uid
     * @dw 判断是否关注
     */
    public static void getIsFollow(int uid, int touid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.isAttention")
                .addParams("uid", String.valueOf(uid))
                .addParams("touid", String.valueOf(touid))
                .tag("getIsFollow")
                .build()
                .execute(callback);

    }

    /**
     * @param uid   当前用户id
     * @param touid 关注用户id
     * @dw 关注
     */
    public static void showFollow(int uid, int touid, String token, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.setAttention")
                .addParams("uid", String.valueOf(uid))
                .addParams("showid", String.valueOf(touid))
                .addParams("token", token)
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param uid 查询用户id
     * @dw 获取homepage中的用户信息
     */
    public static void getHomePageUInfo(int uid, int ucuid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getUserHome")
                .addParams("uid", String.valueOf(uid))
                .addParams("ucuid", String.valueOf(ucuid))
                .tag("getHomePageUInfo")
                .build()
                .execute(callback);
    }

    /**
     * @param ucid 自己的id
     * @param uid  查询用户id
     * @dw 获取homepage用户的fans
     */
    public static void getFansList(int uid, int ucid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getFans")
                .addParams("uid", String.valueOf(uid))
                .addParams("ucuid", String.valueOf(ucid))
                .tag("getFansList")
                .build()
                .execute(callback);
    }

    /**
     * @param ucid 自己的id
     * @param uid  查询用户id
     * @dw 获取homepage用户的关注用户列表
     */
    public static void getAttentionList(int uid, int ucid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getAttention")
                .addParams("uid", String.valueOf(uid))
                .addParams("ucuid", String.valueOf(ucid))
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param uid 查询用户id
     * @dw 获取映票排行
     */
    public static void getYpOrder(int uid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getCoinRecord")
                .addParams("uid", String.valueOf(uid))
                .tag("getYpOrder")
                .build()
                .execute(callback);

    }

    /**
     * @param uid   查询用户id
     * @param token token
     * @dw 获取收益信息
     */
    public static void getWithdraw(int uid, String token, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getWithdraw")
                .addParams("uid", String.valueOf(uid))
                .addParams("token", token)
                .tag("getWithdraw")//BBB
                .build()
                .execute(callback);

    }

    /**
     * @dw 获取同城
     */
    public static void samecity(int num ,String city, String lat,String lng,StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.samecity")
                .addParams("lat", lat)
                .addParams("lng", lng)
                .addParams("city", city)
                .addParams("num", String.valueOf(num))
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @dw 获取下载地址
     */
    public static void getConfig(StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getConfig")
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param uid          用户id
     * @param token
     * @param protraitFile 图片文件
     * @dw 更新头像
     */
    public static void updatePortrait(int uid, String token, File protraitFile, StringCallback callback) {
        OkHttpUtils.post()
                .addFile("file", "wp.png", protraitFile)
                .addParams("uid", String.valueOf(uid))
                .addParams("token", token)
                .url(AppConfig.MAIN_URL + "appapi/?service=User.upload")
                .tag("phonelive")
                .build()
                .execute(callback);

    }

    //上传图片
    public static void uploadimg(int uid, String title,String filename, String token, File file, StringCallback callback) {
        OkHttpUtils okHttpUtils = OkHttpUtils.getInstance();
        okHttpUtils.setWriteTimeout(30, TimeUnit.SECONDS);
        okHttpUtils.setReadTimeout(30,TimeUnit.SECONDS);
        okHttpUtils.setConnectTimeout(30, TimeUnit.SECONDS);
        okHttpUtils.post()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.uploadimg")
                .addParams("uid", String.valueOf(uid))
                .addParams("title", title)
                .addParams("token", token)
                .addFile("file", filename, file)
                .tag("uploadimg")
                .build()
                .execute(callback);;
    }
    //上传视频
    public static void uploadvideo(String time,int uid, String title,String filename, String filename1,String token, File file,File file1, StringCallback callback) {
        //client.setConnectTimeout(30, TimeUnit.SECONDS); // connect timeout
        //client.setReadTimeout(30, TimeUnit.SECONDS);
        OkHttpUtils okHttpUtils = OkHttpUtils.getInstance();
        okHttpUtils.setWriteTimeout(1500, TimeUnit.SECONDS);
        okHttpUtils.setReadTimeout(1500,TimeUnit.SECONDS);
        okHttpUtils.setConnectTimeout(1500, TimeUnit.SECONDS);

        okHttpUtils.post()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.uploadvideo")
                .addParams("uid", String.valueOf(uid))
                .addParams("title", title)
                .addParams("time", time)
                .addParams("token", token)

                .addFile("file",filename,file)
                .addFile("file1",filename1,file1)
                .tag("uploadvideo")
                .build()
                .execute(callback);
    }

    /**
     * @param uid   用户id
     * @param token
     * @dw 提现
     */
    public static void requestCash(int uid, String token, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.userCash")
                .addParams("uid", String.valueOf(uid))
                .addParams("token", token)
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param uid 用户id
     * @dw 直播记录
     */
    public static void getLiveRecord(int uid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getLiveRecord")
                .addParams("uid", String.valueOf(uid))
                .tag("getLiveRecord")
                .build()
                .execute(callback);
    }

    /**
     * @param uid 用户id
     * @dw 支付宝下订单
     */
    public static void getAliPayOrderNum(int uid, String token, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getAliOrderId")
                .addParams("uid", String.valueOf(uid))
                .addParams("money", "1")
                .addParams("token", token)
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    //定位
    public static void getAddress(StringCallback callback) {
        OkHttpUtils.get()
                .url("http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json")
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param screenKey 搜索关键词
     * @param uid       用户id
     * @dw 搜索
     */
    public static void search(String screenKey, StringCallback callback, int uid) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.search")
                .addParams("key", screenKey)
                .addParams("uid", String.valueOf(uid))
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @dw 获取地区列表
     */
    public static void getAreaList(StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getArea")
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param sex  性别
     * @param area 地区
     * @dw 地区检索
     */

    public static void selectTermsScreen(int sex, String area, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.searchArea")
                .addParams("sex", String.valueOf(sex))
                .addParams("key", area)
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param uidList 用户id字符串 以逗号分割
     * @dw 批量获取用户信息
     */
    public static void getMultiBaseInfo(int action, int uid, String uidList, StringCallback callback) {

        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getMultiBaseInfo")
                .addParams("uids", uidList)
                .addParams("type", String.valueOf(action))
                .addParams("uid", String.valueOf(uid))
                .tag("phonelive")
                .build()
                .execute(callback);


    }

    /**
     * @param uid 用户id
     * @dw 获取已关注正在直播的用户
     */
    public static void getAttentionLive(int uid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.attentionLive")
                .addParams("uid", String.valueOf(uid))
                .tag("attentionLive")
                .build()
                .execute(callback);
    }

    /**
     * @param
     * @dw 获取校内
     */
    public static void inschool(int num,int uid,String city, String area, String list1, String list2, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.inschool")
                .addParams("city", city)
                .addParams("area", area)
                .addParams("list1", list1)
                .addParams("list2", list2)
                .addParams("num", String.valueOf(num))
                .addParams("uid", String.valueOf(uid))
                .tag("inschool")
                .build()
                .execute(callback);
    }

    /**
     * @param
     * @dw 获取校外
     */
    public static void outschool(int num ,int uid,String city, String area, String list1, String list2, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.outschool")
                .addParams("city", city)
                .addParams("area", area)
                .addParams("list1", list1)
                .addParams("list2", list2)
                .addParams("num", String.valueOf(num))
                .addParams("uid", String.valueOf(uid))
                .tag("outschool")
                .build()
                .execute(callback);
    }

    /**
     * @param uid   当前用户id
     * @param ucuid to uid
     * @dw 获取用户信息私聊专用
     */

    public static void getPmUserInfo(int uid, int ucuid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getPmUserInfo")
                .addParams("uid", String.valueOf(uid))
                .addParams("ucuid", String.valueOf(ucuid))
                .tag("getPmUserInfo")
                .build()
                .execute(callback);

    }

    /**
     * @param uid   用户id
     * @param price 价格
     * @dw 微信支付
     */
    public static void wxPay(int uid, String token, String price, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getWxOrderId")
                .addParams("uid", String.valueOf(uid))
                .addParams("token", token)
                .addParams("money", price)
                .build()
                .execute(callback);

    }

    /**
     * @param platDB 用户信息
     * @param type   平台
     * @dw 第三方登录
     */
    public static void otherLogin(String type, PlatformDb platDB, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.userLoginByThird")
                .addParams("openid", platDB.getUserId())
                .addParams("nicename", platDB.getUserName())
                .addParams("type", type)
                .addParams("avatar", platDB.getUserIcon())
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param roomnum 房间号码
     * @param touid   操作id
     * @param token   用户登录token
     * @dw 设为管理员
     */
    public static void setManage(int roomnum, int touid, String token, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.setAdmin")
                .addParams("showid", String.valueOf(roomnum))
                .addParams("uid", String.valueOf(touid))
                .addParams("token", token)
                .tag("phonelive")
                .build()
                .execute(callback);

    }

    /**
     * @param uid    用户id
     * @param showid 房间号码
     * @dw 判断是否为管理员
     */

    public static void isManage(int showid, int uid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getIsAdmin")
                .addParams("showid", String.valueOf(showid))
                .addParams("uid", String.valueOf(uid))
                .tag("phonelive")
                .build()
                .execute(callback);

    }

    /**
     * @param showid 房间id
     * @param touid  被禁言用户id
     * @param token  用户登录token
     * @dw 禁言
     */
    public static void setShutUp(int showid, int touid, int uid, String token, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.setShutUp")
                .addParams("showid", String.valueOf(showid))
                .addParams("touid", String.valueOf(touid))
                .addParams("uid", String.valueOf(uid))
                .addParams("token", token)
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    //是否禁言解除
    public static void isShutUp(int uid, int showid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.isShutUp")
                .addParams("showid", String.valueOf(showid))
                .addParams("uid", String.valueOf(uid))
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    //token是否过期
    public static void tokenIsOutTime(int uid, String token, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.iftoken")
                .addParams("uid", String.valueOf(uid))
                .addParams("token", token)
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @dw 拉黑
     */
    public static void pullTheBlack(int uid, int touid, String token, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.setBlackList")
                .addParams("uid", String.valueOf(uid))
                .addParams("showid", String.valueOf(touid))
                .addParams("token", token)
                .tag("setBlackList")
                .build()
                .execute(callback);

    }

    /**
     * @dw 黑名单列表
     */
    public static void getBlackList(int uid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getBlackList")
                .addParams("uid", String.valueOf(uid))
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    public static void getMessageCode(String phoneNum) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getCode")
                .addParams("mobile", phoneNum)
                .tag("phonelive")
                .build()
                .execute(null);
    }

    /**
     * @param uid 用户id
     * @dw 获取用户余额
     */
    public static void getUserDiamondsNum(int uid, String token, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getUserPrivateInfo")
                .addParams("uid", String.valueOf(uid))
                .addParams("token", token)
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param uid    用户id
     * @param token  用户登录token
     * @param showid 房间号
     * @dw 点亮
     */
    public static void showLit(int uid, String token, int showid) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.setLight")
                .addParams("uid", String.valueOf(uid))
                .addParams("token", token)
                .addParams("showid", String.valueOf(showid))
                .tag("phonelive")
                .build()
                .execute(null);
    }

    /**
     * @param keyword 歌曲关键词
     * @dw 百度接口搜索音乐
     */
    public static void searchMusic(String keyword, StringCallback callback) {
        //baidu music interface (http://tingapi.ting.baidu.com/v1/restserver/ting)
        OkHttpUtils.get()
                .url("http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.search.catalogSug&query=" + keyword)
                .tag("phonelive")
                .build()
                .execute(callback);

    }

    /**
     * @param songid 歌曲id
     * @dw 获取music信息
     */
    public static void getMusicFileUrl(String songid, StringCallback callback) {
        OkHttpUtils.get()
                .url("http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.downWeb&songid=" + songid + "&bit=24&_t=" + System.currentTimeMillis())
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param musicUrl 下载地址
     * @dw 下载音乐文件
     */
    public static void downloadMusic(String musicUrl, FileCallBack fileCallBack) {
        OkHttpUtils.get()
                .url(musicUrl)
                .tag("phonelive")
                .build()
                .execute(fileCallBack);
    }

    /**
     * @dw 开播等级限制
     */
    public static void getLevelLimit(int uid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getLevelLimit")
                .addParams("uid", String.valueOf(uid))
                .tag("phonelive")
                .build()
                .execute(callback);
    }
    /**
    * @dw 主播判断
    */
    public static void checkcreateroom(int uid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.checkcreateroom")
                .addParams("uid", String.valueOf(uid))
                .tag("checkcreateroom")
                .build()
                .execute(callback);
    }

    /**
     * @dw 检查新版本
     */
    public static void checkUpdate(StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getVersion")
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    public static void downloadLrc(String musicLrc, FileCallBack fileCallBack) {
        OkHttpUtils.get()
                .url(musicLrc)
                .tag("phonelive")
                .build()
                .execute(fileCallBack);
    }

    /**
     * @param apkUrl 下载地址
     * @dw 下载最新apk
     */
    public static void getNewVersionApk(String apkUrl, FileCallBack fileCallBack) {
        OkHttpUtils.get()
                .url(apkUrl)
                .tag("phonelive")
                .build()
                .execute(fileCallBack);
    }

    /**
     * @param uid 用户id
     * @dw 获取管理员列表
     */
    public static void getManageList(int uid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getAdminList")
                .addParams("uid", String.valueOf(uid))
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param mRoomNum 房间号码
     * @param size     当前人数
     * @dw 获取更多用户列表
     */
    public static void loadMoreUserList(int size, int mRoomNum, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getRedislist")
                .addParams("size", String.valueOf(size))
                .addParams("showid", String.valueOf(mRoomNum))
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    /**
     * @param uid 用户id
     * @dw 判断是否是第一次充值
     */

    public static void getCharge(int uid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getCharge")
                .addParams("uid", String.valueOf(uid))
                .tag("phonelive")
                .build()
                .execute(callback);
    }

    //举报
    public static void report(int uid, int touid) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.setReport")
                .addParams("uid", String.valueOf(uid))
                .addParams("touid", String.valueOf(touid))
                .addParams("content", "涉嫌传播淫秽色情信息")
                .tag("report")
                .build()
                .execute(null);

    }

    public static void getVideoCode(String url, StringCallback callback) {
        OkHttpUtils.get()
                .url(url)
                .tag("getVideoCode")
                .build()
                .execute(callback);
    }

    //获取直播记录
    public static void getLiveRecordById(String showid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getAliCdnRecord")
                .addParams("id", showid)
                .tag("getLiveRecordById")
                .build()
                .execute(callback);
    }

    //获取直播记录次数
    public static void videohit(int id, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.videohit")
                .addParams("id", String.valueOf(id))
                .tag("videohit")
                .build()
                .execute(callback);
    }
    //获取录播记录次数
    public static void livehit(int id, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.livehit")
                .addParams("id", String.valueOf(id))
                .tag("livehit")
                .build()
                .execute(callback);
    }

    //获取区域
    public static void getquyu(String city, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getquyu")
                .addParams("city", city)
                .tag("getquyu")
                .build()
                .execute(callback);
    }

    //获取城市
    public static void getcity(StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getcity")
                .tag("getcity")
                .build()
                .execute(callback);
    }

    //获取q全部
    public static void getmainlist(StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getmainlist")
                .tag("getmainlist")
                .build()
                .execute(callback);
    }

    //获取最近城市
    public static void getevercity(String uid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getevercity")
                .addParams("uid", uid)
                .tag("getevercity")
                .build()
                .execute(callback);
    }
    //获取最近城市
    public static void imglist(int uid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.imglist")
                .addParams("uid", String.valueOf(uid))
                .tag("imglist")
                .build()
                .execute(callback);
    }

    public static void videolist(int uid, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.videolist")
                .addParams("uid", String.valueOf(uid))
                .tag("videolist")
                .build()
                .execute(callback);
    }


    public static void getkeywords(String keywords, StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service", "User.getkeywords")
                .addParams("keywords", keywords)
                .tag("getkeywords")
                .build()
                .execute(callback);
    }

    //修改直播状态
    public static void changeLiveState(String uid,String token,String stream,String status,StringCallback callback){
        OkHttpUtils.post()
                .url(AppConfig.MAIN_URL)
                .addParams("service","User.changeLive")
                .addParams("uid",uid)
                .addParams("token",token)
                .addParams("stream",stream)
                .addParams("status",status)
                .build()
                .execute(callback);
    }

    //获取上传文件大小限制
    public static void getVideoUploadMaxSize(StringCallback callback) {
        OkHttpUtils.get()
                .url(AppConfig.MAIN_URL)
                .addParams("service","User.maxUpload")
                .tag("getVideoUploadMaxSize")
                .build()
                .execute(callback);
    }
}
