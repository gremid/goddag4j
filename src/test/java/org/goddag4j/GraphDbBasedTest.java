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

public abstract class GraphDbBasedTest {
    private static final File GRAPH_DIR = new File(System.getProperty("java.io.tmpdir", System.getProperty("user.dir", ".")),
            "goddag-test-db");
    protected static GraphDatabaseService db;
    protected Transaction transaction;

    @BeforeClass
    public static void initGraphDb() throws IOException {
        if (GRAPH_DIR.exists()) {
            Preconditions.checkState(GRAPH_DIR.isDirectory(), GRAPH_DIR + " is not a directory");
            Files.deleteDirectoryContents(GRAPH_DIR);
        }
        db = new EmbeddedGraphDatabase(GRAPH_DIR.getAbsolutePath());
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
    }

    @After
    public void endTransaction() {
        if (transaction != null) {
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
