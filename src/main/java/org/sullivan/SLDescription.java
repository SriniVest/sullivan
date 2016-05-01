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

/**
 * SLKnowledge에서 한 줄의 description을 의미
 */
public class SLDescription {

    /**
     * 설명 데이터
     */
    public String description;

    /**
     * description의 메타데이터
     */
    public SLDescriptionInfo info;

    public SLDescription(String description) {
        this.description = description;
        this.info = new SLDescriptionInfo();
    }

    public SLDescription(String description, SLDescriptionInfo info) {
        this.description = description;
        this.info = info;
    }

    /**
     * Description의 메타데이터를 표현하는 클래스
     */
    public static class SLDescriptionInfo {

        /**
         * 설명의 영향력
         */
        public int prominence;

        /**
         * 설명에 대한 평점
         */
        public int rate;

        /**
         * 설명을 올린 사람
         */
        public String provider;

        /**
         * 설명이 등록된 날짜
         */
        public String registeredDate;

        public SLDescriptionInfo() {
        }
    }

}
