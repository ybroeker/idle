package asap.realizerdemo.motiongraph;

import static asap.realizerdemo.motiongraph.Util.X;
import static asap.realizerdemo.motiongraph.Util.Y;
import static asap.realizerdemo.motiongraph.Util.Z;
import hmi.animation.ConfigList;
import hmi.animation.SkeletonInterpolator;
import hmi.math.Quat4f;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Util-Class to load mocap-files.
 * <p>
 * @author yannick-broeker
 */
public final class LoadMotion {

    /**
     * Y-Height for root.
     */
    public static final float Y_DEFAULT = 0.975211761f;

    /**
     * Loads a list of mocap-files.
     * @param files array of filenames
     * @return List of the loaded motions
     * @throws IOException if motion cant be read
     */
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
        }

        System.out.println("motions loaded");

        return motions;
    }

    /**
     * sets root position to (0,Y_DEFAULT,0).
     * <p>
     * @param motion
     */
    public static void fixRootTransformation(SkeletonInterpolator motion) {
        ConfigList newConfig = new ConfigList(motion.getConfigSize());

        float[] config0 = motion.getConfig(0).clone();

        float[] quat1 = {config0[Quat4f.S + 3], config0[Quat4f.X + 3], config0[Quat4f.Y + 3], config0[Quat4f.Z + 3]};
        //Quaternion of first motion

        float[] firstRollPitchYaw = new float[3];
        Quat4f.getRollPitchYaw(quat1, firstRollPitchYaw);

        //float zRot = 0;
        for (int i = 0; i < motion.getConfigList().size(); i++) {
            float[] config = motion.getConfig(i);
            double time = motion.getTime(i);

            config[Y] = config[Y] - config0[Y] + Y_DEFAULT;
            config[X] = config[X] - config0[X] + 0;
            config[Z] = config[Z] - config0[Z] + 0;

            float[] quat = {config[Quat4f.S + 3], config[Quat4f.X + 3], config[Quat4f.Y + 3], config[Quat4f.Z + 3]};

            float[] rollPitchYaw = new float[3];
            Quat4f.getRollPitchYaw(quat, rollPitchYaw);

            //zRot = rollPitchYaw[1]=0;
            Quat4f.setFromRollPitchYaw(quat, rollPitchYaw[0], rollPitchYaw[1], rollPitchYaw[2] - firstRollPitchYaw[2]);
            config[Quat4f.S + 3] = quat[Quat4f.S];
            config[Quat4f.X + 3] = quat[Quat4f.X];
            config[Quat4f.Y + 3] = quat[Quat4f.Y];
            config[Quat4f.Z + 3] = quat[Quat4f.Z];

            newConfig.addConfig(time, config);
        }

        motion.setConfigList(newConfig);
    }

    /**
     * Filters joints which cant be processed.
     * <p>
     * @param motion
     */
    public static void fixJoints(SkeletonInterpolator motion) {
        String[] partIds = motion.getPartIds();

        for (int i = 0; i < partIds.length; i++) {
            switch (partIds[i]) {
                case "vl3":
                    partIds[i] = "vt10";
                    break;
                case "vt9":
                    partIds[i] = "vt6";
                    break;
                case "vc7":
                    partIds[i] = "vt1";
                    break;
                case "l_acromioclavicular":
                    partIds[i] = "l_sternoclavicular";
                    break;
                case "r_acromioclavicular":
                    partIds[i] = "r_sternoclavicular";
                    break;
                default:
            }
        }
    }

    private LoadMotion() {
    }
}
