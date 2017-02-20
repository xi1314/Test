package com.ruziniu.phonelive.ui.other;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;

import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.bean.ChatBean;
import com.ruziniu.phonelive.bean.SendGiftBean;
import com.ruziniu.phonelive.bean.SendSocketMessageBean;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.interf.ChatServerInterface;
import com.ruziniu.phonelive.ui.DrawableRes;
import com.ruziniu.phonelive.utils.StringUtils;
import com.ruziniu.phonelive.utils.TDevice;
import com.ruziniu.phonelive.utils.TLog;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.Call;

/**
 * 直播间业务逻辑处理
 */
public class ChatServer {
    public static final int[] heartImg = new int[]{R.drawable.plane_heart_cyan, R.drawable.plane_heart_pink, R.drawable.plane_heart_red, R.drawable.plane_heart_yellow};
    private static final String EVENT_NAME = "broadcast";
    private static final int SEND_CHAT = 2;
    private static final int SYSTEM_NOT = 1;
    private static final int NOTICE = 0;
    private static final int PRIVELEGE = 4;
    public static int LIVE_USER_NUMS;
    private Socket mSocket;
    private Context context;
    private int showid;
    private ChatServerInterface mChatServer;
    private Gson mGson;
    //服务器连接关闭监听
    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            TLog.log("socket断开连接");
        }
    };
    //服务器连接失败监听
    private Emitter.Listener onError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            mChatServer.onError();
            TLog.log("socket连接Error");
        }
    };
    //服务器消息监听
    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            try {
                String res = args[0].toString();
                if (res.equals("stopplay")) {
                    mChatServer.onSystemNot(1);
                    return;
                }
                JSONObject resJson = new JSONObject(res);
                JSONArray msgArrayJson = resJson.getJSONArray("msg");
                JSONObject contentJson = msgArrayJson.getJSONObject(0);
                int msgType = contentJson.getInt("msgtype");
                String method = contentJson.getString("_method_");
                int action = contentJson.getInt("action");

                //获取用户动作
                switch (msgType) {
                    case SEND_CHAT://聊天
                        if (action == 0) {//公聊
                            onMessage(res, contentJson);
                        }
                        break;
                    case SYSTEM_NOT://系统
                        if (action == 0) { // sendgift
                            onSendGift(contentJson);
                        } else if (action == 18) {//close room
                            //房间关闭
                            mChatServer.onSystemNot(0);
                        } else if (action == 13) {
                            //系统消息
                            onSystemMessage(contentJson);

                        }else if(action == 7){
                            //弹幕
                            onDanmuMessage(contentJson, method);
                        }
                        break;
                    case NOTICE://通知
                        if (action == 0) {//上下线
                            JSONObject uInfo = contentJson.getJSONObject("ct");
                            ChatServer.LIVE_USER_NUMS += 1;
                            mChatServer.onUserStateChange(mGson.fromJson(uInfo.toString(), UserBean.class), true);
                        } else if (action == 1) {
                            JSONObject uInfo = contentJson.getJSONObject("ct");
                            ChatServer.LIVE_USER_NUMS -= 1;
                            mChatServer.onUserStateChange(mGson.fromJson(uInfo.toString(), UserBean.class), false);
                        } else if (action == 2) {//点亮
                            mChatServer.onLit();
                        } else if (action == 3) {//僵尸粉丝推送
                            mChatServer.onAddZombieFans(contentJson.getString("ct"));
                        }
                        break;
                    case PRIVELEGE:
                        onPrivate(contentJson);
                        break;
                }



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //特权平陵
    private void onPrivate(JSONObject contentJson) throws JSONException {
        SpannableStringBuilder msg = new SpannableStringBuilder(contentJson.getString("ct"));
        SpannableStringBuilder name = new SpannableStringBuilder("系统消息:");
        name.setSpan(new ForegroundColorSpan(Color.rgb(215, 126, 23)), 0, name.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        msg.setSpan(new ForegroundColorSpan(Color.rgb(109, 198, 232)), 0, msg.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ChatBean c = new ChatBean();
        c.setType(13);
        c.setSendChatMsg(msg);
        c.setUserNick(name);
        mChatServer.onPrivilegeAction(c, contentJson);
    }

    private void onDanmuMessage(JSONObject contentJson, String method) throws JSONException {
        String ct = contentJson.getString("ct");
        ChatBean c = new ChatBean();
        c.setId(contentJson.getInt("uid"));
        c.setSignature(contentJson.getString("usign"));
        c.setLevel(contentJson.getInt("level"));
        c.setUser_nicename(contentJson.getString("uname"));
        c.setAvatar(contentJson.getString("uhead"));
        c.setMethod(method);

        JSONObject jsonObject = new JSONObject(ct);

        c.setContent(jsonObject.getString("content"));
        mChatServer.onMessageListen(1,c);
    }

    //系统消息
    private void onSystemMessage(JSONObject contentJson) throws JSONException {
        SpannableStringBuilder msg = new SpannableStringBuilder(contentJson.getString("ct"));
        SpannableStringBuilder name = new SpannableStringBuilder("系统消息:");
        name.setSpan(new ForegroundColorSpan(Color.rgb(215, 126, 23)), 0, name.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ChatBean c = new ChatBean();
        c.setType(13);
        c.setSendChatMsg(msg);
        c.setUserNick(name);
        mChatServer.setManage(contentJson, c);
    }

    //礼物信息
    private void onSendGift(JSONObject contentJson) throws JSONException {
        ChatBean c = new ChatBean();
        c.setId(contentJson.getInt("uid"));
        c.setSignature(contentJson.getString("usign"));
        c.setLevel(contentJson.getInt("level"));
        c.setUser_nicename(contentJson.getString("uname"));
        c.setCity(contentJson.getString("city"));
        c.setSex(contentJson.getInt("sex"));
        c.setAvatar(contentJson.getString("uhead"));


        contentJson.getJSONObject("ct").put("evensend", contentJson.getString("evensend"));

        SendGiftBean mSendGiftInfo = mGson.fromJson(contentJson.getJSONObject("ct").toString(), SendGiftBean.class);//gift info

        int level = c.getLevel();
        String uname = "_ " + c.getUser_nicename() + ":";
        SpannableStringBuilder msg = new SpannableStringBuilder("我送了" + mSendGiftInfo.getGiftcount() + "个" + mSendGiftInfo.getGiftname());
        SpannableStringBuilder name = new SpannableStringBuilder(uname);

        Drawable d = context.getResources().getDrawable(DrawableRes.LevelImg[(level != 0 ? level - 1 : 0)]);
        d.setBounds(0, 0, (int) TDevice.dpToPixel(30), (int) TDevice.dpToPixel(15));
        ImageSpan is = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
        name.setSpan(new ForegroundColorSpan(Color.rgb(215, 126, 23)), 1, name.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        name.setSpan(is, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        msg.setSpan(new ForegroundColorSpan(Color.rgb(232, 109, 130)), 0, msg.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        c.setSendChatMsg(msg);
        c.setUserNick(name);


        mChatServer.onShowSendGift(mSendGiftInfo, c);
    }

    //消息信息
    private void onMessage(String res, JSONObject contentJson) throws JSONException {
        String ct = contentJson.getString("ct");

        ChatBean c = new ChatBean();
        c.setId(contentJson.getInt("uid"));
        c.setSignature(contentJson.getString("usign"));
        c.setLevel(contentJson.getInt("level"));
        c.setUser_nicename(contentJson.getString("uname"));
        c.setCity(contentJson.getString("city"));
        c.setSex(contentJson.getInt("sex"));
        c.setAvatar(contentJson.getString("uhead"));


        int level = c.getLevel();
        String uname = "_ " + c.getUser_nicename() + ":";

        SpannableStringBuilder msg = new SpannableStringBuilder(ct);
        msg.setSpan(new ForegroundColorSpan(Color.argb(255,255,255,255)), 0, msg.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableStringBuilder name = new SpannableStringBuilder(uname);
        //添加等级图文混合
        Drawable levelDrawable = context.getResources().getDrawable(DrawableRes.LevelImg[(level != 0 ? level - 1 : 0)]);
        levelDrawable.setBounds(0, 0, (int) TDevice.dpToPixel(30), (int) TDevice.dpToPixel(15));
        ImageSpan levelImage = new ImageSpan(levelDrawable, ImageSpan.ALIGN_BASELINE);
        name.setSpan(new ForegroundColorSpan(Color.rgb(215, 126, 23)), 1, name.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        name.setSpan(levelImage, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //获取被@用户id
        String touid = contentJson.getString("touid");
        //判断如果是@方式聊天,被@方用户显示粉色字体
        if ((!touid.equals("0") && (Integer.parseInt(touid) == AppContext.getInstance().getLoginUid()
             /* || c.getId() == AppContext.getInstance().getLoginUid()*/))) {
            msg.setSpan(new ForegroundColorSpan(Color.rgb(232, 109, 130)), 0, msg.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        //判断是否是点亮
        if (res.indexOf("heart") > 0) {
            //int[] heartImg = new int[]{Color.rgb(30,190,204),Color.rgb(241,96,246),Color.rgb(218,53,42),Color.rgb(205,203,49)};
            int index = contentJson.getInt("heart");
            msg.append("❤");
            //添加点亮图文混合
            Drawable hearDrawable = context.getResources().getDrawable(heartImg[index]);
            hearDrawable.setBounds(0,0, (int) TDevice.dpToPixel(10), (int) TDevice.dpToPixel(10));
            ImageSpan hearImage = new ImageSpan(hearDrawable, ImageSpan.ALIGN_BASELINE);
            msg.setSpan(hearImage, msg.length() - 1, msg.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        c.setSendChatMsg(msg);
        c.setUserNick(name);


        mChatServer.onMessageListen(2,c);
    }


    //服务器连接结果监听
    private Emitter.Listener onConn = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (args[0].toString().equals("ok")) {
                mChatServer.onConnect(true);
                PhoneLiveApi.getRoomUserList(showid, callback);
            } else {
                mChatServer.onConnect(false);
            }

        }
    };

    public ChatServer(ChatServerInterface chatServerInterface, Context context, int showid) throws URISyntaxException {
        this.mChatServer = chatServerInterface;
        this.context = context;
        this.showid = showid;
        mGson = new Gson();
        mSocket = AppContext.getInstance().getSocket();
    }

    /**
     * @param millis  要转化的日期毫秒数。
     * @param pattern 要转化为的字符串格式（如：yyyy-MM-dd HH:mm:ss）。
     * @return 返回日期字符串。
     */
    public static String millisToStringDate(long millis, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern,
                Locale.getDefault());
        return format.format(new Date(millis));
    }

    /**
     * @param res 用户信息json格式
     * @dw 连接socket服务端
     */
    public void connectSocketServer(String res, String token, int mNowRoomNum) {
        try {
            JSONObject resJson = new JSONObject(res);

            if (null != mSocket) {
                mSocket.connect();
                JSONObject dataJson = new JSONObject();
                dataJson.put("uid", resJson.getString("id"));
                dataJson.put("token", token);
                dataJson.put("roomnum", mNowRoomNum);
                mSocket.emit("conn", dataJson);
                mSocket.on("conn", onConn);
                mSocket.on("broadcastingListen", onMessage);
                mSocket.on(mSocket.EVENT_DISCONNECT, onDisconnect);
                mSocket.on(mSocket.EVENT_ERROR, onError);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param handler 定时发送心跳包,在连接成功后调用
     */
    public void heartbeat(final Handler handler) {
        if(handler != null){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //TLog.log("心跳包发送....");
                    if (mSocket == null) return;
                    //TLog.log("心跳包发送....");
                    mSocket.emit("heartbeat", "heartbeat");
                    handler.postDelayed(this, 4000);
                }
            }, 4000);

        }
    }

    /**
     * @param u           用户基本信息
     * @param mNowRoomNum 房间号码(主播id)
     * @dw 主播连接socket服务端
     */
    public void connectSocketServer(UserBean u, int mNowRoomNum) {
        if (null == mSocket) return;
        try {
            mSocket.connect();
            JSONObject dataJson = new JSONObject();
            dataJson.put("uid", u.getId());
            dataJson.put("token", u.getToken());
            dataJson.put("roomnum", u.getId());
            mSocket.emit("conn", dataJson);
            mSocket.on("conn", onConn);
            mSocket.on("broadcastingListen", onMessage);
            mSocket.on(mSocket.EVENT_DISCONNECT, onDisconnect);
            mSocket.on(mSocket.EVENT_ERROR, onError);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //关闭房间
    public void closeLive() {
        if (null == mSocket) {
            return;
        }
        SendSocketMessageBean socketMessageBean = new SendSocketMessageBean();
        List<SendSocketMessageBean.MsgBean> msgBeanList = new ArrayList<>();
        SendSocketMessageBean.MsgBean msgBean = getMsgBean("StartEndLive",context.getString(R.string.livestart),"18","1",0,null);
        msgBean.setTougood("");
        msgBean.setTouid(0);
        msgBean.setTouname("");
        msgBean.setUgood("");
        msgBeanList.add(msgBean);
        socketMessageBean.setMsg(msgBeanList);
        socketMessageBean.setRetcode("000000");
        socketMessageBean.setRetmsg("ok");
        String json = mGson.toJson(socketMessageBean);
        try {
            mSocket.emit(EVENT_NAME, new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param mUser    用户信息
     * @param evensend 是否在连送规定时间内
     * @dw token 发送礼物凭证
     */
    public void doSendGift(String token, UserBean mUser, String evensend) {
        if (null != mSocket) {
            SendSocketMessageBean socketMessageBean = new SendSocketMessageBean();
            List<SendSocketMessageBean.MsgBean> msgBeanList = new ArrayList<>();
            SendSocketMessageBean.MsgBean msgBean = getMsgBean("SendGift",token,"0","1",0,mUser);
            msgBean.setTougood("");
            msgBean.setTouid(0);
            msgBean.setTouname("");
            msgBean.setUgood("");
            msgBean.setEvensend(evensend);
            msgBeanList.add(msgBean);
            socketMessageBean.setMsg(msgBeanList);
            socketMessageBean.setRetcode("000000");
            socketMessageBean.setRetmsg("ok");
            String json = mGson.toJson(socketMessageBean);
            try {
                mSocket.emit(EVENT_NAME, new JSONObject(json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * @param mUser 用户信息
     * @dw token 发送弹幕凭证
     */
    public void doSendBarrage(String token, UserBean mUser) {
        if (null != mSocket) {

            SendSocketMessageBean socketMessageBean = new SendSocketMessageBean();
            List<SendSocketMessageBean.MsgBean> msgBeanList = new ArrayList<>();
            SendSocketMessageBean.MsgBean msgBean = getMsgBean("SendBarrage",token,"7","1",0,mUser);
            msgBean.setTougood("");
            msgBean.setTouid(0);
            msgBean.setTouname("");
            msgBean.setUgood("");
            msgBeanList.add(msgBean);
            socketMessageBean.setMsg(msgBeanList);
            socketMessageBean.setRetcode("000000");
            socketMessageBean.setRetmsg("ok");
            String json = mGson.toJson(socketMessageBean);
            try {
                mSocket.emit(EVENT_NAME, new JSONObject(json));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * @param mUser   当前用户bean
     * @param mToUser 被操作用户bean
     * @dw 禁言
     */
    public void doSetShutUp(UserBean mUser, UserBean mToUser) {
        if (null == mSocket) {
            return;
        }
        JSONObject msgJson = new JSONObject();
        JSONObject msgJson2 = new JSONObject();
        JSONArray msgArrJson = new JSONArray();
        try {
            msgJson2.put("_method_", "ShutUpUser");
            msgJson2.put("action", "1");
            msgJson2.put("ct", mToUser.getUser_nicename() + "被禁言");
            msgJson2.put("msgtype", "4");
            msgJson2.put("uid", mUser.getId());
            msgJson2.put("uname", mUser.getUser_nicename());
            msgJson2.put("touid", mToUser.getId());
            msgJson2.put("touname", mToUser.getUser_nicename());
            msgArrJson.put(0, msgJson2);

            msgJson.put("msg", msgArrJson);
            msgJson.put("retcode", "000000");
            msgJson.put("retmsg", "ok");
            mSocket.emit(EVENT_NAME, msgJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    //设为管理员
    public void doSetOrRemoveManage(UserBean user, UserBean touser, String content) {
        if (null == mSocket) {
            return;
        }
        SendSocketMessageBean socketMessageBean = new SendSocketMessageBean();
        List<SendSocketMessageBean.MsgBean> msgBeanList = new ArrayList<>();
        SendSocketMessageBean.MsgBean msgBean = getMsgBean("SystemNot",content,"13","4",0,user);
        msgBean.setTougood("");
        msgBean.setTouid(touser.getId());
        msgBean.setTouname(touser.getUser_nicename());
        msgBeanList.add(msgBean);
        socketMessageBean.setMsg(msgBeanList);
        socketMessageBean.setRetcode("000000");
        socketMessageBean.setRetmsg("ok");
        String json = mGson.toJson(socketMessageBean);
        try {
            mSocket.emit(EVENT_NAME, new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    //发送系统消息
    public void doSendSystemMessage(String msg,UserBean user){
        if (null == mSocket) {
            return;
        }
        SendSocketMessageBean socketMessageBean = new SendSocketMessageBean();
        List<SendSocketMessageBean.MsgBean> msgBeanList = new ArrayList<>();
        SendSocketMessageBean.MsgBean msgBean = getMsgBean("SystemNot",msg,"13","4",0,user);
        msgBean.setTougood("");
        msgBean.setTouid(0);
        msgBean.setTouname("");
        msgBeanList.add(msgBean);
        socketMessageBean.setMsg(msgBeanList);
        socketMessageBean.setRetcode("000000");
        socketMessageBean.setRetmsg("ok");
        String json = mGson.toJson(socketMessageBean);
        try {
            mSocket.emit(EVENT_NAME, new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param sendMsg 发言内容
     * @param user    用户信息
     * @dw 发言
     */
    public void doSendMsg(String sendMsg, UserBean user, int reply) {
        if (null == mSocket) {
            return;
        }
        SendSocketMessageBean socketMessageBean = new SendSocketMessageBean();
        List<SendSocketMessageBean.MsgBean> msgBeanList = new ArrayList<>();
        SendSocketMessageBean.MsgBean msgBean = getMsgBean("SendMsg",sendMsg,"0","2",reply,user);
        msgBean.setTougood("");
        msgBean.setTouid(reply);
        msgBean.setTouname("");
        msgBean.setUgood("");
        msgBeanList.add(msgBean);
        socketMessageBean.setMsg(msgBeanList);
        socketMessageBean.setRetcode("000000");
        socketMessageBean.setRetmsg("ok");
        String json = mGson.toJson(socketMessageBean);
        try {
            mSocket.emit(EVENT_NAME, new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param index
     * @param user  用户信息
     * @dw 我点亮了
     */
    public void doSendLitMsg(UserBean user, int index) {
        if (null == mSocket) {
            return;
        }
        JSONObject msgJson = new JSONObject();
        JSONObject msgJson2 = new JSONObject();
        JSONArray msgArrJson = new JSONArray();
        try {
            //json对象msgjson数组下数组下标为0的json对象
            msgJson2.put("_method_", "SendMsg");
            msgJson2.put("action", "0");
            msgJson2.put("ct", "我点亮了");
            msgJson2.put("msgtype", "2");
            msgJson2.put("timestamp", millisToStringDate(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
            msgJson2.put("tougood", "");
            msgJson2.put("touname", "");
            msgJson2.put("heart", index + 1);
            msgJson2.put("touid", 0);
            msgJson2.put("ugood", "");
            msgJson2.put("city", AppContext.address);
            msgJson2.put("level", user.getLevel());
            msgJson2.put("uid", user.getId());
            msgJson2.put("sex", user.getSex());
            msgJson2.put("uname", user.getUser_nicename());
            msgJson2.put("uhead", user.getAvatar());
            msgJson2.put("usign", user.getSignature());
            msgArrJson.put(0, msgJson2);

            msgJson.put("msg", msgArrJson);
            msgJson.put("retcode", "000000");
            msgJson.put("retmsg", "ok");

            mSocket.emit(EVENT_NAME, msgJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    //获取发送消息bean对象
    private SendSocketMessageBean.MsgBean getMsgBean(String method,String ct,String action,String msgType,int reply,UserBean mUser){
        SendSocketMessageBean.MsgBean msgBean = new SendSocketMessageBean.MsgBean();
        msgBean.set_method_(method);
        msgBean.setAction(action);
        msgBean.setCt(ct);
        msgBean.setMsgtype(msgType);
        msgBean.setTimestamp(millisToStringDate(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
        msgBean.setCity(AppContext.address);
        if(mUser != null){
            msgBean.setUgood(String.valueOf(mUser.getId()));
            msgBean.setLevel(mUser.getLevel());
            msgBean.setUid(mUser.getId());
            msgBean.setSex(mUser.getSex());
            msgBean.setUname(mUser.getUser_nicename());
            msgBean.setUhead(mUser.getAvatar());
            msgBean.setUsign(mUser.getSignature());
        }

        return msgBean;
    }
    //获取僵尸粉丝
    public void getZombieFans() {
        if (null == mSocket) {
            return;
        }
        SendSocketMessageBean socketMessageBean = new SendSocketMessageBean();
        List<SendSocketMessageBean.MsgBean> msgBeanList = new ArrayList<>();
        SendSocketMessageBean.MsgBean msgBean = getMsgBean("requestFans","","","",0,null);
        msgBean.setTougood("");
        msgBean.setTouid(0);
        msgBean.setTouname("");
        msgBeanList.add(msgBean);
        socketMessageBean.setMsg(msgBeanList);
        socketMessageBean.setRetcode("000000");
        socketMessageBean.setRetmsg("ok");
        String json = mGson.toJson(socketMessageBean);
        try {
            mSocket.emit(EVENT_NAME, new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param index 点亮心在数组中的下标
     * @dw 点亮
     */
    public void doSendLit(int index) {
        if (null == mSocket) {
            return;
        }
        JSONObject msgJson = new JSONObject();
        JSONObject msgJson2 = new JSONObject();
        JSONArray msgArrJson = new JSONArray();
        try {
            //json对象msgjson数组下数组下标为0的json对象
            msgJson2.put("_method_", "light");
            msgJson2.put("action", "2");
            msgJson2.put("msgtype", "0");
            msgJson2.put("timestamp", millisToStringDate(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
            msgJson2.put("tougood", "");
            msgJson2.put("touname", "");
            msgJson2.put("ugood", "");
            msgArrJson.put(0, msgJson2);

            msgJson.put("msg", msgArrJson);
            msgJson.put("retcode", "000000");
            msgJson.put("retmsg", "ok");
            mSocket.emit(EVENT_NAME, msgJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //释放资源
    public void close() {
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off("conn");
            mSocket.off("broadcastingListen");
            mSocket.close();
            mSocket = null;
        }

    }

    //用户列表回调
    private StringCallback callback = new StringCallback() {
        @Override
        public void onError(Call call, Exception e) {
            //AppContext.showToast("获取观众列表失败");
        }

        @Override
        public void onResponse(String s) {

            String res = ApiUtils.checkIsSuccess(s);
            if (res != null) {
                try {
                    JSONObject infoJsonObj = new JSONObject(res);
                    JSONArray uListJsonArray = infoJsonObj.getJSONArray("list");//观众列表
                    String votes = infoJsonObj.getString("votestotal");//票
                    List<UserBean> uList = new ArrayList<>();
                    for (int i = 0; i < uListJsonArray.length(); i++) {
                        UserBean u = mGson.fromJson(uListJsonArray.getString(i), UserBean.class);
                        uList.add(u);
                    }
                    ChatServer.LIVE_USER_NUMS = StringUtils.toInt(infoJsonObj.getString("nums"), 0);
                    mChatServer.onUserList(uList, votes);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    };
}
