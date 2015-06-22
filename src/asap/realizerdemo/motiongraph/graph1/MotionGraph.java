package asap.realizerdemo.motiongraph.graph1;

import asap.realizerdemo.motiongraph.AbstractMotionGraph;
import asap.realizerdemo.motiongraph.IEquals;
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

            // Mirror every motion
            SkeletonInterpolator newSp = new SkeletonInterpolator(sp);
            newSp.mirror();

            Edge mirroredEdge = new Edge(newSp);
            Node mirroredStartNode = new Node(null, mirroredEdge);
            Node mirroredEndNode = new Node(mirroredEdge, null);

            nodes.add(mirroredEndNode);
            nodes.add(mirroredStartNode);
            edges.add(mirroredEdge);

            i++;
        }

        this.connectMotions();

        for (Edge edge : edges) {
            System.out.println(edge);
        }

    }

    /**
     * Randomly chooses a path through the motion graph until it reaches an end.
     *
     * @return List of Skeletoninterpolators in chronological order
     */
    @Override
    public List<SkeletonInterpolator> randomWalk() {
        List<SkeletonInterpolator> result = new LinkedList<SkeletonInterpolator>();
        int outgoingEdgesBound;
        Edge currentEdge;

        Random r = new Random();
        int bound = edges.size();

        Node currentNode = edges.get(r.nextInt(bound)).getStartNode();
        System.out.println("Der Weg beginnt bei Node: " + currentNode.getId());

        do {
            outgoingEdgesBound = currentNode.getOutgoingEdges().size();
            currentEdge = currentNode.getOutgoingEdges().get(r.nextInt(outgoingEdgesBound));
            //choose random outgoing motion from current Node

            result.add(currentEdge.getMotion()); //add motion
            currentNode = currentEdge.getEndNode();

            System.out.println(currentEdge);

        } while (currentNode.getOutgoingEdges().size() != 0);

        return result;
    }

    /**
     * Randomly chooses a path through the motion graph with given number of frames.
     *
     * @param length length for the random-walk
     * @return List of Skeletoninterpolators in chronological order
     */
    @Override
    public List<SkeletonInterpolator> randomWalk(int length) {
        throw new UnsupportedOperationException("Not Supported yet!");
    }

    public void connectMotions() {
        IEquals equals = new Equals();

        for (Edge start : edges) {
            for (Edge end : edges) {
                if (equals.startEndEquals(start.getMotion(), end.getMotion())) {
                    Node deletedNode = end.getStartNode();
                    nodes.remove(deletedNode);
                    deletedNode.getOutgoingEdges().remove(end);
                    end.setStartNode(start.getEndNode());

                }
            }
        }
    }
}
