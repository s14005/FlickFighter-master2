package jp.ac.it_college.std.flickfighter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class BattleActivity extends Activity
        implements TextWatcher, LimitTimeSurfaceView.EnemyActionListener
        , DetectableKeyboard.OnKeyboardVisibilityListener {


    private DatabaseHelper helper;
    public static SQLiteDatabase mDb;
    private static final String POINT = "point";
    private int playerPow;
    private int playerDefence;
    private int playerLife;
    private InputMethodManager inputMethodManager;
    private EditText userInputText;
    private LinearLayout textBox;
    private String text;
    private LimitTimeSurfaceView limitTimeSurfaceView;
    private SurfaceView limitTimeBar;
    private Handler mHandler;
    //Timer初期化
    private TextView timerLabel;
    //intentで渡すフィールド
    private int stageId;
    private long currentTime = 0;
    //getIntentで使う定数
    public static final String PREF_RARE_CRUSHING = "rare_crushing";
    public static final String PREF_CLEAR_TIME = "clear_time";
    public static final String PREF_NO_DAMAGE = "no_damage";
    public static final String PREF_CLEAR_JUDGE = "clearJudge";
    private ProgressBar playerLifeGauge;
    //フラグいろいろ
    private boolean rareFrag = false;
    private boolean no_damage = true;
    //TODO:変数名要変更
    private int maxBattleCount = 5;
    private int battleCount = 0;
    private TextView battleCountView;

    private TextView enemyString;
    private int enemyLife;
    private int enemyPow;
    private ProgressBar enemyLifeGauge;
    private ImageView enemyImage;
    //キーボード表示ボタン
    private Button mKeyBoardShownButton;

    //タイマー用フィールド
    private Timer timer;
    private TimerTask timerTask;

    //効果音用
    private SoundPool se;
    private int damageSoundId;
    private int attackSoundId;
    private int enemyAdventSoundId;
    private int bossAdventSoundId;
    private int enemyDownSoundId;
    private int gameClearSoundId;
    private int gameOverSoundId;

    //BGM用
    private MediaPlayer battleBgm;
    private MediaPlayer bossBgm;

    private boolean gameIsRunning;

    //ボス出現時の警告メッセージ用メッセージと点滅用Winker
    private Winker messageWinker;
    private TextView messageTextView;
    private TextView bossAdventMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        stageId = getIntent().getExtras().getInt(StageSelectFragment.STAGE_ID);
        SharedPreferences playerStatus = getSharedPreferences("status", MODE_PRIVATE);
        playerPow = playerStatus.getInt("attackLevel", 1);
        playerDefence = playerStatus.getInt("defenceLevel", 0);
        playerLife = playerStatus.getInt("lifeLevel", 5);

        //プレイヤーの体力ゲージ表示
        playerLifeGauge = (ProgressBar) findViewById(R.id.player_life_gauge);
        playerLifeGauge.setMax(playerLife);
        playerLifeGauge.setProgress(playerLife);

        battleCountView = (TextView) findViewById(R.id.battle_count);
        battleCountView.setText(battleCount + " / " + maxBattleCount);

        //Timer表示
        timerLabel = (TextView) findViewById(R.id.timer_label);

        textBox = (LinearLayout) findViewById(R.id.text_box);

        mHandler = new Handler(getMainLooper());
        limitTimeBar = (SurfaceView) findViewById(R.id.limit_time_bar);
        limitTimeSurfaceView = new LimitTimeSurfaceView(limitTimeBar, mHandler, this);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        enemyString = (TextView) findViewById(R.id.enemyString);
        userInputText = (EditText) findViewById(R.id.userInputText);
        userInputText.addTextChangedListener(this);

        //敵キャラ表示
        enemyLifeGauge = (ProgressBar) findViewById(R.id.enemy_life_gauge);
        enemyImage = (ImageView) findViewById(R.id.enemy_image);
        enemyStringView();
        enemySummon();

        //キーボードの表示・非表示を検出するリスナーをセット
        new DetectableKeyboard(this).setKeyboardListener(this);

        //キーボード表示ボタンのonClickListener
        mKeyBoardShownButton = (Button) findViewById(R.id.keyBoardShownButton);
        mKeyBoardShownButton
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        keyBoardShown();
                    }
                });

        //効果音再生用SoundPool
        se = new SoundPool(7, AudioManager.STREAM_MUSIC, 0);

        //戦闘曲用MediaPlayer
        battleBgm = MediaPlayer.create(this, R.raw.battle_bgm);
        battleBgm.setLooping(true);
        battleBgm.setVolume(0.3f, 0.3f);
        battleBgm.start();

        //ボス用のBGM
        bossBgm = MediaPlayer.create(this, R.raw.boss_bgm01);
        bossBgm.setLooping(true);
        bossBgm.setVolume(0.3f, 0.3f);

        //メッセージと点滅用のWinker
        messageTextView = (TextView) findViewById(R.id.text_message);
        bossAdventMessage = (TextView) findViewById(R.id.text_boss_advent_message);
        messageWinker = new Winker(bossAdventMessage);

        //pri
        //playerStatus = getActivity().getSharedPreferences("status", Context.MODE_PRIVATE);

        gameReady();
    }

    public static String randomWordView(int i) {
        String sql = "SELECT * FROM BOOK ORDER BY RANDOM() LIMIT 1";
        Cursor c = mDb.rawQuery(sql, null);
        boolean isEof = c.moveToFirst();
        /*while (isEof) {
            String.format(c.getString(i));
        isEof = c.moveToNext();
        }*/
        return String.format(c.getString(i));
    }
    public static String bossWordView(int i) {

        //String text = "";
        String sql = "SELECT * FROM BOOK ORDER BY RANDOM() LIMIT 1";
        Cursor c = mDb.rawQuery(sql, null);
        boolean isEof = c.moveToFirst();
        /*while (isEof) {
         String.format(c.getString(i));
        isEof = c.moveToNext();
        }*/
        return String.format(c.getString(i));
    }


    private void gameReady() {
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(3000);
        animation.setFillAfter(true);
        enemyImage.startAnimation(animation);

        //効果音の読み込み
        damageSoundId = se.load(this, R.raw.se_damage01, 1);
        attackSoundId = se.load(this, R.raw.se_attack01, 1);
        enemyAdventSoundId = se.load(this, R.raw.se_enemyadvent01, 1);
        bossAdventSoundId = se.load(this, R.raw.se_bossadvent01, 1);
        enemyDownSoundId = se.load(this, R.raw.se_enemydown01, 1);
        gameClearSoundId = se.load(this, R.raw.se_gameclear01, 1);
        gameOverSoundId = se.load(this, R.raw.se_gameover01, 1);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //ゲーム開始メッセージのアニメーションを開始
                messageTextView.setTextColor(Color.WHITE);
                messageTextView.setText("Ready?");
                messageTextView.setVisibility(View.VISIBLE);

                ScaleAnimation scaleAnimation = new ScaleAnimation(
                        0.5f, 1.5f, 0.5f, 1.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(2000);


                AnimationSet startAnimationSet = new AnimationSet(false);
                startAnimationSet.addAnimation(scaleAnimation);
                startAnimationSet.setFillAfter(true);
                messageTextView.startAnimation(startAnimationSet);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                messageTextView.setText("GO!");

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //開始メッセージを非表示にする
                        messageTextView.setText("");
                        messageTextView.setVisibility(View.INVISIBLE);

                        // タイマーをセット
                        timer = new Timer();
                        timerTask = new Task1();
                        timer.scheduleAtFixedRate(timerTask, 0, 100);
                        gameStart();
                    }
                }, 500);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        //BGM一時停止
        battleBgm.pause();
        bossBgm.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //SoundPoolの開放
        se.release();
        helper.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDb = helper.getWritableDatabase();
        if (battleCount < maxBattleCount) {
            battleBgm.start();
        } else {
            bossBgm.start();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //バトル画面のレイアウトサイズを取得
        RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.root_layout);

        // 敵の画像などの表示位置を端末のウィンドウサイズに合わせて変更
        int marginHeight = rootLayout.getHeight() / 4;
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) findViewById(R.id.enemy_image)
                        .getLayoutParams();
        layoutParams.setMargins(0, marginHeight, 0, 0);

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //エンターキー押下時の処理を無効にする
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            gameStop();

            new AlertDialog.Builder(this)
                    .setTitle("確認")
                    .setMessage("ステータス画面へ戻りますか?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(getApplicationContext(), StatusAndStageSelectActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            gameStart();
                            keyBoardShown();
                        }
                    }).show();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void gameStart() {
        gameIsRunning = true;
        textBox.setVisibility(View.VISIBLE);
        enemyLifeGauge.setVisibility(View.VISIBLE);
        playerLifeGauge.setVisibility(View.VISIBLE);
        limitTimeBar.setVisibility(View.VISIBLE);
        userInputText.requestFocus();
        limitTimeSurfaceView.startMeasurement();
    }

    public void gameStop() {
        gameIsRunning = false;
        textBox.setVisibility(View.INVISIBLE);
        enemyLifeGauge.setVisibility(View.INVISIBLE);
        playerLifeGauge.setVisibility(View.INVISIBLE);
        limitTimeBar.setVisibility(View.INVISIBLE);
        limitTimeSurfaceView.stopMeasurement();
    }

    private void gameEnd(final boolean isGameClear) {
        gameStop();
        battleBgm.stop();
        bossBgm.stop();
        timer.cancel();

        //ゲームオーバーのアニメーション
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.f, 1.f);

        TranslateAnimation moveDownAnimation =
                new TranslateAnimation(0, 0, -200, 0);

        final AnimationSet gameOverAnimation = new AnimationSet(true);
        gameOverAnimation.setFillAfter(true);
        gameOverAnimation.setDuration(2000);
        gameOverAnimation.addAnimation(alphaAnimation);
        gameOverAnimation.addAnimation(moveDownAnimation);

        //ゲームオーバー用BGM
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (isGameClear) {
                    se.play(gameClearSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                    messageTextView.setTextColor(Color.YELLOW);
                    messageTextView.setText("CLEAR!");
                } else {
                    //SharedPreferences playerStatus = getSharedPreferences("status", MODE_PRIVATE);
                    //SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
                    //SharedPreferences.Editor editor = playerStatus.edit();
                    //editor.putInt(POINT, playerStatus.getInt(POINT, 0) + 10);
                    //editor.putInt("LevelSave", 0);
                    //editor.apply();
                    //SharedPreferences.Editor editor = playerStatus.edit();
                    //editor.putInt(PO, playerStatus.getInt(PO, 0) + 10);
                    // SharedPreferences playerStatus = getSharedPreferences("point", Context.MODE_PRIVATE);
                    // SharedPreferences.Editor editor = playerStatus.edit();
                    //editor.putInt(POINT, playerStatus.getInt(POINT, 0) + 10);
                    //editor.apply();
                    //editor.commit();
                    se.play(gameOverSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                    messageTextView.setTextColor(Color.GRAY);
                    messageTextView.setText("GAME OVER");
                }

                messageTextView.setVisibility(View.VISIBLE);
                messageTextView.startAnimation(gameOverAnimation);
            }
        }, 1000);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goToResult(isGameClear);
            }
        }, 5000);

    }

    public void goToResult(boolean clear) {
        Intent intent = new Intent(this, ResultActivity.class)
                .putExtra(PREF_CLEAR_JUDGE, clear)
                .putExtra(StageSelectFragment.STAGE_ID, stageId)
                .putExtra(PREF_CLEAR_TIME, currentTime)
                .putExtra(PREF_NO_DAMAGE, no_damage)
                .putExtra(PREF_RARE_CRUSHING, rareFrag);
        startActivity(intent);
        finish();
    }

    public void keyBoardShown() {
        inputMethodManager.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
    }

    public void enemyStringView() {

        File file = new File("/data/data/com.example.koga.sqlitetest/databases/uma.db");
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
        }

        if (battleCount == maxBattleCount) {
            text = /*EnemyInfo.*/bossWordView(stageId);
            enemyString.setText(text);
        } else {
            //敵の文字列を表示
            text = /*EnemyInfo.*/randomWordView(stageId);
            enemyString.setText(text);
        }
    }

    public void enemySummon() {
        battleCount++;
        battleCountView.setText(battleCount + " / 5");

        int enemyId;
        enemyImage.setImageResource(
                EnemyInfo.enemyPath[enemyId = EnemyInfo.randomEnemySummons(EnemyInfo.enemyPath.length)]);

        if (enemyId == 0) {
            rareFrag = true;
        }
        enemyLife = EnemyInfo.enemyLifeSetting(enemyId);
        enemyPow = EnemyInfo.enemyPowSetting(enemyId);

        enemyLifeGauge.setMax(enemyLife);
        enemyLifeGauge.setProgress(enemyLife);
    }

    public void bossSummon() {
        battleCount++;
        battleCountView.setText(battleCount + " / 5");

        ImageView bossImage = (ImageView) findViewById(R.id.enemy_image);

        int bossId = stageId - 1;
        bossImage.setImageResource(EnemyInfo.bossPath[bossId]);
        enemyLife = EnemyInfo.bossLifeSetting(bossId);
        enemyPow = EnemyInfo.bossPowSetting(bossId);

        enemyLifeGauge.setMax(enemyLife);
        enemyLifeGauge.setProgress(enemyLife);
    }

    public void enemyAttackAnimation(View view) {
        ScaleAnimation animation = new ScaleAnimation(
                1, 2, 1, 2,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(100);

        view.startAnimation(animation);
    }

    private void enemyDamageAnimation(View view) {
        TranslateAnimation translateAnimation =
                new TranslateAnimation(-20, 20, 0, 0);

        translateAnimation.setRepeatMode(Animation.REVERSE);
        translateAnimation.setDuration(100);

        view.startAnimation(translateAnimation);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //入力された文字の長さがenemyStringより長い場合はメソッドを抜ける
        if (s.length() > enemyString.length()) {
            return;
        }

        if (gameIsRunning) {
            if (enemyString.getText().toString().substring(0, s.length())
                    .equals(s.toString().substring(0, s.length()))) {
                if (enemyString.getText().length() == s.length()) {
                    //全部打ち終わったら文字を切り替える
                    userInputText.setText("");

                    //プレイヤー側の攻撃処理
                    enemyLifeGauge.setProgress(enemyLife -= playerPow);

                    //攻撃音を再生
                    se.play(attackSoundId, 1.0f, 1.0f, 0, 0, 1.0f);

                    if (enemyLife <= 0) {
                        //敵撃破アニメーション
                        enemyCrushingAnimation(enemyImage);
                    } else {
                        //敵がダメージを負った時のアニメーション
                        enemyDamageAnimation(enemyImage);
                    }

                    enemyStringView();

                    // リミットタイムをリセットする
                    limitTimeSurfaceView.resetLimitTime();
                } else {
                    //文字列が一致すれば色を変える
                    String txtStr = "<font color=#00ff00>" + text.substring(0, s.length()) +
                            "</font>" + text.substring(s.length(), text.length());
                    enemyString.setText(Html.fromHtml(txtStr));
                }
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void enemyAttack() {
        no_damage = false;

        enemyAttackAnimation(findViewById(R.id.enemy_image));

        playerLife -= (enemyPow - playerDefence) >= 0 ? enemyPow - playerDefence : 0;

        if (playerLife <= 0) {


            //ゲームオーバー時の処理


            gameEnd(false);
        }

        playerLifeGauge.setProgress(playerLife);

        //ダメージ音再生
        se.play(damageSoundId, 1.0f, 1.0f, 0, 0, 1.0f);

    }

    private void enemyCrushingAnimation(final View view) {
        gameStop();

        TranslateAnimation iterateAnimation = new TranslateAnimation(
                -30, 30, 0, 0);
        iterateAnimation.setRepeatMode(Animation.REVERSE);
        iterateAnimation.setRepeatCount(4);
        iterateAnimation.setDuration(250);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.f, 0.f);
        alphaAnimation.setDuration(1000);

        TranslateAnimation moveDownAnimation = new TranslateAnimation(
                0, 0, 0, 100);
        moveDownAnimation.setDuration(1000);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(iterateAnimation);
        animationSet.addAnimation(moveDownAnimation);
        animationSet.setFillAfter(true);
        view.startAnimation(animationSet);

        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //敵撃破時のSEを再生
                se.play(enemyDownSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (battleCount < maxBattleCount - 1) {
                    enemySummon();
                } else if (battleCount == maxBattleCount - 1) {
                    bossSummon();
                } else {

                    SharedPreferences playerStatus = getSharedPreferences("status", MODE_PRIVATE);
                    //SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = playerStatus.edit();
                    if(stageId == 1){
                        editor.putInt(POINT, playerStatus.getInt(POINT, 0) + 10);
                    }else if(stageId == 2){
                        editor.putInt(POINT, playerStatus.getInt(POINT, 0) + 100);
                    }else if(stageId == 3){
                        editor.putInt(POINT, playerStatus.getInt(POINT, 0) + 1000);
                    }else {
                    }
                    //editor.putInt(POINT, playerStatus.getInt(POINT, 0) + 10);
                    //editor.putInt("LevelSave", 0);
                    editor.apply();
                    //ゲームクリア時の処理
                    gameEnd(true);
                    return;
                }
                readyAnimation(view);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void readyAnimation(View view) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.f, 1.f);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, 0);

        AnimationSet animationSet = new AnimationSet(true);
        //雑魚敵かボスかで登場時間を変える
        if (battleCount < maxBattleCount) {
            animationSet.setDuration(1000);
        } else {
            animationSet.setDuration(3000);
        }
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(translateAnimation);

        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //敵登場用SE
                if (battleCount < maxBattleCount) {
                    //雑魚敵が出てきた時の効果音
                    se.play(enemyAdventSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                } else {
                    //戦闘BGMを停止しボス出現時のSEを再生
                    battleBgm.stop();
                    //警告メッセージを表示
                    bossAdventMessage.setVisibility(View.VISIBLE);
                    messageWinker.startWink();

                    se.play(bossAdventSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (battleCount == maxBattleCount) {
                    //ボス用のBGMを再生開始
                    bossBgm.start();
                    //警告メッセージを非表示にする
                    bossAdventMessage.setVisibility(View.INVISIBLE);
                    messageWinker.stopWink();
                }
                gameStart();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(animationSet);
    }

    /** implemented OnKeyboardVisibilityListener */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onVisibilityChanged(boolean isVisible) {
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) playerLifeGauge.getLayoutParams();

        if (isVisible) {
            /* キーボード表示時の処理をここに書く */

            //プレイヤーライフの表示位置固定を解除
            layoutParams.removeRule(RelativeLayout.ABOVE);
        } else {
            /* キーボード非表示時の処理をここに書く */

            //プレイヤーライフゲージの表示位置をキーボード表示ボタンの上に固定
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.keyBoardShownButton);
        }
    }

    @Override
    public void onSuggestVisibilityChanged(int marginHeight) {
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) playerLifeGauge.getLayoutParams();
        layoutParams.setMargins(0, marginHeight - playerLifeGauge.getHeight(), 0, 0);
    }

    public class Task1 extends TimerTask {

        private Handler handler;
        private long startTime;

        public Task1() {
            handler = new Handler();
            startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    currentTime = System.currentTimeMillis() - startTime;
                    String elapsedTime = String.format("%02d:%02d",
                            currentTime / 60000, currentTime % 60000 / 1000);
                    timerLabel.setText(String.valueOf(elapsedTime));
                }
            });
        }
    }
}