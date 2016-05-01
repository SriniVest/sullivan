package org.sullivan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 설명이 부족한 클러스터들에 대한 description 데이터를 사용자에게 요쳥한다.
 */
public class SLDescriptionRequest {

    /**
     * 요청 대상 클러스터
     */
    public SLCluster targetCluster;

    /**
     * 타겟 노드
     */
    private SLNode targetNode;

    /**
     * 음성 재생용 유틸
     */
    public SLPlayer player;


    public SLDescriptionRequest(SLCluster cluster) {
        this.targetCluster = cluster;
        this.player = new SLPlayer();
    }

    /**
     * 랜덤으로 n 노드의 소리를 들려준다.
     */
    public void play(int times) {

        List<SLNode> nodes = targetCluster.getNodes();

        for (int i = 0; i < times; i++) {
            targetNode = nodes.get((int) Math.round((nodes.size() - 1) * Math.random()));
            player.play(targetNode.info.source);
        }
    }

    /**
     * 사용자의 응답을 기록한다.
     *
     * @param response
     */
    public void answer(String response) {
        if (targetNode == null) return;
        targetNode.descriptions.add(new SLDescription(response, new SLDescription.SLDescriptionInfo()));
    }

    /**
     * 요청 우선순위. 급한 request를 우선 처리하기 위함.
     */
    private static Map<SLCluster, Integer> requestPriority;
    public static int numberOfRequests = 0;

    static {
        requestPriority = new HashMap<>();
    }

    /**
     * 유저에게 description을 요청한다.
     *
     * @param cluster
     */
    public static void request(SLCluster cluster) {

        if (!requestPriority.containsKey(cluster))
            requestPriority.put(cluster, 0);

        // 빈도수가 많은 요청일수록 우선순위를 높히기 위함.
        requestPriority.put(cluster, requestPriority.get(cluster) + 1);
        numberOfRequests++;
    }

    /**
     * 요청된 description을 우선순위에 따라 처리한다.
     *
     * @return
     */
    public static SLDescriptionRequest resolve() {

        // 우선순위 분석.
        // 모델: description density * sqrt(number of requests)

        double maximum = Double.NEGATIVE_INFINITY;
        SLCluster resolvedCluster = null;

        for (SLCluster cluster : requestPriority.keySet()) {
            double priorityIndex = cluster.getDescriptionDensity() * Math.sqrt(requestPriority.get(cluster));

            if (priorityIndex > maximum) {
                maximum = priorityIndex;
                resolvedCluster = cluster;
            }
        }

        if (requestPriority.get(resolvedCluster) <= 1) {
            requestPriority.remove(resolvedCluster);
        } else {
            requestPriority.put(resolvedCluster, requestPriority.get(resolvedCluster) - 1);
        }
        numberOfRequests--;

        SLDescriptionRequest descriptionRequest = new SLDescriptionRequest(resolvedCluster);

        return descriptionRequest;
    }

}
