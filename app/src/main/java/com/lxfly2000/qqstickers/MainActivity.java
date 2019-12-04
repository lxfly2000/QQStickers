package com.lxfly2000.qqstickers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.lxfly2000.utilities.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK&&data!=null){
            editEmId.setText(String.valueOf(data.getIntExtra(FavoritesDB.keyPrimary,lastSuccessNavigateId)));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_favorites:
                startActivityForResult(new Intent(this, FavoritesActivity.class), FavoritesActivity.REQUEST_CHOOSE);
                break;
            case R.id.menu_about:
                startActivity(new Intent(this,AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitApp();
    }

    EditText editTitle,editEmId;
    TextView textRealSize,textIsShow,textFeeType,textDesc,textTags;
    Button buttonDownload,buttonOpenLink,buttonPrevious,buttonNext;
    ToggleButton toggleFavorite;
    GridView gridEmList;
    ImageView imageIcon;
    AndroidDownloadFileTask headTask=null,jsonTask=null;
    SimpleAdapter listAdapter;
    class EmotionItem extends HashMap<String,Object> {
        static final String keyImg="img";
        static final String keyName="name";
        public AndroidDownloadFileTask task=null;
        public EmotionItem(Object img,String name){
            put(keyImg,img);
            put(keyName,name);
        }
        public void ReleaseImage() {
            Object obj = get(keyImg);
            if (obj instanceof Bitmap) {
                Bitmap img = (Bitmap) obj;
                img.recycle();
            }
        }
        public void StopTask(){
            if(task!=null&&!task.isCancelled())
                task.cancel(true);
        }
    }
    ArrayList<EmotionItem>emItems=null;
    SimpleAdapter.ViewBinder emItemsViewBinder=new SimpleAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Object o, String s) {
            if(o instanceof Bitmap) {
                ((ImageView)view).setImageBitmap((Bitmap)o);
            }else if(o instanceof Drawable){
                ((ImageView)view).setImageDrawable((Drawable)o);
            }else if(o instanceof String){
                ((TextView)view).setText((String)o);
            }
            return true;
        }
    };
    FavoritesDB favoritesDB;

    void InitApp(){
        preferences=Values.GetPreference(this);
        long t=System.currentTimeMillis();
        if(preferences.getLong("first_use",Long.MAX_VALUE)>t){
            preferences.edit().putLong("first_use",t).apply();
            try {
                FileUtility.WriteStreamToFile(FavoritesDB.GetDBPath(this), getAssets().open("QQStickers.db"));
            }catch (IOException e){
                ReportException(e,true);
            }
        }
        favoritesDB=new FavoritesDB(this);
        gridEmList=findViewById(R.id.gridEmotions);
        imageIcon=findViewById(R.id.imageEmotionIcon);
        editTitle=findViewById(R.id.editTitle);
        editEmId=findViewById(R.id.editEmotionID);
        textRealSize=findViewById(R.id.textRealSize);
        textIsShow=findViewById(R.id.textIsShow);
        textFeeType=findViewById(R.id.textFeeType);
        textDesc=findViewById(R.id.textDescription);
        textTags=findViewById(R.id.textTags);
        buttonDownload=findViewById(R.id.buttonDownload);
        toggleFavorite=findViewById(R.id.toggleFavorite);
        buttonOpenLink=findViewById(R.id.buttonOpenLink);
        buttonPrevious=findViewById(R.id.buttonPrevious);
        buttonNext=findViewById(R.id.buttonNext);
        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String downloadUrl = String.format("https://imgcache.qq.com/qqshow/admindata/comdata/vipEmoji_item_%d/%s",
                            lastSuccessNavigateId, json.getJSONObject("data").getJSONArray("baseInfo").getJSONObject(0).getString("zip"));
                    String fileName=String.format("%d_%s.zip",lastSuccessNavigateId,
                            json.getJSONObject("data").getJSONArray("baseInfo").getJSONObject(0).getString("name"));
                    String savePath=getExternalFilesDir("download").getPath()+"/"+fileName;
                    if(FileUtility.IsFileExists(savePath)){
                        AndroidUtility.MessageBox(MainActivity.this,getString(R.string.msg_file_exists,savePath),buttonDownload.getText().toString());
                        return;
                    }
                    AndroidSysDownload sysDownload=new AndroidSysDownload(getBaseContext());
                    sysDownload.StartDownloadFile(downloadUrl,savePath,fileName,savePath);
                }catch (JSONException e){
                    ReportException(e,true);
                }
            }
        });
        buttonOpenLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url="https://gxh.vip.qq.com/club/themes/mobile/bq/html/detail.html?id=%d";
                AndroidUtility.OpenUri(getBaseContext(),String.format(url,lastSuccessNavigateId));
            }
        });
        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editEmId.length()>0) {
                    int id = Integer.parseInt(editEmId.getText().toString());
                    id = Math.max(0, id - 1);
                    editEmId.setText(String.valueOf(id));
                }
            }
        });
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editEmId.length()>0) {
                    int id = Integer.parseInt(editEmId.getText().toString());
                    id = Math.min(Integer.MAX_VALUE, id + 1);
                    editEmId.setText(String.valueOf(id));
                }
            }
        });
        editEmId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                editEmId.setError(null);
                if(charSequence.length()>0)
                    NavigateEmId(Integer.parseInt(charSequence.toString()));
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Nothing
            }
        });
        toggleFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String iconPath=Values.GetIconPath(getBaseContext(),lastSuccessNavigateId);
                if(((ToggleButton)view).isChecked()){
                    favoritesDB.Add(lastSuccessNavigateId,editTitle.getText().toString(),iconPath,lastIconStream);
                }else{
                    favoritesDB.Delete(lastSuccessNavigateId,iconPath);
                }
            }
        });
        emItems=new ArrayList<>();
        listAdapter=new SimpleAdapter(this,emItems,R.layout.grid_item,
                new String[]{EmotionItem.keyImg,EmotionItem.keyName},
                new int[]{R.id.imageEmotionItem,R.id.textName});
        listAdapter.setViewBinder(emItemsViewBinder);
        gridEmList.setAdapter(listAdapter);
        LoadLastEmId();
    }

    SharedPreferences preferences;

    static final String keyLastEmId="emid";
    static final int vdLastEmId=11449;

    void LoadLastEmId(){
        lastSuccessNavigateId=preferences.getInt(keyLastEmId,vdLastEmId);
        editEmId.setText(String.valueOf(lastSuccessNavigateId));
    }

    int lastSuccessNavigateId;

    void NavigateEmId(final int id){
        preferences.edit().putInt(keyLastEmId,id).apply();
        String reqUrl=String.format("https://imgcache.qq.com/qqshow/admindata/comdata/vipEmoji_item_%d/xydata.json",id);
        if(jsonTask!=null&&!jsonTask.isCancelled())
            jsonTask.cancel(true);
        jsonTask=new AndroidDownloadFileTask() {
            @Override
            public void OnReturnStream(ByteArrayInputStream stream, boolean success, int response, Object extra, URLConnection connection) {
                if(!success){
                    ReportFail(false);
                    return;
                }
                if(response!=200){
                    ReportMessage("HTTP: "+response,false);
                    return;
                }
                try{
                    lastSuccessNavigateId=id;
                    ShowJson(StreamUtility.GetStringFromStream(stream,false));
                }catch (IOException e){
                    ReportException(e,false);
                }
            }
        };
        jsonTask.execute(reqUrl);
    }

    Bitmap lastIconBitmap=null;
    ByteArrayInputStream lastIconStream=null;
    JSONObject json;

    void ShowJson(String strData){
        try{
            json=new JSONObject(strData);
            toggleFavorite.setChecked(favoritesDB.Find(lastSuccessNavigateId));
            editTitle.setText(json.getJSONObject("data").getJSONArray("baseInfo").getJSONObject(0).getString("name"));
            int realSize=0;
            try{
                realSize=json.getJSONObject("data").getJSONArray("baseInfo").getJSONObject(0).getInt("realSize");
            }catch (JSONException e){/*Nothing*/}
            textRealSize.setText(getString(R.string.label_real_size,GetSizeString(realSize),realSize));
            boolean isShow=json.getJSONObject("data").getJSONArray("operationInfo").getJSONObject(0).getInt("isShow")!=0;
            if(isShow){
                textIsShow.setText(R.string.label_show_true);
                textIsShow.setTextColor(getResources().getColor(R.color.colorAvailable));
            }else{
                textIsShow.setText(R.string.label_show_false);
                textIsShow.setTextColor(getResources().getColor(R.color.colorUnavailable));
            }
            boolean feeTypeFree=json.getJSONObject("data").getJSONArray("baseInfo").getJSONObject(0).getInt("feeType")==1;
            if(feeTypeFree){
                textFeeType.setText(R.string.label_fee_free);
                textFeeType.setTextColor(getResources().getColor(R.color.colorAvailable));
            }else{
                textFeeType.setText(R.string.label_fee_restricted);
                textFeeType.setTextColor(getResources().getColor(R.color.colorUnavailable));
            }
            textDesc.setText(json.getJSONObject("data").getJSONArray("baseInfo").getJSONObject(0).getString("desc"));
            textTags.setText(getString(R.string.label_tag,json.getJSONObject("data").getJSONArray("baseInfo").getJSONObject(0).getString("tag")));
            String urlIcon=String.format(Values.urlEmotionHead,lastSuccessNavigateId%10,lastSuccessNavigateId);
            if(headTask!=null&&!headTask.isCancelled())
                headTask.cancel(true);
            headTask=new AndroidDownloadFileTask() {
                @Override
                public void OnReturnStream(ByteArrayInputStream stream, boolean success, int response, Object extra, URLConnection connection) {
                    if (!success) {
                        ReportFail(true);
                        imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_broken_image_red_24dp));
                        return;
                    }
                    if (response != 200) {
                        ReportMessage("HTTP: " + response, true);
                        imageIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_broken_image_red_24dp));
                        return;
                    }
                    if (lastIconBitmap != null)
                        lastIconBitmap.recycle();
                    lastIconStream=stream;
                    lastIconBitmap = BitmapFactory.decodeStream(lastIconStream);
                    imageIcon.setImageBitmap(lastIconBitmap);
                    listAdapter.notifyDataSetChanged();
                }
            };
            headTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,urlIcon);
            JSONArray md5Info=json.getJSONObject("data").getJSONArray("md5Info");
            for(int i=0;i<md5Info.length();i++){
                JSONObject singleInfo=md5Info.getJSONObject(i);
                String name=singleInfo.getString("name");
                if(emItems.size()==i)
                    emItems.add(new EmotionItem(getResources().getDrawable(R.drawable.ic_broken_image_red_24dp),name));
                EmotionItem item=emItems.get(i);
                item.put(EmotionItem.keyName,name);
                item.StopTask();
                GridDownloadImage(i,0,name,singleInfo.getString("md5"));
            }
            while(emItems.size()>md5Info.length()){
                EmotionItem item=emItems.get(md5Info.length());
                item.StopTask();
                item.ReleaseImage();
                emItems.remove(item);
            }
            listAdapter.notifyDataSetChanged();
        }catch (JSONException e){
            ReportException(e,false);
        }
    }

    static String[]urlsEm={
            "https://imgcache.qq.com/club/item/parcel/item/%s/%s/300x300.png",
            "https://imgcache.qq.com/club/item/parcel/item/%s/%s/200x200.png",
            "https://imgcache.qq.com/club/item/parcel/item/%s/%s/126x126.png"
    };

    void GridDownloadImage(final int index, final int urlIndex, final String name, final String md5){
        if(urlIndex>=urlsEm.length){
            emItems.get(index).put(EmotionItem.keyImg,getResources().getDrawable(R.drawable.ic_broken_image_red_24dp));
            return;
        }
        EmotionItem item=emItems.get(index);
        item.task=new AndroidDownloadFileTask() {
            @Override
            public void OnReturnStream(ByteArrayInputStream stream, boolean success, int response, Object extra, URLConnection connection) {
                EmotionItem item = emItems.get(index);
                if (!success) {
                    GridDownloadImage(index, urlIndex + 1, name, md5);
                    return;
                }
                if (response != 200) {
                    GridDownloadImage(index, urlIndex + 1, name, md5);
                    return;
                }
                item.ReleaseImage();
                item.put(EmotionItem.keyImg,BitmapFactory.decodeStream(stream));
                listAdapter.notifyDataSetChanged();
            }
        };
        String url=String.format(urlsEm[urlIndex],md5.substring(0,2),md5);
        item.task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,url);
    }

    String GetSizeString(int byteCount){
        float m=(float)byteCount;
        if(m<1024.0f)
            return byteCount+" B";
        m=m/1024.0f;
        if(m<1024.0f)
            return String.format("%.2f KB",m);
        m=m/1024.0f;
        if(m<1024.0f)
            return String.format("%.2f MB",m);
        m=m/1024.0f;
        return String.format("%.2f GB",m);
    }

    int GetEmId(){
        return Integer.parseInt(((EditText)findViewById(R.id.editEmotionID)).getText().toString());
    }

    String GetEmName(){
        return ((EditText)findViewById(R.id.editTitle)).getText().toString();
    }

    void ReportFail(boolean toast){
        ReportMessage(getString(R.string.msg_error_fail),toast);
    }

    void ReportException(Exception e,boolean toast){
        ReportMessage(getString(R.string.msg_exception,e.getLocalizedMessage()),toast);
    }

    void ReportMessage(String msg,boolean toast){
        if(toast) {
            Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
        }else{
            editEmId.setError(msg);
            editEmId.requestFocus();
        }
    }
}
