package org.manatee;

/**
 * Created by HyunJun on 2016-04-13.
 */
public interface NodeFactoryEvent {

    void onGenerated(int nid, Node node);
}
