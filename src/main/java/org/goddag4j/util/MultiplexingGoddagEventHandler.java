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
