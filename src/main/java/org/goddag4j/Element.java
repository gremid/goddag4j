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
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.collection.IterableWrapper;

public class Element extends GoddagNode {

    private static final String PREFIX = GoddagNode.PREFIX + ".element";

    public Element(Node node) {
        super(node);
    }

    public Element(GraphDatabaseService db, String prefix, String name) {
        this(db.createNode());
        setNodeType(NodeType.ELEMENT);
        setName(name);
        setPrefix(prefix);
    }

    public Iterable<Attribute> getAttributes() {
        return new IterableWrapper<Attribute, Relationship>(node.getRelationships(ATTRIBUTE)) {

            @Override
            protected Attribute underlyingObjectToObject(Relationship object) {
                return new Attribute(object.getOtherNode(node));
            }
        };
    }

    public Attribute getAttribute(String attributePrefix, String attributeName) {
        String qName = getQName(attributePrefix, attributeName);
        for (Attribute attr : getAttributes()) {
            if (qName.equals(attr.getQName())) {
                return attr;
            }
        }
        return null;
    }

    public String getAttributeValue(String attributePrefix, String attributeName) {
        final Attribute attribute = getAttribute(attributePrefix, attributeName);
        return (attribute == null ? null : attribute.getValue());
    }

    public Attribute setAttribute(String attributePrefix, String attributeName, String attributeValue) {
        Attribute attr = getAttribute(attributePrefix, attributeName);
        if (attr == null) {
            final Node attrNode = node.getGraphDatabase().createNode();
            node.createRelationshipTo(attrNode, ATTRIBUTE);

            attr = new Attribute(attrNode);
            attr.setPrefix(attributePrefix);
            attr.setName(attributeName);
        }

        attr.setValue(attributeValue);
        return attr;
    }

    public void removeAttribute(String attributePrefix, String attributeName) {
        String qName = getQName(attributePrefix, attributeName);
        Attribute toDelete = null;
        for (Attribute attr : getAttributes()) {
            if (qName.equals(attr.getQName())) {
                toDelete = attr;
                break;
            }
        }
        if (toDelete == null) {
            throw new IllegalArgumentException(qName);
        }

        final Node node = toDelete.node;
        node.getSingleRelationship(ATTRIBUTE, INCOMING).delete();
        node.delete();
    }

    public String getPrefix() {
        return getOptionalStringProperty(PREFIX + ".prefix");
    }

    public void setPrefix(String prefix) {
        setOptionalStringProperty(PREFIX + ".prefix", prefix);
    }

    public String getName() {
        return (String) node.getProperty(PREFIX + ".name");
    }

    public void setName(String name) {
        node.setProperty(PREFIX + ".name", name);
    }

    @Override
    public boolean delete(Element root) {
        if (super.delete(root)) {
            List<Relationship> toDelete = new ArrayList<Relationship>();
            for (Relationship attrRel : node.getRelationships(ATTRIBUTE, OUTGOING)) {
                toDelete.add(attrRel);
            }
            for (Relationship r : toDelete) {
                r.delete();
                r.getEndNode().delete();
            }
            return true;
        }
        return false;
    }

    public static String getQName(String prefix, String name) {
        return (prefix == null || prefix.length() == 0 ? "" : prefix + ":") + name;
    }

    public String getQName() {
        return getQName(getPrefix(), getName());
    }

    @Override
    public String toString() {
        return "<" + PREFIX + " '" + getQName() + "'/> " + node.toString();
    }

    protected static final RelationshipType ATTRIBUTE = new RelationshipType() {

        public String name() {
            return GoddagNode.PREFIX + ".attr";
        }
    };

}
