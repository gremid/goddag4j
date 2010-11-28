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
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class GoddagEdge implements RelationshipType {
    public static final GoddagEdge HAS_FIRST_CHILD = new GoddagEdge("has-first-child");
    public static final GoddagEdge IS_LAST_CHILD_OF = new GoddagEdge("is-last-child-of");
    public static final GoddagEdge HAS_SIBLING = new GoddagEdge("has-sibling");
    public static final GoddagEdge CONTAINS = new GoddagEdge("contains");
    public static final GoddagEdge HAS_ATTRIBUTE = new GoddagEdge("has-attribute");

    private static final String ROOT_PROPERTY = GoddagNode.PREFIX + ".root";

    private final String name;

    private GoddagEdge(String name) {
        this.name = (GoddagNode.PREFIX + "." + name);
    }

    public String name() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof RelationshipType) {
            return this.name.equals(((RelationshipType) obj).name());
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static Relationship find(Iterable<Relationship> edges, long root) {
        for (Relationship edge : edges) {
            final long rootId = ((Long) edge.getProperty(ROOT_PROPERTY)).longValue();
            if (root == rootId) {
                return edge;
            }
        }
        return null;
    }

    public static Element getRoot(Relationship rel) {
        final long rootId = ((Long) rel.getProperty(ROOT_PROPERTY)).longValue();
        return (Element) GoddagTreeNode.wrap(rel.getGraphDatabase().getNodeById(rootId));
    }

    public static void add(GoddagEdge relationshipType, Node from, Node to, long root) {
        final Relationship r = from.createRelationshipTo(to, relationshipType);
        r.setProperty(ROOT_PROPERTY, root);
    }

    public static void remove(Iterable<Relationship> rels, long root) {
        for (Relationship r : rels) {
            final long rootId = ((Long) r.getProperty(ROOT_PROPERTY)).longValue();
            if (rootId == root) {
                r.delete();
                return;
            }
        }
    }
}
