package com.example.hiri;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;
import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;
import com.kakao.sdk.newtoneapi.TextToSpeechManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

public class show_totalweatherActivity extends AppCompatActivity implements TextToSpeechListener {

    String StrUrl,ServiceKey,tmFc,information="",getData="";
    StringBuilder TotalUrl;
    public TextToSpeechClient ttsClient;
    Button recall_btn, call_btn;
    WebView mWebView;
    WebSettings mWebSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_totalweather);

        TextToSpeechManager.getInstance().initializeLibrary(getApplicationContext());//tts 초기화
        SpeechRecognizerManager.getInstance().initializeLibrary(getApplicationContext());//stt 초기화

        call_btn = findViewById(R.id.button_voice);
        recall_btn = findViewById(R.id.button_repeat);
        mWebView = findViewById(R.id.webView);

        ttsClient = new TextToSpeechClient.Builder()
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2  )     // NEWTONE_TALK_1  통합 음성합성방식   NEWTONE_TALK_2 편집 합성 방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM    )  //TTS 음색 모드 설정(여성 차분한 낭독체)
                //VOICE_MAN_READ_CALM  남성 차분한 낭독체
                //VOICE_WOMAN_DIALOG_BRIGHT  여성 밝은 대화체
                //VOICE_MAN_DIALOG_BRIGHT 남성 밝은 대화체
                .setListener(this)
                .build();
        TotalUrl = new StringBuilder();

        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
        String time_text = new SimpleDateFormat("HHmm",Locale.getDefault()).format(currentTime);

        String buf_hh = time_text.substring(0,2);
        int int_hh = Integer.parseInt(buf_hh);
        if(int_hh<18&&int_hh>=6){
            tmFc = date_text +"0600";
        }else{
            tmFc = date_text +"1800";
        }



        mWebView.setWebViewClient(new WebViewClient());
        mWebSettings = mWebView.getSettings();
        mWebSettings.setJavaScriptEnabled(true);

        mWebView.loadUrl("http://m.kma.go.kr/m/index.jsp#none");

        StrUrl = "http://apis.data.go.kr/1360000/MidFcstInfoService/getMidFcst";
        ServiceKey = "LjJVA0wW%2BvsEsLgyJaBLyTywryRMuelTIYxsWnQTaPpxdZjpuxVCdCtyNxvObDmBJ57VVaSi3%2FerYKQFQmKs8g%3D%3D";
        TotalUrl.append(StrUrl + "?ServiceKey=" + ServiceKey + "&stnId=108"+"&tmFc="+tmFc);
        Log.d("TAG", "TotalUrl=: "+TotalUrl);

        DownLoad1 d1 = new DownLoad1();
        d1.execute(String.valueOf(TotalUrl));


    }

    @Override
    public void onFinished() {

    }

    @Override
    public void onError(int code, String message) {

    }


    public class DownLoad1 extends AsyncTask<String, Void, String> {////////////////////// 사용자가 입력한 dgsbjtCd 진료과목을 갖는 병원 파싱

        @Override
        protected String doInBackground(String... url) {
            try {
                return (String) DownLoadUrl1((String) url[0]);
            } catch (IOException e) {
                return "다운로드 실패";
            }
        }

        protected void onPostExecute(String result) {
            //super.onPostExecute(result);
            String resultCode = "";   //결과코드
            String wfSv = "";   // 중기예보데이터


            boolean ho_resultCode = false;//결과코드
            boolean ho_wfSv = false;// 중기예보 tag


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
                            case "wfSv":
                                ho_wfSv = true;
                                break;
                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        if (ho_resultCode) {
                            resultCode = xmlPullParser.getText();
                            ho_resultCode = false;
                        }
                        if (resultCode.equals("00")) {
                            if (ho_wfSv) {
                                wfSv = xmlPullParser.getText();
                                information += wfSv + "/";
                                ho_wfSv = false;
                            }

                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        ;
                    }
                    eventType = xmlPullParser.next();

                }


                Log.d("TAG", "\ninformation_total:" + information); //데이터 파싱 한것을 로그 출력
                StringTokenizer token = new StringTokenizer(information,"/");
                Log.d("TAG", "information_token_totatl: "+token.countTokens());

                information = "전국 중기예보 날씨를 알려드리겠습니다." + information +"   \n이상입니다.";

                call_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ttsClient.play(information);
                    }
                });


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
                return getData;
            } finally {
                com.disconnect();

            }
        }
    }
}
