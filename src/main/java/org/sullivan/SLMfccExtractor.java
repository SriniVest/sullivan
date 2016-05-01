/**
 *   ___      _ _ _
 * / __|_  _| | (_)_ ____ _ _ _
 * \__ \ || | | | \ V / _` | ' \
 * |___/\_,_|_|_|_|\_/\__,_|_||_|
 *
 * Copyright 2016 Sullivan Project
 * https://github.com/agemor/sullivan
 *
 * This file is distributed under
 * GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 * for more details, See README.md
 *
 * Sullivan is developed by HyunJun Kim (me@hyunjun.org)
 */

package org.sullivan;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.mfcc.MFCC;

import java.util.ArrayList;
import java.util.List;

/**
 * 음성 데이터로부터 특징행렬(Feature Matrix)를 계산한다.
 */
public class SLMfccExtractor extends MFCC {

    /**
     * 계산된 특징행렬
     */
    private List<float[]> featureMatrix;

    // 재활용 시 초기화를 위한 상태변수
    private boolean afterProcessing = false;

    public SLMfccExtractor(int bufferSize, int sampleRate, int featureNumber) {
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
