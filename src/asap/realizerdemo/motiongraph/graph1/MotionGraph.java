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

    public MotionGraph(List<SkeletonInterpolator> motions) {
        super(motions);

        int i = 0;

        for (SkeletonInterpolator sp : motions) {
            Edge newEdge = new Edge(sp);
            Node startNode = new Node(null, newEdge);
            Node endNode = new Node(newEdge, null);
            edges.add(newEdge);

            System.out.println("Motion Nr." + i + newEdge.toString() );
            i++; 
        }

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
