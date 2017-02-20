package com.ruziniu.phonelive.ui.other;

import android.content.Context;

import com.ksyun.media.streamer.kit.KSYStreamer;


/**
 * Created by weipeng on 16/9/8.
 */
public class LiveStream extends KSYStreamer {
    private int musicVolue;
    private int mvoice ;
    public LiveStream(Context context) {
        super(context);
    }

    public int getMusicVolue() {
        return musicVolue;
    }

    public void setMusicVolue(int musicVolue) {
        this.musicVolue = musicVolue;
    }

    public int getMvoice() {
        return mvoice;
    }

    public void setMvoice(int mvoice) {
        this.mvoice = mvoice;
    }
}
