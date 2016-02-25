package jp.ac.it_college.std.flickfighter;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class StatusFragment extends Fragment implements View.OnClickListener {

    private SharedPreferences playerStatus;
    private static final String ATTACK = "attackLevel";
    private static final String DEFENCE = "defenceLevel";
    private static final String LIFE = "lifeLevel";
    private static final String POINT = "point";


    private TextView pointView;

    private SoundPool soundPool;
    private int buttonClickSoundId;
    private int cancelSoundId;
    private int levelUpSoundId;
    private int errorSoundId;

    private View rootView;
    private Button gatyaButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_status, container, false);

        rootView.findViewById(R.id.button_levelUp_attack).setOnClickListener(this);
        rootView.findViewById(R.id.button_levelUp_defence).setOnClickListener(this);
        rootView.findViewById(R.id.button_levelUp_life).setOnClickListener(this);
        rootView.findViewById(R.id.button_stage_select).setOnClickListener(this);

        rootView.findViewById(R.id.button_stage_select).setOnClickListener(this);
       // rootView.findViewById(R.id.button_gatya).setOnClickListener(this);
        //rootView.findViewById(R.id.button_character).setOnClickListener(this);

        playerStatus = getActivity().getSharedPreferences("status", Context.MODE_PRIVATE);
        statusDisplay(); //ステータスの状態を表示
        pointView = (TextView) rootView.findViewById(R.id.point_view_label);
        //現在のポイントを表示
        pointView.setText(String.valueOf(playerStatus.getInt(POINT, 0)));
        //pointView.setText(String.valueOf(playerStatus.getInt(POINT, 0)));
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        return rootView;
    }



    @Override
    public void onClick(View view) {
        soundPool.play(buttonClickSoundId, 1.0f, 1.0f, 0, 0, 1.0f);

        switch (view.getId()) {
            case R.id.button_levelUp_attack:
                levelUp(ATTACK);
                break;
            case R.id.button_levelUp_defence:
                levelUp(DEFENCE);
                break;
            case R.id.button_levelUp_life:
                levelUp(LIFE);
                break;
            case R.id.button_stage_select:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new StageSelectFragment());
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
                break;
        }

    }

    private void levelUp(final String statusName) {
        new AlertDialog.Builder(getActivity())
                .setTitle("レベルアップ")
                .setMessage("必要ポイント: 5 \nレベルアップしますか? \n 現在のポイント：" + playerStatus.getInt(POINT, 0))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (5 <= playerStatus.getInt(POINT, 0)) {
                            int defaultValue = 0;
                            if (statusName.equals(ATTACK)) {
                                defaultValue = 1;
                            } else if (statusName.equals(DEFENCE)) {
                                defaultValue = 0;
                            } else if (statusName.equals(LIFE)) {
                                defaultValue = 5;
                            }
                            //プリファレンスに保存されている各ステータス値を加算
                            SharedPreferences.Editor editor = playerStatus.edit();
                            editor.putInt(statusName, playerStatus.getInt(statusName, defaultValue) + 1)
                                    .putInt(POINT, playerStatus.getInt(POINT, 0) - 5)
                                    .apply();
                            statusDisplay();
                            Toast.makeText(getActivity(), "レベルアップしました！", Toast.LENGTH_SHORT).show();
                            pointView.setText(String.valueOf(playerStatus.getInt(POINT,0)));
                            //レベルアップのSEを再生
                            soundPool.play(levelUpSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                        } else {
                            //エラー音を再生
                            soundPool.play(errorSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                            Toast.makeText(getActivity(), "ポイントが足りません！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //キャンセル音再生
                        soundPool.play(cancelSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                        return;
                    }
                })
                .show();
    }

    private void statusDisplay(){
        ((TextView)rootView.findViewById(R.id.status_attack_level))
                .setText(String.valueOf(playerStatus.getInt(ATTACK, 1)));
        ((TextView)rootView.findViewById(R.id.status_defence_level))
                .setText(String.valueOf(playerStatus.getInt(DEFENCE, 0)));
        ((TextView)rootView.findViewById(R.id.status_life_level))
                .setText(String.valueOf(playerStatus.getInt(LIFE, 5)));
    }

    @Override
    public void onResume() {
        super.onResume();
        //ボタン押下時の効果音を読み込み
        buttonClickSoundId = soundPool.load(getActivity(), R.raw.se_button_click01, 1);
        cancelSoundId = soundPool.load(getActivity(), R.raw.se_cancel01, 1);
        levelUpSoundId = soundPool.load(getActivity(), R.raw.se_levelup01, 1);
        errorSoundId = soundPool.load(getActivity(), R.raw.se_error01, 1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        soundPool.release();
    }
}
