package com.example.hiri;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;

public class find_hos_pha extends AppCompatActivity implements TextToSpeechListener {
    private TextToSpeechClient ttsClient;
    public boolean findbool1 =false, findbool2 = false, state = false;
    Button button1,button2;
    Intent i = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_hos_pha);

        state = getIntent().getBooleanExtra("state",false);
        Log.d("TAG", "\nnfind_hos_pha_state: "+state+"\n");
        //레이아웃 개체

        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //음성
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-kr");

        button1  = findViewById(R.id.button_hospital);
        button2 = findViewById(R.id.button_pharmacy);

        ttsClient = new TextToSpeechClient.Builder()   //음성 합성 포맷
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2  )     // NEWTONE_TALK_1  통합 음성합성방식   NEWTONE_TALK_2 편집 합성 방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM    )  //TTS 음색 모드 설정(여성 차분한 낭독체)
                //VOICE_MAN_READ_CALM  남성 차분한 낭독체
                //VOICE_WOMAN_DIALOG_BRIGHT  여성 밝은 대화체
                //VOICE_MAN_DIALOG_BRIGHT 남성 밝은 대화체
                .setListener(this)
                .build();

        String str = "병원 약국 찾기 서비스 입니다. 화면의 가로 반을 기준으로 상단 버튼은 병원찾기버튼 이고  \n  하단 버튼은 약국찾기버튼 입니다.";
        if(state)
            ttsClient.play(str);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override                           //병원 찾기
            public void onClick(View v) {

                String text = "병원 찾기 서비스 버튼을 누르셨습니다.";
                if(state){
                    ttsClient.stop();

                    ttsClient.play(text);
                    findbool1 = true;
                    findbool2 = false;

                }else{
                    Intent intent = new Intent(getApplicationContext(),findhospActivity.class);//병원 찾기 에틱비티를 이동하기 위한 인텐트 선언
                    intent.putExtra("state",state);
                    startActivity(intent);
                }


            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override                       //약국 찾기
            public void onClick(View v) {

                String text = "약국 찾기 서비스 버튼을 누르셨습니다.";
                if(state ) {
                    ttsClient.stop();
                    ttsClient.play(text);
                    findbool1 = false;
                    findbool2 = true;
                }else{
                    Intent intent = new Intent(getApplicationContext(), findphaActivity.class);//약국 찾기 에틱비티를 이동하기 위한 인텐트 선언
                    intent.putExtra("state",state);
                    startActivity(intent);
                }

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
            intent.putExtra("state",state);
            startActivity(intent);
        }else if(findbool2){
            findbool2 = false;
            Intent intent = new Intent(getApplicationContext(), findphaActivity.class);//약국 찾기 에틱비티를 이동하기 위한 인텐트 선언
            intent.putExtra("state",state);
            startActivity(intent);
        }
    }

    @Override
    public void onError(int code, String message) {
        Log.d("TAG","Error code:"+code +"Error message"+message);
    }

    // 더 이상 쓰지 않는 경우에는 다음과 같이 해제
    public void onDestroy() {
        super.onDestroy();
        if(ttsClient!=null){
            ttsClient.stop();
        }

    }

}
