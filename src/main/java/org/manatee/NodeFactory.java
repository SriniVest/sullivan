package org.manatee;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.filters.BandPass;
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.mfcc.MFCC;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by HyunJun on 2016-04-10.
 */
public class NodeFactory implements AudioProcessor {

    protected int audioBufferSize = 5000;
    protected int bufferOverlap = 1;

    // 이벤트 리스너
    private List<NodeFactoryListener> eventListeners;

    // 프로세싱 대기열
    public Queue<NodeComponent> processingQueue;
    private NodeComponent componentInProcess;

    // 프로세서
    protected AudioDispatcher dispatcher;
    protected VolumeNormalizer normalizer;
    protected MFCC mfccProcessor;
    protected BandPass bandpassFilter;
    protected AudioPlayer player;

    // 프로세싱 결과물
    private List<float[]> mfccMatrix;
    private int mfccMatrixIndex;

    public NodeFactory(int audioBufferSize, int bufferOverlap) {

        this.audioBufferSize = audioBufferSize;
        this.bufferOverlap = bufferOverlap;

        processingQueue = new LinkedList<NodeComponent>();
        eventListeners = new ArrayList<NodeFactoryListener>();
    }

    /**
     * PCM 데이터를 통해 Node를 생성합니다.
     *
     * @param pcmData
     */
    public void insert(float[] pcmData, int sampleRate, int pid) {
        insert(new NodeComponent(pcmData, sampleRate, pid));
    }

    public void insert(NodeComponent component) {

        processingQueue.offer(component);

        // 큐가 비어 있었다면 처리 작업을 재개한다.
        if (processingQueue.size() == 1)
            preprocess();
    }

    /**
     * 음성 데이터를 dispatcher에 밀어넣고 처리를 시작한다.
     */
    private void preprocess() {

        if (processingQueue.size() < 1)
            return;

        componentInProcess = processingQueue.poll();

        // 음성 데이터를 전처리한다.
        float[] alignedPcmData = zeroAlign(componentInProcess.pcmData);

        float pcmDuration = (float) alignedPcmData.length / componentInProcess.sampleRate;

        // 프로세서를 초기화한다.
        dispatcher = null;
        bandpassFilter = new BandPass(4000, 3500, componentInProcess.sampleRate);
        mfccProcessor = new MFCC(audioBufferSize, componentInProcess.sampleRate, 12, 30, 133.3334f, componentInProcess.sampleRate / 2f);
        normalizer = new VolumeNormalizer();

        mfccMatrix = new ArrayList<float[]>();
        mfccMatrixIndex = 0;

        try {
            dispatcher = AudioDispatcherFactory.fromFloatArray(alignedPcmData, componentInProcess.sampleRate, audioBufferSize, bufferOverlap);
            player = new AudioPlayer(dispatcher.getFormat());
        } catch (UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }

        // 필터링
        dispatcher.addAudioProcessor(normalizer);
        dispatcher.addAudioProcessor(bandpassFilter);
        dispatcher.addAudioProcessor(mfccProcessor);
        dispatcher.addAudioProcessor(player);
        dispatcher.addAudioProcessor(this);

        dispatcher.run();
    }

    /**
     * 계산된 MFCC값을 행렬에 추가한다.
     *
     * @param audioEvent
     * @return
     */
    @Override
    public boolean process(AudioEvent audioEvent) {

        float[] mfcc = mfccProcessor.getMFCC();

        String output = "";
        for (int i = 0; i < mfcc.length; i++) {
            output += mfcc[i] + ", ";
        }
        System.out.println(output);

        // MFCC 행렬에 추가한다
        mfccMatrix.add(mfcc);
        mfccMatrixIndex += mfcc.length;

        return true;
    }

    /**
     * 계산된 MFCC값을 이벤트를 통해 반환하고, 새 작업 루틴을 시작한다.
     */
    private void postprocess() {

        // 새 노드를 생성한다.
        Node node = new Node(mfccMatrix, componentInProcess.pid);
        node.secondsProcessed = dispatcher.secondsProcessed();

        // 새 작업 루틴을 시작한다.
        if (processingQueue.size() > 0)
            preprocess();

        // 이벤트를 dispatch한다.
        for (NodeFactoryListener listener : eventListeners) {
            listener.onNodeGenerated(node);
        }
    }

    @Override
    public void processingFinished() {
        postprocess();
        dispatcher = null;
    }

    /**
     * 발음이 시작하기 전, 발음이 끝나고 난 후 소리 공백을 제거한다. (time-domain)
     *
     * @param pcmData
     * @return
     */
    private float[] zeroAlign(float[] pcmData) {

        // 변수는 두 개. threshold랑, min threshold samples
        final float THRESHOLD = 0.01f;
        final int MIN_THRESHOLD_SAMPLES = componentInProcess.sampleRate / 50;

        int validStartIndex = 0;
        int validEndIndex = 0;
        int validCount = 0;

        // 왼쪽
        for (int i = 0; i < pcmData.length; i++) {
            if (pcmData[i] > THRESHOLD) {
                if (validCount < 1)
                    validStartIndex = i;
                validCount++;
            } else {
                validCount = 0;
            }
            if (validCount > MIN_THRESHOLD_SAMPLES)
                break;
        }

        // 오른쪽
        validCount = 0;
        for (int i = pcmData.length - 1; i > 1; i--) {
            if (pcmData[i] > THRESHOLD) {
                if (validCount < 1)
                    validEndIndex = i;
                validCount++;
            } else {
                validCount = 0;
            }
            if (validCount > MIN_THRESHOLD_SAMPLES)
                break;
        }

        return Arrays.copyOfRange(pcmData, validEndIndex, validStartIndex);
    }

    /**
     * Node Factory에 리스너를 추가한다.
     *
     * @param listener
     */
    public void addEventListener(NodeFactoryListener listener) {
        this.eventListeners.add(listener);
    }

    /**
     * NodeFactory에서 리스너를 제거한다.
     *
     * @param listener
     */
    public void removeEventListener(NodeFactoryListener listener) {
        this.eventListeners.remove(listener);
    }


    /**
     * 일반 .wav 파일 데이터를 float PCM 배열로 변환한다.
     * @param source
     * @return
     */
    public static float[] convertFileToPcm(File source) {

        TarsosDSPAudioFloatConverter converter = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(source);
            converter = TarsosDSPAudioFloatConverter.getConverter(JVMAudioInputStream.toTarsosDSPFormat(audioInputStream.getFormat()));

            int read;
            byte[] buff = new byte[1024 * 1024];
            while ((read = audioInputStream.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            out.flush();

        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        byte[] audioBytes = out.toByteArray();
        byte[] audioBytesPadded = new byte[audioBytes.length * 4];

        System.arraycopy(audioBytes,0,audioBytesPadded,0,audioBytes.length);

        float[] audioFloats = new float[audioBytes.length];

        converter.toFloatArray(audioBytesPadded, 0, audioFloats, 0, audioFloats.length - 1);

        return audioFloats;
    }

    /**
     * 노드를 구성하기 위해 필요한 데이터의 집합
     */
    public class NodeComponent {

        public float[] pcmData;
        public int sampleRate;

        public int pid;

        public NodeComponent(float[] pcmData, int sampleRate, int pid) {
            this.pcmData = pcmData;
            this.sampleRate = sampleRate;
            this.pid = pid;
        }
    }

}


