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

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
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
        addKeyboardFunction(Key.KEY_END, KeyAction.KEY_PRESS, () ->
        {
            stop();
        });
        
        addKeyboardFunction(Key.KEY_LEFT_ALT, KeyAction.KEY_PRESS, () ->
        {
            setCursorEnabled(!isCursorEnabled());
        });
        
        addKeyboardFunction(Key.KEY_F12, KeyAction.KEY_PRESS, () ->
        {
            screenShot("screenie" + System.nanoTime()/50);
        });
        
        addKeyboardFunction(Key.KEY_S, KeyAction.KEY_PRESS, () ->
        {
            getLogger().log("Recorded FPS: " + getFPS());
        });
        
        setCursorEnabled(false);
    }
    
    @Override
    public void render()
    {
        GL11.glRotatef((float) GLFW.glfwGetTime() * 50.f, 0.f, 0.f, 1.f);
        
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glColor3f(1.f, 0.f, 0.f);
        GL11.glVertex3f(-0.6f, -0.4f, 0.f);
        GL11.glColor3f(0.f, 1.f, 0.f);
        GL11.glVertex3f(0.6f, -0.4f, 0.f);
        GL11.glColor3f(0.f, 0.f, 1.f);
        GL11.glVertex3f(0.f, 0.6f, 0.f);
        GL11.glEnd();
    }
    
    public static void main(String[] args)
    {
        new CustomGame(args);
    }
}
