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
package wrath.client.input;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import wrath.client.Game;
import wrath.util.Logger;

/**
 * Class to manage all input operations.
 * Used to organize code and clean up the {@link wrath.client.Game} class.
 */
public class InputManager
{
    private final ArrayList<KeyData> defaults = new ArrayList<>();

    private final HashMap<Integer, Runnable> persMap = new HashMap<>();
    private final HashMap<Integer, KeyData> keyMap = new HashMap<>();
    private static final HashMap<String, Runnable> savedFuncMap = new HashMap<>();
    
    private GLFWCharCallback charStr;
    private GLFWCursorPosCallback curStr;
    private GLFWKeyCallback keyStr;
    private GLFWMouseButtonCallback mkeyStr;
    
    private long cursor = -1;
    private double curx = 0;
    private double cury = 0;
    
    private final Game game;
    
    /**
     * Adds a listener to a specified String ID to be added later to a Keyboard or mouse function.
     * @param id The String ID of the saved function.
     * @param event The event to be saved.
     */
    public static void addSavedFunction(String id, Runnable event)
    {
        savedFuncMap.put(id, event);
    }

    /**
     * Gets the Runnable associated with the saved function ID.
     * @param functionID The function ID to reverse reference.
     * @return Returns the Runnable event associated with the String Function ID.
     */
    public static Runnable getSavedFunction(String functionID)
    {
        return savedFuncMap.get(functionID);
    }
    
    
    
    /**
     * Constructor.
     * @param game The {@link wrath.client.Game} the InputManager is attached to.
     */
    public InputManager(Game game)
    {
        this.game = game;
    }
    
    /**
     * Binds a function to the specified key/button.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param event The {@link wrath.client.KeyRunnable} event to run after specified button is affected by the specified action.
     */
    public void bindKey(int key, Runnable event)
    {
        bindKey(key, Key.MOD_NONE, Key.KeyAction.KEY_PRESS, event);
    }

    /**
     * Binds a function to the specified key/button.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
     * @param event The {@link wrath.client.KeyRunnable} event to run after specified button is affected by the specified action.
     */
    public void bindKey(int key, Key.KeyAction action, Runnable event)
    {
        bindKey(key, Key.MOD_NONE, action, event);
    }

    /**
     * Binds a function to the specified key/button.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param keyMod The {@link wrath.client.input.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
     * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
     * @param event The {@link wrath.client.KeyRunnable} event to run after specified button is affected by the specified action.
     */
    public void bindKey(int key, int keyMod, Key.KeyAction action, Runnable event)
    {
        if(action == Key.KeyAction.KEY_HOLD_DOWN)
            keyMap.put(key, new KeyData(Key.KeyAction.KEY_PRESS, () ->
                                {
                                    persMap.put(key, event);
                                    event.run();
            }, key, keyMod));
        else
            keyMap.put(key, new KeyData(action, event, key, keyMod));
    }

    /**
     * Binds a function to the specified key/button.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param functionId The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
     */
    public void bindKey(int key, String functionId)
    {
        bindKey(key, Key.MOD_NONE, Key.KeyAction.KEY_PRESS, functionId);
    }

    /**
     * Binds a function to the specified key/button.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
     * @param functionId The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
     */
    public void bindKey(int key, Key.KeyAction action, String functionId)
    {
        bindKey(key, Key.MOD_NONE, action, functionId);
    }

    /**
     * Binds a function to the specified key/button.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param keyMod The {@link wrath.client.input.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
     * @param action The {@link wrath.client.Game.KeyAction} that will trigger the event.
     * @param functionId The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
     */
    public void bindKey(int key, int keyMod, Key.KeyAction action, String functionId)
    {
        if(!savedFuncMap.containsKey(functionId))
            return;
        Runnable event = savedFuncMap.get(functionId);
        if(action == Key.KeyAction.KEY_HOLD_DOWN)
            keyMap.put(key, new KeyData(Key.KeyAction.KEY_PRESS, () ->
                                {
                                    persMap.put(key, event);
                                    event.run();
            }, key, keyMod));
        else
            keyMap.put(key, new KeyData(action, event, key, keyMod));
    }

