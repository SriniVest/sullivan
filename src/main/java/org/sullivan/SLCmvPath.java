/**
 *   ___      _ _ _
 * / __|_  _| | (_)_ ____ _ _ _
 * \__ \ || | | | \ V / _` | ' \
 * |___/\_,_|_|_|_|\_/\__,_|_||_|
 *
 * Copyright 2016 Sullivan Project
 * https://github.com/agemor/sullivan
 *
 * This file is distributed under
 * GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 * for more details, See README.md
 *
 * Sullivan is developed by HyunJun Kim (me@hyunjun.org)
 */

package org.sullivan;

import java.util.LinkedList;
import java.util.List;

/**
 * 클러스터 백트레킹에 사용되는 거리 모델을 위한 경로 표현
 *
 * CMV: Compensated local maximum value
 *
 */
public class SLCmvPath<T extends SLMeasurable<T>> {

    public static float correctionCoefficient = 1;

    /**
     * 경로가 저장되는 스택
     */
    public List<T> steps;

    public SLCmvPath() {
        steps = new LinkedList<>();
    }

    /**
     * 경로 스택의 맨 뒤에 경로를 추가한다.
     *
     * @param step
     * @return
     */
    public SLCmvPath<T> addStep(T step) {
        this.steps.add(step);
        return this;
    }

    /**
     * 경로 스택의 맨 앞에 경로를 추가한다.
     * @param step
     * @return
     */
    public SLCmvPath<T> addStepToFront(T step) {
        this.steps.add(0, step);
        return this;
    }

    /**
     * 경로 스택에서 특정 경로를 제거한다.
     *
     * @param step
     * @return
     */
    public SLCmvPath<T> removeStep(T step) {
        this.steps.remove(step);
        return this;
    }

    /**
     * 경로 스택에서 특정 인덱스에 위치한 경로를 제거한다.
     *
     * @param index
     * @return
     */
    public SLCmvPath<T> removeStepAt(int index) {
        this.steps.remove(index);
        return this;
    }

    /**
     * 경로의 코스트를 계산한다.
     *
     * 코스트는 기하적 거리의 합이 아닌, Compensated-maximum-value cost를 사용한다.
     * @return
     */
    public double getCost() {

        // 만약 경로가 하나라면 코스트를 구할 수 없다.
        if (steps.size()  < 2) {
            System.out.println("Unable to get cost of single-step path.");
            return -1;
        }

        double totalCost = 0;

        /**
         * 경로 코스트 산출 공식
         *
         * COST = P * SQRT(N) * MAX(S1, S2, S3, ..., SN);
         */
        for (int i = 0; i < steps.size() - 1; i++)
            totalCost = Math.max(totalCost, steps.get(i).getDistance(steps.get(i + 1)));

        return correctionCoefficient * Math.sqrt(steps.size()) * totalCost;
    }
}
