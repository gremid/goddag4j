package org.goddag4j;

public interface GoddagEventHandler {
    void startElement(Element element);

    void endElement(Element element);

    void text(Text text);

    void comment(Comment comment);

    void processingInstruction(ProcessingInstruction pi);
}
