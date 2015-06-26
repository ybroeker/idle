package asap.realizerdemo.motiongraph;

import hmi.animation.SkeletonInterpolator;

/**
 * Interface for Blendings.
 * 
 * @author yannick-broeker
 */
public interface IBlend {
    
    /**
     * Returns the Blending of {@code frames} between the two Motions.
     * @param start first Motion
     * @param end second Motion
     * @param frames number of Frames to use
     * @return Blending of the two Motions 
     */
    SkeletonInterpolator blend(SkeletonInterpolator first, SkeletonInterpolator second, int frames);
}
