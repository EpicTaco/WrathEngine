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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import wrath.client.Game;
import wrath.client.enums.ImageFormat;
import wrath.client.enums.RenderMode;
import wrath.client.enums.WindowState;
import wrath.common.Closeable;
import wrath.util.Logger;

/**
 * Class to manage all input operations.
 * Used to organize code and clean up the {@link wrath.client.Game} class.
 */
public class InputManager implements Closeable
{
    private final ArrayList<KeyData> defaults = new ArrayList<>();

    private final HashMap<Integer, Runnable> persMap = new HashMap<>();
    private final HashMap<Integer, KeyList> keyMap = new HashMap<>();
    private static final HashMap<String, Runnable> savedFuncMap = new HashMap<>();
    
    private GLFWCharCallback charStr;
    private GLFWCursorPosCallback curStr;
    private GLFWKeyCallback keyStr;
    private GLFWMouseButtonCallback mkeyStr;
    private GLFWScrollCallback scrStr;
    
    private long cursor = -1;
    private double curx = 0;
    private double cury = 0;
    
    /**
     * Adds a listener to a specified String ID to be added later to a Keyboard or mouse function.
     * Name is NOT case sensitive!
     * @param id The String ID of the saved function.
     * @param function The event to be saved.
     */
    public static void addSavedFunction(String id, Runnable function)
    {
        savedFuncMap.put(id.trim().toLowerCase(), function);
    }

    /**
     * Gets the Runnable associated with the saved function ID.
     * @param functionID The function ID to reverse reference.
     * @return Returns the Runnable event associated with the String Function ID.
     */
    public static Runnable getSavedFunction(String functionID)
    {
        return savedFuncMap.get(functionID.trim().toLowerCase());
    }
    
    
    
    /**
     * Constructor.
     */
    public InputManager()
    {   
        afterConstructor();
    }
    
    private void afterConstructor()
    {
        Game.getCurrentInstance().addToTrashCleanup(this);
        savedFuncMap.clear();
        addSavedFunction("stop", () ->
        {
            Game.getCurrentInstance().stop();
        });
        
        addSavedFunction("toggle_fps", () ->
        {
            Game.getCurrentInstance().getRenderer().setRenderFPS(!Game.getCurrentInstance().getRenderer().isRenderingFPS());
        });
        
        addSavedFunction("screenshot", () ->
        {
            DateFormat format = new SimpleDateFormat("MM_dd_yyyy___HHmmss");
            Calendar now = Calendar.getInstance();
            Game.getCurrentInstance().getWindowManager().screenShot("screenshot_" + format.format(now.getTime()), ImageFormat.PNG);
        });
        
        addSavedFunction("toggle_windowstate_fullscreen", () ->
        {
            if(Game.getCurrentInstance().getWindowManager().getWindowState() == WindowState.FULLSCREEN) Game.getCurrentInstance().getWindowManager().setWindowState(WindowState.WINDOWED);
            else Game.getCurrentInstance().getWindowManager().setWindowState(WindowState.FULLSCREEN);
        });
        
        addSavedFunction("center_window", () ->
        {
            Game.getCurrentInstance().getWindowManager().centerWindow();
        });
        
        addSavedFunction("toggle_windowstate_fullwindowed", () ->
        {
            if(Game.getCurrentInstance().getWindowManager().getWindowState() == WindowState.FULLSCREEN_WINDOWED) Game.getCurrentInstance().getWindowManager().setWindowState(WindowState.WINDOWED);
            else Game.getCurrentInstance().getWindowManager().setWindowState(WindowState.FULLSCREEN_WINDOWED);
        });
        
        addSavedFunction("minimize_window", () ->
        {
            Game.getCurrentInstance().getWindowManager().minimizeWindow();
        });
        
        addSavedFunction("bind_keys_to_defaults", () ->
        {
            bindKeysToDefaults();
        });
        
        addSavedFunction("reset_keys", () ->
        {
            unbindAllKeys();
            bindKeysToEngineDefault();
            bindKeysToDefaults();
        });
        
        addSavedFunction("save_internals", () ->
        {
            saveKeys();
            Game.getCurrentInstance().getConfig().save();
        });
        
        addSavedFunction("toggle_cursor", () ->
        {
            setCursorEnabled(!isCursorEnabled());
        });
        
        addSavedFunction("move_forward", () ->
        {
            Game.getCurrentInstance().getPlayerCamera().translatePosition(0, 0, -0.02f);
        });
        
        addSavedFunction("move_left", () ->
        {
            Game.getCurrentInstance().getPlayerCamera().translatePosition(-0.02f, 0, 0);
        });
        
        addSavedFunction("move_backward", () ->
        {
            Game.getCurrentInstance().getPlayerCamera().translatePosition(0, 0, 0.02f);
        });
        
        addSavedFunction("move_right", () ->
        {
            Game.getCurrentInstance().getPlayerCamera().translatePosition(0.02f, 0, 0);
        });
        
        addSavedFunction("move_up", () ->
        {
            Game.getCurrentInstance().getPlayerCamera().translatePosition(0, 0.02f, 0);
        });
        
        addSavedFunction("move_down", () ->
        {
            Game.getCurrentInstance().getPlayerCamera().translatePosition(0, -0.02f, 0);
        });
    }
    
