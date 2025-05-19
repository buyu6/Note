package com.example.newnote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String CREATE_NOTE = "create table Note("
            + "id integer primary key autoincrement, "
            + "title text, "
            + "content text)";
    private Context mContext;

    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, getDatabasePath(context, name), factory, version);
        mContext = context;
    }

    private static String getDatabasePath(Context context, String name) {
        // 获取应用私有目录
        File dbDir = context.getDatabasePath(name).getParentFile();
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        return context.getDatabasePath(name).getAbsolutePath();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_NOTE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // 删除旧表
        sqLiteDatabase.execSQL("drop table if exists Note");
        // 创建新表
        onCreate(sqLiteDatabase);
    }

    public Cursor getAllNote() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.query("Note", null, null, null, null, null, null);
    }
}
