/*
 * Created on Mar 10, 2005
 *
 */
/*
 * -----------------------------------------------------------------------------
 *
 * <p><b>License and Copyright: </b>The contents of this file are subject to the
 * Educational Community License (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at <a href="http://www.opensource.org/licenses/ecl1.txt">
 * http://www.opensource.org/licenses/ecl1.txt.</a></p>
 *
 * <p>Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.</p>
 *
 * <p>The entire file consists of original code.  Copyright &copy; 2002-2006 by 
 * The Rector and Visitors of the University of Virginia. 
 * All rights reserved.</p>
 *
 * -----------------------------------------------------------------------------
 */

package org.goddag4j.token;

import java.util.ArrayList;
import java.util.List;

import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.Text;

public class LineTokenMarkupGenerator extends AbstractTokenMarkupGenerator {

    @Override
    protected int[] getTokenStarts(Text textNode) {
        String textContent = textNode.getText();
        List<Integer> tokenStarts = new ArrayList<Integer>();
        boolean prevIsLinebreak = true;
        for (int cc = 0; cc < textContent.length(); cc++) {
            char currentChar = textContent.charAt(cc);
            boolean thisIsLinebreak = (currentChar == '\n' || currentChar == '\r');
            if (!thisIsLinebreak && prevIsLinebreak) {
                tokenStarts.add(cc);
            }
            prevIsLinebreak = thisIsLinebreak;
        }

        final int[] result = new int[tokenStarts.size()];
        for (int tsc = 0;  tsc < tokenStarts.size(); tsc++) {
            result[tsc] = tokenStarts.get(tsc);
        }
        return result;
    }

    protected Element createTokenElement(Element parent) {
        return new Element(parent.node.getGraphDatabase(), "tei", "line");
    }

    @Override
    protected boolean isTokenStart(Element root, GoddagTreeNode prev, GoddagTreeNode current) {
        if (current.getNodeType() != GoddagNode.NodeType.TEXT) {
            return false;
        }
        String currentText = current.getText(root);
        if (prev == null) {
            return !currentText.startsWith("\n") && !currentText.startsWith("\r");
        }
        final String prevText = prev.getText(root);
        return ((prevText.charAt(prevText.length() - 1) == '\n') || (prevText.charAt(prevText.length() - 1) == '\r'))
                && ((currentText.charAt(0) != '\n') && (currentText.charAt(0) != '\r'));
    }
}
