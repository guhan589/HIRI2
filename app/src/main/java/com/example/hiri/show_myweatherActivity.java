package com.example.hiri;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kakao.sdk.newtoneapi.SpeechRecognizerManager;
import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;
import com.kakao.sdk.newtoneapi.TextToSpeechManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class show_myweatherActivity extends AppCompatActivity implements TextToSpeechListener {
    private LocationManager locationMgr;
    public TextToSpeechClient ttsClient;
    Double latitude = 0.0, longitude = 0.0;
    int count =0;
    Location location;
    String address="",information="",getData="",str_x="",str_y="",StrUrl,ServiceKey;
    ArrayList<String> list;
    HashMap<String,String> map,pty,image;
    String[] buffer;
    StringBuilder TotalUrl = new StringBuilder();
    StringBuilder totalmessage = new StringBuilder();//음성 호출 메시지
    //totalmessage.ape
    TextView pty_textview,reh_textview,t1h_textview,pm10_textview,pm25_textview;
    ImageView imageView ;
    Button bt1;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_myweather);

        list = new ArrayList<String>(); //ArrayList 객체 생성
        map = new HashMap<String, String>(); //Hashmap 객체 생성
        pty = new HashMap<String,String>();
        image = new HashMap<String, String>();
        list.add("REH");list.add("T1H");list.add("PTY");// REH: 습도 T1H: 온도  PTY: 강수형태
                                                        // 대량의 데이터 중에 사용할 데이터 (습도, 온도 강수형태)를 추출하기 위해서 list에 표현
        pty.put("0","맑음");pty.put("1","비");pty.put("2","비/눈");pty.put("3","눈");pty.put("4","소나기"); //강수코드
        image.put("맑음","weather_sun");image.put("비","weather_rain");image.put("비/눈","weather_snowy_rain");image.put("눈","weather_snowy");image.put("소나기","weather_rainshower");


        TextToSpeechManager.getInstance().initializeLibrary(getApplicationContext());//tts 초기화
        SpeechRecognizerManager.getInstance().initializeLibrary(getApplicationContext());//stt 초기화


        //////////// 텍스트뷰 객체 선언///////////////////
        pty_textview =findViewById(R.id.textView_weather);
        t1h_textview = findViewById(R.id.textView_tem);//온도
        reh_textview = findViewById(R.id.textView_chance);//습도
        pm10_textview = findViewById(R.id.textView_dust_pm10);//미세먼지
        pm25_textview = findViewById(R.id.textView_dust_pm25);//초미세먼지

        bt1 = findViewById(R.id.button_voice);


        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsClient.play(String.valueOf(totalmessage));
            }
        });

        ///////////퍼미션 접근 권한 여부 판단 및 권한 허용 ///////////////////
        final int PERMISSION = 1;
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //퍼미션접근 권한
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }

        ttsClient = new TextToSpeechClient.Builder()
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2  )     // NEWTONE_TALK_1  통합 음성합성방식   NEWTONE_TALK_2 편집 합성 방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM    )  //TTS 음색 모드 설정(여성 차분한 낭독체)
                //VOICE_MAN_READ_CALM  남성 차분한 낭독체
                //VOICE_WOMAN_DIALOG_BRIGHT  여성 밝은 대화체
                //VOICE_MAN_DIALOG_BRIGHT 남성 밝은 대화체
                .setListener(this)
                .build();

        startLocationService();//내위치 경도 위도 값 추출하는 함수 호출
        ///////////// GPS 환결설정//////////////////////
        locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        String bestProvider = locationMgr.getBestProvider(criteria, true);

        //Location location = locationMgr.getLastKnownLocation(bestProvider);
       // double x = location.getLatitude(); //내위치 x좌표
        //double y = location.getLongitude();// 내위치 y좌표
        Log.d("TAG", "ㅊ: "+latitude+","+longitude);

        address = getAddress(latitude,longitude);//경도 위도값을 매개변수로 하여 주소를 가져옴
        Log.d("TAG", "address: "+address);;
        StringTokenizer token = new StringTokenizer(address," ");
        buffer = new String[token.countTokens()];
        int i = 0 ;
        while(token.hasMoreTokens()){ // 주소 문자열 자르기
            buffer[i] = token.nextToken(); //buffer[0] = 대한민국 buffer[1] = 경기도 buffer[2] = 이천시 buffer[3] = 신둔면
            buffer[i] = buffer[i].replaceAll(" ","");
            i++;
        }
        ///////////////////  xls 데이터 가져오기   //////////////////
        try {
            InputStream is = getBaseContext().getResources().getAssets().open("동네예보 조회.xls");
            Workbook wb = Workbook.getWorkbook(is);

            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    int colTotal = sheet.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet.getColumn(colTotal-1).length;
                    Log.d("TAG", "rowTotal=: "+rowTotal+"\n");
                    StringBuilder sb;
                    for(int row=rowIndexStart;row<rowTotal;row++) {//행
                        sb = new StringBuilder();
                        /*for(int col=0;col<colTotal-1;col++) {//열
                            String contents = sheet.getCell(col, row).getContents(); //열의 값을 가져와 contents String 변수에 저장

                            sb.append("col"+col+" : "+contents+" , ");
                        }*/
                        String state = sheet.getCell(0,row).getContents();//광역시, 특별시, 도
                        String city = sheet.getCell(1,row).getContents(); // 시.도,군
                        String town = sheet.getCell(2,row).getContents(); // 읍, 면, 동
                        town.replaceAll(" ","");
                        Log.d("TAG", "state: "+state+","+"city:"+city+"town:"+town+"\n");
                        String m_town = buffer[3].substring(0,buffer[3].length()-1);
                        Log.d("TAG", "m_town: "+m_town+"\n");
                        if(buffer[1].equals(state) && buffer[2].equals(city) && buffer[3].contains(town)){
                            str_x = sheet.getCell(3,row).getContents();
                            str_y = sheet.getCell(4,row).getContents();
                            Log.d("TAG", "address: "+buffer[1]+","+buffer[2]+","+buffer[3]);
                            Log.d("TAG", "information: "+ state+","+city+","+town+","+str_x+","+str_y);
                            break;
                        }else{
                            Log.d("TAG", "miss: "+row+"\n");
                            continue;
                        }

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
        String time_text = new SimpleDateFormat("HHmm",Locale.getDefault()).format(currentTime);
        String hh = time_text.substring(0,2);
        Log.d("TAG", "hhhhhhhhhh: "+hh);
        String mm = time_text.substring(2);

        int int_hh = Integer.parseInt(hh); //int형 시간
        int int_mm = Integer.parseInt(mm);//분을 int형으로 변환

        Log.d("TAG", "int_hh: "+int_hh);
        Log.d("TAG", "int_mm: "+int_mm);

        if(int_mm<30){
            int_hh -= 1;
            String time_hh = Integer.toString(int_hh);
            String time_mm = Integer.toString(int_mm);
            if(time_hh.length()==1)
                time_hh = "0"+time_hh;
            if(time_mm.length()==1)
                time_mm = "0"+time_mm;
            Log.d("TAG", "time_hh:: "+time_hh);
            Log.d("TAG", "time_mm:: "+time_mm);
            Log.d("TAG", "time_mm length:: "+time_mm.length());
            time_text = time_hh+time_mm;
        }

        Log.d("TAG", "mm:: "+mm);

        Log.d("TAG", "date_text=: "+date_text);
        Log.d("TAG", "time_text=: "+time_text);


         StrUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getUltraSrtNcst";
         ServiceKey = "LjJVA0wW%2BvsEsLgyJaBLyTywryRMuelTIYxsWnQTaPpxdZjpuxVCdCtyNxvObDmBJ57VVaSi3%2FerYKQFQmKs8g%3D%3D";
        TotalUrl.append(StrUrl + "?ServiceKey=" + ServiceKey + "&base_date="+date_text+"&base_time="+time_text+"&nx="+str_x+"&ny="+str_y);
        Log.d("TAG", "TotalUrl=: "+TotalUrl);
        DownLoad1 d1 = new DownLoad1();
        d1.execute(String.valueOf(TotalUrl));
       //d1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(TotalUrl));
        Log.d("TAG", "TotalUrl_1: "+TotalUrl+"\n");


        TotalUrl.setLength(0);
        StrUrl = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getCtprvnMesureSidoLIst";
        String sidcd = buffer[1].substring(0,2);
        String str = sidcd+"&searchCondition=HOUR&pageNo=1&numOfRows=1000";

        TotalUrl.append(StrUrl + "?ServiceKey=" + ServiceKey + "&sidoName="+str);


        DownLoad2 d2 = new DownLoad2();
        d2.execute(String.valueOf(TotalUrl));
        //d2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(TotalUrl));
        Log.d("TAG", "TotalUrl_2: "+TotalUrl);
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
            String category = "";   // 카테코리(온도 습도 ....)
            String obsrValue = "";   // 해당 카테코리의 값

            boolean ho_resultCode = false;//결과코드
            boolean ho_category = false;//카테코리
            boolean ho_obsrValue = false;//해당 카테코리 값

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
                            case "category":
                                ho_category = true;
                                break;
                            case "obsrValue":
                                ho_obsrValue = true;
                                break;
                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        if (ho_resultCode) {
                            resultCode = xmlPullParser.getText();
                            ho_resultCode = false;
                        }
                        if (resultCode.equals("00")) {
                            if (ho_category) {
                                category = xmlPullParser.getText();
                                information += category + "/";
                                ho_category = false;
                            }
                            if (ho_obsrValue) {
                                obsrValue = xmlPullParser.getText();//
                                information += obsrValue + "/";
                                list.add(information);
                                ho_obsrValue = false;
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
                //String[]buffer = new String[3];
                int  i = 0,j = 0;
                while (token.hasMoreTokens()){
                    String key = token.nextToken();
                    if(list.size()!=0){ // 대량의 데이터중 추출하려는 데이터가 리스트에  있을경우
                        for(i = 0;i<list.size();i++){
                            if(key.equals(list.get(i))){
                                String value = token.nextToken();
                                map.put(key,value);
                                break;
                            }else
                                continue;
                        }
                    }else // 대량의 데이터들중에 추출하려는 데이터 목록이 없을경우
                        break;
                }

                /*
                ////////// 임시적으로 데이터 확인을 위함 코드(시작) ////////////////
                Set<String> keys = map.keySet();

                for (String key:keys){
                    Log.d("TAG", "key===1: "+key+"\n");
                    Log.d("TAG", "value===1:: "+map.get(key)+"\n");
                }
                ////////// 임시적으로 데이터 확인을 위함 코드(끝) ////////////////*/


                String pty_str = pty.get(map.get("PTY"));//강수형태
                String reh = map.get("REH");//습도
                String t1h = map.get("T1H");//온도
                String t1h_1="";String state_temp="";
                if("-".contains(t1h))
                    state_temp = t1h.substring(0,1);

                if(state_temp.equals("-")){
                    state_temp = "영하";
                    t1h_1 = t1h.substring(1,4);
                }
                else {
                    state_temp = "영상";
                    t1h_1 = t1h;
                }
                pty_textview.setText(pty_str);
                t1h_textview.setText(t1h);
                reh_textview.setText(reh);

                imageView = findViewById(R.id.image_weather);
                switch (pty_str){
                    case "비":
                        imageView.setImageResource(R.drawable.weather_rain);
                        break;
                    case "비/눈":
                        imageView.setImageResource(R.drawable.weather_snowy_rain);
                        break;
                    case "눈":
                        imageView.setImageResource(R.drawable.weather_snowy);
                        break;
                    case "소나기":
                        imageView.setImageResource(R.drawable.weather_rainshower);
                        break;
                    default:
                        imageView.setImageResource(R.drawable.weather_sun);
                        break;

                }

                totalmessage.append("현재 위치하고 계시는 "+buffer[2]+"  "+buffer[3]+"의 날씨를 알려드리겠습니다. 하늘 상태는"+pty_str+"이며 현재 온도는  "+ state_temp+"  "+t1h_1 + "도 입니다.");

                Log.d("TAG", "isCancelled: "+isCancelled());

                Log.d("TAG", "total_information=: "+pty_str+","+reh+","+t1h+"\b");



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
    public class DownLoad2 extends AsyncTask<String, Void, String> {////////////////////// 사용자가 입력한 dgsbjtCd 진료과목을 갖는 병원 파싱

        @Override
        protected String doInBackground(String... url) {
            try {
                Log.d("TAG", "doInBackground2: \n");
                return (String) DownLoadUrl2((String) url[0]);
            } catch (IOException e) {
                return "다운로드 실패";
            }
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            information = "";
            String resultCode = "";   //결과코드
            String cityName = "";   // 도시이름
            String pm10Value = "";   // 미세먼지 10
            String pm25Value = ""; //미세먼지 25
            boolean ho_resultCode = false;//결과코드
            boolean ho_cityName = false;//도시이름
            boolean ho_pm10Value = false;//미세먼지 10
            boolean ho_pm25Value = false;//미세먼지 25

            boolean ho_break = false;
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
                            case "cityName":
                                ho_cityName = true;
                                break;
                            case "pm10Value":
                                ho_pm10Value = true;
                                break;
                            case "pm25Value":
                                ho_pm25Value = true;
                                break;
                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        if (ho_resultCode) {
                            resultCode = xmlPullParser.getText();
                            ho_resultCode = false;
                        }
                        if (resultCode.equals("00")) {
                            if (ho_cityName) {
                                cityName = xmlPullParser.getText();
                                if(cityName.equals(buffer[2])){
                                    information += cityName + "/";
                                    ho_break =true;
                                }

                                ho_cityName = false;

                            }
                            if (ho_pm10Value) {
                                if(ho_break){
                                    pm10Value = xmlPullParser.getText();//
                                    information += pm10Value + "/";
                                    list.add(information);

                                }
                                ho_pm10Value = false;
                            }
                            if (ho_pm25Value) {
                                if(ho_break){
                                    pm25Value = xmlPullParser.getText();//
                                    information += pm25Value + "\n";
                                    list.add(information);
                                    break;
                                }
                                ho_pm25Value = false;
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        ;
                    }
                    eventType = xmlPullParser.next();
                }
                Log.d("TAG", "\ninformation_total_2:" + information); //데이터 파싱 한것을 로그 출력
                StringTokenizer token = new StringTokenizer(information,"/");
                Log.d("TAG", "information_token_totatl_2: "+token.countTokens());
                String[]buffer = new String[token.countTokens()];
                int i =0;
                while (token.hasMoreTokens()){
                    buffer[i] = token.nextToken();  //buffer[0]: 시도이름 buffer[1]: pm10  buffer[2]: pm25
                    buffer[i].replaceAll("\t\r","");
                    i++;
                }
                buffer[2] = buffer[2].substring(0,2);
                Log.d("TAG", "buffer[0]: "+buffer[0].length());
                Log.d("TAG", "buffer[1]: "+buffer[1].length());
                Log.d("TAG", "buffer[2]: "+buffer[2].length());
                int pm10 = Integer.parseInt(buffer[1]);
                int pm25 = Integer.parseInt(buffer[2]);//초미세먼지
                String pm10_state = "";
                String pm25_state = "";
                String message =  "";
                boolean pm10_boolstate = false, pm25_boolstate=false;
                //////미세먼지 등급/////
                if(pm10 <=30){
                    pm10_state = "좋음";
                }else if(pm10<=80){
                    pm10_state = "보통";
                }else if(pm10<150){
                    pm10_state = "나쁨";
                    pm10_boolstate = true;
                }else {
                    pm10_state = "매우나쁨";
                    pm10_boolstate = true;
                }

                ///////초미세먼지 농도/////
                if(pm25 <=15){
                    pm25_state = "좋음";
                }else if(pm25<=35){
                    pm25_state = "보통";
                }else if(pm25<75){
                    pm25_state = "나쁨";
                    pm25_boolstate = true;
                }else{
                    pm25_state = "매우나쁨";    //pm25_state: 초미새먼지  pm10_state: 미세먼지
                    pm25_boolstate = true;
                }

                pm10_textview.setText(pm10_state);
                pm25_textview.setText(pm25_state);
                if(pm10_boolstate||pm25_boolstate){
                    message = "대기질 상태가 안좋으니 외출 시 마스크를 꼭 착용하세요.~";
                    pm10_boolstate = false;
                    pm25_boolstate = false;
                }

                totalmessage.append("\n 미세먼지 농도는"+pm10_state+"이고 초미세먼지 농도는"+pm25_state+"입니다.\n"+message);

                Log.d("TAG", "\npm10_state:"+pm10_state+"\npm25_state:"+pm25_state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public String DownLoadUrl2(String myurl) throws IOException {
            HttpURLConnection com = null;
            BufferedReader bufferedReader;
            try {
                Log.d("TAG", "\n==========DownLoadUrl2==========\n ");
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



    /////////////////  GPS 설정 및 현재 GPS를 통한 나의 경도 위도 값 가져오는 메소드 (시작)//////////////////
    private void startLocationService(){//내 GPS를 이용하여 위도 경도 얻는 함수
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);//위치관리자 생성

        GPSListener gpsListener = new GPSListener();
        long minTime = 1000;//단위 millisecond
        float minDistance = 10;//단위 m

        try{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime,minDistance,gpsListener);;//gps
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,minTime,minDistance,gpsListener);//네트워크
            //5초 마다 or 10m 이동할떄마다 업데이트   network는 gps에 비해 정확도가 떨어짐

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);//최근 gps기록  실내에서는 안잡힐수가 있다
            //location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(location!=null){
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d("TAG", "latitude: "+latitude);
                Log.d("TAG", "longitude: "+longitude);
            }
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }
    /////////////////  GPS 설정 및 현재 GPS를 통한 나의 경도 위도 값 가져오는 메소드 (끝)//////////////////
    private class GPSListener implements LocationListener {//위치리너스 클래스

        @Override
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
    //////////////위도와 경도를 주소로 변환하는 메소드 (시작)/////////////////
    public String getAddress(double lat, double lng){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> address = null;
        try{
            address = geocoder.getFromLocation(lat,lng,3);//3은 최대 결과
        } catch (IOException e) {
            e.printStackTrace();
        }

        Address address1 = address.get(0);
        address.clear();
        return address1.getAddressLine(0).toString();
    }
    //////////////위도와 경도를 주소로 변환하는 메소드 (끝)/////////////////
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
