package asap.realizerdemo.motiongraph;

import hmi.animation.SkeletonInterpolator;
import java.util.List;

/**
 * Abstract class for Motiongraphs.
 *
 * @author yannick-broeker
 */
public abstract class AbstractMotionGraph {

    /**
     * Constructs a new MotionGraph.
     *
     * @param motions Start-Motions for the Motiongraph
     */
    public AbstractMotionGraph(List<SkeletonInterpolator> motions) {

    }

    /**
     * returns a random-Walk through the Graph.
     *
     * @return random-Walk through the Graph
     */
    public abstract List<SkeletonInterpolator> randomWalk();

    /**
     * returns a random-walk with the given lenght through the Graph.
     *
     * @param lenght lenght for the random-walk
     * @return random-walk through the Graph
     */
    public abstract List<SkeletonInterpolator> randomWalk(int lenght);
}
