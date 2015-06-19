package asap.realizerdemo;

import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
import asap.realizerdemo.motiongraph.IEquals;
import asap.realizerdemo.motiongraph.LoadMotion;
import asap.realizerdemo.motiongraph.graph1.Equals;
import asap.realizerdemo.motiongraph.graph1.MotionGraph;
import asap.realizerdemo.motiongraph.movementdetection.IMovementDetector;
import asap.realizerdemo.motiongraph.movementdetection.MovementDetector;
import asap.realizerport.RealizerPort;
import com.google.common.collect.Iterables;
import hmi.animation.ConfigList;
import hmi.animation.SkeletonInterpolator;
import hmi.animation.VJoint;
import hmi.audioenvironment.AudioEnvironment;
import hmi.environmentbase.Environment;
import hmi.jcomponentenvironment.JComponentEnvironment;
import hmi.mixedanimationenvironment.MixedAnimationEnvironment;
import hmi.physicsenvironment.OdePhysicsEnvironment;
import hmi.renderenvironment.HmiRenderEnvironment;
import hmi.util.Console;
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
 *
 * @author hvanwelbergen
 *
 */
public class AsapRealizerDemo {

    private final HmiRenderEnvironment hre;
    private final OdePhysicsEnvironment ope;

    private VJoint sphereJoint;
    protected JFrame mainJFrame = null;

    private static int id=0;
    
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

        System.out.println("0+1:" + equals.startEndEquals(motions.get(0), motions.get(1)));

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
            /*
             realizerPort.addListeners(new BMLFeedbackListener[]{new BMLFeedbackListener() {

             @Override
             public void feedback(String feedback) {
             if (feedback.contains("bml1:end")) {
             realizerPort.performBML("<bml id=\"bml1\" composition=\"MERGE\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\">   \n"
             + "   <keyframe id=\"bml1:headmotion\" xmlns=\"http://hmi.ewi.utwente.nl/bmlt\">"
             + skeletonInterpolator.toXMLString()
             + "</keyframe>   \n</bml>");
             }
             }
             }});
             */

            JFrame frame = new JFrame();
            JButton button = new JButton();
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    realizerPort.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"  id=\"bml1\" xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\">   \n"
                            + "   <bmlt:keyframe id=\"kf1\">"
                            + motion.toXMLString()
                            + "</bmlt:keyframe>   \n</bml>");
                }
            });

            frame.pack();
            frame.add(button);
            frame.setVisible(true);
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
        String spec = "asaparmandia_y_test.xml";
        if (args.length == 1) {
            spec = args[0];
        }
        AsapRealizerDemo demo = new AsapRealizerDemo(new JFrame("AsapRealizer demo 1"), spec);
        demo.startClocks();
        //demo.test(demo.avh.getRealizerPort());

        //AsapRealizerDemo demo = new AsapRealizerDemo();
        //demo.test(null);        
        List<SkeletonInterpolator> motions = LoadMotion.loadMotion(new String[]{
            "idle_0_10.xml", "idle_10_20.xml", "idle_20_30.xml", "idle_30_40.xml", "idle_40_50.xml",
            "idle_50_60.xml", "idle_60_70.xml"

        });

        //MotionGraph test = new MotionGraph(motions);
        //motions = test.randomWalk();
        //demo.testStopping(motions);
        
        
        
        demo.play(demo.concatMotions(demo.testStopping(motions)), demo.avh.getRealizerPort());
        ///demo.play(demo.concatMotions(motions), demo.avh.getRealizerPort());

        //SkeletonInterpolator skeletonInterpolator = motions.get(3);
        //skeletonInterpolator.mirror();
        //IEquals equals = new Equals();
        //System.out.println("e: "+equals.startEndEquals(skeletonInterpolator, motions.get(4)));
    }

    public SkeletonInterpolator concatMotions(List<SkeletonInterpolator> motions) {
        double globTime = 0;

        ConfigList config = new ConfigList(motions.get(0).getConfigSize());
        String configType = motions.get(0).getConfigType();
        String[] partIds = motions.get(0).getPartIds();

        for (SkeletonInterpolator motion : motions) {
            
              
            
            double startTime = motion.getStartTime();
            for (int i = 0; i < motion.size(); i++) {
                config.addConfig(motion.getTime(i) - startTime + globTime, motion.getConfig(i));
            }
            System.out.println("t:"+startTime);
            globTime = motion.getEndTime() - startTime + globTime;
            System.out.println("gt:"+globTime);  

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
                realizerPort.performBML("<bml xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\"  id=\"bml"+id+"\" xmlns:bmlt=\"http://hmi.ewi.utwente.nl/bmlt\">   \n"
                        + "   <bmlt:keyframe id=\"kf"+id+"\">"
                        + skeletonInterpolator.toXMLString()
                        + "</bmlt:keyframe>   \n</bml>");
            }
        });

        frame.pack();
        frame.add(button);
        frame.setVisible(true);
    }

    public List<SkeletonInterpolator> testStopping(List<SkeletonInterpolator> motions) {
        IMovementDetector movementDetector = new MovementDetector();
        
        List<SkeletonInterpolator> stopMotions = new LinkedList<>();
        
        for (SkeletonInterpolator motion : motions) {

            movementDetector.getStops(motion);

            int[] stops = movementDetector.getStops(motion);

            int i = 0;
            int max = 0;
            for (int j = 1; j < stops.length; j++) {
                if (stops[j] - stops[j - 1] > max) {
                    max = stops[j] - stops[j - 1];
                    i = j - 1;
                }
            }
            stopMotions.add(motion.subSkeletonInterpolator(i,i+max));
            System.out.println("motion:");
            System.out.println("stops.lenght=" + stops.length + "; frame=" + i + "; length=" + max);

        }
        
        return stopMotions;
        
    }
}
