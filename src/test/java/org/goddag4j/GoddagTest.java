package org.goddag4j;

import static org.goddag4j.GoddagNodeEdge.DESCENDANT_AXIS;
import static org.neo4j.graphdb.Direction.INCOMING;
import junit.framework.Assert;

import org.goddag4j.Element;
import org.goddag4j.Text;
import org.junit.Test;
import org.neo4j.helpers.collection.IteratorUtil;

public class GoddagTest extends GoddagTestBase {

    @Test
    public void splitContent() {
        Element[] roots = new Element[10];
        for (int i = 0; i < roots.length; i++) {
            roots[i] = Element.create(db, "tei", "p");
        }
        Text content = Text.create(db, "0123456789");
        for (Element root : roots) {
            root.insert(root, content, null);
        }
        Assert.assertEquals(roots.length, IteratorUtil.count(content.node.getRelationships(DESCENDANT_AXIS, INCOMING).iterator()));

        content.split(new Integer[] { 4 });
        for (Element root : roots) {
            Assert.assertEquals(2, IteratorUtil.count(root.getChildren(root).iterator()));
        }

        for (int i = 0; i < roots.length; i++) {
            roots[i].delete(root);
        }
    }
}
