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
 * 고음에 대해서, 저음에 대해 강조 효과를 준다.
 * 이후 bandpass filtering과 연계하여 잡음이 제거되는 효과가 있다.
 */
public class SLHighFrequencyCompensator implements AudioProcessor {

    public float compensationCoefficient = 0.9f;

    public SLHighFrequencyCompensator() {
    }

    public SLHighFrequencyCompensator(float compensationCoefficient) {
        this.compensationCoefficient = compensationCoefficient;
    }

    @Override
    public boolean process(AudioEvent audioEvent) {

        float[] buffer = audioEvent.getFloatBuffer().clone();

        for (int i = buffer.length - 1; i > 0; i--) {
            buffer[i] = buffer[i] - buffer[i - 1] * compensationCoefficient;
        }
        audioEvent.setFloatBuffer(buffer);

        return true;
    }

    @Override
    public void processingFinished() {
    }

}
