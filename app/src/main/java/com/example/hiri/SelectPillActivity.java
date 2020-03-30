package com.example.hiri;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class SelectPillActivity extends AppCompatActivity implements TextToSpeechListener {
    private ArrayList<SelectPillItem> mArrayList;
    AssetManager am;
    InputStream is;
    boolean state,reco_bool,click_bool;
    byte[] buf;
    String text, result,information="",str,total_text,pill_name;
    int position;
    String[] str_buffer;
    int i =0;
    RecyclerView recyclerView;
    SelectPillItemAdapter adapter;
    public Button button_repeat,button_voice;
    private TextToSpeechClient ttsClient;
    Intent reco;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_pill);
        state = getIntent().getBooleanExtra("state",false);
        result = getIntent().getStringExtra("st_result");
        Log.d("TAG", "SelectPillActivity_state: "+state+"\n");
        Log.d("TAG", "SelectPillActivity_result1: "+result+"============\n");

        button_repeat = findViewById(R.id.button_repeat); //다시듣기
        button_voice = findViewById(R.id.button_voice); //음성호출
        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1); // ~행 열
        recyclerView.setLayoutManager(gridLayoutManager);
        mArrayList = new ArrayList<>();
        adapter = new SelectPillItemAdapter(mArrayList);
        recyclerView.setAdapter(adapter);

        reco = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        reco.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        reco.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-kr");

        state = getIntent().getBooleanExtra("state",false);
        button_repeat = findViewById(R.id.button_repeat); //다시듣기
        button_voice = findViewById(R.id.button_voice); //음성호출

        button_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsClient.stop();
                while(ttsClient.isPlaying())
                    ;
                ttsClient.play(total_text);
            }
        });
        button_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsClient.stop();
                while(ttsClient.isPlaying())
                    ;
                String text = "음성호출 버튼을 누르셨습니다. \n 누르신 후에  띵동 소리가 나면 \n그 후에 의약품 번호를 말하세요.  \n";
                ttsClient.play(text);

                while(ttsClient.isPlaying())
                    ;
                SpeechRecognizer Speech = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());//getApplicationContext:현재 엑티비티
                Speech.setRecognitionListener(listener);
                Speech.startListening(reco);
            }
        });

        ttsClient = new TextToSpeechClient.Builder()   //음성 합성 포맷
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2  )     // NEWTONE_TALK_1  통합 음성합성방식   NEWTONE_TALK_2 편집 합성 방식
                .setSpeechSpeed(0.8)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM    )  //TTS 음색 모드 설정(여성 차분한 낭독체)
                //VOICE_MAN_READ_CALM  남성 차분한 낭독체
                //VOICE_WOMAN_DIALOG_BRIGHT  여성 밝은 대화체
                //VOICE_MAN_DIALOG_BRIGHT 남성 밝은 대화체
                .setListener(this)
                .build();


        am = getResources().getAssets();

        try {
            is = am.open("pill_name.txt");
            int size = is.available();
            buf = new byte[size];
            is.read(buf);
            text = new String(buf);
            StringTokenizer token = new StringTokenizer(text,"/");
            str_buffer = new String[token.countTokens()];
            Log.d("TAG", "onCreate_ token.countTokens()"+token.countTokens());
            while(token.hasMoreTokens()){
                str_buffer[i] = token.nextToken();
                Log.d("TAG", "onCreate_buffer["+i+"]="+str_buffer[i]);
                Log.d("TAG", "onCreate_buffer["+i+"].length="+str_buffer[i].length());
                i++;
            }
            Log.d("TAG", "onCreate_text: "+text);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int j =1;

        Log.d("TAG", "onCreate_buffer.length: "+str_buffer.length);
        for(i = 0; i< str_buffer.length;i++){
            if(result.contains(str_buffer[i])){
                Log.d("TAG", "=======================: "+i+"\n");
                mArrayList.add(new SelectPillItem(Integer.toString(j),str_buffer[i]));
                information += j+"번"+str_buffer[i]+"\n";
                j++;

            }
        }
        adapter.setOnItemClickListener(new SelectPillItemAdapter.OnSelectPillItemClickListener() {
            @Override
            public void onItemClick(SelectPillItemAdapter.ViewHolder holder, View view, int position) {
                Log.d("TAG", "position: "+position);
                ttsClient.stop();
                while(ttsClient.isPlaying())
                    ;
                pill_name =adapter.getItem(position).getPill_name();
                Log.d("TAG", "str: "+str+"\n");
                Intent intent = new Intent(getApplicationContext(),Result_inf.class);
                intent.putExtra("state",state);
                intent.putExtra("pill_name",pill_name);
                startActivity(intent);
            }
        });

        ttsClient.stop();
        while(ttsClient.isPlaying())
            ;
        j-=1;
        String front_text = "약 봉투 스캔이 완료되었습니다. \n 처방 받은 의약품 총 \n"+j+"건을 인식하였습니다. \n 해당 약품을 불러드리겠습니다.";
        String end_text = "\n이상입니다. 의약품 목록을 다시 듣고 싶으시면 왼쪽 삳단 다시듣기 버튼을 누르시거나 \n 해당 약품을 자세히 알고 싶으시면 \n오른쪽 상단 호출 버튼을 눌러 의약품의 번호를 말하세요.";
        total_text = front_text+information+end_text;
        if(state)
            ttsClient.play(total_text);
    }
    public void onDestroy() {
        super.onDestroy();
        if(is!=null){
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(ttsClient!=null)
            ttsClient.stop();

    }

    @Override
    public void onFinished() {
        if(reco_bool){
            reco_bool=false;
            ttsClient.stop();
            while(ttsClient.isPlaying())
                ;
            Intent intent = new Intent(getApplicationContext(),Result_inf.class);
            intent.putExtra("state",state);
            intent.putExtra("pill_name",pill_name);
            startActivity(intent);
        }
    }

    @Override
    public void onError(int code, String message) {

    }

    /////////////////음성호출/////////////////////
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
                    ttsClient.play(message);
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    ttsClient.play(message);
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    ttsClient.play(message);
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    ttsClient.play(message);
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    ttsClient.play(message);
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    ttsClient.play(message);
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "음성인식 서비스가 과부하 되었습니다.";
                    ttsClient.play(message);
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버 장애";
                    ttsClient.play(message);
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    ttsClient.play(message);
                    break;
                default:
                    message = "알 수 없는 오류임";
                    ttsClient.play(message);
                    break;
            }

            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle bundle) {//인식결과가 준비되면 호출
            String text;
            ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);//ArrayList는 크기를 미리 정하지 않아 얼마든지 많은 수의 데이터를 저장
            str = matches.get(0);
            Log.d("TAG", "strstrstrstr1: " + str + "\n");
            Log.d("TAG", "strstrstrstr1.length: " + str.length() + "\n");
            str = str.replaceAll("일","1");
            if(str.length()==2) {
                str = str.substring(0, 1);
            }else if(str.length()==3)
                str =str.substring(0,2);


            reco_bool=true;
            Log.d("TAG", "strstrstrstr2: " + str + "\n");
            Log.d("TAG", "strstrstrstr2.length: " + str.length() + "\n");
            position = Integer.parseInt(str);
            if(position<=str_buffer.length){
                pill_name = adapter.getItem(position-1).getPill_name();
                text = "의약품 "+pill_name+"을 \n 상세 검색하겠습니다.";
            }
            else
                text = "검색할 의약품 번호를 잘 못 말씀하셨습니다. \n 오른쪽 상단 호출버튼을 눌러 다시 번호를 말하세요.";

            ttsClient.stop();
            while(ttsClient.isPlaying())
                ;
            ttsClient.play(text);//출력할 문자
            matches.clear();
        }


        @Override
        public void onPartialResults(Bundle bundle) {//부분 인식 결과를 사용할 수 있을 때 호출

        }
        @Override
        public void onEvent(int i, Bundle bundle) {//향후 이벤트를 추가하기 위해 예약

        }
    };

}
