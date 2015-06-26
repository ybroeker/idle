package asap.realizerdemo.motiongraph;

import hmi.animation.ConfigList;
import hmi.animation.SkeletonInterpolator;

/**
 * Created by Zukie on 26/06/15.
 */
public class Alignment implements IAlignment {

    /**
     * Align motion's root positions before blending.
     * <p>
     * @param first First motion
     * @param second motion to be blended in.
     */
    public SkeletonInterpolator align(SkeletonInterpolator first, SkeletonInterpolator second, int frames) {

        float[] config;
        ConfigList configList = new ConfigList(second.getConfigSize());
        String configType = second.getConfigType();
        String[] partIds = second.getPartIds().clone();

        for (int i = 0; i < partIds.length; i++) {
            partIds[i] = second.getPartIds()[i];
        } // copy second.partIds

        String[] firstPartIds = first.getPartIds();
        float[] firstConfig = first.getConfig(first.size() - frames); // Frame where blending starts

        SkeletonInterpolator newSecond = new SkeletonInterpolator();
        newSecond.setConfigType(configType);
        newSecond.setPartIds(partIds);
        for (int i = 0; i < second.getConfigList().size(); i++) {
            config = second.getConfig(i).clone();

            // Adjust Translation
            config[Util.X] = config[Util.X] - second.getConfig(0)[Util.X] + firstConfig[Util.X];
            config[Util.Y] = config[Util.Y] - second.getConfig(0)[Util.Y] + firstConfig[Util.Y];
            config[Util.Z] = config[Util.Z] - second.getConfig(0)[Util.Z] + firstConfig[Util.Z];

            newSecond.getConfigList().addConfig(second.getTime(i), config); //Set new config for new SkeletonInterplator

        }

        /*
         (Translation:[0]-[2];Rotation:[3]-[6])
         TODO Rotation anpassen.
         */
        return newSecond;
    }
}
