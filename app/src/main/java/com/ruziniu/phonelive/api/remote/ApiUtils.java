package com.ruziniu.phonelive.api.remote;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.AppManager;
import com.ruziniu.phonelive.ui.LiveLoginSelectActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ApiUtils {
    public final static int SUCCESS_CODE = 200;//成功请求到服务端
    public final static String TOKEN_TIMEOUT = "700";

    public static String checkIsSuccess(String res) {
        try {
            JSONObject resJson = new JSONObject(res);

            if (Integer.parseInt(resJson.getString("ret")) == SUCCESS_CODE) {
                JSONObject dataJson = resJson.getJSONObject("data");
                String code = dataJson.getString("code");
                if (code.equals(TOKEN_TIMEOUT)) {
                    AppManager.getAppManager().finishAllActivity();
                    Intent intent = new Intent(AppContext.getInstance(), LiveLoginSelectActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    AppContext.getInstance().startActivity(intent);
                    return null;
                } else if (!code.equals("0")) {
                    Toast.makeText(AppContext.getInstance(), dataJson.get("msg").toString(), Toast.LENGTH_SHORT).show();
                    return null;
                } else {
                    return dataJson.get("info").toString();
                }
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static boolean checkIsSuccess2(String res) {
        try {
            JSONObject resJson = new JSONObject(res);
            if (Integer.parseInt(resJson.getString("ret")) == SUCCESS_CODE) {
                JSONObject dataJson = resJson.getJSONObject("data");
                String code = dataJson.getString("code");
                if (code.equals(TOKEN_TIMEOUT)) {
                    AppManager.getAppManager().finishAllActivity();
                    Intent intent = new Intent(AppContext.getInstance(), LiveLoginSelectActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    AppContext.getInstance().startActivity(intent);
                } else if (code.equals("0")) {
                    return true;
                }else{
                    Toast.makeText(AppContext.getInstance(), dataJson.get("msg").toString(), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static JSONArray arrayCheckIsSuccess(String res) {
        try {
            JSONObject resJson = new JSONObject(res);

            if (Integer.parseInt(resJson.getString("ret")) == SUCCESS_CODE) {
                JSONObject dataJson = resJson.getJSONObject("data");
                String code = dataJson.getString("code");
                if (code.equals(TOKEN_TIMEOUT)) {
                    AppManager.getAppManager().finishAllActivity();
                    Intent intent = new Intent(AppContext.getInstance(), LiveLoginSelectActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    AppContext.getInstance().startActivity(intent);
                    return null;
                } else if (code.equals("0")) {
                    return dataJson.getJSONArray("info");
                } else {
                    //return dataJson.get("info").toString();
                    Toast.makeText(AppContext.getInstance(), dataJson.get("msg").toString(), Toast.LENGTH_SHORT).show();
                    return null;
                }
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }
}
