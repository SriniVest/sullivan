package org.sullivan;

/**
 * 시스템이 닫히기 전에 실행되는 명령들을 모아 놓은 클래스
 */
public class SLShutdownHook extends Thread {

    public SLShutdownHook(SLMain main) {

        // 모든 워드를 export한다.
        for (String wordKey : main.words.keySet()) {
            main.wordExporter.export(main.words.get(wordKey));
        }

    }

    public void run() {

    }
}
