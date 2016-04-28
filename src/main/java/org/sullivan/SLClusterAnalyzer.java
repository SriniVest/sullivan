package org.sullivan;

/**
 * 클러스터 분석을 수행하는 클래스
 * Agglomerative-Hierarchical 클러스터 분석을 수행한다.
 * <p>
 * 추가로 최소 threshold와, min-cluster수에 제한을 두었다.
 * 초기에는 많은 단어에 대해 데이터가 부족할 것이기 때문이다.
 */
public class SLClusterAnalyzer {

    public SLClusterGroup context;

    public SLClusterAnalyzer(SLClusterGroup context) {
        this.context = context;
    }

    /**
     * 클러스터 전분석을 수행한다.
     *
     * @return
     */
    public void build() {

        // 클러스터의 크기가 1이라면 분석할 필요가 없다.
        if (context.clusters.size() <= 1)
            return;

        // 모든 클러스터가 isolated 상태라면 분석을 종료한다.
        if (context.isolatedClusters.size() == context.clusters.size())
            return;

        // 임의의 노드를 하나 선택한다.
        SLCluster cluster = context.clusters.getRandomElement(context.isolatedClusters);

        // 최소 쌍방-인접 클러스터를 찾는다.
        SLCluster adjacentCluster = context.clusters.getAdjacentElement(cluster);

        // 고립된 클러스터라면, 다음에 선택되지 않도록 플래그에 추가해 준다.
        if (adjacentCluster == null) {
            context.isolatedClusters.add(cluster);
            build();
            return;
        }

        // 피선택된 클러스터의 플래그를 제거한다. (없을 수도 있음)
        context.isolatedClusters.remove(adjacentCluster);

        // 두 클러스터를 합치는 것이 적합한지 검사
        // TODO: 클러스터 분산도도 체크한다.
        if (cluster.getDistance(adjacentCluster) > SLCluster.DISTANCE_THRESHOLD || false) {

            // 적절하지 않다면 두 클러스터 모두 고립 클러스터로 등록한다.
            context.isolatedClusters.add(cluster);
            context.isolatedClusters.add(adjacentCluster);

            build();
            return;
        }

        // 두 클러스터를 병합한 후, 등록한다.
        SLCluster mergedCluster = cluster.merge(adjacentCluster);

        context.clusters.remove(cluster);
        context.clusters.remove(adjacentCluster);
        context.clusters.add(mergedCluster);

        // 클러스터 사이즈가 지나치게 작아지는 것을 막는다.
        if (context.clusters.size() <= Math.max(Math.sqrt(context.nodes.size() / 2), 3))
            return;

        build();
    }

    /**
     * 노드 하나를 클러스터 군에 추가한 뒤, 편입시킨다.
     *
     * @param node
     */
    public SLCluster insertNode(SLNode node) {

        // 모든 클러스터의 centroid를 비교해서 가장 가까운 것을 택한다.
        // 만약 그 거리가 threshold보다 작으면 편입, 크면 독립

        double minimum = Double.POSITIVE_INFINITY;
        SLCluster closestCluster = null;

        for (SLCluster cluster : context.clusters.getList()) {
            double distance = context.nodes.getDistance(cluster.getCentroid(), node);
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
}
