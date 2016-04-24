package org.manatee;

import com.sun.org.apache.bcel.internal.generic.NEW;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HyunJun on 2016-04-24.
 */
public class Cluster implements Measurable<Cluster>{

    public DistanceMap<Node> nodeDistanceMap;

    public Node centroid;
    public double averageCentroidDistance = 0;

    private List<Node> nodes;
    public List<String> description;

    private NeuralNetwork neuralNetwork;
    private int neuralNetworkCapacity = 8;
    private boolean neuralNetworkTrained = false;


    public Cluster(DistanceMap<Node> nodeDistanceMap) {
        this.nodeDistanceMap = nodeDistanceMap;
        nodes = new ArrayList<>();
        description = new ArrayList<>();
        neuralNetwork = new NeuralNetwork(12 * neuralNetworkCapacity, 7);
    }

    public void trainNeuralNetwork() {

            List<double[]> trainingData = new ArrayList<>();

            // method 1: compressionRate가 1보다 크면, ration splition, 작으면 interpolation
            // method 2: downsampling
            for (Node node : nodes) {
                double compressionRate =  node.featureMatrix.size() / neuralNetworkCapacity;
                double[] data = new double[12 * neuralNetworkCapacity];

                for(int i = 0; i < neuralNetworkCapacity; i ++) {
                    for (int j = 0; j < 12; j++) {
                        data[i * neuralNetworkCapacity + j] = node.featureMatrix.get((int)Math.floor(i * compressionRate))[j];
                    }
                }
                trainingData.add(data);
            }

            // 400번 훈련
            for (int n = 0; n < 400; n++) {
            for (int i = 0; i < trainingData.size(); i++) {
                neuralNetwork.train(trainingData.get(i), new double[]{nodeDistanceMap.get(centroid, nodes.get(i))}, 0.15);
            }
        }
    }

    public List<Node> getNodes() {
        return this.nodes;
    }

    public void addNode(Node node) {
        this.nodes.add(node);
        updateCentroid();

        neuralNetworkTrained = false;
    }

    public void addNodes(List<Node> nodes) {
        this.nodes.addAll(nodes);
        updateCentroid();

        neuralNetworkTrained = false;
    }

    public void removeNode(Node node){
        this.nodes.remove(node);
        updateCentroid();

        neuralNetworkTrained = false;
    }

    public void removeNodes(List<Node> nodes) {
        this.nodes.removeAll(nodes);
        updateCentroid();

        neuralNetworkTrained = false;
    }

    /**
     * 클러스터 중심을 업데이트한다.
     */
    protected void updateCentroid() {

        if (nodes.size() < 3) {

            if (nodes.size() > 0)
                centroid = nodes.get(0);

            if (nodes.size() > 1) {
                averageCentroidDistance = nodeDistanceMap.get(nodes.get(0), nodes.get(1));
            } else {
                averageCentroidDistance = 0;
            }
            return;
        }

        Node centroidCandidate = null;
        double minimumSum = Double.POSITIVE_INFINITY;

        for(Node nodeA : nodes) {

            double sum = 0;

            for (Node nodeB : nodes) {
                if (nodeA == nodeB) continue;

                sum += nodeDistanceMap.get(nodeA, nodeB);
            }

            if (sum < minimumSum) {
                minimumSum = sum;
                centroidCandidate = nodeA;
            }
        }
        centroid = centroidCandidate;
        averageCentroidDistance = minimumSum / nodes.size();
    }

    /**
     * 두 클러스터 사이 거리를 구한다.
     *
     * @param cluster
     * @return
     */
    public double getDistance(Cluster cluster) {
        return getCentroidDistance(cluster);
    }

    public double getCentroidDistance(Cluster cluster) {
        return nodeDistanceMap.get(centroid, cluster.centroid);
    }


    /**
     * 인공신경망을 통해 학습된 클러스터 특징으로 거리를 계산한다.
     *
     * @param cluster
     * @return
     */
    public double getNeuralDistance(Cluster cluster) {

        // 학습되어 있지 않다면 학습한다.
        if (neuralNetworkTrained == false) {
            trainNeuralNetwork();
            neuralNetworkTrained = true;
        }

        double compressionRate =  cluster.centroid.featureMatrix.size() / neuralNetworkCapacity;
        double[] data = new double[12 * neuralNetworkCapacity];

        for(int i = 0; i < neuralNetworkCapacity; i ++) {
            for (int j = 0; j < 12; j++) {
                data[i * neuralNetworkCapacity + j] = cluster.centroid.featureMatrix.get((int)Math.floor(i * compressionRate))[j];
            }
        }

        return neuralNetwork.evaluate(data);
    }
}
