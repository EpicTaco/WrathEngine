/**
 *  Wrath Engine 
 *  Copyright (C) 2015  Trent Spears
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * NOTES:
 *  - Add font rendering
 *  - Work on audio
 *  - Add an in-game structures (Item framework, Entity framework, etc)
 *  - Add physics
 *  - Add networking
 *  - Add encryption
 *  - Add external modding API
 *  - Add GUI Toolkits.
 */
package wrath.client;

import java.awt.Font;
import java.awt.FontFormatException;
import wrath.client.input.KeyData;
import wrath.client.handlers.GameEventHandler;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALContext;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;
import wrath.client.input.Key;
import wrath.client.input.Key.KeyAction;
import wrath.common.scheduler.Scheduler;
import wrath.util.Config;
import wrath.util.Logger;

/**
 * The entry point and base of the game. Make a class extending this and overriding at least render() method.
 * @author Trent Spears
 */
public class Game 
{
    /**
     * Enumerator describing whether the game should be run in 2D Mode or 3D Mode.
     */
    public static enum RenderMode {Mode2D,Mode3D;}
    /**
     * Enumerator describing the display mode of the Window.
     */
    public static enum WindowState {FULLSCREEN, FULLSCREEN_WINDOWED, WINDOWED, WINDOWED_UNDECORATED;}
    
    private final RenderMode MODE;
    private final String TITLE;
    private final double TPS;
    private final String VERSION;

    private final Config gameConfig = new Config("game");
    private GameEventHandler gameHandler = null;
    private final Logger gameLogger = new Logger("info");
    private final Scheduler gameScheduler = new Scheduler();
    
    private GLFWCharCallback charStr;
    private GLFWCursorPosCallback curStr;
    private GLFWErrorCallback errStr;
    private GLFWKeyCallback keyStr;
    private GLFWMouseButtonCallback mkeyStr;
    private GLFWFramebufferSizeCallback winSizeStr;
    
    private ALContext audiocontext;
    private boolean isRunning = false;
    
    private final WindowManager winManager = new WindowManager();
    private final InputManager inpManager = new InputManager();
    
    /**
     * Constructor.
     * Describes all the essential and unmodifiable variables of the Game.
     * @param gameTitle Title of the Game.
     * @param version Version of the Game.
     * @param ticksPerSecond The amount of times the logic of the game should update in one second. Recommended 30.
     * @param renderMode Describes how to game should be rendered (2D or 3D).
     */
    public Game(String gameTitle, String version, double ticksPerSecond, RenderMode renderMode)
    {
        MODE = renderMode;
        TITLE = gameTitle;
        VERSION = version;
        TPS = ticksPerSecond;
        
        System.setProperty("org.lwjgl.librarypath", "assets/native");
        
        File screenshotDir = new File("etc/screenshots");
        if(!screenshotDir.exists()) screenshotDir.mkdirs();
    }
    
    /**
     * Gets the {@link wrath.util.Config} object of the game.
     * @return Returns the configuration object of the game.
     */
    public Config getConfig()
    {
        return gameConfig;
    }
    
    /**
     * Returns the standard error logger for the application as a whole.
     * Same operation as {@link wrath.util.Logger#getErrorLogger() }.
     * @return Returns the standard error {@link wrath.util.Logger} for the game.
     */
    public Logger getErrorLogger()
    {
        return Logger.getErrorLogger();
    }
    
    /**
     * Gets the currently-registered {@link wrath.client.GameEventHandler} linked to this Game.
     * @return Returns the Client's general GameEventHandler.
     */
    public GameEventHandler getGameEventHandler()
    {
        return gameHandler;
    }
    
    /**
     * Gets the {@link wrath.client.Game.InputManager} linked to this {@link wrath.client.Game} instance.
     * @return Returns the {@link wrath.client.Game.InputManager} linked to this {@link wrath.client.Game} instance.
     */
    public InputManager getInputManager()
    {
        return inpManager;
    }
    
    /**
     * Gets the standard {@link wrath.util.Logger} for the game.
     * @return Returns the standard {@link wrath.util.Logger} for the game.
     */
    public Logger getLogger()
    {
        return gameLogger;
    }
    
    /**
     * Gets the Operating System the game is running on.
     * @return Returns the enumerator-style object representing what Operating System the game is running on.
     */
    public LWJGLUtil.Platform getOS()
    {
        return LWJGLUtil.getPlatform();
    }
    
    /**
     * Gets whether the game should be rendered in 2D or 3D.
     * @return Returns the enumerator-style representation of the game's rendering mode.
     */
    public RenderMode getRenderMode()
    {
        return MODE;
    }
    
