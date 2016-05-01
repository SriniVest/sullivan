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
