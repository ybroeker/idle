package asap.realizerdemo.motiongraph.movementdetection;

import hmi.animation.SkeletonInterpolator;

/** 
 * Class to detect Movement in Motions.
 * @author yannick-broeker
 */
public interface IMovementDetector {
    /**
     * Detect Stops in {@code motion} around Frame {@code frame}. 
     * @param motion Motion to detect Stops in
     * @param frame Frame to dectet Stops
     * @return if there is no Movement around {@code frame}
     */
    boolean isStopped(SkeletonInterpolator motion, int frame);
    
    /**
     * Gets all Points in {code motion} where isn't any Movement.
     * @param motion Motion to detect Movemet in
     * @return Array of Frames where isn't any movement
     */
    int[] getStops(SkeletonInterpolator motion);
}