    /**
     * Sets default key binding.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param function The {@link java.lang.Runnable} event to run after specified button is affected by the specified action.
     */
    public void addDefaultKeyBinding(int key, Runnable function)
    {
        defaults.add(new KeyData(key, Key.MOD_NONE, KeyAction.KEY_PRESS, function));
    }
    
    /**
     * Sets default key binding.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param action The {@link wrath.client.input.KeyAction} that will trigger the event.
     * @param function The {@link java.lang.Runnable} event to run after specified button is affected by the specified action.
     */
    public void addDefaultKeyBinding(int key, KeyAction action, Runnable function)
    {
        defaults.add(new KeyData(key, Key.MOD_NONE, action, function));
    }
    
    /**
     * Sets default key binding.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param keyMod The {@link wrath.client.input.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
     * @param action The {@link wrath.client.input.KeyAction} that will trigger the event.
     * @param function The {@link java.lang.Runnable} event to run after specified button is affected by the specified action.
     */
    public void addDefaultKeyBinding(int key, int keyMod, KeyAction action, Runnable function)
    {
        defaults.add(new KeyData(key, keyMod, action, function));
    }
    
    /**
     * Sets default key binding.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param functionID The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
     */
    public void addDefaultKeyBinding(int key, String functionID)
    {
        defaults.add(new KeyData(key, Key.MOD_NONE, KeyAction.KEY_PRESS, functionID));
    }
    
    /**
     * Sets default key binding.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param action The {@link wrath.client.input.KeyAction} that will trigger the event.
     * @param functionID The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
     */
    public void addDefaultKeyBinding(int key, KeyAction action, String functionID)
    {
        defaults.add(new KeyData(key, Key.MOD_NONE, action, functionID));
    }
    
    /**
     * Sets default key binding.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param keyMod The {@link wrath.client.input.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
     * @param action The {@link wrath.client.input.KeyAction} that will trigger the event.
     * @param functionID The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
     */
    public void addDefaultKeyBinding(int key, int keyMod, KeyAction action, String functionID)
    {
        defaults.add(new KeyData(key, keyMod, action, functionID));
    }
    
    /**
     * Binds a function to the specified key/button.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param event The {@link java.lang.Runnable} event to run after specified button is affected by the specified action.
     */
    public void bindKey(int key, Runnable event)
    {
        bindKey(key, Key.MOD_NONE, KeyAction.KEY_PRESS, event);
    }

