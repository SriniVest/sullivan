package org.sullivan;

import java.util.ArrayList;
import java.util.List;

/**
 * 클러스터 간 비교 처리가 용이하도록 클러스터군의 정보를 종합하는 클래스
 * 큰 의미로는 클러스터 군으로 볼 수 있다.
 * 컨텍스트를 공유함으로써 고비용 고빈도의 계산(예를 들어 노드 간 거리 계산)을 재사용 할 수 있게 되므로
 * 실행 시간에서 이득을 볼 수 있다.
 */
public class SLClusterGroup {

    /**
     * 워드 내부에서 공유되는 노드 맵
     */
    public SLDistanceMap<SLNode> wordNodes;

    /**
     * 클러스터 그룹의 노드들
     */
    public List<SLNode> nodes;

    /**
     * 클러스터 분석기
     */
    public SLClusterAnalyzer analyzer;

    /**
     * 전체 클러스터 리스트
     */
    public SLDistanceMap<SLCluster> clusters;

    public SLClusterGroup(SLDistanceMap<SLNode> distanceCache) {

        this.wordNodes = distanceCache;
        this.nodes = new ArrayList<>();

        this.analyzer = new SLClusterAnalyzer(this);
        this.clusters = new SLDistanceMap<>();
    }

    /**
     * 클러스터 그룹에 노드를 추가한다. 추가한 노드는 분석된다.
     *
     * @param node
     */
    public SLCluster addNode(SLNode node) {

        // 워드 노드에 추가한다.
        wordNodes.add(node);

        // 그룹내 노드 리스트에 추가한다.
        nodes.add(node);

        // 분석한다.
        return analyzer.insert(node);
    }

    /**
     * 그룹에서 노드를 제거한다.
     */
    public void removeNode(SLNode node) {

        // 분석에서 제거한다.
        analyzer.displace(node);

        // 그룹 내 리스트에서 제거한다.
        nodes.remove(node);

        // 전체 워드 캐시에서 제거한다.
        wordNodes.remove(node);
    }

    /**
     * 이 클러스터군의 Davies-Bouldin Index를 구한다.
     *
     * @return
     */
    public double getDaviesBouldinIndex() {
        double sum = 0;
        for (SLCluster clusterA : clusters.getList()) {
            double max = 0;
            for (SLCluster clusterB : clusters.getList()) {
                if (clusterA == clusterB) continue;
                double exp = (clusterA.getAverageCentroidDistance()
                        + clusterB.getAverageCentroidDistance()) / clusterA.getDistance(clusterB);
                max = Math.max(max, exp);
            }
            sum += max;
        }
        return sum / clusters.size();
    }
}
