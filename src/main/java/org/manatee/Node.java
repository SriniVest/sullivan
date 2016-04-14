package org.manatee;

import java.util.List;

/**
 * Created by HyunJun on 2016-04-10.
 */
public class Node {

    public float secondsProcessed = 0;
    public List<float[]> featureMatrix;

    public int pid = 0;

    public Node(List<float[]> featureMatrix, int pid) {
        this.featureMatrix = featureMatrix;
        this.pid = pid;
    }

}
