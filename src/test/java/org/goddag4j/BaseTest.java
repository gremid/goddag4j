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

public abstract class BaseTest {
    private static final File GRAPH_DIR = new File(System.getProperty("java.io.tmpdir", System.getProperty("user.dir", ".")),
            "goddag-test-db");
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
        db = new EmbeddedGraphDatabase(graphRootDir.getAbsolutePath());
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
            root = Element.create(db, "tei", "text");
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

    protected static final RelationshipType TEST = new RelationshipType() {

        public String name() {
            return "goddag.test";
        }
    };
}
