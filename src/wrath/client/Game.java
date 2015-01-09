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

import wrath.util.Config;
import wrath.util.Logger;

/**
 * The entry point and base of the game. Make a class extending this and overriding at least render() method.
 * @author EpicTaco
 */
public class Game 
{
    /**
     * Enumerator describing what Operating Platform the game is running off of.
     */
    public static enum Platform {LINUX, MACOS, SOLARIS, WINDOWS;}
    /**
     * Enumerator describing whether the game should be run in 2D Mode or 3D Mode.
     */
    public static enum RenderMode {Mode2D,Mode3D;}
    /**
     * Enumerator describing the display mode of the Window.
     */
    public static enum WindowState {FULLSCREEN, FULLSCREEN_WINDOWED, WINDOWED, WINDOWED_UNDECORATED;}
    
    private final RenderMode MODE;
    private final Platform OS;
    private final String TITLE;
    private final double TPS;
    private final String VERSION;
     
    private final Config gameConfig = new Config("game");
    private final Logger gameLogger = new Logger("info");
    
    private boolean isRunning = false;
    
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
        
        String osBuf = System.getProperty("os.name").toLowerCase();
        if(osBuf.contains("win")) OS = Platform.WINDOWS;
        else if(osBuf.contains("mac")) OS = Platform.MACOS;
        else if(osBuf.contains("nix") || osBuf.contains("nux") || osBuf.contains("aix")) OS = Platform.LINUX;
        else if(osBuf.contains("sunos")) OS = Platform.SOLARIS;
        else
        {
            OS = null;
            Logger.getErrorLogger().log("Could not determine OS Type! You must define java.library.path in the runtime options using '-Djava.library.path='!");
            return;
        }
        
        
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
    public Platform getOS()
    {
        return OS;
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
        while(isRunning/*&& Display.isCloseRequested()*/)
        {
            //TODO: Create timings
            onTickPreprocessor();
            render();
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
        onTick();
    }
    
    /**
     * Override-able method that is called as much as possible to issue rendering commands.
     */
    protected void render(){}
    
    /**
     * Method that is used to load the game and all of it's resources.
     * @param args Arguments, usually from the main method (entry point).
     */
    public void start(String[] args)
    {
        isRunning = true;
        gameLogger.log("Launching " + TITLE + " Client v." + VERSION + "...");
        
        for(String a : args)
        {
            String[] b = a.split("=", 2);
            if(b.length <= 1) return;
            if(b[0].startsWith("#")) continue;
            
            gameConfig.setProperty(b[0], b[1]);
        }
        
        //TODO: initialize display and opengl
        
        loop();
    }
    
    /**
     * Method that flags the game to stop.
     */
    public void stop()
    {
        isRunning = false;
    }
    
    /**
     * Method to stop the game and close all of it's resources.
     */
    private void stopImpl()
    {
        gameConfig.save();
        gameLogger.close();
    }
}