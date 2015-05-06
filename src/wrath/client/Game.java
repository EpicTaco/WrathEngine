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
package wrath.client;

import java.awt.Font;
import java.awt.FontFormatException;
import wrath.client.input.InputManager;
import wrath.client.events.GameEventHandler;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.Sys;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALContext;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.system.MemoryUtil;
import wrath.client.events.InputEventHandler;
import wrath.client.events.PlayerEventHandler;
import wrath.common.scheduler.Scheduler;
import wrath.common.scripts.ScriptManager;
import wrath.util.Config;
import wrath.util.Logger;

/**
 * The entry point and base of the game. Make a class extending this and overriding at least render() method.
 * @author Trent Spears
 */
public class Game 
{   
    private final RenderMode MODE;
    private final String TITLE;
    private final double TPS;
    private final String VERSION;

    private final Config gameConfig = new Config("game");
    private final Logger gameLogger = new Logger("info");
    private final Scheduler gameScheduler = new Scheduler();
    
    private GLFWErrorCallback errStr;
    private GLFWFramebufferSizeCallback winSizeStr;
    
    private ALContext audiocontext;
    private boolean isRunning = false;
    
    private final EventManager evManager;
    private final InputManager inpManager;
    private final WindowManager winManager;
    
    
    /**
     * Constructor.
     * Describes all the essential and unmodifiable variables of the Game.
     * @param gameTitle Title of the Game.
     * @param version Version of the Game.
     * @param ticksPerSecond The amount of times the logic of the game should update in one second. Recommended 30.
     * @param renderMode Describes how to game should be rendered (2D or 3D).
     */
    public Game(String gameTitle, String version, double ticksPerSecond, String renderMode)
    {
        MODE = RenderMode.valueOf(renderMode);
        TITLE = gameTitle;
        VERSION = version;
        TPS = ticksPerSecond;
        this.evManager = new EventManager();
        this.inpManager = new InputManager(this);
        this.winManager = new WindowManager();
        
        File nativeDir = new File("assets/native");
        if(!nativeDir.exists())
            ClientUtils.throwInternalError("Missing assets folder! Try re-downloading!", true);
        
        System.setProperty("org.lwjgl.librarypath", "assets/native");
        
        File screenshotDir = new File("etc/screenshots");
        if(!screenshotDir.exists()) screenshotDir.mkdirs();
    }
    
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
        this.evManager = new EventManager();
        this.inpManager = new InputManager(this);
        this.winManager = new WindowManager();
        
        File nativeDir = new File("assets/native");
        if(!nativeDir.exists())
            ClientUtils.throwInternalError("Missing assets folder! Try re-downloading!", true);
        
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
     * Gets the {@link wrath.client.Game.EventManager} class that manages all event handlers.
     * This class is used to control, access and change Event Handlers from the {@link wrath.client.handlers} package.
     * @return Returns the {@link wrath.client.Game.EventManager} class that manages all event handlers.
     */
    public EventManager getEventManager()
    {
        return evManager;
    }
    
    /**
     * Gets the {@link wrath.util.Logger} associated with this Game.
     * @return Returns the {@link wrath.util.Logger} associated with this Game.
     */
    public Logger getGameLogger()
    {
        return gameLogger;
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
     * Gets the standard info {@link wrath.util.Logger} for the game.
     * @return Returns the standard info {@link wrath.util.Logger} for the game.
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
     * Recommended to not be over 64 or under 10.
     * If the TPS is set over 60 and VSync is on, the ticks will be forced to 60 TPS.
     * Unfortunately, there are not any good ways to overcome said bug, though I am looking into potential solutions.
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
        int afpsCount = 0;
        double fpsBuf = 0;
        int fpsCount = 0;
        
        //Input Checking
        int inpCount = 0;
        double checksPerSec = gameConfig.getDouble("PersistentInputChecksPerSecond", 10.0);
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
            
            //Tick occurs
            while(delta >= 1)
            {
                onTickPreprocessor();
                
                //Persistent input management
                if(INPUT_CHECK_TICKS == 1 || inpCount >= INPUT_CHECK_TICKS)
                {
                    inpManager.onPersistentInput();
                    inpCount -= INPUT_CHECK_TICKS;
                }
                else inpCount++;
                
                //FPS Counter
                if(winManager.windowOpen)
                    if(fpsCount >= TPS)
                    {
                        afpsCount++;
                        winManager.fps = fpsBuf;
                        winManager.avgFps = winManager.totalFramesRendered / afpsCount;
                        fpsBuf = 0;
                        fpsCount-=TPS;
                    }
                    else fpsCount++;
                
                delta--;
            }
            
            //While the window is open
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
                winManager.totalFramesRendered++;
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
        evManager.getGameEventHandler().onTick();
    }
    
