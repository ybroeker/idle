package asap.realizerdemo.motiongraph;

import asap.realizerdemo.motiongraph.alignment.IAlignment;
import hmi.animation.SkeletonInterpolator;
import java.util.List;

/**
 * Interface for Motiongraphs.
 * <p> 
* @author yannick-broeker
 */
public interface IMotionGraph {

    /**
     * Returns next motion to be displayed.
     * <p>
     * @return Skeletoninterpolator next.
     */
    public abstract SkeletonInterpolator next();

}
