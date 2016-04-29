package org.sullivan;

import java.util.*;

/**
 * Created by HyunJun on 2016-04-25.
 */
public class SLCli {

    /**
     * Command-line 스캔을 위한 Scanner
     */
    public Scanner scanner;

    /**
     * Command-line 입력을 처리할 이벤트 리스너
     */
    public List<SLCliListener> listeners;

    /**
     * 정의된 명령들
     */
    public List<String> commands;
    public Map<String, String> patterns;
    public Map<String, String> descriptions;

    public SLCli() {
        scanner = new Scanner(System.in);
        listeners = new ArrayList<>();

        commands = new ArrayList<>();
        patterns = new HashMap<>();
        descriptions = new HashMap<>();
    }


    public void initialize() {

        printLine("Welcome to Sullivan CLI (engine version: 2.1.3)");
        printLine("Copyright (c) 2016 Sullivan Project. All rights reserved.");
        printLine("Type [help] to view all available commands.");

        addCommand("load-all", "load-all", "Initialize pronunciation database.");
        addCommand("save-all", "save-all", "Save word model data.");
        addCommand("load", "load <word-name>", "Load and analyze word model for later evaluation.");
        addCommand("save", "save <word-name>", "Save word model data.");
        addCommand("evaluate", "evaluate <word-name> <*.wav|*.pronunciation>", "Evaluate pronunciation.");
        addCommand("status", "status <word-name>", "Show inner status of certain word.");
        addCommand("resolve", "resolve", "Answer to spontaneous system learning data requests.");
        addCommand("exit", "Exit Sullivan.");

        start();
    }

    /**
     * 에러 메시지를 표시한다.
     *
     * @param message
     */
    public void error(Object message) {
        printLine(message);
    }

    /**
     * 알림 메시지를 표시한다.
     *
     * @param message
     */
    public void notify(Object message) {
        printLine(message);
    }

    public String getResponse() {
        stop();
        String result = "";
        while (scanner.hasNext()) {
            result = scanner.next().trim();
            break;
        }

        return result;
    }

    /**
     * CLI에 새 명령을 추가한다.
     *
     * @param command
     * @param pattern
     * @param description
     */
    public void addCommand(String command, String pattern, String description) {
        commands.add(command);
        patterns.put(command, pattern);
        descriptions.put(command, description);
    }

    public void addCommand(String command, String description) {
        addCommand(command, command, description);
    }

    /**
     * CLI 이벤트를 받을 리스너를 추가한다.
     *
     * @param listener
     */
    public void addEventListener(SLCliListener listener) {
        this.listeners.add(listener);
    }

    /**
     * 이벤트 리스너 목록에서 제거한다.
     *
     * @param listener
     */
    public void removeEventListener(SLCliListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * 스케너 루프를 실행한다.
     */
    public void start() {

        termiate = false;

        while (scanner.hasNextLine() && !termiate) {
            String input = scanner.nextLine().trim();

            if (input.equals("help")) {
                help();
                continue;
            }

            String header = input.split(" ")[0];
            boolean unknown = true;

            for (String command : commands) {
                if (header.equals(command)) {
                    unknown = false;
                    List<String> arguments = parseArguments(input, patterns.get(command));

                    if (arguments == null) {
                        printLine("Invalid argument. (Spaces are not allowed in argument.)");
                        break;
                    }

                    dispatchEvent(command, arguments);
                    break;
                }
            }

            if (unknown) {
                printLine("'" + input + "' is undefined command.");
            }
        }
    }

    /**
     * 메인 루프를 정지한다.
     */
    private boolean termiate = false;
    private void stop() {
        termiate = true;
    }


    /**
     * 명령어에서 <>로 둘러싸인 변수 부분을 캐치한다.
     *
     * @param command
     * @return
     */
    private List<String> parseArguments(String command, String pattern) {

        List<String> arguments = new ArrayList<>();

        String[] commandChunks = command.split(" ");
        String[] patternChunks = pattern.split(" ");

        if (commandChunks.length != patternChunks.length)
            return null;

        for (int i = 0; i < commandChunks.length; i++) {
            if (patternChunks[i].charAt(0) == '<')
                arguments.add(commandChunks[i]);
        }

        return arguments;
    }

    /**
     * 정의된 모든 명령을 보여준다.
     */
    private void help() {
        for (String command : commands) {
            printLine("" + patterns.get(command) + ": " + descriptions.get(command));
        }
    }

    /**
     * 이벤트를 발생시킨다.
     *
     * @param command
     * @param arguments
     */
    private void dispatchEvent(String command, List<String> arguments) {
        for (SLCliListener listener : listeners) {
            boolean propagation = listener.onCLICommand(command, arguments);
            if (!propagation) break;
        }
    }

    private void printLine(Object o) {
        System.out.println(o);
    }
}
