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
}
