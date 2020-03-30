package com.example.hiri;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

public class findhospActivity extends AppCompatActivity implements TextToSpeechListener {
    private TextToSpeechClient ttsClient;
    private ArrayList<HospitalSubject> mArrayList;
    private ArrayList<String> list;
    HashMap<String,String> code, view_code;
    String str=null,str2=null,dgsbjtCd=null,text="";
    public boolean callbool = false, distancebool = false, scorebool=false, state=false,reco_bool=false;
    public Intent reco;
    public Button button_repeat,button_voice;
    String[] buffer;
    RecyclerView recyclerView;
    HospitalSubjectAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_find_hospital_subject);

        state = getIntent().getBooleanExtra("state",false);
        button_repeat = findViewById(R.id.button_repeat); //다시듣기
        button_voice = findViewById(R.id.button_voice); //음성호출


        ttsClient = new TextToSpeechClient.Builder()   //음성 합성 포맷
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2  )     // NEWTONE_TALK_1  통합 음성합성방식   NEWTONE_TALK_2 편집 합성 방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM    )  //TTS 음색 모드 설정(여성 차분한 낭독체)
                //VOICE_MAN_READ_CALM  남성 차분한 낭독체
                //VOICE_WOMAN_DIALOG_BRIGHT  여성 밝은 대화체
                //VOICE_MAN_DIALOG_BRIGHT 남성 밝은 대화체
                .setListener(this)
                .build();
        text = "병원 찾기 서비스 입니다. \n 우측 상단의 호출버튼을 누르시고 \n 원하시는 진료과목을 말씀 하시면 해당 과목을 진료하는 병원의 정보를 알 수 있습니다. \n" +
                "다시 들으시려면 왼쪽 상단에 다시듣기 버튼을 누르세요.\n ";
        if(state) {
            ttsClient.stop();
            ttsClient.play(text);
        }

        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // ~행 2열
        recyclerView.setLayoutManager(gridLayoutManager);
        mArrayList = new ArrayList<>();
        adapter = new HospitalSubjectAdapter(mArrayList);
        recyclerView.setAdapter(adapter);

        code = new HashMap<>();  // 진료명에 따른 진료과목코드 쌍
        code.put("일반의", "00");code.put("내과", "01");code.put("신경과", "02");code.put("정신건강의학과", "03");code.put("외과","04");code.put("정형외과", "05");
        code.put("신경외과", "06");code.put("흉부외과","07");code.put("성형외과","08");code.put("마취통증의학과","09");code.put("산부인과","10");code.put("소아과","11");//소아청소년과
        code.put("안과","12");code.put("이비인후과","13");code.put("피부과","14");code.put("비뇨기과","15");code.put("영상의학과","16");code.put("방사선종양학과","17");
        code.put("병리과","18");code.put("진단검사의학과","19");code.put("결핵과","20");code.put("재활의학과","21");code.put("핵의학과","22");code.put("가정의학과","23");
        code.put("응급실","24");/*응급의학과*/code.put("직업환경의학과","25");code.put("예방의학과","26");code.put("치과","49");/*code.put("구강악안면외과","50");*/code.put("치과보철과","51");code.put("치과교정과","52");
        code.put("소아치과","53");code.put("치주과","54");code.put("치과보존과","55");code.put("구강내과","56");/*code.put("구강악안면방사선과","57");*/code.put("구강병리과","58");
        code.put("예방치과","59");code.put("치과소계","60");code.put("한방내과","80");code.put("한방부인과","81");code.put("한의원","82");/*code.put("한방안이비인후피부과","83");*/code.put("한방신경정신과","84");
        code.put("침구과","85");code.put("한방재활의학과","86");code.put("사상체질과","87");code.put("한방응급","88");code.put("한방소계","90");

        view_code = new HashMap<>();
        view_code.put("내과", "01");view_code.put("신경과", "02");view_code.put("정신건강의학과", "03");view_code.put("외과","04");view_code.put("정형외과", "05");
        view_code.put("신경외과", "06");view_code.put("흉부외과","07");view_code.put("성형외과","08");view_code.put("산부인과","10");view_code.put("소아과","11");//소아청소년과
        view_code.put("안과","12");view_code.put("이비인후과","13");view_code.put("피부과","14");view_code.put("비뇨기과","15");
        view_code.put("가정의학과","23");view_code.put("응급실","24");/*응급의학과*/view_code.put("치과","49");view_code.put("소아치과","53");
        view_code.put("한의원","82");/*code.put("한방안이비인후피부과","83");*/view_code.put("한방응급","88");

        Set set = view_code.keySet();
        Iterator iterator = set.iterator();
        int num=1;
        while(iterator.hasNext()){
            String key = (String) iterator.next();
            mArrayList.add(new HospitalSubject(Integer.toString(num), key));
            num++;
            //adapter.notifyDataSetChanged();
        }

        adapter.setOnItemClickListener(new HospitalSubjectAdapter.OnHospitalSubjectItemClickListener(){

            @Override
            public void onItemClick(HospitalSubjectAdapter.ViewHolder holder, View view, int position) {
                Log.d("TAG", "position: "+position);
                ttsClient.stop();
                while(ttsClient.isPlaying())
                    ;
                str =adapter.getItem(position).get_name();
                Log.d("TAG", "str: "+str+"\n");
                Iterator<String> iterator = code.keySet().iterator();
                while (iterator.hasNext()) {//토근의 개수가 null일 때까지
                    String key = iterator.next();
                    Log.d("TAG", "key: "+key+"\n");
                    Log.d("TAG", "key.equals(str): "+key.equals(str)+"\n");
                    if (key.equals(str)) {
                        str2 = str+"\n을 진료하는 병원을 검색합니다.";
                        dgsbjtCd = code.get(key);
                        Log.d("TAG", "dgsbjtCd: "+dgsbjtCd+"\n");
                        scorebool=true;
                        ttsClient.play(str2);
                        break;
                    }
                }
            }
        });

        reco = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        reco.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        reco.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-kr");

       button_voice.setOnClickListener(new View.OnClickListener() { //음성호출
            @Override
            public void onClick(View v) {
                if(state){
                    callbool = true;
                    ttsClient.stop();
                    Log.d("TAG", "\n=======ttsClient.isPlaying()1: "+ttsClient.isPlaying()+"======\n");
                    while(ttsClient.isPlaying())
                        ;
                    Log.d("TAG", "\n=======ttsClient.isPlaying()2: "+ttsClient.isPlaying()+"======\n");

                    String text = "음성호출 버튼을 누르셨습니다. \n 누르신 후에  띵동 소리가 나면 \n그 후에 진료과목을 말하세요.  \n";
                    ttsClient.play(text);

                    while(ttsClient.isPlaying())
                        ;
                    SpeechRecognizer Speech = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());//getApplicationContext:현재 엑티비티
                    Speech.setRecognitionListener(listener);
                    Speech.startListening(reco);
                }

            }
        });
        button_repeat.setOnClickListener(new View.OnClickListener() { //다시듣기
            @Override
            public void onClick(View v) {
                if(state) {
                    ttsClient.stop();
                    ttsClient.play(text);
                }
            }
        });

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
            //resultText = (TextView) findViewById(R.id.resultText);//res resultText
            ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);//ArrayList는 크기를 미리 정하지 않아 얼마든지 많은 수의 데이터를 저장
            str = matches.get(0);
            Log.d("TAG", "strstrstrstr: "+str+"\n");
            StringTokenizer token = new StringTokenizer(str," ");
            buffer = new String[token.countTokens()];
            int i = 0;
            while(token.hasMoreTokens()){
                buffer[i] = token.nextToken();
                buffer[i] = buffer[i].replaceAll("\t\r","");
                Log.d("TAG", "buffer["+i+"] "+buffer[i]+"\n");
                i++;
            }
            //tr = str.replaceAll(" ", "");//음성인식으로 받은 문자열 공백 제거
            String str2 = null;
            Iterator<String> iterator = code.keySet().iterator();
            Log.d("TAG", "buffer[0].length(): "+buffer[0].length()+"\n");
            while (iterator.hasNext()) {//토근의 개수가 null일 때까지
                String key = iterator.next();
                if (buffer[0].equals(key)) {//key.equals(buffer[0])
                    Log.d("TAG", "=======================buffer[0].equals(key)===============\n");
                    str2 = " \n"+buffer[0]+"를 진료하는 병원을 검색합니다.";


                    while(ttsClient.isPlaying())
                        ;
                    ttsClient.stop();

                    dgsbjtCd = code.get(key);
                    Log.d("TAG", "dgsbjtCd: "+dgsbjtCd+"\n");

                    reco_bool=true;

                    Log.d("TAG", "scorebool: "+scorebool);;
                    break;

                } else {
                    Log.d("TAG", "=======================buffer[0].Notequals(key)===============\n");
                    str2 = "찾고자하는 진료과목을 검색하지 못하였습니다. 호출버튼을 눌러 다시 말하십시오.";

                }
            }
            ttsClient.play(str2);//출력할 문자
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
    public void onFinished() {
        if(scorebool){
            scorebool = false;
            Intent intent2 = new Intent(getApplicationContext(),search_hosActivity.class);
            intent2.putExtra("yadmNum",str);//진료과목 명
            intent2.putExtra("dgsbjtCd",dgsbjtCd);//진료과목 코드
            intent2.putExtra("state",state);
            startActivity(intent2);//진료과목을 검색하는 클래스로 이동
            dgsbjtCd=null;
            Log.d("TAG", "scorebool: "+scorebool);;
        }
        else if(reco_bool){
            Intent intent2 = new Intent(getApplicationContext(),search_hosActivity.class);
            intent2.putExtra("yadmNum",buffer[0]);//진료과목 명
            intent2.putExtra("dgsbjtCd",dgsbjtCd);//진료과목 코드
            intent2.putExtra("state",state);
            startActivity(intent2);//진료과목을 검색하는 클래스로 이동
            dgsbjtCd=null;
            reco_bool = false;
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
