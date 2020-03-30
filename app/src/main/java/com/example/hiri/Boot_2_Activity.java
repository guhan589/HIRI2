package com.example.hiri;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;

public class Boot_2_Activity extends AppCompatActivity implements TextToSpeechListener {
    Button button_blind, button_elder;
    boolean state = false , blind_state = false, elder_state = false;
    private TextToSpeechClient ttsClient;
    String text="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot_2_);
        button_blind = findViewById(R.id.button_blind);
        button_elder = findViewById(R.id.button_elder);


        ttsClient = new TextToSpeechClient.Builder()     //음성 합성 포맷
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2  )     // NEWTONE_TALK_1  통합 음성합성방식   NEWTONE_TALK_2 편집 합성 방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM    )  //TTS 음색 모드 설정(여성 차분한 낭독체)
                //VOICE_MAN_READ_CALM  남성 차분한 낭독체
                //VOICE_WOMAN_DIALOG_BRIGHT  여성 밝은 대화체
                //VOICE_MAN_DIALOG_BRIGHT 남성 밝은 대화체
                .setListener(this)
                .build();

        ttsClient.stop();
        while (ttsClient.isPlaying())
            ;
        text = "시각장애인이시면 화면의 상단 버튼을,\n" +
                "노약자이시면 화면의 하단 버튼을 눌러주시기 바랍니다.";
        ttsClient.play(text);

        button_blind.setOnClickListener(new View.OnClickListener() {//시각장애인용
            @Override
            public void onClick(View v) {  //////시각장애인용 버튼
                ttsClient.stop();
                text = "시각장애인용 모드로 진입합니다.";
                while (ttsClient.isPlaying())
                    ;
                ttsClient.play(text);
                state = true;
                blind_state = true;
                elder_state = false;
            }
        });

        button_elder.setOnClickListener(new View.OnClickListener() {   /////노약자용 버튼
            @Override
            public void onClick(View v) {//////시각장애인용 버튼
                ttsClient.stop();
                text = "노약자용 모드로 진입합니다.";
                while (ttsClient.isPlaying())
                    ;
                ttsClient.play(text);
                state = false;
                blind_state = false;
                elder_state = true;

            }
        });
    }
    public void onDestroy() {
        super.onDestroy();
        if(ttsClient!=null){
            ttsClient.stop();
        }

    }

    @Override
    public void onFinished() {

        if(blind_state){//시각장애인용
            blind_state=false;
            Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
            intent.putExtra("state",state);

            startActivity(intent);
            finish();
        }else if(elder_state){//노약자용
            elder_state= false;
            Intent intent = new Intent(getApplicationContext(),HomeActivity.class);
            intent.putExtra("state",state);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onError(int code, String message) {

    }
}
