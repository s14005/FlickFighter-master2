package jp.ac.it_college.std.flickfighter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;


public class ResultActivity extends Activity
        implements View.OnClickListener {

    private int stageId;

    private SoundPool soundPool;
    private int buttonClickSoundId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        stageId = getIntent().getExtras().getInt(StageSelectFragment.STAGE_ID);

        //リトライ・終了ボタンのsetOnClickListener
        findViewById(R.id.button_retry).setOnClickListener(this);
        findViewById(R.id.button_to_status).setOnClickListener(this);

        TextView clearLabel = (TextView)findViewById(R.id.result_clear_text);


        //ステージ突破成功時にチャレンジフラグメントを表示する
        Bundle challengeFrag = new Bundle();
        if(getIntent().getExtras().getBoolean(BattleActivity.PREF_CLEAR_JUDGE)) {
            clearLabel.setText("CLEAR!");
            //各チャレンジの成功・失敗判定をBundleにput

            //クリアタイム
            challengeFrag.putLong(
                    ChallengeFragment.CHALLENGE_CLEAR_WITHIN_3MIN,
                    getIntent().getLongExtra(BattleActivity.PREF_CLEAR_TIME, 180 * 1000));

            //ノーダメージ
            challengeFrag.putBoolean(
                    ChallengeFragment.CHALLENGE_CLEAR_NO_DAMAGE,
                    getIntent().getBooleanExtra(BattleActivity.PREF_NO_DAMAGE, false));

            //レアモンスター撃破
            challengeFrag.putBoolean(
                    ChallengeFragment.CHALLENGE_CLEAR_RARE_CRUSHING,
                    getIntent().getBooleanExtra(BattleActivity.PREF_RARE_CRUSHING, false));

        } else {
            //ゲームオーバー時には各チャレンジに失敗フラグを建ててフラグメント表示（仕様決まり次第、要変更)

            clearLabel.setText("Failure...");
            //各チャレンジの成功・失敗判定をBundleにput
            //クリアタイム
            challengeFrag.putLong(
                    ChallengeFragment.CHALLENGE_CLEAR_WITHIN_3MIN, 180 * 1000);

            //ノーダメージ
            challengeFrag.putBoolean(
                    ChallengeFragment.CHALLENGE_CLEAR_NO_DAMAGE, false);

            //レアモンスター撃破
            challengeFrag.putBoolean(
                    ChallengeFragment.CHALLENGE_CLEAR_RARE_CRUSHING, false);


        }

        //ステージ突破成功・失敗の判定後フラグメントを切り替える
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container,
                            ChallengeFragment.newInstance(challengeFrag))
                    .commit();
        }

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        soundPool.play(buttonClickSoundId, 1.0f, 1.0f, 0, 0, 1.0f);

        switch (view.getId()) {
            case R.id.button_retry:
                intent = new Intent(this, BattleActivity.class)
                        .putExtra(StageSelectFragment.STAGE_ID, stageId);
                break;
            case R.id.button_to_status:
                intent = new Intent(this, StatusAndStageSelectActivity.class);
                break;
        }

        if(intent != null) {
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //効果音の読み込み
        buttonClickSoundId = soundPool.load(this, R.raw.se_button_click01, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //SoundPoolの開放
        soundPool.release();
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
