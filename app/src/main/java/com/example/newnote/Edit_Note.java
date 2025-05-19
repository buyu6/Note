package com.example.newnote;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Edit_Note extends BaseActivity {
    private static final String PREF_NAME = "src_pref";
    private static final String KEY_BACKGROUND = "background_id";
    private Note note;
    private DatabaseHelper dbHelper;
    private NoteAdapter adapter;
    private LinearLayout container;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        container=(LinearLayout)findViewById(R.id.edit_layout) ;
        dbHelper=new DatabaseHelper(this,"Note.fy",null,1);
        EditText titleEdit=findViewById(R.id.title_edit);
        EditText contentEdit=findViewById(R.id.content_edit);
        long id=getIntent().getLongExtra("item_id",0);
        String title=getIntent().getStringExtra("item_title");
        String content=getIntent().getStringExtra("item_content");
        restoreBackground();
        titleEdit.setText(title);
        contentEdit.setText(content);
        Button editButton=findViewById(R.id.button_edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              SQLiteDatabase db=dbHelper.getWritableDatabase();
                ContentValues values=new ContentValues();
                String etitle=titleEdit.getText().toString();
                String econtent=contentEdit.getText().toString();
                values.put("content", econtent);
                values.put("title",etitle);
                db.update("Note", values, "id=?", new String[]{String.valueOf(id)});
                Toast.makeText(Edit_Note.this, "保存成功", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                finish();

            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.changed_src) {
            // 显示背景选择对话框
            String[] backgrounds = {"背景1", "背景2", "背景3", "背景4"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("选择背景");
            builder.setItems(backgrounds, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int backgroundId=0;
                    switch (which) {
                        case 0:
                            backgroundId = R.drawable.edit_a;
                            break;
                        case 1:
                            backgroundId = R.drawable.edit_b;
                            break;
                        case 2:
                            backgroundId = R.drawable.edit_c;
                            break;
                        case 3:
                            backgroundId = R.drawable.edit_d;
                            break;
                        default:
                            break;
                    }
                    container.setBackgroundResource(backgroundId);
                    // 保存背景选择
                    saveBackground(backgroundId);
                    Toast.makeText(Edit_Note.this, "背景已更换", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        }
        return true;
    }
    private void restoreBackground() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int backgroundId = prefs.getInt(KEY_BACKGROUND, R.drawable.edit_b); // 默认使用背景edit_b
        container.setBackgroundResource(backgroundId);
    }

    private void saveBackground(int backgroundId) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_BACKGROUND, backgroundId);
        editor.apply();
    }
}