package asap.realizerdemo.motiongraph;

import static asap.realizerdemo.motiongraph.Util.X;
import static asap.realizerdemo.motiongraph.Util.Y;
import hmi.animation.ConfigList;
import hmi.animation.SkeletonInterpolator;
import hmi.math.Quat4f;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author yannick-broeker
 */
public class LoadMotion {

    public static int test = 0;
    
    public static final float Y_DEFAULT = 0.975211761f;

    public static List<SkeletonInterpolator> loadMotion(String[] files) throws IOException {

        List<SkeletonInterpolator> motions = new LinkedList<>();

        for (String file : files) {
            
            SkeletonInterpolator skeletonInterpolator = SkeletonInterpolator.read("idle", file);

            LoadMotion.fixRootTransformation(skeletonInterpolator);
            System.out.println(file + " rootTransform fixed");

            LoadMotion.fixJoints(skeletonInterpolator);
            System.out.println(file + " Joints fixed");

            motions.add(skeletonInterpolator);
            System.out.println(file + " loaded");

            test++;
        }

        System.out.println("motions loaded");

        return motions;
    }
    
    public static void fixRootTransformation(SkeletonInterpolator motion) {
        ConfigList newConfig = new ConfigList(motion.getConfigSize());
            

        //float zRot = 0;
        for (int i = 0; i < motion.getConfigList().size(); i++) {
            float[] config = motion.getConfig(i);
            double time = motion.getTime(i);

            config[Y] = Y_DEFAULT;
            config[X] = config[X]+test;
            
            float[] quat = {config[3], config[4], config[5], config[6]};

            float[] rollPitchYaw = new float[3];
            Quat4f.getRollPitchYaw(quat, rollPitchYaw);
            //zRot = rollPitchYaw[1]=0;
            Quat4f.setFromRollPitchYaw(quat, rollPitchYaw[0], rollPitchYaw[1], rollPitchYaw[2]);
            config[3] = quat[0];
            config[4] = quat[1];
            config[5] = quat[2];
            config[6] = quat[3];

            newConfig.addConfig(time, config);
        }

        motion.setConfigList(newConfig);
    }

    public static void fixJoints(SkeletonInterpolator motion) {
        Set<String> joints = new java.util.HashSet<String>();
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

    private LoadMotion() {
    }
}
