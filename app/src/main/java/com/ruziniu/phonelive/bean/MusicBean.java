package com.ruziniu.phonelive.bean;

/**
 * Created by Administrator on 2016/5/4.
 */
public class MusicBean {

    /**
     * bitrate_fee : {"0":"0|0","1":"0|0"}
     * yyr_artist : 0
     * songname : 海阔天空
     * artistname : 信乐团
     * control : 0000000000
     * songid : 391713
     * has_mv : 0
     * encrypted_songid : 69055fa210956ea4e26L
     */

    private String bitrate_fee;
    private String yyr_artist;
    private String songname;
    private String artistname;
    private String control;
    private String songid;
    private String has_mv;
    private String encrypted_songid;

    public String getBitrate_fee() {
        return bitrate_fee;
    }

    public void setBitrate_fee(String bitrate_fee) {
        this.bitrate_fee = bitrate_fee;
    }

    public String getYyr_artist() {
        return yyr_artist;
    }

    public void setYyr_artist(String yyr_artist) {
        this.yyr_artist = yyr_artist;
    }

    public String getSongname() {
        return songname;
    }

    public void setSongname(String songname) {
        this.songname = songname;
    }

    public String getArtistname() {
        return artistname;
    }

    public void setArtistname(String artistname) {
        this.artistname = artistname;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getSongid() {
        return songid;
    }

    public void setSongid(String songid) {
        this.songid = songid;
    }

    public String getHas_mv() {
        return has_mv;
    }

    public void setHas_mv(String has_mv) {
        this.has_mv = has_mv;
    }

    public String getEncrypted_songid() {
        return encrypted_songid;
    }

    public void setEncrypted_songid(String encrypted_songid) {
        this.encrypted_songid = encrypted_songid;
    }
}