    /**
     * Gets the standardized {@link wrath.common.scheduler.Scheduler} for the game.
     * @return Returns the scheduler for the game.
     */
    public Scheduler getScheduler()
    {
        return gameScheduler;
    }
    
    /**
     * Gets the title/name of the Game.
     * @return Returns the title of the Game.
     */
    public String getTitle()
    {
        return TITLE;
    }
    
    /**
     * Gets the amount of times the game's logic will update in one second.
     * Recommended to not be over 60 or under 10.
     * If it is over 60 and VSync is on, the ticks will be called to 60 FPS.
     * @return Returns the Ticks-per-second of the game's logic.
     */
    public double getTPS()
    {
        return TPS;
    }
    
    /**
     * Gets the {@link java.lang.String} representation of the Game's Version.
     * @return Returns the version of the game in a {@link java.lang.String} format.
     */
    public String getVersion()
    {
        return VERSION;
    }
    
    /**
     * Gets the {@link wrath.client.Game.WindowManager} linked to this {@link wrath.client.Game} instance.
     * @return Returns the {@link wrath.client.Game.WindowManager} linked to this {@link wrath.client.Game} instance.
     */
    public WindowManager getWindowManager()
    {
        return winManager;
    }
    
    /**
     * Returns whether or not the game is currently running.
     * @return Returns true if the game is running, otherwise false.
     */
    public boolean isRunnning()
    {
        return isRunning;
    }
    
    /**
     * Private loop (main game loop).
     */
    private void loop()
    {
        // FPS counter.
        int fpsBuf = 0;
        int fpsCount = 0;
        
        //Input Checking
        int inpCount = 0;
        double checksPerSec = gameConfig.getDouble("PersInputCheckPerSecond", 10.0);
        if(checksPerSec > TPS) checksPerSec = TPS;
        final double INPUT_CHECK_TICKS = TPS / checksPerSec;
        
        //Timings
        long last = System.nanoTime();
        final double conv = 1000000000.0 / TPS;
        double delta = 0.0;
        long now;
        
        while(isRunning && (!winManager.windowOpen || GLFW.glfwWindowShouldClose(winManager.window) != GL11.GL_TRUE))
        {
            now = System.nanoTime();
            delta += (now - last) / conv;
            last = now;
            
            while(delta >= 1)
            {
                onTickPreprocessor();
                
                if(INPUT_CHECK_TICKS == 1 || inpCount >= INPUT_CHECK_TICKS)
                {
                    inpManager.onPersistentInput();
                    inpCount -= INPUT_CHECK_TICKS;
                }
                else inpCount++;
                
                if(fpsCount >= TPS)
                {
                    winManager.fps = fpsBuf;
                    fpsBuf = 0;
                    fpsCount-=TPS;
                }
                else fpsCount++;
                
                delta--;
            }
            
            if(winManager.windowOpen)
            {
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                winManager.getBackground().renderBackground();
                render();
                winManager.getGUI().renderGUI();
                GL11.glFlush();
                GLFW.glfwSwapBuffers(winManager.window);
            
                GLFW.glfwPollEvents();
                fpsBuf++;
            }
        }
        
        stop();
        stopImpl();
    }
    
    /**
     * Private method that takes care of all background processes before onTick() is called.
     */
    private void onTickPreprocessor()
    {
        gameScheduler.onTick();
        if(gameHandler != null) gameHandler.onTick();
    }
    
    /**
     * Override-able method that is called as much as possible to issue rendering commands.
     */
    protected void render(){} //TODO: Add Rendering Pipeline (kind of a biggie)

    /**
     * Adds a {@link wrath.client.GameEventHandler} to the client.
     * @param handler The GameEventHandler the client should report to.
     */
    public void setGameEventHandler(GameEventHandler handler)
    {
        gameHandler = handler;
    }
    
    /**
     * Method that is used to load the game and all of it's resources.
     */
    public void start()
    {
        start(new String[1]);
    }
    
