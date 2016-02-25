package jp.ac.it_college.std.flickfighter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class ChallengeFragment extends Fragment
        implements ViewSwitcher.ViewFactory{
    public static final String CHALLENGE_CLEAR_WITHIN_3MIN = "clear_within_3min";
    public static final String CHALLENGE_CLEAR_NO_DAMAGE = "no_damage";
    public static final String CHALLENGE_CLEAR_RARE_CRUSHING = "rare_crushing";
    private static final String STAGE_CLEAR = "stage_clear";
    private TextView text;
    private boolean mChallenge1;
    private boolean mChallenge2;
    //private boolean mChallenge3;
    private View rootVie;
    private SharedPreferences a;
    private SharedPreferences playerStatus;
    private SharedPreferences.Editor editor;

    private int stageId;
    public static final String PREF_POINT = "point";
    //規定時間
    public static final long STIPULATED_TIME = 180 * 1000;
    private int[] img =  {
            R.drawable.first_clear
            ,R.drawable.failure
            ,R.drawable.clear2
    };
    private ImageSwitcher challenge1, challenge2, challenge3;

    public static ChallengeFragment newInstance(Bundle frags) {
        ChallengeFragment fragment = new ChallengeFragment();
        fragment.setArguments(frags);
        return fragment;
    }

    public ChallengeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.fragment_challenge);
        playerStatus = getActivity().getSharedPreferences("status", Context.MODE_PRIVATE);
        editor = playerStatus.edit();
        stageId = getActivity().getIntent().getExtras().getInt(StageSelectFragment.STAGE_ID);
        editor.putBoolean(stageId + STAGE_CLEAR, true).apply();
        if (getArguments() != null) {
            mChallenge1 = getArguments().getLong(CHALLENGE_CLEAR_WITHIN_3MIN) < STIPULATED_TIME;
            mChallenge2 = getArguments().getBoolean(CHALLENGE_CLEAR_NO_DAMAGE);

      //      mChallenge3 = getArguments().getBoolean(CHALLENGE_CLEAR_RARE_CRUSHING);
        }

        // intentから指定キーの文字列を取得する
        //okane = (TextView) rootVie.findViewById(R.id.okane);
        //現在のポイントを表示
        //okane.setText(""+10);
        //setContextView(R.layout.fragment_challenge);
        //TextView okane = (TextView)findViewById(R.id.okane);
        //okane.setText("" + 10);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_challenge, container, false);
        challenge1 = (ImageSwitcher) view.findViewById(R.id.challenge_clear1);
        challenge1.setFactory(this);
        challenge2 = (ImageSwitcher) view.findViewById(R.id.challenge_clear2);
        challenge2.setFactory(this);

        //TextView okane = (TextView)findViewById(R.id.okane);
        //okane.setText("" + 10);

        // 現在のintentを取得する

        //Intent intent = getExtras();
        //int result = intent.getIntExtra( "cul",0);
        //SharedPreferences prefs
     //   SharedPreferences prefs = /*getActivity().*/getSharedPreferences("HANTEI", Context.MODE_PRIVATE);
      //  int han = prefs.getInt("HAN", 0);

        //if (han == 1) {

        ImageView imageView1 = (ImageView) view.findViewById(R.id.imageView);
        imageView1.setImageResource(R.drawable.kane);

            text = (TextView) view.findViewById(R.id.okane);
            if (stageId == 1) {
                text.setText("10コインGET");
                //text.setTextSize(50.0f);
            } else if (stageId == 2) {
                text.setText("100コインGET");
                //text.setTextSize(20.0f);
            } else if (stageId == 3) {
                text.setText("1000コインGET");
                //text.setTextSize(30.0f);
            } else {
            }
//        }else {
  //

    //    challenge3 = (ImageSwitcher) view.findViewById(R.id.challenge_clear3);
    //    challenge3.setFactory(this);

        checkChallenge();
        return view;
    }



    private void checkChallenge() {
        if (mChallenge1) {
            challenge1.setImageResource(img[2]);
            if (!playerStatus.getBoolean(stageId + BattleActivity.PREF_CLEAR_TIME, false)) {
                challenge1.setImageResource(img[0]);
                editor.putInt(PREF_POINT, playerStatus.getInt(PREF_POINT, 0) + 1)
                        .putBoolean(stageId + BattleActivity.PREF_CLEAR_TIME, true)
                        .apply();
            }
        } else {
            challenge1.setImageResource(img[1]);
        }

        if (mChallenge2) {
            challenge2.setImageResource(img[2]);
            if(!playerStatus.getBoolean(stageId + BattleActivity.PREF_NO_DAMAGE, false)) {
                challenge2.setImageResource(img[0]);
                editor.putInt(PREF_POINT, playerStatus.getInt(PREF_POINT, 0) + 1)
                        .putBoolean(stageId + BattleActivity.PREF_NO_DAMAGE, true)
                        .apply();
            }
        } else {
            challenge2.setImageResource(img[1]);
        }

        /*if (mChallenge3) {
            challenge3.setImageResource(img[2]);
            if (!playerStatus.getBoolean(stageId + BattleActivity.PREF_RARE_CRUSHING, false)) {
                challenge3.setImageResource(img[0]);
                editor.putInt(PREF_POINT, playerStatus.getInt(PREF_POINT, 0) + 1)
                        .putBoolean(stageId + BattleActivity.PREF_RARE_CRUSHING, true)
                        .apply();
            }
        } else {
            challenge3.setImageResource(img[1]);
        }*/

    }

    @Override
    public View makeView() {
        // ApiDemos->Views->ImageSwitcherのソースからメソッドを丸々コピー
        ImageView i = new ImageView(getActivity().getApplicationContext());
        i.setBackgroundColor(0xFF000000);
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(
                Gallery.LayoutParams.MATCH_PARENT, Gallery.LayoutParams.MATCH_PARENT));
        return i;
    }


}
