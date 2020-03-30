package com.example.hiri;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.StringTokenizer;


public class Hos_resultActivity extends AppCompatActivity implements MapView.POIItemEventListener, MapView.CurrentLocationEventListener, MapView.MapViewEventListener, MapView.OpenAPIKeyAuthenticationResultListener, TextToSpeechListener {
    //public MapView mapView;
    double x, y;
    int count = 0;

    String information,showmeRoute,str;
    StringBuilder hos_list;
    String[] buffer;
    Hospital hospital;
    Location location;
    Double latitude = 0.0, longitude = 0.0;
    public HospitalAdapter mAdapter;
    private ArrayList<Hospital> mArrayList;
    private TextToSpeechClient ttsClient;
    public RecyclerView recyclerView;
    public MapView mapView;
    LinearLayoutManager mlinearLayoutManager;
    Button call_button, repeat_button;
    boolean state = false;
    int find_count = 0;
    Intent reco;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pharmacy);
        startLocationService();

        repeat_button = findViewById(R.id.button_repeat);//다시듣기
        call_button = findViewById(R.id.button_voice);//음성호출

        ////////////////// Intent 받은 데이터 추출 과정 //////////////////////
        String yadmNm = getIntent().getStringExtra("yadmNm");
        information = getIntent().getStringExtra("information");
        state = getIntent().getBooleanExtra("state", false);
        find_count = getIntent().getIntExtra("count", 0);

        Log.d("TAG", "yadmNm: " + yadmNm + "\n");
        Log.d("TAG", "information: " + information + "\n");
        Log.d("TAG", "state: " + state + "\n");
        Log.d("TAG", "find_count: " + find_count + "\n");
        //Intent intent1 = new Intent(getApplicationContext(),LoadingActivity.class);
        // startActivity(intent1);

        int count = 0;
        hos_list = new StringBuilder();

        recyclerView = findViewById(R.id.recyclerView);
        mlinearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(mlinearLayoutManager);
        recyclerView.setHasFixedSize(true);

        mArrayList = new ArrayList<>();
        mAdapter = new HospitalAdapter(mArrayList);
        recyclerView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), mlinearLayoutManager.getOrientation());//구분선을 넣기 위함

        recyclerView.addItemDecoration(dividerItemDecoration);


        ////////////////////////카카오 음성 뉴톤톡(tts)////////////////////////////
        ttsClient = new TextToSpeechClient.Builder()   //음성 합성 포맷
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2)     // NEWTONE_TALK_1  통합 음성합성방식   NEWTONE_TALK_2 편집 합성 방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM)  //TTS 음색 모드 설정(여성 차분한 낭독체)
                //VOICE_MAN_READ_CALM  남성 차분한 낭독체
                //VOICE_WOMAN_DIALOG_BRIGHT  여성 밝은 대화체
                //VOICE_MAN_DIALOG_BRIGHT 남성 밝은 대화체
                .setListener(this)
                .build();

        //////////////////////구글 음성인식 기반(STT)//////////////////////
        reco = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //구글 기반의 음성인식 객체 선언
        reco.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName()); //구글 기반의 음성인식 패키지 설정
        reco.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-kr");//구글 기반 음성인식 언어 설정

        StringTokenizer token = new StringTokenizer(information, "\n");
        buffer = new String[token.countTokens()];
        while (token.hasMoreTokens()) { //병원 별로 자르기
            buffer[count] = token.nextToken();
            buffer[count] = buffer[count].replaceAll("\n", "");
            Log.d("TAG", "\nbuffer[" + count + "]=" + buffer[count] + "\n");
            count++;
        }

        hos_list.append(yadmNm + "를 진료하는 병원 총\n" + find_count + "건을 \n 검색하였습니다. 병원 목록을 불러 드리겠습니다.\n");
        String[] buffer2 = new String[7];
        for (int i = 0; i < buffer.length; i++) {//병원의 정보 자르기
            token = new StringTokenizer(buffer[i], "/");
            Log.d("TAG", "buffer[" + i + "]=" + buffer[i] + "\n");
            //buffer 0: 병원 주소  buffer1: 병원규모 buffer 2: 전화번호  buffer 3 : y좌표  buffer 4: x 좌표  , buffer 5: 병원이름, buffer 6: 병원등급
            buffer2[0] = token.nextToken(); //주소
            buffer2[1] = token.nextToken(); //규모
            buffer2[2] = token.nextToken(); //전화번호
            buffer2[3] = token.nextToken(); // y좌표
            buffer2[4] = token.nextToken(); //x 좌표
            buffer2[5] = token.nextToken(); // 병원 이름
            buffer2[6] = token.nextToken(); // 병원등급
            hos_list.append(i + 1 + "번 \n" + buffer2[5] + "\n");
            x = Double.parseDouble(buffer2[4]);
            y = Double.parseDouble(buffer2[3]);
            Log.d("TAG", "buffer2[0]: " + buffer2[0] + "buffer2[1]: " + buffer2[1] + "buffer2[2]: " + buffer2[2] + "buffer2[3]: " + buffer2[3] + "buffer2[4]: " + buffer2[4]
                    + "buffer2[5]: " + buffer2[5] + "buffer2[6]: " + buffer2[6]);
            String[] array = buffer2[0].split("\\(");//주소의 (읍,면,동) 의 정보 삭제
            hospital = new Hospital(array[0], buffer2[1], buffer2[2], buffer2[5], x, y);// 주소, 규모 ,전화번호, 약국이름, x,y
            mArrayList.add(hospital);
            mAdapter.notifyDataSetChanged();

        }
        hos_list.append("\n이상입니다. 원하시는 병원에 전화를 걸고 싶으시거나 \n 길을 찾고 싶으시면 \n 오른쪽 상단 음성 호출버튼을 눌러주세요.");
        while(ttsClient.isPlaying())
            ;
        if (state) {
            ttsClient.stop();
            ttsClient.play(hos_list.toString());
        }


        ////////////카카오 지도 관련 코드/////////////
        mapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.mapview);
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);
        mapView.setOpenAPIKeyAuthenticationResultListener(this);
        mapView.setCurrentLocationEventListener(this);

        //mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(x, y), true);//

        // 줌 레벨 변경
        // mapView.setZoomLevel(1, true);
        // 중심점 변경 + 줌 레벨 변경
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(mAdapter.getItem(0).getX(), mAdapter.getItem(0).getY()), 1, true); //짇
        // 줌 인
        mapView.zoomIn(true);
        // 줌 아웃
        mapView.zoomOut(true);

        repeat_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { ////////////다시 듣기 버튼 구현
                if (state) {
                    ttsClient.stop();
                    while(ttsClient.isPlaying())
                        ;
                    ttsClient.play(hos_list.toString());
                }
            }
        });

        call_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsClient.stop();
                String text = "음성호출 버튼을 누르셨습니다. \n 누르신 후에  띵동 소리가 나면 \n그 후에 병원의 번호와 기능을  말씀하세요.  \n 예를 들어 1번 길 찾아줘";
                while(ttsClient.isPlaying())
                    ;
                ttsClient.play(text);
                while (ttsClient.isPlaying())
                    ;
                SpeechRecognizer Speech = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());//getApplicationContext:현재 엑티비티
                Speech.setRecognitionListener(listener);
                Speech.startListening(reco);
            }
        });

        //////////// 지도 위 병원 마크 ///////////////
        MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(x, y); /////초기 지도 화면 시점 지점
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName(buffer2[4]); //////마커의 이름을 설정
        marker.setTag(0);
        marker.setMapPoint(MARKER_POINT);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapView.addPOIItem(marker);

        mAdapter.setOnItemClickListener(new HospitalAdapter.OhHospitalItemClickListener() {
            @Override
            public void onItemClick(HospitalAdapter.ViewHolder holder, View view, int position) {
                double ph_latitude = mAdapter.getItem(position).getX();   /////////// 리스트 목록의 병원들을 터치할떄 해당 병원의 x좌표를 가져옴
                double ph_longitude = mAdapter.getItem(position).getY();    /////////// 리스트 목록의 병원들을 터치할떄 해당 병원의 y좌표를 가져옴

                String phr_name = mAdapter.getItem(position).getYadmNm();   /////////// 리스트 목록의 병원들을 터치할떄 해당 병원의 이름을 가져옴
                mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(ph_latitude, ph_longitude), 1, true);
                MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(ph_latitude, ph_longitude);  /////////지도의 시점을 병원의 위치 설정
                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(phr_name);
                marker.setTag(0);
                marker.setMapPoint(MARKER_POINT);
                marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                mapView.addPOIItem(marker);
            }

            @Override
            public void onCallClick(int position) {//////////전화걸기
                String telno = mAdapter.getItem(position).getTelno();
                Log.d("TAG", "telno: " + telno);
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + telno));
                startActivity(intent);
            }

            @Override
            public void onPath(int position) {//////////////경로 탐색
                double ph_latitude = mAdapter.getItem(position).getX();
                double ph_longitude = mAdapter.getItem(position).getY();
                Log.d("TAG", "latitude: " + latitude);
                Log.d("TAG", "longitude: " + longitude);

                Log.d("TAG", "ph_latitude: " + ph_latitude);
                Log.d("TAG", "ph_longitude: " + ph_longitude);
                showmeRoute = "daummaps://route?sp=" + latitude + "," + longitude + "&ep=" + ph_latitude + "," + ph_longitude + "&by=FOOT";  //출발지 도착지
                android.net.Uri u = android.net.Uri.parse(showmeRoute);
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(showmeRoute));

                i.setData(u);
                startActivity(i);
            }
        });
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {  //사용자가 MapView 에 등록된 POI Item 아이콘(마커)를 터치한 경우 호출된다.

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {//단말 사용자가 길게 누른 후(long press) 끌어서(dragging) 위치 이동이 가능한 POI Item의 위치를 이동시킨 경우 호출된다

    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) { // 단말의 현위치 좌표값을 통보받을 수 있다.

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) { //단말의 방향(Heading) 각도값을 통보받을 수 있다.

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) { //현위치 갱신 작업에 실패한 경우 호출된다.

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {//현위치 트랙킹 기능이 사용자에 의해 취소된 경우 호출된다.
        //처음 현위치를 찾는 동안에 현위치를 찾는 중이라는 Alert Dialog 인터페이스가 사용자에게 노출된다.
        //첫 현위치를 찾기전에 사용자가 취소 버튼을 누른 경우 호출 된다.

    }

    @Override
    public void onMapViewInitialized(MapView mapView) { // MapView가 사용가능 한 상태가 되었을 떄 알려주는데 메소드

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {// 지도 중심 좌표가 이동한 경우 호출

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {// 지도 확대 / 축소 레벨이 변경된 경우 호출

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) { // 지도 위를 터지한 경우 호출

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {// 지도 위 한 지점을 더블 터치한 경우

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {// 지도 위 한 지점을 길게 누른 경우 호출

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {//지도 드래그를 시작한 경우 호출

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {//지도 드래그를 끝낸 경우 호출

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {//지도의 이동이 완료된 경우 호출하는 메소드

    }

    @Override
    public void onDaumMapOpenAPIKeyAuthenticationResult(MapView mapView, int i, String s) { // 발급받은 인즈KEY 값을 서버에 요청하여 인증 여부를 통보받는 메소드

    }

    private void startLocationService() {//내 GPS를 이용하여 위도 경도 얻는 함수
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);//위치관리자 생성

        GPSListener gpsListener = new GPSListener();
        long minTime = 1000;//단위 millisecond
        float minDistance = 10;//단위 m

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, gpsListener); //// 위치 기반을 GPS모듈을 이용함
            ;//gps
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,minTime,minDistance,gpsListener);//// 위치 기반을 네트워크 모듈을 이용함
            //5초 마다 or 10m 이동할떄마다 업데이트   network는 gps에 비해 정확도가 떨어짐

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);//최근 gps기록  실내에서는 안잡힐수가 있다
            //location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);    ///네트워크로 얻은 마지막 위치좌표를 이용

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d("TAG", "latitude: " + latitude);
                Log.d("TAG", "longitude: " + longitude);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFinished() {

    }

    @Override
    public void onError(int code, String message) {

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
            StringTokenizer token;
            String[]buffer;
            int position,i=0;
            android.net.Uri u;
            Intent intent;
            String name;

            ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);//ArrayList는 크기를 미리 정하지 않아 얼마든지 많은 수의 데이터를 저장
            str = matches.get(0);
            Log.d("TAG", "strstrstrstr: " + str + "\n");
            if(str.contains("길")){////////////////////길찾기
                Log.d("TAG", "길찾기: " + str + "\n");

                token = new StringTokenizer(str,"길");
                buffer = new String[token.countTokens()];
                while(token.hasMoreTokens()){
                    buffer[i] = token.nextToken();
                    i++;
                }
                buffer[0] = buffer[0].substring(0,buffer[0].length()-2); //문자끝 자르기
                //buffer[0].replace("일","1");
                //buffer[0].replace("이","2");
                if(buffer[0].equals("일")){
                    buffer[0] = "1";
                }else if(buffer[0].equals("이"))
                    buffer[0] = "2";

                Log.d("TAG", "buffer[0].length(): "+buffer[0].length()+"\n");
                if(buffer[0].length()==2) { //10번

                    Log.d("TAG", "buffer[0]1: " + buffer[0] + "\n");
                    position = Integer.parseInt(buffer[0]);

                    double ph_latitude = mAdapter.getItem(position-1).getX();
                    double ph_longitude = mAdapter.getItem(position-1).getY();
                    name = mAdapter.getItem(position-1).getYadmNm();

                    Log.d("TAG", "latitude: " + latitude);
                    Log.d("TAG", "longitude: " + longitude);

                    Log.d("TAG", "ph_latitude: " + ph_latitude);
                    Log.d("TAG", "ph_longitude: " + ph_longitude);
                    showmeRoute = "daummaps://route?sp=" + latitude + "," + longitude + "&ep=" + ph_latitude + "," + ph_longitude + "&by=FOOT";  //출발지 도착지
                    u = android.net.Uri.parse(showmeRoute);
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(showmeRoute));

                    ttsClient.stop();
                    while (ttsClient.isPlaying())
                        ;
                    ttsClient.play("현재 위치에서 "+name + "까지 경로를 탐색합니다.");
                    intent.setData(u);
                    startActivity(intent);
                }else{ // 한자리 번호

                    Log.d("TAG", "buffer[0].length(): "+buffer[0].length()+"\n");
                    Log.d("TAG", "buffer[0]2: " + buffer[0] + "\n");

                    position = Integer.parseInt(buffer[0]);

                    double ph_latitude = mAdapter.getItem(position-1).getX();
                    double ph_longitude = mAdapter.getItem(position-1).getY();
                    name = mAdapter.getItem(position-1).getYadmNm();

                    Log.d("TAG", "latitude: " + latitude);
                    Log.d("TAG", "longitude: " + longitude);

                    Log.d("TAG", "ph_latitude: " + ph_latitude);
                    Log.d("TAG", "ph_longitude: " + ph_longitude);
                    showmeRoute = "daummaps://route?sp=" + latitude + "," + longitude + "&ep=" + ph_latitude + "," + ph_longitude + "&by=FOOT";  //출발지 도착지
                    u = android.net.Uri.parse(showmeRoute);
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(showmeRoute));


                    ttsClient.stop();
                    while (ttsClient.isPlaying())
                        ;
                    ttsClient.play("현재 위치에서 "+name + "까지 경로를 탐색합니다.");

                    intent.setData(u);
                    startActivity(intent);

                }
            }else if(str.contains("전화")){/////////////////////////전화
                Log.d("TAG", "전화: " + str + "\n");
                token = new StringTokenizer(str,"전화");
                buffer = new String[token.countTokens()];

                while(token.hasMoreTokens()){
                    buffer[i] = token.nextToken();
                    i++;
                }
                buffer[0] = buffer[0].substring(0,buffer[0].length()-2); //문자끝 자르기
                //buffer[0].replace("일","1");
                //buffer[0].replace("이","2");
                if(buffer[0].equals("일")){
                    buffer[0] = "1";
                }else if(buffer[0].equals("이"))
                    buffer[0] = "2";
                Log.d("TAG", "buffer[0].length(): "+buffer[0].length()+"\n");
                if(buffer[0].length()==3) { //10번

                    Log.d("TAG", "buffer[0]1: " + buffer[0] + "\n");
                    position = Integer.parseInt(buffer[0]);

                    Log.d("TAG", "latitude: " + latitude);
                    Log.d("TAG", "longitude: " + longitude);


                    String telno = mAdapter.getItem(position-1).getTelno();
                    name = mAdapter.getItem(position-1).getYadmNm();
                    Log.d("TAG", "telno: " + telno);
                    intent = new Intent(Intent.ACTION_CALL);
                    ttsClient.stop();
                    while (ttsClient.isPlaying())
                        ;
                    ttsClient.play(name + "에 통화 연결를 하겠습니다.");
                    while (ttsClient.isPlaying())
                        ;
                    intent.setData(Uri.parse("tel:" + telno));
                    startActivity(intent);
                }else{ // 한자리 번호

                    Log.d("TAG", "buffer[0]2: " + buffer[0] + "\n");

                    position = Integer.parseInt(buffer[0]);


                    String telno = mAdapter.getItem(position-1).getTelno();
                    name = mAdapter.getItem(position-1).getYadmNm();

                    Log.d("TAG", "telno: " + telno);
                    intent = new Intent(Intent.ACTION_CALL);
                    ttsClient.stop();
                    while (ttsClient.isPlaying())
                        ;
                    ttsClient.play(name + "에 통화 연결를 하겠습니다.");
                    while (ttsClient.isPlaying())
                        ;
                    intent.setData(Uri.parse("tel:" + telno));
                    startActivity(intent);
                }
            }

            matches.clear();

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };
    public void onDestroy() {
        super.onDestroy();
        if(ttsClient!=null){
            ttsClient.stop();
        }
    }
}
