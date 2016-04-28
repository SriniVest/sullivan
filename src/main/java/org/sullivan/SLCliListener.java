package org.sullivan;

import java.util.List;

/**
 * Created by HyunJun on 2016-04-25.
 */
public interface SLCliListener {

    /**
     * CLI에 커멘드가 입력됬을 때 발생되는 이벤트
     *
     * @param command
     * @param arguments
     * @return
     */
    boolean onCLICommand(String command, List<String> arguments);

}
