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

package org.goddag4j.util;

import java.util.ArrayList;
import java.util.List;

import org.goddag4j.Comment;
import org.goddag4j.Element;
import org.goddag4j.GoddagEventHandler;
import org.goddag4j.ProcessingInstruction;
import org.goddag4j.Text;

public class MultiplexingGoddagEventHandler implements GoddagEventHandler {

    private List<GoddagEventHandler> handlers;

    public MultiplexingGoddagEventHandler(GoddagEventHandler... handlers) {
        setHandlers(handlers);
    }

    public void setHandlers(GoddagEventHandler... handlers) {
        this.handlers = new ArrayList<GoddagEventHandler>();
        for (GoddagEventHandler handler : handlers) {
            this.handlers.add(handler);
        }
    }

    @Override
    public void startElement(Element element) {
        for (GoddagEventHandler handler : handlers) {
            handler.startElement(element);
        }
    }

    @Override
    public void endElement(Element element) {
        for (GoddagEventHandler handler : handlers) {
            handler.endElement(element);
        }
    }

    @Override
    public void text(Text text) {
        for (GoddagEventHandler handler : handlers) {
            handler.text(text);
        }
    }

    @Override
    public void comment(Comment comment) {
        for (GoddagEventHandler handler : handlers) {
            handler.comment(comment);
        }
    }

    @Override
    public void processingInstruction(ProcessingInstruction pi) {
        for (GoddagEventHandler handler : handlers) {
            handler.processingInstruction(pi);
        }
    }

}
