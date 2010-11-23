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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

public class Text extends GoddagTreeNode {

    private static final String PREFIX = GoddagTreeNode.PREFIX + ".text";

    public Text(Node node) {
        super(node);
    }

    public Text(GraphDatabaseService db, String content) {
        this(db.createNode());
        setNodeType(NodeType.TEXT);
        setText(content);
    }

    @Override
    public String getText(Element root) {
        return getText();
    }

    public String getText() {
        return (String) node.getProperty(PREFIX);
    }

    public void setText(String content) {
        node.setProperty(PREFIX, content);
    }

    public Text[] split(int... positions) {
        final String content = getText();
        final int contentLength = content.length();

        final GraphDatabaseService db = node.getGraphDatabase();
        List<Text> segments = new ArrayList<Text>(positions.length + 1);
        int lastPos = 0;
        for (int pc = 0; pc < positions.length; pc++) {
            if (positions[pc] < 0 || positions[pc] >= contentLength) {
                throw new IllegalArgumentException(Integer.toString(positions[pc]));
            }
            if (lastPos == positions[pc]) {
                continue;
            }
            segments.add(new Text(db, content.substring(lastPos, positions[pc])));
            if ((pc + 1) == positions.length) {
                segments.add(new Text(db, content.substring(positions[pc])));
            } else {
                lastPos = positions[pc];
            }
        }

        if (segments.isEmpty()) {
            return new Text[] { this };
        }

        for (Element root : getRoots()) {
            final GoddagTreeNode parent = getParent(root);
            for (Text segment : segments) {
                parent.insert(root, segment, this);
            }
            parent.remove(root, this, false);
        }
        return segments.toArray(new Text[0]);
    }

    @Override
    public String toString() {
        return "<" + PREFIX + " '" + getText() + "' /> " + node.toString();
    }
}
