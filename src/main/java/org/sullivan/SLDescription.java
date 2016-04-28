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
        this.info = new SLDescriptionInfo("");
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
         * 추가된 날짜
         */
        public String date;

        /**
         * 추가한 자의 이름
         */
        public String name;

        /**
         * 평점(이 설명의 유용성)
         */
        public int rate;

        public String raw;

        public SLDescriptionInfo(String raw) {
            this.raw = raw;
        }
    }

}
