package org.manatee;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

/**
 * Created by HyunJun on 2016-04-14.
 */
public class VolumeNormalizer implements AudioProcessor {
    public VolumeNormalizer(){

    }

    @Override
    public boolean process(AudioEvent audioEvent){

        float[] buffer = audioEvent.getFloatBuffer();
        double volume = getRmsVolume(buffer);

        float[] newBuffer = new float[buffer.length];

        for (int i = 0; i < newBuffer.length; i++) {
            newBuffer[i] = buffer[i] * (float) (0.5d / volume);
        }

        audioEvent.setFloatBuffer(newBuffer);

        return true;
    }

    public double getRmsVolume(float[] pcmData) {
        double sum = 0d;

        if (pcmData.length == 0)
            return sum;

        for (int i = 0; i < pcmData.length; i++) {
            sum += pcmData[i];
        }

        double average = sum / pcmData.length;
        double sumMeanSquare = 0d;

        for (int i=0; i < pcmData.length; i++) {
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
