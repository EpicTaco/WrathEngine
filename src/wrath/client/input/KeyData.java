/**
 *  Wrath Engine
 *  Copyright (C) 2015 Trent Spears
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
package wrath.client.input;

import org.lwjgl.glfw.GLFW;
import wrath.client.input.Key.KeyAction;

/**
 * Keeps track of keys.
 * @author Trent Spears
 */
public class KeyData
{   
    private final KeyAction action;
    private final int actionRaw;
    private final Runnable function;
    private final int key;
    private final int mod;
    
    /**
     * Constructor.
     * @param action The {@link wrath.client.input.Key.KeyAction} that will trigger the function.
     * @param function The {@link java.lang.Runnable} function that will be triggered by the Key functions.
     * @param glfwKey The GLFW key that will trigger the function. This can be found at {@link wrath.client.input.Key}.
     * @param glfwMod The Mod keys that will trigger the function. This can be found at {@link wrath.client.input.Key}.
     */
    public KeyData(KeyAction action, Runnable function, int glfwKey, int glfwMod)
    {
        this.action = action;
            
        if(action == KeyAction.KEY_RELEASE) this.actionRaw = GLFW.GLFW_RELEASE;
            else this.actionRaw = GLFW.GLFW_PRESS;
        
        this.function = function;
        this.key = glfwKey;
        this.mod = glfwMod;
    }
    
    /**
     * Constructor.
     * @param action The {@link wrath.client.input.Key.KeyAction} that will trigger the function.
     * @param functionID The saved function ID that will be triggered by the Key functions.
     * @param glfwKey The GLFW key that will trigger the function. This can be found at {@link wrath.client.input.Key}.
     * @param glfwMod The Mod keys that will trigger the function. This can be found at {@link wrath.client.input.Key}.
     */
    public KeyData(KeyAction action, String functionID, int glfwKey, int glfwMod)
    {
        this.action = action;
            
        if(action == KeyAction.KEY_RELEASE) this.actionRaw = GLFW.GLFW_RELEASE;
            else this.actionRaw = GLFW.GLFW_PRESS;
        
        this.function = InputManager.getSavedFunction(functionID);
        this.key = glfwKey;
        this.mod = glfwMod;
    }
    
    /**
     * Executes the function assigned to the key.
     */
    public void execute()
    {
        function.run();
    }
    
    /**
    * Gets the {@link wrath.client.Game.KeyAction} of the key set.
    * This is used to determine whether to execute the function when a key is pressed, or execute the function when the key is released.
    * @return Returns the {@link wrath.client.Game.KeyAction} specified in the Constructor.
    */
    public KeyAction getAction()
    {
        return action;
    }
       
    /**
     * Gets the function associated with this Key Data set.
     * @return Returns the function associated with this Key Data set.
     */
    public Runnable getEvent()
    {
        return function;
    }
    
    /**
     * Gets the {@link wrath.client.input.Key}.
     * @return Returns the key id.
     */
    public int getKey() 
    {
        return key;
    }

    /**
     * Returns the {@link wrath.client.input.Key} modification.
     * @return Gets the KeyMod assigned to the data.
     */
    public int getKeyMod()
    {
        return mod;
    }
    
    /**
     * Used by the internal engine to obtain the GLFW action code.
     * @return Returns the int version of the KeyAction for use with GLFW.
     */
    public int getRawAction() 
    {
        return actionRaw;
    }   
}