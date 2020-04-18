package Starace;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.input.controls.ActionListener;
import com.jme3.math.*;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
//import com.jme3.bullet.collision.shapes.MeshCollisionShape;
//import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
//import com.jme3.bullet.util.Converter;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
//import com.jme3.scene.Geometry;
import com.jme3.network.Client;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
//import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.scene.Spatial;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
//import javafx.scene.paint.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.util.SkyFactory;
//import com.sun.scenario.Settings;
import org.lwjgl.opengl.Display;

import java.io.IOException;


public class Mygame extends SimpleApplication {

    private BulletAppState physic;
    private Node Starship;
    private boolean forward = false;
    private boolean right = false;
    private boolean left = false;
    private boolean backward = false;

    private boolean clock = false;
    private boolean anticlock = false;
    private RigidBodyControl rigidBodyCar;
    private final float accForce = 1000f;
    private final float brkForce = 100f;
    private float strValue = 0;
    private float accValue = 0;
    private Vector3f a;


    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1366, 768);
        settings.setSamples(4);
        settings.setTitle("Starace");
        //settings.setFrameRate(60);
        Mygame game = new Mygame();
        game.setSettings(settings);
        game.setShowSettings(false);
        game.setPauseOnLostFocus(false);
        game.start(JmeContext.Type.Display);


    }


    @Override
    public void simpleInitApp() {

        try {
            Client myClient = Network.connectToServer("127.0.0.1",6666);
            myClient.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Server myServer = Network.createServer(6666);
            myServer.start();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Network Error");
        }
        Spatial sky = SkyFactory.createSky(assetManager, "Resources/Sky/1.jpg", SkyFactory.EnvMapType.EquirectMap);
        flyCam.setMoveSpeed(30);
        viewPort.setBackgroundColor(ColorRGBA.LightGray);
        Starship = new Node();
        Starship.move(24.0f, -2.2f, 0.0f);
        Starship.rotate(0.0f, 42 * FastMath.DEG_TO_RAD, 0.0f);
        Spatial roadmodel = assetManager.loadModel("Resources/RaceTrack/FullRaceTrack.obj");
        roadmodel.scale(50);
        roadmodel.center();

        BulletAppState physic = new BulletAppState();
        stateManager.attach(physic);
        physic.setDebugEnabled(false);
        PhysicsSpace physicsSpace = physic.getPhysicsSpace();

        physicsSpace.setGravity(new Vector3f(0, -9.81f, 0));
        CollisionShape shape = CollisionShapeFactory.createMeshShape(roadmodel);

        //initStarship();

        initLight();

        initFire();

        rigidBodyCar = new RigidBodyControl(10);


        //rigidBodyCar.setCollisionShape(new  BoxCollisionShape(new Vector3f(1.5f,1.5f,1.5f)));
        rigidBodyCar.setCollisionShape(new CapsuleCollisionShape(0.8f, 5f,0));
        //rigidBodyCar.applyForce(new Vector3f(0, 0, -50), Vector3f.ZERO);
        rigidBodyCar.setFriction(0.0f);
        Starship.addControl(rigidBodyCar);
        physicsSpace.add(rigidBodyCar);
        RigidBodyControl rigidBodyRoad = new RigidBodyControl(0);
        CollisionShape CShapeRoad = CollisionShapeFactory.createMeshShape(roadmodel);
        rigidBodyRoad.setCollisionShape(CShapeRoad);
        roadmodel.addControl(rigidBodyRoad);
        physicsSpace.add(rigidBodyRoad);
        rigidBodyCar.setAngularDamping(0.1f);
        rigidBodyCar.setAngularVelocity(new Vector3f(0, 0, 0));

        rigidBodyCar.setLinearDamping(0.5f);




//        float stiffness = 120.0f;//200=f1 car
//        float compValue = 0.2f; //(lower than damp!)
//        float dampValue = 0.3f;
//        final float mass = 400;

        Spatial model = assetManager.loadModel("Resources/Starship/Sample_Ship1.obj");
        model.scale(5);
        model.center();
//        vehicle = new VehicleControl();
        Starship.attachChild(model);

        Ctrlkeys();
        CameraNode camNode = new CameraNode("CameraNode", cam);
        camNode.move(6.5f,1.5f,0f);
        camNode.rotate( 0f , -90*FastMath.DEG_TO_RAD  , 0);
        Starship.attachChild(camNode);

        rootNode.attachChild(sky);
        rootNode.attachChild(roadmodel);
        rootNode.attachChild(Starship);

    }




    private void Ctrlkeys() {

        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Clock", new KeyTrigger(KeyInput.KEY_I));
        inputManager.addMapping("Anticlock", new KeyTrigger(KeyInput.KEY_Y));
        inputManager.addListener(listener, "Lefts");
        inputManager.addListener(listener, "Rights");
        inputManager.addListener(listener, "Ups");
        inputManager.addListener(listener, "Downs");
        inputManager.addListener(listener, "Clock");
        inputManager.addListener(listener, "Anticlock");
    }

    private final ActionListener listener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {

            if (name.equals("Lefts")) {
                if (isPressed) {
                    left = true;
                } else {
                    left = false;
                }
                //vehicle.steer(strValue);
            } else if (name.equals("Rights")) {
                if (isPressed) {
                    right = true;
                } else {
                    right = false;
                }
                //vehicle.steer(strValue);
            } else if (name.equals("Ups")) {
                if (isPressed) {
                    forward = true;
                } else {
                    forward = false;
                }
            } else if (name.equals("Downs")) {
                if (isPressed) {
                    backward = true;
                } else {
                    backward = false;
                }
            } else if (name.equals("Anticlock")) {
                if (isPressed) {
                    anticlock = true;
                } else {
                    anticlock = false;
                }
            } else if (name.equals("Clock")) {
                if (isPressed) {
                    clock = true;
                } else {
                    clock = false;
                }
            }
        }
    };
