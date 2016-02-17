package jp.ac.it_college.std.flickfighter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;

public class StatusAndStageSelectActivity extends Activity {
    private MediaPlayer bgm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_and_stage_select);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,
                            new StatusFragment())
                    .commit();
        }

        bgm = MediaPlayer.create(this, R.raw.status_and_stageselect_bgm01);
        bgm.setLooping(true);
        bgm.setVolume(0.3f, 0.3f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //BGM再生開始
        bgm.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //BGM再生停止
        bgm.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //MediaPlayerの開放
        bgm.release();
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
