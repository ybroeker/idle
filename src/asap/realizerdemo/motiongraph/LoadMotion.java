package asap.realizerdemo.motiongraph;

import hmi.animation.ConfigList;
import hmi.animation.SkeletonInterpolator;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author yannick-broeker
 */
public class LoadMotion {

    /**
     * Index of X,Y,Z-RootTransform in ConfigList.
     */
    public static final int Z = 2, Y = 1, X = 0;

    public static List<SkeletonInterpolator> loadMotion(String[] files) throws IOException {

        List<SkeletonInterpolator> motions = new LinkedList<SkeletonInterpolator>();

        for (String file : files) {
            System.out.println(file);
            SkeletonInterpolator skeletonInterpolator = SkeletonInterpolator.read("idle", file);
            motions.add(skeletonInterpolator);

        }
        return motions;
    }

    public static void fixRootTransformation(SkeletonInterpolator motion) {

        float x = 0, z = 0;

        ConfigList newConfig = new ConfigList(motion.getConfigSize());

        x = motion.getConfig(0)[X];
        z = motion.getConfig(0)[Z];

        for (int i = 0; i < motion.getConfigList().size(); i++) {
            float[] config = motion.getConfig(i);
            double time = motion.getTime(i);

            config[Y] = 1;
            config[X] = config[X] - x;
            config[Z] = config[Z] - z;

            newConfig.addConfig(time, config);
        }

        motion.setConfigList(newConfig);
    }

    public static void fixJoints(SkeletonInterpolator motion) {
        Set<String> joints = new java.util.HashSet<>();
        for (String partId : motion.getPartIds()) {
            joints.add(partId);
        }
        joints.remove("vl3");
        joints.remove("vt9");
        joints.remove("vc7");
        joints.remove("l_acromioclavicular");
        joints.remove("r_acromioclavicular");

        motion.filterJoints(joints);
    }
}
