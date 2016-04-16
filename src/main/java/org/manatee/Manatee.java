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

        factory = new NodeFactory(2000, 100);
        factory.addEventListener(this);

        finalPid = 4;
        factory.playSound = true;
        factory.insert(NodeFactory.convertFileToPcm(new File("./data/hello1.wav")), 44100, 1);
        factory.insert(NodeFactory.convertFileToPcm(new File("./data/hello2.wav")), 44100, 2);
        factory.insert(NodeFactory.convertFileToPcm(new File("./data/hello3.wav")), 44100, 3);

        factory.insert(NodeFactory.convertFileToPcm(new File("./data/hello4.wav")), 44100, 4);
    }

    private void onAllNodeGenerated() {

        print("All nodes are generated");
        double c = NodeComparator.getDistance(nodes.get(1), nodes.get(2));

        print(NodeComparator.getDistance(nodes.get(0), nodes.get(1)) - c);
        print(NodeComparator.getDistance(nodes.get(0), nodes.get(2))- c);
        print(NodeComparator.getDistance(nodes.get(1), nodes.get(2))- c);
        print(NodeComparator.getDistance(nodes.get(0), nodes.get(3))- c);
        print(NodeComparator.getDistance(nodes.get(1), nodes.get(3))- c);


/*
        double c = NodeComparator.getDistance(nodes.get(0), nodes.get(1));

        print(": Hellos");
        print(NodeComparator.getDistance(nodes.get(0), nodes.get(1)) - c);
        print(NodeComparator.getDistance(nodes.get(0), nodes.get(4)) - c);
        print(NodeComparator.getDistance(nodes.get(1), nodes.get(4)) - c);
        print("");
        print(": Hello vs Greets(2)");
        print(NodeComparator.getDistance(nodes.get(0), nodes.get(2)) - c);
        print(NodeComparator.getDistance(nodes.get(1), nodes.get(2)) - c);
        print(NodeComparator.getDistance(nodes.get(4), nodes.get(2)) - c);
        print("");
        print(": Hello vs Greets(3)");
        print(NodeComparator.getDistance(nodes.get(0), nodes.get(3)) - c);
        print(NodeComparator.getDistance(nodes.get(1), nodes.get(3)) - c);
        print(NodeComparator.getDistance(nodes.get(4), nodes.get(3)) - c);
        print("");
        print(": Greets");
        print(NodeComparator.getDistance(nodes.get(3), nodes.get(2)) - c);*/
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
