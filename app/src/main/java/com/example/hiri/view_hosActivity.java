package com.example.hiri;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

import static android.speech.tts.TextToSpeech.ERROR;

public class view_hosActivity extends AppCompatActivity {
    ArrayList<String> list;
    Button CallButton, ReCallButton;
    private Vibrator vibrator;
    TextToSpeech tts;
    int count1 = 0,count2 = 0,num=0;
    String teststring = "",str="",yadmNm;
    HashMap<String,String> code;
    Intent call = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_hos);

        String yadmNm = getIntent().getStringExtra("yadmNm");
        String totalinformation  = getIntent().getStringExtra("information");


        Log.d("TAG", "view_hos_Activity_yadmNm: "+yadmNm);;
        Log.d("TAG", "view_hos_Activity_totalinformation: "+totalinformation);;
        list=new ArrayList<String>();
        StringTokenizer token1 = new StringTokenizer(totalinformation, "\n");
        String[] buffer = new String[token1.countTokens()];
        count1 = 0;
        //////////////////////////////////////////////////////////////////////////////////////////////////////
        while(token1.hasMoreTokens()){
            buffer[count1] = token1.nextToken();
            buffer[count1] = buffer[count1].replaceAll("\n", "");
            list.add(buffer[count1]);
            count1++;
        }
        int i = 0;
        //////////////////////////////////////////////////////////////////////////////////////////////////////
        code = new HashMap<>();
        while(i<list.size()){
            StringTokenizer token2 = new StringTokenizer(list.get(i),"/");
            String[] buffer2 = new String[token2.countTokens()];
            count2 = 0;
            while(token2.hasMoreTokens()){
                buffer2[count2] = token2.nextToken();
                buffer2[count2] = buffer2[count2].replaceAll("\n", "");
                count2++;
            }
            String str1="";
            str1 = Integer.toString(i+1);
            str1 = str1+"번";
            code.put(str1,buffer2[4]);

            teststring+= (i+1)+"번"+buffer2[4]+"\n";//+"평가등급 "+buffer2[5]
            i++;
        }

        Iterator<String> it = code.keySet().iterator();
        while(it.hasNext()){
            String key1 = it.next();
            String key2 = code.get(key1);
        }
        ttsC = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                if(status != ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
                if(count1== 0){
                    str = "   말씀하신 진료과목에 해당하는 병원이 없습니다.  다시 들으시려면 핸드폰 화면 세로중앙을 기준으로 하단 버튼을 누르십시오.";}
                else {
                    str = " 말씀하신 진료과목  "+yadmNm+" 를 진료하는 병원 총" + count1 + "건을 검색하였습니다. 병원목록을 불러드리겠습니다.  " + teststring + "  이상입니다. 다시 들으시려면 핸드폰 화면 세로중앙을 기준으로 하단 버튼을 누르시거나  "+
                            "  검색하고자 하는 병원이 있으면 상단 버튼을 눌러 병원의 번호를 말하십시오.";
                }
                tts.setPitch(0.8f);
                tts.speak(str,TextToSpeech.QUEUE_FLUSH,null);
            }

        });
        call = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        call.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        call.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-kr");

        CallButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View V) {
                String str1;
                vibrator.vibrate(250);
                str1 = "찾고자 하는 병원 번호를 말하십시오. 예를 들어 1번";
                tts.speak(str1, TextToSpeech.QUEUE_FLUSH, null);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SpeechRecognizer Speech = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());//getApplicationContext:현재 엑티비티
                        Speech.setRecognitionListener(listener);
                        Speech.startListening(call);
                    }
                }, 3800);
            }
        });

        ReCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(250);
                tts.speak(str,TextToSpeech.QUEUE_FLUSH,null);
            }
        });
    }

    private RecognitionListener listener = new RecognitionListener() {

        @Override
        public void onReadyForSpeech(Bundle bundle) {//사용자가 말하기 시작할 준비가되면 호출

        }

        @Override
        public void onBeginningOfSpeech() {//사용자가 말하기 시작했을 때 호출

        }

        @Override
        public void onRmsChanged(float v) {//입력받는 소리의 크기를 알려줌

        }

        @Override
        public void onBufferReceived(byte[] buffer) {//사용자가 말을 시작하고 인식이 된 단어를 buffer에 담습니다.

        }

        @Override
        public void onEndOfSpeech() {//사용자가 말하기를 중지하면 호출됩니다.

        }

        @Override
        public void onError(int error) {//네트워크 또는 인식 오류가 발생했을 때 호출
            String message;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    tts.speak(message.toString(), TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    tts.speak(message.toString(), TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    tts.speak(message.toString(), TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    tts.speak(message.toString(), TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    tts.speak(message.toString(), TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    tts.speak(message.toString(), TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "음성인식 서비스가 과부하 되었습니다.";
                    tts.speak(message.toString(), TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버 장애";
                    tts.speak(message.toString(), TextToSpeech.QUEUE_FLUSH, null);
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    tts.speak(message.toString(), TextToSpeech.QUEUE_FLUSH, null);
                    break;
                default:
                    message = "알 수 없는 오류임";
                    tts.speak(message.toString(), TextToSpeech.QUEUE_FLUSH, null);
                    break;
            }

            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle bundle) {//인식결과가 준비되면 호출
            ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);//ArrayList는 크기를 미리 정하지 않아 얼마든지 많은 수의 데이터를 저장
            String str1 = matches.get(0);
            str1 = str1.replaceAll(" ", "");//음성인식으로 받은 문자열 공백 제거
            String str2 = null;
            Iterator<String> iterator = code.keySet().iterator();
            while (iterator.hasNext()) {//토근의 개수가 null일 때까지
                String key = iterator.next();
                if (key.equals(str1)) {
                    str2 = "찾고자하는 병원을 검색합니다.";
                    str1 = str1.replaceAll("번","");
                    num = Integer.parseInt(str1);
                    String str3 = list.get(num-1);
                    Intent intent = new Intent(getApplicationContext(),Result.class);
                    intent.putExtra("list",list.get(num-1));
                    intent.putExtra("teststring",teststring);
                    startActivity(intent);
                    break;
                } else {
                    str2 = "찾고자하는 진료과목을 검색하지 못하였습니다. 호출버튼을 눌러 다시 말하십시오.";
                }
            }
            tts.speak(str2, TextToSpeech.QUEUE_FLUSH, null);//출력할 문자
            matches.clear();

        }

        @Override
        public void onPartialResults(Bundle bundle) {//부분 인식 결과를 사용할 수 있을 때 호출

        }

        @Override
        public void onEvent(int i, Bundle bundle) {//향후 이벤트를 추가하기 위해 예약

        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
    }
}
