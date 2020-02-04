package com.example.hiri;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

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

public class search_hosActivity extends AppCompatActivity {
    String getData;
    int count = 0, num = 0;
    String count1 = "", totalyadmNm = "", information = "", buffer = "", str = "", yadmNm1 = "";
    ArrayList<String> list, list2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_hos);

        list = new ArrayList<String>();
        list2 = new ArrayList<String>();


        yadmNm1 = getIntent().getStringExtra("yadmNum");
        String dgsbjtCd = getIntent().getStringExtra("dgsbjtCd");//findhospActivity에서 넘겨준 진료과목 코드
        Log.d("TAG", "search_hos_yadmNm1: " + yadmNm1);
        Log.d("TAG", "search_hos_dgsbjtCd: " + dgsbjtCd);


        String StrUrl = "http://apis.data.go.kr/B551182/hospInfoService/getHospBasisList";
        String ServiceKey = "LjJVA0wW%2BvsEsLgyJaBLyTywryRMuelTIYxsWnQTaPpxdZjpuxVCdCtyNxvObDmBJ57VVaSi3%2FerYKQFQmKs8g%3D%3D";
        String TotalUrl = StrUrl + "?ServiceKey=" + ServiceKey + "&sgguCd=320400&dgsbjtCd=" + dgsbjtCd;
        DownLoad1 d1 = new DownLoad1();
        d1.execute(TotalUrl);

        list.clear();
    }

    public class DownLoad1 extends AsyncTask<String, Void, String> {////////////////////// 사용자가 입력한 dgsbjtCd 진료과목을 갖는 병원 파싱

        @Override
        protected String doInBackground(String... url) {
            try {
                Log.d("TAG", "search_hos_Activity_doInBackground: ");
                return (String) DownLoadUrl1((String) url[0]);
            } catch (IOException e) {
                return "다운로드 실패";
            }
        }

        protected void onPostExecute(String result) {
            //super.onPostExecute(result);
            String resultCode = "";   //결과코드
            String yadmNm = "";   //요양기관 이름
            String addr = "";   //요양기관 주소
            String telno = "";   //요양기관 전화번호
            String XPos = "";   //요양기관x좌표
            String YPos = "";  //요양기관 y좌표

            boolean ho_resultCode = false;
            boolean ho_yadmNm = false;//요양기관 이름
            boolean ho_addr = false;//요양기관 주소
            boolean ho_telno = false;//요양기관 전화번호
            boolean ho_XPos = false;//요양기관 x좌료
            boolean ho_YPos = false;
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
                                Log.d("TAG", "resultCode");
                                ho_resultCode = true;
                                break;
                            case "yadmNm":
                                Log.d("TAG", "yadmNm");
                                ho_yadmNm = true;
                                break;
                            case "addr":
                                Log.d("TAG", "addr");
                                ho_addr = true;
                                break;
                            case "telno":
                                Log.d("TAG", "telno");
                                ho_telno = true;
                                break;
                            case "XPos":
                                Log.d("TAG", "XPos");
                                ho_XPos = true;
                                break;
                            case "YPos":
                                Log.d("TAG", "YPos");
                                ho_YPos = true;
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
                            }
                            if (ho_yadmNm) {
                                yadmNm = xmlPullParser.getText();//요양기관 이름
                                String countname = Integer.toString(count + 1);
                                String name = countname + "번" + yadmNm + "\n";
                                //totalyadmNm = totalyadmNm + name;//병원 목록 음성 출력
                                information = information + yadmNm + "/";//병원 정보 문자열
                                list.add(information);
                                //textView.append(information+list.size()+"\n");
                                Log.d("TAG", "\n==========DownLoad1_ho_yadmNm===========: \n");
                                String StrUrl2 = "http://apis.data.go.kr/B551182/hospAsmRstInfoService/getGnhpSprmAsmRstList";
                                String ServiceKey2 = "LjJVA0wW%2BvsEsLgyJaBLyTywryRMuelTIYxsWnQTaPpxdZjpuxVCdCtyNxvObDmBJ57VVaSi3%2FerYKQFQmKs8g%3D%3D";
                                String TotalUrl2 = StrUrl2 + "?ServiceKey=" + ServiceKey2 + "&sgguCd=320400&yadmNm=" + yadmNm;
                                DownLoad2 d2 = new DownLoad2();
                                d2.execute(TotalUrl2);
                                information = "";
                                count++;
                                ho_yadmNm = false;
                            }
                            if (ho_telno) {
                                telno = xmlPullParser.getText();
                                //textView.append(count+"]"+telno+"\n");
                                information = information + telno + "/";
                                ho_telno = false;
                            }
                            if (ho_XPos) {
                                XPos = xmlPullParser.getText();
                                //textView.append(count+"]"+XPos+"\n");
                                information = information + XPos + "/";
                                ho_XPos = false;
                            }
                            if (ho_YPos) {
                                YPos = xmlPullParser.getText();
                                //textView.append(count+"]"+YPos+"\n");
                                information = information + YPos + "/";
                                ho_YPos = false;
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        ;
                    }
                    eventType = xmlPullParser.next();
                }
                Log.d("TAG", "information2" + information);
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

    public class DownLoad2 extends AsyncTask<String, Void, String> {//////////////////////////////// 2차 파싱 해당 병원에 대한 등급 파싱

        @Override
        protected String doInBackground(String... url) {
            try {
                return (String) DownLoadUrl2((String) url[0]);
            } catch (IOException e) {
                return "다운로드 실패";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(list.get(0));
            String resultCode = "";
            String asmGrd = "";

            boolean ho_resultCode = false;//결과코드
            boolean ho_body = false;
            boolean ho_item = false;//세부하목
            boolean ho_asmGrd = false;//평가등급
            boolean ho_addr = false;//요양기관주소
            //int num = 0;
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
                            case "body":
                                ho_body = true;
                                break;
                            case "item":
                                ho_item = true;
                                break;
                            case "addr":
                                ho_addr = true;
                                break;
                            case "asmGrd":
                                ho_asmGrd = true;
                                break;
                        }
                    } else if (eventType == XmlPullParser.TEXT) {

                        if (ho_resultCode) {
                            resultCode = xmlPullParser.getText();
                            ho_resultCode = false;
                        }
                        if (resultCode.equals("00") && ho_body == true && ho_item == true) {
                            if (ho_addr && ho_asmGrd) {
                                asmGrd = xmlPullParser.getText();
                                String str = list.get(num) + asmGrd + "\n";//asmGrd 평가등급
                                list.set(num, str);//해당병원 평점 넣기
                                num++;
                                ho_asmGrd = false;
                                ho_addr = false;
                                ho_body = false;
                                ho_item = false;
                            }
                        } else if (resultCode.equals("00") && ho_body == true && ho_item == false) {
                            String str = list.get(num) + "6\n";//asmGrd 평가등급이 없으면 null
                            list.set(num, str);//해당병원 평점 넣기
                            //textView.append(str+list.size());
                            num++;
                            ho_body = false;
                        }

                    } else if (eventType == XmlPullParser.END_TAG) {
                        ;
                    }
                    eventType = xmlPullParser.next();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (num == 10) {
                Log.d("TAG", "\n==========num==10===========: \n");
                for (int i = 0; i < num; i++) {
                    for (int j = 0; j < num - i - 1; j++) {
                        StringTokenizer token1 = new StringTokenizer(list.get(j), "/");
                        StringTokenizer token2 = new StringTokenizer(list.get(j + 1), "/");
                        String[] buffer1 = new String[token1.countTokens()];
                        String[] buffer2 = new String[token2.countTokens()];
                        int count1 = 0;
                        while (token1.hasMoreTokens()) {
                            buffer1[count1] = token1.nextToken();
                            buffer1[count1] = buffer1[count1].replaceAll("\n", "");
                            count1++;
                        }
                        count1 = 0;
                        while (token2.hasMoreTokens()) {
                            buffer2[count1] = token2.nextToken();
                            buffer2[count1] = buffer2[count1].replaceAll("\n", "");
                            count1++;
                        }
                        int a = Integer.parseInt(buffer1[5]);
                        int b = Integer.parseInt(buffer2[5]);
                        if (a > b) {
                            list2.add(list.get(j));
                            list.set(j, list.get(j + 1));
                            list.set(j + 1, list2.get(0));
                            list2.clear();
                        }
                    }
                }
                for (int i = 0; i < num; i++) {
                    //textView.append(list.get(i)+"\n");
                    totalyadmNm += list.get(i) + "\n";
                }
                Intent intent = new Intent(getApplicationContext(), view_hosActivity.class);
                intent.putExtra("yadmNm", yadmNm1);
                intent.putExtra("information", totalyadmNm);
                Log.d("TAG", "\n======================================\n");
                startActivity(intent);
                finish();
            }
        }

        public String DownLoadUrl2(String myurl) throws IOException {
            HttpURLConnection com = null;
            BufferedReader bufferedReader;
            try {
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
