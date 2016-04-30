package org.sullivan;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.util.*;

/**
 * 음성 데이터를 특징행렬(Feature Matrix)로 가공한 형태인 노드로 만들어 준다.
 * 정확한 특징 분석을 위해 여러 음성 전처리 기법을 적용하였다.
 */
public class SLFeatureExtractor implements AudioProcessor {

    /**
     * 음성 처리 버퍼 크기
     */
    public int bufferSize = 5000;

    /**
     * 버퍼 간 겹침 크기
     */
    public int bufferOverlap = 1;

    /**
     * 이벤트 리스너들
     */
    private List<SLFeatureExtractorListener> listeners;

    /**
     * MFCC 추출기
     */
    private SLMfccExtractor mfccExtractor;

    /**
     * 처리되고 있는 pcm데이터
     */
    private SLPcmData pcmData;

    /**
     * 노드 처리기를 생성한다.
     *
     * @param bufferSize
     * @param bufferOverlap
     */
    public SLFeatureExtractor(int bufferSize, int bufferOverlap) {

        this.bufferSize = bufferSize;
        this.bufferOverlap = bufferOverlap;

        listeners = new ArrayList<>();
    }


    /**
     * 음성 데이터 전처리 프로세스
     */
    public void process(SLPcmData pcmData) {

        this.pcmData = pcmData;

        /** 전음 처리 **/

        // 음성 앞, 뒤의 여백을 제거한다.
        SLSilenceTruncator.truncate(pcmData);

        // 음성 데이터를 화자 일반화한다.
        SLPitchNormalizer.normalize(pcmData);

        /** 프레임별 처리 **/
        AudioDispatcher dispatcher = pcmData.getAudioDispatcher(bufferSize, bufferOverlap);

        // 볼륨 크기 일반화
        SLVolumeNormalizer volumeNormalizer = new SLVolumeNormalizer();

        // 고주파수 보상
        SLHighFrequencyCompensator hfCompensator = new SLHighFrequencyCompensator();

        // 성대 음역대로 주파수 대역 거르기
        SLVoiceBandpassFilter bandpassFilter = new SLVoiceBandpassFilter(pcmData.sampleRate);

        // 특징행렬 추출기
        mfccExtractor = new SLMfccExtractor(bufferSize, bufferOverlap, 12);

        dispatcher.addAudioProcessor(volumeNormalizer);
        dispatcher.addAudioProcessor(hfCompensator);
        dispatcher.addAudioProcessor(bandpassFilter);
        dispatcher.addAudioProcessor(mfccExtractor);

        dispatcher.addAudioProcessor(this);

        // 프로세싱 시작. 프로세싱은 별개의 스레드에서 이루어진다.
        dispatcher.run();
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        return true;
    }

    /**
     * 계산된 MFCC값을 이벤트를 통해 반환하고, 새 작업 루틴을 시작한다.
     */
    private void postprocess() {


        // 특징행렬 취득
        List<float[]> featureMatrix = mfccExtractor.getFeatureMatrix();

        // 모든 데이터는 일반화를 위해 Big Endian으로 통일한다.
        if (pcmData.isBigEndian)
            Collections.reverse(featureMatrix);

        // 이벤트를 dispatch한다.
        for (SLFeatureExtractorListener listener : listeners) {
            listener.onFeatureExtracted();
        }
    }

    @Override
    public void processingFinished() {
        postprocess();
    }

    /**
     * 노드 생성 관련 이벤트를 받기 위한 리스너를 추가한다.
     *
     * @param listener
     */
    public void addEventListener(SLFeatureExtractorListener listener) {
        this.listeners.add(listener);
    }

    /**
     * 리스너를 제거한다.
     *
     * @param listener
     */
    public void removeEventListener(SLFeatureExtractorListener listener) {
        this.listeners.remove(listener);
    }


}
