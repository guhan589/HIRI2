package com.example.hiri;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;

public class find_hos_pha extends AppCompatActivity implements TextToSpeechListener {
    private TextToSpeechClient ttsClient;
    public boolean findbool1 =false, findbool2 = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_hos_pha);


        //레이아웃 개체
        Button button1,button2;
        button1  = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);

        ttsClient = new TextToSpeechClient.Builder()   //음성 합성 포맷
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2  )     // NEWTONE_TALK_1  통합 음성합성방식   NEWTONE_TALK_2 편집 합성 방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM    )  //TTS 음색 모드 설정(여성 차분한 낭독체)
                //VOICE_MAN_READ_CALM  남성 차분한 낭독체
                //VOICE_WOMAN_DIALOG_BRIGHT  여성 밝은 대화체
                //VOICE_MAN_DIALOG_BRIGHT 남성 밝은 대화체
                .setListener(this)
                .build();

        String str = "병원 약국 찾기 서비스 입니다. 화면의 세로 반을 기준으로 왼쪽 버튼은 병원찾기 이고 오른쪽 버튼은 약국찾기 입니다.";
        ttsClient.play(str);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override                           //병원 찾기
            public void onClick(View v) {
                String text = "병원 찾기 서비스 버튼을 누르셨습니다.";
                ttsClient.play(text);
                findbool1 = true;
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override                       //약국 찾기
            public void onClick(View v) {
                String text = "약국 찾기 서비스 버튼을 누르셨습니다.";
                ttsClient.play(text);
                findbool2 = true;
            }
        });
    }

    @Override
    public void onFinished() {
        int intSentSize = ttsClient.getSentDataSize();      //세션 중에 전송한 데이터 사이즈
        int intRecvSize = ttsClient.getReceivedDataSize();  //세션 중에 전송받은 데이터 사이즈

        final String strInacctiveText = "handleFinished() SentSize : " + intSentSize + "  RecvSize : " + intRecvSize;

        Log.i("find_hos_pha TAG", strInacctiveText);

        if(findbool1){
            findbool1 = false;
            Intent intent = new Intent(getApplicationContext(),findhospActivity.class);//병원 찾기 에틱비티를 이동하기 위한 인텐트 선언
            startActivity(intent);
        }else if(findbool2){
            findbool2 = false;
            Intent intent = new Intent(getApplicationContext(), findphaActivity.class);//약국 찾기 에틱비티를 이동하기 위한 인텐트 선언
            startActivity(intent);
        }
    }

    @Override
    public void onError(int code, String message) {
        Log.d("TAG","Error code:"+code +"Error message"+message);
    }
}
