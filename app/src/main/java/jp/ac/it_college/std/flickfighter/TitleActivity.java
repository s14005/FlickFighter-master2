package jp.ac.it_college.std.flickfighter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class TitleActivity extends Activity {

    //DB
    private DatabaseHelper helper;
    public static SQLiteDatabase mDb;

    private MediaPlayer bgm;
    private Winker winker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);
        winker = new Winker(findViewById(R.id.label_start));
        bgm = MediaPlayer.create(this, R.raw.title_bgm01);
        bgm.setLooping(true);
        bgm.setVolume(0.6f, 0.6f);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // タイトル画面のSTARTの点滅
        winker.startWink();
        //BGM再生開始
        bgm.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //点滅停止
        winker.stopWink();
        //BGM一時停止
        bgm.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bgm.stop();
    }

    public void startClicked(View v) {
        /*File file = new File("/data/data/com.example.koga.sqlitetest/databases/uma.db");
        if (file.exists()) {
            file.delete();
            Log.d("kawasaki_debug", "ファイルを消去しました");
        } else {
            Log.d("kawasaki_debug", "ファイルはありません");
        }
        //ＤＢの作成
        helper = new DatabaseHelper(this);
        //CSVファイルの読み込み準備
        AssetManager as = getResources().getAssets();
        //ＤＢオープン
        mDb = helper.getWritableDatabase();
        //ＤＢ値格納変数
        ContentValues value = new ContentValues();

        try {

            BufferedReader bf = new BufferedReader(new InputStreamReader(as.open("uma.csv")));
            String s;
            while ((s = bf.readLine()) != null) {
                String[] strAry = s.split(",");
                Log.d("kawasaki_debug", "" + strAry[1] + "");
                value.put("NAME", strAry[0]);
                value.put("TEL", strAry[1]);
                mDb.insert("BOOK", null, value);
            }

        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            Log.d("kawasaki_debug", "" + e + "");
        }*/
        Intent intent = new Intent(this, StatusAndStageSelectActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            new AlertDialog.Builder(this)
                    .setTitle("確認")
                    .setMessage("ゲームを終了しますか?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
