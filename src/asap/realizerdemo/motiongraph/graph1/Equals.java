package asap.realizerdemo.motiongraph.graph1;

import asap.realizerdemo.motiongraph.IEquals;
import hmi.animation.SkeletonInterpolator;

/**
 *
 * @author yannick-broeker
 */
public class Equals implements IEquals {

    /**
     * Compares last frame of first and first frame of second.
     *
     * @param first first Motion
     * @param second second motion
     * @return true, if last frame of first an first frame of second equal.
     */
    @Override
    public boolean startEndEquals(SkeletonInterpolator start, SkeletonInterpolator end) {
        float[] endConfig = end.getConfig(0);

        float[] startConfig = start.getConfig(start.getConfigList().size() - 1);

        if (!start.getConfigType().equals(end.getConfigType())) {
            return false;//erstmal nur genau gleiche, besser transform ignorieren
        }

        if (startConfig.length != endConfig.length) {
            return false;//erstmal nur genau gleiche, besser transform ignorieren
        }

        //transform ignorieren, reihenfolge der Joints bisher ignoriert
        for (int i = 3; i < startConfig.length; i++) {
            if (startConfig[i] != endConfig[i]) {
                return false;
            }
        }

        return true;

    }

}
