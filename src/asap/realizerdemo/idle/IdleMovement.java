package asap.realizerdemo.idle;

import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.MUSetupException;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.animationengine.motionunit.TimedAnimationUnit;
import asap.animationengine.restpose.PostureShiftTMU;
import asap.animationengine.restpose.RestPose;
import asap.animationengine.transitions.SlerpTransitionToPoseMU;
import asap.animationengine.transitions.TransitionMU;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.OffsetPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.pegboard.TimePeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedPlanUnitState;
import asap.realizerdemo.motiongraph.LoadMotion;
import static asap.realizerdemo.motiongraph.Util.X;
import static asap.realizerdemo.motiongraph.Util.Z;
import asap.realizerdemo.motiongraph.alignment.IAlignment;
import asap.realizerdemo.motiongraph.graph1.MotionGraph;
import hmi.animation.ConfigList;
import hmi.animation.Hanim;
import hmi.animation.SkeletonInterpolator;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.util.Resources;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * TODO Javadoc.
 * <p>
 * @author yannick-broeker
 */
public class IdleMovement implements RestPose {

    private static final String wigglyJointId = Hanim.vt1;

    private AnimationPlayer aniPlayer;
    private double startTime = 0;
    private VJoint restPoseTree; // Holds the pose on a VJoint structure. Joints not in the pose are set to have identity rotation.
    private MotionGraph motionGraph;
    private SkeletonInterpolator motion;

    public IdleMovement(MotionGraph test, SkeletonInterpolator motion) {
        this.motionGraph = test;
        this.motion = motion;
    }

    public IdleMovement() throws IOException {
        List<SkeletonInterpolator> motions = LoadMotion.loadMotion(new String[]{
            "idle_0_10.xml",
            "idle_10_20.xml",
            "idle_20_30.xml",
            "idle_30_40.xml",
            "idle_40_50.xml",
            "idle_50_60.xml",
            "idle_60_70.xml",
            "1_From500.xml",
            "3_0-530.xml",
            "5.xml",
            "3_1536-2517.xml",
            "4.xml",
            "6.xml",
            "2_0-867.xml",
            "2_1998-2778.xml",
            "2_867-1998.xml",
            "3_530-1536.xml",});

        motionGraph = new MotionGraph.Builder(motions).getInstance();

        motion = alignStart(motionGraph.next(), 0);
    }

    @Override
    public RestPose copy(AnimationPlayer player) {
        IdleMovement copy = new IdleMovement(motionGraph, motion);
        copy.setAnimationPlayer(player);

        return copy;
    }

    @Override
    public void setAnimationPlayer(AnimationPlayer player) {
        this.aniPlayer = player;
        restPoseTree = player.getVCurr().copyTree("rest-");
        for (VJoint vj : restPoseTree.getParts()) {
            vj.setRotation(Quat4f.getIdentity());
        }
    }

    @Override
    public void setResource(Resources res) {

    }

    /**
     * Sets the start-time for the motion.
     * <p>
     * @param motion motion to set time for.
     * @param time start-time
     * @return motion with defined start-time
     */
    private SkeletonInterpolator alignTime(SkeletonInterpolator motion, double time) {
        ConfigList config = new ConfigList(motion.getConfigSize());
        String configType = motion.getConfigType();
        String[] partIds = motion.getPartIds();

        double startTime = motion.getStartTime();
        for (int i = 0; i < motion.size(); i++) {

            config.addConfig(motion.getTime(i) - startTime + time, motion.getConfig(i));
        }

        return new SkeletonInterpolator(partIds, config, configType);
    }

    /**
     * Sets start-time for this motion and sets ist start-root to (0,Y,0).
     * <p>
     * @param motion motion to set time an root for.
     * @param time start-time
     * @return aligned motion
     */
    private SkeletonInterpolator alignStart(SkeletonInterpolator motion, double time) {
        ConfigList config = new ConfigList(motion.getConfigSize());
        String configType = motion.getConfigType();
        String[] partIds = motion.getPartIds();

        float[] config0 = motion.getConfig(0).clone();

        double startTime = motion.getStartTime();
        for (int i = 0; i < motion.size(); i++) {

            motion.getConfig(i)[X] = motion.getConfig(i)[X] - config0[X] + 0;
            motion.getConfig(i)[Z] = motion.getConfig(i)[Z] - config0[Z] + 0;
            config.addConfig(motion.getTime(i) - startTime + time, motion.getConfig(i));
        }

        return new SkeletonInterpolator(partIds, config, configType);
    }