    /**
     * Binds a function to the specified key/button.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param action The {@link wrath.client.input.KeyAction} that will trigger the event.
     * @param event The {@link java.lang.Runnable} event to run after specified button is affected by the specified action.
     */
    public void bindKey(int key, KeyAction action, Runnable event)
    {
        bindKey(key, Key.MOD_NONE, action, event);
    }

    /**
     * Binds a function to the specified key/button.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param keyMod The {@link wrath.client.input.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
     * @param action The {@link wrath.client.input.KeyAction} that will trigger the event.
     * @param function The {@link java.lang.Runnable} event to run after specified button is affected by the specified action.
     */
    public void bindKey(int key, int keyMod, KeyAction action, Runnable function)
    {
        if(keyMap.containsKey(key))
        {
            KeyList list = keyMap.get(key);
            list.addBinding(new KeyData(key, keyMod, action, function));
        }
        else keyMap.put(key, new KeyList(new KeyData(key, keyMod, action, function)));
    }

    /**
     * Binds a function to the specified key/button.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param functionID The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
     */
    public void bindKey(int key, String functionID)
    {
        bindKey(key, Key.MOD_NONE, KeyAction.KEY_PRESS, functionID);
    }

    /**
     * Binds a function to the specified key/button.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param action The {@link wrath.client.input.KeyAction} that will trigger the event.
     * @param functionID The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
     */
    public void bindKey(int key, KeyAction action, String functionID)
    {
        bindKey(key, Key.MOD_NONE, action, functionID);
    }

    /**
     * Binds a function to the specified key/button.
     * @param key The {@link wrath.client.input.Key} to respond to.
     * @param keyMod The {@link wrath.client.input.Key} MOD_x to respond to; e.g. MOD_ALT to activate when ALT is also held down, -1 for none.
     * @param action The {@link wrath.client.input.KeyAction} that will trigger the event.
     * @param functionID The pre-assigned Function ID, as assigned by {@link #addSavedFunction(java.lang.String, java.lang.Runnable) }.
     */
    public void bindKey(int key, int keyMod, KeyAction action, String functionID)
    {
        if(!savedFuncMap.containsKey(functionID)) return;
        if(keyMap.containsKey(key))
        {
            KeyList list = keyMap.get(key);
            list.addBinding(new KeyData(key, keyMod, action, functionID));
        }
        else keyMap.put(key, new KeyList(new KeyData(key, keyMod, action, functionID)));
        Game.getCurrentInstance().getLogger().println("Function '" + functionID + "' bound to key '" + key + "'.");
    }

    /**
     * Binds all pre-defined default keys.
     * Note that this DOES NOT clear all user-defined bindings, refer to {@link wrath.client.input.InputManager#unbindAllKeys()}.
     */
    public void bindKeysToDefaults()
    {
        Game.getCurrentInstance().getLogger().println("Setting keys to defaults!");
        defaults.stream().forEach((d) ->
        {
            if(keyMap.containsKey(d.getKey()))
            {
                KeyList list = keyMap.get(d.getKey());
                list.addBinding(d);
            }
            else keyMap.put(d.getKey(), new KeyList(d));
        });
    }
    
