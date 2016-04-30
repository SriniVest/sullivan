package org.sullivan;

import java.util.ArrayList;
import java.util.List;

/**
 * 한 단어를 표현하는 클래스
 * <p>
 * 단어의 발음에 대한 모든 정보가 여기에 저장되고,
 * 이 단어와 관련한 모든 작업이 여기서 처리된다.
 */
public class SLWord {

    /**
     * 워드의 이름
     */
    public String name;

    /**
     * 워드의 메타데이터
     */
    public SLWordInfo info;

    /**
     * 워드 내의 모든 노드가 들어있는 리스트 (최상위 distance 캐시 객체)
     */
    public SLDistanceMap<SLNode> nodes;

    /**
     * 클러스터 레이어
     */
    public SLClusterLayer layer;


    public SLWord(String name, SLWordInfo info) {

        this.name = name;
        this.info = info;

        nodes = new SLDistanceMap<>();
        layer = new SLClusterLayer();
    }

    /**
     * 클러스터 분석을 통해 노드의 성질을 유추한다.
     *
     * @param node
     * @return
     */
    public SLEvaluationReport evaluate(SLNode node) {

        // 결과 리포트
        SLEvaluationReport report = new SLEvaluationReport();
        report.attempt = node;

        // 가장 유사한 모델 클러스터
        SLCluster closestModelCluster;

        // 노드가 분석에 의해 추가된 클러스터
        SLCluster analyzedCluster;

        // 모델 클러스터에서 가장 인접한 노드를 찾는다.
        closestModelCluster = clusterLayer.model.clusters.getClosestElement(node.asCluster());

        // 근접 유사도를 검사한다.
        double distance = closestModelCluster.getDistance(node.asCluster());

        // 최대 유사도 거리 한계치를 벗어날 경우: Failure 클러스터에 삽입한다.
        if (distance > SLCluster.DISTANCE_THRESHOLD) {
            report.classifiedAsFailure = true;
            analyzedCluster = clusterLayer.failure.analyzer.insertNode(node);
        }

        // 한계치 내부에 있을 경우: Success 클러스터에 삽입한다.
        else {
            report.classifiedAsFailure = false;
            analyzedCluster = clusterLayer.success.analyzer.insertNode(node);
        }

        // 클러스터 특성을 분석한다.
        report.characteristics.analyzed = analyzedCluster;

        // 1. 모델 특성: 어떤 모델에 가장 가까운가.
        report.characteristics.model = closestModelCluster;

        // 2. 세부 발음 특성: THRESHOLD 내에 있는 클러스터거나,
        // 거리의 Gaussian 분포에서 유사도 상위 30%안에 있는 클러스터의 특성을 제시한다.
        report.characteristics.success = clusterLayer.success.clusters.getCloseElements(analyzedCluster, 0.3f);

        // 3. 취약점 분석
        // 마찬가지로 THRESHOLD 안에 있는 클러스터와 가우시안 상위 30% 클러스터 특징을 불러온다.
        report.characteristics.failure = clusterLayer.failure.clusters.getCloseElements(analyzedCluster, 0.3f);

        // 4. 교정 (Failure 의 경우)
        // success layer 까지의 최단경로를 찾는다. 이 때 경로는
        // d = P * sqrt(n) * max(d1, d2, ... , dn)으로 모델링한다. (P는 보정 상수)

        /**
         * analyzed cluster (failure layer)에서 시작한다.
         * BFS로 노드를 계산하는데...
         * 재귀 + DP(캐싱) 사용
         */

        if (report.classifiedAsFailure) {
            report.backtrackingPath = getOptimalPathToSuccess(analyzedCluster);
        }

        return report;
    }

    /**
     * 실패사례 레이어의 특정 클러스터가 성공사례 레이어로 가기까지의 최적 경로를 계산한다.
     *
     * @param start
     * @return
     */
    private SLCmvPath<SLCluster> getOptimalPathToSuccess(SLCluster start) {
        return getOptimalPathToSuccess(start, new ArrayList<>());
    }

    private SLCmvPath<SLCluster> getOptimalPathToSuccess(SLCluster start, List<SLCluster> excluded) {

        List<SLCluster> targetClusters = clusterLayer.failure.clusters.getList();

        // 이 노드가 종착점일 경우
        SLCmvPath<SLCluster> optimalPath = new SLCmvPath<>();

        // 가장 가까운 success 클러스터를 찾는다.
        SLCluster closestCluster = clusterLayer.success.clusters.getClosestElement(start);
        optimalPath.addStep(closestCluster);
        optimalPath.addStepToFront(start);

        for (SLCluster targetCluster : targetClusters) {
            if (excluded.contains(targetCluster)) continue;

            List<SLCluster> subExcluded = new ArrayList<>(excluded);
            subExcluded.add(start);

            // 이 노드가 종착점이 아닌 가장 짧은 노드. 재귀적으로 계산
            SLCmvPath<SLCluster> path = getOptimalPathToSuccess(targetCluster, subExcluded);
            path.addStepToFront(start);

            // 새로 구한 경로가 더 좋으면 교체
            if (path.getCost() < optimalPath.getCost()) {
                optimalPath = path;
            }
        }

        return optimalPath;
    }

    /**
     * 이 클러스터의 상태에 대한 보고서를 리턴한다.
     *
     * @return
     */
    public String getStatus() {

        String report = "";

        report += "name: " + name + "\n";
        report += "version: " + info.version + "\n";
        report += "updated: " + info.registeredDate + "\n";
        report += "model layer: \n";
        report += "    total wordNodes: " + nodeLayer.model.size() + "\n";
        report += "    total clusters: " + clusterLayer.model.clusters.size() + "\n";
        report += getLayerStatus(clusterLayer.model.clusters.getList());

        report += "success layer: \n";
        report += "    total wordNodes: " + nodeLayer.success.size() + "\n";
        report += "    total clusters: " + clusterLayer.success.clusters.size() + "\n";
        report += getLayerStatus(clusterLayer.success.clusters.getList());

        report += "failure layer: \n";
        report += "    total wordNodes: " + nodeLayer.failure.size() + "\n";
        report += "    total clusters: " + clusterLayer.failure.clusters.size() + "\n";
        report += getLayerStatus(clusterLayer.failure.clusters.getList());

        return report;
    }

    private String getLayerStatus(List<SLCluster> clusters) {

        String report = "";

        int index = 0;

        for (SLCluster cluster : clusters) {
            report += "    cluster#" + (index++) + ": \n";
            report += "        size: " + cluster.getNodes().size() + "\n";
            report += "        centroid: " + cluster.getCentroid().uid + "\n";
            report += "        dd: " + cluster.getDescriptionDensity() + "\n";
            report += "        acd: " + cluster.getAverageCentroidDistance() + "\n";
        }

        return report;
    }

    /**
     * 워드 내부 클러스터의 레이어를 표현하는 클래스
     */
    public static class SLClusterLayer {

        /**
         * 모델 클러스터
         */
        public SLClusterGroup model;

        /**
         * 성공사례 클러스터
         */
        public SLClusterGroup success;

        /**
         * 실패사례 클러스터
         */
        public SLClusterGroup failure;

        public SLClusterLayer(SLDistanceMap<SLNode> context) {
            this.model = new SLClusterGroup(context);
            this.success = new SLClusterGroup(context);
            this.failure = new SLClusterGroup(context);
        }
    }

    /**
     * 워드의 메타데이터를 표현하는 클래스
     */
    public static class SLWordInfo {

        public String version;
        public String registeredDate;

        public SLWordInfo() {
        }
    }
}
