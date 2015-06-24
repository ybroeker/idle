package asap.realizerdemo.motiongraph;

/**
 *
 * @author yannick-broeker
 */
public class Util {

    public static final String ROOT_TRANSFORM = "T1";
    public static final String ROTATION = "R";
    public static final String TRANSLATION = "T";

    public static final String HUMANOID_ROOT = "HumanoidRoot",
            VL3 = "vl3",
            VT9 = "vt9",
            VC7 = "vc7",
            SKULLBASE = "skullbase",
            L_ACROMIOCLAVICULAR = "l_acromioclavicular",
            L_SHOULDER = "l_shoulder",
            L_ELBOW = "l_elbow",
            L_WRIST = "l_wrist",
            R_ACROMIOCLAVICULAR = "r_acromioclavicular",
            R_SHOULDER = "r_shoulder",
            R_ELBOW = "r_elbow",
            R_WRIST = "r_wrist",
            L_HIP = "l_hip",
            L_KNEE = "l_knee",
            L_ANKLE = "l_ankle",
            R_HIP = "r_hip",
            R_KNEE = "r_knee",
            R_ANKLE = "r_ankle";

    
    public static final String 
            ACROMIOCLAVICULAR = "acromioclavicular",
            SHOULDER = "shoulder",
            ELBOW = "elbow",
            WRIST = "wrist",
            
            HIP = "hip",
            KNEE = "knee",
            ANKLE = "ankle";
            
    
    /**
     * Index of X,Y,Z-RootTransform in ConfigList.
     */
    public static final int Z = 2, Y = 1, X = 0;

    public static double euclidDistance(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("vec1.length != vec2.length");
        }

        double temp = 0;
        for (int i = 0; i < vec1.length; i++) {
            temp += Math.pow(vec1[i] - vec2[i], 2);
        }

        return Math.sqrt(temp);

    }

}
