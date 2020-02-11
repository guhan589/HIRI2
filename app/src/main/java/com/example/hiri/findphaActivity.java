package com.example.hiri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.kakao.sdk.newtoneapi.TextToSpeechClient;
import com.kakao.sdk.newtoneapi.TextToSpeechListener;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class findphaActivity extends AppCompatActivity implements TextToSpeechListener, MapView.POIItemEventListener, MapView.OpenAPIKeyAuthenticationResultListener, MapView.CurrentLocationEventListener, MapView.MapViewEventListener {
    private LocationManager locationMgr;
    public TextToSpeechClient ttsClient;
    public RecyclerView recyclerView;
    public MapView mapView;
    private ArrayList<Pharmacy> mArrayList;
    public PharmacyAdapter mAdapter;
    Pharmacy pharmacy;
    LinearLayoutManager mlinearLayoutManager;

    Button recall_btn, call_btn, telno_btn;
    Double latitude = 0.0, longitude = 0.0;
    int count =0;
    Location location;
    String getData="", information="",StrUrl,ServiceKey;
    StringBuilder TotalUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pharmacy);
        startLocationService();
        recall_btn = findViewById(R.id.button_repeat);
        call_btn = findViewById(R.id.button_voice);
        //telno_btn = findViewById(R.id.button_calling);


        recyclerView = findViewById(R.id.recyclerView);
        mlinearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(mlinearLayoutManager);
        recyclerView.setHasFixedSize(true);

        mArrayList = new ArrayList<>();
        mAdapter = new PharmacyAdapter(mArrayList);
        recyclerView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),mlinearLayoutManager.getOrientation());//구분선을 넣기 위함
        recyclerView.addItemDecoration(dividerItemDecoration);

        ////////////카카오 지도 관련 코드/////////////
        mapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.mapview);
        mapViewContainer.addView(mapView);


        mapView.setMapViewEventListener( this);
        mapView.setPOIItemEventListener(this);

        mapView.setOpenAPIKeyAuthenticationResultListener(this);

        mapView.setCurrentLocationEventListener(this);

        //mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(x, y), true);//

// 줌 레벨 변경
        // mapView.setZoomLevel(1, true);

// 중심점 변경 + 줌 레벨 변경
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(latitude, longitude), 1, true); //짇

// 줌 인
        mapView.zoomIn(true);

