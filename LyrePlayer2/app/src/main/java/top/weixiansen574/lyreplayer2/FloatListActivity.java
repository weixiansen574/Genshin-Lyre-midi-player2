package top.weixiansen574.lyreplayer2;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FloatListActivity extends AppCompatActivity {
    RecyclerView music_list;
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
        setContentView(R.layout.activity_float_list);
        music_list = findViewById(R.id.music_list_main);
        MusicListAdapter adapter = new MusicListAdapter(FloatListActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        music_list.setLayoutManager(linearLayoutManager);
        music_list.setAdapter(adapter);
    }
}
