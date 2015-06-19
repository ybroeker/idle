package asap.realizerdemo.motiongraph.graph1;

import asap.realizerdemo.motiongraph.AbstractMotionGraph;
import hmi.animation.Skeleton;
import hmi.animation.SkeletonInterpolator;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Zukie on 15/06/15.
 */
public class MotionGraph extends AbstractMotionGraph {

    private List<Edge> edges = new LinkedList<Edge>();
    private List<Node> nodes = new LinkedList<Node>();

    public MotionGraph(List<SkeletonInterpolator> motions) {
        super(motions);

        int i = 0;

        for (SkeletonInterpolator sp : motions) {
            Edge newEdge = new Edge(sp);
            Node startNode = new Node(null, newEdge);
            Node endNode = new Node(newEdge, null);

            nodes.add(startNode);
            nodes.add(endNode);
            edges.add(newEdge);

            System.out.println(newEdge.toString());
            i++;
        }

/*        for (Node n : nodes) {
            System.out.println("Node Nr: " + n.getId() + " hat " + n.getIncomingEdges().size() + " Eingehende und " + n.getOutgoingEdges().size() + " ausgehende Edges.");

        }*/

    }

    @Override
    public List<SkeletonInterpolator> randomWalk() {
       List<SkeletonInterpolator> result = new LinkedList<SkeletonInterpolator>();
        int outgoingEdgesBound;
        Edge currentEdge;

        Random r = new Random();
        int bound = nodes.size();

        Node currentNode = nodes.get(r.nextInt(bound));
        System.out.println("Der Weg beginnt bei: " + currentNode.getId());

        do {
            outgoingEdgesBound = currentNode.getOutgoingEdges().size();
            currentEdge = currentNode.getOutgoingEdges().get(r.nextInt(outgoingEdgesBound));
            //choose random outgoing motion from current Node

            result.add(currentEdge.getMotion()); //add motion
            currentNode = currentEdge.getEndNode();

            System.out.println(currentEdge.getId());

        } while (currentNode.getOutgoingEdges().size()!=0);



        return result;
    }

    @Override
    public List<SkeletonInterpolator> randomWalk(int lenght) {
throw new UnsupportedOperationException("Not Supported yet!");
    }


}
