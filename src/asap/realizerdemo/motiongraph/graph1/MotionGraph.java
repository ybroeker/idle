package asap.realizerdemo.motiongraph.graph1;

import asap.realizerdemo.motiongraph.IMotionGraph;
import asap.realizerdemo.motiongraph.alignment.Alignment;
import asap.realizerdemo.motiongraph.alignment.IAlignment;
import asap.realizerdemo.motiongraph.blending.Blend;
import asap.realizerdemo.motiongraph.blending.IBlend;
import asap.realizerdemo.motiongraph.metrics.Equals;
import asap.realizerdemo.motiongraph.metrics.IDistance;
import asap.realizerdemo.motiongraph.metrics.IEquals;
import asap.realizerdemo.motiongraph.metrics.JointAngles;
import asap.realizerdemo.motiongraph.split.DefaultSplit;
import asap.realizerdemo.motiongraph.split.ISplit;
import hmi.animation.SkeletonInterpolator;
import java.util.Iterator;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Use {@link Builder} to create an Instance.
 * <p>
 * TODO: Change frame-based-calculation to time-based-calculation, maybe in bachelor-thesis.
 * <p>
 * Created by Zukie on 15/06/15.
 * <p>
 * @author Zukie
 */
public final class MotionGraph implements IMotionGraph {

    /**
     * Number of Frames to be blended. TODO: set
     */
    public static final int DEFAULT_BLENDING_FRAMES = 100;
    /**
     * Max distance suitable for blending. TODO: set
     */
    public static final double DEFAULT_THRESHOLD = 20;

    /**
     * If all motions should be added normal. TODO: set
     */
    public static final boolean NORMAL = true;
    /**
     * If all Motions should also be added mirrored. TODO: set
     * <p>
     */
    public static final boolean MIRRORED = false;

    /**
     * All Edges of the graph.
     */
    private final List<Edge> edges = new LinkedList<>();
    /**
     * All Nodes of the graph.
     */
    private final List<Node> nodes = new LinkedList<>();
    /**
     * Alignment used to align motions.
     */
    private final IAlignment align;
    /**
     * Blending used to Blend motions.
     */
    private final IBlend blending;
    /**
     * Split used to split motions.
     */
    private final ISplit split;
    /**
     * Equals used to conncat Motion.
     */
    private final IEquals equals;
    /**
     * Metric to calculate distance between Motions.
     */
    private final IDistance metric;

    /**
     * Random-Generator used in {@link #next()}.
     */
    private final Random r = new Random();

    /**
     * Current Node used by {@link #next}.
     */
    private Node currentNode;

    /**
     * Construtor for MotionGraph.
     * <p>
     * Sets align, metric etc. and starts building the motionGraph with calling {@link #init}.
     * <p>
     * @param motions List of motions to use for building the motiongraph.
     * @param align Alignement to use.
     * @param metric Metric to use.
     * @param blending Blending to use.
     * @param split Split to use.
     * @param equals Equal to use.
     */
    public MotionGraph(List<SkeletonInterpolator> motions, IAlignment align, IDistance metric, IBlend blending, ISplit split, IEquals equals) {
        if (motions == null || motions.isEmpty()) {
            throw new IllegalArgumentException("motions null or empty.");
        }
        if (align == null) {
            throw new IllegalArgumentException("No IAlignment specified.");
        }
        if (metric == null) {
            throw new IllegalArgumentException("No IDistance specified.");
        }
        if (blending == null) {
            throw new IllegalArgumentException("No IBlend specified.");
        }
        if (split == null) {
            throw new IllegalArgumentException("No ISplit specified.");
        }

        if (equals == null) {
            throw new IllegalArgumentException("No IEquals specified.");
        }

        this.align = align;
        this.metric = metric;
        this.blending = blending;
        this.split = split;
        this.equals = equals;
        this.init(motions);
    }

    /**
     * Initialise MotionGraph. Creates Edges vor every Motion and mirrors them.
     * <p>
     * @param motions
     */
    private void init(List<SkeletonInterpolator> motions) {

        for (SkeletonInterpolator sp : motions) {

            if (NORMAL) {
                Edge newEdge = new Edge(sp);
                Node startNode = new Node(null, newEdge);
                Node endNode = new Node(newEdge, null);

                nodes.add(startNode);
                nodes.add(endNode);
                edges.add(newEdge);
            }

            // Mirror every motion
            if (MIRRORED) {
                SkeletonInterpolator newSp = new SkeletonInterpolator(sp);
                newSp.mirror();

                Edge mirroredEdge = new Edge(newSp);
                Node mirroredStartNode = new Node(null, mirroredEdge);
                Node mirroredEndNode = new Node(mirroredEdge, null);

                nodes.add(mirroredEndNode);
                nodes.add(mirroredStartNode);
                edges.add(mirroredEdge);
            }

        }

        System.out.println("NODES BEFORE: " + nodes.size());
        System.out.println("EDGES BEFORE: " + edges.size());

        this.connectMotions();
        this.split();
        this.createBlends();
        this.prune();
        System.out.println("NODES AFTER: " + nodes.size());
        System.out.println("EDGES AFTER: " + edges.size());
    }