    /**
     * Binds all pre-defined default keys.
     * Note that this DOES NOT clear all user-defined bindings, refer to {@link wrath.client.Game.InputManager#unbindAllKeys()}.
     */
    public void bindKeysToDefaults()
    {
        defaults.stream().forEach((d) ->
        {
            keyMap.put(d.getKey(), d);
        });
    }

    /**
     * Closes all input and input function.
     * This cannot be called while the window is open.
     * @param destroyCursor True if client is closing or want cursor to be destroyed, otherwise false.
     */
    public void closeInput(boolean destroyCursor)
    {
        keyStr.release();
        mkeyStr.release();
        charStr.release();
        curStr.release();
        if(destroyCursor && cursor != -1) GLFW.glfwDestroyCursor(cursor);
    }
    
    /**
     * Gets the X position of the cursor.
     * @return Returns the X position of the cursor.
     */
    public double getCursorX()
    {
        return (2 / (double) game.getWindowManager().getWidth() * curx) - 1.0;
    }

    /**
     * Gets the Y position of the cursor.
     * @return Returns the Y position of the cursor.
     */
    public double getCursorY()
    {
        return (2 / (double) game.getWindowManager().getHeight() * cury) - 1.0;
    }

    /**
     * Returns whether or not the cursor is enabled.
     * @return Returns true if the cursor is enabled, otherwise false.
     */
    public boolean isCursorEnabled()
    {
        return GLFW.glfwGetInputMode(game.getWindowManager().getWindowID(), GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_NORMAL;
    }

    /**
     * Checks to see if a key has an action bound to it.
     * @param key The {@link wrath.client.input.Key} to check binds.
     * @return Returns true if an action is already bound to the key.
     */
    public boolean isKeyBound(int key)
    {
        return keyMap.containsKey(key);
    }

    /**
     * Loads pre-saved keys.
     */
    public void loadKeys()
    {
        File inpFile = new File("etc/kays.dat");
        if(!inpFile.exists())
        {
            try
            {
                inpFile.createNewFile();
            }
            catch(IOException ex)
            {
                Logger.getErrorLogger().log("Could not create Key Bindings file!");
            }
            bindKeysToDefaults();
            saveKeys();
        }
        else
        {
            //TODO: Interpret keys.
        }
    }

    /**
     * Used by the {@link wrath.client.Game} class to run tasks assigned to persistent keys.
     */
    public void onPersistentInput()
    {
        //TODO: Secure method.
        persMap.entrySet().stream().map((pairs) -> (Runnable) pairs.getValue()).forEach((ev) ->
        {
            ev.run();
        });
    }

    /**
     * Used by the {@link wrath.client.Game} class to initialize all of the GLFW input callbacks.
     */
    public void openInput()
    {
        if(cursor != -1) GLFW.glfwSetCursor(game.getWindowManager().getWindowID(), cursor);
        
        GLFW.glfwSetCharCallback(game.getWindowManager().getWindowID(), (charStr = new GLFWCharCallback()
        {
            @Override
            public void invoke(long window, int codepoint)
            {
                if(game.getGameEventHandler() != null) game.getGameEventHandler().onCharInput((char) codepoint);
            }
        }));
        
        GLFW.glfwSetCursorPosCallback(game.getWindowManager().getWindowID(), (curStr = new GLFWCursorPosCallback()
        {
            @Override
            public void invoke(long window, double x, double y)
            {
                curx = x;
                cury = y;
                if(game.getGameEventHandler() != null) game.getGameEventHandler().onCursorMove(getCursorX(), getCursorY());
            }
        }));
        
        GLFW.glfwSetKeyCallback(game.getWindowManager().getWindowID(), (keyStr = new GLFWKeyCallback()
        {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods)
            {
                if(persMap.containsKey(key) && action == GLFW.GLFW_RELEASE) persMap.remove(key);
                else if(keyMap.containsKey(key))
                {
                    KeyData dat = keyMap.get(key);
                    if(dat.getRawAction() == action && (dat.getKeyMod() == -1 || mods == dat.getKeyMod())) dat.execute();
                }
            }
        }));
        
        GLFW.glfwSetMouseButtonCallback(game.getWindowManager().getWindowID(), (mkeyStr = new GLFWMouseButtonCallback()
        {
            @Override
            public void invoke(long window, int button, int action, int mods)
            {
                if(persMap.containsKey(button) && action == GLFW.GLFW_RELEASE) persMap.remove(button);
                else if(keyMap.containsKey(button))
                {
                    KeyData dat = keyMap.get(button);
                    if(dat.getRawAction() == action && (dat.getKeyMod() == -1 || mods == dat.getKeyMod())) dat.execute();
                }
            }
        }));
    }
    
