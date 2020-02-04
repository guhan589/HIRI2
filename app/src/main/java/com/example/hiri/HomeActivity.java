package com.example.hiri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;
import com.kakao.sdk.newtoneapi.TextToSpeechManager;

public class HomeActivity extends AppCompatActivity implements TextToSpeechListener {
    private TextToSpeechClient ttsClient;
    public boolean findbool1 = false, findbool2 = false, weatherbool = false, sosbool = false;
    final int PERMISSION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //레이아웃 버튼 객체
        Button findbutton1, findbutton2, weatherbutton, sosbutton;
        findbutton1 = findViewById(R.id.button1);
        findbutton2 = findViewById(R.id.button2);
        weatherbutton = findViewById(R.id.button3);
        sosbutton =  findViewById(R.id.button4);

        //내부변수
        int buttoncount1=0, buttoncount2 = 0, buttoncount3 = 0, buttoncount4 = 0;


        if (Build.VERSION.SDK_INT >= 23) {      //퍼미션 권한 부여
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CALL_PHONE}, PERMISSION);
        }//퍼미션접근 권한

        TextToSpeechManager.getInstance().finalizeLibrary();
        ttsClient = new TextToSpeechClient.Builder()
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2  )     // NEWTONE_TALK_1  통합 음성합성방식   NEWTONE_TALK_2 편집 합성 방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM    )  //TTS 음색 모드 설정(여성 차분한 낭독체)
                //VOICE_MAN_READ_CALM  남성 차분한 낭독체
                //VOICE_WOMAN_DIALOG_BRIGHT  여성 밝은 대화체
                //VOICE_MAN_DIALOG_BRIGHT 남성 밝은 대화체
                .setListener(this)
                .build();

        findbutton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = "약품 찾기 서비스를 버튼을 누르셨습니다.";
                ttsClient.play(str);

            }
        });
        findbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String str = "병원 및 약국 찾기 서비스 버튼을 누르셨습니다.";
                ttsClient.play(str);
                findbool1 = false;
                findbool2 = true;
                weatherbool = false;
                sosbool = false;

            }
        });
        weatherbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str =  "날씨 정보 서비스 버튼을 누르셨습니다.";
                ttsClient.play(str);
            }
        });
        sosbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = "SOS 요청 서비스 버튼을 누르셨습니다.";
                ttsClient.play(str);

            }
        });
    }

    @Override
    public void onFinished() {//음성 합성이 모두 완료했을 때 발생하는 메소드
        int intSentSize = ttsClient.getSentDataSize();      //세션 중에 전송한 데이터 사이즈
        int intRecvSize = ttsClient.getReceivedDataSize();  //세션 중에 전송받은 데이터 사이즈

        final String strInacctiveText = "handleFinished() SentSize : " + intSentSize + "  RecvSize : " + intRecvSize;

        Log.i("HomeActivity TAG", strInacctiveText);
        if(findbool1){

        }
        else if(findbool2){
            findbool2 = false;
            Intent intent = new Intent(getApplicationContext(), find_hos_pha.class);

            startActivity(intent);
        }
        else if(weatherbool){

        }
        else if(sosbool){

        }
    }

    @Override
    public void onError(int code, String message) {//음성 합성 에러 메소드
        Log.d("TAG","Error code:"+code +"Error message"+message);
    }
}

