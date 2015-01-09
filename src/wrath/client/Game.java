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
    public static enum Platform {LINUX, MACOS, SOLARIS, WINDOWS;}
    public static enum RenderMode {Mode2D, Mode3D;}
    public static enum WindowState {FULLSCREEN, FULLSCREEN_WINDOWED, WINDOWED, WINDOWED_UNDECORATED;}
    
    private final RenderMode MODE;
    private final Platform OS;
    private final String TITLE;
    private final double TPS;
    private final String VERSION;
     
    private final Config gameConfig = new Config("game");
    private final Logger gameLogger = new Logger("info");
    
    private boolean isRunning = false;
    
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
    
    public Config getConfig()
    {
        return gameConfig;
    }
    
    public Logger getErrorLogger()
    {
        return Logger.getErrorLogger();
    }
    
    public Logger getLogger()
    {
        return gameLogger;
    }
    
    public Platform getOS()
    {
        return OS;
    }
    
    public RenderMode getRenderMode()
    {
        return MODE;
    }
    
    public String getTitle()
    {
        return TITLE;
    }
    
    public double getTPS()
    {
        return TPS;
    }
    
    public String getVersion()
    {
        return VERSION;
    }
    
    public boolean isRunnning()
    {
        return isRunning;
    }
    
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
    
    protected void onGameClose(){}
    
    protected void onGameOpen(){}
    
    protected void onTick(){}
    
    private void onTickPreprocessor()
    {
        onTick();
    }
    
    protected void render(){}
    
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
    
    public void stop()
    {
        isRunning = false;
    }
    
    private void stopImpl()
    {
        gameConfig.save();
        gameLogger.close();
    }
}