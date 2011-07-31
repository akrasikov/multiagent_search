/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package masearch;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

/**
 *
 * @author Anton
 */
public class InterfaceAgent extends GuiAgent {

    private AgentSearchView view;
    private List<DFAgentDescription> searchAgents = new ArrayList<DFAgentDescription>();

    @Override
    protected void setup() {

        addBehaviour(new OneShotBehaviour(this) {

            @Override
            public void action() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("Searching");
                template.addServices(sd);

                try {
                    DFAgentDescription[] results = DFService.search(myAgent, template);
                    searchAgents.clear();
                    searchAgents.addAll(Arrays.asList(results));
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }

            }
        });

        addBehaviour(new TickerBehaviour(this, 60000) {

            @Override
            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("Searching");
                template.addServices(sd);

                try {
                    DFAgentDescription[] results = DFService.search(myAgent, template);
                    searchAgents.clear();
                    searchAgents.addAll(Arrays.asList(results));
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });

        addBehaviour(new CyclicBehaviour(this) {

            @Override
            public void action() {
                ACLMessage reply = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if (reply != null) {
                    String content = reply.getContent();
                    String sender = reply.getSender().getName();
                    System.out.println(content);
                } else {
                    block();
                }
            }
        });

        view = new AgentSearchView(AgentSearchApp.getApplication());
        view.setAgent(this);
        AgentSearchApp.launch(AgentSearchApp.class, new String[]{});
        AgentSearchApp.getApplication().show(view);
    }

    @Override
    protected void onGuiEvent(GuiEvent ev) {
        if (searchAgents.size() > 0) {
            String msgContent = (String) ev.getParameter(0) + ":" + (String) ev.getParameter(1);
            ACLMessage toSend = new ACLMessage(ACLMessage.REQUEST);
            toSend.setContent(msgContent);
            for (DFAgentDescription dfd : searchAgents) {
                toSend.addReceiver(dfd.getName());
            }
            send(toSend);
        }
    }
}
