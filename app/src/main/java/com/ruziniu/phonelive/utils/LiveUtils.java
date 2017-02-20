package com.ruziniu.phonelive.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by weipeng on 16/8/16.
 */
public class LiveUtils {
    /**
     * @dw 获取歌词字符串
     * @param fileName 歌词文件目录
     * */
    public static String getFromFile(String fileName){
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            InputStreamReader inputReader = new InputStreamReader(fileInputStream);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String Result="";
            while((line = bufReader.readLine()) != null){
                if(line.trim().equals(""))
                    continue;
                Result += line + "\r\n";
            }
            fileInputStream.close();
            inputReader.close();
            bufReader.close();
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Subscription startInterval(final Runnable runnable){
        Subscription subscription = Observable.interval(1, 6 * 10000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        TLog.log("" + aLong);
                        runnable.run();
                    }
                });
        return subscription;
    }


}
