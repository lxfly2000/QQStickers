package com.lxfly2000.qqstickers;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class AboutActivity extends AppCompatActivity {
    static void SetTextViewWithURL(TextView textView, String url){
        if(url!=null)
            textView.setText(Html.fromHtml(String.format("<a href=\"%s\">%s</a>",url,textView.getText())));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_check_update:
                new UpdateChecker(this).CheckForUpdate(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ActionBar bar=getSupportActionBar();
        if(bar!=null)
            bar.setDisplayHomeAsUpEnabled(true);

        ((TextView)findViewById(R.id.textVersionInfo)).setText(getString(R.string.label_version,BuildConfig.VERSION_NAME,BuildConfig.BUILD_DATE));
        SetTextViewWithURL(findViewById(R.id.textViewGotoGithub),Values.urlAuthor);
        SetTextViewWithURL(findViewById(R.id.textViewMadeBy),Values.urlAuthorGithubHome);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about,menu);
        return true;
    }
}
