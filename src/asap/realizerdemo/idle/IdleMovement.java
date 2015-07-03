package asap.realizerdemo.idle;

import hmi.animation.Hanim;
import hmi.animation.VJoint;
import hmi.math.Quat4f;
import hmi.util.Resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import asap.realizerdemo.motiongraph.alignment.Alignment;
import asap.realizerdemo.motiongraph.alignment.IAlignment;
import asap.realizerdemo.motiongraph.graph1.MotionGraph;
import hmi.animation.ConfigList;
import hmi.animation.SkeletonInterpolator;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IdleMovement implements RestPose {

    private AnimationPlayer aniPlayer;
    private double startTime = 0;
    private static final String wigglyJointId = Hanim.vt1;
    private VJoint restPoseTree; // Holds the pose on a VJoint structure. Joints not in the pose are set to have identity rotation.
    private float amount = 10;
    MotionGraph test;
    SkeletonInterpolator motion;

    @Override
    public RestPose copy(AnimationPlayer player) {
        IdleMovement copy = new IdleMovement(test, motion);
            copy.setAnimationPlayer(player);

        
        return copy;
    }

    @Override
    public void setAnimationPlayer(AnimationPlayer player) {
        this.aniPlayer = player;
        System.out.println(player.getVCurr());
        System.out.println("#####");
        restPoseTree = player.getVCurr().copyTree("rest-");
        System.out.println(restPoseTree);
        System.out.println("#####");
        for (VJoint vj : restPoseTree.getParts()) {
            System.out.println(vj.getSid());
            //System.out.println("#####");
            vj.setRotation(Quat4f.getIdentity());
        }
    }

    @Override
    public void setResource(Resources res) {

    }

    public IdleMovement(MotionGraph test, SkeletonInterpolator motion) {
        this.test = test;
        this.motion = motion;
    }

    public IdleMovement() throws IOException {
        List<SkeletonInterpolator> motions = LoadMotion.loadMotion(new String[]{
            /*"idle_0_10.xml",*/"idle_10_20.xml", "idle_20_30.xml", "idle_30_40.xml", "idle_40_50.xml",
            "idle_50_60.xml", "idle_60_70.xml"
        });

        //AsapRealizerDemo demo = new AsapRealizerDemo();
        //demo.test(null);
        test = new MotionGraph.Builder(motions).align(new Alignment()).getInstance();

        test.split();
        test.createBlends();

        motion = concatMotions(test.randomWalk(), new Alignment());
        System.out.println("MotionsLoaded");
    }

    public SkeletonInterpolator concatMotions(List<SkeletonInterpolator> motions, IAlignment align) {
        double globTime = 0;

        List<SkeletonInterpolator> newMotions = new LinkedList<>();
        newMotions.add(motions.get(0));
        for (int i = 1; i < motions.size(); i++) {
            newMotions.add(align.align(motions.get(i - 1), motions.get(i), 1));
        }

        ConfigList config = new ConfigList(newMotions.get(0).getConfigSize());
        String configType = newMotions.get(0).getConfigType();
        String[] partIds = newMotions.get(0).getPartIds();

        for (SkeletonInterpolator motion : newMotions) {

            double startTime = motion.getStartTime();
            for (int i = 0; i < motion.size(); i++) {
                config.addConfig(motion.getTime(i) - startTime + globTime, motion.getConfig(i));
            }
//            System.out.println("t:" + startTime);
            globTime = motion.getEndTime() - startTime + globTime;
//            System.out.println("gt:" + globTime);

        }

        return new SkeletonInterpolator(partIds, config, configType);

    }

    @Override
    public void play(double time, Set<String> kinematicJoints, Set<String> physicalJoints) {

        System.out.println("#####\n"+time+"\n#####");
        /*
         if (kinematicJoints.contains(wigglyJointId))
         {
         double t = time - startTime;
         VJoint vj = aniPlayer.getVNext().getPart(wigglyJointId);
         float q[] = Quat4f.getQuat4f();
         Quat4f.setFromAxisAngleDegrees(q, 0, 0, 1, (float) Math.sin(t * 10) * amount);
         vj.setRotation(q);
         }
         */
        motion.setTarget(aniPlayer.getVNext());
        motion.time(time);
        /*VJoint vj = aniPlayer.getVNext().getPart("");
         float q[] = Quat4f.getQuat4f();
         Quat4f.setFromAxisAngleDegrees(q, 0, 0, 1, 1);
         vj.setRotation(q);
         */
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
        if (name.equals("amount")) {
            this.amount = Float.parseFloat(value);
        }
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
