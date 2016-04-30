package org.sullivan;

import java.util.*;

/**
 * 계산 코스트가 높은 거리 데이터를 캐시해주는 클래스
 */
public class SLDistanceMap<T extends SLMeasurable<T>> {

    protected Map<T, Map<T, Double>> distanceMap;

    public SLDistanceMap() {
        distanceMap = new HashMap<>();
    }

    /**
     * 거리 데이터 불러오기
     *
     * @param element1
     * @param element2
     * @return
     */
    public double getDistance(T element1, T element2) {

        if (element1 == null || element2 == null)
            return Double.POSITIVE_INFINITY;

        if (!distanceMap.containsKey(element1))
            add(element1);

        Map<T, Double> map = distanceMap.get(element1);

        // 아직 거리 계산이 되지 않았을 경우
        if (!map.containsKey(element2)) {

            double distance = element1.getDistance(element2);

            if (!distanceMap.containsKey(element2))
                add(element2);

            // 데이터 대칭
            map.put(element2, distance);
            distanceMap.get(element2).put(element1, distance);
        }
        return map.get(element2).doubleValue();
    }

    public List<T> getList() {
        return new ArrayList<>(distanceMap.keySet());
    }

    /**
     * 최소 쌍방-인접 원소 찾기
     *
     * @param element
     * @return
     */
    public T getAdjacentElement(T element) {

        T closestElement = getClosestElement(element);

        if (closestElement != null) {
            if (getClosestElement(closestElement) == element)
                return closestElement;
        }
        return null;
    }

    /**
     * 가장 근접한 노드를 리턴한다.
     *
     * @param targetElement
     * @return
     */
    public T getClosestElement(T targetElement) {

        double minimum = Double.POSITIVE_INFINITY;
        T closestElement = null;

        for (T element : getList()) {
            double distance = getDistance(targetElement, element);
            if (distance < minimum) {
                minimum = distance;
                closestElement = element;
            }
        }
        return closestElement;
    }

    /**
     * 거리 분포가 Gaussian이라고 가정하고 상위 n퍼센트 안에 드는 모든 원소를 크기순으로 리턴한다.
     * 리턴되는 값의 최소 길이는 2로 정한다.
     *
     * @param targetElement
     * @return
     */
    public List<T> getCloseElements(T targetElement, float ratio) {

        // 크기순 정렬
        List<T> closeElements = new LinkedList<>();

        // insertion sort. 클러스터의 개수가 그리 많지 않을 것으로 예상.
        for (T element : getList()) {

            for (int i = 0; i < closeElements.size(); i++) {

                T sortedElement = closeElements.get(i);

                if (getDistance(sortedElement, element) > getDistance(targetElement, element)) {
                    closeElements.add(i, element);
                    break;
                }
            }

            if (closeElements.size() < 1)
                closeElements.add(element);
        }
        int elementNumber = (int) Math.max(Math.floor(closeElements.size() * ratio), Math.min(2, closeElements.size()));


        return closeElements.subList(0, elementNumber);
    }

    /**
     * 랜덤한 원소를 리턴한다.
     *
     * @return
     */
    public T getRandomElement(List<T> except) {

        Random random = new Random();
        List<T> keys = new ArrayList<T>(distanceMap.keySet());

        if (except != null)
            keys.removeAll(except);

        return keys.get(random.nextInt(keys.size()));
    }

    /**
     * DistanceMap 내의 모든 원소를 제거한다.
     */
    public void clear() {
        distanceMap.clear();
    }

    /**
     * 특정 원소를 갖고 있는지 확인한다.
     *
     * @param element
     * @return
     */
    public boolean hasElement(T element) {
        return this.distanceMap.containsKey(element);
    }

    public int size() {
        return distanceMap.size();
    }

    /**
     * 원소 추가
     *
     * @param element
     */
    public void add(T element) {
        distanceMap.put(element, new HashMap<>());
    }

    /**
     * 해당 원소를 업데이트한다.
     *
     * @param element
     */
    public void update(T element) {

        remove(element);
        add(element);
    }

    /**
     * 원소 제거
     *
     * @param element
     */
    public void remove(T element) {
        distanceMap.remove(element);

        // 사용하지 않는 레퍼런스는 제거해야 gc가 된다.
        for (T key : distanceMap.keySet()) {
            distanceMap.get(key).remove(element);
        }

    }
}
