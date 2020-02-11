package com.example.hiri;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class Hos_resultActivity extends AppCompatActivity implements MapView.POIItemEventListener, MapView.CurrentLocationEventListener, MapView.MapViewEventListener, MapView.OpenAPIKeyAuthenticationResultListener {
    //public MapView mapView;
    double x, y;
    int count = 0 ;
    String information;
    String[] buffer;
    Hospital hospital;
    public HospitalAdapter mAdapter;
    private ArrayList<Hospital> mArrayList;
    public RecyclerView recyclerView;
    public MapView mapView;
    LinearLayoutManager mlinearLayoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_pharmacy);
        ////////////////// Intent 받은 데이터 추출 과정 //////////////////////
        String yadmNm = getIntent().getStringExtra("yadmNm");
        information = getIntent().getStringExtra("information");

        Log.d("TAG", "yadmNm: "+yadmNm+"\n");
        Log.d("TAG", "information: "+information+"\n");


        int count = 0;
        recyclerView = findViewById(R.id.recyclerView);
        mlinearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(mlinearLayoutManager);
        recyclerView.setHasFixedSize(true);

        mArrayList = new ArrayList<>();
        mAdapter = new HospitalAdapter(mArrayList);
        recyclerView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),mlinearLayoutManager.getOrientation());//구분선을 넣기 위함

        recyclerView.addItemDecoration(dividerItemDecoration);

        StringTokenizer token = new StringTokenizer(information, "\n");
        buffer = new String[token.countTokens()];
        while (token.hasMoreTokens()) {
            buffer[count] = token.nextToken();
            buffer[count] = buffer[count].replaceAll("\n", "");
            Log.d("TAG", "\nbuffer["+count+"]="+ buffer[count]+"\n");
            count++;
        }
        String[] buffer2 = new String[7];

        for(int i = 0 ; i<buffer.length;i++){
            token = new StringTokenizer(buffer[i], "/");
            Log.d("TAG", "buffer["+i+"]="+buffer[i]+"\n");
            //buffer 0: 병원 주소  buffer1: 병원규모 buffer 2: 전화번호  buffer 3 : y좌표  buffer 4: x 좌표  , buffer 5: 병원이름, buffer 6: 병원등급
            buffer2[0] = token.nextToken(); //주소
            buffer2[1] = token.nextToken(); //규모
            buffer2[2] = token.nextToken(); //전화번호
            buffer2[3] = token.nextToken(); // y좌표
            buffer2[4] = token.nextToken(); //x 좌표
            buffer2[5] = token.nextToken(); // 병원 이름
            buffer2[6] = token.nextToken(); // 병원등급

            x = Double.parseDouble(buffer2[4]);
            y = Double.parseDouble(buffer2[3]);
            Log.d("TAG", "buffer2[0]: "+buffer2[0]+"buffer2[1]: "+buffer2[1]+"buffer2[2]: "+buffer2[2]+"buffer2[3]: "+buffer2[3]+"buffer2[4]: "+buffer2[4]
            +"buffer2[5]: "+buffer2[5]+"buffer2[6]: "+buffer2[6]);
            String[] array = buffer2[0].split("\\(");//주소의 (읍,면,동) 의 정보 삭제
            hospital = new Hospital(array[0],buffer2[1],buffer2[2],buffer2[5],x,y);// 주소, 규모 ,전화번호, 약국이름, x,y
            mArrayList.add(hospital);
            mAdapter.notifyDataSetChanged();

        }


        Log.d("TAG", "Hos_resultActivtiy  x: "+x);
        Log.d("TAG", "Hos_resultActivtiy  y: "+y);


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
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(x, y), 1, true); //짇

// 줌 인
        mapView.zoomIn(true);

// 줌 아웃
        mapView.zoomOut(true);


        //////////// 지도 위 병원 마크 ///////////////
        MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(x, y);
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName(buffer2[4]);
        marker.setTag(0);
        marker.setMapPoint(MARKER_POINT);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapView.addPOIItem(marker);

        mAdapter.setOnItemClickListener(new HospitalAdapter.OhHospitalItemClickListener(){
            @Override
            public void onItemClick(HospitalAdapter.ViewHolder holder, View view, int position) {
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
}
