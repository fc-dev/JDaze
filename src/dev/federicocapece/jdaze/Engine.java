package dev.federicocapece.jdaze;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * The Engine of JDaze.
 * The Engine is the very core of JDaze, it manages the game loop.
 * To use the Engine you need to add the Engine.renderer to your GUI first,
 * then call Engine.start() and enjoy the magic of your GameObjects being automatically updated and drawn.
 * NOTE: Instantiate the GameObjects only after the start() of the Engine.
 */
public final class Engine {
    /**
     * The thread of the gameLoop, use start() and stop() to manage it.
     * Do not touch this directly, for any reason, this will cause unexpected behaviour in the engine.
     */
    private static Thread gameThread = null;

    /**
     * The renderer of the Engine.
     * You can add this to the GUI like you would normally add any Swing component.
     */
    public final static Renderer renderer;

    /**
     * The camera that is being used by the renderer to render the screen.
     */
    public final static Camera camera;

    /**
     * The list of the gameObjects that are currently managed from the Game Loop.
     * You shouldn't touch this directly.
     */
    protected final static ArrayList<GameObject> gameObjects;
    /**
     * The list of the gameObjects that will be destroyed in this Game Loop.
     * You shouldn't touch this directly.
     */
    protected final static ArrayList<GameObject> toDestroyGameObject;

    /**
     * The StopWatch used to measure the Game Loop duration.
     */
    private static StopWatch stopWatch = new StopWatch();

    /**
     * The time in seconds that the last Game Loop run did take.
     * This is essential for making physics frame-rate independent
     *
     * <pre>
     * Example:
     * Vector movement = Vector.UP().multiply(speed);
     * this will move the item up of 'speed' every frame
     *
     * Vector movement = Vector.UP().multiply(speed * Engine.deltaTime);
     * this will move the item up of 'speed' every second.
     * </pre>
     */
    public static float deltaTime;

    /**
     * The milliseconds that a Game Loop execution should take to match the desired framerate.
     */
    private static float targetCycleMS;

    static {
        gameObjects = new ArrayList<>();
        toDestroyGameObject = new ArrayList<>();

        //creating the renderer canvas
        renderer = new Renderer();
        //linking the Input listeners to the canvas
        renderer.addKeyListener(Input.keyListener);
        renderer.addMouseListener(Input.mouseInputListener);
        renderer.addMouseMotionListener(Input.mouseInputListener);
        renderer.addMouseWheelListener(Input.mouseWheelListener);

        //setting the Engine.camera to the renderer.camera
        camera = renderer.camera;
    }


    /**
     * Start the GameLoop.
     * This should be called after putting Engine.renderer inside the GUI and after repainting and re-validating the GUI.
     * Since the GameLoop starts in a new Thread this will be non-blocking for your code,
     * so you should start the Engine before every other Game related stuff
     * (for example before GameObjects' instantiation)
     * The framecap will be 60FPS if you don't specify it.
     */
    public static void start(){
        start(60);
    }

    /**
     * Start the GameLoop.
     * This should be called after putting Engine.renderer inside the GUI and after repainting and re-validating the GUI.
     * Since the GameLoop starts in a new Thread this will be non-blocking for your code,
     * so you should start the Engine before every other Game related stuff
     * (for example before GameObjects' instantiation)
     * @param maxFPS the framecap to the Engine
     */
    public static void start(int maxFPS){
        //#region Closing the last Engine thread
        if(gameThread != null) {
            gameThread.interrupt();
            while (gameThread.isAlive()) {
                try {
                    gameThread.interrupt();
                    gameThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //#endregion

        //#region Forcing redraw of the GUI
        Window frame = SwingUtilities.getWindowAncestor(Engine.renderer);
        boolean previouslyVisible = frame.isVisible();
        frame.setVisible(true);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(previouslyVisible);
        //#endregion

        //initialize engine stuff for the gameLoop
        targetCycleMS = 1000f / maxFPS;
        gameObjects.clear();
        renderer.init();


        //starting the gameloop
        gameThread = new Thread(){
            @Override
            public void run() {
                stopWatch.start();
                while (!isInterrupted()){
                    update();
                }
            }
        };

        gameThread.start();
    }

    /**
     * Stop the execution of the Game Loop
     */
    public static void stop(){
        if(gameThread != null) gameThread.interrupt();
    }


    /**
     * The Game Loop, does literally everything.
     */
    private static void update(){
        //restarting stopwatch to measure MS in this game cycle
        stopWatch.start();

        //clean the screen buffer
        renderer.clean();

        //#region GameObjects update/destroy/draw
        synchronized (gameObjects){
            //run each gameObject update
            int size = gameObjects.size();
            for (int i = 0; i < size; i++) {
                GameObject gameObject = gameObjects.get(i);
                //skip gameObject if it's been destroyed
                if(!toDestroyGameObject.contains(gameObject))
                    gameObject.update();
            }

            //delete deleted gameObjects
            synchronized (toDestroyGameObject){
                for (GameObject gameObject : toDestroyGameObject){
                    gameObjects.remove(gameObject);
                }
                toDestroyGameObject.clear();
            }

            //draw the gameObjects on the buffer
            for (GameObject gameObject : gameObjects) {
                renderer.update(gameObject);
            }
        }
        //#endregion

        //draw the buffer to the canvas
        renderer.update();

        //cleaning mouse wheel rotation for next game loop
        Input.mouseWheelReset();

        //sleeping for the remaining time of this cycle
        float targetSleep = targetCycleMS - stopWatch.getElapsedMS();
        try {
            while(targetSleep > 1){
                Thread.sleep(1);
                targetSleep = targetCycleMS - stopWatch.getElapsedMS();
            }
        }catch (InterruptedException ex){
            return;
        }

        //calculating deltaTime for next Cycle
        deltaTime = stopWatch.getElapsedMS() / 1000f;
    }

}
