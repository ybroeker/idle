package asap.realizerdemo.motiongraph.alignment;

import hmi.animation.SkeletonInterpolator;

/**
 * N-OP-Alignement for testing, does nothing.
 * <p>
 * Created by Zukie on 03/07/15.
 */
public class NopAlignment implements IAlignment {

    /**
     * Does nothing, just returns second.
     * @param first
     * @param second
     * @param frames
     * @return 
     */
    @Override
    public SkeletonInterpolator align(SkeletonInterpolator first, SkeletonInterpolator second, int frames) {
        return second;
    }
}
