package com.example.hiri;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;

public class select_weatherActivity extends AppCompatActivity implements TextToSpeechListener {
    TextToSpeechClient ttsClient;
    String text;
    boolean state, bool_weather1,bool_weather2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_weather);
        Button button1, button2;
        button1 = findViewById(R.id.button_my_weather);
        button2 = findViewById(R.id.button_total_weather);

        state = getIntent().getBooleanExtra("state",false);
        ttsClient = new TextToSpeechClient.Builder()
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2  )     // NEWTONE_TALK_1  통합 음성합성방식   NEWTONE_TALK_2 편집 합성 방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM    )  //TTS 음색 모드 설정(여성 차분한 낭독체)
                //VOICE_MAN_READ_CALM  남성 차분한 낭독체
                //VOICE_WOMAN_DIALOG_BRIGHT  여성 밝은 대화체
                //VOICE_MAN_DIALOG_BRIGHT 남성 밝은 대화체
                .setListener(this)
                .build();

        ttsClient.stop();
        while(ttsClient.isPlaying())
            ;

        text = "날씨 정보입니다. 상단 버튼에는 현재 위치 날씨버튼이며 \n 하단 버튼은 전국 날씨 중기 예보입니다.";
        ttsClient.play(text);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//동네날씨
                ttsClient.stop();
                while(ttsClient.isPlaying())
                    ;
                text = "우리 동네 날씨 서비스 버튼을 선택하셨습니다.";
                ttsClient.play(text);
                bool_weather1 = true;
                bool_weather2 = false;

            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               ttsClient.stop();
                while(ttsClient.isPlaying())
                    ;
                text = "전국 중기예보 날씨 서비스 버튼을 선택하셨습니다.";
                ttsClient.play(text);
                bool_weather1 = false;
                bool_weather2 = true;
            }
        });


    }

    @Override
    public void onFinished() {
        if(bool_weather1){
            bool_weather1 = false;
            Intent intent = new Intent(getApplicationContext(),show_myweatherActivity.class);
            intent.putExtra("state",state);
            startActivity(intent);
        }else if(bool_weather2){
            bool_weather2 = false;
            Intent intent = new Intent(getApplicationContext(),show_totalweatherActivity.class);
            intent.putExtra("state",state);
            startActivity(intent);
        }
    }

    @Override
    public void onError(int code, String message) {

    }
}
