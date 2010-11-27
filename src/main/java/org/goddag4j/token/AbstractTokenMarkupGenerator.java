package org.goddag4j.token;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.goddag4j.Element;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.Text;

public abstract class AbstractTokenMarkupGenerator implements TokenMarkupGenerator {
    protected static final Logger LOG = Logger.getLogger(AbstractTokenMarkupGenerator.class.getPackage().getName());

    public void generate(Iterable<Text> input, Element to) {
        splitUp(input, to);
        groupTokenContents(to);
    }

    private void splitUp(Iterable<Text> input, Element into) {
        for (Text textNode : input) {
            for (Text segment : textNode.split(getTokenStarts(textNode))) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest("New text node: " + segment);
                }
                into.insert(into, segment, null);
            }
        }
    }

    private void groupTokenContents(Element in) {
        GoddagTreeNode prev = null;
        GoddagTreeNode current = in.getFirstChild(in);
        GoddagTreeNode token = null;
        do {
            if (current == null) {
                break;
            }

            if (isTokenStart(in, prev, current)) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest("New token");
                }
                token = createTokenElement(in);
                in.insert(in, token, current);
            }

            GoddagTreeNode next = current.getNextSibling(in);
            if (token != null) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest("Add to token: " + current);
                }
                token.insert(in, current, null);
            }
            prev = current;
            current = next;
        } while (true);
    }

    protected abstract int[] getTokenStarts(Text textNode);

    protected abstract boolean isTokenStart(Element root, GoddagTreeNode prev, GoddagTreeNode current);

    protected abstract Element createTokenElement(Element parent);
}