    /**
     * Binds all default engine functions to keys.
     * For further reference, refer to DefaultKeyBindings.txt in the default package.
     */
    public void bindKeysToEngineDefault()
    {
        bindKey(Key.KEY_ESCAPE, Key.MOD_SHIFT, KeyAction.KEY_PRESS, "stop");
        bindKey(Key.KEY_F3, Key.MOD_NONE, KeyAction.KEY_PRESS, "toggle_fps");
        bindKey(Key.KEY_F12, Key.MOD_NONE, KeyAction.KEY_PRESS, "screenshot");
        bindKey(Key.KEY_ENTER, Key.MOD_ALT, KeyAction.KEY_PRESS, "toggle_windowstate_fullscreen");
        bindKey(Key.KEY_ENTER, Key.MOD_SHIFT, KeyAction.KEY_PRESS, "center_window");
        bindKey(Key.KEY_UP, Key.MOD_ALT, KeyAction.KEY_PRESS, "toggle_windowstate_fullwindowed");
        bindKey(Key.KEY_DOWN, Key.MOD_ALT, KeyAction.KEY_PRESS, "minimize_window");
        bindKey(Key.KEY_HOME, Key.MOD_CTRL + Key.MOD_SHIFT, KeyAction.KEY_PRESS, "bind_keys_to_defaults");
        bindKey(Key.KEY_HOME, Key.MOD_CTRL + Key.MOD_ALT + Key.MOD_SHIFT, KeyAction.KEY_PRESS, "reset_keys");
        bindKey(Key.KEY_S, Key.MOD_CTRL + Key.MOD_ALT, KeyAction.KEY_PRESS, "save_internals");
        bindKey(Key.KEY_C, Key.MOD_ALT, KeyAction.KEY_PRESS, "toggle_cursor");
        
        if(Game.getCurrentInstance().getRenderMode() == RenderMode.Mode3D)
        {
            bindKey(Key.KEY_W, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "move_forward");
            bindKey(Key.KEY_S, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "move_backward");
        }
        else
        {
            bindKey(Key.KEY_W, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "move_up");
            bindKey(Key.KEY_S, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "move_down");
        }
        bindKey(Key.KEY_A, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "move_left");
        bindKey(Key.KEY_D, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "move_right");
    }
    
    @Override
    public void close()
    {
        keyStr.release();
        mkeyStr.release();
        charStr.release();
        curStr.release();
        scrStr.release(); 
    }
    
    /**
     * Destroys the custom cursor and all of it's data, if it exists.
     */
    public void destroyCursor()
    {
        if(cursor != -1) GLFW.glfwDestroyCursor(cursor);
    }
    
    /**
     * Gets the X position of the cursor.
     * @return Returns the X position of the cursor.
     */
    public double getCursorX()
    {
        return (2 / (double) Game.getCurrentInstance().getWindowManager().getWidth() * curx) - 1.0;
    }

    /**
     * Gets the Y position of the cursor.
     * @return Returns the Y position of the cursor.
     */
    public double getCursorY()
    {
        return ((2 / (double) Game.getCurrentInstance().getWindowManager().getHeight() * cury) - 1.0) * -1;
    }

