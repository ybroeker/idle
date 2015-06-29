package asap.realizerdemo.motiongraph.graph1;

import asap.realizerdemo.motiongraph.*;
import asap.realizerdemo.motiongraph.metrics.JointAngles;
import hmi.animation.Skeleton;
import hmi.animation.SkeletonInterpolator;
import org.apache.commons.math3.analysis.function.Abs;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Zukie on 15/06/15.
 */
public class MotionGraph extends AbstractMotionGraph {

    private List<Edge> edges = new LinkedList<Edge>();
    private List<Node> nodes = new LinkedList<Node>();
    //private List<Edge> blends = new LinkedList<Edge>();
    private IAlignment align;
    private AbstractBlend blending;
    private AbstractDistance metric;
    /**
     * Number of Frames to be blended.
     */
    public static int BLENDING_FRAMES = 100;
    /**
     * Max distance suitable for blending.
     */
    public static double THRESHOLD = 20;
    /**
     * Min number of frames needed when spliiting a motion.
     */
    public static int MIN_SIZE = 150;

    /**
     * How many times is the motionGraph to be splitted.
     */
    public static final int SPLIT_NUMBER = 20;


    public MotionGraph(List<SkeletonInterpolator> motions) {
        this(motions, new Alignment());

    }

    public MotionGraph(List<SkeletonInterpolator> motions, Alignment align) {
        super(motions);
        this.align = align;

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

/*        for (Edge edge : edges) {
            System.out.println(edge);
        }*/
    }


    /**
     * Randomly splits Motions in the graph.
     */
    public void split() {
        Random r = new Random();
        int bound = this.edges.size();

        for (int i = 0; i < SPLIT_NUMBER; i++) {
            Edge splittingEdge = this.edges.get(r.nextInt(bound)); //Randomly choose Edge to be splitted
            int splittingBound = splittingEdge.getMotion().size(); //Get boundary for splitting
            int splittingPoint = r.nextInt(splittingBound); //Randomly choose splitting point
            Edge firstEdge = new Edge(splittingEdge.getMotion().subSkeletonInterpolator(0, splittingPoint));
            //fist half of splitted motion
            Edge secondEdge = new Edge(splittingEdge.getMotion().subSkeletonInterpolator(splittingPoint));
            //second half of splitted motion

            if (firstEdge.getMotion().size() >= MIN_SIZE && secondEdge.getMotion().size() >= MIN_SIZE) {

                Node startNode = splittingEdge.getStartNode();
                Node endNode = splittingEdge.getEndNode();

                firstEdge.setStartNode(startNode);
                secondEdge.setEndNode(endNode);

                this.edges.add(firstEdge);
                this.edges.add(secondEdge);

                Node newNode = new Node(firstEdge, secondEdge);


                this.nodes.add(newNode);

                removeEdge(splittingEdge);
            }


        }
/*
        System.out.println("Splitting Executed");
*/

    }

    /**
     * Remove Edge from MotionGraph and it's nodes.
     *
     * @param edge
     */
    public void removeEdge(Edge edge) {
        this.edges.remove(edge);
        edge.getStartNode().getIncomingEdges().remove(edge);
        edge.getStartNode().getOutgoingEdges().remove(edge);

        edge.getEndNode().getIncomingEdges().remove(edge);
        edge.getEndNode().getOutgoingEdges().remove(edge);
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
/*
        System.out.println("Der Weg beginnt bei Node: " + currentNode.getId());
*/

        do {
            outgoingEdgesBound = currentNode.getOutgoingEdges().size();
            currentEdge = currentNode.getOutgoingEdges().get(r.nextInt(outgoingEdgesBound));
            //choose random outgoing motion from current Node

            System.out.println("isBlend: "+currentEdge.isBlend());

            result.add(currentEdge.getMotion()); //add motion
            currentNode = currentEdge.getEndNode();

            // System.out.println(currentEdge);

        } while (currentNode.hasNext());

//        System.out.println("RandomWalk executed");
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

    /**
     * Connect all Motions that are similar enough with blends.
     */
    public void createBlends() {

        List<Edge> oldEdges = new LinkedList<>(edges);

//        System.out.println("Blending started");
        this.blending = new Blend(this.align);
        this.metric = new JointAngles();

        SkeletonInterpolator blendedMotion;
        SkeletonInterpolator blendStart;
        SkeletonInterpolator blendEnd;

        for (Edge e : oldEdges) {
            if (e == null) continue;
            for (Edge g : oldEdges) {
                if (g == null) continue;
                if (e==g) continue;//TODO vllt doch?

                if (metric.distance(e.getMotion(), g.getMotion(), BLENDING_FRAMES) <= THRESHOLD) {

                    blendedMotion = blending.blend(e.getMotion(), g.getMotion(), BLENDING_FRAMES);

                    blendStart = e.getMotion().subSkeletonInterpolator(e.getMotion().size() - BLENDING_FRAMES);
                    //Frames of the first motion to be blended
                    blendEnd = g.getMotion().subSkeletonInterpolator(0,BLENDING_FRAMES);
                    //Frames of the second motion to be blended


                    SkeletonInterpolator split1 = e.getMotion().subSkeletonInterpolator(0, e.getMotion().size() - BLENDING_FRAMES);
                    //Calculate incoming edge for new Node

                    SkeletonInterpolator split2 = g.getMotion().subSkeletonInterpolator(BLENDING_FRAMES);
                    //Calculate outgoing Edge for new Node

                    // Edges for splittet motions
                    Edge firstMotionPart1 = new Edge(split1);
                    Edge firstMotionPart2 = new Edge(blendStart);
                    Edge secondMotionPart1 = new Edge(blendEnd);
                    Edge secondMotionPart2 = new Edge(split2);

                    Edge blending = new Edge(e.getEndNode(), g.getStartNode(), blendedMotion);

                    //split first motion
                    e.getStartNode().addOutgoingEdge(firstMotionPart1);
                    e.getEndNode().addIncomingEdge(firstMotionPart2);
                    Node newStart = new Node(firstMotionPart1, firstMotionPart2);
                    newStart.addOutgoingEdge(blending);

                    //Split second motion
                    g.getStartNode().addOutgoingEdge(secondMotionPart1);
                    g.getEndNode().addIncomingEdge(secondMotionPart2);
                    Node newEnd = new Node(secondMotionPart1, secondMotionPart2);
                    newEnd.addIncomingEdge(blending);

                        edges.add(blending);
                        edges.add(firstMotionPart1);
                        edges.add(firstMotionPart2);
                        edges.add(secondMotionPart1);
                        edges.add(secondMotionPart2);

                        blending.setBlend(true);
                       // System.out.println("blend created");
                        this.removeEdge(e);
                        this.removeEdge(g);


                }
            }

        }
       // System.out.println("blending executed. New Graph as follows: ");
       // System.out.println(this.toString());
    }

    @Override
    public String toString() {
        String ret = "Edges: " + edges.size() + "\n";
        for (Edge edge : edges) {
            ret += edge + "\n";
        }
        return ret;

    }

    public IAlignment getAlign() {
        return align;
    }

}
