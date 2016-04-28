package org.sullivan;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Description 데이터가 저장되는 *.knowledge 파일을 파싱하는 클래스
 */
public class SLKnowledgeLoader {

    private static final String CLUSTER_DELIMITER = "|";
    private static final String DESCRIPTION_DELIMITER = "#";
    private static final String METADATA_DELIMITER = "/";

    public SLKnowledgeLoader() {
    }

    /**
     * 파일로부터 지식의 리스트를 뽑아온다.
     *
     * @param source
     * @return
     */
    public List<SLKnowledge> load(File source) {

        List<SLKnowledge> knowledgeList = new ArrayList<>();

        // 파일에서 텍스트를 추출한다.
        String data = readText(source);
        if (data == null) return knowledgeList;

        // 클러스터 분리
        String[] clusters = data.split(CLUSTER_DELIMITER);

        for (String cluster : clusters) {

            cluster = cluster.trim();
            if (cluster.length() < 1) continue;

            // 설명 분리
            String[] descriptions = cluster.split(DESCRIPTION_DELIMITER);

            // 중심 노드의 해시
            String centroidHash = descriptions[0].trim();
            if (centroidHash.length() < 1) continue;

            SLKnowledge knowledge = new SLKnowledge(centroidHash);

            for (int i = 1; i < descriptions.length; i++) {

                descriptions[i] = descriptions[i].trim();
                if (descriptions[i].length() < 1) continue;

                String[] metadata = descriptions[i].split(METADATA_DELIMITER);

                // 설명만 있는 경우
                if (metadata.length == 1)
                    knowledge.addDescription(new SLDescription(metadata[0].trim()));

                    // 메타데이터도 함께 있을 경우
                else
                    // TODO: 메타데이터 추가하는 기능 만들어야 함!
                    knowledge.addDescription(new SLDescription(metadata[0].trim(), new SLDescription.SLDescriptionInfo(metadata[1].trim())));

                // 리스트에 추가
                knowledgeList.add(knowledge);
            }
        }

        return knowledgeList;
    }

    /**
     * 파일로부터 텍스트 데이터를 읽어온다.
     *
     * @param source
     * @return
     */
    private String readText(File source) {

        try (BufferedReader reader = new BufferedReader(new FileReader(source))) {

            StringBuilder builder = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {

                builder.append(line);
                builder.append(System.lineSeparator());

                line = reader.readLine();
            }

            return builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
