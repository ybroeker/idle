package asap.realizerdemo.motiongraph.metrics;

import hmi.animation.SkeletonInterpolator;

/**
 *
 * @author yannick-broeker
 */
public interface IEquals {
    boolean startEndEquals(SkeletonInterpolator motion1, SkeletonInterpolator motion2);
    
    
}
