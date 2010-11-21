package org.goddag4j;

import org.neo4j.graphdb.Direction;
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

    public static long getRoot(Relationship r) {
        return (Long) r.getProperty(ROOT_PROPERTY);
    }

    public static void setRoot(Relationship r, long root) {
        r.setProperty(ROOT_PROPERTY, root);
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

    public static Relationship find(Iterable<Relationship> edges, long root) {
        for (Relationship edge : edges) {
            if (root == (Long) edge.getProperty(ROOT_PROPERTY, -1)) {
                return edge;
            }
        }
        return null;
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
