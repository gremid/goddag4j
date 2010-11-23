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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

public class ProcessingInstruction extends GoddagTreeNode {

    public static final String PREFIX = GoddagTreeNode.PREFIX + ".pi";

    public ProcessingInstruction(Node node) {
        super(node);
    }

    public ProcessingInstruction(GraphDatabaseService db, String target, String instruction) {
        this(db.createNode());
        setNodeType(NodeType.PI);
        setTarget(target);
        setInstruction(instruction);
    }

    @Override
    public String getText(Element root) {
        return "";
    }

    public String getTarget() {
        return (String) node.getProperty(PREFIX + ".target");
    }

    public void setTarget(String target) {
        node.setProperty(PREFIX + ".target", target);
    }

    public String getInstruction() {
        return (String) node.getProperty(PREFIX + ".instruction");
    }

    public void setInstruction(String instruction) {
        node.setProperty(PREFIX + ".instruction", instruction);
    }

    @Override
    public String toString() {
        return "<" + PREFIX + " '" + getTarget() + "'/> " + node.toString();
    }
}
