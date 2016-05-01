package org.sullivan;

import java.util.ArrayList;
import java.util.List;

/**
 * 클러스터 분석을 통한 종합 발음 평가 결과를 표현하고 가공하는 클래스
 */
public class SLEvaluationReport {

    /**
     * 평가 대상
     */
    public SLNode attempt;

    /**
     * 발음의 특성
     */
    public SLReportCharacteristics characteristics;

    /**
     * 발음의 분류
     */
    public boolean classifiedAsFailure = false;

    /**
     * 발음 역추적 패스 (발음 실패로 분류되었을 경우에만 사용됨)
     */
    public SLCmvPath<SLCluster> backtrackingPath;

    public SLEvaluationReport() {
        this.characteristics = new SLReportCharacteristics();
    }

    public String getResult() {
        /**
         * 보여줄 데이터
         *
         * 1. 종합 점수: model 데이터와 비교한 결과.
         * 2. 유사도 그래프: model 데이터의 dtw time 그래프 보여줌
         * 3. 발음 특성: 발음 특성 description 상위권부터 나열
         * 4. 교정 트랙: 교정 패스의 대표 description 나열
         */
        String report = "";

        double cost = characteristics.model.getCentroid().getDistance(attempt); // distance. 이제 이걸 정규분포화해야하는데..! (TODO)
        report += "pronunciation score: " + cost + " (" + Math.round(SLCluster.DISTANCE_THRESHOLD * 100 / cost) + "%)\n";
        report += "*lower the score, the better.\n";
        report += "classification: " + (classifiedAsFailure ? "failed" : "succeed") + "\n";

        double[] costGraph = characteristics.model.getCentroid().getCostPath(attempt);
        report += "accuracy graph: (" + Math.round(costGraph[0] * 100);
        for (int i = 1; i < costGraph.length; i++)
            report += ", " + Math.round(costGraph[i] * 100);
        report += ")\n";

        report += "pronunciation characteristics: \n";
        int rank = 1;

        for (SLDescription description :  characteristics.model.getDescriptions()) {
            report += "    (" + (rank++) + ") " + description.description + "\n";
        }
        for (SLCluster cluster : characteristics.success) {
            for (SLDescription description : cluster.getDescriptions()) {
                report += "    (" + (rank++) + ") " + description.description + "\n";
            }
        }
        report += "*higher the rank, higher the feasibility.\n";

        report += "pronunciation weaknesses: \n";
        rank = 1;
        for (SLCluster cluster : characteristics.failure) {
            for (SLDescription description : cluster.getDescriptions()) {
                report += "    (" + (rank++) + ") " + description.description + "\n";
            }
        }
        report += "*higher the rank, higher the feasibility.\n";

        if (classifiedAsFailure) {
            report += "optimal correction route: \n";

            int no = 1;

            for (SLCluster step : backtrackingPath.steps) {
                report += "    (" + (no++) + ") " + step.getCentroid().uid + "\n";
                for (SLDescription description : step.getDescriptions()) {
                    report += "       " + description.description + "\n";
                }
            }
            report += "total correction cost: " + Math.round(backtrackingPath.getCost()) + "";
        }

        return report;
    }

    public String getResultAsJson() {

        String report = "{";

        double cost = characteristics.model.getCentroid().getDistance(attempt); // distance. 이제 이걸 정규분포화해야하는데..! (TODO)
        report += "\"score\": " + cost + ",\n";
        report += "\"threshold: " + SLCluster.DISTANCE_THRESHOLD + ",";
        report += "\"classification\": " + (classifiedAsFailure ? "\"failed\"" : "\"succeed\"") + ",\n";
        report += "\"graph\": [";

        double[] costGraph = characteristics.model.getCentroid().getCostPath(attempt);
        report += Math.round(costGraph[0] * 100);
        for (int i = 1; i < costGraph.length; i++)
            report += ", " + Math.round(costGraph[i] * 100);
        report += "],\n";

        report += "\"characteristics\": [";
        for (SLCluster cluster : characteristics.success) {
            for (SLDescription description : cluster.getDescriptions()) {
                report += "\"" + description.description + "\",";
            }
        }
        report += "],\n";

        report += "\"weaknesses\": \n";
        for (SLCluster cluster : characteristics.failure) {
            for (SLDescription description : cluster.getDescriptions()) {
                report += "\"" + description.description + "\",";
            }
        }
        report += "],\n";

        if (classifiedAsFailure) {
            report += "\"route\": [";
            for (SLCluster step : backtrackingPath.steps) {
                report += "{\n\"node\": " + step.getCentroid().uid + "\",\n\"description\": [";
                for (SLDescription description : step.getDescriptions()) {
                    report += description.description + ", ";
                }
                report += "]}\n";
            }
            report += "],\n";
            report += "\"routeCost\": " + Math.round(backtrackingPath.getCost()) + "\n";
        }
        report += "\n}";

        return report;
    }

    /**
     * 리포트에서 발음 특성을 표현하는 클래스
     */
    public static class SLReportCharacteristics {

        /**
         * 가장 근접한 모델 발음
         */
        public SLCluster model;

        /**
         * 유사한 성공사례 발음
         */
        public List<SLCluster> success;

        /**
         * 유사한 실패사례 발음
         */
        public List<SLCluster> failure;

        /**
         * 가장 근접한 모델 발음
         */
        public SLCluster analyzed;


        public SLReportCharacteristics() {
            this.success = new ArrayList<>();
            this.failure = new ArrayList<>();
        }

        public SLReportCharacteristics(SLCluster model, List<SLCluster> success, List<SLCluster> failure, SLCluster analyzed) {
            this.model = model;
            this.success = success;
            this.failure = failure;
            this.analyzed = analyzed;
        }
    }
}
