package org.sullivan;


import be.tarsos.dsp.filters.BandPass;

/**
 * Created by HyunJun on 2016-04-26.
 */
public class SLVoiceBandpassFilter extends BandPass {

    public static int[] FREQUENCY_RANGE = new int[]{300, 3400};

    public SLVoiceBandpassFilter(int sampleRate) {
        super((FREQUENCY_RANGE[0] + FREQUENCY_RANGE[1]) / 2,
                (FREQUENCY_RANGE[0] + FREQUENCY_RANGE[1]) / 2 - FREQUENCY_RANGE[0], sampleRate);
    }
}
