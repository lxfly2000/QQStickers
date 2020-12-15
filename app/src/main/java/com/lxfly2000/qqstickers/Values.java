package com.lxfly2000.qqstickers;

import android.content.Context;
import android.content.SharedPreferences;

public class Values {
    static final String appIdentifier="QQStickers";
    static final String typeFavorite="favorites";
    static final String urlEmotionHead="https://imgcache.qq.com/club/item/parcel/img/parcel/%d/%d/200x200.png";
    public static SharedPreferences GetPreference(Context context){
        return context.getSharedPreferences(appIdentifier, Context.MODE_PRIVATE);
    }
    public static String GetIconPath(Context context,int emid){
        return context.getExternalFilesDir(Values.typeFavorite).getPath()+"/"+emid+".png";
    }
    public static final String urlAuthor="https://github.com/lxfly2000/QQStickers";
    public static final String urlAuthorGithubHome="https://github.com/lxfly2000";
    public static String GetCheckUpdateURL(){
        return urlAuthor+"/raw/master/app/build.gradle";
    }
    public static final String keySkippedVersionCode="skip_ver_code";
    public static final int vDefaultSkippedVersionCode=0;
    public static final String GetNewReleaseURL(){
        return urlAuthor+"/releases";
    }
    public static final int vDefaultLinkToOpen=0;
    public static final boolean vDefaultNavigateFavoriteOnly=false;
    public static final boolean vDefaultPreviewGifOnMainView=false;
}
