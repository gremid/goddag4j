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

import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.helpers.collection.IteratorUtil;

public class MultiRootedTree implements Iterable<Element> {

    private final RelationshipType rootRelation;
    private final Node node;

    public MultiRootedTree(Node node, RelationshipType rootRelation) {
        this.node = node;
        this.rootRelation = rootRelation;
    }

    public Node getNode() {
        return node;
    }
    
    public Element newRoot(String prefix, String name) {
        final Element root = new Element(node.getGraphDatabase(), prefix, name);
        addRoot(root);
        return root;
    }

    public void addRoot(Element root) {
        node.createRelationshipTo(root.node, rootRelation);
    }

    public Iterator<Element> iterator() {
        return new IterableWrapper<Element, Relationship>(node.getRelationships(rootRelation, OUTGOING)) {

            @Override
            protected Element underlyingObjectToObject(Relationship object) {
                return (Element) GoddagTreeNode.wrap(object.getOtherNode(node));
            }
        }.iterator();
    }

    public Element findRoot(String prefix, String name) {
        for (Element root : this) {
            if (name.equals(root.getName()) && prefix.equals(root.getPrefix())) {
                return root;
            }
        }
        return null;
    }

    public Element getRoot(String prefix, String name) {
        Element root = findRoot(prefix, name);
        if (root == null) {
            root = newRoot(prefix, name);
        }
        return root;
    }

    public void delete() {
        final List<Relationship> rootRels = new ArrayList<Relationship>();
        IteratorUtil.addToCollection(node.getRelationships(rootRelation, OUTGOING).iterator(), rootRels);

        for (Relationship rootRel : rootRels) {
            final Element root = (Element) GoddagTreeNode.wrap(rootRel.getEndNode());
            root.clear(root);
            root.delete();            
            rootRel.delete();
        }
    }

    public static final RelationshipType ROOT_RELATION = new RelationshipType() {

        public String name() {
            return GoddagTreeNode.PREFIX + ".root";
        }
    };
}
