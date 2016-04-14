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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by HyunJun on 2016-04-10.
 */
public class Manatee implements NodeFactoryListener {

    List<Node> nodes;
    NodeFactory factory;

    private int finalPid = 0;

    public Manatee() {

        nodes = new ArrayList<Node>();

        factory = new NodeFactory(1000, 1);
        factory.addEventListener(this);

        float[] sample1 = NodeFactory.convertFileToPcm(new File("./data/hello1.wav"));
        float[] sample2 = NodeFactory.convertFileToPcm(new File("./data/hello2.wav"));
        float[] sample3 = NodeFactory.convertFileToPcm(new File("./data/greet1.wav"));
        float[] sample4 = NodeFactory.convertFileToPcm(new File("./data/greet2.wav"));

        finalPid = 4;

        factory.insert(sample1, 44100, 1);
        factory.insert(sample2, 44100, 2);
        factory.insert(sample3, 44100, 3);
        factory.insert(sample4, 44100, 4);
    }

    private void onAllNodeGenerated() {

        print("All nodes are generated");

        double d1 = NodeComparator.getDistance(nodes.get(0), nodes.get(1));

        double d2 = NodeComparator.getDistance(nodes.get(0), nodes.get(2));

        double d3 = NodeComparator.getDistance(nodes.get(1), nodes.get(2));

        double d4 = NodeComparator.getDistance(nodes.get(3), nodes.get(2));

        print(d1);
        print(d2);
        print(d3);
        print(d4);
    }


    public void onNodeGenerated(Node node) {
        nodes.add(node);

        print(node.pid);

        if (node.pid == finalPid) {
            onAllNodeGenerated();
        }
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
