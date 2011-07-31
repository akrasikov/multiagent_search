/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package masearch;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author akrasikov
 */
public class IndexerAgent extends Agent {

    private static Logger logger = Logger.getLogger(IndexerAgent.class.getName());
    private Analyzer analyzer;
    private Directory directory;

    @Override
    protected void setup() {

        addBehaviour(new OneShotBehaviour() {

            public void action() {
                analyzer = new StandardAnalyzer(Version.LUCENE_30);

                try {
                    directory = FSDirectory.open(new File(Config.INDEX_DIR));

                    logger.info(Boolean.toString(IndexReader.indexExists(directory)));
                    if (!IndexReader.indexExists(directory)) {
                        IndexWriter writer = new IndexWriter(directory, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
                        File root = new File("c:/Users/");
                        logger.info("Indexing directory: " + root.getCanonicalPath());
                        Iterator<File> i = FileUtils.iterateFiles(root, Config.EXTENSIONS, true);
                        while (i.hasNext()) {
                            File f = i.next();
                            try {
                                logger.info("indexing: " + f.getCanonicalPath());
                                writer.addDocument(FileDocument.Document(f));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }

                        writer.optimize();
                        writer.close();
                        logger.info("Indexing completed.");
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    private static void indexDocs(IndexWriter writer, File file) {
        // do not try to index files that cannot be read
        if (file.canRead()) {
            if (file.isDirectory()) {
                String[] files = file.list();
                // an IO error could occur
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        indexDocs(writer, new File(file, files[i]));
                    }
                }
            } else {
                System.out.println("adding " + file);
                try {
                    writer.addDocument(FileDocument.Document(file));
                } // at least on windows, some temporary files raise this exception with an "access denied" message
                // checking if the file can be read doesn't help
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
