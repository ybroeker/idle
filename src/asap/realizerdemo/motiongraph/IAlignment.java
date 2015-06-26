package asap.realizerdemo.motiongraph;

import hmi.animation.SkeletonInterpolator;

/**
 * Created by Zukie on 26/06/15.
 */
public interface IAlignment {
    /**
     * Align motion's root positions before blending.
     *
     * @param first  First motion
     * @param second motion to be blended in.
     */
    public SkeletonInterpolator align(SkeletonInterpolator first, SkeletonInterpolator second, int frames);
}
