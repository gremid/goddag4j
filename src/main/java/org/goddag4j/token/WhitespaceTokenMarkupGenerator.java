package org.goddag4j.token;

import java.util.ArrayList;
import java.util.List;

import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.Text;

public class WhitespaceTokenMarkupGenerator extends AbstractTokenMarkupGenerator {

    @Override
    protected int[] getTokenStarts(Text textNode) {
        final String textContent = textNode.getText();
        List<Integer> tokenStarts = new ArrayList<Integer>();

        boolean prevCharIsWhitespace = true;
        for (int cc = 0; cc < textContent.length(); cc++) {
            boolean thisCharIsWhitespace = Character.isWhitespace(textContent.charAt(cc));
            if (!thisCharIsWhitespace && prevCharIsWhitespace) {
                tokenStarts.add(cc);
            }
            prevCharIsWhitespace = thisCharIsWhitespace;
        }

        final int[] result = new int[tokenStarts.size()];
        for (int tsc = 0;  tsc < tokenStarts.size(); tsc++) {
            result[tsc] = tokenStarts.get(tsc);
        }
        return result;
    }

    @Override
    protected boolean isTokenStart(Element root, GoddagTreeNode prev, GoddagTreeNode current) {
        if (current.getNodeType() != GoddagNode.NodeType.TEXT) {
            return false;
        }
        String currentText = current.getText(root);
        if (prev == null) {
            return !Character.isWhitespace(currentText.charAt(0));
        }
        final String prevText = prev.getText(root);
        return Character.isWhitespace(prevText.charAt(prevText.length() - 1)) && !Character.isWhitespace(currentText.charAt(0));
    }

    @Override
    protected Element createTokenElement(Element parent) {
        Element tokenElement = new Element(parent.node.getGraphDatabase(), "tei", "seg");
        tokenElement.setAttribute("tei", "function", "ws");
        return tokenElement;
    }
}
