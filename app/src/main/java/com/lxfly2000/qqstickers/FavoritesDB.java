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

    public int FindNextID(int id){
        //查找第一个大于ID的值即为所求，若无记录返回ID本身，若无符合条件的值则返回第一个ID
        Cursor c=QueryAll();
        if(c.getCount()==0){
            return id;
        }
        c.moveToFirst();
        int nextEmId=c.getInt(c.getColumnIndex(keyPrimary));
        while (!c.isAfterLast()){
            int emid=c.getInt(c.getColumnIndex(keyPrimary));
            if(emid>id){
                nextEmId=emid;
                break;
            }
            c.moveToNext();
        }
        c.close();
        return nextEmId;
    }

    public int FindPreviousID(int id){
        //倒序查找，代码同上，只需把排序改为倒序，比较改为小于即可
        Cursor c=getReadableDatabase().query(tableName,new String[]{keyPrimary,keyName},null,null,null,null,keyPrimary+" DESC");
        if(c.getCount()==0){
            return id;
        }
        c.moveToFirst();
        int nextEmId=c.getInt(c.getColumnIndex(keyPrimary));
        while(!c.isAfterLast()){
            int emid=c.getInt(c.getColumnIndex(keyPrimary));
            if(emid<id){
                nextEmId=emid;
                break;
            }
            c.moveToNext();
        }
        c.close();
        return nextEmId;
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

    public Cursor QueryWithString(String q){
        //https://blog.csdn.net/fantianheyey/article/details/9199235
        return getReadableDatabase().query(tableName,new String[]{keyPrimary,keyName},
                keyName+" like ?",new String[]{"%"+q+"%"},null,null,keyPrimary);
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
