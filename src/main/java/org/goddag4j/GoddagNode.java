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

import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IterableWrapper;

public abstract class GoddagNode {
    public enum NodeType {
        TEXT, ELEMENT, COMMENT, PI, ATTRIBUTE;
    }

    public static final NodeType[] NODE_TYPES = NodeType.values();
    public static final String PREFIX = "goddag";
    public static final String NODE_TYPE_PROPERTY = PREFIX + ".nt";

    public final Node node;

    protected GoddagNode(Node node) {
        this.node = node;
    }

    public NodeType getNodeType() {
        return getNodeType(node);
    }

    protected static NodeType getNodeType(Node node) {
        return (NodeType) NODE_TYPES[(Integer) node.getProperty(NODE_TYPE_PROPERTY)];
    }

    protected void setNodeType(NodeType nodeType) {
        node.setProperty(NODE_TYPE_PROPERTY, nodeType.ordinal());
    }

    public static GoddagNode wrap(Node node) {
        final NodeType nodeType = getNodeType(node);
        switch (nodeType) {
        case TEXT:
            return new Text(node);
        case ELEMENT:
            return new Element(node);
        case COMMENT:
            return new Comment(node);
        case PI:
            return new ProcessingInstruction(node);
        case ATTRIBUTE:
            return new Attribute(node);
        default:
            throw new IllegalArgumentException(nodeType.toString());
        }
    }

    protected String getOptionalStringProperty(String propertyName) {
        return getOptionalStringProperty(node, propertyName);
    }

    protected void setOptionalStringProperty(String propertyName, String propertyValue) {
        setOptionalStringProperty(node, propertyName, propertyValue);
    }

    public static String getOptionalStringProperty(Node node, String propertyName) {
        return (String) node.getProperty(propertyName, null);
    }

    public static void setOptionalStringProperty(Node node, String propertyName, String propertyValue) {
        if (propertyValue == null) {
            node.removeProperty(propertyName);
        } else {
            node.setProperty(propertyName, propertyValue);
        }
    }
    

    public static class GoddageNodeWrappingIterable extends IterableWrapper<GoddagNode, Node> {

        public GoddageNodeWrappingIterable(Iterable<Node> iterableToWrap) {
            super(iterableToWrap);
        }

        @Override
        protected GoddagNode underlyingObjectToObject(Node object) {
            return wrap(object);
        }
    }
}