    /**
     * Returns whether or not the cursor is enabled.
     * @return Returns true if the cursor is enabled, otherwise false.
     */
    public boolean isCursorEnabled()
    {
        return GLFW.glfwGetInputMode(Game.getCurrentInstance().getWindowManager().getWindowID(), GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_NORMAL;
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
        File inpFile = new File(Game.getCurrentInstance().getConfig().getString("KeyBindsFile", "assets/keys.dat"));
        if(!inpFile.exists())
        {
            try
            {
                inpFile.createNewFile();
                Game.getCurrentInstance().getLogger().println("Saved key bindings not found, Generating file @ '" + inpFile.getName() + "'!");
            }
            catch(IOException ex)
            {
                System.err.println("Could not create Key Bindings file!");
            }
            bindKeysToDefaults();
            saveKeys();
        }
        else
        {
            try
            {
                //Format: 'functionID:mod:action:key'
                if(inpFile.length() < 1)
                {
                    bindKeysToDefaults();
                    return;
                }
                
                Game.getCurrentInstance().getLogger().println("Reading key bindings from file '" + inpFile.getName() + "'!");
                try(BufferedReader in = new BufferedReader(new FileReader(inpFile))) 
                {
                    String inputBuffer;
                    int lineNum = 0;
                    while((inputBuffer = in.readLine()) != null)
                    {
                        lineNum++;
                        String functionID;
                        int keyMod;
                        KeyAction action;
                        int key;
                        
                        String[] inputArray = inputBuffer.split(":");
                        if(inputArray.length < 4) continue;
                        
                        functionID = inputArray[0].trim().toLowerCase();
                        if(!savedFuncMap.containsKey(functionID))
                        {
                            Game.getCurrentInstance().getLogger().println("Unknown function '" + functionID + "' while loading key binds from file '" + inpFile.getAbsolutePath() + "'; Line " + lineNum);
                            continue;
                        }
                        
                        try
                        {
                            keyMod = Integer.parseInt(inputArray[1]);
                        }
                        catch(NumberFormatException e)
                        {
                            Game.getCurrentInstance().getLogger().println("Unknown key mod value '" + inputArray[1] + "' while loading key binds from file '" + inpFile.getAbsolutePath() + "'; Line " + lineNum);
                            continue;
                        }
                        
                        action = KeyAction.valueOf(inputArray[2]);
                        if(action == null)
                        {
                            Game.getCurrentInstance().getLogger().println("Unknown key action value '" + inputArray[2] + "' while loading key binds from file '" + inpFile.getAbsolutePath() + "'; Line " + lineNum);
                            continue;
                        }
                        
                        try
                        {
                            key = Integer.parseInt(inputArray[3]);
                        }
                        catch(NumberFormatException e)
                        {
                            Game.getCurrentInstance().getLogger().println("Unknown key value '" + inputArray[3] + "' while loading key binds from file '" + inpFile.getAbsolutePath() + "'; Line " + lineNum);
                            continue;
                        }
                        
                        bindKey(key, keyMod, action, functionID);
                    }
                }
            }
            catch(IOException e)
            {
                System.err.println("Could not load saved keys from file '" + inpFile.getAbsolutePath() + "'! Binding default keys!");
                bindKeysToDefaults();
            }
        }
    }

    /**
     * Used by the {@link wrath.client.Game} class to run tasks assigned to persistent keys.
     * Not necessary to call.
     */
    public void onPersistentInput()
    {
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
        if(cursor != -1) GLFW.glfwSetCursor(Game.getCurrentInstance().getWindowManager().getWindowID(), cursor);
        
        GLFW.glfwSetCharCallback(Game.getCurrentInstance().getWindowManager().getWindowID(), (charStr = new GLFWCharCallback()
        {
            @Override
            public void invoke(long window, int codepoint)
            {
                Game.getCurrentInstance().getEventManager().getInputEventHandler().onCharInput((char) codepoint);
            }
        }));
        
        GLFW.glfwSetCursorPosCallback(Game.getCurrentInstance().getWindowManager().getWindowID(), (curStr = new GLFWCursorPosCallback()
        {
            @Override
            public void invoke(long window, double x, double y)
            {
                curx = x;
                cury = y;
                Game.getCurrentInstance().getEventManager().getInputEventHandler().onCursorMove(getCursorX(), getCursorY());
            }
        }));
        
        GLFW.glfwSetKeyCallback(Game.getCurrentInstance().getWindowManager().getWindowID(), (keyStr = new GLFWKeyCallback()
        {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods)
            {
                if(persMap.containsKey(key) && action == GLFW.GLFW_RELEASE) persMap.remove(key);
                if(keyMap.containsKey(key)) keyMap.get(key).trigger(action, mods);
            }
        }));
        
        GLFW.glfwSetMouseButtonCallback(Game.getCurrentInstance().getWindowManager().getWindowID(), (mkeyStr = new GLFWMouseButtonCallback()
        {
            @Override
            public void invoke(long window, int button, int action, int mods)
            {
                if(persMap.containsKey(button) && action == GLFW.GLFW_RELEASE) persMap.remove(button);
                if(keyMap.containsKey(button)) keyMap.get(button).trigger(action, mods);
            }
        }));
        
        GLFW.glfwSetScrollCallback(Game.getCurrentInstance().getWindowManager().getWindowID(), (scrStr = new GLFWScrollCallback()
        {
            @Override
            public void invoke(long window, double xoff, double yoff)
            {
                Game.getCurrentInstance().getEventManager().getInputEventHandler().onScroll(xoff, yoff);
            }
        }));
    }
    
