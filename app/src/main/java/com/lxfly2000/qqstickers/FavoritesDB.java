package com.lxfly2000.qqstickers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.lxfly2000.utilities.FileUtility;

import java.io.ByteArrayInputStream;

public class FavoritesDB extends SQLiteOpenHelper {
    private static final String fileNameDB="QQStickers.db";
    private static final String tableName="Favorites";
    static final String keyPrimary="emid",keyName="name";

    public static String GetDBPath(Context context){
        return context.getExternalFilesDir(null)+"/"+fileNameDB;
    }

    public FavoritesDB(Context context) {
        super(context, GetDBPath(context), null, 1);
    }

    public void Add(int id, String name, String path, ByteArrayInputStream stream){
        ContentValues values=new ContentValues();
        values.put(keyPrimary,id);
        values.put(keyName,name);
        getWritableDatabase().insert(tableName,null,values);
        FileUtility.WriteStreamToFile(path,stream);
    }

    public void Delete(int id,String path){
        getWritableDatabase().delete(tableName,keyPrimary+"=?",new String[]{String.valueOf(id)});
        FileUtility.DeleteFile(path);
    }

    public int GetCount(){
        Cursor c= getReadableDatabase().query(tableName,new String[]{keyPrimary},null,null,null,null,null);
        int count=c.getCount();
        c.close();
        return count;
    }

    public boolean Find(int id){
        Cursor c= getReadableDatabase().query(tableName,new String[]{keyPrimary},keyPrimary+"=?",new String[]{String.valueOf(id)},null,null,null);
        int count=c.getCount();
        c.close();
        return count>0;
    }

    public String GetName(int id){
        Cursor c= getReadableDatabase().query(tableName,new String[]{keyPrimary,keyName},keyPrimary+"=?",new String[]{String.valueOf(id)},null,null,null);
        c.moveToFirst();
        String name=c.getString(0);
        c.close();
        return name;
    }

    public Cursor QueryAll(){
        return getReadableDatabase().query(tableName,new String[]{keyPrimary,keyName},null,null,null,null,keyPrimary);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("Create Table "+tableName+"("+keyPrimary+" Integer Default 11449 Not null Primary key,"+
                keyName+" Text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //Nothing
    }
}
