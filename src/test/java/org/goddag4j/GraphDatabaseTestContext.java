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

package org.goddag4j;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.goddag4j.io.GoddagJSONWriter;
import org.goddag4j.io.NamespaceMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

public abstract class GraphDatabaseTestContext {
    private static final File GRAPH_DIR = new File(System.getProperty("java.io.tmpdir", System.getProperty("user.dir", ".")),
            "goddag-test-db");
    protected static final Logger log = Logger.getLogger(GraphDatabaseTestContext.class.getName());
    protected static GraphDatabaseService db;

    protected Transaction transaction;
    protected Element root;


    @BeforeClass
    public static void initGraphDb() throws IOException {
        final File graphRootDir = GRAPH_DIR.getCanonicalFile();
        if (graphRootDir.exists()) {
            Preconditions.checkState(graphRootDir.isDirectory(), graphRootDir + " is not a directory");
            Files.deleteRecursively(graphRootDir);
        }
        
        final String graphRootDirPath = graphRootDir.getAbsolutePath();
        log.info("Firing up test graph database in " + graphRootDirPath);
        db = new EmbeddedGraphDatabase(graphRootDirPath);
    }

    @AfterClass
    public static void shutdownGraphDb() throws IOException {
        if (db != null) {
            db.shutdown();
        }
    }

    @Before
    public void startTransaction() {
        if (db != null) {
            transaction = db.beginTx();
        }
        
        if (root == null) {
            root = new Element(db, "tei", "text");
            db.getReferenceNode().createRelationshipTo(root.node, TEST);
        }
    }

    @After
    public void endTransaction() {
        if (transaction != null) {
            transaction.success();
            transaction.finish();
            transaction = null;
        }
    }

    protected static void dump(Iterable<Element> roots) throws IOException {
        final JsonGenerator out = new JsonFactory().createJsonGenerator(System.out, JsonEncoding.UTF8);
        try {
            new GoddagJSONWriter(NamespaceMap.EMPTY).write(roots, out);
        } finally {
            out.flush();
        }
        System.out.println();
        System.out.flush();
    }

    protected static void dump(Source source) throws Exception {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(source, new StreamResult(System.out));
        System.out.println();
        System.out.flush();
    }
    
    protected static final RelationshipType TEST = new RelationshipType() {

        public String name() {
            return "goddag.test";
        }
    };
}
