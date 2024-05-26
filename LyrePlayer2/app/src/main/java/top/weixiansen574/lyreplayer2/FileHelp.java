package top.weixiansen574.lyreplayer2;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

public class FileHelp extends AppCompatActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //工具栏返回上一级按钮
        if (item.getItemId() == 16908332){
            finish();
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_help);
    }
}