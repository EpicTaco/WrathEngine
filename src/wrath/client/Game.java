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
 *  - Work on Scheduler.
 *  - Work on audio.
 *  - Work on in-game structures (Item framework, Entity framework, etc).
 *  - Add physics.
 *  - Add networking.
 *  - Add encryption.
 *  - Make external modding API.
 */
package wrath.client;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;
import wrath.util.Config;
import wrath.util.Logger;

/**
 * The entry point and base of the game. Make a class extending this and overriding at least render() method.
 * @author EpicTaco
 */
public class Game 
{
    /**
     * Used to differentiate between whether the action should execute when a key is pressed or released.
     */
    public static enum KeyAction {KEY_HOLD_DOWN, KEY_PRESS, KEY_RELEASE;}
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
    private final Logger gameLogger = new Logger("info");
    
    private GLFWErrorCallback errStr;
    private GLFWKeyCallback keyStr;
    private GLFWMouseButtonCallback mkeyStr;
    private GLFWFramebufferSizeCallback winSizeStr;
    
    private boolean isRunning = false;
    private int resWidth = 800;
    private int resHeight = 600;
    private long window;
    private WindowState windowState;
    
    private final HashMap<Integer, IKeyData> keyboardMap = new HashMap<>();
    private final HashMap<Integer, Runnable> persKeyboardMap = new HashMap<>();
    private final HashMap<Integer, IKeyData> mouseMap = new HashMap<>();
    private final HashMap<Integer, Runnable> persMouseMap = new HashMap<>();
    
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
     * Adds a listener to a specified key on the {@link wrath.client.Key}.
     * @param key The key to respond to.
     * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
     * @param event The {@link java.lang.Runnable} event to run after specified button is affected by the specified action.
     */
    public void addKeyboardFunction(int key, KeyAction action, Runnable event)
    {
        if(action == KeyAction.KEY_HOLD_DOWN)
            keyboardMap.put(key, new IKeyData(KeyAction.KEY_PRESS, () ->
            {
                persKeyboardMap.put(key, event);
                event.run();
            }));
        else keyboardMap.put(key, new IKeyData(action, event));
    }
    
    /**
     * Adds a listener to a specified key on the {@link wrath.client.Key}.
     * @param key The key to respond to.
     * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
     * @param event The {@link java.lang.Runnable} event to run after specified button is affected by the specified action.
     */
    public void addMouseFunction(int key, KeyAction action, Runnable event)
    {
        if(action == KeyAction.KEY_HOLD_DOWN)
            mouseMap.put(key, new IKeyData(KeyAction.KEY_PRESS, () ->
            {
                persMouseMap.put(key, event);
                event.run();
            }));
        else mouseMap.put(key, new IKeyData(action, event));
    }
    
