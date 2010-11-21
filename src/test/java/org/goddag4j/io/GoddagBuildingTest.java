package org.goddag4j.io;

import static org.goddag4j.io.NamespaceMap.TEI_MAP;

import java.net.URL;
import java.util.Collections;

import org.goddag4j.Element;
import org.goddag4j.GraphDbBasedTest;
import org.goddag4j.io.GoddagIOUtil;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;

public class GoddagBuildingTest extends GraphDbBasedTest {
    private static final URL TEST_XML_RESOURCE = GoddagBuildingTest.class.getResource("/george-algabal-tei.xml");

    @Test
    public void build() throws Exception {
        Element root = GoddagIOUtil.parse(new InputSource(TEST_XML_RESOURCE.toString()), TEI_MAP, db);
        Assert.assertNotNull(root);
        Assert.assertTrue(root.getDescendants(root).iterator().hasNext());

        GoddagIOUtil.dump(Collections.singleton(root), System.out);
    }
}
