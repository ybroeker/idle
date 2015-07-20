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
     * Returns next motion to be displayed.
     * <p>
     * @return Skeletoninterpolator next.
     */
    public abstract SkeletonInterpolator next();



}
