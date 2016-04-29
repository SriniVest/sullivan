package org.sullivan;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 워드 데이터 파일(*.word)로부터 워드(한 단어에 관한 총괄 데이터)를 생성한다.
 */
public class SLWordLoader implements SLNodeGeneratorListener {

    /**
     * .word 파일은 XML 형식을 갖고 있다.
     * XML 파일을 파싱하기 위한 변수들
     */
    private DocumentBuilderFactory builderFactory;
    private DocumentBuilder builder;
    private Document document;

    /**
     * 워드의 메타데이터
     */
    private SLWord.SLWordInfo wordInfo;

    /**
     * 노드 생성기
     */
    private SLNodeGenerator nodeGenerator;

    /**
     * 생성되어야 할 총 노드 수
     */
    private int totalGeneratingNodes = 0;

    /**
     * 현재 생성된 노드 수
     */
    private int currentGeneratedNodes = 0;

    /**
     * 생성된 노드들
     */
    private List<SLNode> generatedNodes;

    /**
     * 워드 생성 이벤트를 받는 리스너들
     */
    private List<SLWordLoaderListener> listeners;

    public SLWordLoader() {

        nodeGenerator = new SLNodeGenerator(5000, 1);
        builderFactory = DocumentBuilderFactory.newInstance();

        nodeGenerator.addEventListener(this);

        listeners = new ArrayList<>();

        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void load(File wordDataFile) {
        try {
            generatedNodes = new ArrayList<>();

            List<SLNode.SLNodeInfo> wordModelData;
            List<SLNode.SLNodeInfo> wordSuccessData;
            List<SLNode.SLNodeInfo> wordFailureData;

            document = builder.parse(wordDataFile);
            document.getDocumentElement().normalize();

            // 워드의 메타데이터를 읽어온다.
            Node word = document.getElementsByTagName("word").item(0);
            Node nameNode = document.getElementsByTagName("name").item(0);

            wordInfo = new SLWord.SLWordInfo(nameNode.getTextContent());
            wordInfo.version = word.getAttributes().getNamedItem("version").getNodeValue();
            wordInfo.date = word.getAttributes().getNamedItem("date").getNodeValue();

            // 워드의 음성 데이터를 읽어온다.
            wordModelData = getNodesInLayer("model");
            wordSuccessData = getNodesInLayer("success");
            wordFailureData = getNodesInLayer("failure");

            totalGeneratingNodes = wordModelData.size() + wordSuccessData.size() + wordFailureData.size();

            // 로드된 정보를 바탕으로 워드 내부의 노드를 생성한다.
            generateNodes(wordModelData);
            generateNodes(wordSuccessData);
            generateNodes(wordFailureData);

        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 레이어로부터 데이터 파일의 목록을 구한다.
     *
     * @param layer
     * @return
     */
    private List<SLNode.SLNodeInfo> getNodesInLayer(String layer) {

        List<SLNode.SLNodeInfo> nodes = new ArrayList<>();

        // 레이어 노드
        Node layerNode = document.getElementsByTagName(layer).item(0);

        // 자식 노드
        NodeList dataNodes = layerNode.getChildNodes();

        for (int i = 0; i < dataNodes.getLength(); i++) {

            // 데이터 노드
            Node dataNode = dataNodes.item(i);

            // <data> 만 분석
            if (!dataNode.getNodeName().equals("data"))
                continue;

            // 데이터 노드의 속성
            NamedNodeMap attributes = dataNode.getAttributes();

            // 데이터 음성 소스
            String source = attributes.getNamedItem("source").getNodeValue();

            // 노드 메타데이터
            SLNode.SLNodeInfo nodeInfo = new SLNode.SLNodeInfo(source);

            nodeInfo.layer = layer;
            nodeInfo.uid = attributes.getNamedItem("uid").getNodeValue();

            if (nodeInfo.uid == null || nodeInfo.uid.equals(""))
                System.out.println("Node uid must not be null. (" + source + ")");

            nodeInfo.recorder = attributes.getNamedItem("name").getNodeValue();
            nodeInfo.recorderSex = attributes.getNamedItem("sex").getNodeValue() == "male" ? true : false;
            nodeInfo.recorderAge = Integer.parseInt(attributes.getNamedItem("age").getNodeValue());
            nodeInfo.recordedDate = attributes.getNamedItem("date").getNodeValue();

            NodeList descriptionNodes = dataNode.getChildNodes();

            for (int j = 0; j < descriptionNodes.getLength(); j++) {
                Node descriptionNode = descriptionNodes.item(j);

                // description 노드에 대해서만 검사
                if (!descriptionNode.getNodeName().equals("description"))
                    continue;

                NamedNodeMap descriptionAttributes = descriptionNode.getAttributes();
                String descriptionText = descriptionNode.getNodeValue();
                String name = descriptionAttributes.getNamedItem("name").getNodeValue();

                SLDescription description = new SLDescription(descriptionText, new SLDescription.SLDescriptionInfo(name));
                nodeInfo.descriptions.add(description);
            }

            nodes.add(nodeInfo);
        }

        return nodes;
    }

    /**
     * 노드 정보를 바탕으로 노드를 생성한다.
     *
     * @param nodeInfos
     * @return
     */
    private void generateNodes(List<SLNode.SLNodeInfo> nodeInfos) {

        for (SLNode.SLNodeInfo nodeInfo : nodeInfos) {

            File audioFile = new File("./data/" + nodeInfo.source);

            // 파일이 존재하지 않을 경우
            if (!audioFile.exists()) {
                System.out.println("Warning: Audio source '" + nodeInfo.source + "' does not exists.");
                totalGeneratingNodes--;
                continue;
            }

            // 파일 확장자 구하기
            String extension = "";
            int i = audioFile.getName().lastIndexOf('.');
            if (i > 0) {
                extension = audioFile.getName().substring(i + 1).toLowerCase();
            }

            SLPcmData pcmData = null;

            // 오디오 데이터를 PCM 형식으로 변환한다.
            if (extension.equals("pronunciation")) {
                pcmData = SLPcmData.importPcm(audioFile);
            } else if (extension.equals("wav")) {
                pcmData = SLPcmData.importWav(audioFile);
            }

            nodeGenerator.insert(pcmData, nodeInfo);
        }
    }

    /**
     * 노드가 생성되었을때
     *
     * @param node
     */
    public void onNodeGenerated(SLNode node) {

        currentGeneratedNodes++;

        if (node != null)
            generatedNodes.add(node);

        // Uid maximum을 업데이트
        int parsedUid = Integer.parseInt(node.info.uid);
        SLNode.maximumUid = Math.max(SLNode.maximumUid, parsedUid);

        // 연관된 knowledge가 있다면 링크시킨다.
        //linkKnowledge(node);

        // 만약 모든 노드가 다 생성되었다면
        if (currentGeneratedNodes >= totalGeneratingNodes) {
            generateWord();
        }
    }

    /**
     * 생성된 노드들을 조합하여 워드 데이터를 만든다.
     */
    private void generateWord() {

        // 워드 생성
        SLWord word = new SLWord(wordInfo);

        // 생성된 노드들을 워드에 종류별로 추가
        for (SLNode node : generatedNodes) {
            switch (node.info.layer) {
                case "model":
                    word.nodeLayer.model.add(node);
                    break;
                case "success":
                    word.nodeLayer.success.add(node);
                    break;
                case "failure":
                    word.nodeLayer.failure.add(node);
                    break;
                default:
            }
        }

        // 클러스터링한다.
        word.clusterLayer.model.analyzer.initialize();
        word.clusterLayer.success.analyzer.initialize();
        word.clusterLayer.failure.analyzer.initialize();

        // knowledge와 링크시킨다.
       /* allocateKnowledge(word.clusterLayer.model.clusters, "model");
        allocateKnowledge(word.clusterLayer.success.clusters, "model");
        allocateKnowledge(word.clusterLayer.failure.clusters, "model");*/

        // 이벤트를 발생시킨다.
        dispatchEvent(word);
    }
/*

    private void linkKnowledge(SLNode node) {
        for (SLKnowledge knowledge : knowledgeList) {
            if (knowledge.targetNodeUid.equals(node.info.uid)) {
                knowledge.targetNode = node;
            }
        }
    }


    private void allocateKnowledge(SLDistanceMap<SLCluster> clusters, String layer) {


        for (SLCluster cluster : clusters.getList()) {

            boolean found = false;

            for (SLKnowledge knowledge : knowledgeList) {
                if (knowledge.targetNode == cluster.getCentroid()) {
                    cluster.knowledge = knowledge;

                    // 중복 할당 방지
                    knowledge.targetNode = null;
                    found = true;
                }
            }

            if (found) continue;

            double minimumDistance = Double.POSITIVE_INFINITY;
            SLKnowledge closestKnowledge = null;

            // centroid에 해당하는 knowledge가 없으면 가장 가까운 노드를 찾는다.
            for (SLKnowledge knowledge : knowledgeList) {

                // 동일 레이어가 아니면 패스
                if (knowledge.targetNode == null || !knowledge.targetNode.info.layer.equals(layer))
                    continue;

                // 가장 가까운 knowledge holder를 찾는다.
                double distance = cluster.getContext().nodes.getDistance(knowledge.targetNode, cluster.getCentroid());

                if (minimumDistance > distance) {
                    minimumDistance = distance;
                    closestKnowledge = knowledge;
                }
            }

            // threshold를 만족하면
            if (minimumDistance < SLCluster.DISTANCE_THRESHOLD) {
                cluster.knowledge = closestKnowledge;
            }

        }
    }

    */

    /**
     * 각 리스너들에게 워드가 생성되었다고 알린다.
     *
     * @param word
     */
    private void dispatchEvent(SLWord word) {
        for (SLWordLoaderListener listener : listeners) {
            listener.onWordGenerated(word);
        }
    }

    /**
     * 이벤트 리스너를 추가한다.
     *
     * @param listener
     */
    public void addEventListener(SLWordLoaderListener listener) {
        this.listeners.add(listener);
    }

    /**
     * 이벤트 리스너를 제거한다.
     *
     * @param listener
     */
    public void removeEventListener(SLWordLoaderListener listener) {
        this.listeners.remove(listener);
    }


}
