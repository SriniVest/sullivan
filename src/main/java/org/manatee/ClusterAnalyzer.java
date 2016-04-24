package org.manatee;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HyunJun on 2016-04-24.
 */
public class ClusterAnalyzer {

    public static double DISTANCE_THRESHOLD = 600;

    public ClusterAnalyzer() {

    }

    public DistanceMap<Cluster> analyze(DistanceMap<Node> nodes) {

        DistanceMap<Cluster> clusters = new DistanceMap<>();

        for (Node node : nodes.getNodes()) {

            Cluster cluster = new Cluster(nodes);
            cluster.addNode(node);

            clusters.put(cluster);
        }

        return analyze(nodes, clusters, new ArrayList<Cluster>());
    }

    public DistanceMap<Cluster> analyze(DistanceMap<Node> nodes, DistanceMap<Cluster> clusters, List<Cluster> excluded) {

        if (clusters.size() <= 1)
            return clusters;

        // 만약 모든 노드가 excluded 상태라면
        if (excluded.size() == clusters.size())
            return clusters;

        // 임의의 노드를 하나 선택한다.
        Cluster cluster = clusters.getRandomNode(excluded);

        // 최소 쌍방 인접 클러스터를 찾는다.
        Cluster adjacentCluster = clusters.getAdjacentNode(cluster);

        // 랜덤을 잘못 선택했다면, 리겜!
        if (adjacentCluster == null) {
            excluded.add(cluster);
            return analyze(nodes, clusters, excluded);
        }

        // 인접 노드의 excluded는 풀어준다.
        excluded.remove(adjacentCluster);

        // 두개를 합쳐 보고, 부적절하다면, 리겜!
        if (cluster.getDistance(adjacentCluster) > DISTANCE_THRESHOLD || false) {
            excluded.add(adjacentCluster);
            excluded.add(cluster);

            return analyze(nodes, clusters, excluded);
        }

        // 적절. 두 클러스터를 합친다.
        Cluster mergedCluster = merge(cluster, adjacentCluster);

        clusters.remove(cluster);
        clusters.remove(adjacentCluster);
        clusters.put(mergedCluster);

        // 클러스터 사이즈가 지나치게 작아지는 것을 막는다.
        if (clusters.size() <= Math.max(Math.sqrt(nodes.size() / 2), 3))
            return clusters;

        return analyze(nodes, clusters, excluded);
    }

    /**
     * 두 클러스터를 하나로 합친다.
     *
     * @param cluster1
     * @param cluster2
     * @return
     */
    public Cluster merge(Cluster cluster1, Cluster cluster2) {

        Cluster mergedCluster = new Cluster(cluster1.nodeDistanceMap);

        mergedCluster.addNodes(cluster1.getNodes());
        mergedCluster.addNodes(cluster2.getNodes());

        mergedCluster.description.addAll(cluster1.description);
        mergedCluster.description.addAll(cluster2.description);

        return mergedCluster;
    }

    /**
     * Davies-Bouldin Index를 구한다.
     *
     * @param clusters
     * @return
     */
    public static double getDaviesBouldinIndex(DistanceMap<Cluster> clusters) {
        double sum = 0;

        for (Cluster clusterA : clusters.getNodes()) {
            double max = 0;
            for (Cluster clusterB : clusters.getNodes()) {
                if (clusterA == clusterB) continue;
                double exp = (clusterA.averageCentroidDistance
                        + clusterB.averageCentroidDistance) / clusterA.getDistance(clusterB);
                max = Math.max(max, exp);
            }
            sum += max;
        }
        return sum / clusters.size();
    }

}
