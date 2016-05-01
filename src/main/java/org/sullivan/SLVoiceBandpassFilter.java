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
