package org.sullivan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 노드는 음성 데이터 하나를 의미한다.
 */
public class SLNode implements SLMeasurable<SLNode>, SLFeatureExtractorListener {

    public static int maximumUid = 0;

    /**
     * 노드의 고유 번호
     */
    public String uid;

    /**
     * 노드에 대한 설명
     */
    public List<SLDescription> descriptions;

    /**
     * 노드의 메타데이터
     */
    public SLNodeInfo info;

    /**
     * 노드의 Feature 행렬
     */
    public List<float[]> featureMatrix;
    public SLFeatureExtractor featureExtractor;

    /**
     * PCM 데이터로부터 노드 생성
     * 이 PCM 데이터의 특성행렬로의 변환은 병렬적으로 이루어진다.
     *
     * @param pcmData
     * @param info
     */
    public SLNode(String uid, SLPcmData pcmData, SLNodeInfo info) {

        this.uid = uid;
        this.info = info;
        this.descriptions = new ArrayList<>();

        featureExtractor = new SLFeatureExtractor(5000, 1);
        featureExtractor.addEventListener(this);
        featureExtractor.process(pcmData);
    }

    /**
     * 이미 존재하는 특성행렬로 노드 생성
     *
     * @param featureMatrix
     * @param info
     */
    public SLNode(String uid, List<float[]> featureMatrix, SLNodeInfo info) {
        this.uid = uid;
        this.info = info;
        this.descriptions = new ArrayList<>();
        this.featureMatrix = featureMatrix;
    }

    /**
     * 이 노드 하나만 포함하고 있는 클러스터를 생성한다.
     *
     * @return
     */
    public SLClustere asCluster(SLClusterGroup context) {
        SLCluster cluster = new SLCluster(context);
        cluster.addNode(this);

        return cluster;
    }

    /**
     * 독립형 단일 노드 클러스터
     *
     * @return
     */
    public SLCluster asCluster() {
        SLCluster cluster = new SLCluster(new SLClusterGroup(new SLDistanceMap<SLNode>()));
        cluster.addNode(this);

        return cluster;
    }

    /**
     * 다른 노드와의 거리를 계산한다.
     * 시간 축에 대해 Dynamic Time Warping을,
     * 주파수 축에 대해선 Euclidean Distance를 적용한다
     *
     * @param node
     * @return
     */
    public double getDistance(SLNode node) {

        List<float[]> n = this.featureMatrix;
        List<float[]> m = node.featureMatrix;

        int nl = n.size() + 1;
        int ml = m.size() + 1;

        double[][] map = new double[nl][ml];

        for (int i = 1; i < nl; i++)
            map[i][0] = Float.POSITIVE_INFINITY;

        for (int i = 1; i < ml; i++)
            map[0][i] = Float.POSITIVE_INFINITY;

        map[0][0] = 0;

        for (int i = 1; i < nl; i++) {
            for (int j = 1; j < ml; j++) {
                double cost = getLocalEuclideanDistance(n.get(i - 1), m.get(j - 1));
                map[i][j] = cost + Math.min(Math.min(
                        map[i - 1][j], // Insertion
                        map[i][j - 1]), // Deletion
                        map[i - 1][j - 1]); // Match
            }
        }

        return map[nl - 1][ml - 1];
    }

    /**
     * 두 노드 간 cost path를 계산한다.
     *
     * @param node
     * @return
     */
    public double[] getCostPath(SLNode node) {

        List<float[]> n = this.featureMatrix;
        List<float[]> m = node.featureMatrix;

        int nl = n.size() + 1;
        int ml = m.size() + 1;

        double[][] map = new double[nl][ml];

        for (int i = 1; i < nl; i++)
            map[i][0] = Float.POSITIVE_INFINITY;

        for (int i = 1; i < ml; i++)
            map[0][i] = Float.POSITIVE_INFINITY;

        map[0][0] = 0;

        for (int i = 1; i < nl; i++) {
            for (int j = 1; j < ml; j++) {
                double cost = getLocalEuclideanDistance(n.get(i - 1), m.get(j - 1));
                map[i][j] = cost + Math.min(Math.min(
                        map[i - 1][j], // Insertion
                        map[i][j - 1]), // Deletion
                        map[i - 1][j - 1]); // Match
            }
        }

        // 여기까진 일반적인 distance 구하는 거랑 일치.
        // 역추적하면 path를 구할 수 있다.
        int i = nl - 1;
        int j = ml - 1;

        double[] path = new double[Math.max(i, j) + 1];
        int pathIndex = path.length - 1;

        while (true) {
            double minimum = map[i][j]; // 시작점

            int ni = 0, nj = 0;

            if (i > 0 && j > 0 && map[i - 1][j - 1] < minimum) {
                minimum = map[i - 1][j - 1];
                ni = i - 1;
                nj = j - 1;
            }

            if (i > 0 && j >= 0 && map[i - 1][j] < minimum) {
                minimum = map[i - 1][j];
                ni = i - 1;
                nj = j;
            }

            if (i >= 0 && j > 0 && map[i][j - 1] < minimum) {
                minimum = map[i][j - 1];
                ni = i;
                nj = j - 1;
            }

            path[pathIndex--] = minimum;

            if (pathIndex < 0) break;

            i = ni;
            j = nj;
        }

        // 누적분포 -> 일반 분포로 변경
        double maximum = path[0];
        for (int k = 1; k < path.length; k++) {
            path[k] = path[k] - path[k - 1];
            maximum = Math.max(path[k], maximum);
        }

        // 일반화
        for (int k = 0; k < path.length; k++) {
            path[k] = path[k] / maximum;
        }

        return path;
    }

    /**
     * 두 특성 행렬의 행에 대해 Euclidean 거리를 계산한다.
     *
     * @param a
     * @param b
     * @return
     */
    private double getLocalEuclideanDistance(float[] a, float[] b) {
        if (a.length != b.length)
            return Float.POSITIVE_INFINITY;
        double sum = 0;

        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }

    /**
     * feature matrix 추출이 완료되었을 때
     *
     * @param featureMatrix
     */
    public void onFeatureExtracted(List<float[]> featureMatrix) {
        this.featureMatrix = featureMatrix;
        featureExtractor = null;
    }

    // 노드의 동일성은 uid로만 판단한다.
    @Override
    public boolean equals(Object node) {
        return this.uid.equals(((SLNode) node).uid);
    }

    /**
     * 노드의 메타데이터를 담는 클래스
     */
    public static class SLNodeInfo {

        /**
         * 음성 데이터 소스
         */
        public File source;

        /**
         * 데이터의 레이어
         */
        public String layer;

        /**
         * 녹음자의 ID
         */
        public String recorder;

        /**
         * 녹음자의 나이
         */
        public String recorderAge;

        /**
         * 녹음자의 성별
         */
        public String recorderSex;

        /**
         * 녹음된 날짜
         */
        public String recordedDate;


        public SLNodeInfo() {
        }
    }

}
