package org.sullivan;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.Timestamp;
import java.util.List;

/**
 * 메모리상의 word 데이터를 디스크 상의 word 데이터로 변환한다.
 */
public class SLWordExporter {

    private DocumentBuilderFactory builderFactory;
    private DocumentBuilder builder;
    private Document document;

    private TransformerFactory transformerFactory;

    public SLWordExporter() {

        builderFactory = DocumentBuilderFactory.newInstance();
        transformerFactory = TransformerFactory.newInstance();

        try {
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * word 데이터를 export한다.
     *
     * @param word
     */
    public void export(SLWord word) {

        // 새 document를 만든다.
        document = builder.newDocument();

        // word 노드
        Element wordElement = document.createElement("word");
        wordElement.setAttribute("version", SLMain.ENGINE_VERSION);
        wordElement.setAttribute("registeredDate", new Timestamp(System.currentTimeMillis()).toString());
        document.appendChild(wordElement);

        // name 노드
        Element nameElement = document.createElement("name");
        nameElement.setNodeValue(word.name);
        wordElement.appendChild(nameElement);

        // layer 노드
        Element modelElement = generateLayerElement("model", word.layer.model.nodes);
        Element successElement = generateLayerElement("success", word.layer.success.nodes);
        Element failureElement = generateLayerElement("failure", word.layer.failure.nodes);

        wordElement.appendChild(modelElement);
        wordElement.appendChild(successElement);
        wordElement.appendChild(failureElement);

        // 파일 저장
        try {
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File("./data/" + word.name + ".word"));

            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 레이어 하나의 element를 생성한다.
     *
     * @param layer
     * @param nodes
     * @return
     */
    private Element generateLayerElement(String layer, List<SLNode> nodes) {

        Element layerElement = document.createElement(layer);

        for (SLNode node : nodes) {

            Element dataElement = document.createElement("data");

            dataElement.setAttribute("uid", node.uid);
            dataElement.setAttribute("source", node.info.source.getPath());
            dataElement.setAttribute("name", node.info.recorder);
            dataElement.setAttribute("sex", node.info.recorderSex);
            dataElement.setAttribute("age", node.info.recorderAge);
            dataElement.setAttribute("registeredDate", node.info.recordedDate);

            // description
            for (SLDescription description : node.descriptions) {
                Element descriptionElement = document.createElement("description");

                descriptionElement.setAttribute("prominence", String.valueOf(description.info.prominence));
                descriptionElement.setAttribute("rate", String.valueOf(description.info.rate));
                descriptionElement.setAttribute("provider", String.valueOf(description.info.provider));
                descriptionElement.setAttribute("registeredDate", String.valueOf(description.info.registeredDate));
                descriptionElement.setNodeValue(description.description);

                dataElement.appendChild(descriptionElement);
            }
            layerElement.appendChild(dataElement);

        }
        return layerElement;
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

}
