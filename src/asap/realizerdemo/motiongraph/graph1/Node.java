package asap.realizerdemo.motiongraph.graph1;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Zukie on 15/06/15.
 */
public class Node {

    public static int nodeId = 0;
    private int id;
    private List<Edge> incomingEdges = new LinkedList<Edge>();
    private List<Edge> outgoingEdges = new LinkedList<Edge>();


    /**
     *
     * @param incomingEdge
     * @param outgoingEdge
     */
    public Node(Edge incomingEdge, Edge outgoingEdge) {
        this.id = nodeId++;

        if (incomingEdge != null) {
            this.incomingEdges.add(incomingEdge);
            incomingEdge.setEndNode(this);
        }
        if (outgoingEdge != null) {
            this.outgoingEdges.add(outgoingEdge);
            outgoingEdge.setStartNode(this);
        }



    }


    public List<Edge> getIncomingEdges() {
        return incomingEdges;
    }

    public void setIncomingEdges(List<Edge> incomingEdges) {
        this.incomingEdges = incomingEdges;
    }

    public List<Edge> getOutgoingEdges() {
        return outgoingEdges;
    }

    public void setOutgoingEdges(List<Edge> outgoingEdges) {
        this.outgoingEdges = outgoingEdges;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
