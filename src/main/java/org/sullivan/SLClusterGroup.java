package org.sullivan;

import java.util.LinkedList;
import java.util.List;

/**
 * 클러스터 간 비교 처리가 용이하도록 클러스터군의 정보를 종합하는 클래스
 * 큰 의미로는 클러스터 군으로 볼 수 있다.
 * 컨텍스트를 공유함으로써 고비용 고빈도의 계산(예를 들어 노드 간 거리 계산)을 재사용 할 수 있게 되므로
 * 실행 시간에서 이득을 볼 수 있다.
 */
public class SLClusterGroup {

    /**
     * 클러스터군의 전체 노드 데이터베이스
     */
    public SLDistanceMap<SLNode> nodes;

    /**
     * 클러스터 분석기
     */
    public SLClusterAnalyzer analyzer;

    /**
     * 전체 클러스터 리스트
     */
    public SLDistanceMap<SLCluster> clusters;

    /**
     * 고립군으로 분류된 클러스터 리스트 (프로세싱에서만 사용되는 임시 value)
     */
    public List<SLCluster> isolatedClusters;

    public SLClusterGroup(SLDistanceMap<SLNode> nodeMap) {
        this.analyzer = new SLClusterAnalyzer(this);
        this.nodes = nodeMap;
        this.clusters = new SLDistanceMap<>();
        this.isolatedClusters = new LinkedList<>();

        analyzer.build();
    }

    public SLClusterGroup() {
        this(new SLDistanceMap<SLNode>());
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
