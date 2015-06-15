package asap.realizerdemo.motiongraph.movementdetection;

import asap.realizerdemo.motiongraph.Util;
import hmi.animation.SkeletonInterpolator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author yannick-broeker
 */
public class MovementDetector implements IMovementDetector {

    /**
     * Number of Frames to use for Motiondetection.
     */
    public static final int FRAMES = 30;

    public static final double THRESHOLD = 0.001;

    @Override
    public boolean isStopped(SkeletonInterpolator motion, int frame) {
        if (!((frame - FRAMES/2)>0 && (frame - FRAMES/2 + FRAMES+1)<=motion.size())) {
            //frame zu nah an Anfang/Ende
            return false;
        }

        double tmp = 0;
        for (int i = 0; i < FRAMES; i++) {
            tmp += Util.euclidDistance(motion.getConfig(frame - FRAMES/2 +i), motion.getConfig(frame - FRAMES/2 + i+1));
        }
        
        return (tmp<THRESHOLD);
    }

    @Override
    public int[] getStops(SkeletonInterpolator motion) {
        System.out.println("");
        List<Integer> stops = new LinkedList<Integer>();
        for (int i = 0; i < motion.size(); i++) {
            if (isStopped(motion, i)) {
                stops.add(i);
            }
        }
        
        int[] ret = new int[stops.size()];
        for (int i = 0; i < stops.size(); i++) {
            ret[i] = stops.get(i);
        }
        
        return ret;
        
    }

}
