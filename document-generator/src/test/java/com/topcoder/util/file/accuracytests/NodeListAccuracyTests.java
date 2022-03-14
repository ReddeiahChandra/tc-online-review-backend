/**
 * Copyright ?2004, TopCoder, Inc. All rights reserved
 */

package com.topcoder.util.file.accuracytests;

import com.topcoder.util.file.fieldconfig.Field;
import com.topcoder.util.file.fieldconfig.NodeList;
import com.topcoder.util.file.fieldconfig.Node;

import junit.framework.TestCase;

/**
 * <p>Test the NodeList class</p>
 *
 * @author TCSDEVELOPER
 * @version 1.0
 */

public class NodeListAccuracyTests extends TestCase {
    /**
     * NodeList instance for test.
     */
    NodeList list = null;

    /**
     * Tests constructor and getNodes.
     */
    public void testNodeList() {
        Node[] nodes = new Node[2];
            
        nodes[0] = new Field("testName", "testVal", "testDesc", true);
        nodes[1] = new Field("testName2", "testVal2", "testDesc2", false);
        list = new NodeList(nodes);

        nodes = list.getNodes();
        assertEquals(2, nodes.length);
        assertEquals("testName", ((Field) nodes[0]).getName());
        assertEquals("testName2", ((Field) nodes[1]).getName());
    }
}
