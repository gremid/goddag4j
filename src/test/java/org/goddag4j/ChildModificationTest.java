package org.goddag4j;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.Text;
import org.junit.Test;
import org.neo4j.helpers.collection.IteratorUtil;

public class ChildModificationTest extends GoddagTestBase {
    @Test
    public void addAndRemove() {
        for (int i = 0; i < 10; i++) {
            root.insert(root, Element.create(db, "tei", "p"), null);
        }
        Assert.assertEquals(10, IteratorUtil.count(root.getChildren(root).iterator()));


        List<GoddagNode> elements = new ArrayList<GoddagNode>();
        IteratorUtil.addToCollection(root.getChildren(root).iterator(), elements);
        Assert.assertEquals(10, elements.size());
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                root.remove(root, elements.get(i));
            }
        }
        Assert.assertEquals(5, IteratorUtil.count(root.getChildren(root).iterator()));
    }

    @Test
    public void insertBefore() {
        GoddagNode last = null;
        for (int i = 0; i < 10; i++) {
            Text text = Text.create(db, Integer.toString(i));
            root.insert(root, text, last);
            last = text;
        }
        
        int i = 0;
        for (GoddagNode text : root.getChildren(root)) {
            Assert.assertEquals(Integer.toString(9 - (i++)), text.getText(root));
        }
    }

    @Test
    public void insertAndDelete() {
        GoddagNode last = null;
        for (int i = 0; i < 10; i++) {
            Text text = Text.create(db, Integer.toString(i));
            root.insert(root, text, last);
            last = text;
        }
        List<GoddagNode> nodes = new ArrayList<GoddagNode>();
        IteratorUtil.addToCollection(root.getChildren(root).iterator(), nodes);
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                nodes.get(i).delete(root);
            }
        }
        Assert.assertEquals(5, IteratorUtil.count(root.getChildren(root).iterator()));
    }
}