    /**
     * Saves key bindings (containing saved functions) to pre-configured file "KeyBindsFile".
     */
    public void saveKeys()
    {
        //Format: 'functionID:mod:action:key'
        File inpFile = new File(Game.getCurrentInstance().getConfig().getString("KeyBindsFile", "assets/keys.dat"));
        if(!inpFile.exists())
        {
            try
            {
                inpFile.createNewFile();
            }
            catch(IOException ex)
            {
                System.err.println("Could not create Key Bindings file! I/O Error Occured!");
            }
        }
        
        try 
        {
            try(PrintWriter out = new PrintWriter(inpFile)) 
            {
                Iterator it = keyMap.entrySet().iterator();
                while(it.hasNext())
                {
                    KeyData[] list = ((KeyList) ((Map.Entry) it.next()).getValue()).getAllElements();
                    
                    for(KeyData dat : list)
                        if(dat.getFunctionID() != KeyData.CUSTOM_EVENT)
                            out.println(dat.getFunctionID().trim().toLowerCase() + ":" + dat.getKeyMod() + ":" + dat.getAction().toString() + ":" + dat.getKey());
                }
            }
        }
        catch (IOException e) 
        {
            System.err.println("Could not save key bindings, I/O Error Occured!");
        }
        
        Game.getCurrentInstance().getLogger().println("Finished writing key binds data to file '" + inpFile.getName() + "'!");
    }

    /**
     * Changes the cursor from a list of standard cursors located in
     * {@link wrath.client.input.Key}.
     * @param cursormode The {@link wrath.client.input.Key} Cursor to switch to.
     */
    public void setCursor(int cursormode)
    {
        if(cursor != -1)
        {
            GLFW.glfwDestroyCursor(cursor);
            cursor = -1;
        }
        cursor = GLFW.glfwCreateStandardCursor(cursormode);
        GLFW.glfwSetCursor(Game.getCurrentInstance().getWindowManager().getWindowID(), cursor);
    }

