package com.halflike.myapplication.app;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.halflike.myapplication.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by luox on 14/12/8.
 */
public class DecibelSample {

    private final int SAMPLE_RATE_IN_HZ = 8000;
    private final int MAX_BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    private static DecibelSample mDecibelSample;
    private GetDbValueLisener mLisener;
    private boolean sampled;
    private float meanDb = 0;
    private float maxDb = 0;
    private ArrayList<Float> dbList = null;

    public AudioRecord mAudioRecord;
    public boolean initIsSucceed = false;
    //get db value frequency in one second
    public float rateOneSecond = 5;

    private DecibelSample() {
        try {
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                    AudioFormat.ENCODING_PCM_16BIT, MAX_BUFFER_SIZE);
            initIsSucceed = true;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            initIsSucceed = false;
        }
        dbList = new ArrayList<Float>();
    }

    public interface GetDbValueLisener {
        public void getDbValue(float db);
    }

    public static DecibelSample getInstance() {
        if (mDecibelSample == null) {
            synchronized (DecibelSample.class) {
                if (mDecibelSample == null) {
                    mDecibelSample = new DecibelSample();
                }
            }
        }
        return mDecibelSample;
    }

    public boolean isSampling() {
        return sampled;
    }

    public void setLisener(GetDbValueLisener lisener) {
        mLisener = lisener;
    }

    /**
     * Calculate function:
     *     DB = 10 * log10(A1^2 / A0^2)
     */
    public void startSample() {
        if (!initIsSucceed) {
            Logger.e("Initialized AudioRecord faild!");
            return;
        }
        sampled = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();
                short[] buffer = new short[MAX_BUFFER_SIZE];
                while (sampled) {
                    int num = mAudioRecord.read(buffer, 0, MAX_BUFFER_SIZE);
                    long value = 0;
                    // squares sum
                    for (int i = 0; i < num; i++) {
                        value += (long) buffer[i] * (long) buffer[i];
                    }
                    double mean = value / (double) num;
                    float db = (float) (10 * Math.log10(mean / 1));
                    dbList.add(db);
                    if (mLisener != null) {
                        mLisener.getDbValue(db);
                    }
                    Logger.d("DB value:" + db);
                    try {
                        Thread.sleep((long) (1000 / rateOneSecond));
                    } catch (InterruptedException e) {

                    }
                }
            }
        }).start();

    }

    public void stopSample() {
        sampled = false;
        calculaDbValueInRunTime();
        dbList.clear();
    }

    public float getMeanDb() {
        return meanDb;
    }

    public float getMaxDb() {
        return maxDb;
    }

    private void calculaDbValueInRunTime() {
        if(dbList == null) {
            return;
        }
        double sumDb = 0;
        for(int i = 0; i < dbList.size(); i++) {
            sumDb += dbList.get(i);
        }
        meanDb = (float) (sumDb / dbList.size());
        maxDb = Collections.max(dbList);
    }

}


