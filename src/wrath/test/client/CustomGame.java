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
package wrath.test.client;

import wrath.client.EntryObject;
import wrath.client.ExternalPluginManager;
import wrath.client.Game;
import wrath.client.enums.RenderMode;
import wrath.client.events.GameEventHandler;
import wrath.common.scripts.PythonScriptManager;

/**
 * Example game for testing the engine.
 * Extends {@link wrath.client.Game} class.
 * @author Trent Spears
 */
public class CustomGame extends Game implements GameEventHandler, EntryObject
{
    private final PythonScriptManager scripts;
    
    /**
     * Do not use start() in the constructor! EVER!
     */
    public CustomGame()
    {
        super("Test Client", "INDEV", 30, RenderMode.Mode2D);
        
        scripts = new PythonScriptManager(this);
        ExternalPluginManager.setPythonScriptManager(scripts);
        
        getEventManager().addGameEventHandler(this);
    }
    
    /**
     * This is the entry point of the game, implemented from {@link wrath.client.EntryObject}.
     * @param args The arguments passed from the command-line.
     */
    @Override
    public void init(String[] args)
    {
        start(args);
    }
    
    @Override
    public void render()
    {
        getWindowManager().getFontRenderer().renderString("this is a long string", 0, 0);
    }
    
    @Override
    public void onGameOpen()
    {
        getInputManager().bindDefaultEngineKeys();
    }
    
    @Override
    public void onGameClose(){}
    
    @Override
    public void onLoadJavaPlugin(Object obj){}
    
    @Override
    public void onTick(){}
    
    @Override
    public void onResolutionChange(int ow, int oh, int w, int h){}
    
    @Override
    public void onWindowOpen()
    {
        getInputManager().setCursorEnabled(true);
    }
}
