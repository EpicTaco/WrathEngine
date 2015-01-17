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
 *  - Work on audio
 *  - Add an in-game structures (Item framework, Entity framework, etc)
 *  - Add physics
 *  - Add networking
 *  - Add encryption
 *  - Add external modding API
 *  - Add GUI Toolkits.
 */
package wrath.client;

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
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;
import wrath.client.Key.KeyAction;
import wrath.common.scheduler.Scheduler;
import wrath.util.Config;
import wrath.util.Logger;

/**
 * The entry point and base of the game. Make a class extending this and overriding at least render() method.
 * @author EpicTaco
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
    private final Logger gameLogger = new Logger("info");
    private final Scheduler gameScheduler = new Scheduler();
    
    private GLFWErrorCallback errStr;
    private GLFWKeyCallback keyStr;
    private GLFWMouseButtonCallback mkeyStr;
    private GLFWFramebufferSizeCallback winSizeStr;
    
    private float fps = 0;
    private boolean isRunning = false;
    private int resWidth = 800;
    private int resHeight = 600;
    private long window;
    private boolean windowOpen = false;
    private WindowState windowState = null;
    private int winWidth = 800;
    private int winHeight = 600;
    private float ratio = resWidth / resHeight;
    
    private final HashMap<Integer, KeyData> keyboardMap = new HashMap<>();
    private final HashMap<Integer, Runnable> persKeyboardMap = new HashMap<>();
    private final HashMap<Integer, KeyData> mouseMap = new HashMap<>();
    private final HashMap<Integer, Runnable> persMouseMap = new HashMap<>();
    private final HashMap<String, Runnable> savedFuncMap = new HashMap<>();
    
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
     * @param key The {@link wrath.client.Key} to respond to.
     * @param keyMod The {@link wrath.client.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
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
     * Adds a listener to a specified key on the {@link wrath.client.Key}.
     * @param key The key to respond to.
     * @param keyMod The {@link wrath.client.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
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
     * Adds a listener to a specified key on the {@link wrath.client.Key}.
     * @param key The key to respond to.
     * @param keyMod The {@link wrath.client.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
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
     * Adds a listener to a specified key on the {@link wrath.client.Key}.
     * @param key The key to respond to.
     * @param keyMod The {@link wrath.client.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
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
        
        winSizeStr.release();
        keyStr.release();
        mkeyStr.release();
        GLFW.glfwDestroyWindow(window);
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
     * Gets the last recorded FPS count.
     * @return Returns the last FPS count.
     */
    public float getFPS()
    {
        return fps;
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
     * Returns the resolution ratio for (primarily) use with glOrtho().
     * Calculated as resolution_width/resolution_height.
     * @return Returns the rendering ratio.
     */
    public float getRenderingRatio()
    {
        return ratio;
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
     * Returns the height of the window.
     * @return Returns the height of the window.
     */
    public int getWindowHeight()
    {
        return winHeight;
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
    public int getWindowWidth()
    {
        return winWidth;
    }
    
    /**
     * Gets the current state of the window as of {@link wrath.client.Game.WindowState}.
     * @return Returns the current state of the window.
     */
    public WindowState getWindowState()
    {
        return windowState;
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
     * Tells whether or not the window is open.
     * @return Returns true if the window is open, otherwise false.
     */
    public boolean isWindowOpen()
    {
        return windowOpen;
    }
    
    /**
     * Private method to start the display.
     * Made independent from start() so window options can be adjusted real-time.
     */
    public void initWindow()
    {
        if(windowOpen) return;
        
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, ClientUtils.getLWJGLBoolean(gameConfig.getBoolean("WindowResizable", true)));
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, ClientUtils.getLWJGLBoolean(gameConfig.getBoolean("APIForwardCompatMode", false)));
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, ClientUtils.getLWJGLBoolean(gameConfig.getBoolean("DebugMode", false)));
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, gameConfig.getInt("DisplaySamples", 0));
        GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, gameConfig.getInt("DisplayRefreshRate", 0));

        String wstatestr = gameConfig.getString("WindowState", "Windowed");
        
        if(wstatestr.equalsIgnoreCase("windowed")) windowState = WindowState.WINDOWED;
        else if(wstatestr.equalsIgnoreCase("windowed_undecorated")) windowState = WindowState.WINDOWED_UNDECORATED;
        else if(wstatestr.equalsIgnoreCase("fullscreen_windowed")) windowState = WindowState.FULLSCREEN_WINDOWED;
        else if(wstatestr.equalsIgnoreCase("fullscreen")) windowState = WindowState.FULLSCREEN;
        
        resWidth = gameConfig.getInt("ResolutionWidth", 800);
        resHeight = gameConfig.getInt("ResolutionHeight", 600);
        
        if(windowState == WindowState.FULLSCREEN)
        {
            ByteBuffer videomode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            winWidth = GLFWvidmode.width(videomode);
            winHeight = GLFWvidmode.height(videomode);
            window = GLFW.glfwCreateWindow(winWidth, winHeight, TITLE, GLFW.glfwGetPrimaryMonitor(), MemoryUtil.NULL);
        }
        else if(windowState == WindowState.FULLSCREEN_WINDOWED)
        {
            ByteBuffer videomode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            winWidth = GLFWvidmode.width(videomode);
            winHeight = GLFWvidmode.height(videomode);
            
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GL11.GL_FALSE);
            window = GLFW.glfwCreateWindow(winWidth, winHeight, TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
        }
        else if(windowState == WindowState.WINDOWED)
        {
            if(!gameConfig.getBoolean("ResolutionIsWindowSize", true))
            {
                winWidth = gameConfig.getInt("WindowWidth", 800);
                winHeight = gameConfig.getInt("WindowHeight", 600);
                window = GLFW.glfwCreateWindow(winWidth, winHeight, TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
            }
            else
            {
                winWidth = resWidth;
                winHeight = resHeight;
                window = GLFW.glfwCreateWindow(winWidth, winHeight, TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
            }
        }
        else if(windowState == WindowState.WINDOWED_UNDECORATED)
        {
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GL11.GL_FALSE);
            
            if(!gameConfig.getBoolean("ResolutionIsWindowSize", true))
            {
                winWidth = gameConfig.getInt("WindowWidth", 800);
                winHeight = gameConfig.getInt("WindowHeight", 600);
                window = GLFW.glfwCreateWindow(winWidth, winHeight, TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
            }
            else
            {
                winWidth = resWidth;
                winHeight = resHeight;
                window = GLFW.glfwCreateWindow(winWidth, winHeight, TITLE, MemoryUtil.NULL, MemoryUtil.NULL);
            }
        }
        
        if(window == MemoryUtil.NULL)
        {
            Logger.getErrorLogger().log("Could not initialize window! Window Info[(" + resWidth + "x" + resHeight + ")@(" + winWidth + "x" + winHeight + ")]");
            ClientUtils.throwInternalError("Window failed to initialize!", false);
            stopImpl();
        }
        
        GLFW.glfwSetKeyCallback(window, (keyStr = new GLFWKeyCallback()
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
        
        GLFW.glfwSetMouseButtonCallback(window, (mkeyStr = new GLFWMouseButtonCallback() 
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
        
        GLFW.glfwMakeContextCurrent(window);
        if(gameConfig.getBoolean("DisplayVsync", true)) GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);
        GLContext.createFromCurrent();
        
        GLFW.glfwSetFramebufferSizeCallback(window,(winSizeStr = new GLFWFramebufferSizeCallback() 
        {
            @Override
            public void invoke(long window, int width, int height) 
            {
                if(width <= 0 || height <= 0) return;
                
                GL11.glViewport(0, 0, width, height);
                winWidth = width;
                winHeight = height;
                
                gameConfig.setProperty("WindowWidth", width + "");
                gameConfig.setProperty("WindowHeight", height + "");
                
                if(gameConfig.getBoolean("ResolutionIsWindowSize", true))
                {
                    GL11.glMatrixMode(GL11.GL_PROJECTION);
                    GL11.glLoadIdentity();
                    
                    resWidth = winWidth;
                    resHeight = winHeight;
                    ratio = resWidth / resHeight;
                    if(MODE == RenderMode.Mode2D) GL11.glOrtho(-ratio, ratio, -1.f, 1.f, 1.f,- 1.f);
                    //TODO: Make 3D!    else GL11.glOrtho(0.0f, resWidth, resHeight, 0.0f, 0.0f, 1.0f);
                    
                    gameConfig.setProperty("ResolutionWidth", width + "");
                    gameConfig.setProperty("ResolutionHeight", height + "");
                }
            }
        }));
        
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glViewport(0, 0, winWidth, winHeight);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        ratio = resWidth / resHeight;
        if(MODE == RenderMode.Mode2D) GL11.glOrtho(-ratio, ratio, -1.f, 1.f, 1.f,- 1.f);
        //TODO: Make 3D!    else GL11.glOrtho(0.0f, resWidth, resHeight, 0.0f, 0.0f, 1.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        
        windowOpen = true;
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
        double checksPerSec = gameConfig.getDouble("PersInputCheckPerSecond", 15.0);
        if(checksPerSec > TPS) checksPerSec = TPS;
        final double INPUT_CHECK_TICKS = TPS / checksPerSec;
        
        //Timings
        long last = System.nanoTime();
        final double conv = 1000000000.0 / TPS;
        double delta = 0.0;
        long now;
        
        while(isRunning && (!windowOpen || GLFW.glfwWindowShouldClose(window) != GL11.GL_TRUE))
        {
            now = System.nanoTime();
            delta += (now - last) / conv;
            last = now;
            
            while(delta >= 1)
            {
                onTickPreprocessor();
                
                if(INPUT_CHECK_TICKS == 1 || inpCount >= INPUT_CHECK_TICKS)
                {
                    persKeyboardMap.entrySet().stream().map((pairs) -> (Runnable) pairs.getValue()).forEach((ev) -> 
                    {
                        ev.run();
                    });

                    persMouseMap.entrySet().stream().map((pairs) -> (Runnable) pairs.getValue()).forEach((ev) -> 
                    {
                        ev.run();
                    });
                    
                    inpCount -= INPUT_CHECK_TICKS;
                }
                else inpCount++;
                
                if(fpsCount >= TPS)
                {
                    fps = fpsBuf;
                    fpsBuf = 0;
                    fpsCount-=TPS;
                }
                else fpsCount++;
                
                delta--;
            }
            
            if(windowOpen)
            {
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                render();
                GL11.glFlush();
                fpsBuf++;
                GLFW.glfwSwapBuffers(window);
            
                GLFW.glfwPollEvents();
            }
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
        gameScheduler.onTick();
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
     * Takes a screen-shot and saves it to the file specified as a PNG.
     * @param saveToName The name of the file to save the screen-shot to (excluding file extension).
     */
    public void screenShot(String saveToName)
    {
        screenShot(saveToName, ClientUtils.ImageFormat.PNG);
    }
    
    /**
     * Takes a screen-shot and saves it to the file specified.
     * @param saveToName The name of the file to save the screen-shot to (excluding file extension).
     * @param format The format to save the image as.
     */
    public void screenShot(String saveToName, ClientUtils.ImageFormat format)
    {
        File saveTo = new File("etc/screenshots/" + saveToName + "." + format.name().toLowerCase());
        
        int width = this.resWidth;
        int height = this.resHeight;
                
        GL11.glReadBuffer(GL11.GL_FRONT);
        ByteBuffer buffer = BufferUtils.createByteBuffer(resWidth * resHeight * 4);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
 
        Thread t = new Thread(() -> 
        {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
            for(int x = 0; x < width; x++)
            {
                for(int y = 0; y < height; y++)
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
            catch(IOException e)
            {
                Logger.getErrorLogger().log("Could not save Screenshot to '" + saveTo.getName() + "'! I/O Error has occured!");
            } 
        });
        
        t.start();
    }
    
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
        ratio = resWidth / resHeight;
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        if(MODE == RenderMode.Mode2D) GL11.glOrtho(-ratio, ratio, -1.f, 1.f, 1.f,- 1.f);
        //TODO: Make 3D!    else GL11.glOrtho(0.0f, resWidth, resHeight, 0.0f, 0.0f, 1.0f);
        
        gameConfig.setProperty("ResolutionWidth", width + "");
        gameConfig.setProperty("ResolutionHeight", height + "");
        
        if(gameConfig.getBoolean("ResolutionIsWindowSize", true))
        {
            winWidth = resWidth;
            winHeight = resHeight;
            GLFW.glfwSetWindowSize(window, width, height);
            GL11.glViewport(0, 0, width, height);
            
            gameConfig.setProperty("WindowWidth", width + "");
            gameConfig.setProperty("WindowHeight", height + "");
        }

    }
    
    /**
     * Changes the size of the window.
     * @param width New width of the window, measured in pixels.
     * @param height New height of the window, measures in pixels.
     */
    public void setWindowResolution(int width, int height)
    {
        winWidth = width;
        winHeight = height;
        GLFW.glfwSetWindowSize(window, width, height);
        GL11.glViewport(0, 0, width, height);
        
        gameConfig.setProperty("WindowWidth", width + "");
        gameConfig.setProperty("WindowHeight", height + "");
        
        if(gameConfig.getBoolean("ResolutionIsWindowSize", true))
        {
            resWidth = winWidth;
            resHeight = winHeight;
            ratio = resWidth / resHeight;
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            if(MODE == RenderMode.Mode2D) GL11.glOrtho(-ratio, ratio, -1.f, 1.f, 1.f,- 1.f);
            //TODO: Make 3D!    else GL11.glOrtho(0.0f, resWidth, resHeight, 0.0f, 0.0f, 1.0f);
            
            gameConfig.setProperty("ResolutionWidth", width + "");
            gameConfig.setProperty("ResolutionHeight", height + "");
        }
    }
    
    /**
     * Changes the state of the window.
     * This method will require the window to restart, and this will be done via {@link wrath.client.Game#destroyWindow() } and {@link wrath.client.Game#initWindow() }.
     * @param state The state to set the window to.
     */
    public void setWindowState(WindowState state)
    {
        gameConfig.setProperty("WindowState", state.toString().toUpperCase());
        destroyWindow();
        initWindow();
        gameConfig.save();
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
        
        initWindow();
        
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
        destroyWindow();
        GLFW.glfwTerminate();
        
        gameConfig.save();
        if(gameLogger != null && !gameLogger.isClosed()) gameLogger.close();
        errStr.release();
        }catch(Exception e){}
        
        System.exit(0);
    }
}