    /**
     * Override-able method that is called as much as possible to issue rendering commands.
     */
    protected void render(){} //TODO: Add Rendering Pipeline (kind of a biggie)
    
    /**
     * Method that is used to load the game and all of it's resources.
     */
    public void start()
    {
        start(new String[0]);
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
            if(b.length <= 1) continue;
            
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
        
        evManager.getGameEventHandler().onGameOpen();
        inpManager.loadKeys();
        loop();
    }
    
    /**
     * Method that flags the game to stop.
     */
    public void stop()
    {
        if(!isRunning) return;
        evManager.getGameEventHandler().onGameClose();
        isRunning = false;
    }
    
    /**
     * Method to stop the game and close all of it's resources.
     */
    private void stopImpl()
    {
        try{
        winManager.closeWindow();
        inpManager.closeInput(true);
        GLFW.glfwTerminate();
        
        gameConfig.save();
        inpManager.saveKeys();
        gameLogger.log("Stopping '" + TITLE + "' Client v." + VERSION + "!");
        if(gameLogger != null && !gameLogger.isClosed()) gameLogger.close();
        errStr.release();
        }catch(Exception e){}
        
        ScriptManager.closeScripting();
        
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
         * @return Returns the OpenGL Texture ID of the background image, 0 if no image was set.
         */
        public int getBackgroundTextureID()
        {
            return backTexture;
        }
        
        /**
         * Changes the static background image.
         * @param imageFile The file to load the background texture from.
         * @return Returns the OpenGL Texture ID number for the background image being set.
         */
        public int setBackgroundImage(File imageFile)
        {
             return setBackgroundImage(ClientUtils.loadImageFromFile(imageFile));
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
     * Class to manage all event handlers from the {@link wrath.client.handlers} package.
     */
    public class EventManager
    {
        /**
         * Constructor.
         * Protected so multiple instances aren't made pointlessly.
         */
        protected EventManager(){}
        
        private final ArrayList<GameEventHandler> gameHandlers = new ArrayList<>();
        private final ArrayList<InputEventHandler> inpHandlers = new ArrayList<>();
        private final ArrayList<PlayerEventHandler> plrHandlers = new ArrayList<>();
        
        private final GameEventHandler ghan = new RootGameEventHandler();
        private final InputEventHandler ihan = new RootInputEventHandler();
        private final PlayerEventHandler phan = new RootPlayerEventHandler();
        
        /**
         * Sets the {@link wrath.client.handlers.GameEventHandler} to associate with this Game.
         * @param handler The {@link wrath.client.handlers.GameEventHandler} to add to the list of handlers that handles all of this Game's events.
         */
        public void addGameEventHandler(GameEventHandler handler)
        {
            gameHandlers.add(handler);
        }
        
        /**
         * Sets the {@link wrath.client.handlers.InputEventHandler} to associate with this Game's Input Manager.
         * @param handler The {@link wrath.client.handlers.InputEventHandler} to add to the list of handlers that handles all of this Game's Input events.
         */
        public void addInputEventHandler(InputEventHandler handler)
        {
            inpHandlers.add(handler);
        }
        
        /**
         * Sets the {@link wrath.client.handlers.PlayerEventHandler} to associate with the player.
         * @param handler The {@link wrath.client.handlers.PlayerEventHandler} to add to the list of handlers that handles all of the Player's events.
         */
        public void addPlayerEventHandler(PlayerEventHandler handler)
        {
            plrHandlers.add(handler);
        }
        
        /**
         * Gets the root {@link wrath.client.handlers.GameEventHandler} linked to this Game.
         * @return Returns the root {@link wrath.client.handlers.GameEventHandler} linked to this Game.
         */
        public GameEventHandler getGameEventHandler()
        {
            return ghan;
        }
        
        /**
         * Gets the root {@link wrath.client.handlers.InputEventHandler}s linked to this Game's Input Manager.
         * @return Returns the root {@link wrath.client.handlers.GameEventHandler} linked to this Game's Input Manager.
         */
        public InputEventHandler getInputEventHandler()
        {
            return ihan;
        }
        
        /**
         * Gets the root  {@link wrath.client.handlers.PlayerEventHandler} linked to the player.
         * @return Returns the {@link wrath.client.handlers.GameEventHandler}s linked to the player.
         */
        public PlayerEventHandler getPlayerEventHandler()
        {
            return phan;
        }
    }
    
    /**
     * Class to define Graphical User Interface (GUI) of the game.
     * This *will* include method to control pop-ups, sub-windows, etc.
     */
    public class GUI
    {
        /**
         * Constructor.
         * Protected so multiple instances aren't made pointlessly.
         */
        protected GUI(){}
        
        /**
         * Method to render the GUI defined by the class.
         */
        private void renderGUI()
        {
            //This is just the outline of what is to come.
        }
    }
    
    private class RootGameEventHandler implements GameEventHandler
    {

        @Override
        public void onGameClose()
        {
            evManager.gameHandlers.stream().forEach((handler) -> 
            {
                handler.onGameClose();
            });
        }

        @Override
        public void onGameOpen()
        {
            evManager.gameHandlers.stream().forEach((handler) -> 
            {
                handler.onGameOpen();
            });
        }

        @Override
        public void onTick()
        {
            evManager.gameHandlers.stream().forEach((handler) -> 
            {
                handler.onTick();
            });
        }

        @Override
        public void onWindowOpen()
        {
            evManager.gameHandlers.stream().forEach((handler) -> 
            {
                handler.onWindowOpen();
            });
        }

        @Override
        public void onResolutionChange(int oldWidth, int oldHeight, int newWidth, int newHeight)
        {
            evManager.gameHandlers.stream().forEach((handler) -> 
            {
                handler.onResolutionChange(oldWidth, oldHeight, newWidth, newHeight);
            });
        }
        
    }
    
    private class RootInputEventHandler implements InputEventHandler
    {

        @Override
        public void onCharInput(char c)
        {
            evManager.inpHandlers.stream().forEach((handler) -> 
            {
                handler.onCharInput(c);
            });
        }

        @Override
        public void onCursorMove(double x, double y)
        {
            evManager.inpHandlers.stream().forEach((handler) -> 
            {
                handler.onCursorMove(x, y);
            });
        }

        @Override
        public void onScroll(double xoffset, double yoffset)
        {
            evManager.inpHandlers.stream().forEach((handler) -> 
            {
                handler.onScroll(xoffset, yoffset);
            });
        }
        
    }
    
    private class RootPlayerEventHandler implements PlayerEventHandler
    {
        
    }
    
    /**
     * Class to manage anything to do with the Game Window.
     */
    public class WindowManager
    {
        private double avgFps = 0;
        private Font font = new Font("Times New Roman", Font.PLAIN, 16);
        private double fps = 0;
        private int totalFramesRendered = 0;
        private long window;
        private boolean windowOpen = false;
        private WindowState windowState = null;
        
        private final Background back = new Background();
        private final GUI front = new GUI();

        private int width = 800;
        private int height = 600;

        /**
         * Constructor.
         * Protected so multiple instances aren't made pointlessly.
         */
        protected WindowManager(){}
        
        /**
        * Centers the window in the middle of the designated primary monitor.
        * Does not work in Fullscreen or Fulscreen_Windowed modes.
        */
        public void centerWindow()
        {
            if(windowState == WindowState.FULLSCREEN || windowState == WindowState.FULLSCREEN_WINDOWED) return;
            
            ByteBuffer vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            GLFW.glfwSetWindowPos(window, (GLFWvidmode.width(vidmode) / 2) - (width / 2), (GLFWvidmode.height(vidmode) / 2) - (height / 2));
        }
    
        /**
        * Destroys and deallocates all GLFW/window resources.
        */
        public void closeWindow()
        {
            if(!windowOpen) return;
            windowOpen = false;
        
            gameLogger.log("Closing window [" + width + "x" + height + "]");
        
            winSizeStr.release();
            inpManager.closeInput(false);
            AL.destroy(audiocontext);
            GLFW.glfwDestroyWindow(window);
            
            gameConfig.save();
        }
        
        /**
         * Gets the average FPS of the game while it has been running.
         * @return Returns the average FPS of the game while it has been running.
         */
        public double getAverageFPS()
        {
            return avgFps;
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
        public double getFPS()
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
         * Returns the height of the window.
         * @return Returns the height (in pixels) of the window.
         */
        public int getHeight()
        {
            return height;
        }
        
        /**
         * Gets the amount of frames the game has rendered since it launched.
         * @return Returns the amount of frames the game has rendered since it launched.
         */
        public int getTotalFramesRendered()
        {
            return totalFramesRendered;
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
         * @return Returns the width (in pixels) of the window.
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
         * Force minimizes the window.
         */
        public void minimizeWindow()
        {
            if(!windowOpen) return;
            
            GLFW.glfwIconifyWindow(window);
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

            windowState = WindowState.valueOf(gameConfig.getString("WindowState", "fullscreen_windowed").toUpperCase());
            
            if(windowState == WindowState.FULLSCREEN) 
            {
                ByteBuffer videomode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
                int twidth = GLFWvidmode.width(videomode);
                int theight = GLFWvidmode.height(videomode);
                if(!gameConfig.getBoolean("FullscreenUsesResolution", false))
                {
                    gameConfig.setProperty("Width", twidth + "");
                    gameConfig.setProperty("Height", theight + "");
                    width = twidth;
                    height = theight;
                }

                window = GLFW.glfwCreateWindow(twidth, theight, TITLE, GLFW.glfwGetPrimaryMonitor(), MemoryUtil.NULL);
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

            gameLogger.log("Opened window [" + width + "x" + height + "] in " + windowState.toString().toUpperCase() + " mode.");

            inpManager.openInput();

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

                    int ow = winManager.width;
                    int oh = winManager.height;
                    winManager.width = width;
                    winManager.height = height;

                    gameConfig.setProperty("Width", width + "");
                    gameConfig.setProperty("Height", height + "");
                    GL11.glViewport(0, 0, width, height);
                    evManager.getGameEventHandler().onResolutionChange(ow, oh, width, height);
                  
                }
            }));

            GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GL11.glViewport(0, 0, width, height);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            if(MODE == RenderMode.Mode2D) GL11.glOrtho(-1, 1, 1, -1, 1, -1);
            //TODO: Make 3D!    else GL11.glOrtho(-ratio, ratio, -1.f, 1.f, 1.f, -1.f);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            if(gameConfig.getBoolean("FullscreenUsesResolution", false) && gameConfig.getString("WindowState").equalsIgnoreCase("Fullscreen"))
            {
                winManager.setResolution(gameConfig.getInt("Width", 800), gameConfig.getInt("Height", 600));
                width = gameConfig.getInt("Width", 800);
                height = gameConfig.getInt("Height", 600);
            }
            
            centerWindow();
            
            evManager.getGameEventHandler().onWindowOpen();

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
         * @param saveToName The name of the file to save the screen-shot to (excluding file extension).
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
                BufferedImage image = ClientUtils.getByteBufferToImage(buffer, width, height);
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
                closeWindow();
                openWindow();
            }
        }
    }
}