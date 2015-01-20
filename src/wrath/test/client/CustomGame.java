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
import wrath.client.handlers.GameEventHandler;
import wrath.client.Game;
import wrath.client.input.Key;
import wrath.client.input.Key.KeyAction;

/**
 * Example game for testing the engine.
 * Extends {@link wrath.client.Game} class.
 * @author Trent Spears
 */
public class CustomGame extends Game implements GameEventHandler
{   
    int x=0,y=0,stage=1,texture=-1;
    
    public CustomGame(String[] args)
    {
        super("Test Client", "INDEV", 30, RenderMode.Mode2D);
        setGameEventHandler(this);
        start(args);
    }
    
    @Override
    public void onCharInput(char c){}
    
    @Override
    public void onCursorMove(double x, double y){}
    
    @Override
    public void onGameOpen()
    {
        setupInputFunctions();
        
        addKeyboardFunction(Key.KEY_ENTER, Key.MOD_ALT, KeyAction.KEY_PRESS, "toggle_fullscreen");
        addKeyboardFunction(Key.KEY_END, Key.MOD_NONE, KeyAction.KEY_PRESS, "stop");
        addKeyboardFunction(Key.KEY_F12, Key.MOD_NONE, KeyAction.KEY_PRESS, "screenshot");
        addKeyboardFunction(Key.KEY_F10, Key.MOD_ALT, KeyAction.KEY_PRESS, "cursor_toggle");
        addKeyboardFunction(Key.KEY_SPACE, Key.MOD_SHIFT, KeyAction.KEY_PRESS, () -> {System.out.println(getFPS());});
        addMouseFunction(Key.MOUSE_BUTTON_1, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, () -> {System.out.println("test");});
        
        setCursor(Key.CURSOR_CROSSHAIR);
        setCursorEnabled(false);
    }
    
    @Override
    public void onGameClose(){}
    
    @Override
    public void onTick(){}
    
    @Override
    public void onWindowResize(int w, int h){}
    
    @Override
    public void onWindowOpen()
    {
        setBackgroundImage(new File("assets/textures/background.jpg"));
    }
    
    @Override
    public void render()
    {
    }
    
    private void setupInputFunctions()
    {
        addSavedFunction("stop", this::stop);

        addSavedFunction("cursor_toggle", () -> 
        {
            setCursorEnabled(!isCursorEnabled());
        });
        
        addSavedFunction("screenshot", () -> 
        {
            screenShot("screenie" + System.nanoTime()/1000000000);
        });
        
        addSavedFunction("get_fps", () -> 
        {
            getLogger().log("Recorded FPS: " + getFPS());
        });
        
        addSavedFunction("toggle_fullscreen", () -> 
        {
            if(getWindowState() == WindowState.WINDOWED) setWindowState(WindowState.FULLSCREEN_WINDOWED);
            else setWindowState(WindowState.WINDOWED);
        });
    }
    
    public static void main(String[] args)
    {
        new CustomGame(args);
    }
}
