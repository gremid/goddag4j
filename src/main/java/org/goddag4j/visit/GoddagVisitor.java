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

package org.goddag4j.visit;

import org.goddag4j.Comment;
import org.goddag4j.Element;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.ProcessingInstruction;
import org.goddag4j.Text;

public abstract class GoddagVisitor {

    public void visit(Element root, Element element) {
        startElement(root, element);
        for (GoddagTreeNode child : element.getChildren(root)) {
            switch (child.getNodeType()) {
            case TEXT:
                text(root, (Text) child);
                break;
            case ELEMENT:
                visit(root, (Element) child);
                break;
            case COMMENT:
                comment(root, (Comment) child);
                break;
            case PI:
                processingInstruction(root, (ProcessingInstruction) child);
                break;
            }
        }
        endElement(root, element);
    }

    public void startElement(Element root, Element element) {
    }

    public void endElement(Element root, Element element) {
    }

    public void text(Element root, Text text) {
    }

    public void comment(Element root, Comment comment) {
    }

    public void processingInstruction(Element root, ProcessingInstruction pi) {
    }
}
