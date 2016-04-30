package org.sullivan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 클러스터 분석을 수행하는 클래스
 * Agglomerative-Hierarchical 클러스터 분석을 수행한다.
 * <p>
 * 추가로 최소 threshold와, min-cluster수에 제한을 두었다.
 * 초기에는 많은 단어에 대해 데이터가 부족할 것이기 때문이다.
 */
public class SLClusterAnalyzer {

    /**
     * 분석의 대상이 되는 클러스터군
     */
    public SLClusterGroup context;

    /**
     * 고립군으로 분류된 클러스터 리스트 (프로세싱에서만 사용되는 임시 value)
     */
    private List<SLCluster> isolatedClusters;

    public SLClusterAnalyzer(SLClusterGroup context) {
        this.context = context;
        this.isolatedClusters = new LinkedList<>();
    }

    /**
     * 클러스터 전분석을 시행한다.
     */
    public void initialize() {

        // 초기화
        context.clusters.clear();
        isolatedClusters.clear();

        for (SLNode node : context.nodes) {
            context.clusters.add(node.asCluster(context));
        }

        agglomerate();
    }

    /**
     * 노드 하나를 추가한 후 분석한다.
     *
     * @param node
     */
    public SLCluster insert(SLNode node) {

        // 모든 클러스터의 centroid를 비교해서 가장 가까운 것을 택한다.
        // 만약 그 거리가 threshold보다 작으면 편입, 크면 독립

        double minimum = Double.POSITIVE_INFINITY;
        SLCluster closestCluster = null;

        for (SLCluster cluster : context.clusters.getList()) {
            double distance = context.wordNodes.getDistance(cluster.getCentroid(), node);
            if (distance < minimum) {
                minimum = distance;
                closestCluster = cluster;
            }
        }

        if (minimum < SLCluster.DISTANCE_THRESHOLD) {
            closestCluster.addNode(node);
        } else {
            context.clusters.add(node.asCluster(context));
        }

        return closestCluster;
    }

    /**
     * 노드를 제거한 후 재 분석한다.
     *
     * @param node
     */
    public void displace(SLNode node) {
        ㅂㄷㅎㅂㄷㅎ;
    }

    /**
     * 한계에 이를 때까지 내부를 자가 응집한다.
     *
     * @return
     */
    private void agglomerate() {

        // 클러스터의 크기가 1이라면 분석할 필요가 없다.
        if (context.clusters.size() <= 1)
            return;

        // 모든 클러스터가 isolated 상태라면 분석을 종료한다.
        if (isolatedClusters.size() == context.clusters.size())
            return;

        // 임의의 노드를 하나 선택한다.
        SLCluster cluster = context.clusters.getRandomElement(isolatedClusters);

        // 최소 쌍방-인접 클러스터를 찾는다.
        SLCluster adjacentCluster = context.clusters.getAdjacentElement(cluster);

        // 고립된 클러스터라면, 다음에 선택되지 않도록 플래그에 추가해 준다.
        if (adjacentCluster == null) {
            isolatedClusters.add(cluster);
            agglomerate();
            return;
        }

        // 피선택된 클러스터의 플래그를 제거한다. (없을 수도 있음)
        isolatedClusters.remove(adjacentCluster);

        // 두 클러스터를 합치는 것이 적합한지 검사
        // TODO: 클러스터 분산도도 체크한다.
        if (cluster.getDistance(adjacentCluster) > SLCluster.DISTANCE_THRESHOLD || false) {

            // 적절하지 않다면 두 클러스터 모두 고립 클러스터로 등록한다.
            isolatedClusters.add(cluster);
            isolatedClusters.add(adjacentCluster);

            agglomerate();
            return;
        }

        // 두 클러스터를 병합한 후, 등록한다.
        SLCluster mergedCluster = cluster.merge(adjacentCluster);

        context.clusters.remove(cluster);
        context.clusters.remove(adjacentCluster);
        context.clusters.add(mergedCluster);

        // 클러스터 사이즈가 지나치게 작아지는 것을 막는다.
        if (context.clusters.size() <= Math.max(Math.sqrt(context.wordNodes.size() / 2), 3))
            return;

        agglomerate();
    }
}