    /**
     * Method that is used to load the game and all of it's resources.
     * @param args Arguments, usually from the main method (entry point).
     */
    public void start(String[] args)
    {
        isRunning = true;
        gameLogger.log("Launching '" + TITLE + "' Client v." + VERSION + " on '" + LWJGLUtil.getPlatformName() +"' platform with LWJGL v." + Sys.getVersion() + "!");
        
        //Interpret command-line arguments
        for(String a : args)
        {
            String[] b = a.split("=", 2);
            if(b.length <= 1) return;
            
            gameConfig.setProperty(b[0], b[1]);
        }

        //Initialize GLFW and OpenGL
        GLFW.glfwSetErrorCallback((errStr = new GLFWErrorCallback()
        {
            @Override
            public void invoke(int error, long description) 
            {
                Logger.getErrorLogger().log("GLFW hit ERROR ID '" + error + "' with message '" + description + "'!");
            }
        }));
        
        if(GLFW.glfwInit() != GL11.GL_TRUE)
        {
            Logger.getErrorLogger().log("Could not initialize GLFW! Unknown Error!");
            ClientUtils.throwInternalError("Failed to initialize GLFW!", false);
            stopImpl();
            return;
        }
        
        winManager.openWindow();
        
        if(gameHandler != null) gameHandler.onGameOpen();
        loop();
    }
    
    /**
     * Method that flags the game to stop.
     */
    public void stop()
    {
        if(!isRunning) return;
        
        if(gameHandler != null) gameHandler.onGameClose();
        
        isRunning = false;
    }
    
    /**
     * Method to stop the game and close all of it's resources.
     */
    private void stopImpl()
    {
        try{
        winManager.destroyWindow();
        if(inpManager.cursor != -1) GLFW.glfwDestroyCursor(inpManager.cursor);
        GLFW.glfwTerminate();
        
        gameConfig.save();
        gameLogger.log("Stopping '" + TITLE + "' Client v." + VERSION + "!");
        if(gameLogger != null && !gameLogger.isClosed()) gameLogger.close();
        errStr.release();
        }catch(Exception e){}
        
        System.exit(0);
    }
    
    /**
     * Class to define the background of the game.
     */
    public class Background
    {
        private int backTexture = 0;
        private float br = 1, bg = 1, bb = 1, ba = 0;
        private boolean def = true;
        
        /**
         * Used to render user-set background.
         */
        private void renderBackground()
        {
            if(def) return;
           
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, backTexture);
            GL11.glColor4f(br, bg, bb, ba);
            GL11.glBegin(GL11.GL_QUADS);
            {
                GL11.glTexCoord2f(0, 0);
                GL11.glVertex2f(-1, -1);

                GL11.glTexCoord2f(1, 0);
                GL11.glVertex2f(1, -1);

                GL11.glTexCoord2f(1, 1);
                GL11.glVertex2f(1, 1);

                GL11.glTexCoord2f(0, 1);
                GL11.glVertex2f(-1, 1);
            }
            GL11.glEnd();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL11.glColor4f(1, 1, 1, 0);
        }
        
        /**
         * Gets the RGBA Values of the background color in an array format.
         * Element 0 of the array is the R (red) value, scaled 0.0 (no red) to 1.0 (pure red).
         * Element 1 of the array is the G (green) value, scaled 0.0 (no green) to 1.0 (pure green).
         * Element 2 of the array is the B (blue) value, scaled 0.0 (no blue) to 1.0 (pure blue).
         * Element 3 of the array is the A (alpha transparency) value, scaled 0.0 (opaque) to 1.0 (transparent).
         * @return 
         */
        public float[] getBackgroundRGBA()
        {
            return new float[]{br, bg, bb, ba};
        }
        
        /**
         * Gets the OpenGL Texture ID of the background image.
         * @return Returns the OpenGL Texture ID of the background image.
         */
        public int getBackgroundTextureID()
        {
            return backTexture;
        }
        
        /**
         * Changes the static background image.
         * @param imageFile The file to load the background texture from.
         */
        public void setBackgroundImage(File imageFile)
        {
            setBackgroundImage(ClientUtils.loadImageFromFile(imageFile));
        }

        /**
         * Changes the static background image.
         * @param image The image to load the background texture from.
         * @return Returns the OpenGL Texture ID number for the background image being set.
         */
        public int setBackgroundImage(BufferedImage image)
        {
            int tex = ClientUtils.get2DTexture(image);
            setBackgroundImage(tex);
            return tex;
        }

        /**
         * Changes the static background image.
         * @param texture The texture ID of the background image, 0 is clear.
         */
        public void setBackgroundImage(int texture)
        {
            def = br == 1 && bg == 1 && bb == 1 && ba == 0 && texture == 0;
            
            backTexture = texture;
        }

        /**
         * Sets the RGBA configuration of the background image.
         * @param red The red value, max 1.0.
         * @param green The green value, max 1.0.
         * @param blue The blue value, max 1.0.
         * @param alpha The transparency, 1.0 being opaque.
         */
        public void setBackgroundRGBA(float red, float green, float blue, float alpha)
        {
            def = red == 1 && green == 1 && blue == 1 && alpha == 0 && backTexture == 0;
            
            br = red;
            bg = green;
            bb = blue;
            ba = alpha;
        }

