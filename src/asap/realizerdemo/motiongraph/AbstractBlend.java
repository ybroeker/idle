package asap.realizerdemo.motiongraph;

import hmi.animation.SkeletonInterpolator;

/**
 * Interface for Blendings.
 * 
 * @author yannick-broeker
 */
public abstract class AbstractBlend {
    
    /**
     * Returns the Blending of {@code frames} between the two Motions.
     * @param first first Motion
     * @param second second Motion
     * @param frames number of Frames to use
     * @return Blending of the two Motions 
     */
    public abstract SkeletonInterpolator blend(SkeletonInterpolator first, SkeletonInterpolator second, int frames);
}
