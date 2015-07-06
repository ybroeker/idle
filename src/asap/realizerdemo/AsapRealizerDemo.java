package asap.realizerdemo;

import asap.realizerdemo.motiongraph.alignment.IAlignment;
import asap.realizerdemo.motiongraph.alignment.Alignment;
import asap.realizerdemo.motiongraph.alignment.NopAlignment;
import asap.realizerdemo.motiongraph.metrics.IEquals;
import asap.realizerdemo.motiongraph.blending.IBlend;
import asap.realizerdemo.motiongraph.metrics.IDistance;
import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.realizerdemo.motiongraph.*;

import static asap.realizerdemo.motiongraph.Util.X;
import static asap.realizerdemo.motiongraph.Util.Y;
import static asap.realizerdemo.motiongraph.Util.Z;
import asap.realizerdemo.motiongraph.metrics.Equals;
import asap.realizerdemo.motiongraph.graph1.MotionGraph;
import asap.realizerdemo.motiongraph.metrics.JointAngles;
import asap.realizerdemo.motiongraph.movementdetection.IMovementDetector;
import asap.realizerdemo.motiongraph.movementdetection.MovementDetector;
import asap.realizerport.RealizerPort;
import hmi.animation.ConfigList;
import hmi.animation.Skeleton;
import hmi.animation.SkeletonInterpolator;
import hmi.animation.VJoint;
import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.Environment;
import hmi.jcomponentenvironment.JComponentEnvironment;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.util.Console;
import hmi.worldobjectenvironment.VJointWorldObject;
import hmi.worldobjectenvironment.WorldObjectEnvironment;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import saiba.bml.BMLInfo;
import saiba.bml.core.FaceLexemeBehaviour;
import saiba.bml.core.HeadBehaviour;
import saiba.bml.core.PostureShiftBehaviour;

/**
 * Simple demo for the AsapRealizer+environment
 * <p>
 * @author hvanwelbergen
 * <p>
 */
public class AsapRealizerDemo {

    private final HmiRenderEnvironment hre;
    private final OdePhysicsEnvironment ope;

    private VJoint sphereJoint;
    protected JFrame mainJFrame = null;

