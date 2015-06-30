package asap.realizerdemo.motiongraph.graph1;

import asap.realizerdemo.motiongraph.AbstractMotionGraph;
import asap.realizerdemo.motiongraph.alignment.Alignment;
import asap.realizerdemo.motiongraph.alignment.IAlignment;
import asap.realizerdemo.motiongraph.blending.Blend;
import asap.realizerdemo.motiongraph.blending.IBlend;
import asap.realizerdemo.motiongraph.metrics.Equals;
import asap.realizerdemo.motiongraph.metrics.IDistance;
import asap.realizerdemo.motiongraph.metrics.IEquals;
import asap.realizerdemo.motiongraph.metrics.JointAngles;
import hmi.animation.SkeletonInterpolator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Zukie on 15/06/15.
 * @author Zukie
 */
public final class MotionGraph extends AbstractMotionGraph {
    
    /**
     * Number of Frames to be blended.
     */
    public static final int DEFAULT_BLENDING_FRAMES = 100;
    /**
     * Max distance suitable for blending.
     */
    public static final double DEFAULT_THRESHOLD = 20;
    /**
     * Min number of frames needed when spliiting a motion.
     */
    public static final int DEFAULT_MIN_SIZE = 150;
    /**
     * How many times is the motionGraph to be splitted.
     */
    public static final int DEFAULT_SPLIT_NUMBER = 20;

    private final List<Edge> edges = new LinkedList<>();
    private final List<Node> nodes = new LinkedList<>();

    private final IAlignment align;
    private final IBlend blending;
    private final IDistance metric;

    /**
     * Creates a new Motiongraph from {@code motions}.
     * <p>
     * Uses {@link asap.realizerdemo.motiongraph.alignment.Alignment} as align,
     * {@link asap.realizerdemo.motiongraph.metrics.JointAngles} als DistanceMetric and
     * {@link asap.realizerdemo.motiongraph.blending.Blend} as blending.
     * <p>
     * @param motions motions to create Graph from.
     */
    public MotionGraph(List<SkeletonInterpolator> motions) {
        super(motions);
        if (motions == null || motions.isEmpty()) {
            throw new IllegalArgumentException("motions null or empty.");
        }
        this.align = new Alignment();
        this.metric = new JointAngles(align);
        this.blending = new Blend(this.align);

        this.init(motions);
    }

    public MotionGraph(List<SkeletonInterpolator> motions, IAlignment align, IDistance metric, IBlend blending) {
        super(motions);
        if (motions == null || motions.isEmpty()) {
            throw new IllegalArgumentException("motions null or empty.");
        }
        if (align == null) {
            throw new IllegalArgumentException("No IAlignment specified.");
        }
        if (metric == null) {
            throw new IllegalArgumentException("No AbstractDistance specified.");
        }
        if (blending == null) {
            throw new IllegalArgumentException("No AbstractBlend specified.");
        }

        this.align = align;
        this.metric = metric;
        this.blending = blending;

        this.init(motions);
    }

    private void init(List<SkeletonInterpolator> motions) {
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

        for (int i = 0; i < DEFAULT_SPLIT_NUMBER; i++) {
            Edge splittingEdge = this.edges.get(r.nextInt(bound)); //Randomly choose Edge to be splitted
            int splittingBound = splittingEdge.getMotion().size(); //Get boundary for splitting
            int splittingPoint = r.nextInt(splittingBound); //Randomly choose splitting point
            Edge firstEdge = new Edge(splittingEdge.getMotion().subSkeletonInterpolator(0, splittingPoint));
            //fist half of splitted motion
            Edge secondEdge = new Edge(splittingEdge.getMotion().subSkeletonInterpolator(splittingPoint));
            //second half of splitted motion

            if (firstEdge.getMotion().size() >= DEFAULT_MIN_SIZE && secondEdge.getMotion().size() >= DEFAULT_MIN_SIZE) {

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
     * <p>
     * @param edge
     */
    private void removeEdge(Edge edge) {
        this.edges.remove(edge);
        edge.getStartNode().getIncomingEdges().remove(edge);
        edge.getStartNode().getOutgoingEdges().remove(edge);

        edge.getEndNode().getIncomingEdges().remove(edge);
        edge.getEndNode().getOutgoingEdges().remove(edge);
    }

    /**
     * Randomly chooses a path through the motion graph until it reaches an end.
     * <p>
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

            System.out.println("isBlend: " + currentEdge.isBlend());

            result.add(currentEdge.getMotion()); //add motion
            currentNode = currentEdge.getEndNode();

            // System.out.println(currentEdge);
        } while (currentNode.hasNext());

//        System.out.println("RandomWalk executed");
        return result;
    }

    /**
     * Randomly chooses a path through the motion graph with given number of frames.
     * <p>
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

        System.out.println("Blending started");

        for (Edge e : oldEdges) {
            if (e == null) {
                continue;
            }
            for (Edge g : oldEdges) {
                if (g == null) {
                    continue;
                }
                if (e == g) {
                    continue;//TODO vllt doch?
                }
                if (metric.distance(e.getMotion(), g.getMotion(), DEFAULT_BLENDING_FRAMES) <= DEFAULT_THRESHOLD) {

                    SkeletonInterpolator blendStart = e.getMotion().subSkeletonInterpolator(e.getMotion().size() - DEFAULT_BLENDING_FRAMES);
                    //Frames of the first motion to be blended
                    SkeletonInterpolator blendEnd = g.getMotion().subSkeletonInterpolator(0, DEFAULT_BLENDING_FRAMES);
                    //Frames of the second motion to be blended

                    SkeletonInterpolator blendedMotion = blending.blend(blendStart, blendEnd, DEFAULT_BLENDING_FRAMES);

                    SkeletonInterpolator split1 = e.getMotion().subSkeletonInterpolator(0, e.getMotion().size() - DEFAULT_BLENDING_FRAMES);
                    //Calculate incoming edge for new Node

                    SkeletonInterpolator split2 = g.getMotion().subSkeletonInterpolator(DEFAULT_BLENDING_FRAMES);
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

    /**
     * Builder for MotionGraph.
     */
    public static class Builder {

        private IAlignment align;
        private IBlend blending;
        private IDistance metric;
        private final List<SkeletonInterpolator> motions;

        public Builder(List<SkeletonInterpolator> motions) {
            this.motions = motions;
        }

        public MotionGraph getInstance() {
            this.align = align != null ? align : new Alignment();
            this.metric = metric != null ? metric : new JointAngles(align);
            this.blending = blending != null ? blending : new Blend(align);
            return new MotionGraph(this.motions, this.align, this.metric, this.blending);
        }

        public Builder align(IAlignment align) {
            this.align = align;
            return this;
        }

        public Builder blending(IBlend blending) {
            this.blending = blending;
            return this;
        }

        public Builder metric(IDistance metric) {
            this.metric = metric;
            return this;
        }

    }

}