    /**
     * Aligns newMotion on motion. Sets start-time of newMotion to time and sets root of newMotion to last root of
     * motion.
     * <p>
     * @param motion motion to align newMotion on
     * @param newMotion motion to be aligned
     * @param align Alignment to use
     * @param time time to set as start-time
     * @return aligned motion.
     */
    private SkeletonInterpolator alignMotions(SkeletonInterpolator motion, SkeletonInterpolator newMotion, IAlignment align, double time) {
        newMotion = align.align(motion, newMotion, 1);
        return alignTime(newMotion, time);
    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints) {
        if (time > motion.getEndTime()) {
            SkeletonInterpolator next = motionGraph.next();
            motion = alignMotions(motion, next, motionGraph.getAlign(), time);
        }

        motion.setTarget(aniPlayer.getVNext());
        motion.time(time);
    }

    @Override
    public TimedAnimationUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, String bmlId, String id,
            BMLBlockPeg bmlBlockPeg, PegBoard pb) {
        return createTransitionToRest(fbm, joints, startTime, 1, bmlId, id, bmlBlockPeg, pb);
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, double startTime, double duration,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb) {
        TimePeg startPeg = new TimePeg(bmlBlockPeg);
        startPeg.setGlobalValue(startTime);
        TimePeg endPeg = new OffsetPeg(startPeg, duration);
        return createTransitionToRest(fbm, joints, startPeg, endPeg, bmlId, id, bmlBlockPeg, pb);
    }

    @Override
    public TimedAnimationMotionUnit createTransitionToRest(FeedbackManager fbm, Set<String> joints, TimePeg startPeg, TimePeg endPeg,
            String bmlId, String id, BMLBlockPeg bmlBlockPeg, PegBoard pb) {
        TransitionMU mu = createTransitionToRest(joints);
        mu.addKeyPosition(new KeyPosition("start", 0));
        mu.addKeyPosition(new KeyPosition("end", 1));
        TimedAnimationMotionUnit tmu = new TimedAnimationMotionUnit(fbm, bmlBlockPeg, bmlId, id, mu, pb, aniPlayer);
        tmu.setTimePeg("start", startPeg);
        tmu.setTimePeg("end", endPeg);
        tmu.setState(TimedPlanUnitState.LURKING);
        return tmu;
    }

    @Override
    public double getTransitionToRestDuration(VJoint vCurrent, Set<String> joints) {
        return 1;
    }

    @Override
    public TransitionMU createTransitionToRest(Set<String> joints) {

        System.out.println("126 joints: " + joints);

        float rotations[] = new float[joints.size() * 4];
        int i = 0;
        List<VJoint> targetJoints = new ArrayList<VJoint>();
        List<VJoint> startJoints = new ArrayList<VJoint>();
        for (String joint : joints) {
            VJoint vj = restPoseTree.getPartBySid(joint);
            vj.getRotation(rotations, i);
            targetJoints.add(aniPlayer.getVNextPartBySid(joint));
            startJoints.add(aniPlayer.getVCurrPartBySid(joint));
            i += 4;
        }
        TransitionMU mu = new SlerpTransitionToPoseMU(targetJoints, startJoints, rotations);
        mu.setStartPose();
        return mu;
    }

    @Override
    public void startRestPose(double time) {
        startTime = time;
    }

    @Override
    public void setParameterValue(String name, String value) throws ParameterException {
    }

    @Override
    public PostureShiftTMU createPostureShiftTMU(FeedbackManager bbf, BMLBlockPeg bmlBlockPeg, String bmlId, String id, PegBoard pb)
            throws MUSetupException {
        List<VJoint> targetJoints = new ArrayList<VJoint>();
        List<VJoint> startJoints = new ArrayList<VJoint>();
        targetJoints.add(aniPlayer.getVNextPartBySid(wigglyJointId));
        startJoints.add(aniPlayer.getVCurrPartBySid(wigglyJointId));
        AnimationUnit mu = new SlerpTransitionToPoseMU(startJoints, targetJoints, Quat4f.getIdentity());
        return new PostureShiftTMU(bbf, bmlBlockPeg, bmlId, id, mu.copy(aniPlayer), pb, this, aniPlayer);
    }

}
