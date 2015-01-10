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

import wrath.client.Game;
import wrath.client.Key;

/**
 * Example game for testing the engine.
 * Extends {@link wrath.client.Game} class.
 * @author Trent Spears
 */
public class CustomGame extends Game
{
    public CustomGame(String[] args)
    {
        super("Test Client", "INDEV", 30, RenderMode.Mode2D);
        start(new String[]{"WindowState=windowed", "ResolutionWidth=1024", "ResolutionHeight=720", "ResolutionIsWindowSize=true"});
    }
    
    @Override
    public void onGameOpen()
    {
        addKeyboardFunction(Key.KEY_W, KeyAction.KEY_HOLD_DOWN, () ->
        {
            System.out.println("csp");
        });
        
        addKeyboardFunction(Key.KEY_END, KeyAction.KEY_PRESS, () ->
        {
            stop();
        });
        
        addKeyboardFunction(Key.KEY_LEFT_ALT, KeyAction.KEY_PRESS, () ->
        {
            setCursorEnabled(!isCursorEnabled());
        });
        
        this.setCursorEnabled(true);
    }
    
    public static void main(String[] args)
    {
        new CustomGame(args);
    }
}
