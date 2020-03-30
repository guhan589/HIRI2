package com.example.hiri;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.StringTokenizer;

public class Result_inf extends AppCompatActivity implements TextToSpeechListener {

    private TextToSpeechClient ttsClient;
    boolean state;
    Bitmap bitmap;
    String getData="", information="",str_pill_name;
    StringBuilder text = new StringBuilder();
    String[] care_buffer, effect_buffer;
    ImageView imageView;
    TextView pill_name, pill_inform1,pill_effect,pill_inform2;
    Button btn_repeat;
    StringTokenizer token;
    HashMap<String,String> care_map,effect_map;
    int i = 0;
   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_inf);

        state = getIntent().getBooleanExtra("state",false);
        str_pill_name = getIntent().getStringExtra("pill_name");
        ttsClient = new TextToSpeechClient.Builder()   //음성 합성 포맷
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2  )     // NEWTONE_TALK_1  통합 음성합성방식   NEWTONE_TALK_2 편집 합성 방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM    )  //TTS 음색 모드 설정(여성 차분한 낭독체)
                //VOICE_MAN_READ_CALM  남성 차분한 낭독체
                //VOICE_WOMAN_DIALOG_BRIGHT  여성 밝은 대화체
                //VOICE_MAN_DIALOG_BRIGHT 남성 밝은 대화체
                .setListener(this)
                .build();

        imageView = findViewById(R.id.image_pill);
        pill_name = findViewById(R.id.pill_name);
        pill_inform1 = findViewById(R.id.pill_inform);
        pill_effect = findViewById(R.id.textView_effect);
        pill_inform2 = findViewById(R.id.textView_inform);
        effect_map = new HashMap<>();
        care_map = new HashMap<>();
        btn_repeat = findViewById(R.id.button_repeat);
        btn_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsClient.stop();
                while(ttsClient.isPlaying())
                    ;
                ttsClient.play("다시듣기 버튼을 누르셨습니다.\n");
                while(ttsClient.isPlaying())
                    ;
                ttsClient.play(text.toString());
            }
        });
        effect_map.put("아섹정","신체에서 발생하는 통증들을 느끼지 못하도록 신경의 전기신호를 일시적으로 차단합니다.");
        care_map.put("아섹정","복용시 어지러움을 느끼는 환자는 운전을 하거나 위험한 기계조작을 피해야 합니다. /매일 세잔 이상 정기적으로 술을 마시는 사람이 이 약이나 다른 해열진통제를 복용해야 할 경우 반드시 의사 또는 약사와 상의해야 합니다.. 이러한 사람이 이 약을 복용하면 위장출혈이 유발될 수 있다/복용 후 " +
                "피부증상이나 경련증상이 발생하면 약 복용을 즉시 중단하고 의사에게 알립니다.");
        effect_map.put("에이프로젠세파클러캡슐","세균에 의한 각 종 감영증 치료 역할을 합니다.");
        care_map.put("에이프로젠세파클러캡슐","이 약의 사용에 있어서 \n 내성균의 발현을 방지하기 위하여 감수성을 확인하고 \n 치료 상 필요한 최소 기간만 투여하는 것이 \n 바람직합니다.");
       effect_map.put("네렉손서방정"," 신체에 통증이 있는 근육들을 풀어주는 역할을 합니다.");
       care_map.put("네렉손서방정","약 투여중 무력감, 비틀거림이 나타나면 약 복용을 중지 해야합니다./약 복용후 주의력 및 집중력 저하, 비틀거림, 졸음 등이 나타날 수 있으며 운전을 하거나 위험한 기계조작을 피해야 합니다.");
       effect_map.put("정장생캡슐","급ㆍ만성장염, 급ㆍ만성설사, 급성이질 등 \n각 종 원인에 기인한 장내 이상발효를 억제 합니다.");
       care_map.put("정장생캡슐","정해진 용법ㆍ용량을 잘 지키셔야합니다./1개월정도 투여하여도 증상의 개선이 없을 경우에는 투여를 중지하고 약사 또는 의사와 상의합니다.");
       effect_map.put("팍스정","염증, 통증 및 발열을 수반하는 감영증의 치료보조 및 진통제 입니다.");
       care_map.put("팍스정","해당 의약품을 장기 투여할 경우에는 원칙적으로 5일 이내로 복용해야합니다./ 이 약을 장기간 투여하는 환자는 정기적으로 임상검사 해야합니다.");


        ttsClient.stop();
        while(ttsClient.isPlaying())
            ;
        Log.d("TAG", "result_inf_state: "+state+"\n");
        Log.d("TAG", "str_pill_name: "+str_pill_name+"\n");
        String StrUrl = "http://apis.data.go.kr/1470000/MdcinGrnIdntfcInfoService/getMdcinGrnIdntfcInfoList";
        String ServiceKey = "LjJVA0wW%2BvsEsLgyJaBLyTywryRMuelTIYxsWnQTaPpxdZjpuxVCdCtyNxvObDmBJ57VVaSi3%2FerYKQFQmKs8g%3D%3D";
        String TotalUrl = StrUrl + "?ServiceKey=" + ServiceKey + "&pageNo=1&item_name=" + str_pill_name;
        Log.d("TAG", "TotalUrl: "+TotalUrl+"\n");
        DownLoad1 d1 = new DownLoad1();
        CheckTypesTask checkTypesTask = new CheckTypesTask();
        checkTypesTask.execute();

        d1.execute(TotalUrl);

    }

    @Override
    public void onFinished() {

    }

    @Override
    public void onError(int code, String message) {

    }

    public class DownLoad1 extends AsyncTask<String, Void, String> {////////////////////// 사용자가 입력한 dgsbjtCd 진료과목을 갖는 병원 파싱
        ProgressDialog asyncDialog = new ProgressDialog(Result_inf.this);
        @Override
        protected String doInBackground(String... url) {
            try {
                return (String) DownLoadUrl1((String) url[0]);
            } catch (IOException e) {
                return "다운로드 실패";
            }
        }
        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("검색중입니다.");

            asyncDialog.show();
            super.onPreExecute();
        }

        protected void onPostExecute(String result) {
            //super.onPostExecute(result);

            String resultCode = "";   //결과코드
            String item_image = "";   // 중기예보데이터
            String class_name = "";
            String telno = "";
            String xpos =  "";
            String ypos = "";
            String yadmNm = "";

            boolean ho_resultCode = false;//결과코드
            boolean ho_item_image = false;// 중기예보 tag
            boolean ho_class_name = false;
            boolean ho_telno = false;
            boolean ho_xpos = false;
            boolean ho_ypos = false;
            boolean ho_yadmNM = false;


            try {
                XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
                xmlPullParserFactory.setNamespaceAware(true);
                XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();

                xmlPullParser.setInput(new StringReader(result));
                int eventType = xmlPullParser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {
                    } else if (eventType == XmlPullParser.START_TAG) {
                        String tag_name = xmlPullParser.getName();
                        switch (tag_name) {
                            case "resultCode":
                                ho_resultCode = true;
                                break;
                            case "ITEM_IMAGE":
                                ho_item_image = true;
                                break;
                            case "CLASS_NAME":
                                ho_class_name = true;
                                break;
                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        if (ho_resultCode) {
                            resultCode = xmlPullParser.getText();
                            ho_resultCode = false;
                        }
                        if (resultCode.equals("00")) {
                            if (ho_item_image) { //아이템 이미지
                                item_image = xmlPullParser.getText();
                                information += item_image + "\n";
                                ho_item_image = false;
                            }else if(ho_class_name){ //클래스 이름
                                class_name = xmlPullParser.getText();
                                information += class_name + "\n";
                                ho_class_name = false;
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        ;
                    }
                    eventType = xmlPullParser.next();

                }


                Log.d("TAG", "\ninformation_total:" + information); //데이터 파싱 한것을 로그 출력

                token = new StringTokenizer(information,"\n");
                String[] buffer = new String[token.countTokens()];
                 i = 0;
                while(token.hasMoreTokens()){
                    buffer[i] = token.nextToken();
                    Log.d("TAG", "buffer====>:["+i+"]="+buffer[i]);
                    i++;
                }


                Thread thread = new Thread(){
                    @Override
                    public void run(){//의약품 이미지
                        try{
                            URL url = new URL(buffer[0]);
                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
                            con.setDoInput(true);
                            con.connect();
                            InputStream is = con.getInputStream();
                            bitmap = BitmapFactory.decodeStream(is);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };

                thread.start();

                try{
                    thread.join();
                    imageView.setImageBitmap(bitmap);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                i=0;

                //Log.d("TAG", "onPostExecutepill_name: "+pill_name);
                //String str = care_map.get("아섹정");
               // Log.d("TAG", "strstrstrstrstr1111=: "+str);
                String effect = effect_map.get(str_pill_name);
                pill_effect.setText(effect);
                token = new StringTokenizer(care_map.get(str_pill_name),"/");
                care_buffer = new String[token.countTokens()];
                text.append(str_pill_name+"에 대한 약 효능 및 주의사항은 다음과 같습니다. 이 약의 효능은 \n"+effect+"복용시 주의할점은\n");
                while (token.hasMoreTokens()){
                    care_buffer[i] = token.nextToken();
                    care_buffer[i] = (i+1)+"번 \n"+care_buffer[i]+"\n";
                    pill_inform2.append(care_buffer[i]);
                    text.append(care_buffer[i]);

                    i++;
                }
                text.append("이상입니다. 다시 들으시려면 \n 화면 상단 다시듣기 버튼을 누르세요.");

                pill_name.setText(str_pill_name);
                pill_inform1.setText(buffer[1]);

                ttsClient.stop();
                while(ttsClient.isPlaying())
                    ;
                if(state)
                    ttsClient.play(text.toString());

                Log.d("TAG", "text.toString(): "+text.toString()+"\n");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String DownLoadUrl1(String myurl) throws IOException {
            HttpURLConnection com = null;
            BufferedReader bufferedReader;
            try {
                Log.d("TAG", "\n==========DownLoadUrl1==========\n ");
                URL url = new URL(myurl);
                com = (HttpURLConnection) url.openConnection();
                com.setRequestMethod("GET");
                BufferedInputStream bufferedInputStream = new BufferedInputStream(com.getInputStream());
                bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream, "UTF-8"));

                String total = null;
                getData = "";
                while ((total = bufferedReader.readLine()) != null) {
                    getData += total;
                }
                asyncDialog.dismiss();
                return getData;
            } finally {
                com.disconnect();

            }
        }
    }
    private class CheckTypesTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog asyncDialog = new ProgressDialog(Result_inf.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("검색중입니다.");

            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                for (int i = 0; i < 5; i++) {
                    asyncDialog.setProgress(i * 30);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            asyncDialog.dismiss();
            super.onPostExecute(aVoid);
            //finish();
        }
    }
    public void onDestroy() {
        super.onDestroy();
        if(ttsClient!=null)
            ttsClient.stop();
    }
}
