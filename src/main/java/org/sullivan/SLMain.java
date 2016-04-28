package org.sullivan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sullivan 시스템의 메인
 */
public class SLMain implements SLCliListener, SLWordLoaderListener, SLNodeGeneratorListener {

    public static final String ENGINE_VERSION = "2.1.3";

    /**
     * 시스템의 Command-line interface
     */
    public SLCli cli;

    /**
     * 학습된 단어들
     */
    public Map<String, SLWord> words;

    /**
     * Word 로더
     */
    public SLWordLoader wordLoader;

    /**
     * Word 익스포터
     */
    public SLWordExporter wordExporter;

    /**
     * Node 생성기
     */
    public SLNodeGenerator nodeGenerator;
    private SLWord requestWord;

    /**
     * 시스템 상태 변수
     */
    private boolean initialized = false;

    public SLMain() {

        cli = new SLCli();

        words = new HashMap<>();
        wordLoader = new SLWordLoader();
        wordExporter = new SLWordExporter();

        nodeGenerator = new SLNodeGenerator(5000, 1);

        cli.addEventListener(this);
        cli.initialize();
    }

    /**
     * 사용자의 명령을 입력받았을 때 호출된다.
     *
     * @param command
     * @param arguments
     * @return
     */

    public boolean onCLICommand(String command, List<String> arguments) {

        String wordName = "";

        switch (command) {
            case "load-all":
                if (!initialized)
                    initialized = loadAllWordsFromBatch();
                else
                    cli.error("System is already initialized.");
                break;
            case "save-all":
                for (String wordKey : words.keySet()) {
                    wordExporter.export(words.get(wordKey));
                    cli.notify("Word is saved to [" + wordKey + ".word]");
                }
                break;
            case "load":
                wordLoader.load(new File(arguments.get(0)));
                break;
            case "save":
                wordName = arguments.get(0).trim();
                if (!words.containsKey(wordName))
                    cli.error("No such word \'" + wordName + "\'.");
                wordExporter.export(words.get(wordName));
                cli.notify("Word is saved to [" + wordName + ".word]");
                break;
            case "evaluate":
                wordName = arguments.get(0).trim();
                if (!words.containsKey(wordName))
                    cli.error("No such word \'" + wordName + "\'.");
                requestWord = words.get(wordName);
                generateNode(new File(arguments.get(1).trim()));
                break;
            case "status":
                wordName = arguments.get(0).trim();
                // 단어의 노드와 클러스터의 개수 등등을 보여줌
                cli.notify(words.get(wordName).getStatus());
                break;
            case "resolve":
                if (SLDescriptionRequest.numberOfRequests < 1) {
                    cli.notify("There are no requests to resolve.");
                } else {
                    cli.notify("There are " + SLDescriptionRequest.numberOfRequests + " requests to resolve.");
                    SLDescriptionRequest request = SLDescriptionRequest.resolve();
                    request.play(3);
                    String response = cli.getResponse();
                    cli.start();
                    request.answer(response);
                }
                break;
            case "exit":
                System.exit(0);
                break;
            default:
        }

        return true;
    }

    /**
     * 워드 배치 파일 (words.sullivan)에 입력되어 있는 단어들을 전부 로드한다.
     *
     * @return
     */
    private boolean loadAllWordsFromBatch() {

        try (BufferedReader reader = new BufferedReader(new FileReader("words.sullivan"))) {
            for (String line; (line = reader.readLine()) != null; ) {

                // # 문자는 주석으로 처리한다.
                line = line.split("#")[0].trim();

                // 빈 공간은 처리하지 않는다.
                if (line.length() < 1) continue;

                // 워드 로드
                wordLoader.load(new File("./data/" + line + ".word"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        cli.notify("Initialization successful");
        return true;
    }

    /**
     * 입력된 파일 소스를 바탕으로 노드를 생성한다.
     *
     * @param source
     */
    public void generateNode(File source) {
        SLNode.SLNodeInfo nodeInfo = new SLNode.SLNodeInfo(source.getPath());
        nodeInfo.uid = (++SLNode.maximumUid) + "";

        SLPcmData pcmData = SLPcmData.importWav(source);
        nodeGenerator.insert(pcmData, nodeInfo);
    }

    /**
     * 워드가 생성되었을 때 호출된다.
     *
     * @param word
     */
    public void onWordGenerated(SLWord word) {
        words.put(word.info.name, word);
        cli.notify("Word '" + word.info.name + "' loaded.");
    }

    /**
     * 노드가 생성되었을 때 호출된다.
     *
     * @param node
     */
    public void onNodeGenerated(SLNode node) {
        if (requestWord != null) {
            SLEvaluationReport report = requestWord.evaluate(node);
            cli.notify(report.getResult());
        }
    }

    public static void main(String[] args) {

        SLMain entry = new SLMain();

        // 데이터 손실을 막기 위해 shutdown hook을 추가한다.
        Runtime.getRuntime().addShutdownHook(new Thread(new SLShutdownHook(entry)));
        entry.words.get("hello");
    }
}
