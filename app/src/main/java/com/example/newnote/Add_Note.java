package com.example.newnote;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Add_Note extends BaseActivity {
    private DatabaseHelper dbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        
        dbHelper = new DatabaseHelper(this, "Note.fy", null, 1);
        EditText titleEdit = (EditText) findViewById(R.id.title_add);
        EditText contentEdit = (EditText) findViewById(R.id.content_add);
        Button saveButton = (Button) findViewById(R.id.button_add);
        
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEdit.getText().toString();
                String content = contentEdit.getText().toString();
                
                if (title.isEmpty() && content.isEmpty()) {
                    Toast.makeText(Add_Note.this, "标题和内容不能同时为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("title", title);
                    values.put("content", content);
                    db.insert("Note", null, values);
                    Toast.makeText(Add_Note.this, "保存成功", Toast.LENGTH_SHORT).show();
                    
                    // 发送广播通知主界面刷新数据
                    Intent intent = new Intent("com.example.newnote.REFRESH_DATA");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        sendBroadcast(intent, String.valueOf(Context.RECEIVER_NOT_EXPORTED));
                    } else {
                        sendBroadcast(intent);
                    }
                    
                    finish();
                } catch (Exception e) {
                    Toast.makeText(Add_Note.this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
