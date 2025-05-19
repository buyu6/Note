package com.example.newnote;

import static com.example.newnote.R.drawable.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

public class MainActivity extends BaseActivity implements ItemClickListener{
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String PREF_NAME = "background_pref";
    private static final String KEY_BACKGROUND = "background_id";
    private static final String KEY_ACTION_BAR_COLOR = "action_bar_color";
    private static final String KEY_ACTION_BAR_TEXT_COLOR = "action_bar_text_color";
    private DatabaseHelper dbHelper;
    private List<Note> noteList = new ArrayList<>();
    private NoteAdapter adapter;
    FrameLayout container;
    private RefreshReceiver refreshReceiver;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @SuppressLint({"WrongViewCast", "UnspecifiedRegisterReceiverFlag"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 检查并请求权限
        checkAndRequestPermissions();
        
        dbHelper = new DatabaseHelper(this, "Note.fy", null, 1);
        dbHelper.getWritableDatabase();
        RecyclerView recyclerView=(RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter=new NoteAdapter(noteList);
        recyclerView.setAdapter(adapter);
        adapter.setItemClickListener(this);
        adapter.setItemLongClickListener(this);
        container=findViewById(R.id.container);
        
        // 恢复保存的背景和标题栏颜色
        restoreBackground();
        
        // 设置悬浮按钮点击事件
        com.google.android.material.floatingactionbutton.FloatingActionButton fab = findViewById(R.id.fab_add_note);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Add_Note.class);
                startActivity(intent);
            }
        });
        
        // 注册广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.newnote.REFRESH_DATA");
        refreshReceiver = new RefreshReceiver();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(refreshReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(refreshReceiver, intentFilter);
        }
        
        loadNotes();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        if (refreshReceiver != null) {
            unregisterReceiver(refreshReceiver);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到主界面时刷新数据
        refreshData();
    }
    private void refreshData() {
        noteList.clear();
        loadNotes();
        adapter.notifyDataSetChanged();
    }
    class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshData();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_item) {
            Intent intent = new Intent(MainActivity.this, Add_Note.class);
            startActivity(intent);
        } else if (item.getItemId()==R.id.force_offline) {
            Toast.makeText(this, "退出成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.change_background) {
            // 显示背景选择对话框
            String[] backgrounds = {"背景1", "背景2", "背景3", "背景4"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("选择背景");
            builder.setItems(backgrounds, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int backgroundId = 0;
                    switch (which) {
                        case 0:
                            backgroundId = R.drawable.a;
                            break;
                        case 1:
                            backgroundId = R.drawable.c;
                            break;
                        case 2:
                            backgroundId = R.drawable.d;
                            break;
                        case 3:
                            backgroundId = R.drawable.e;
                            break;
                        default:
                            break;
                    }
                    if (backgroundId != 0) {
                        container.setBackgroundResource(backgroundId);
                        // 更新标题栏颜色
                        updateActionBarColor(backgroundId);
                        // 保存背景选择
                        saveBackground(backgroundId);
                        Toast.makeText(MainActivity.this, "背景已更换", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.show();
        }
        return true;
    }
    private void loadNotes(){
        Cursor cursor=dbHelper.getAllNote();
        if(cursor.moveToFirst()){
            do{
                long id=cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String title=cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String content=cursor.getString(cursor.getColumnIndexOrThrow("content"));
                noteList.add(new Note(id,title,content));
            }while(cursor.moveToNext());
        }
        cursor.close();
    }
    @Override
    public void onItemClick(int position){
        Intent intent=new Intent("com.example.newnote.ACTION_START");
        intent.putExtra("item_id",noteList.get(position).getId());
        intent.putExtra("item_title",noteList.get(position).getTitle());
        intent.putExtra("item_content",noteList.get(position).getContent());
        startActivity(intent);
    }
    @Override
    public void onItemLongClick(int position){
        AlertDialog.Builder dialog=new AlertDialog.Builder(MainActivity.this);
        dialog.setCancelable(true);
        dialog.setPositiveButton("编辑", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent=new Intent("com.example.newnote.ACTION_START");
                intent.putExtra("item_id",noteList.get(position).getId());
                intent.putExtra("item_title",noteList.get(position).getTitle());
                intent.putExtra("item_content",noteList.get(position).getContent());
                startActivity(intent);
            }
        });
        dialog.setNegativeButton("删除", new DialogInterface.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SQLiteDatabase db=dbHelper.getWritableDatabase();
                long id=noteList.get(position).getId();
                db.delete("Note","id=?",new String[]{String.valueOf(id)});
                noteList.remove(position);
                adapter.notifyDataSetChanged();
            }
        });
        dialog.show();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-12使用存储权限
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }, PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限获取成功，初始化数据库
                dbHelper = new DatabaseHelper(this, "Note.fy", null, 1);
                loadNotes();
            } else {
                Toast.makeText(this, "需要存储权限才能使用笔记功能", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateActionBarColor(int backgroundId) {
        // 更新ActionBar颜色
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 使用背景图片作为标题栏背景
            actionBar.setBackgroundDrawable(getResources().getDrawable(backgroundId));
            // 清除原有标题
            actionBar.setDisplayShowTitleEnabled(false);
            // 设置标题文字颜色
            android.widget.TextView titleTextView = new android.widget.TextView(this);
            titleTextView.setText("NewNote");
            titleTextView.setTextColor(getResources().getColor(R.color.black));
            titleTextView.setTextSize(20);
            actionBar.setCustomView(titleTextView);
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

    private void saveActionBarColors(int actionBarColor, int actionBarTextColor) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_ACTION_BAR_COLOR, actionBarColor);
        editor.putInt(KEY_ACTION_BAR_TEXT_COLOR, actionBarTextColor);
        editor.apply();
    }

    private void restoreActionBarColors() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int actionBarColor = prefs.getInt(KEY_ACTION_BAR_COLOR, R.color.action_bar_bg_2);
        int actionBarTextColor = prefs.getInt(KEY_ACTION_BAR_TEXT_COLOR, R.color.action_bar_text_2);
        
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(getResources().getColor(actionBarColor)));
            android.widget.TextView titleTextView = new android.widget.TextView(this);
            titleTextView.setText(actionBar.getTitle());
            titleTextView.setTextColor(getResources().getColor(actionBarTextColor));
            titleTextView.setTextSize(20);
            actionBar.setCustomView(titleTextView);
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

    private void restoreBackground() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int backgroundId = prefs.getInt(KEY_BACKGROUND, R.drawable.d);
        container.setBackgroundResource(backgroundId);
        updateActionBarColor(backgroundId);
    }

    private void saveBackground(int backgroundId) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_BACKGROUND, backgroundId);
        editor.apply();
    }
}