package org.manatee;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.mfcc.MFCC;

import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Created by HyunJun on 2016-04-10.
 */
public class NodeFactory implements AudioProcessor {

    private int audioBufferSize = 10000;
    private int bufferOverlap = 0;

    public NodeFactory(int audioBufferSize, int bufferOverlap) {
        this.audioBufferSize = audioBufferSize;
        this.bufferOverlap = bufferOverlap;
    }

    /**
     * PCM 데이터를 통해 Node를 생성합니다.
     *
     * @param pcmData
     */
    public Node generate (float[] pcmData, int sampleRate) {

        AudioDispatcher dispatcher = null;
        MFCC mfccProcessor = new MFCC(10, sampleRate);


        // 전처리 과정을 거친다.

        /**
        * NOISE GATE -> ZERO ALIGN -> NORMALIZE VOLUME
        */
        pcmData = noiseGate(pcmData, 0.4f);
        pcmData = zeroAlign(pcmData);
        pcmData = normalizeVolume(pcmData);

        try { dispatcher = AudioDispatcherFactory.fromFloatArray(pcmData, sampleRate, audioBufferSize, bufferOverlap); }
        catch (UnsupportedAudioFileException e) { e.printStackTrace(); }

        dispatcher.addAudioProcessor(mfccProcessor);
        dispatcher.addAudioProcessor(this);

        dispatcher.run();

        return null;
    }

    @Override
    public boolean process(AudioEvent audioEvent){

        // MFCC 행렬을 생성한다.

        return true;
    }

    @Override
    public void processingFinished() {
        // 끝났음 이벤트를 dispatch한다
    }

    /**
     * 노이즈 제거
     * FIR 필터에서 Window를 구현한다. (frequency-domain)
     *
     * @param pcmData
     * @param threshold
     * @return
     */
    private float[] noiseGate(float[] pcmData, float threshold) {
        return pcmData;
    }

    /**
     * 0 정렬. 발음이 시작하기 전 소리를 제거한다. (time-domain)
     *
     * @param pcmData
     * @return
     */
    private float[] zeroAlign(float[] pcmData) {
        return pcmData;
    }

    /**
     * 최대 볼륨 m을 100이라 할때 다른 볼륨의 값을 계산한다. (time-domain)
     * @param pcmData
     * @return
     */
    private float[] normalizeVolume(float[] pcmData) {
        return pcmData;
    }


}
