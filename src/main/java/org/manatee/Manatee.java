package org.manatee;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.filters.BandPass;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by HyunJun on 2016-04-10.
 */
public class Manatee implements NodeFactoryListener {

    NodeFactory factory;

    public Manatee() {
        factory = new NodeFactory(5000, 1);
    }

    public void onNodeGenerated(Node node) {

    }

    public static void print(Object message) {
        System.out.println(message);
    }

    public static void main(String[] args) {

        float[] pcmData = NodeFactory.convertFileToPcm(new File("./data/hello_long_converted.wav"));
        pcmData = zeroAlign(pcmData, 44100);

        try {
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromFloatArray(pcmData, 44100, 5000, 1);
            AudioPlayer player = new AudioPlayer(dispatcher.getFormat());

            VolumeDetector vd = new VolumeDetector();
            BandPass bp = new BandPass(4000, 3500, dispatcher.getFormat().getSampleRate());

            dispatcher.addAudioProcessor(vd);
            dispatcher.addAudioProcessor(bp);
            dispatcher.addAudioProcessor(player);

            dispatcher.run();

        } catch (LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }

        print("hello, world!");
    }

    private static float[] zeroAlign(float[] pcmData, int sampleRate) {

        // 변수는 두 개. threshold랑, min threshold samples
        final float THRESHOLD = 0.01f;
        final int MIN_THRESHOLD_SAMPLES = sampleRate / 50;

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
}
