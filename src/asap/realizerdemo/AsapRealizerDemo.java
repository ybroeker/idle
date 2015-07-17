package asap.realizerdemo;

import asap.bml.ext.bmlt.BMLTInfo;
import asap.environment.AsapEnvironment;
import asap.environment.AsapVirtualHuman;
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
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import saiba.bml.BMLInfo;
import saiba.bml.core.FaceLexemeBehaviour;
import saiba.bml.core.HeadBehaviour;
import saiba.bml.core.PostureShiftBehaviour;

/**
 * Simple demo for the AsapRealizer+environment
 * <p/>
 *
 * @author hvanwelbergen
 *         <p/>
 */
public class AsapRealizerDemo {

    private final HmiRenderEnvironment hre;
    private final OdePhysicsEnvironment ope;

    private VJoint sphereJoint;
    protected JFrame mainJFrame = null;


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

        /*
        hre.loadBox("bluebox", new float[] { 0.05f, 0.05f, 0.05f }, HmiRenderEnvironment.RenderStyle.FILL, new float[] { 0.2f, 0.2f, 1, 1 }, new float[] { 0.2f,
                0.2f, 1f, 1f }, new float[] { 0.2f, 0.2f, 1f, 0f }, new float[] { 0.2f, 0.2f, 1f, 1f });
        VJoint boxJoint = hre.getObjectRootJoint("bluebox");
        boxJoint.setTranslation(0.0f, 0.0f, 0.0f);
        we.getWorldObjectManager().addWorldObject("bluebox", new VJointWorldObject(boxJoint));
        */
        // set camera position
        // hre.setNavigationEnabled(false);
        hre.setViewPoint(new float[]{0, 1, 4});
        avh = ee.loadVirtualHuman("", spec, "AsapRealizer Idle-Demo");

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

        AsapRealizerDemo demo = new AsapRealizerDemo(new JFrame("AsapRealizer Idle-Demo"), spec);
        demo.startClocks();
        
        /*
        demo.avh.getRealizerPort().performBML("<bml id=\"bml1\" xmlns=\"http://www.bml-initiative.org/bml/bml-1.0\">\n" +
"    <postureShift id=\"pose1\" start=\"0\">\n" +
"        <stance type=\"STANDING\"/>\n" +
"        <pose part=\"BODY\" lexeme=\"IDLE\"/>\n" +
"    </postureShift>\n" +
"</bml>");*/
    }
}
