package asap.realizerdemo.motiongraph;

import asap.realizerdemo.motiongraph.alignment.IAlignment;
import hmi.animation.SkeletonInterpolator;
import java.util.List;

/**
 * Abstract class for Motiongraphs.
 *TODO: Edge next() statt randomwalk
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
     * //TODO: remove
     * Constructor for using different aligning methods.
     * @param motions
     * @param align
     */
    public AbstractMotionGraph(List<SkeletonInterpolator> motions, IAlignment align){}

    /**
     * returns a random-Walk through the Graph.
     *
     * @return random-Walk through the Graph
     */
   // public abstract List<SkeletonInterpolator> randomWalk();

    /**
     * returns a random-walk with the given lenght through the Graph.
     *
     * @param lenght lenght for the random-walk
     * @return random-walk through the Graph
     */
  //  public abstract List<SkeletonInterpolator> randomWalk(int lenght);

    public abstract SkeletonInterpolator next();



}