    private static int id = 0;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AsapRealizerDemo.class);

    /**
     * Für Testzwecke...
     */
    AsapVirtualHuman avh;

    public AsapRealizerDemo(JFrame j, String spec) throws IOException {
        Console.setEnabled(false);
        System.setProperty("sun.java2d.noddraw", "true"); /* avoid potential
         interference with (non-Jogl) Java using direct draw*/

        mainJFrame = j;

        BMLTInfo.init();
        BMLInfo.addCustomFloatAttribute(FaceLexemeBehaviour.class, "http://asap-project.org/convanim", "repetition");
        BMLInfo.addCustomStringAttribute(HeadBehaviour.class, "http://asap-project.org/convanim", "spindirection");
        BMLInfo.addCustomFloatAttribute(PostureShiftBehaviour.class, "http://asap-project.org/convanim", "amount");

        hre = new HmiRenderEnvironment() {
            @Override
            protected void renderTime(double currentTime) {
                super.renderTime(currentTime);
                /*
                 * double speed = 1;
                 * if (sphereJoint != null) sphereJoint.setTranslation(0.75f + (float) Math.sin(currentTime * speed) * 1.5f,
                 * (float) Math.cos(currentTime * speed) * 1f + 1.5f, 0.5f);
                 */
            }
        };

        ope = new OdePhysicsEnvironment();
        WorldObjectEnvironment we = new WorldObjectEnvironment();
        MixedAnimationEnvironment mae = new MixedAnimationEnvironment();
        final AsapEnvironment ee = new AsapEnvironment();
        AudioEnvironment aue = new AudioEnvironment("LJWGL_JOAL");

        final JComponentEnvironment jce = setupJComponentEnvironment();

        hre.init(); // canvas does not exist until init was called
        we.init();
        ope.init();
        aue.init();
        mae.init(ope);

        ArrayList<Environment> environments = new ArrayList<Environment>();
        environments.add(hre);
        environments.add(we);
        environments.add(ope);
        environments.add(mae);
        environments.add(ee);
        environments.add(aue);
        environments.add(jce);

        ee.init(environments, ope.getPhysicsClock()); // if no physics, just use renderclock here!
        // ee.init(environments, hre.getRenderClock()); // if no physics, just use renderclock here!

        // this clock method drives the engines in ee. if no physics, then register ee as a listener at the render clock!
        ope.addPrePhysicsCopyListener(ee);
        // hre.getRenderClock().addClockListener(ee);

        hre.loadCheckerBoardGround("ground", 0.5f, 0f);
        hre.setBackground(0.2f, 0.2f, 0.2f);

        hre.loadBox("bluebox", new float[] { 0.05f, 0.05f, 0.05f }, HmiRenderEnvironment.RenderStyle.FILL, new float[] { 0.2f, 0.2f, 1, 1 }, new float[] { 0.2f,
                0.2f, 1, 1 }, new float[] { 0.2f, 0.2f, 1, 0 }, new float[] { 0.2f, 0.2f, 1, 1 });
        VJoint boxJoint = hre.getObjectRootJoint("bluebox");
        boxJoint.setTranslation(0.1f, 1.5f, 0.4f);
        we.getWorldObjectManager().addWorldObject("bluebox", new VJointWorldObject(boxJoint));
        
        // set camera position
        // hre.setNavigationEnabled(false);
        hre.setViewPoint(new float[]{0, 1, 2});
        avh = ee.loadVirtualHuman("", spec, "AsapRealizer demo 2");

        j.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                // ee.requestShutdown();
                // while(!ee.isShutdown()){}
                System.exit(0);
            }
        });

        mainJFrame.setSize(1000, 600);

        java.awt.Component canvas = hre.getAWTComponent(); // after init, get canvas and add to window
        mainJFrame.add(canvas, BorderLayout.CENTER);
        mainJFrame.setVisible(true);

    }

    public AsapRealizerDemo() {
        hre = null;
        ope = null;
    }

    private void test(final RealizerPort realizerPort) throws IOException {

        IMovementDetector movementDetector = new MovementDetector();

        List<SkeletonInterpolator> motions = LoadMotion.loadMotion(new String[]{
            //"y_headmove_Ses01F_impro01_F011.xml",
            //"y_headmove_Ses01M_impro05_M031.xml", 
            //"y_headmove_Ses03F_impro02_F016.xml",
            // "y_headmove_Ses05M_script01_1_M027.xml",
            //"idle_0_0.99.xml",
            "idle_0_10.xml", "idle_10_20.xml", "idle_20_30.xml", "idle_30_40.xml", "idle_40_50.xml", "idle_50_60.xml", "idle_60_70.xml"

        });

        IEquals equals = new Equals();

//        System.out.println("0+1:" + equals.startEndEquals(motions.get(0), motions.get(1)));

        /*
         int[] stops = movementDetector.getStops(motion);

        
         int i = 0;
         int max = 0;
         for (int j = 1; j < stops.length; j++) {
         System.out.println(stops[j]);
         if (stops[j]-stops[j-1] > max) {
         max=stops[j]-stops[j-1];
         i=j-1;
         }
         }
        
         System.out.println("stops.lenght="+stops.length+" i="+i);
         if (stops.length>i+1) {
         System.out.println(stops[i] + "-" + stops[i + 1]);
         }
         

         final SkeletonInterpolator skeletonInterpolator = motion.subSkeletonInterpolator(stops[i], stops[i + 1]);
         */
        if (realizerPort != null) {
            final SkeletonInterpolator motion = motions.get(0);
            play(motion, realizerPort);
        }
        //realizerPort.addListener(...);
    }

    private JComponentEnvironment setupJComponentEnvironment() {
        final JComponentEnvironment jce = new JComponentEnvironment();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    mainJFrame.setLayout(new BorderLayout());

                    JPanel jPanel = new JPanel();
                    jPanel.setPreferredSize(new Dimension(400, 40));
                    jPanel.setLayout(new GridLayout(1, 1));
                    jce.registerComponent("textpanel", jPanel);
                    mainJFrame.add(jPanel, BorderLayout.SOUTH);
                }
            });
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return jce;
    }

    public void startClocks() {
        hre.startRenderClock();
        ope.startPhysicsClock();
    }

    public static void main(String[] args) throws IOException {
        // examples for conversational animation seminar:
        // String spec = "asaparmandia_motionsamples.xml";
        String spec = "asaparmandia_motionsamples.xml";//"asaparmandia_y_test.xml";
        if (args.length == 1) {
            spec = args[0];
        }

        AsapRealizerDemo demo = new AsapRealizerDemo(new JFrame("AsapRealizer demo 1"), spec);
        demo.startClocks();
        //demo.test(demo.avh.getRealizerPort());

       // List<SkeletonInterpolator> motions = LoadMotion.loadMotion(new String[]{
       //     /*"idle_0_10.xml",*/"idle_10_20.xml", "idle_20_30.xml", "idle_30_40.xml", "idle_40_50.xml",
       //     "idle_50_60.xml", "idle_60_70.xml"
       // });
/*
        //AsapRealizerDemo demo = new AsapRealizerDemo();
        //demo.test(null);
       // MotionGraph test = new MotionGraph.Builder(motions).align(new Alignment()).getInstance();

        MotionGraph test = new MotionGraph(motions);
        test.split();
        test.createBlends();
        demo.play(test, demo.avh.getRealizerPort());*/
        //SkeletonInterpolator newS =  test.getAlign().align(motions.get(0), motions.get(1), motions.get(0).size()-1);
        //motions.remove(1);
        //motions.add(newS);

//        demo.play(motions.get(1),demo.avh.getRealizerPort());
        //demo.play(demo.testBlending(motions.get(0),motions.get(1), new Blend(), new Alignment()),demo.avh.getRealizerPort());
        //demo.play(demo.concatMotions(motions),demo.avh.getRealizerPort());
        //MotionGraph test = new MotionGraph(motions);
        //test.split();
        //System.out.println(test);
        //demo.play(demo.concatMotions(test.randomWalk()), demo.avh.getRealizerPort());
        //demo.testDistance(new JointAngles(new Alignment()), motions.get(0));

        // motions = test.randomWalk();
        //demo.testStopping(motions);
        //demo.play(demo.defaultPose(motions.get(0)), demo.avh.getRealizerPort());
        //demo.play(demo.concatMotions(demo.testStopping(motions)), demo.avh.getRealizerPort());
        ///demo.play(demo.concatMotions(motions), demo.avh.getRealizerPort());
        //SkeletonInterpolator skeletonInterpolator = motions.get(3);
        //skeletonInterpolator.mirror();
        //IEquals equals = new Equals();
        //System.out.println("e: "+equals.startEndEquals(skeletonInterpolator, motions.get(4)));
    }

    public SkeletonInterpolator defaultPose(SkeletonInterpolator skeleton) {
        ConfigList configList = new ConfigList(skeleton.getConfigSize());
        String type = skeleton.getConfigType();
        String[] partIds = skeleton.getPartIds();

        float[] config = new float[skeleton.getConfigSize()];
        for (int i = 0; i < skeleton.getConfigSize(); i++) {
            config[i] = 0;
        }
        config[X] = skeleton.getConfig(0)[X];
        config[Y] = skeleton.getConfig(0)[Y];
        config[Z] = skeleton.getConfig(0)[Z];

        configList.addConfig(0, config);

        return new SkeletonInterpolator(partIds, configList, type);
    }

    public void testDistance(IDistance distanceMetric, SkeletonInterpolator skeletonInterpolator) {

        int bound = skeletonInterpolator.size() / 4;
        int splitPoint = new Random().nextInt(skeletonInterpolator.size() - bound * 2) + bound;

        SkeletonInterpolator start = skeletonInterpolator.subSkeletonInterpolator(0, splitPoint);
        SkeletonInterpolator end = skeletonInterpolator.subSkeletonInterpolator(splitPoint - bound / 2);

//        System.out.printf("frames: %3d\n",bound / 2);
/*
         for (int i = 0; i <= bound / 2; i++) {
         System.out.printf("i: %3d d: %.7f\n",i, distanceMetric.distance(start, end, i));
         }*/
    }

    /**
     * Verknüpft eine Liste von Motions zu einer einzelnen. TODO: Transform anpassen.
     * <p>
     * @param motions
     * @return
     */
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

    public void play(final SkeletonInterpolator skeletonInterpolator, final RealizerPort realizerPort) {
        JFrame frame = new JFrame();
        JButton button = new JButton();
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                id++;
                realizerPort.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"  id=\"bml" + id + "\" xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\">   \n"
                        + "   <bmlt:keyframe id=\"kf" + id + "\">"
                        + skeletonInterpolator.toXMLString()
                        + "</bmlt:keyframe>   \n</bml>");
            }
        });

        // frame.pack();
        frame.add(button);
        frame.setVisible(true);

    }

    public void play(final MotionGraph motionGraph, final RealizerPort realizerPort) {
        JFrame frame = new JFrame();
        JButton button = new JButton("Random Walk");
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                id++;
//                System.out.println("TEST");
                realizerPort.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"  id=\"bml" + id + "\" xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\">   \n"
                        + "   <bmlt:keyframe id=\"kf" + id + "\">"
                        + concatMotions(motionGraph.randomWalk(), motionGraph.getAlign()).toXMLString()
                        + "</bmlt:keyframe>   \n</bml>");
            }
        });

        // frame.pack();
        frame.setSize(50, 50);
        frame.add(button);
        frame.setVisible(true);

    }

    public List<SkeletonInterpolator> testStopping(List<SkeletonInterpolator> motions) {
        IMovementDetector movementDetector = new MovementDetector();

        List<SkeletonInterpolator> stopMotions = new LinkedList<>();

        for (SkeletonInterpolator motion : motions) {
//            System.out.println("motion:");
            movementDetector.getStops(motion);

            int[] stops = movementDetector.getStops(motion);

            int i = 0;
            int y = 0;
            int max = 0;
            for (int j = 1; j < stops.length; j++) {
                if (stops[j] - stops[j - 1] > max) {
                    max = stops[j] - stops[j - 1];
                    i = j - 1;
                }

                if (stops[j] - stops[j - 1] == 1) {

                } else {
                    if (j - y > 10) {
                        stopMotions.add(motion.subSkeletonInterpolator(y, j - 1));
                        //System.out.println("y:"+y+",j="+j);
                        y = j;
                    }
                }

            }
            //stopMotions.add(motion.subSkeletonInterpolator(i,i+max));

//            System.out.println("stops.lenght=" + stops.length + "; frame=" + i + "; length=" + max);
        }

        return stopMotions;

    }

    public SkeletonInterpolator testBlending(SkeletonInterpolator first, SkeletonInterpolator second, IBlend blend, IAlignment align) {
        SkeletonInterpolator newSecond;
        SkeletonInterpolator blended;

        newSecond = align.align(first, second, first.size());
        blended = blend.blend(first, newSecond, first.size());

        return blended;
    }
}
