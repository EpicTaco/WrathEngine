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
import wrath.client.Game;
import wrath.client.Game.RenderMode;
import wrath.client.handlers.GameEventHandler;
import wrath.client.input.Key;
/**
 * Example game for testing the engine.
 * Extends {@link wrath.client.Game} class.
 * @author Trent Spears
 */
public class CustomGame extends Game implements GameEventHandler
{
    private final TempWorld world;
    
    public CustomGame(String[] args)
    {
        super("Test Client", "INDEV", 30, RenderMode.Mode2D);
        
        File worldFile = new File("etc/world.dat");
        if(worldFile.exists()) world = TempWorld.load(worldFile);
        else world = new TempWorld(64, worldFile);
        
        setGameEventHandler(this);
        setWindowState(Game.WindowState.WINDOWED);
        start(args);
    }
    
    @Override
    public void render()
    {
        world.drawWorld();
    }
    
    public static void main(String[] args)
    {
        new CustomGame(args);
    }
    
    @Override
    public void onCharInput(char c){}
    
    @Override
    public void onCursorMove(double x, double y){}
    
    @Override
    public void onGameOpen()
    {
        addKeyboardFunction(Key.KEY_ESCAPE, () ->
        {
            stop();
        });
        
        addKeyboardFunction(Key.KEY_F3, () ->
        {
            System.out.println(getFPS());
        });
        
        addMouseFunction(Key.MOUSE_BUTTON_1, () ->
        {
            int[] tile = world.getBounds(getCursorX(), getCursorY());
            if(tile.length >= 2) world.setTile(tile[0], tile[1], TempWorld.GRASS);
        });
        
        addMouseFunction(Key.MOUSE_BUTTON_2, () ->
        {
            int[] tile = world.getBounds(getCursorX(), getCursorY());
            if(tile.length >= 2) world.setTile(tile[0], tile[1], TempWorld.STONE);
        });
        
        addMouseFunction(Key.MOUSE_BUTTON_3, () ->
        {
            int[] tile = world.getBounds(getCursorX(), getCursorY());
            if(tile.length >= 2) world.setTile(tile[0], tile[1], TempWorld.AIR);
        });
    }
    
    @Override
    public void onGameClose()
    {
        world.save();
    }
    
    @Override
    public void onTick(){}
    
    @Override
    public void onWindowResize(int w, int h){}
    
    @Override
    public void onWindowOpen()
    {
        setCursorEnabled(true);
    }
}
