package org.manatee;

/**
 * Created by HyunJun on 2016-04-10.
 */
public class Node {

    public float secondsProcessed = 0;
    public float[] featureMatrix;

    public int pid = 0;

    public Node(float[] featureMatrix, int pid) {
        this.featureMatrix = featureMatrix;
        this.pid = pid;
    }

}