    /**
     * Enables or disables the cursor.
     * @param cursorEnable Whether the cursor should be enabled or
     * disabled.
     */
    public void setCursorEnabled(boolean cursorEnable)
    {
        if(cursorEnable)
            GLFW.glfwSetInputMode(Game.getCurrentInstance().getWindowManager().getWindowID(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        else
            GLFW.glfwSetInputMode(Game.getCurrentInstance().getWindowManager().getWindowID(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    /**
     * Sets all the default engine functions and bindings to the default keys.
     * For further reference, refer to DefaultKeyBindings.txt in the default package.
     */
    public void setEngineKeysToDefault()
    {
        addDefaultKeyBinding(Key.KEY_ESCAPE, Key.MOD_SHIFT, KeyAction.KEY_PRESS, "stop");
        addDefaultKeyBinding(Key.KEY_F3, Key.MOD_NONE, KeyAction.KEY_PRESS, "toggle_fps");
        addDefaultKeyBinding(Key.KEY_F12, Key.MOD_NONE, KeyAction.KEY_PRESS, "screenshot");
        addDefaultKeyBinding(Key.KEY_ENTER, Key.MOD_ALT, KeyAction.KEY_PRESS, "toggle_windowstate_fullscreen");
        addDefaultKeyBinding(Key.KEY_ENTER, Key.MOD_SHIFT, KeyAction.KEY_PRESS, "center_window");
        addDefaultKeyBinding(Key.KEY_UP, Key.MOD_ALT, KeyAction.KEY_PRESS, "toggle_windowstate_fullwindowed");
        addDefaultKeyBinding(Key.KEY_DOWN, Key.MOD_ALT, KeyAction.KEY_PRESS, "minimize_window");
        addDefaultKeyBinding(Key.KEY_HOME, Key.MOD_CTRL + Key.MOD_SHIFT, KeyAction.KEY_PRESS, "bind_keys_to_defaults");
        addDefaultKeyBinding(Key.KEY_HOME, Key.MOD_CTRL + Key.MOD_ALT + Key.MOD_SHIFT, KeyAction.KEY_PRESS, "reset_keys");
        addDefaultKeyBinding(Key.KEY_S, Key.MOD_CTRL + Key.MOD_ALT, KeyAction.KEY_PRESS, "save_internals");
        addDefaultKeyBinding(Key.KEY_C, Key.MOD_ALT, KeyAction.KEY_PRESS, "toggle_cursor");
        if(Game.getCurrentInstance().getRenderMode() == RenderMode.Mode3D)
        {
            addDefaultKeyBinding(Key.KEY_W, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "move_forward");
            addDefaultKeyBinding(Key.KEY_S, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "move_backward");
        }
        else
        {
            addDefaultKeyBinding(Key.KEY_W, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "move_up");
            addDefaultKeyBinding(Key.KEY_S, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "move_down");
        }
        addDefaultKeyBinding(Key.KEY_A, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "move_left");
        addDefaultKeyBinding(Key.KEY_D, Key.MOD_NONE, KeyAction.KEY_HOLD_DOWN, "move_right");
    }
    
    /**
     * Clears all defined key bindings.
     * This DOES NOT set them to default, refer to {@link wrath.client.input.InputManager#bindKeysToDefaults()}.
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
    
    /**
     * Keeps track of keys.
     *
     * @author Trent Spears
     */
    public class KeyData
    {
        public static final String CUSTOM_EVENT = "__CUSTOM";
        
        private final KeyAction action;
        private final int actionRaw;
        private final Runnable function;
        private final String functionID;
        private final int key;
        private final int mod;

        /**
         * Constructor.
         *
         * @param action The {@link wrath.client.input.KeyAction} that will
         * trigger the function.
         * @param function The {@link java.lang.Runnable} function that will be
         * triggered by the Key functions.
         * @param glfwKey The GLFW key that will trigger the function. This can
         * be found at {@link wrath.client.input.Key}.
         * @param glfwMod The Mod keys that will trigger the function. This can
         * be found at {@link wrath.client.input.Key}.
         */
        public KeyData(int glfwKey, int glfwMod, KeyAction action, Runnable function)
        {
            this.action = action;
            if (action == KeyAction.KEY_RELEASE)this.actionRaw = GLFW.GLFW_RELEASE;
            else this.actionRaw = GLFW.GLFW_PRESS;
           
            if(action == KeyAction.KEY_HOLD_DOWN)
                this.function = () ->
                {
                    persMap.put(glfwKey, function);
                    function.run();
                };
            else this.function = function;
            this.functionID = CUSTOM_EVENT;
            this.key = glfwKey;
            this.mod = glfwMod;
        }

        /**
         * Constructor.
         *
         * @param action The {@link wrath.client.input.KeyAction} that will
         * trigger the function.
         * @param functionID The saved function ID that will be triggered by the
         * Key functions.
         * @param glfwKey The GLFW key that will trigger the function. This can
         * be found at {@link wrath.client.input.Key}.
         * @param glfwMod The Mod keys that will trigger the function. This can
         * be found at {@link wrath.client.input.Key}.
         */
        public KeyData(int glfwKey, int glfwMod, KeyAction action, String functionID)
        {
            this.action = action;
            if (action == KeyAction.KEY_RELEASE) this.actionRaw = GLFW.GLFW_RELEASE;
            else this.actionRaw = GLFW.GLFW_PRESS;
            
            if(action == KeyAction.KEY_HOLD_DOWN)
                this.function = () ->
                {
                    persMap.put(glfwKey, getSavedFunction(functionID));
                    getSavedFunction(functionID).run();
                };
            else this.function = getSavedFunction(functionID);
            this.functionID = functionID;
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
         * Gets the {@link wrath.client.input.KeyAction} of the key set. This is
         * used to determine whether to execute the function when a key is
         * pressed, or execute the function when the key is released.
         *
         * @return Returns the {@link wrath.client.input.KeyAction} specified in
         * the Constructor.
         */
        public KeyAction getAction()
        {
            return action;
        }

        /**
         * Gets the function associated with this Key Data set.
         *
         * @return Returns the function associated with this Key Data set.
         */
        public Runnable getEvent()
        {
            return function;
        }

        /**
         * Gets the String name of the function bound to this Key Data.
         * If function bound to this Key Data is NOT a saved function, method will return {@link wrath.client.input.InputManager.KeyData#CUSTOM_EVENT}.
         * @return Returns the String name of the function bound to this Key Data.
         */
        public String getFunctionID()
        {
            return functionID;
        }
        
        /**
         * Gets the {@link wrath.client.input.Key}.
         *
         * @return Returns the key id.
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Returns the {@link wrath.client.input.Key} modification.
         *
         * @return Gets the KeyMod assigned to the data.
         */
        public int getKeyMod()
        {
            return mod;
        }

        /**
         * Used by the internal engine to obtain the GLFW action code.
         *
         * @return Returns the int version of the KeyAction for use with GLFW.
         */
        public int getRawAction()
        {
            return actionRaw;
        }
    }
    
    /**
     * Used completely behind-the-scenes to allow for multiple actions to be bound to one key.
     */
    private class KeyList
    {
        private final ArrayList<KeyData> down_list = new ArrayList<>();
        private final ArrayList<KeyData> up_list = new ArrayList<>();
        
        /**
         * Constructor.
         * @param first The first KeyData element in this list. 
         */
        protected KeyList(KeyData first)
        {
            if(first.getAction() == KeyAction.KEY_RELEASE) up_list.add(first);
            else down_list.add(first);
        }
        
        /**
         * Adds a key binding to the Key List.
         * @param binding The KeyData binding to add to the Key List.
         */
        protected void addBinding(KeyData binding)
        {
            if(binding.getAction() == KeyAction.KEY_RELEASE) up_list.add(binding);
            else down_list.add(binding);
        }
        
        /**
         * Checks to see if the Key List contains the specified KeyData.
         * @param binding The {@link wrath.client.input.InputManager.KeyData} to check the presence of.
         * @return Returns true if the binding argument is present, otherwise false.
         */
        protected boolean containsBinding(KeyData binding)
        {
            return (up_list.contains(binding) || down_list.contains(binding));
        }
        
        /**
         * Returns a complete list of all key bindings present in this Key List.
         * @return Returns a complete list of all key bindings present in this Key List.
         */
        protected KeyData[] getAllElements()
        {
            KeyData[] ret = new KeyData[up_list.size() + down_list.size()];
            down_list.toArray(ret);
            int i = down_list.size();
            for(KeyData d : up_list)
            {
                ret[i] = d;
                i++;
            }
            return ret;
        }
        
        /**
         * Removes Key Data (specified by the binding argument) from this Key List, if present.
         * @param binding The {@link wrath.client.input.InputManager.KeyData} to remove from this Key List.
         */
        protected void removeBinding(KeyData binding)
        {
            if(up_list.contains(binding)) up_list.remove(binding);
            else if(down_list.contains(binding)) down_list.remove(binding);
        }
        
        /**
         * Used to activate functions bound to this button.
         * @param rawAction The GLFW key-action code that occurred.
         * @param keyMod The GLFW key mods code that occurred.
         */
        private void trigger(int rawAction, int keyMod)
        {
            if(rawAction == GLFW.GLFW_PRESS)
                down_list.stream().forEach((d) -> 
                {
                    if(d.getKeyMod() == keyMod) d.execute();
                });
            else if(rawAction == GLFW.GLFW_RELEASE)
                up_list.stream().forEach((d) -> 
                {
                    if(d.getKeyMod() == keyMod) d.execute();
                });
        }
    }
}
