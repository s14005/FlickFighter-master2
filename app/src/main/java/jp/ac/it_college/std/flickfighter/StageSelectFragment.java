package jp.ac.it_college.std.flickfighter;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class StageSelectFragment extends Fragment implements View.OnClickListener {

    private static final String STAGE_CLEAR = "stage_clear";
    public static final String STAGE_ID = "stageId";
    private ImageView clearLabel;
    private SharedPreferences playerStatus;

    private SoundPool soundPool;
    private int buttonClickSoundId;
    private int cancelSoundId;
    private int stageSelectSoundId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stage_select, container, false);

        playerStatus = getActivity().getSharedPreferences("status", Context.MODE_PRIVATE);

        //img_clear_labelをステージの数だけ回してIDをそれぞれ取得し、クリアしてれば画像を表示
        for (int stageId = 1; stageId <= 10; stageId++) {
            int viewId = getResources().getIdentifier("img_clear_label" + stageId, "id", getActivity().getPackageName());
            clearLabel = (ImageView) view.findViewById(viewId);

            if (playerStatus.getBoolean(stageId + STAGE_CLEAR, false)) {
                if (playerStatus.getBoolean(stageId + BattleActivity.PREF_CLEAR_TIME, false)
                        && playerStatus.getBoolean(stageId + BattleActivity.PREF_NO_DAMAGE, false)
                        && playerStatus.getBoolean(stageId + BattleActivity.PREF_RARE_CRUSHING, false)) {
                    clearLabel.setImageResource(R.drawable.clear1);
                } else {
                    clearLabel.setImageResource(R.drawable.clear2);
                }
            }
        }

        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);

        view.findViewById(R.id.button_to_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                soundPool.play(buttonClickSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, new StatusFragment());
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }
        });

        view.findViewById(R.id.button_stage1).setOnClickListener(this);
        view.findViewById(R.id.button_stage2).setOnClickListener(this);
        view.findViewById(R.id.button_stage3).setOnClickListener(this);
        view.findViewById(R.id.button_stage4).setOnClickListener(this);
        view.findViewById(R.id.button_stage5).setOnClickListener(this);
        view.findViewById(R.id.button_stage6).setOnClickListener(this);
        view.findViewById(R.id.button_stage7).setOnClickListener(this);
        view.findViewById(R.id.button_stage8).setOnClickListener(this);
        view.findViewById(R.id.button_stage9).setOnClickListener(this);
        view.findViewById(R.id.button_stage10).setOnClickListener(this);

        return view;
    }

    public void goToBattle(final int stageId, String stageName){
        final Intent intent = new Intent(getActivity(), BattleActivity.class);
        new AlertDialog.Builder(getActivity())
                .setTitle("ステージセレクト")
                .setMessage(stageName + "に挑戦しますか?")
                .setPositiveButton("挑戦", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        soundPool.play(stageSelectSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                        intent.putExtra(STAGE_ID, stageId);
                        startActivity(intent);
                        getActivity().finish();
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        soundPool.play(cancelSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                        return;
                    }
                }).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        //効果音の読み込み
        buttonClickSoundId = soundPool.load(getActivity(), R.raw.se_button_click01, 1);
        cancelSoundId = soundPool.load(getActivity(), R.raw.se_cancel01, 1);
        stageSelectSoundId = soundPool.load(getActivity(), R.raw.se_stageselect01, 1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //SoundPoolの開放
        soundPool.release();
    }

    @Override
    public void onClick(View view) {
        soundPool.play(buttonClickSoundId, 1.0f, 1.0f, 0, 0, 1.0f);

        switch (view.getId()) {
            case R.id.button_stage1:
                goToBattle(1, (String)((TextView)view).getText());
                break;
            case R.id.button_stage2:
                goToBattle(2, (String)((TextView)view).getText());
                break;
            case R.id.button_stage3:
                goToBattle(3, (String)((TextView)view).getText());
                break;
            case R.id.button_stage4:
                goToBattle(4, (String)((TextView)view).getText());
                break;
            case R.id.button_stage5:
                goToBattle(5, (String)((TextView)view).getText());
                break;
            case R.id.button_stage6:
                goToBattle(6, (String)((TextView)view).getText());
                break;
            case R.id.button_stage7:
                goToBattle(7, (String)((TextView)view).getText());
                break;
            case R.id.button_stage8:
                goToBattle(8, (String)((TextView)view).getText());
                break;
            case R.id.button_stage9:
                goToBattle(9, (String)((TextView)view).getText());
                break;
            case R.id.button_stage10:
                goToBattle(10, (String)((TextView)view).getText());
                break;
        }
    }
}
