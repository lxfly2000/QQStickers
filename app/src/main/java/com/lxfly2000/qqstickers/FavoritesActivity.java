package com.lxfly2000.qqstickers;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.lxfly2000.utilities.AndroidDownloadFileTask;
import com.lxfly2000.utilities.FileUtility;

import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

public class FavoritesActivity extends AppCompatActivity {
    static final int REQUEST_CHOOSE=1;
    ListView listFavorites;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        ActionBar bar=getSupportActionBar();
        if(bar!=null)
            bar.setDisplayHomeAsUpEnabled(true);
        Init();
    }

    static class FavoriteItem extends HashMap<String,Object> {
        static final String keyImg="img";
        static final String keyEmid="emid";
        static final String keyName="name";
        public FavoriteItem(Bitmap image, int id, String name){
            put(keyImg,image);
            put(keyEmid,id);
            put(keyName,name);
        }
    }

    ArrayList<FavoriteItem>favoriteItems;
    SimpleAdapter.ViewBinder listBinder=new SimpleAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Object o, String s) {
            if(view instanceof ImageView){
                ImageView imageView=(ImageView)view;
                if(o instanceof Bitmap){
                    imageView.setImageBitmap((Bitmap)o);
                }else if(o instanceof Drawable){
                    imageView.setImageDrawable((Drawable)o);
                }
                return true;
            }
            return false;
        }
    };

    FavoritesDB favoritesDB=null;
    SimpleAdapter adapter=null;

    void Init(){
        favoriteItems=new ArrayList<>();
        listFavorites=findViewById(R.id.listFavorites);
        adapter=new SimpleAdapter(this,favoriteItems,R.layout.favorite_item,
                new String[]{FavoriteItem.keyImg,FavoriteItem.keyEmid,FavoriteItem.keyName},
                new int[]{R.id.imageEmotion,R.id.imageEmotion,R.id.textEmotionTitle});
        adapter.setViewBinder(listBinder);
        listFavorites.setAdapter(adapter);

        listFavorites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent();
                FavoriteItem item=favoriteItems.get(i);
                intent.putExtra(FavoritesDB.keyPrimary,(int)item.get(FavoriteItem.keyEmid));
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        listFavorites.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                //Nothing
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem!=lastTopItemIndex||firstVisibleItem+visibleItemCount!=lastBottomItemIndex+1){
                    if(listFavorites.getAdapter()!=null) {
                        DisplayImagesVisible(firstVisibleItem,firstVisibleItem+visibleItemCount-1);
                    }
                }
            }
        });

        favoritesDB=new FavoritesDB(this);
        Cursor c=favoritesDB.QueryAll();
        for(c.moveToFirst();!c.isAfterLast();c.moveToNext()){
            int emid=c.getInt(c.getColumnIndex(FavoritesDB.keyPrimary));
            favoriteItems.add(new FavoriteItem(null,emid,emid+" "+c.getString(c.getColumnIndex(FavoritesDB.keyName))));
        }
        c.close();
        adapter.notifyDataSetChanged();
    }

    int lastTopItemIndex,lastBottomItemIndex;

    private void DisplayImagesVisible(int top,int bottom){
        final SimpleAdapter adapter=(SimpleAdapter)listFavorites.getAdapter();
        for(int i=lastTopItemIndex;i<=lastBottomItemIndex;i++){
            if(i>=0&&i<top||i>bottom&&i<listFavorites.getCount()){
                HashMap<String,Object>item=(HashMap)adapter.getItem(i);
                if(item.get(FavoriteItem.keyImg)instanceof Bitmap){
                    //((Bitmap)item.get(FavoriteItem.keyImg)).recycle();
                    //item.remove(FavoriteItem.keyImg);
                    item.put(FavoriteItem.keyImg,getResources().getDrawable(R.drawable.ic_broken_image_red_24dp));
                }
            }
        }
        lastTopItemIndex=top;
        lastBottomItemIndex=bottom;
        for(int i=top;i<=bottom;i++){
            final HashMap<String,Object>item=(HashMap)adapter.getItem(i);
            if(item.get(FavoriteItem.keyImg)==null){
                int emid=(int)item.get(FavoriteItem.keyEmid);
                String coverUrl=String.format(Values.urlEmotionHead,emid%10,emid);
                final String coverPath=Values.GetIconPath(this,emid);
                if(FileUtility.IsFileExists(coverPath)){
                    AsyncTask<Object,Integer,Boolean> task=new AsyncTask<Object, Integer, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Object... objects) {
                            item.put(FavoriteItem.keyImg, BitmapFactory.decodeFile(coverPath));
                            return (int)objects[0]>=lastTopItemIndex&&(int)objects[0]<=lastBottomItemIndex;
                        }
                        @Override
                        protected void onPostExecute(Boolean result){
                            if(result) {
                                adapter.notifyDataSetChanged();
                            }else if(item.get(FavoriteItem.keyImg)instanceof Bitmap){
                                ((Bitmap)item.get(FavoriteItem.keyImg)).recycle();
                                item.remove(FavoriteItem.keyImg);
                            }
                        }
                    };
                    task.execute(i);
                }else{
                    AndroidDownloadFileTask task=new AndroidDownloadFileTask() {
                        @Override
                        public void OnReturnStream(ByteArrayInputStream stream, boolean success, int response, Object extra, URLConnection connection) {
                            int index=(int)extra;
                            if(success) {
                                FileUtility.WriteStreamToFile(coverPath,stream);
                                ((HashMap<String, Object>) adapter.getItem(index)).put(FavoriteItem.keyImg, BitmapFactory.decodeFile(coverPath));
                            }else {
                                ((HashMap<String,Object>)adapter.getItem(index)).put(FavoriteItem.keyImg,getResources().getDrawable(R.drawable.ic_broken_image_red_24dp));
                            }
                            adapter.notifyDataSetChanged();
                        }
                    };
                    task.SetExtra(i);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,coverUrl,coverPath);
                    item.put(FavoriteItem.keyImg,task);
                }
            }
        }
    }
}
