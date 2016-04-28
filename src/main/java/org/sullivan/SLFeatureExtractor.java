package org.sullivan;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.mfcc.MFCC;

import java.util.ArrayList;
import java.util.List;

/**
 * 음성 데이터로부터 특징행렬(Feature Matrix)를 계산한다.
 */
public class SLFeatureExtractor extends MFCC {

    /**
     * 계산된 특징행렬
     */
    private List<float[]> featureMatrix;

    // 재활용 시 초기화를 위한 상태변수
    private boolean afterProcessing = false;

    public SLFeatureExtractor(int bufferSize, int sampleRate, int featureNumber) {
        super(bufferSize, sampleRate, featureNumber, 30, 133.3334f, (float) sampleRate / 2f);

        featureMatrix = new ArrayList<>();
    }

    /**
     * 특징행렬을 리턴한다.
     *
     * @return
     */
    public List<float[]> getFeatureMatrix() {
        return featureMatrix;
    }

    @Override
    public boolean process(AudioEvent audioEvent) {

        // MFCC 계산
        super.process(audioEvent);

        if (afterProcessing) {
            afterProcessing = false;
            featureMatrix.clear();
        }

        float[] mfcc = getMFCC();
        featureMatrix.add(mfcc);

        return true;
    }

    @Override
    public void processingFinished() {
        super.processingFinished();
        afterProcessing = true;
    }


}
