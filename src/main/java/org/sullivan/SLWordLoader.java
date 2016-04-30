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
public class SLWordLoader {

    /**
     * 워드의 세 레이어
     */
    private enum Layer {
        MODEL("model"), SUCCESS("success"), FAILURE("failure");

        private String label;

        Layer(String label) {
            this.label = label;
        }
    }

    /**
     * .word 파일은 XML 형식을 갖고 있다.
     * XML 파일을 파싱하기 위한 변수들
     */
    private DocumentBuilderFactory builderFactory;
    private DocumentBuilder builder;
    private Document document;

    /**
     * 파일을 처리할 패스
     */
    private File targetPath;


    /**
     * 워드 생성 이벤트를 받는 리스너들
     */
    private List<SLWordLoaderListener> listeners;

    public SLWordLoader() {

        builderFactory = DocumentBuilderFactory.newInstance();

        listeners = new ArrayList<>();

        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void load(File wordDataFile) {
        try {

            targetPath = wordDataFile.getAbsoluteFile().getParentFile();

            // XML 파일을 파싱한다.
            document = builder.parse(wordDataFile);
            document.getDocumentElement().normalize();

            NodeList childNodes = document.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {

                Node childNode = childNodes.item(i);

                if (!childNode.getNodeName().equals("word"))
                    continue;

                SLWordEntry wordEntry = getWordEntry(childNode);

                if (wordEntry == null)
                    System.out.println("Invalid word data format.");

                generateWord(wordEntry);

            }

        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * XML 노드로부터 워드 정보를 잃어온다.
     *
     * @param wordNode
     * @return
     */
    private SLWordEntry getWordEntry(Node wordNode) {

        NodeList childNodes = wordNode.getChildNodes();

        Node nameNode = null;
        Node modelNode = null;
        Node successNode = null;
        Node failureNode = null;

        for (int i = 0; i < childNodes.getLength(); i++) {

            Node childNode = childNodes.item(i);

            switch (childNode.getNodeName()) {
                case "name":
                    nameNode = childNode;
                    break;
                case "model":
                    modelNode = childNode;
                    break;
                case "success":
                    successNode = childNode;
                    break;
                case "failure":
                    failureNode = childNode;
                    break;
            }
        }

        // 필수 요소는 없으면 안 된다.
        if (wordNode == null || nameNode == null || modelNode == null || successNode == null || failureNode == null)
            return null;

        NamedNodeMap wordAttributes = wordNode.getAttributes();

        String name = nameNode.getTextContent();
        String version = getAttributeValue(wordAttributes, "version");
        String registeredDate = getAttributeValue(wordAttributes, "registered-registeredDate");

        // 워드의 음성 데이터를 읽어온다.
        List<SLNodeEntry> modelLayerNodeEntries = getNodeEntries(modelNode);
        List<SLNodeEntry> successLayerNodeEntries = getNodeEntries(successNode);
        List<SLNodeEntry> failureLayerNodeEntries = getNodeEntries(failureNode);

        SLWordEntry wordEntry = new SLWordEntry(name, modelLayerNodeEntries, successLayerNodeEntries, failureLayerNodeEntries);
        wordEntry.version = version;
        wordEntry.registeredDate = registeredDate;

        return wordEntry;
    }

    /**
     * XML 노드로부터 내부의 노드(데이터) 정보를 불러온다.
     *
     * @param layerNode
     * @return
     */
    private List<SLNodeEntry> getNodeEntries(Node layerNode) {

        List<SLNodeEntry> nodeEntries = new ArrayList<>();

        NodeList childNodes = layerNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {

            Node childNode = childNodes.item(i);

            // 태그 이름이 형식에 맞는지 체크한다.
            if (!childNode.getNodeName().equals("data"))
                continue;

            NamedNodeMap attributes = childNode.getAttributes();

            // 값들을 읽어온다.
            Layer layer = Layer.valueOf(layerNode.getNodeName());
            String source = getAttributeValue(attributes, "source");
            String uid = getAttributeValue(attributes, "uid");
            int iuid = 0;
            String recorder = getAttributeValue(attributes, "recorder");
            String recorderSex = getAttributeValue(attributes, "recorder-sex");
            String recorderAge = getAttributeValue(attributes, "recorder-age");
            String recordedDate = getAttributeValue(attributes, "recorded-registeredDate");
            List<SLDescription> descriptions = new ArrayList<>();

            if (source.equals("unknown"))
                continue;
            if (uid.equals("unknown"))
                continue;
            if (!uid.matches("^-?\\d+$"))
                continue;
            iuid = Integer.parseInt(uid);

            NodeList descriptionNodes = childNode.getChildNodes();

            // 각 설명에 대하여
            for (int j = 0; j < descriptionNodes.getLength(); j++) {

                Node descriptionNode = descriptionNodes.item(j);
                if (!descriptionNode.getNodeName().equals("description"))
                    continue;

                NamedNodeMap descriptionAttributes = descriptionNode.getAttributes();

                // 값들을 읽어온다.
                String text = descriptionNode.getTextContent();
                String prominence = getAttributeValue(descriptionAttributes, "prominence");
                String rate = getAttributeValue(descriptionAttributes, "rate");
                String provider = getAttributeValue(descriptionAttributes, "provider");
                String registeredDate = getAttributeValue(descriptionAttributes, "registered-registeredDate");

                SLDescription.SLDescriptionInfo descriptionInfo = new SLDescription.SLDescriptionInfo();

                // 채워 넣는다.
                if (prominence.matches("^-?\\d+$"))
                    descriptionInfo.prominence = Integer.parseInt(prominence);
                if (rate.matches("^-?\\d+$"))
                    descriptionInfo.rate = Integer.parseInt(rate);
                descriptionInfo.provider = provider;
                descriptionInfo.registeredDate = registeredDate;

                SLDescription description = new SLDescription(text, descriptionInfo);

                // 리스트에 추가
                descriptions.add(description);
            }

            // 채워 넣는다.
            SLNodeEntry nodeEntry = new SLNodeEntry(source, iuid, descriptions);
            nodeEntry.recorder = recorder;
            nodeEntry.recorderAge = recorderAge;
            nodeEntry.recorderSex = recorderSex;
            nodeEntry.recordedDate = recordedDate;

            nodeEntries.add(nodeEntry);
        }

        return nodeEntries;
    }


    /**
     * 워드 정보를 바탕으로 워드를 생성한다.
     *
     * @param wordEntry
     * @return
     */
    private SLWord generateWord(SLWordEntry wordEntry) {

        // 워드 메타데이터
        SLWord.SLWordInfo wordInfo = new SLWord.SLWordInfo();
        wordInfo.version = wordEntry.version;
        wordInfo.registeredDate = wordEntry.registeredDate;

        SLWord word = new SLWord(wordEntry.name, wordInfo);

        List<SLNode> modelLayerNodes = generateNodes(wordEntry.modelLayerNodeEntries);
        List<SLNode> successLayerNodes = generateNodes(wordEntry.successLayerNodeEntries);
        List<SLNode> failureLayerNodes = generateNodes(wordEntry.failureLayerNodeEntries);

        word.layer.model.nodes = modelLayerNodes;
        word.layer.success.nodes = successLayerNodes;
        word.layer.failure.nodes = failureLayerNodes;

        // 클러스터링 수행
        word.layer.model.analyzer.initialize();
        word.layer.success.analyzer.initialize();
        word.layer.failure.analyzer.initialize();

        return word;
    }

    /**
     * 노드 정보를 바탕으로 노드를 생성한다.
     *
     * @param nodeEntries
     * @return
     */
    private List<SLNode> generateNodes(List<SLNodeEntry> nodeEntries) {

        List<SLNode> nodes = new ArrayList<>();

        for (SLNodeEntry nodeEntry : nodeEntries) {

            // --------- PCM 데이터 불러오기 --------- //

            File audioFile = new File(targetPath, nodeEntry.source);

            // 파일이 존재하지 않을 경우
            if (!audioFile.exists()) {
                System.out.println("Warning: Audio source '" + audioFile.getPath() + "' does not exist.");
                continue;
            }

            SLPcmData pcmData = null;
            boolean processed = false;

            switch (getFileExtension(audioFile)) {
                case "wav":
                    pcmData = SLPcmData.importPcm(audioFile);
                    break;
                case "spd": // 이미 전처리된 포맷
                    pcmData = SLPcmData.importWav(audioFile);
                    processed = true;
                    break;
                default:
                    // 지원하지 않는 포맷
                    break;
            }

            if (pcmData == null) {
                continue;
            }

            // --------- Node Info 생성하기 --------- //

            SLNode.SLNodeInfo nodeInfo = new SLNode.SLNodeInfo();
            nodeInfo.source = audioFile;
            nodeInfo.recorder = nodeEntry.recorder;
            nodeInfo.recorderAge = nodeEntry.recorderAge;
            nodeInfo.recorderSex = nodeEntry.recorderSex;
            nodeInfo.recordedDate = nodeEntry.recordedDate;

            // --------- 노드 생성하기 ---------
            SLNode node = new SLNode(nodeEntry.uid, nodeInfo, pcmData, processed);

            // 최고 uid 업데이트
            SLNode.maximumUid = Math.max(SLNode.maximumUid, nodeEntry.uid);

            // 리스트에 추가
            nodes.add(node);
        }

        return nodes;
    }

    /**
     * 파일의 확장명을 구한다.
     *
     * @param file
     * @return
     */
    private String getFileExtension(File file) {

        int i = file.getName().lastIndexOf('.');

        if (i > 0)
            return file.getName().substring(i + 1).toLowerCase();
        else
            return "";
    }

    /**
     * XML 어트리뷰트에서 속성을 구한다. 속성이 비었다면 'unknown'을 리턴한다.
     *
     * @param attributes
     * @param attributeName
     * @return
     */
    private String getAttributeValue(NamedNodeMap attributes, String attributeName) {

        Node attribute = attributes.getNamedItem(attributeName);

        if (attribute != null && attribute.getNodeValue().trim().length() < 1)
            return attribute.getNodeValue();
        else
            return "unknown";
    }

    /**
     * 워드를 구성하는데 필요한 정보들
     */
    private static class SLWordEntry {

        /**
         * 단어의 이름
         */
        public String name;

        /**
         * 단어 포멧의 버전
         */
        public String version;

        /**
         * 등록된 날
         */
        public String registeredDate;

        /**
         * 모델 레이어
         */
        public List<SLNodeEntry> modelLayerNodeEntries;

        /**
         * 성공 레이어
         */
        public List<SLNodeEntry> successLayerNodeEntries;

        /**
         * 실패 레이어
         */
        public List<SLNodeEntry> failureLayerNodeEntries;

        public SLWordEntry(String name, List<SLNodeEntry> modelLayerNodeEntries, List<SLNodeEntry> successLayerNodeEntries, List<SLNodeEntry> failureLayerNodeEntries) {
            this.name = name;
            this.modelLayerNodeEntries = modelLayerNodeEntries;
            this.successLayerNodeEntries = successLayerNodeEntries;
            this.failureLayerNodeEntries = failureLayerNodeEntries;
        }
    }

    /**
     * 노드를 구성하는데 필요한 정보들
     */
    private static class SLNodeEntry {

        /**
         * 이 노드의 Unique ID
         */
        public int uid;

        /**
         * 음성 데이터 소스
         */
        public String source;

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

        /**
         * 노드 Description
         */
        public List<SLDescription> descriptions;

        /**
         * 노드를 구성하는 데 필요한 필수 데이터를 입력받는다.
         *
         * @param source
         * @param uid
         * @param descriptions
         */
        public SLNodeEntry(String source, int uid, List<SLDescription> descriptions) {
            this.source = source;
            this.uid = uid;
            this.descriptions = descriptions;
        }
    }

}
