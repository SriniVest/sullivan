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
import be.tarsos.dsp.AudioProcessor;

/**
 * 한 프레임에서, 볼륨 크기를 일반화한다.
 */
public class SLVolumeNormalizer implements AudioProcessor {

    public double normalizationCoefficient = 1d;

    public SLVolumeNormalizer() {
    }

    public SLVolumeNormalizer(double normalizationCoefficient) {
        this.normalizationCoefficient = normalizationCoefficient;
    }

    @Override
    public boolean process(AudioEvent audioEvent) {

        float[] buffer = audioEvent.getFloatBuffer();
        double volume = getRmsVolume(buffer);

        float[] newBuffer = new float[buffer.length];

        for (int i = 0; i < newBuffer.length; i++) {
            newBuffer[i] = buffer[i] * (float) (normalizationCoefficient / volume);
        }

        audioEvent.setFloatBuffer(newBuffer);

        return true;
    }

     /**
     * 프레임에서 Root-mean-square 볼륨을 구한다.
     *
     * @param pcmData
     * @return
     */
    public double getRmsVolume(float[] pcmData) {
        double sum = 0d;

        if (pcmData.length == 0)
            return sum;

        for (int i = 0; i < pcmData.length; i++) {
            sum += pcmData[i];
        }
        double average = sum / pcmData.length;
        double sumMeanSquare = 0d;

        for (int i = 0; i < pcmData.length; i++) {
            sumMeanSquare += Math.pow(pcmData[i] - average, 2d);
        }
        double averageMeanSquare = sumMeanSquare / pcmData.length;
        double rootMeanSquare = Math.sqrt(averageMeanSquare);

        return rootMeanSquare;
    }

    @Override
    public void processingFinished() {
    }

}
