package com.example.hiri;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;

public class HomeActivity extends AppCompatActivity implements TextToSpeechListener,SensorEventListener  {
    private TextToSpeechClient ttsClient;
    public boolean findbool1 = false, findbool2 = false, infor = false, sosbool = false, state = false;
    String text;
    StringBuilder text1;
    int count = 0 ;
    final int PERMISSION = 1;

    private long lastTime=0;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;
    private float x, y, z;
    private Vibrator vibrator;

    private static final int SHAKE_THRESHOLD1 = 4600;
    private static final int SHAKE_THRESHOLD2 = 2000;
    private static final int DATA_X = SensorManager.DATA_X;
    private static final int DATA_Y = SensorManager.DATA_Y;
    private static final int DATA_Z = SensorManager.DATA_Z;

    private SensorManager sensorManager;
    private Sensor accelerormeterSensor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); //진동울리기 같은 하드웨어적인 부분은 시스템으로 서비스를 가져와야함.
        //레이아웃 버튼 객체
        Button findbutton1, findbutton2, infor_btn,sosbutton;
        findbutton1 = findViewById(R.id.button_pill);
        findbutton2 = findViewById(R.id.button_bag);
        infor_btn = findViewById(R.id.button_find);
        sosbutton =  findViewById(R.id.button_call);

        state = getIntent().getBooleanExtra("state",false); // 음성 서비스 사용 유무

        Log.d("TAG", "\nHomeActivity_state: "+state+"\n");


        if (Build.VERSION.SDK_INT >= 23) {      //퍼미션 권한 부여
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO,Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE,Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION);
        }//퍼미션접근 권한
        text1 = new StringBuilder();
        text1.append(" 홈 화면 기능에 대해 설명해 드리겠습니다. 화면의 상단 왼쪽에는 알약 촬영 버튼이 있습니다. \n" +
                "알약 촬영 버튼은\n 알약을 촬영하여 \n그 의약품의 성분이나\n 주희할 점을 알려주는 기능을 제공합니다.\n" +
                "\n" +"화면 상단 오른쪽에는 약봉투 촬영 버튼이 있습니다. \n "+
                "약봉투 촬영 버튼은\n 약봉투를 촬영하여 \n 약 봉투의 적혀있는 의약품에 대한 성분이나\n주의할 점을 알려주는 기능을 제공합니다 "+
                "화면의 하단 왼쪽에는 병원 및 약국 찾기 버튼이 있습니다.\n" +
                "병원 및 약국 찾기 버튼은 현재 위치 주변에 있는 병원과 약국을 찾을 수 있는 기능을 제공합니다.\n" +
                "화면의 하단 오른쪽에는 복지콜 버튼이 있습니다.\n" +
                "복지콜 버튼은 보건복지콜센터(129)로  통화 연결되는 기능을 제공합니다. \n 이상입니다.");

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

        text = "홈화면에 집입하셨습니다. 제공하는 서비스에 대한 설명을 듣고 싶으시면 핸드폰을 흔드세요.";
        ttsClient.play(text);
        findbutton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = "알약 찍기  버튼을 누르셨습니다.";
                if (state) {
                    findbool1 = true;
                    findbool2 = false;
                    infor = false;
                    sosbool = false;
                    ttsClient.stop();
                    ttsClient.play(text);
                }else{
                    ttsClient.stop();
                    while (ttsClient.isPlaying())
                        ;
                    Intent intent = new Intent(getApplicationContext(), take_pil.class);
                    intent.putExtra("state",state);
                     startActivity(intent);
                }

            }
        });
        findbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = "약봉투 찍기 버튼을 누르셨습니다.";
                if(state){//보이스 ON
                    ttsClient.stop();
                    ttsClient.play(text);
                    findbool1 = false;
                    findbool2 = true;
                    infor = false;
                    sosbool = false;
                }else{
                    ttsClient.stop();
                    while (ttsClient.isPlaying())
                        ;
                    Intent intent = new Intent(getApplicationContext(), take_env.class);
                    intent.putExtra("state",state);
                    startActivity(intent);
                }
            }
        });
        infor_btn.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View v) {
                text = "병원 및 약국 찾기 서비스 버튼을 누르셨습니다.";
                if(state){//보이스 ON
                    ttsClient.stop();
                    ttsClient.play(text);
                    findbool1 = false;
                    findbool2 = false;
                    infor = true;
                    sosbool = false;
                }else{
                    ttsClient.stop();
                    while (ttsClient.isPlaying())
                        ;
                    Intent intent = new Intent(getApplicationContext(), find_hos_pha.class);
                    intent.putExtra("state",state);
                    startActivity(intent);
                }
            }
        });
        sosbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = "복지콜 버튼을 누르셨습니다. 통화연결을 하겠습니다.";
                if(state){//보이스 ON
                    ttsClient.stop();
                    ttsClient.play(str);
                    findbool1 = false;
                    findbool2 = false;
                    infor = false;
                    sosbool = true;
                }else{
                    ttsClient.stop();
                    while (ttsClient.isPlaying())
                        ;
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.putExtra("state",state);
                    intent.setData(Uri.parse("tel:129"));
                    startActivity(intent);
                }


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
            findbool1 = false;
            Intent intent = new Intent(getApplicationContext(), take_pil.class);
            intent.putExtra("state",state);
            startActivity(intent);
        }
        else if(findbool2){
            findbool2 = false;
            Intent intent = new Intent(getApplicationContext(), take_env.class);
            intent.putExtra("state",state);
            startActivity(intent);
        }
        else if(infor){
            infor = false;
            Intent intent = new Intent(getApplicationContext(), find_hos_pha.class);
            intent.putExtra("state",state);
            startActivity(intent);
        }
        else if(sosbool){
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.putExtra("state",state);
            intent.setData(Uri.parse("tel:129"));
            startActivity(intent);
        }
    }

    @Override
    public void onError(int code, String message) {//음성 합성 에러 메소드
        Log.d("TAG","Error code:"+code +"Error message"+message);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (accelerormeterSensor != null)
            sensorManager.registerListener((SensorEventListener) this, accelerormeterSensor,
                    SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime); //즉정시간 0.1
            if (gabOfTime > 100) {
                Log.d("TAG", "\ngabOfTime: "+gabOfTime+"\n");
                lastTime = currentTime;
                x = event.values[SensorManager.DATA_X];
                y = event.values[SensorManager.DATA_Y];
                z = event.values[SensorManager.DATA_Z];

                Log.d("TAG", "x=:" +x+"\n");
                Log.d("TAG", "lastX=:" +lastX+"\n");
                Log.d("TAG", "z=:"+ z+"\n");
                Log.d("TAG", "lastZ=:" +lastZ+"\n");
                speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000; //abs:절대값
                Log.d("TAG", "\nspeed: "+speed+"\n");
                if (speed<SHAKE_THRESHOLD1&&speed > SHAKE_THRESHOLD2&&state) {
                    vibrator.vibrate(1000);
                    ttsClient.stop();

                   while (ttsClient.isPlaying())
                       ;
                   ttsClient.play(text1.toString());
                }

                lastX = event.values[DATA_X];
                lastY = event.values[DATA_Y];
                lastZ = event.values[DATA_Z];
            }

        }

    }
    public void onDestroy() {
        super.onDestroy();
        if(ttsClient!=null){
            ttsClient.stop();
        }

    }
}

