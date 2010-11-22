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

import static org.neo4j.graphdb.Direction.INCOMING;
import junit.framework.Assert;

import org.goddag4j.GoddagEdge.EdgeType;
import org.junit.Test;
import org.neo4j.helpers.collection.IteratorUtil;

public class TextTest extends GraphDatabaseTestContext {

    @Test
    public void splitContent() {
        Element[] roots = new Element[10];
        for (int i = 0; i < roots.length; i++) {
            roots[i] = new Element(db, "tei", "p");
            root.insert(root, roots[i], null);
        }
        Text content = new Text(db, "0123456789");
        for (Element root : roots) {
            root.insert(root, content, null);
        }
        Assert.assertEquals(roots.length, IteratorUtil.count(content.node.getRelationships(EdgeType.CONTAINS, INCOMING).iterator()));

        content.split(new Integer[] { 4 });
        for (Element root : roots) {
            Assert.assertEquals(2, IteratorUtil.count(root.getChildren(root).iterator()));
        }
    }
}
