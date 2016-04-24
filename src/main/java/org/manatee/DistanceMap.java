package org.manatee;

import java.util.*;

/**
 * Created by HyunJun on 2016-04-24.
 */
public class DistanceMap<T extends Measurable<T>> {

    protected Map<T, Map<T, Double>> distanceMap;

    public DistanceMap() {
        distanceMap = new HashMap<>();
    }

    /**
     * 거리 데이터 불러오기
     *
     * @param node1
     * @param node2
     * @return
     */
    public double get(T node1, T node2) {

        if (!distanceMap.containsKey(node1))
            put(node1);

        Map<T, Double> map = distanceMap.get(node1);

        // 아직 거리 계산이 되지 않았을 경우
        if (!map.containsKey(node2)) {

            double distance = node1.getDistance(node2);

            if (!distanceMap.containsKey(node2))
                put(node2);

            // 데이터 대칭
            map.put(node2, distance);
            distanceMap.get(node2).put(node1, distance);
        }

        return map.get(node2).doubleValue();
    }

    public List<T> getNodes() {
        return new ArrayList<>(distanceMap.keySet());
    }

    /**
     * 최소 쌍방 인접 노드 찾기
     *
     * @param node
     * @return
     */
    public T getAdjacentNode(T node) {

        if (!distanceMap.containsKey(node))
            put(node);

        List<T> adjacentNodes = new ArrayList<>();
        double minimum = 0;

        for (T key : distanceMap.keySet()) {

            if (key == node) continue;

            double distance = get(node, key);

            if (distance < minimum) {

                adjacentNodes.clear();
                adjacentNodes.add(key);
                minimum = distance;

            } else if (distance == minimum) {
                adjacentNodes.add(key);
            }
        }

        for (T adjacentNode : adjacentNodes) {
            if (getAdjacentNode(adjacentNode) == node) {
                return adjacentNode;
            }
        }

        return null;
    }

    /**
     * 랜덤한 노드를 리턴한다.
     *
     * @return
     */
    public T getRandomNode(List<T> excluded) {

        Random random = new Random();
        List<T> keys = new ArrayList<T>(distanceMap.keySet());

        if (excluded != null)
            keys.removeAll(excluded);

        return keys.get(random.nextInt(keys.size()));
    }

    public int size() {
        return distanceMap.size();
    }

    /**
     * 원소 추가
     *
     * @param node
     */
    public void put(T node) {
        distanceMap.put(node, new HashMap<>());
    }

    /**
     * 해당 데이터를 업데이트한다.
     * @param node
     */
    public void update(T node) {

        remove(node);
        put(node);

    }

    /**
     * 원소 제거
     *
     * @param node
     */
    public void remove(T node){
        distanceMap.remove(node);

        // 사용하지 않는 레퍼런스는 제거해야 gc가 된다.
        for (T key : distanceMap.keySet()) {
            distanceMap.get(key).remove(node);
        }

    }

}
