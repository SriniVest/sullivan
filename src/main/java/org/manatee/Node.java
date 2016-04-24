package org.manatee;

import java.util.List;

/**
 * Created by HyunJun on 2016-04-10.
 */
public class Node implements Measurable<Node> {

    public float secondsProcessed = 0;
    public List<float[]> featureMatrix;

    public int pid = 0;

    public Node(List<float[]> featureMatrix, int pid) {
        this.featureMatrix = featureMatrix;
        this.pid = pid;
    }

    /**
     * 다른 노드와의 거리를 계산한다.
     *
     * @param node
     * @return
     */
    public double getDistance(Node node) {
        return NodeComparator.getDistance(this, node);
    }

    @Override
    public boolean equals(Object o) {
        return ((Node) o).pid == this.pid;
    }
}
