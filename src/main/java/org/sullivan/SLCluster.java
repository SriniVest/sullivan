/**
 *   ___      _ _ _
 * / __|_  _| | (_)_ ____ _ _ _
 * \__ \ || | | | \ V / _` | ' \
 * |___/\_,_|_|_|_|\_/\__,_|_||_|
 *
 * Copyright 2016 Sullivan Project
 * https://github.com/agemor/sullivan
 *
 * This file is distributed under
 * GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 * for more details, See README.md
 *
 * Sullivan is developed by HyunJun Kim (me@hyunjun.org)
 */

package org.sullivan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 노드의 집합으로 이루어진 클러스터 하나를 표현하는 클래스
 */
public class SLCluster implements SLMeasurable<SLCluster> {

    /**
     * 클러스터 안에 있을 수 있는 노드 간 최대 거리
     */
    public static float DISTANCE_THRESHOLD = 4f;

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

        // centroid와의 연관성에 따라 소팅한다.
        Collections.sort(nodes, (SLNode n1, SLNode n2) ->
                (int) (context.wordNodes.getDistance(n1, centroid) - context.wordNodes.getDistance(n2, centroid))
        );

        for (SLNode node : nodes) {
            descriptions.addAll(node.descriptions);
        }

        /*
        만약 description이 비었다면,
        description density = total descriptions / cluster size
        */
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
        this.nodes.add(node);
        updateCentroid();
    }

    /**
     * 클러스터에 다수의 노드를 추가한다.
     *
     * @param nodes
     */
    public void addNodes(List<SLNode> nodes) {
        this.nodes.addAll(nodes);
        updateCentroid();
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
     * 두 클러스터 간 거리를 구한다.
     *
     * @param cluster
     * @return
     */
    public double getDistance(SLCluster cluster) {
        // TODO: 거리 구하기 알고리즘 Implementation.
        return context.wordNodes.getDistance(centroid, cluster.centroid);
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
        mergedCluster.updateCentroid();

        return mergedCluster;
    }

    /**
     * 클러스터의 중심 노드를 새로 계산한다.
     */
    private void updateCentroid() {

        // 최소 클러스터 크기에 미치지 못할 경우
        if (nodes.size() < 3) {

            if (nodes.size() > 0)
                centroid = nodes.get(0);

            if (nodes.size() > 1)
                averageCentroidDistance = context.wordNodes.getDistance(nodes.get(0), nodes.get(1));
            else
                averageCentroidDistance = 0;

            return;
        }


        double minimumSum = Double.POSITIVE_INFINITY;
        SLNode centroidCandidate = null;

        for (SLNode nodeA : nodes) {

            double sum = 0;

            // 존재하는 다른 모든 노드에 대한 거리를 계산하여 더한다.
            for (SLNode nodeB : nodes) {
                if (nodeA.equals(nodeB)) continue;

                sum += context.wordNodes.getDistance(nodeA, nodeB);
            }

            if (sum < minimumSum) {
                minimumSum = sum;
                centroidCandidate = nodeA;
            }
        }
        centroid = centroidCandidate;
        averageCentroidDistance = minimumSum / nodes.size();
    }

}
