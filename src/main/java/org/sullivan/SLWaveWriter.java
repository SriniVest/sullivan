package org.sullivan;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.jvm.WaveformWriter;

/**
 * Wav 파일을 쓴다.
 */
public class SLWaveWriter extends WaveformWriter{

    public SLWaveWriter(TarsosDSPAudioFormat format, String name) {
        super(format, name);
    }
}
