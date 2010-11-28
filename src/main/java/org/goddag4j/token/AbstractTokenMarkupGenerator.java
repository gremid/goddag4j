package org.goddag4j.token;

import org.goddag4j.Element;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.Text;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

public abstract class AbstractTokenMarkupGenerator implements TokenMarkupGenerator {
    private GraphDatabaseService db;

    public void generate(Iterable<Text> input, Element to) {
        this.db = to.node.getGraphDatabase();
        splitUp(input, to);
        groupTokenContents(to);
    }

    private void splitUp(Iterable<Text> input, Element into) {
        for (Text textNode : input) {
            Transaction tx = db.beginTx();
            try {
                for (Text segment : textNode.split(getTokenStarts(textNode))) {
                    into.insert(into, segment, null);
                }
                tx.success();
            } finally {
                tx.finish();
            }
        }
    }

    private void groupTokenContents(Element in) {
        Transaction tx = db.beginTx();
        try {
            GoddagTreeNode prev = null;
            GoddagTreeNode current = in.getFirstChild(in);
            GoddagTreeNode token = null;
            do {
                if (current == null) {
                    break;
                }

                if (tx == null) {
                    tx = db.beginTx();
                }
                
                if (isTokenStart(in, prev, current)) {
                    token = createTokenElement(in);
                    in.insert(in, token, current);
                }

                GoddagTreeNode next = current.getNextSibling(in);
                if (token != null) {
                    token.insert(in, current, null);
                }
                prev = current;
                current = next;
                
                tx.success();
                tx.finish();
                tx = null;
            } while (true);
        } finally {
            if (tx != null) {
                tx.finish();
            }
        }

    }

    protected abstract int[] getTokenStarts(Text textNode);

    protected abstract boolean isTokenStart(Element root, GoddagTreeNode prev, GoddagTreeNode current);

    protected abstract Element createTokenElement(Element parent);
}
