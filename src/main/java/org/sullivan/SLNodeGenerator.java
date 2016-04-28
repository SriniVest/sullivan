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
public class SLNodeGenerator implements AudioProcessor {

    public static boolean DEBUG_PLAY = false;

    /**
     * 음성 처리 버퍼 크기
     */
    public int bufferSize = 5000;

    /**
     * 버퍼 간 겹침 크기
     */
    public int bufferOverlap = 1;

    /**
     * 프로세싱 큐
     */
    private Queue<SLNodeGeneratorEntry> processingQueue;

    /**
     * 현재 프로세싱 중인 재료
     */
    private SLNodeGeneratorEntry processingEntry;

    /**
     * 음성 처리 모듈들
     */
    private SLVolumeNormalizer volumeNormalizer;
    private SLHighFrequencyCompensator hfCompensator;
    private SLVoiceBandpassFilter bandpassFilter;
    private SLFeatureExtractor featureExtractor;

    /**
     * 이벤트 리스너들
     */
    private List<SLNodeGeneratorListener> listeners;

    /**
     * 노드 처리기를 생성한다.
     *
     * @param bufferSize
     * @param bufferOverlap
     */
    public SLNodeGenerator(int bufferSize, int bufferOverlap) {

        this.bufferSize = bufferSize;
        this.bufferOverlap = bufferOverlap;

        processingQueue = new LinkedList<>();
        listeners = new ArrayList<>();
    }

    /**
     * 노드 처리를 시작한다.
     *
     * @param pcmData
     * @param nodeInfo
     */
    public void insert(SLPcmData pcmData, SLNode.SLNodeInfo nodeInfo) {

        // 프로세싱 큐에 넣는다.
        processingQueue.add(new SLNodeGeneratorEntry(pcmData, nodeInfo));

        // 큐가 비었었는데, 이번에 새로 작업이 들어온 거라면, 작업 시작!
        if (processingQueue.size() == 1)
            preprocess();
    }

    /**
     * 음성 데이터 전처리 프로세스
     */
    private void preprocess() {

        // 남아있는 작업이 없다면 그만둔다.
        if (processingQueue.size() < 1)
            return;

        processingEntry = processingQueue.poll();

        /** 전음 처리 **/

        // 음성 앞, 뒤의 여백을 제거한다.
        SLSilenceTruncator.truncate(processingEntry.pcmData);

        // 음성 데이터를 화자 일반화한다.
        SLPitchNormalizer.normalize(processingEntry.pcmData);

        /** 프레임별 처리 **/
        AudioDispatcher dispatcher = processingEntry.pcmData.getAudioDispatcher(bufferSize, bufferOverlap);

        // 볼륨 크기 일반화
        volumeNormalizer = new SLVolumeNormalizer();

        // 고주파수 보상
        hfCompensator = new SLHighFrequencyCompensator();

        // 성대 음역대로 주파수 대역 거르기
        bandpassFilter = new SLVoiceBandpassFilter(processingEntry.pcmData.sampleRate);

        // 특징행렬 추출기
        featureExtractor = new SLFeatureExtractor(bufferSize, bufferOverlap, 12);

        dispatcher.addAudioProcessor(volumeNormalizer);
        dispatcher.addAudioProcessor(hfCompensator);
        dispatcher.addAudioProcessor(bandpassFilter);
        dispatcher.addAudioProcessor(featureExtractor);

        // 디버깅 모드에서는 처리된 음성을 들어봐야 하므로 재생 프로세스를 추가한다.
        if (DEBUG_PLAY) dispatcher.addAudioProcessor(getAudioPlayer(dispatcher));

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

        // 파일을 저장한다.
        String filePath = "./data/" + processingEntry.nodeInfo.uid + ".pronunciation";
        SLPcmData.export(processingEntry.pcmData, filePath);

        // 소스를 등록한다.
        processingEntry.nodeInfo.source = filePath;

        // 특징행렬 취득
        List<float[]> featureMatrix = featureExtractor.getFeatureMatrix();

        // 모든 데이터는 일반화를 위해 Big Endian으로 통일한다.
        if (processingEntry.pcmData.isBigEndian)
            Collections.reverse(featureMatrix);

        // 새 노드를 생성한다.
        SLNode node = new SLNode(featureMatrix, processingEntry.nodeInfo);

        // 새 작업 루틴을 시작한다.
        if (processingQueue.size() > 0)
            preprocess();

        // 이벤트를 dispatch한다.
        for (SLNodeGeneratorListener listener : listeners) {
            listener.onNodeGenerated(node);
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
    public void addEventListener(SLNodeGeneratorListener listener) {
        this.listeners.add(listener);
    }

    /**
     * 리스너를 제거한다.
     *
     * @param listener
     */
    public void removeEventListener(SLNodeGeneratorListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * (디버깅용) 음성 재생이 가능한 플레이어 컴포넌트를 가져온다.
     *
     * @param dispatcher
     * @return
     */
    private AudioPlayer getAudioPlayer(AudioDispatcher dispatcher) {
        try {
            return new AudioPlayer(dispatcher.getFormat());
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 가공을 위해 들어온 재료 데이터를 표현하는 클래스
     */
    private static class SLNodeGeneratorEntry {

        public SLPcmData pcmData;
        public SLNode.SLNodeInfo nodeInfo;

        public SLNodeGeneratorEntry(SLPcmData pcmData, SLNode.SLNodeInfo nodeInfo) {
            this.pcmData = pcmData;
            this.nodeInfo = nodeInfo;
        }
    }

}
