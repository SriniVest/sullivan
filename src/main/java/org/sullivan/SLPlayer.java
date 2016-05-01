package org.sullivan;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 음성 데이터를 플레이해준다.
 */
public class SLPlayer implements AudioProcessor {

    public Queue<File> playList;

    public SLPlayer() {
        playList = new LinkedList<>();
    }

    /**
     * 소스에서 음원을 재생한다.
     *
     * @param source
     */
    public void play(File source) {
        playList.add(source);
        if (playList.size() == 1)
            process();
    }

    /**
     * 음원 파일 로드
     */
    private void process() {

        if (playList.size() < 1)
            return;

        File audioFile = playList.poll();

        if (!audioFile.exists()) {
            process();
            return;
        }

        // 파일 확장자 구하기
        String extension = "";
        int i = audioFile.getName().lastIndexOf('.');
        if (i > 0) {
            extension = audioFile.getName().substring(i + 1).toLowerCase();
        }

        SLPcmData pcmData = null;

        // 오디오 데이터를 PCM 형식으로 변환한다.
        if (extension.equals("spd")) {
            pcmData = SLPcmData.importWav(audioFile);
        } else if (extension.equals("wav")) {
            pcmData = SLPcmData.importWav(audioFile);
        }

        AudioDispatcher dispatcher = pcmData.getAudioDispatcher(5000, 0);
        dispatcher.addAudioProcessor(this);
        dispatcher.addAudioProcessor(getAudioPlayer(dispatcher));
        dispatcher.run();
    }

    private AudioPlayer getAudioPlayer(AudioDispatcher dispatcher) {
        try {
            return new AudioPlayer(dispatcher.getFormat());
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        return true;
    }

    @Override
    public void processingFinished() {
        process();
    }


}
