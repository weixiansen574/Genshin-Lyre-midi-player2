package top.weixiansen574.lyreplayer2.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
@SuppressLint("Range")
public class MusicDao extends SQLiteOpenHelper {
    public static final int VERSION = 1;
    public static final String DB_NAME = "musics.db";
    private final SQLiteDatabase database;
    public MusicDao(@Nullable Context context) {
        super(context, DB_NAME, null, VERSION);
        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE musics (\n" +
                "    name                TEXT    PRIMARY KEY\n" +
                "                                UNIQUE\n" +
                "                                NOT NULL,\n" +
                "    instrument          INTEGER NOT NULL,\n" +
                "    transposition       INTEGER NOT NULL,\n" +
                "    invalid_key_setting INTEGER NOT NULL,\n" +
                "    mapping1            INTEGER NOT NULL,\n" +
                "    mapping2            INTEGER NOT NULL,\n" +
                "    mapping3            INTEGER NOT NULL,\n" +
                "    mapping4            INTEGER NOT NULL,\n" +
                "    mapping5            INTEGER NOT NULL,\n" +
                "    mapping6            INTEGER NOT NULL,\n" +
                "    mapping7            INTEGER NOT NULL,\n" +
                "    create_date         LONG    NOT NULL,\n" +
                "    path                TEXT    NOT NULL\n" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean checkMusicExists(String name){
        Cursor cursor = database.rawQuery("SELECT * FROM musics WHERE name = ?", new String[]{name});
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public int insertMusic(MusicBean musicBean){
        ContentValues cv = new ContentValues();
        cv.put("name", musicBean.name);
        cv.put("instrument", musicBean.instrument);
        cv.put("transposition", musicBean.transposition);
        cv.put("invalid_key_setting", musicBean.invalid_key_setting);
        cv.put("mapping1", musicBean.mapping1);
        cv.put("mapping2", musicBean.mapping2);
        cv.put("mapping3", musicBean.mapping3);
        cv.put("mapping4", musicBean.mapping4);
        cv.put("mapping5", musicBean.mapping5);
        cv.put("mapping6", musicBean.mapping6);
        cv.put("mapping7", musicBean.mapping7);
        cv.put("create_date", musicBean.create_date);
        cv.put("path", musicBean.path);
        return (int) database.insert("musics", null, cv);
    }

    public int updateMusic(MusicBean musicBean){
        ContentValues cv = new ContentValues();
        cv.put("name", musicBean.name);
        cv.put("instrument", musicBean.instrument);
        cv.put("transposition", musicBean.transposition);
        cv.put("invalid_key_setting", musicBean.invalid_key_setting);
        cv.put("mapping1", musicBean.mapping1);
        cv.put("mapping2", musicBean.mapping2);
        cv.put("mapping3", musicBean.mapping3);
        cv.put("mapping4", musicBean.mapping4);
        cv.put("mapping5", musicBean.mapping5);
        cv.put("mapping6", musicBean.mapping6);
        cv.put("mapping7", musicBean.mapping7);
        cv.put("create_date", musicBean.create_date);
        cv.put("path", musicBean.path);
        return database.update("musics", cv, "name = ?", new String[]{musicBean.name});
    }

    public int deleteMusic(String name){
        return database.delete("musics", "name = ?", new String[]{name});
    }

    public List<MusicBean> getAllMusics(){
        List<MusicBean> musicList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM musics order by create_date desc", null);
        while (cursor.moveToNext()){
            MusicBean musicBean = new MusicBean(
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getInt(cursor.getColumnIndex("instrument")),
                    cursor.getInt(cursor.getColumnIndex("transposition")),
                    cursor.getInt(cursor.getColumnIndex("invalid_key_setting")),
                    cursor.getInt(cursor.getColumnIndex("mapping1")),
                    cursor.getInt(cursor.getColumnIndex("mapping2")),
                    cursor.getInt(cursor.getColumnIndex("mapping3")),
                    cursor.getInt(cursor.getColumnIndex("mapping4")),
                    cursor.getInt(cursor.getColumnIndex("mapping5")),
                    cursor.getInt(cursor.getColumnIndex("mapping6")),
                    cursor.getInt(cursor.getColumnIndex("mapping7")),
                    cursor.getLong(cursor.getColumnIndex("create_date")),
                    cursor.getString(cursor.getColumnIndex("path")));
            musicList.add(musicBean);
        }
        cursor.close();
        return musicList;
    }


}
