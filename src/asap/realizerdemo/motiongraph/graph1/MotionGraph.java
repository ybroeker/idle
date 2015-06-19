package asap.realizerdemo.motiongraph.graph1;

import asap.realizerdemo.motiongraph.AbstractMotionGraph;
import hmi.animation.SkeletonInterpolator;

import java.util.LinkedList;
import java.util.List;

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
    public SkeletonInterpolator randomWalk() {
        return null;
    }

    @Override
    public SkeletonInterpolator randomWalk(int lenght) {
        return null;
    }


}
