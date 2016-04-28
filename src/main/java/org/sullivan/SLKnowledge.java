package org.sullivan;

import java.util.ArrayList;
import java.util.List;

/**
 * 한 클러스터에 관한 '인간적' 데이터
 */
public class SLKnowledge {

    /**
     * 타겟 노드(centroid)의 해시
     */
    public String targetNodeUid;

    /**
     * 타겟 노드
     */
    public SLNode targetNode;

    /**
     * 설명 모음
     */
    public List<SLDescription> descriptions;

    public SLKnowledge() {
        this.descriptions = new ArrayList<>();
    }

    public SLKnowledge(String targetNodeUid) {
        this.targetNodeUid = targetNodeUid;
        this.descriptions = new ArrayList<>();
    }

    /**
     * 지식에 한 줄의 설명을 추가한다.
     *
     * @param description
     */
    public void addDescription(SLDescription description) {
        this.descriptions.add(description);
    }

    /**
     * 지식에서 설명을 제거한다.
     *
     * @param description
     */
    public void removeDescription(SLDescription description) {
        this.descriptions.remove(description);
    }

    /**
     * 두 지식을 합친다.
     *
     * @param knowledge1
     * @return
     */
    public static SLKnowledge merge(SLKnowledge knowledge1, SLKnowledge knowledge2) {

        SLKnowledge mergedKnowledge = new SLKnowledge();

        mergedKnowledge.descriptions.addAll(knowledge2.descriptions);
        mergedKnowledge.descriptions.addAll(knowledge1.descriptions);

        return mergedKnowledge;
    }
}
