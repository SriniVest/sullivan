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