//            } else if (name.equals("Reset")) {
//                if (isPressed) {
//                    System.out.println("Reset");
//                    vehicle.setPhysicsLocation(Vector3f.ZERO);
//                    vehicle.setPhysicsRotation(new Matrix3f());
//                    vehicle.setLinearVelocity(Vector3f.ZERO);
//                    vehicle.setAngularVelocity(Vector3f.ZERO);
//                    vehicle.resetSuspension();
//                } else {
//                }
//            }
//        }
//    };
//
//    private Geometry findGeom(Spatial spatial, String name) {
//        if (spatial instanceof Node) {
//            Node node = (Node) spatial;
//            for (int i = 0; i < node.getQuantity(); i++) {
//                Spatial child = node.getChild(i);
//                Geometry result = findGeom(child, name);
//                if (result != null) {
//                    return result;
//                }
//            }
//        } else if (spatial instanceof Geometry) {
//            if (spatial.getName().startsWith(name)) {
//                return (Geometry) spatial;
//            }
//        }
//        return null;
//    }
//
//        public void initStarship() {
//
//
//        }

        public void initLight () {
            DirectionalLight sun = new DirectionalLight();
            sun.setDirection(new Vector3f(-1, -2, -3));
            AmbientLight ambient = new AmbientLight();
            ColorRGBA lightColor = new ColorRGBA();
            sun.setColor(lightColor.mult(0.7f));
            ambient.setColor(lightColor.mult(0.3f));
            rootNode.addLight(sun);
            rootNode.addLight(ambient);
        }

        public void initFire () {
            ParticleEmitter fire =
                    new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 100);
            Material mat_red = new Material(assetManager,
                    "Common/MatDefs/Misc/Particle.j3md");
                    mat_red.setTexture("Texture", assetManager.loadTexture(
                            "Effects/Explosion/flame.png"));
                    fire.setMaterial(mat_red);
                    fire.setParticlesPerSec(250);
                    fire.setImagesX(2);
                    fire.setImagesY(2); // 2x2 texture animation
                    fire.setEndColor(new ColorRGBA(0.3f, 0f, 0.7f, 1f));
                    fire.setStartColor(new ColorRGBA(0.3f, 0.0f, 0.8f, 1f));
                    fire.getParticleInfluencer().setInitialVelocity(new Vector3f(15, 0, 0));
                    fire.setStartSize(0.2f);
                    fire.setEndSize(0.1f);
                    fire.setGravity(0, 0, 0);
                    fire.setLowLife(1f);
                    fire.setHighLife(1.44f);
                    fire.move(2, 0, 0);
                    fire.setFaceNormal(Vector3f.NAN);
                    fire.setSelectRandomImage(true);
                    fire.getParticleInfluencer().setVelocityVariation(0.1f);
                    Starship.attachChild(fire);

                }


    @Override
    public void simpleUpdate(float tpf) {

        if (forward) {
            rigidBodyCar.applyCentralForce(cam.getDirection().mult(150));
        }
        if (left){
            rigidBodyCar.applyTorque(new Vector3f (0,50,0));
        }
        if (right){
            rigidBodyCar.applyTorque(new Vector3f (0,-50,0));
        }
        rigidBodyCar.setAngularVelocity(new Vector3f (0,rigidBodyCar.getAngularVelocity().getY(),0));
        if (backward) {
            rigidBodyCar.applyCentralForce(cam.getDirection().mult(-150));
        }
        if (clock){
            rigidBodyCar.applyTorque(new Vector3f (-100,0,0));
        }
        if (anticlock){
            rigidBodyCar.applyTorque(new Vector3f (100,0,0));
        }

    }
}
