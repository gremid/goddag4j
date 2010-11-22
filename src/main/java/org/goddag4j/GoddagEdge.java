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

import java.util.Collections;
import java.util.Iterator;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class GoddagEdge {
    public enum EdgeType implements RelationshipType {
        HAS_FIRST_CHILD, IS_LAST_CHILD_OF, HAS_SIBLING, CONTAINS, HAS_ATTRIBUTE        
    }

    public static final String ROOT_PROPERTY = GoddagNode.PREFIX + ".root";

    public static Relationship find(Iterable<Relationship> edges, long root) {
        for (Relationship edge : edges) {
            final long[] rootIds = (long[]) edge.getProperty(ROOT_PROPERTY);
            for (long rootId : rootIds) {
                if (root == rootId) {
                    return edge;
                }
            }
        }
        return null;
    }

    public static Iterable<Element> getRoots(Relationship rel) {
        if (rel == null) {
            return Collections.emptyList();
        }
        final long[] rootIds = (long[]) rel.getProperty(ROOT_PROPERTY);
        final GraphDatabaseService db = rel.getGraphDatabase();
        return new Iterable<Element>() {

            @Override
            public Iterator<Element> iterator() {
                return new Iterator<Element>() {
                    private final int numRoots = rootIds.length;
                    private int rootIndex = 0;

                    @Override
                    public boolean hasNext() {
                        return rootIndex < numRoots;
                    }

                    @Override
                    public Element next() {
                        return (Element) GoddagNode.wrap(db.getNodeById(rootIds[rootIndex++]));
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public static void add(EdgeType relationshipType, Node from, Node to, long root) {
        for (Relationship r : from.getRelationships(relationshipType, OUTGOING)) {
            if (to.equals(r.getEndNode())) {
                final long[] existing = (long[]) r.getProperty(ROOT_PROPERTY);
                final int numExisting = existing.length;
                final long[] extended = new long[numExisting + 1];
                System.arraycopy(existing, 0, extended, 0, numExisting);
                extended[numExisting] = root;
                r.setProperty(ROOT_PROPERTY, extended);
                return;
            }
        }
        Relationship r = from.createRelationshipTo(to, relationshipType);
        r.setProperty(ROOT_PROPERTY, new long[] { root });
    }

    public static void remove(Iterable<Relationship> rels, long root) {
        for (Relationship r : rels) {
            final long[] rootIds = (long[]) r.getProperty(ROOT_PROPERTY);
            final int numRoots = rootIds.length;
            for (int rc = 0; rc < numRoots; rc++) {
                if (rootIds[rc] == root) {
                    if (numRoots == 1) {
                        r.delete();
                        return;
                    }

                    final long[] shrunk = new long[numRoots - 1];
                    System.arraycopy(rootIds, 0, shrunk, 0, rc);
                    System.arraycopy(rootIds, rc, shrunk, rc, numRoots - rc);
                    r.setProperty(ROOT_PROPERTY, shrunk);
                    return;
                }
            }
        }
    }
}