    /**
     * Randomly splits Motions in the graph. TODO: Create Split-Class.
     */
    private void split() {
        List<Edge> oldEdges = new LinkedList<>(edges);
        for (Edge oldEdge : oldEdges) {

            Node startNode = oldEdge.getStartNode();
            Node endNode = oldEdge.getEndNode();
            removeEdge(oldEdge);

            List<SkeletonInterpolator> splits = split.split(oldEdge.getMotion());

            for (int i = 0; i < splits.size(); i++) {
                SkeletonInterpolator get = splits.get(i);
                Edge newEdge = new Edge(get);
                newEdge.setStartNode(startNode);

                if (i == splits.size() - 1) {
                    startNode = endNode;
                } else {
                    startNode = new Node();
                    this.nodes.add(startNode);
                }
                newEdge.setEndNode(startNode);
                this.edges.add(newEdge);
            }

        }
    }

    /**
     * Remove Edge from MotionGraph and it's nodes.
     * <p/>
     *
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
     * Removes all Nodes, which have no successors.
     */
    private void prune() {

        boolean pruned = true;

        do {
            pruned = true;
            for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                Node node = iterator.next();
                if (!node.hasNext()) {
                    while (!node.getIncomingEdges().isEmpty()) {
                        removeEdge(node.getIncomingEdges().get(0));
                    }

                    pruned = false;
                    iterator.remove();
                    break;
                }
            }
        } while (!pruned);

    }

    /**
     * Returns next motion to be displayed.
     * <p>
     * @return Skeletoninterpolator next.
     */
    @Override
    public SkeletonInterpolator next() {
        if (currentNode == null) {
            this.currentNode = nodes.get(r.nextInt(nodes.size()));
        }

        Edge currentEdge = currentNode.getOutgoingEdges().get(r.nextInt(currentNode.getOutgoingEdges().size()));
        currentEdge.played++;
        if (currentEdge.isBlend()) {
            System.out.println("Edge: " + currentEdge.getId() + " p: " + currentEdge.played + " (blend)");
        } else {
            System.out.println("Edge: " + currentEdge.getId() + " p: " + currentEdge.played);
        }

        SkeletonInterpolator next = currentEdge.getMotion();

        if (currentEdge.getEndNode().hasNext()) {
            currentNode = currentEdge.getEndNode();
            return next;

        } else {
            this.currentNode = null;
            next();
        }

        return null;

    }

    /**
     * Reconnect all Motions that have been cut in xml-format. Will not be needed in final implementation
     */
    private void connectMotions() {
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
    private void createBlends() {
        List<Node> starts = new LinkedList<>();
        for (Node node : nodes) {
            if (!node.getIncomingEdges().isEmpty()) {
                starts.add(node);
            }
        }
        List<Node> ends = new LinkedList<>();
        for (Node node : nodes) {
            if (node.hasNext()) {
                ends.add(node);
            }
        }

        for (Node start : starts) {
            for (Node end : ends) {
                if (start == end) {
                    //motions already connected
                    continue;
                }

                if (start.getIncomingEdges().get(0).getMotion().size() >= DEFAULT_BLENDING_FRAMES
                        && end.getOutgoingEdges().get(0).getMotion().size() >= DEFAULT_BLENDING_FRAMES) {

                    if (metric.distance(start.getIncomingEdges().get(0).getMotion(),
                            end.getOutgoingEdges().get(0).getMotion(), DEFAULT_BLENDING_FRAMES) <= DEFAULT_THRESHOLD) {
                        createBlending(start.getIncomingEdges().get(0), end.getOutgoingEdges().get(0));
                    }
                }
            }

        }
    }

    /**
     * Creates Connection between first and second.
     * <p>
     * If first are longer than {@link #DEFAULT_BLENDING_FRAMES} Frames, first is splittet in two piece, with the
     * blended Part {@link #DEFAULT_BLENDING_FRAMES} long. Else, the Start-Node of first is used of the StartNode of the
     * Blending
     * <p>
     * Same for second, excpet that its End-node is used, if it isn't splitet, as the blending-End-Node.
     * <p>
     * @param first first motion
     * @param second second motion
     */
    private void createBlending(Edge first, Edge second) {
        Node newEnd;
        Node newStart;
        SkeletonInterpolator blendEnd;
        SkeletonInterpolator blendStart;

        //Split first
        if (first.getMotion().size() > DEFAULT_BLENDING_FRAMES) {
            blendStart = first.getMotion().subSkeletonInterpolator(first.getMotion().size() - DEFAULT_BLENDING_FRAMES);
            Edge firstMotionPart2 = new Edge(blendStart);

            SkeletonInterpolator split1 = first.getMotion().subSkeletonInterpolator(0, first.getMotion().size() 
                    - DEFAULT_BLENDING_FRAMES);//could be length 0
            Edge firstMotionPart1 = new Edge(split1);

            first.getStartNode().addOutgoingEdge(firstMotionPart1);
            first.getEndNode().addIncomingEdge(firstMotionPart2);
            newStart = new Node(firstMotionPart1, firstMotionPart2);
            this.removeEdge(first);

            edges.add(firstMotionPart1);
            edges.add(firstMotionPart2);
            nodes.add(newStart);
        } else {
            newStart = first.getStartNode();
            blendStart = first.getMotion();
        }

        //split second
        if (second.getMotion().size() > DEFAULT_BLENDING_FRAMES) {

            blendEnd = second.getMotion().subSkeletonInterpolator(0, DEFAULT_BLENDING_FRAMES);
            Edge secondMotionPart1 = new Edge(blendEnd);

            SkeletonInterpolator split2 = second.getMotion().subSkeletonInterpolator(DEFAULT_BLENDING_FRAMES);//could be length 0
            Edge secondMotionPart2 = new Edge(split2);

            second.getStartNode().addOutgoingEdge(secondMotionPart1);
            second.getEndNode().addIncomingEdge(secondMotionPart2);
            newEnd = new Node(secondMotionPart1, secondMotionPart2);
            this.removeEdge(second);
            edges.add(secondMotionPart1);
            edges.add(secondMotionPart2);
            nodes.add(newEnd);
        } else {
            newEnd = second.getEndNode();
            blendEnd = second.getMotion();
        }

        //create Blend
        SkeletonInterpolator blendedMotion = blending.blend(blendStart, blendEnd, DEFAULT_BLENDING_FRAMES);
        Edge blended = new Edge(blendedMotion);
        blended.setBlend(true);

        newStart.addOutgoingEdge(blended);
        newEnd.addIncomingEdge(blended);

        edges.add(blended);
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
        private ISplit split;
        private IEquals equal;
        private final List<SkeletonInterpolator> motions;

        /**
         * Creates a new Builder for the MotionGraph.
         * <p>
         * @param motions List of Motions to use.
         */
        public Builder(List<SkeletonInterpolator> motions) {
            this.motions = motions;
        }

        /**
         * Returns an new MotionGraph-Instance.
         * <p>
         * @return MotionGraph-Instance
         */
        public MotionGraph getInstance() {
            this.align = align != null ? align : new Alignment();
            this.metric = metric != null ? metric : new JointAngles(align);
            this.blending = blending != null ? blending : new Blend(align);
            this.split = split != null ? split : new DefaultSplit();
            this.equal = equal != null ? equal : new Equals();
            return new MotionGraph(this.motions, this.align, this.metric, this.blending, this.split, this.equal);
        }

        /**
         * Sets Aligment for the MotionGraph.
         * <p>
         * @param align Alignemnt
         * @return Builder
         */
        public Builder align(IAlignment align) {
            this.align = align;
            return this;
        }

        /**
         * Sets Blending for the MotionGraph.
         * <p>
         * @param blending Blending
         * @return Builder
         */
        public Builder blending(IBlend blending) {
            this.blending = blending;
            return this;
        }

        /**
         * Sets Metric for the MotionGraph.
         * <p>
         * @param metric Metric
         * @return Builder
         */
        public Builder metric(IDistance metric) {
            this.metric = metric;
            return this;
        }

        /**
         * Sets Split for the MotionGraph.
         * <p>
         * @param split Split
         * @return Builder
         */
        public Builder split(ISplit split) {
            this.split = split;
            return this;
        }

        /**
         * Sets Equals for the MotionGraph.
         * <p>
         * @param equal Equals
         * @return Builder
         */
        public Builder equal(IEquals equal) {
            this.equal = equal;
            return this;
        }
    }
}