        /**
         * Resets the background to a state where it is not rendered.
         */
        public void setBackgroundToDefault()
        {
            def = true;
            br = 1;
            bg = 1;
            bb = 1;
            ba = 0;
            backTexture = 0;
        }
    }
    
    /**
     * Class to define Graphical User Interface (GUI) of the game.
     * This *will* include method to control pop-ups, sub-windows, etc.
     */
    public class GUI
    {
        /**
         * Method to render the GUI defined by the class.
         */
        private void renderGUI()
        {
            //This is just the outline of what is to come.
        }
    }
    
    /**
     * Class to manage all input operations.
     * Used to organize code and clean up the {@link wrath.client.Game} class.
     */
    public class InputManager
    {
        private final HashMap<String, Integer> keyboardDefaultsMap = new HashMap<>();
        private final HashMap<String, Integer> mouseDefaultsMap = new HashMap<>();
        
        private final HashMap<Integer, KeyData> keyboardMap = new HashMap<>();
        private final HashMap<Integer, Runnable> persKeyboardMap = new HashMap<>();
        private final HashMap<Integer, KeyData> mouseMap = new HashMap<>();
        private final HashMap<Integer, Runnable> persMouseMap = new HashMap<>();
        private final HashMap<String, Runnable> savedFuncMap = new HashMap<>();
        
        private long cursor = -1;
        private double curx = 0;
        private double cury = 0;
        
        /**
        * Adds a listener to a specified key on the {@link wrath.client.input.Key}.
        * @param key The {@link wrath.client.input.Key} to respond to.
        * @param event The {@link wrath.client.KeyRunnable} event to run after specified button is affected by the specified action.
        */
        public void addKeyboardFunction(int key, Runnable event)
        {
            addKeyboardFunction(key, Key.MOD_NONE, KeyAction.KEY_PRESS, event);
        }
    
        /**
        * Adds a listener to a specified key on the {@link wrath.client.input.Key}.
        * @param key The {@link wrath.client.input.Key} to respond to.
        * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
        * @param event The {@link wrath.client.KeyRunnable} event to run after specified button is affected by the specified action.
        */
        public void addKeyboardFunction(int key, KeyAction action, Runnable event)
        {
            addKeyboardFunction(key, Key.MOD_NONE, action, event);
        }
    
        /**
         * Adds a listener to a specified key on the {@link wrath.client.input.Key}.
        * @param key The {@link wrath.client.input.Key} to respond to.
        * @param keyMod The {@link wrath.client.input.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
        * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
        * @param event The {@link wrath.client.KeyRunnable} event to run after specified button is affected by the specified action.
        */
        public void addKeyboardFunction(int key, int keyMod, KeyAction action, Runnable event)
        {
            if(action == KeyAction.KEY_HOLD_DOWN)
            keyboardMap.put(key, new KeyData(KeyAction.KEY_PRESS, () -> 
            {
                persKeyboardMap.put(key, event);
                event.run();
            }, key, keyMod));
            else keyboardMap.put(key, new KeyData(action, event, key, keyMod));
        }
        
        /**
        * Adds a listener to a specified key on the {@link wrath.client.input.Key}.
        * @param key The key to respond to.
        * @param functionId The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
        */
        public void addKeyboardFunction(int key, String functionId)
        {
            addKeyboardFunction(key, Key.MOD_NONE, KeyAction.KEY_PRESS, functionId);
        }
    
        /**
        * Adds a listener to a specified key on the {@link wrath.client.input.Key}.
        * @param key The key to respond to.
        * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
        * @param functionId The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
         */
        public void addKeyboardFunction(int key, KeyAction action, String functionId)
        {
            addKeyboardFunction(key, Key.MOD_NONE, action, functionId);
        }
    
        /**
        * Adds a listener to a specified key on the {@link wrath.client.input.Key}.
        * @param key The key to respond to.
        * @param keyMod The {@link wrath.client.input.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
        * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
        * @param functionId The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
        */
        public void addKeyboardFunction(int key, int keyMod, KeyAction action, String functionId)
        {
            if(!savedFuncMap.containsKey(functionId)) return;
        
            Runnable event = savedFuncMap.get(functionId);
            if(action == KeyAction.KEY_HOLD_DOWN)
            keyboardMap.put(key, new KeyData(KeyAction.KEY_PRESS, () -> 
            {
                persKeyboardMap.put(key, event);
                event.run();
            }, key, keyMod));
            else keyboardMap.put(key, new KeyData(action, event, key, keyMod));
        }
    
        /**
        * Adds a listener to a specified key on the {@link wrath.client.input.Key}.
        * @param key The key to respond to.
        * @param event The {@link wrath.client.KeyRunnable} event to run after specified button is affected by the specified action.
        */
        public void addMouseFunction(int key, Runnable event)
        {
            addMouseFunction(key, Key.MOD_NONE, KeyAction.KEY_PRESS, event);
        }
    
        /**
        * Adds a listener to a specified key on the {@link wrath.client.input.Key}.
        * @param key The key to respond to.
        * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
        * @param event The {@link wrath.client.KeyRunnable} event to run after specified button is affected by the specified action.
        */
        public void addMouseFunction(int key, KeyAction action, Runnable event)
        {
            addMouseFunction(key, Key.MOD_NONE, action, event);
        }
    
        /**
        * Adds a listener to a specified key on the {@link wrath.client.input.Key}.
        * @param key The key to respond to.
        * @param keyMod The {@link wrath.client.input.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
        * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
        * @param event The {@link wrath.client.KeyRunnable} event to run after specified button is affected by the specified action.
        */
        public void addMouseFunction(int key, int keyMod, KeyAction action, Runnable event)
        {
            if(action == KeyAction.KEY_HOLD_DOWN)
            mouseMap.put(key, new KeyData(action, () -> 
            {
                persMouseMap.put(key, event);
                event.run();
            }, key, keyMod));
            else mouseMap.put(key, new KeyData(action, event, key, keyMod));
        }
    
        /**
        * Adds a listener to a specified key on the {@link wrath.client.input.Key}.
        * @param key The key to respond to.
        * @param functionId The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
        */
        public void addMouseFunction(int key, String functionId)
        {
            addMouseFunction(key, Key.MOD_NONE, KeyAction.KEY_PRESS, functionId);
        }
    
        /**
        * Adds a listener to a specified key on the {@link wrath.client.input.Key}.
        * @param key The key to respond to.
        * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
        * @param functionId The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
        */
        public void addMouseFunction(int key, KeyAction action, String functionId)
        {
            addMouseFunction(key, Key.MOD_NONE, action, functionId);
        }
    
        /**
        * Adds a listener to a specified key on the {@link wrath.client.input.Key}.
        * @param key The key to respond to.
        * @param keyMod The {@link wrath.client.input.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
        * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
        * @param functionId The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
        */
        public void addMouseFunction(int key, int keyMod, KeyAction action, String functionId)
        {
            if(!savedFuncMap.containsKey(functionId)) return;
        
            Runnable event = savedFuncMap.get(functionId);
            if(action == KeyAction.KEY_HOLD_DOWN)
            mouseMap.put(key, new KeyData(action, () -> 
            {
                persMouseMap.put(key, event);
                event.run();
            }, key, keyMod));
            else mouseMap.put(key, new KeyData(action, event, key, keyMod));
        }
    
        /**
        * Adds a listener to a specified String ID to be added later to a Keyboard or mouse function.
        * @param id The String ID of the saved function.
        * @param event The event to be saved.
        */
        public void addSavedFunction(String id, Runnable event)
        {
            savedFuncMap.put(id, event);
        }
        
        /**
        * Gets the X position of the cursor.
        * @return Returns the X position of the cursor.
        */
        public double getCursorX()
        {
            return (2/(double)winManager.width * curx) - 1.0;
        }
    
        /**
        * Gets the Y position of the cursor.
        * @return Returns the Y position of the cursor.
        */
        public double getCursorY()
        {
            return (2/(double)winManager.height * cury) - 1.0;
        }
        
        /**
         * Used by the {@link wrath.client.Game} class to initialize all of the GLFW input callbacks.
         */
        protected void initInputStreams()
        {
            GLFW.glfwSetKeyCallback(winManager.window, (keyStr = new GLFWKeyCallback()
            {
                @Override
                public void invoke(long window, int key, int scancode, int action, int mods)
                {
                    if(persKeyboardMap.containsKey(key) && action == GLFW.GLFW_RELEASE) persKeyboardMap.remove(key);
                    else if(keyboardMap.containsKey(key))
                    {
                        KeyData dat = keyboardMap.get(key);
                        if(dat.getRawAction() == action && (dat.getKeyMod() == -1 || mods == dat.getKeyMod())) dat.execute();
                    }
                }
            }));
        
            GLFW.glfwSetMouseButtonCallback(winManager.window, (mkeyStr = new GLFWMouseButtonCallback() 
            {
                @Override
                public void invoke(long window, int button, int action, int mods) 
                {
                    if(persMouseMap.containsKey(button) && action == GLFW.GLFW_RELEASE) persMouseMap.remove(button);
                    else if(mouseMap.containsKey(button))
                    {
                        KeyData dat = mouseMap.get(button);
                        if(dat.getRawAction() == action && (dat.getKeyMod() == -1 || mods == dat.getKeyMod())) dat.execute();
                    }
                }
            }));
        }
        
        /**
         * Returns whether or not the cursor is enabled.
         * @return Returns true if the cursor is enabled, otherwise false.
         */
        public boolean isCursorEnabled()
        {
            return GLFW.glfwGetInputMode(winManager.window, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_NORMAL;
        }
        
        /**
         * Used by the {@link wrath.client.Game} class to run tasks assigned to persistent keys.
         */
        protected void onPersistentInput()
        {
            persKeyboardMap.entrySet().stream().map((pairs) -> (Runnable) pairs.getValue()).forEach((ev) -> 
            {
                ev.run();
            });

            persMouseMap.entrySet().stream().map((pairs) -> (Runnable) pairs.getValue()).forEach((ev) -> 
            {
                ev.run();
            });
        }
        
        /**
        * Un-binds all functions bound to the specified key on the keyboard.
        * @param key The key to un-bind all functions on.
        */
        public void removeKeyboardFunction(int key)
        {
            if(keyboardMap.containsKey(key))
                keyboardMap.remove(key);
        }
    
        /**
        * Un-binds all functions bound to the specified key on the mouse.
        * @param key The key to un-bind all functions on.
        */
        public void removeMouseFunction(int key)
        {
            if(mouseMap.containsKey(key))
                mouseMap.remove(key);
        }
        
        /**
         * Changes the cursor from a list of standard cursors located in
         * {@link wrath.client.input.Key}.
         * @param cursormode The {@link wrath.client.input.Key} Cursor to switch
         * to.
         */
        public void setCursor(int cursormode)
        {
            if(cursor != -1) 
            {
                GLFW.glfwDestroyCursor(cursor);
                cursor = -1;
            }
            cursor = GLFW.glfwCreateStandardCursor(cursormode);
            GLFW.glfwSetCursor(winManager.window, cursor);
        }

        /**
         * Enables or disables the cursor.
         * @param cursorEnabled Whether the cursor should be enabled or
         * disabled.
         */
        public void setCursorEnabled(boolean cursorEnabled)
        {
            if(cursorEnabled) 
            {
                GLFW.glfwSetInputMode(winManager.window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
            }
            else 
            {
                GLFW.glfwSetInputMode(winManager.window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            }
        }
    }
    
    public class WindowManager
    {
        private Font font = new Font("Times New Roman", Font.PLAIN, 16);
        private float fps = 0;
        private long window;
        private boolean windowOpen = false;
        private WindowState windowState = null;
        
        private final Background back = new Background();
        private final GUI front = new GUI();

        private int width = 800;
        private int height = 600;

        /**
        * Centers the window in the middle of the designated primary monitor.
        * DO NOT use this while in any kind of full-screen mode.
        */
        public void centerWindow()
        {
            ByteBuffer vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            GLFW.glfwSetWindowPos(window, GLFWvidmode.width(vidmode) / 5, GLFWvidmode.height(vidmode) / 5);
        }
    
        /**
        * Destroys and deallocates all GLFW/window resources.
        */
        public void destroyWindow()
        {
            if(!windowOpen) return;
            windowOpen = false;
        
            gameLogger.log("Closing window [(" + width + "x" + height + "]");
        
            winSizeStr.release();
            keyStr.release();
            mkeyStr.release();
            charStr.release();
            curStr.release();
            AL.destroy(audiocontext);
            GLFW.glfwDestroyWindow(window);
        }
        
        /**
         * Gets the {@link wrath.client.Game.Background} linked to this Window.
         * @return Returns the {@link wrath.client.Game.Background} linked to this Window.
         */
        public Background getBackground()
        {
            return back;
        }
        
        /**
        * Gets the default global font.
        * @return The current global font.
        * @see java.awt.Font
        */
        public Font getFont()
        {
            return font;
        }
    
        /**
        * Gets the last recorded Frames-Per-Second count.
        * @return Returns the last FPS count.
        */
        public float getFPS()
        {
            return fps;
        }
        
        /**
         * Gets the {@link wrath.client.Game.GUI} linked to this Window.
         * @return Returns the {@link wrath.client.Game.GUI} linked to this Window.
         */
        public GUI getGUI()
        {
            return front;
        }
        
        /**
         * Gets the GLFW Window ID.
         * @return Returns the {@link org.lwjgl.glfw.GLFW} window ID.
         */
        public long getWindowID()
        {
            return window;
        }

        /**
         * Returns the width of the window.
         * @return Returns the width of the window.
         */
        public int getWidth()
        {
            return width;
        }

        /**
         * Gets the current state of the window as of
         * {@link wrath.client.Game.WindowState}.
         * @return Returns the current state of the window.
         */
        public WindowState getWindowState()
        {
            return windowState;
        }

        /**
         * Tells whether or not the window is open.
         * @return Returns true if the window is open, otherwise false.
         */
        public boolean isWindowOpen()
        {
            return windowOpen;
        }

        /**
         * Method to start the display. Made independent from start() so window
         * options can be adjusted without restarting game.
         */
        public void openWindow()
        {
            if(windowOpen) return;

            GLFW.glfwDefaultWindowHints();
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, ClientUtils.getLWJGLBoolean(gameConfig.getBoolean("WindowResizable", true)));
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, ClientUtils.getLWJGLBoolean(gameConfig.getBoolean("APIForwardCompatMode", false)));
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, ClientUtils.getLWJGLBoolean(gameConfig.getBoolean("DebugMode", false)));
            GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, gameConfig.getInt("DisplaySamples", 0));
            GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, gameConfig.getInt("DisplayRefreshRate", 0));

            String wstatestr = gameConfig.getString("WindowState", "fullscreen_windowed");

            if(wstatestr.equalsIgnoreCase("windowed")) windowState = WindowState.WINDOWED;
            else if(wstatestr.equalsIgnoreCase("windowed_undecorated")) windowState = WindowState.WINDOWED_UNDECORATED;
            else if(wstatestr.equalsIgnoreCase("fullscreen_windowed")) windowState = WindowState.FULLSCREEN_WINDOWED;
            else if(wstatestr.equalsIgnoreCase("fullscreen")) windowState = WindowState.FULLSCREEN;
            

            if(windowState == WindowState.FULLSCREEN) 
            {
                ByteBuffer videomode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
                width = GLFWvidmode.width(videomode);
                height = GLFWvidmode.height(videomode);

                window = GLFW.glfwCreateWindow(width, height, TITLE, GLFW.glfwGetPrimaryMonitor(), MemoryUtil.NULL);
            }
            else if(windowState == WindowState.FULLSCREEN_WINDOWED) 
            {
                ByteBuffer videomode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
                width = GLFWvidmode.width(videomode);
                height = GLFWvidmode.height(videomode);

                GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GL11.GL_FALSE);
                window = GLFW.glfwCreateWindow(width, height, TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
            }
            else if(windowState == WindowState.WINDOWED) 
            {
                width = gameConfig.getInt("Width", 800);
                height = gameConfig.getInt("Height", 600);
                window = GLFW.glfwCreateWindow(width, height, TITLE, MemoryUtil.NULL, MemoryUtil.NULL);

            }
            else if(windowState == WindowState.WINDOWED_UNDECORATED) 
            {
                GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GL11.GL_FALSE);

                width = gameConfig.getInt("Width", 800);
                height = gameConfig.getInt("Height", 600);
                window = GLFW.glfwCreateWindow(width, height, TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
            }

            if(window == MemoryUtil.NULL) 
            {
                Logger.getErrorLogger().log("Could not initialize window! Window Info[" + width + "x" + height + "]");
                ClientUtils.throwInternalError("Window failed to initialize!", false);
                stopImpl();
            }

            gameLogger.log("Opened window [" + width + "x" + height + "]");

            inpManager.initInputStreams();

            GLFW.glfwSetCharCallback(window, (charStr = new GLFWCharCallback()
            {
                @Override
                public void invoke(long window, int codepoint)
                {
                    if(gameHandler != null) gameHandler.onCharInput((char) codepoint);
                }
            }));

            GLFW.glfwMakeContextCurrent(window);
            if(gameConfig.getBoolean("DisplayVsync", false)) GLFW.glfwSwapInterval(1);
            else GLFW.glfwSwapInterval(0);
            
            GLFW.glfwShowWindow(window);
            GLContext.createFromCurrent();
            audiocontext = ALContext.create();
            audiocontext.makeCurrent();

            GLFW.glfwSetFramebufferSizeCallback(window, (winSizeStr = new GLFWFramebufferSizeCallback()
            {
                @Override
                public void invoke(long window, int width, int height)
                {
                    if(width <= 0 || height <= 0) return;

                    winManager.width = width;
                    winManager.height = height;

                    gameConfig.setProperty("Width", width + "");
                    gameConfig.setProperty("Height", height + "");
                    GL11.glViewport(0, 0, width, height);
                    if(gameHandler != null) gameHandler.onWindowResize(width, height);
                  
                }
            }));

            GLFW.glfwSetCursorPosCallback(window, (curStr = new GLFWCursorPosCallback()
            {
                @Override
                public void invoke(long window, double x, double y)
                {
                    inpManager.curx = x;
                    inpManager.cury = y;
                    if(gameHandler != null) gameHandler.onCursorMove(inpManager.getCursorX(), inpManager.getCursorY());
                }
            }));

            if(inpManager.cursor != -1) GLFW.glfwSetCursor(window, inpManager.cursor);

            GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GL11.glViewport(0, 0, width, height);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            if(MODE == RenderMode.Mode2D) GL11.glOrtho(-1, 1, 1, -1, 1, -1);
            //TODO: Make 3D!    else GL11.glOrtho(-ratio, ratio, -1.f, 1.f, 1.f, -1.f);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            if(gameHandler != null) gameHandler.onWindowOpen();

            windowOpen = true;
        }

        /**
         * Takes a screen-shot and saves it to the file specified as a PNG.
         * @param saveToName The name of the file to save the screen-shot to
         * (excluding file extension).
         */
        public void screenShot(String saveToName)
        {
            screenShot(saveToName, ClientUtils.ImageFormat.PNG);
        }

        /**
         * Takes a screen-shot and saves it to the file specified.
         * @param saveToName The name of the file to save the screen-shot to
         * (excluding file extension).
         * @param format The format to save the image as.
         */
        public void screenShot(String saveToName, ClientUtils.ImageFormat format)
        {
            File saveTo = new File("etc/screenshots/" + saveToName + "." + format.name().toLowerCase());

            GL11.glReadBuffer(GL11.GL_FRONT);
            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
            GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            Thread t = new Thread(() -> 
            {
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                for (int x = 0; x < width; x++) 
                {
                    for (int y = 0; y < height; y++) 
                    {
                        int i = (x + (width * y)) * 4;
                        int r = buffer.get(i) & 0xFF;
                        int g = buffer.get(i + 1) & 0xFF;
                        int b = buffer.get(i + 2) & 0xFF;
                        image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                    }
                }

                try 
                {
                    ImageIO.write(image, format.name(), saveTo);
                    gameLogger.log("Saved screenshot '" + saveTo.getName() + "'!");
                }
                catch (IOException e) 
                {
                    Logger.getErrorLogger().log("Could not save Screenshot to '" + saveTo.getName() + "'! I/O Error has occured!");
                }
            });

            t.start();
        }

        /**
         * Sets the game's global font.
         *
         * @param font The {@link java.awt.Font} to derive from.
         */
        public void setFont(Font font)
        {
            this.font = font;
        }

        /**
         * Sets the game's global font.
         *
         * @param fontLocation The file containing the font.
         */
        public void setFont(File fontLocation)
        {
            try 
            {
                font = Font.createFont(Font.TRUETYPE_FONT, fontLocation);
            }
            catch(FontFormatException e) 
            {
                Logger.getErrorLogger().log("Could not load font from '" + fontLocation.getAbsolutePath() + "'! Invalid Format!");
            }
            catch(IOException e) 
            {
                Logger.getErrorLogger().log("Could not load font from '" + fontLocation.getAbsolutePath() + "'! I/O Error!");
            }
        }
        
        /**
        * Changes the size of the window.
        * @param width New width of the window, measured in pixels.
        * @param height New height of the window, measures in pixels.
        */
        public void setResolution(int width, int height)
        {
            this.width = width;
            this.height = height;
            GLFW.glfwSetWindowSize(window, width, height);
            GL11.glViewport(0, 0, width, height);
        
            gameConfig.setProperty("Width", width + "");
            gameConfig.setProperty("Height", height + "");
        }
    
        /**
        * Changes the state of the window.
        * This method will require the window to restart, and this will be done via {@link wrath.client.Game#destroyWindow() } and {@link wrath.client.Game#initWindow() }.
        * @param state The state to set the window to.
        */
        public void setWindowState(WindowState state)
        {
            gameConfig.setProperty("WindowState", state.toString().toUpperCase());
            if(windowOpen)
            {
                destroyWindow();
                openWindow();
            }
            gameConfig.save();
        }
    }
}