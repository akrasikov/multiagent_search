package masearch;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.FSDirectory;

/**
 * Created by IntelliJ IDEA.
 * User: Anton
 * Date: 18.06.2010
 * Time: 3:26:39
 * To change this template use File | Settings | File Templates.
 */
public class SearchAgent extends Agent {

    private Analyzer analyzer;
    private Directory directory;

    @Override
    protected void setup() {
        System.out.println("SearchAgent init. Name: " + getAID().getName());

        analyzer = new StandardAnalyzer(Version.LUCENE_30);
        try {
            directory = FSDirectory.open(new File(Config.INDEX_DIR));
        } catch (IOException ex) {
            Logger.getLogger(SearchAgent.class.getName()).log(Level.SEVERE, null, ex);
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Searching");
        sd.setName(getLocalName() + "-Searching");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (msg != null) {
                    String content = msg.getContent();
                    String[] data = content.split(":");
                    search(data[0], data[1]);
                } else {
                    block();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("SearchAgent " + getAID().getName() + " terminated.");
    }

    private void search(String pattern, String content) {
        addBehaviour(new SearchBehaviour(pattern, content));
    }

    private class SearchBehaviour extends OneShotBehaviour {

        private String pattern;
        private String content;

        private SearchBehaviour(String pattern, String content) {
            this.pattern = pattern;
            this.content = content;
        }

        @Override
        public void action() {
            try {
                IndexReader reader = IndexReader.open(directory, true);
                Searcher searcher = new IndexSearcher(reader);

                MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_30, new String[]{"path", "contents"}, analyzer);
                Query query = parser.parse("path:" + pattern + " contents:" + content);

                System.out.println("Searching: " + pattern + "; " + content);
                TopDocs docs = searcher.search(query, 10);

                StringBuffer sb = new StringBuffer();
                for (ScoreDoc sd : docs.scoreDocs) {
                    sb.append(sd.doc);
                    sb.append(": ");
                    sb.append(searcher.doc(sd.doc));
                    sb.append("\n");
                }
                reader.close();
                searcher.close();
                System.out.println("Completed.");

                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent(sb.toString());
                msg.addReceiver(new AID("interface", AID.ISLOCALNAME));
                myAgent.send(msg);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
