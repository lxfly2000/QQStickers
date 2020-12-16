package com.lxfly2000.qqstickers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {
    static final String keyNeedReload="need_reload";
    SettingsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        fragment=new SettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, fragment)
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:finish();return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            sharedPreferences=Values.GetPreference(getActivity());
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            enumLinkToOpen=findPreference(getString(R.string.key_link_to_open));
            checkNavigateFavoriteOnly=findPreference(getString(R.string.key_only_navigate_favorite));
            checkPreviewGifOnMainView=findPreference(getString(R.string.key_preview_gif_on_main_view));
            enumLinkToOpen.setOnPreferenceChangeListener(onChangeListener);
            checkNavigateFavoriteOnly.setOnPreferenceChangeListener(onChangeListener);
            checkPreviewGifOnMainView.setOnPreferenceChangeListener(onChangeListener);
            PresentSettings();
        }

        SharedPreferences sharedPreferences;
        ListPreference enumLinkToOpen;
        CheckBoxPreference checkNavigateFavoriteOnly,checkPreviewGifOnMainView;

        private boolean updated=false;
        private boolean needReload=false;

        public boolean isUpdated(){
            return updated;
        }

        public boolean isNeedReload(){
            return needReload;
        }

        private void PresentSettings(){
            PresentSettings(enumLinkToOpen,Values.vDefaultLinkToOpen);
            PresentSettings(checkNavigateFavoriteOnly,Values.vDefaultNavigateFavoriteOnly);
            PresentSettings(checkPreviewGifOnMainView,Values.vDefaultPreviewGifOnMainView);
        }

        String Unformat(CharSequence sequence){
            return sequence.toString().replace("%","%%");
        }

        private void PresentSettings(Preference p, Object def){
            if(p instanceof CheckBoxPreference){
                ((CheckBoxPreference) p).setChecked(sharedPreferences.getBoolean(p.getKey(),(boolean)def));
            }else if(p instanceof ListPreference){
                ((ListPreference) p).setValueIndex(sharedPreferences.getInt(p.getKey(),(int)def));
                //不知道为什么这句会对百分号转义…
                p.setSummary(Unformat(((ListPreference) p).getEntries()[sharedPreferences.getInt(p.getKey(),(int)def)]));
            }
        }

        int FindValueAt(CharSequence[]sequences,String s){
            for(int i=0;i<sequences.length;i++){
                if(s.contentEquals(sequences[i]))
                    return i;
            }
            return -1;
        }

        Preference.OnPreferenceChangeListener onChangeListener=(preference, o) -> {
            if(preference instanceof CheckBoxPreference) {
                sharedPreferences.edit().putBoolean(preference.getKey(), (boolean) o).apply();
                if(preference.getKey().equals(checkPreviewGifOnMainView.getKey()))
                    needReload=true;
            }else if(preference instanceof ListPreference){
                try {
                    sharedPreferences.edit().putInt(preference.getKey(), FindValueAt(((ListPreference) preference).getEntries(),(String) o)).apply();
                }catch (NumberFormatException e){
                    return false;
                }
            }
            PresentSettings();
            updated=true;
            return true;
        };
    }

    @Override
    public void finish(){
        if(fragment.isUpdated()){
            Toast.makeText(this,R.string.message_settings_saved,Toast.LENGTH_LONG).show();
            Intent rIntent=new Intent();
            rIntent.putExtra(keyNeedReload,fragment.isNeedReload());
            setResult(RESULT_OK,rIntent);
        }
        super.finish();
    }
}