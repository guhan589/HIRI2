package com.example.hiri;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class take_pil extends AppCompatActivity implements TextToSpeechListener {

    private static final String MODEL_PATH = "tf_lite_model_ver_16.tflite"; // 인터프리터
    private static final boolean QUANT = false;
    private static final String LABEL_PATH = "label.txt";
    private static final int INPUT_SIZE = 224;//336 //224
    private TextToSpeechClient ttsClient;
    private Classifier classifier;

    private Executor executor = Executors.newSingleThreadExecutor();

    private Button btnDetectObject;

    private CameraView cameraView;
    Thread thread;
    boolean state;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_pil);
        cameraView = findViewById(R.id.cameraView);

        btnDetectObject = findViewById(R.id.btnDetectObject);
        state = getIntent().getBooleanExtra("state",false);
        Log.d("TAG", "take_pil_state: "+state);
        CheckTypesTask checkTypesTask = new CheckTypesTask();
        ;
        initTensorFlowAndLoadModel();//---------------------1
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

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                ttsClient.play("촬영이 완료되었습니다. 의약품을 분석하고 있으니 \n 잠시만 기다려 주세요.");
                Bitmap bitmap = cameraKitImage.getBitmap();

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);//true: 이미지 크기 늘릴시 선명하게 보이도록
                cameraView.setVisibility(View.INVISIBLE);

                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);//------------5

                Intent intent = new Intent(getApplicationContext(),Result_inf.class);
                Log.d("TAG", "resultsresultsresultsresults: "+results.toString().length());
                Log.d("TAG", "resultsresultsresultsresults: "+results.toString());
                StringTokenizer token = new StringTokenizer(results.toString()," ");
                String num = token.nextToken();
                String name = token.nextToken();
                String probability = token.nextToken();
                Log.d("TAG", "namenamename: "+name.length());
                Log.d("TAG", "namenamename: "+name);
                intent.putExtra("pill_name",name);
                intent.putExtra("state",state);


                while (ttsClient.isPlaying())
                    ;
                ttsClient.stop();
                startActivity(intent);
                finish();
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });



        btnDetectObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
            }
        });

        if(state){//시각장애인용
            thread = new Thread(){
                @Override
                public void run(){
                    btnDetectObject.setVisibility(View.GONE);
                    tts_timer();
                }
            };
            thread.start();

        }
    }
    private void tts_timer(){
        ttsClient.stop();
        ttsClient.play("화면에는 알약을 촬영하기 위한\n 카메라가 켜져있습니다. 5초 후 자동으로 촬영 됩니다.");
        while (ttsClient.isPlaying())
            ;
        for(int i = 5 ; i > 0 ; i--){
            ttsClient.play(Integer.toString(i));
            while (ttsClient.isPlaying())
                ;
        }
        cameraView.captureImage();
    }
    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ttsClient!=null)
            ttsClient.stop();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    private void initTensorFlowAndLoadModel() {//-------------------2
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_PATH,
                            LABEL_PATH,
                            INPUT_SIZE,
                            QUANT);
                    //makeButtonVisible();//----------------------3
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);//--------------4
            }
        });
    }

    @Override
    public void onFinished() {

    }

    @Override
    public void onError(int code, String message) {

    }
    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(take_pil.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("검색중입니다.");
            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < 7; i++) {
                asyncDialog.setProgress(i * 30);
                //Thread.sleep(500);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            asyncDialog.dismiss();

            //finish();
        }
    }

}
