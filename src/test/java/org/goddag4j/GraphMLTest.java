package org.goddag4j;

import javax.xml.stream.XMLOutputFactory;

import org.goddag4j.io.GraphMLSerializer;
import org.junit.Test;

public class GraphMLTest extends GoddagTestBase {

    @Test
    public void serialize() throws Exception {
        new GraphMLSerializer().write(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out), root);
    }
}