    /**
     * Centers the window in the middle of the designated primary monitor.
     */
    public void centerWindow()
    {
        ByteBuffer vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(window, (GLFWvidmode.width(vidmode) - getWindowWidth()) / 2, (GLFWvidmode.height(vidmode) - getWindowHeight()) / 2);
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
     * Gets the height of the rendering resolution.
     * @return Returns the height of the rendering resolution.
     */
    public int getResolutionHeight()
    {
        return resHeight;
    }
    
    /**
     * Gets the width of the rendering resolution.
     * @return Returns the width of the rendering resolution.
     */
    public int getResolutionWidth()
    {
        return resWidth;
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
     * Gets the height, in pixels, of the window.
     * @return Gets the height of the window, measured in pixels.
     */
    public int getWindowHeight()
    {
        IntBuffer height = IntBuffer.allocate(3);
        IntBuffer width = IntBuffer.allocate(3);
        GLFW.glfwGetFramebufferSize(window, width, height);
        
        return height.get(0);
    }
    
    /**
     * Gets the width, in pixels, of the window.
     * @return Returns the width of the window, measured in pixels.
     */
    public int getWindowWidth()
    {
        IntBuffer height = IntBuffer.allocate(3);
        IntBuffer width = IntBuffer.allocate(3);
        GLFW.glfwGetFramebufferSize(window, width, height);
        
        return width.get(0);
    }
    
    /**
     * Returns whether or not the cursor is enabled.
     * @return Returns true if the cursor is enabled, otherwise false.
     */
    public boolean isCursorEnabled()
    {
        return GLFW.glfwGetInputMode(window, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_NORMAL;
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
        //Set-up timing
        while(isRunning && GLFW.glfwWindowShouldClose(window) != GL11.GL_TRUE)
        {
            //TODO: Create timings
            onTickPreprocessor();
            
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            render();
            GLFW.glfwSwapBuffers(window);
            
            GLFW.glfwPollEvents();
        }
        
        stop();
        stopImpl();
    }
    
    /**
     * Override-able method that is called when the game is closed (as soon as the close is requested).
     */
    protected void onGameClose(){}
    
    /**
     * Override-able method that is called when the game is opened (right before the loop initializes.
     */
    protected void onGameOpen(){}
    
    /**
     * Override-able method that is called every time the game's logic is supposed to update.
     */
    protected void onTick(){}
    
    /**
     * Private method that takes care of all background processes before onTick() is called.
     */
    private void onTickPreprocessor()
    {
        persKeyboardMap.entrySet().stream().map((pairs) -> (Runnable) pairs.getValue()).forEach((ev) -> 
        {
            ev.run();
        });

        persMouseMap.entrySet().stream().map((pairs) -> (Runnable) pairs.getValue()).forEach((ev) -> 
        {
            ev.run();
        });
        
        onTick();
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
     * Override-able method that is called as much as possible to issue rendering commands.
     */
    protected void render(){}
    
    /**
     * Enables or disables the cursor.
     * @param cursorEnabled Whether the cursor should be enabled or disabled.
     */
    public void setCursorEnabled(boolean cursorEnabled)
    {
        if(cursorEnabled) GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        else GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }
    
    /**
     * Sets the rendering resolution as well as the window size of the window.
     * @param width The width, in pixels, of the resolution.
     * @param height The height, in pixels, of the resolution.
     */
    public void setResolution(int width, int height)
    {
        resWidth = width;
        resHeight = height;
        //TODO: Adjust rendering resolution via OpenGL!
    }
    
    /**
     * Changes the size of the window.
     * @param width New width of the window, measured in pixels.
     * @param height New height of the window, measures in pixels.
     */
    public void setWindowResolution(int width, int height)
    {
        GLFW.glfwSetWindowSize(window, width, height);
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
            stopImpl();
            return;
        }
        
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, ClientUtils.getLWJGLBoolean(gameConfig.getBoolean("WindowResizable", true)));
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, ClientUtils.getLWJGLBoolean(gameConfig.getBoolean("APIForwardCompatMode", false)));
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, ClientUtils.getLWJGLBoolean(gameConfig.getBoolean("DebugMode", false)));
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, gameConfig.getInt("DisplaySamples", 0));
        GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, gameConfig.getInt("DisplayRefreshRate", 0));
        
        if(gameConfig.getString("WindowState", "Windowed").equalsIgnoreCase("windowed")) windowState = WindowState.WINDOWED;
        else if(gameConfig.getString("WindowState", "Windowed").equalsIgnoreCase("windowed_undecorated")) windowState = WindowState.WINDOWED_UNDECORATED;
        else if(gameConfig.getString("WindowState", "Windowed").equalsIgnoreCase("fullscreen_windowed")) windowState = WindowState.FULLSCREEN_WINDOWED;
        else if(gameConfig.getString("WindowState", "Windowed").equalsIgnoreCase("fullscreen")) windowState = WindowState.FULLSCREEN;
        
        resWidth = gameConfig.getInt("ResolutionWidth", 800);
        resHeight = gameConfig.getInt("ResolutionHeight", 600);
        
        if(windowState == WindowState.FULLSCREEN)
        {
            ByteBuffer videomode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            window = GLFW.glfwCreateWindow(GLFWvidmode.width(videomode), GLFWvidmode.height(videomode), TITLE, GLFW.glfwGetPrimaryMonitor(), MemoryUtil.NULL);
        }
        else if(windowState == WindowState.FULLSCREEN_WINDOWED)
        {
            ByteBuffer videomode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GL11.GL_FALSE);
            window = GLFW.glfwCreateWindow(GLFWvidmode.width(videomode), GLFWvidmode.height(videomode), TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
        }
        else if(windowState == WindowState.WINDOWED)
        {
            if(!gameConfig.getBoolean("ResolutionIsWindowSize", true)) window = GLFW.glfwCreateWindow(gameConfig.getInt("WindowWidth", 800), gameConfig.getInt("WindowHeight", 600), TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
            else window = GLFW.glfwCreateWindow(gameConfig.getInt("ResolutionWidth", 800), gameConfig.getInt("ResolutionHeight", 600), TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
        }
        else if(windowState == WindowState.WINDOWED_UNDECORATED)
        {
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GL11.GL_FALSE);
            if(!gameConfig.getBoolean("ResolutionIsWindowSize", true)) window = GLFW.glfwCreateWindow(gameConfig.getInt("WindowWidth", 800), gameConfig.getInt("WindowHeight", 600), TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
            else window = GLFW.glfwCreateWindow(gameConfig.getInt("ResolutionWidth", 800), gameConfig.getInt("ResolutionHeight", 600), TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
        }
        
        if(window == MemoryUtil.NULL)
        {
            Logger.getErrorLogger().log("Could not initialize window! Window Info[(" + resWidth + "x" + resHeight + ")@(" + gameConfig.getInt("WindowWidth", 800) + "x" + gameConfig.getInt("WindowHeight", 600) + ")]");
            stopImpl();
        }
        
        GLFW.glfwSetKeyCallback(window, (keyStr = new GLFWKeyCallback()
        {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods)
            {
                if(persKeyboardMap.containsKey(key) && action == GLFW.GLFW_RELEASE) persKeyboardMap.remove(key);
                if(keyboardMap.containsKey(key))
                {
                    IKeyData dat = keyboardMap.get(key);
                    if(dat.getRawAction() == action) dat.execute();
                }
            }
        }));
        
        GLFW.glfwSetMouseButtonCallback(window, (mkeyStr = new GLFWMouseButtonCallback() 
        {
            @Override
            public void invoke(long window, int button, int action, int mods) 
            {
                if(persMouseMap.containsKey(button) && action == GLFW.GLFW_RELEASE) persMouseMap.remove(button);
                if(mouseMap.containsKey(button))
                {
                    IKeyData dat = mouseMap.get(button);
                    if(dat.getRawAction() == action) dat.execute();
                }
            }
        }));
        
        GLFW.glfwMakeContextCurrent(window);
        if(gameConfig.getBoolean("DisplayVsync", true)) GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);
        GLContext.createFromCurrent();
        
        GLFW.glfwSetFramebufferSizeCallback(window,(winSizeStr = new GLFWFramebufferSizeCallback() 
        {
            @Override
            public void invoke(long window, int width, int height) 
            {
                if(gameConfig.getBoolean("ResolutionIsWindowSize", true))
                    GL11.glViewport(0, 0, width, height);
            }
        }));
                
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        onGameOpen();
        loop();
    }
    
    /**
     * Method that flags the game to stop.
     */
    public void stop()
    {
        if(!isRunning) return;
        
        onGameClose();
        gameLogger.log("Stopping '" + TITLE + "' Client v." + VERSION + "!");
        isRunning = false;
    }
    
    /**
     * Method to stop the game and close all of it's resources.
     */
    private void stopImpl()
    {
        try{
        winSizeStr.release();
        keyStr.release();
        mkeyStr.release();
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
        
        gameConfig.save();
        if(gameLogger != null && !gameLogger.isClosed()) gameLogger.close();
        errStr.release();
        }catch(Exception e){}
        
        System.exit(0);
    }
    
    /**
     * Internal class to store Key Data in a hash map.
     */
    private class IKeyData
    {
        private final KeyAction action;
        private final int actionRaw;
        private final Runnable event;

        /**
         * Constructor.
         * @param action The {@link wrath.client.Game.KeyAction} to trigger the event on.
         * @param event The action to execute when the specified key's specified action occurs.
         */
        public IKeyData(KeyAction action, Runnable event)
        {
            this.action = action;
            
            if(action == KeyAction.KEY_RELEASE) this.actionRaw = GLFW.GLFW_RELEASE;
                else this.actionRaw = GLFW.GLFW_PRESS;
            
            this.event = event;
        }
        
        /**
         * Executes the event specified in the constructor, as if the action was triggered by the input.
         */
        public void execute()
        {
            event.run();
        }
        
        /**
         * Gets the {@link wrath.client.Game.KeyAction} of the key set.
         * This is used to determine whether to execute the event when a key is pressed, or execute the event when the key is released.
         * @return Returns the {@link wrath.client.Game.KeyAction} specified in the Constructor.
         */
        public KeyAction getAction()
        {
            return action;
        }
        
        /**
         * Used by the internal engine to obtain the GLFW action code.
         * @return Returns the int version of the KeyAction for use with GLFW.
         */
        public int getRawAction()
        {
            return actionRaw;
        }
    }
}