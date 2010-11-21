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

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.Predicate;
import org.neo4j.helpers.collection.FilteringIterable;

public class GoddagNodeEdge implements RelationshipType {
    public static final String ROOT_PROPERTY = GoddagNode.PREFIX + ".root";

    public static final FirstChildEdge FIRST_CHILD = new FirstChildEdge(null);
    public static final LastChildEdge LAST_CHILD = new LastChildEdge(null);
    public static final SiblingEdge SIBLING_AXIS = new SiblingEdge(null);
    public static final DescendantEdge DESCENDANT_AXIS = new DescendantEdge(null);
    public static final AlignmentEdge ALIGNMENT_AXIS = new AlignmentEdge(null);

    private final String name;
    private final Relationship r;

    public static GoddagNodeEdge forRelationship(Relationship r) {
        String rt = r.getType().name();
        if (DescendantEdge.NAME.equals(rt)) {
            return new DescendantEdge(r);
        } else if (SiblingEdge.NAME.equals(rt)) {
            return new SiblingEdge(r);
        } else if (AlignmentEdge.NAME.equals(rt)) {
            return new AlignmentEdge(r);
        }

        throw new IllegalArgumentException(rt);
    }

    protected GoddagNodeEdge(String name, Relationship r) {
        this.name = name;
        this.r = r;
    }

    public Relationship getUnderlyingRelationship() {
        return r;
    }

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

    public static void add(GoddagNodeEdge relationshipType, Node from, Node to, long root) {
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

    public GoddagNode getStart() {
        return GoddagNode.wrap(r.getStartNode());
    }

    public GoddagNode getEnd() {
        return GoddagNode.wrap(r.getEndNode());
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (r != null && obj != null && obj instanceof GoddagNodeEdge) {
            return r.equals(((GoddagNodeEdge) obj).r);
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (r == null ? super.hashCode() : r.hashCode());
    }

    public static class DescendantEdge extends GoddagNodeEdge {
        public static final String NAME = GoddagNode.PREFIX + ".child";

        public DescendantEdge(Relationship r) {
            super(NAME, r);
        }

    }

    public static class SiblingEdge extends GoddagNodeEdge {
        public static final String NAME = GoddagNode.PREFIX + ".sibling";

        public SiblingEdge(Relationship r) {
            super(NAME, r);
        }
    }

    public static class FirstChildEdge extends GoddagNodeEdge {
        public static final String NAME = GoddagNode.PREFIX + ".first";

        protected FirstChildEdge(Relationship r) {
            super(NAME, r);
        }
    }

    public static class LastChildEdge extends GoddagNodeEdge {
        public static final String NAME = GoddagNode.PREFIX + ".last";

        protected LastChildEdge(Relationship r) {
            super(NAME, r);
        }
    }

    public static class AlignmentEdge extends GoddagNodeEdge {
        public static final String NAME = GoddagNode.PREFIX + ".align";

        public AlignmentEdge(Relationship r) {
            super(NAME, r);
        }

    }

    public static class EdgeRootPredicate implements Predicate<Relationship> {

        private final long rootId;

        public EdgeRootPredicate(long rootId) {
            this.rootId = rootId;
        }

        public boolean accept(Relationship item) {
            return rootId == ((Long) item.getProperty(ROOT_PROPERTY, -1));
        }

    }

    public static class EdgeRelationshipExpander implements RelationshipExpander {
        private final EdgeRootPredicate predicate;
        private final GoddagNodeEdge type;
        private final Direction direction;

        public EdgeRelationshipExpander(long rootId, GoddagNodeEdge type, Direction direction) {
            this.predicate = new EdgeRootPredicate(rootId);
            this.type = type;
            this.direction = direction;
        }

        public Iterable<Relationship> expand(Node node) {
            return new FilteringIterable<Relationship>(node.getRelationships(type, direction), predicate);
        }

        public RelationshipExpander reversed() {
            return new EdgeRelationshipExpander(predicate.rootId, type, direction.reverse());
        }
    }
}
