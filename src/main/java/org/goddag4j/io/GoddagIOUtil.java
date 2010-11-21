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

package org.goddag4j.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URI;
import java.util.Map;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.goddag4j.Element;
import org.neo4j.graphdb.GraphDatabaseService;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GoddagIOUtil {

    public static Element parse(InputSource xml, Map<URI, String> namespaces, GraphDatabaseService db) throws SAXException,
            IOException {
        final GoddagContentHandler ch = new GoddagContentHandler(db, namespaces);
        ch.configuredReader().parse(xml);
        return ch.getRoot();

    }

    public static void serialize(Iterable<Element> roots, Writer out) throws IOException {
        JsonGenerator jsonOut = new JsonFactory().createJsonGenerator(out);
        try {
            new GoddagJsonSerializer().serialize(jsonOut, roots);
        } finally {
            jsonOut.close();
        }
    }

    public static void serialize(Iterable<Element> roots, OutputStream out) throws IOException {
        JsonGenerator jsonOut = new JsonFactory().createJsonGenerator(out, JsonEncoding.UTF8);
        try {
            new GoddagJsonSerializer().serialize(jsonOut, roots);
        } finally {
            jsonOut.close();
        }
    }

    public static void dump(Iterable<Element> roots, PrintStream out) throws IOException {
        JsonGenerator jsonOut = new JsonFactory().createJsonGenerator(out, JsonEncoding.UTF8);
        jsonOut.useDefaultPrettyPrinter();
        try {
            new GoddagJsonSerializer().serialize(jsonOut, roots);
        } finally {
            jsonOut.close();
        }
    }
}
