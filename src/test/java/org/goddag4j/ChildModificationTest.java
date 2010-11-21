/**
 * GODDAG for Java (goddag4j):
 * Java implementation of the GODDAG data model to express document
 * structures including overlapping markup
 *
 * Copyright (C) 2010 the respective authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.goddag4j;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.Text;
import org.junit.Test;
import org.neo4j.helpers.collection.IteratorUtil;

public class ChildModificationTest extends BaseTest {
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
