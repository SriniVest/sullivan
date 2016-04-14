package org.manatee;

import java.util.List;

/**
 * Created by HyunJun on 2016-04-14.
 */
public class NodeComparator {

    /**
     * Dynamic Time Warping 유사도 비교
     *
     * @param n1
     * @param n2
     * @return
     */
    public static double getDistance(Node n1, Node n2) {

        List<float[]> n = n1.featureMatrix;
        List<float[]> m = n2.featureMatrix;

        int nl = n.size() + 1;
        int ml = m.size() + 1;

        double[][] dMap = new double[nl][ml];

        for (int i = 1; i < nl; i++)
            dMap[i][0] = Float.POSITIVE_INFINITY;

        for (int i = 1; i < ml; i++)
            dMap[0][i] = Float.POSITIVE_INFINITY;

        dMap[0][0] = 0;

        for (int i = 1; i < nl; i++) {
            for (int j = 1; j < ml; j++) {
                double cost = getLocalEuclideanDistance(n.get(i - 1), m.get(j - 1));
                dMap[i][j] = cost + Math.min(Math.min(
                        dMap[i - 1][j], // Insertion
                        dMap[i][j - 1]), // Deletion
                        dMap[i - 1][j - 1]); // Match
            }
        }

        return dMap[nl][ml];
    }

    private static double getLocalDtwDistance(float[] a1, float[] a2) {

        int nl = a1.length + 1;
        int ml = a2.length + 1;

        double[][] dMap = new double[nl][ml];

        for (int i = 1; i < nl; i++)
            dMap[i][0] = Float.POSITIVE_INFINITY;

        for (int i = 1; i < ml; i++)
            dMap[0][i] = Float.POSITIVE_INFINITY;

        dMap[0][0] = 0;

        for (int i = 1; i < nl; i++) {
            for (int j = 1; j < ml; j++) {
                double cost = Math.abs(a1[i - 1] - a2[j - 1]);
                dMap[i][j] = cost + Math.min(Math.min(
                        dMap[i - 1][j], // Insertion
                        dMap[i][j - 1]), // Deletion
                        dMap[i - 1][j - 1]); // Match
            }
        }

        return dMap[nl][ml];
    }

    private static double getLocalEuclideanDistance(float[] a1, float[] a2) {
        if (a1.length != a2.length)
            return Float.POSITIVE_INFINITY;
        double sum = 0;

        for (int i = 0; i < a1.length; i++) {
            sum += Math.pow(a1[i] - a2[i], 2);
        }
        return Math.sqrt(sum);
    }
}