    /**
     * Saves key bindings (containing saved functions) to 'etc/keys.dat'.
     */
    public void saveKeys()
    {
        File inpFile = new File("etc/kays.dat");
        if(!inpFile.exists())
        {
            try
            {
                inpFile.createNewFile();
            }
            catch(IOException ex)
            {
                Logger.getErrorLogger().log("Could not create Key Bindings file!");
            }
        }
        else
        {
            try
            {
                PrintWriter out = new PrintWriter(inpFile);
                Iterator it = keyMap.entrySet().iterator();
                while(it.hasNext())
                {
                    Map.Entry pair = (Map.Entry) it.next();
                    KeyData dat = (KeyData) pair.getValue();
                    if(savedFuncMap.containsValue(dat.getEvent()))
                    {
                        //TODO: Obtain function name and write it to file.
                    }
                }
            }
            catch(IOException e)
            {
                Logger.getErrorLogger().log("Could not save key bindings, I/O Error Occured!");
            }
        }
    }

    /**
     * Changes the cursor from a list of standard cursors located in
     * {@link wrath.client.input.Key}.
     * @param cursormode The {@link wrath.client.input.Key} Cursor to switch
     * to.
     */
    public void setCursor(int cursormode)
    {
        if(cursor != -1)
        {
            GLFW.glfwDestroyCursor(cursor);
            cursor = -1;
        }
        cursor = GLFW.glfwCreateStandardCursor(cursormode);
        GLFW.glfwSetCursor(game.getWindowManager().getWindowID(), cursor);
    }

    /**
     * Enables or disables the cursor.
     * @param cursorEnabled Whether the cursor should be enabled or
     * disabled.
     */
    public void setCursorEnabled(boolean cursorEnabled)
    {
        if(cursorEnabled)
            GLFW.glfwSetInputMode(game.getWindowManager().getWindowID(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        else
            GLFW.glfwSetInputMode(game.getWindowManager().getWindowID(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    /**
     * Sets a set of default keys to saved functions.
     * @param list The list of key data to set as default.
     */
    public void setDefaultBindings(Collection<KeyData> list)
    {
        defaults.clear();
        defaults.addAll(list);
    }

    /**
     * Clears all defined key bindings.
     * This DOES NOT set them to default, refer to {@link wrath.client.Game.InputManager#bindKeysToDefaults()}.
     */
    public void unbindAllKeys()
    {
        keyMap.clear();
        persMap.clear();
    }

    /**
     * Un-binds all functions bound to the specified key/button.
     * @param key The key to un-bind all functions on.
     */
    public void unbindKey(int key)
    {
        if(keyMap.containsKey(key))
        {
            keyMap.remove(key);
            if(persMap.containsKey(key))
                persMap.remove(key);
        }
    }
}
