package asap.realizerdemo.motiongraph;

/**
 *
 * @author yannick-broeker
 */
public class Util {
    
    public static final String ROOT_TRANSFORM = "T1";
    public static final String ROTATION = "R";
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
