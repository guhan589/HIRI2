package com.example.hiri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;
import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;
import com.kakao.sdk.newtoneapi.TextToSpeechManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BootActivity extends AppCompatActivity implements TextToSpeechListener {
    private TextToSpeechClient ttsClient;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot);


        TextToSpeechManager.getInstance().initializeLibrary(getApplicationContext());//tts 초기화
        SpeechRecognizerManager.getInstance().initializeLibrary(getApplicationContext());//stt 초기화
        ProgressBar progressBar = findViewById(R.id.progressBar2);
        progressBar.incrementProgressBy(50);
       try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("BootKeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }//키 해시 값 GET


        //TextToSpeechManager.getInstance().finalizeLibrary();
        ttsClient = new TextToSpeechClient.Builder()     //음성 합성 포맷
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2  )     // NEWTONE_TALK_1  통합 음성합성방식   NEWTONE_TALK_2 편집 합성 방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM    )  //TTS 음색 모드 설정(여성 차분한 낭독체)
                                                                                //VOICE_MAN_READ_CALM  남성 차분한 낭독체
                                                                                //VOICE_WOMAN_DIALOG_BRIGHT  여성 밝은 대화체
                                                                                //VOICE_MAN_DIALOG_BRIGHT 남성 밝은 대화체
                .setListener(this)
                .build();

        String str ="안전한 약 복용 시스템 \n P엔H입니다.";
                /*"안녕하세요. 건강보험심사평가원에서 제공하는  \n 노인 및 시각장애인용 \n 의료 정보 서비스 어플리케이션입니다. ";//어플 시작 멘트*/
        //ttsClient.setSpeechText(str);  // ttsClient.play();
        ttsClient.play(str);


    }

    // 더 이상 쓰지 않는 경우에는 다음과 같이 해제
    public void onDestroy() {
        super.onDestroy();
        if(ttsClient!=null){
            ttsClient.stop();
        }

    }


    @Override
    public void onFinished() {//음성 합성이 모두 완료했을 때 발생하는 메소드
        int intSentSize = ttsClient.getSentDataSize();      //세션 중에 전송한 데이터 사이즈
        int intRecvSize = ttsClient.getReceivedDataSize();  //세션 중에 전송받은 데이터 사이즈

        final String strInacctiveText = "handleFinished() SentSize : " + intSentSize + "  RecvSize : " + intRecvSize;
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);//엑티비티 이동을 위한 인텐트 선언
        startActivity(intent);//위에서 선언한 엑티비티로 이동
        finish();
        Log.i("BootAcivity TAG", strInacctiveText);
    }

    @Override
    public void onError(int code, String message) {//음성 합성 에러 메소드
        Log.d("TAG","Error code:"+code +"Error message"+message);
    }
}
