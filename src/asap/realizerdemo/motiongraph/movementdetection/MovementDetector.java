package asap.realizerdemo.motiongraph.movementdetection;

import asap.realizerdemo.motiongraph.Util;
import static asap.realizerdemo.motiongraph.Util.X;
import static asap.realizerdemo.motiongraph.Util.Y;
import static asap.realizerdemo.motiongraph.Util.Z;
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

    public static final double THRESHOLD = 0.0002;

    @Override
    public boolean isStopped(SkeletonInterpolator motion, int frame) {
        if (!((frame - FRAMES / 2) > 0 && (frame - FRAMES / 2 + FRAMES + 1) <= motion.size())) {

            return false;//frame zu nah an Anfang/Ende
        }

        double tmp = 0;

        double res=0; 

        double squaredTransformX = 0, squaredTransformY = 0, squaredTransformZ = 0;

        for (int i = 0; i < FRAMES; i++) {
            int index = 0;
            if (motion.getConfigType().contains(Util.ROOT_TRANSFORM)) {
                squaredTransformX += Math.pow(motion.getConfig(frame - FRAMES / 2 + i)[X] - motion.getConfig(frame - FRAMES / 2 + i + 1)[X], 2);
                squaredTransformY += Math.pow(motion.getConfig(frame - FRAMES / 2 + i)[Y] - motion.getConfig(frame - FRAMES / 2 + i + 1)[Y], 2);
                squaredTransformZ += Math.pow(motion.getConfig(frame - FRAMES / 2 + i)[Z] - motion.getConfig(frame - FRAMES / 2 + i + 1)[Z], 2);
                index+=3;
            }

            int parts = motion.getPartIds().length;
            for (int part = 0; part < parts; part++) {
                tmp += Math.pow(motion.getConfig(frame - FRAMES / 2 + i)[index+part*4] - motion.getConfig(frame - FRAMES / 2 + i + 1)[index+part*4], 2);
                tmp += Math.pow(motion.getConfig(frame - FRAMES / 2 + i)[index+part*4+1] - motion.getConfig(frame - FRAMES / 2 + i + 1)[index+part*4+1], 2);
                tmp += Math.pow(motion.getConfig(frame - FRAMES / 2 + i)[index+part*4+2] - motion.getConfig(frame - FRAMES / 2 + i + 1)[index+part*4+2], 2);
                tmp += Math.pow(motion.getConfig(frame - FRAMES / 2 + i)[index+part*4+3] - motion.getConfig(frame - FRAMES / 2 + i + 1)[index+part*4+3], 2);
            }
            tmp += squaredTransformX + squaredTransformY + squaredTransformZ;
            tmp /= parts+1;
            res+=tmp;
        }

        
        
        
        //ySstem.out.println(frame + ": " + tmp);

        return (tmp < THRESHOLD);
    }

    @Override
    public int[] getStops(SkeletonInterpolator motion) {
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
