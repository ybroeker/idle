package asap.realizerdemo.motiongraph.alignment;

import asap.realizerdemo.motiongraph.Util;
import hmi.animation.ConfigList;
import hmi.animation.SkeletonInterpolator;
import hmi.math.Quat4f;

/**
 * Created by Zukie on 26/06/15.
 * TODO: Align y-rotation
 *
 * @author Zukie
 */
public class Alignment implements IAlignment {


    /**
     * {@inheritDoc}
     */
    @Override
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



        float[] quat1 = {firstConfig[Quat4f.S + 3], firstConfig[Quat4f.X + 3], firstConfig[Quat4f.Y + 3], firstConfig[Quat4f.Z + 3]};
        //Quaternion of first motion

        float[] firstRollPitchYaw = new float[3];
        Quat4f.getRollPitchYaw(quat1, firstRollPitchYaw);

        float[] secondConf = {second.getConfig(0)[Quat4f.S + 3], second.getConfig(0)[Quat4f.X + 3],
                second.getConfig(0)[Quat4f.Y + 3], second.getConfig(0)[Quat4f.Z + 3]};
        float[] secondRollPitchYawConf0 = new float[3];
        Quat4f.getRollPitchYaw(secondConf, secondRollPitchYawConf0);

        for (int i = 0; i < second.getConfigList().size(); i++) {
            config = second.getConfig(i).clone();

            // Adjust Translation
            config[Util.X] = config[Util.X] - second.getConfig(0)[Util.X] + firstConfig[Util.X];
            config[Util.Y] = config[Util.Y] - second.getConfig(0)[Util.Y] + firstConfig[Util.Y];
            config[Util.Z] = config[Util.Z] - second.getConfig(0)[Util.Z] + firstConfig[Util.Z];


            //Adjust Rotation

            float[] quat2 = {config[Quat4f.S + 3], config[Quat4f.X + 3], config[Quat4f.Y + 3], config[Quat4f.Z + 3]};
            // Quaterninon of second motion

            float[] secondRollPitchYaw = new float[3];
            Quat4f.getRollPitchYaw(quat2, secondRollPitchYaw);

            secondRollPitchYaw[2] = secondRollPitchYaw[2] - secondRollPitchYawConf0[2] + firstRollPitchYaw[2];

            Quat4f.setFromRollPitchYaw(quat2, secondRollPitchYaw[0], secondRollPitchYaw[1], secondRollPitchYaw[2]);
            config[Quat4f.S + 3] = quat2[Quat4f.S];
            config[Quat4f.X + 3] = quat2[Quat4f.X];
            config[Quat4f.Y + 3] = quat2[Quat4f.Y];
            config[Quat4f.Z + 3] = quat2[Quat4f.Z];

            configList.addConfig(second.getTime(i), config); //Set new config for new SkeletonInterplator

        }
        SkeletonInterpolator newSecond = new SkeletonInterpolator();
        newSecond.setConfigList(configList);
        newSecond.setConfigType(configType);
        newSecond.setPartIds(partIds);
        /*
         (Translation:[0]-[2];Rotation:[3]-[6])
         TODO Rotation anpassen.
         */
        return newSecond;
    }
}
