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

        factory = new NodeFactory(3000, 1);
        factory.addEventListener(this);

        float[] sample1 = NodeFactory.convertFileToPcm(new File("./data/hello1.wav"));
        float[] sample2 = NodeFactory.convertFileToPcm(new File("./data/hello2.wav"));

        factory.insert(sample1, 44100, 1);
        factory.insert(sample2, 44100, 1);

    }

    public void onNodeGenerated(Node node) {
        print("Node generated!");
    }

    public static void print(Object message) {
        System.out.println(message);
    }

    public static void main(String[] args) {

        print("Manatee in Development");

        Manatee manatee = new Manatee();

        /*try {
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromFloatArray(pcmData, 44100, 5000, 1);
            AudioPlayer player = new AudioPlayer(dispatcher.getFormat());

            VolumeNormalizer vd = new VolumeNormalizer();
            BandPass bp = new BandPass(4000, 3500, dispatcher.getFormat().getSampleRate());

            dispatcher.addAudioProcessor(vd);
            dispatcher.addAudioProcessor(bp);
            dispatcher.addAudioProcessor(player);

            dispatcher.run();

        } catch (LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }*/


    }

}
