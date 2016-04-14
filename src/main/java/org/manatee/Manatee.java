package org.manatee;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.filters.BandPass;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;

import javax.sound.sampled.LineUnavailableException;

/**
 * Created by HyunJun on 2016-04-10.
 */
public class Manatee {

    public static void print(Object message) {
        System.out.println(message);
    }

    public static void main(String[] args) {

        try {
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(5000, 1);
            AudioPlayer player = new AudioPlayer(dispatcher.getFormat());

            VolumeDetector vd = new VolumeDetector();
            BandPass bp = new BandPass(5000, 9000, dispatcher.getFormat().getSampleRate());

            dispatcher.addAudioProcessor(vd);
            dispatcher.addAudioProcessor(bp);
            dispatcher.addAudioProcessor(player);

            dispatcher.run();

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        print("hello, world!");
    }
}
