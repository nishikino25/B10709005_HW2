package com.exaple.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.exaple.myapplication.data.TestUtil;
import com.exaple.myapplication.data.WaitlistContract;
import com.exaple.myapplication.data.WaitlistDbHelper;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    public MyAdapter myAdapter;
    public SQLiteDatabase mDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.recycleview);
        mRecyclerView.setHasFixedSize(true);
        //set Layoutmanager的初始值
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        //宣告一個WaitlistDbHelper物件並給初始值
        WaitlistDbHelper dbHelper = new WaitlistDbHelper(this);

        //查看資料庫data
        //mDb=dbHelper.getReadableDatabase
        //更改資料庫data
        mDb = dbHelper.getWritableDatabase();

        //載入FAKEDATA來測試
//        TestUtil.insertFakeData(mDb); //會自動新增5筆Guest

        Cursor cursor = getAllGuests(); //找到所有Guests結果然後存進Cursor物件
        myAdapter = new MyAdapter(this, cursor);
        mRecyclerView.setAdapter(myAdapter);

        setupSharedPreferences();  //設預設值給preference

        //控制設定，讓USER可以往左右滑動來刪除資料
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                //取得id內容from recyclerview
                long id = (long) viewHolder.itemView.getTag();
                AlertDialog a = new AlertDialog.Builder(MainActivity.this).create();
                a.setTitle("刪除");
                a.setMessage("確定要刪除訊息嗎?\n確定(OK) 取消(Cancel)");
                a.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long id = (long) viewHolder.itemView.getTag();
                        //將要刪除的id傳給removeGuest方法
                        removeGuest(id);
                        //刷新guestlist
                        myAdapter.swapCursor(getAllGuests());
                    }
                });
                a.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myAdapter.swapCursor(getAllGuests());
                        dialog.dismiss();
                    }
                });
                a.show();
            }
        }).attachToRecyclerView(mRecyclerView); //這個的所有操作回傳給mRecyclerView
    }

    //取得所有guest
    public Cursor getAllGuests(){ //把SQL查詢結果存起來
        return mDb.query(
                WaitlistContract.WaitlistEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                WaitlistContract.WaitlistEntry.COLUMN_TIMESTAMP
        );
    }

    //刪除Guest，return True is remove Guest Successful
    private boolean removeGuest(long id){
        return mDb.delete(WaitlistContract.WaitlistEntry.TABLE_NAME,WaitlistContract.WaitlistEntry._ID + "=" + id, null) > 0;
    }

    //從Lesson7-12的defaultSetup改的
    private void setupSharedPreferences(){
        //從PreferenceManager中拿一個預設的SharedPreferences
//        Log.e("zhen", "SetUpSharedPreference");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //預設值
        loadColorFromPreferences(sharedPreferences);
        // 使用監聽器註冊
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        //
    }
    private void loadColorFromPreferences(SharedPreferences sharedPreferences) {
//        Log.d("zhen","sharedPreferences.getString()"+sharedPreferences.getString(getString(R.string.pref_color_key), getString(R.string.pref_color_red_value)));
        setColor(sharedPreferences.getString(getString(R.string.pref_color_key), getString(R.string.pref_color_red_value)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //click item
        Intent it = new Intent();
        if(item.getItemId()==R.id.addpeople){
            it.setClass(this,AddActivity.class);
        }else if(item.getItemId() == R.id.setting){
            it.setClass(this,SettingActivity.class);
        }else{
            Toast.makeText(this,"error: something wrong",Toast.LENGTH_LONG).show();
        }
        startActivity(it);
        return super.onOptionsItemSelected(item);

//        Lession7範例
//        int id =item.getItemId();
//        if(id == R.id.activity_setting){
//            Intent startSettingsActivity=new Intent(this,SettingActivity.class);
//            startActivity(startSettingsActivity);
//            return true;
//        }
//        else if(id == R.id.action_color){
//            Intent startSettingsActivity=new Intent(this,SettingActivity.class);
//            startActivity(startSettingsActivity);
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
    }

    //指定color傳到myAdapter的int circle，接著更新recyclerview，再用onBindViewholder刷新頁面
    public void setColor(String newColorKey) {
        Log.e("zhen", "newcolorkey="+newColorKey);
        if (newColorKey.equals(getString(R.string.pref_color_blue_value))) {
            Toast.makeText(this, "Exchange Blue", Toast.LENGTH_LONG).show();
            myAdapter.circle = 1;

        } else if (newColorKey.equals(getString(R.string.pref_color_green_value))) {
            Toast.makeText(this, "Exchange Green", Toast.LENGTH_LONG).show();
            myAdapter.circle = 2;

        } else {
            Toast.makeText(this, "Exchange Red", Toast.LENGTH_LONG).show();
            myAdapter.circle = 0;
        }
        myAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("zhen","call onSharedPreferenceChanged: key is " + key);
        if (key.equals(getString(R.string.pref_color_key))) {
            loadColorFromPreferences(sharedPreferences);
        }
    }

    //關掉SharedPreference
    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //按返回鍵時MainActivity執行順序是onPause > onRestart > onStart
        //正常新增Guest完畢才能重新載入。
        myAdapter.swapCursor(getAllGuests());
        Log.v("brad","onRestart");
    }
}