// 줌 아웃
        mapView.zoomOut(true);


        //////////// 지도 위 병원 마크 ///////////////
        MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(latitude, longitude);
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("현재 위치");
        marker.setTag(0);
        marker.setMapPoint(MARKER_POINT);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapView.addPOIItem(marker);

        mAdapter.setOnItemClickListener(new PharmacyAdapter.OnPharmacyItemClickListener(){
            @Override
            public void onItemClick(PharmacyAdapter.ViewHolder holder, View view, int position) {
                double ph_latitude = mAdapter.getItem(position).getX();
                double ph_longitude = mAdapter.getItem(position).getY();
                String phr_name = mAdapter.getItem(position).getYadmNm();
                mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(ph_latitude, ph_longitude), 1, true);
                MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(ph_latitude, ph_longitude);
                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(phr_name);
                marker.setTag(0);
                marker.setMapPoint(MARKER_POINT);
                marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

                mapView.addPOIItem(marker);
            }

            @Override
            public void onCallClick(int position) {
                String telno = mAdapter.getItem(position).getTelno();
                Log.d("TAG", "telno: "+telno);
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+telno));
                startActivity(intent);
            }
        });
        recall_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        call_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ttsClient = new TextToSpeechClient.Builder()
                .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_2  )     // NEWTONE_TALK_1  통합 음성합성방식   NEWTONE_TALK_2 편집 합성 방식
                .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
                .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM    )  //TTS 음색 모드 설정(여성 차분한 낭독체)
                //VOICE_MAN_READ_CALM  남성 차분한 낭독체
                //VOICE_WOMAN_DIALOG_BRIGHT  여성 밝은 대화체
                //VOICE_MAN_DIALOG_BRIGHT 남성 밝은 대화체
                .setListener(this)
                .build();
        double x = latitude;
        double y  = longitude;

        TotalUrl = new StringBuilder();
        StrUrl = "http://apis.data.go.kr/B551182/pharmacyInfoService/getParmacyBasisList";
        ServiceKey = "LjJVA0wW%2BvsEsLgyJaBLyTywryRMuelTIYxsWnQTaPpxdZjpuxVCdCtyNxvObDmBJ57VVaSi3%2FerYKQFQmKs8g%3D%3D";
        TotalUrl.append(StrUrl + "?ServiceKey=" + ServiceKey + "&xPos="+longitude+"&yPos="+latitude+"&radius=1000");
        DownLoad1 d1 = new DownLoad1();
        Log.d("TAG", "TotalUrl: "+TotalUrl);

        d1.execute(String.valueOf(TotalUrl));
        Log.d("TAG", "latitude: "+x+"latitude:"+y);
    }

    @Override
    public void onFinished() {

    }

    @Override
    public void onError(int code, String message) {

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
            String addr = "";   // 중기예보데이터
            String distance = "";
            String telno = "";
            String xpos =  "";
            String ypos = "";
            String yadmNm = "";

            boolean ho_resultCode = false;//결과코드
            boolean ho_addr = false;// 중기예보 tag
            boolean ho_distance = false;
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
                            case "addr":
                                ho_addr = true;
                                break;
                            case "distance":
                                ho_distance = true;
                                break;
                            case "telno":
                                ho_telno = true;
                                break;
                            case "XPos":
                                ho_xpos = true;
                                break;
                            case "YPos":
                                ho_ypos = true;
                                break;
                            case "yadmNm":
                                ho_yadmNM = true;
                                break;

                        }
                    } else if (eventType == XmlPullParser.TEXT) {
                        if (ho_resultCode) {
                            resultCode = xmlPullParser.getText();
                            ho_resultCode = false;
                        }
                        if (resultCode.equals("00")) {
                            if (ho_addr) {
                                addr = xmlPullParser.getText();
                                information += addr + "/";
                                ho_addr = false;
                            }else if(ho_distance){
                                distance = xmlPullParser.getText();
                                information += distance + "/";
                                ho_distance = false;
                            }
                            else if(ho_telno){
                                telno = xmlPullParser.getText();
                                information += telno + "/";
                                ho_telno = false;
                            }
                            else if(ho_xpos){
                                xpos = xmlPullParser.getText();
                                information += xpos + "/";
                                ho_xpos = false;
                            }
                            else if(ho_ypos){
                                ypos = xmlPullParser.getText();
                                information += ypos + "/";
                                ho_ypos = false;
                            }
                            else if(ho_yadmNM){
                                yadmNm = xmlPullParser.getText();
                                information += yadmNm + "\n";
                                ho_yadmNM = false;
                            }

                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        ;
                    }
                    eventType = xmlPullParser.next();

                }


                Log.d("TAG", "\ninformation_total:" + information); //데이터 파싱 한것을 로그 출력

                StringTokenizer token = new StringTokenizer(information,"\n");
                String[] buffer = new String[token.countTokens()];
                int i = 0;
                while(token.hasMoreTokens()){
                    buffer[i] = token.nextToken();
                    Log.d("TAG", "buffer====>: "+buffer[i]);
                    i++;

                }
                ;
                Log.d("TAG", "information_token_totatl: "+token.countTokens());
                String[] buffer2 = new String[6];

                for(int count = 0; count<buffer.length;count++){
                    token = new StringTokenizer(buffer[count],"/");
                    i=0;
                    while(token.hasMoreTokens()){
                        buffer2[i] = token.nextToken();
                        i++;
                    }
                    int distance1 = (int) Double.parseDouble(buffer2[1]); // 거리 소수점 버리기 위해서
                    buffer2[1] = Integer.toString(distance1); //문자열 변환
                    String[] array = buffer2[0].split("\\(");//주소의 (읍,면,동) 의 정보 삭제

                    for(i=0;i<array.length;i++)
                        Log.d("TAG", "array["+i+"]="+array[i]);
                        Log.d("TAG", "\nbuffer[0]: "+buffer2[0]+"\n");
                        Log.d("TAG", "\nbuffer[1]: "+buffer2[1]+"\n");
                        Log.d("TAG", "\ndistance1(int): "+distance1+"\n");
                        Log.d("TAG", "\nbuffer[2]: "+buffer2[2]+"\n");
                        Log.d("TAG", "\nbuffer[3]: "+buffer2[3]+"\n");
                        Log.d("TAG", "\nbuffer[4]: "+buffer2[4]+"\n");
                        Log.d("TAG", "\nbuffer[5]: "+buffer2[5]+"\n");

                    pharmacy =new Pharmacy(array[0],buffer2[1],buffer2[2],buffer2[5],Double.parseDouble(buffer2[4]),Double.parseDouble(buffer2[3]));// 주소, 거리, 전화번호 , 약국이름
                    mArrayList.add(pharmacy);
                    mAdapter.notifyDataSetChanged();

                }



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
}
