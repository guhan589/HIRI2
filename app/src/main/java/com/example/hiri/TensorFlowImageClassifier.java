package com.example.hiri;


import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by amitshekhar on 17/03/18.
 */

public class TensorFlowImageClassifier implements Classifier {

    private static final int MAX_RESULTS = 3;
    private static final int BATCH_SIZE = 1;
    private static final int PIXEL_SIZE = 3;
    private static final float THRESHOLD = 0.1f;

    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;

    private Interpreter interpreter;
    private int inputSize;
    private List<String> labelList;
    private boolean quant;

    private TensorFlowImageClassifier() {

    }

    static Classifier create(AssetManager assetManager,
                             String modelPath,
                             String labelPath,
                             int inputSize,
                             boolean quant) throws IOException {

        TensorFlowImageClassifier classifier = new TensorFlowImageClassifier();
        classifier.interpreter = new Interpreter(classifier.loadModelFile(assetManager, modelPath), new Interpreter.Options());
        classifier.labelList = classifier.loadLabelList(assetManager, labelPath);
        classifier.inputSize = inputSize;
        classifier.quant = quant;

        Log.d("TAG", "\ncreate_: modelPath"+modelPath+"\nlabelPath:"+labelPath+"\ninputSize:"+inputSize+"\nquant:"+quant);
        return classifier;
    }

    @Override
    public List<Recognition> recognizeImage(Bitmap bitmap) {
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap); //convertBitmapToByteBuffer: bytebuffer 메모리 할당을 위함
        if(quant){
            byte[][] result = new byte[1][labelList.size()];
            for(int i =0;i<labelList.size();i++)
                Log.d("TAG", "\nresult1: "+result[0][i]+"\n"+"i="+i+"\n");
            Log.d("TAG", "\nlabelList1: "+labelList.size()+"\n");
            interpreter.run(byteBuffer, result);  //텐서플로 파일을 읽어라는 명령  (input,output)  //bytebuffer 변환한 이미지
            return getSortedResultByte(result);
        } else {
            float [][] result = new float[1][labelList.size()];
            for(int i =0;i<labelList.size();i++)
                Log.d("TAG", "\nresult2: "+result[0][i]+"\n"+"i="+i+"\n");
            Log.d("TAG", "\nlabelList2: "+labelList.size()+"\n");
            interpreter.run(byteBuffer, result); ////텐서플로 파일을 읽어라는 명령
            return getSortedResultFloat(result);
        }

    }

    @Override
    public void close() {
        interpreter.close();
        interpreter = null;
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {  //loadModelFile : 텐서플로 파일 읽기
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException { //텍스트 label읽기
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer;

        if(quant) {//allocateDirect: 재할당
            byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE);
            Log.d("TAG", "convertBitmapToByteBuffer1: "+byteBuffer+"\n");
        } else {
            byteBuffer = ByteBuffer.allocateDirect(4 * BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE);
            Log.d("TAG", "convertBitmapToByteBuffer2: "+byteBuffer+"\n");
        }

        byteBuffer.order(ByteOrder.nativeOrder()); //nativeOrder : 네이티브의 바이트 순서
        int[] intValues = new int[inputSize * inputSize];
        Log.d("TAG", "intValues: ="+intValues+"\n");
        Log.d("TAG", "bitmap.getWidth(): ="+bitmap.getWidth()+"\n");
        Log.d("TAG", "bitmap.getHeight(): ="+bitmap.getHeight()+"\n");


        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight()); // bitmap의 픽셀을 가져옴
        int pixel = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int val = intValues[pixel++];
                if(quant){
                    byteBuffer.put((byte) ((val >> 16) & 0xFF));
                    byteBuffer.put((byte) ((val >> 8) & 0xFF));
                    byteBuffer.put((byte) (val & 0xFF));
                } else {
                    byteBuffer.putFloat(((val>>16) & 0xFF)/255.f); // R //IMAGE_MEAN 128   IMAGE_STD: 1280.f
                    byteBuffer.putFloat(((val>>8) & 0xFF)/255.f);// G
                    byteBuffer.putFloat(((val) & 0xFF)/255.f); //B
                }//>>16 8

            }
        }
        return byteBuffer;
    }

    @SuppressLint("DefaultLocale")
    private List<Recognition> getSortedResultByte(byte[][] labelProbArray) { // byte이차원배열

        PriorityQueue<Recognition> pq =
                new PriorityQueue<>(
                        MAX_RESULTS, //최대 몇개의 결과값(3)
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition lhs, Recognition rhs) {
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        for (int i = 0; i < labelList.size(); ++i) {
            float confidence = (labelProbArray[0][i] & 0xff) / 255.0f;
            if (confidence > THRESHOLD) { //labelList.size() 텍스트파일 크기  THRESHOLD:0.1f
                pq.add(new Recognition("" + i,
                        labelList.size() > i ? labelList.get(i) : "unknown",
                        confidence, quant));
            }
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());//해당 큐의 맨 앞에 있는(제일 먼저 저장된) 요소를 반환하고, 해당 요소를 큐에서 제거함.
            //만약 큐가 비어있으면 null을 반환함.
        }

        return recognitions;
    }

    @SuppressLint("DefaultLocale")
    private List<Recognition> getSortedResultFloat(float[][] labelProbArray) {//float 2차원 배열

        PriorityQueue<Recognition> pq =
                new PriorityQueue<>(MAX_RESULTS, new Comparator<Recognition>() {
                    @Override
                    public int compare(Recognition lhs, Recognition rhs) {
                        return Float.compare(rhs.getConfidence(), lhs.getConfidence());  //rhs 가 lhs보다 작으면 0보다 작은 값 크면 큰값 같으면 0
                    }
                });

        for (int i = 0; i < labelList.size(); ++i) {
            float confidence = labelProbArray[0][i];
            if (confidence > THRESHOLD) {//labelList.size() 텍스트파일 크기  THRESHOLD:0.1f           ex) confidence= 1.0 ==> 100%, 0.99==>99908245
                Log.d("TAG", "\ngetSortedResultFloat_confidence: ["+i+"]="+confidence+"\n");
                pq.add(new Recognition("" + i,
                        labelList.size() > i ? labelList.get(i) : "unknown",
                        confidence, quant));
            }
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());//해당 큐의 맨 앞에 있는(제일 먼저 저장된) 요소를 반환하고, 해당 요소를 큐에서 제거함.
            //만약 큐가 비어있으면 null을 반환함.
        }

        return recognitions;
    }
}
