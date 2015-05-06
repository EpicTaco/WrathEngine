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

import java.io.File;
import wrath.client.EntryObject;
import wrath.client.Game;
import wrath.client.RenderMode;
import wrath.client.events.GameEventHandler;
import wrath.client.input.InputManager;
import wrath.client.input.Key;
import wrath.client.input.KeyAction;
import wrath.common.scripts.PythonScriptManager;

/**
 * Example game for testing the engine.
 * Extends {@link wrath.client.Game} class.
 * @author Trent Spears
 */
public class CustomGame extends Game implements GameEventHandler, EntryObject
{
    private final TempWorld world;
    private final PythonScriptManager scripts;
    
    /**
     * Do not use start() in the constructor! EVER!
     */
    public CustomGame()
    {
        super("Test Client", "INDEV", 30, RenderMode.Mode2D);
        
        File worldFile = new File("etc/world.dat");
        if(worldFile.exists()) world = TempWorld.load(worldFile);
        else world = new TempWorld(64, worldFile);
        
        scripts = new PythonScriptManager(this);
        
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
        world.drawWorld();
    }
    
    @Override
    public void onGameOpen()
    { 
        //Pre-defined key functions
        InputManager.addSavedFunction("setgrass", () ->
        {
            int[] tile = world.getBounds(getInputManager().getCursorX(), getInputManager().getCursorY());
            if(tile.length >= 2) world.setTile(tile[0], tile[1], TempWorld.GRASS);
        });
        
        InputManager.addSavedFunction("setstone", () ->
        {
            int[] tile = world.getBounds(getInputManager().getCursorX(), getInputManager().getCursorY());
            if(tile.length >= 2) world.setTile(tile[0], tile[1], TempWorld.STONE);
        });
        
        InputManager.addSavedFunction("setair", () ->
        {
            int[] tile = world.getBounds(getInputManager().getCursorX(), getInputManager().getCursorY());
            if(tile.length >= 2) world.setTile(tile[0], tile[1], TempWorld.AIR);
        });

        getInputManager().bindDefaultEngineKeys();
        getInputManager().addDefaultKeyBinding(Key.MOUSE_BUTTON_1, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "setgrass");
        getInputManager().addDefaultKeyBinding(Key.MOUSE_BUTTON_2, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "setstone");
        getInputManager().addDefaultKeyBinding(Key.MOUSE_BUTTON_3, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "setair");
    }
    
    @Override
    public void onGameClose()
    {
        world.save();
    }
    
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
