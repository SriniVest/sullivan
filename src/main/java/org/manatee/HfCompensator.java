package org.manatee;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

/**
 * Created by HyunJun on 2016-04-15.
 */
public class HfCompensator implements AudioProcessor {

    @Override
    public boolean process(AudioEvent audioEvent){

        float[] buffer = audioEvent.getFloatBuffer().clone();

        for (int i = buffer.length - 1; i > 0; i--) {
            buffer[i] = buffer[i] -  buffer[i - 1] * 0.9f;
        }
        audioEvent.setFloatBuffer(buffer);

        return true;
    }

    @Override
    public void processingFinished() {
    }
}
