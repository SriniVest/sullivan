package org.sullivan;

import java.util.ArrayList;
import java.util.List;

/**
 * 노드의 집합으로 이루어진 클러스터 하나를 표현하는 클래스
 */
public class SLCluster implements SLMeasurable<SLCluster> {

    /**
     * 클러스터 안에 있을 수 있는 노드 간 최대 거리
     */
    public static int DISTANCE_THRESHOLD = 5;

    /**
     * 이 클러스터가 포함하고 있는 노드의 리스트
     */
    private List<SLNode> nodes;

    /**
     * 이 클러스터가 어떤 클러스터군의 구성원인지 의미하는 context
     */
    private SLClusterGroup context;

    /**
     * 클러스터의 중심 노드. 클러스터를 대표하는 값이라고 할 수 있다
     */
    private SLNode centroid = null;

    /**
     * 각 노드가 centroid로부터 떨어저 있는 거리의 평균
     */
    private double averageCentroidDistance;

    /**
     * description 밀도
     */
    private float descriptionDensity;


    public SLCluster(SLClusterGroup context) {
        this.context = context;
        this.nodes = new ArrayList<>();
    }

    public SLCluster(SLClusterGroup context, List<SLNode> nodes) {
        this.context = context;
        this.nodes = nodes;
    }

    /**
     * 클러스터에 속한 노드의 리스트를 리턴한다.
     *
     * @return
     */
    public List<SLNode> getNodes() {
        return this.nodes;
    }

    /**
     * 이 클러스터의 콘텍스트를 리턴한다.
     *
     * @return
     */
    public SLClusterGroup getContext() {
        return this.context;
    }

    /**
     * 클러스터의 centroid를 리턴한다.
     *
     * @return
     */
    public SLNode getCentroid() {
        return this.centroid;
    }

    /**
     * 구성 노드의 centroid로부터 떨어진 정도의 평균을 리턴한다.
     *
     * @return
     */
    public double getAverageCentroidDistance() {
        return this.averageCentroidDistance;
    }

    /**
     * 설명 밀도를 리턴한다.
     *
     * @return
     */
    public float getDescriptionDensity() {
        return descriptionDensity;
    }

    /**
     * 클러스터 내의 모든 node의 description을 relevance 순으로 리턴한다.
     * TODO: relevance 정렬과 caching
     *
     * @return
     */
    public List<SLDescription> getDescriptions() {
        List<SLDescription> descriptions = new ArrayList<>();

        // 이미 정렬되어 있으므로, 순서대로 하면 된다.
        for (SLNode node : nodes) {
            descriptions.addAll(node.info.descriptions);
        }

        // 만약 description이 비었다면,
        // description density = total descriptions / cluster size
        descriptionDensity = ((float) descriptions.size() / (float) nodes.size());
        if (descriptionDensity < 0.3f) {
            SLDescriptionRequest.request(this);
        }

        // TODO: 이 클러스터에 추가적인 DESCRIPTION이 필요한지 DETERMINE 하는 모델 만들기


        return descriptions;
    }


    /**
     * 클러스터에 노드를 추가한다.
     *
     * @param node
     */
    public void addNode(SLNode node) {

        // 등록되지 않은 노드라면 context에 등록한다.
        if (!context.nodes.hasElement(node))
            insertNode(node);

        this.nodes.add(node);
        updateCentroid();
    }

    /**
     * 클러스터에 다수의 노드를 추가한다.
     *
     * @param nodes
     */
    public void addNodes(List<SLNode> nodes) {

        for (SLNode node : nodes) {

            // 등록되지 않은 노드라면 context에 등록한다.
            if (!context.nodes.hasElement(node))
                insertNode(node);
        }
        this.nodes.addAll(nodes);
        updateCentroid();
    }

    /**
     * 노드를 추가한다.
     * centroid와의 연관 정도에 따라 정렬(inertion sort)한다.
     *
     * @param node
     */
    private void insertNode(SLNode node) {

        boolean added = false;

        for (int i = 0; i < this.nodes.size(); i++) {
            if (context.nodes.getDistance(centroid, node) < context.nodes.getDistance(centroid, this.nodes.get(i))) {
                nodes.add(i, node);
                added = true;
                break;
            }
        }

        if (!added)
            nodes.add(node);
    }


    /**
     * 클러스터에서 노드를 제거한다.
     *
     * @param node
     */
    public void removeNode(SLNode node) {
        this.nodes.remove(node);
        updateCentroid();
    }

    /**
     * 클러스터에서 다수의 노드를 제거한다.
     *
     * @param nodes
     */
    public void removeNodes(List<SLNode> nodes) {
        this.nodes.removeAll(nodes);
        updateCentroid();
    }

    /**
     * 클러스터의 중심 노드를 새로 계산한다.
     */
    private void updateCentroid() {

        if (nodes.size() < 3) {
            if (nodes.size() > 0)
                centroid = nodes.get(0);
            if (nodes.size() > 1) {
                averageCentroidDistance = context.nodes.getDistance(nodes.get(0), nodes.get(1));
            } else {
                averageCentroidDistance = 0;
            }
            return;
        }

        SLNode centroidCandidate = null;
        double minimumSum = Double.POSITIVE_INFINITY;

        for (SLNode nodeA : nodes) {

            double sum = 0;

            for (SLNode nodeB : nodes) {
                if (nodeA == nodeB) continue;

                sum += context.nodes.getDistance(nodeA, nodeB);
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
     * 두 클러스터 간 거리를 구한다.
     *
     * @param cluster
     * @return
     */
    public double getDistance(SLCluster cluster) {
        // TODO: 거리 구하기 알고리즘 Implementation.
        return context.nodes.getDistance(centroid, cluster.centroid);
    }

    /**
     * 다른 클러스터와 병합한다. 리턴되는 값은 새 클러스터이다.
     *
     * @param cluster
     * @return
     */
    public SLCluster merge(SLCluster cluster) {

        SLCluster mergedCluster = new SLCluster(cluster.context);

        // 포함하고 있는 노드들을 합친다.
        mergedCluster.nodes.addAll(this.nodes);
        mergedCluster.nodes.addAll(cluster.nodes);

        // 클러스터에 대한 설명을 합친다.
        //mergedCluster.knowledge = SLKnowledge.merge(this.knowledge, cluster.knowledge);

        return mergedCluster;
    }
}
