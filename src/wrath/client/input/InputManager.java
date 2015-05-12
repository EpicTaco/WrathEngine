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
import wrath.client.enums.WindowState;
import wrath.util.Logger;

/**
 * Class to manage all input operations.
 * Used to organize code and clean up the {@link wrath.client.Game} class.
 */
public class InputManager
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
    
    private final Game game;
    
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
     * @param game The {@link wrath.client.Game} the InputManager is attached to.
     */
    public InputManager(Game game)
    {
        this.game = game;
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
     * Binds all default engine functions to keys.
     * For further reference, refer to DefaultKeyBindings.txt in the default package.
     */
    public void bindDefaultEngineKeys()
    {
        bindKey(Key.KEY_ESCAPE, Key.MOD_SHIFT, KeyAction.KEY_PRESS, () ->
        {
            game.stop();
        });
        
        bindKey(Key.KEY_F3, Key.MOD_NONE, KeyAction.KEY_PRESS, () ->
        {
            game.getWindowManager().setRenderFPS(!game.getWindowManager().isRenderingFPS());
        });
        
        bindKey(Key.KEY_F12, Key.MOD_NONE, KeyAction.KEY_PRESS, () ->
        {
            DateFormat format = new SimpleDateFormat("MM_dd_yyyy___HHmmss");
            Calendar now = Calendar.getInstance();
            game.getWindowManager().screenShot("screenshot_" + format.format(now.getTime()), ImageFormat.PNG);
        });
        
        bindKey(Key.KEY_ENTER, Key.MOD_ALT, KeyAction.KEY_PRESS, () ->
        {
            if(game.getWindowManager().getWindowState() == WindowState.FULLSCREEN) game.getWindowManager().setWindowState(WindowState.WINDOWED);
            else game.getWindowManager().setWindowState(WindowState.FULLSCREEN);
        });
        
        bindKey(Key.KEY_ENTER, Key.MOD_SHIFT, KeyAction.KEY_PRESS, () ->
        {
            game.getWindowManager().centerWindow();
        });
        
        bindKey(Key.KEY_UP, Key.MOD_ALT, KeyAction.KEY_PRESS, () ->
        {
            if(game.getWindowManager().getWindowState() == WindowState.FULLSCREEN_WINDOWED) game.getWindowManager().setWindowState(WindowState.WINDOWED);
            else game.getWindowManager().setWindowState(WindowState.FULLSCREEN_WINDOWED);
        });
        
        bindKey(Key.KEY_DOWN, Key.MOD_ALT, KeyAction.KEY_PRESS, () ->
        {
            game.getWindowManager().minimizeWindow();
        });
        
        bindKey(Key.KEY_HOME, Key.MOD_CTRL + Key.MOD_SHIFT, KeyAction.KEY_PRESS, () ->
        {
            bindKeysToDefaults();
        });
        
        bindKey(Key.KEY_HOME, Key.MOD_CTRL + Key.MOD_ALT + Key.MOD_SHIFT, KeyAction.KEY_PRESS, () ->
        {
            unbindAllKeys();
            bindDefaultEngineKeys();
            bindKeysToDefaults();
        });
        
        bindKey(Key.KEY_S, Key.MOD_CTRL + Key.MOD_ALT, KeyAction.KEY_PRESS, () ->
        {
            saveKeys();
            game.getConfig().save();
        });
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
        game.getLogger().log("Function '" + functionID + "' bound to key '" + key + "'.");
    }

    /**
     * Binds all pre-defined default keys.
     * Note that this DOES NOT clear all user-defined bindings, refer to {@link wrath.client.input.InputManager#unbindAllKeys()}.
     */
    public void bindKeysToDefaults()
    {
        game.getLogger().log("Setting keys to defaults!");
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
        scrStr.release();
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
        return ((2 / (double) game.getWindowManager().getHeight() * cury) - 1.0) * -1;
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
        File inpFile = new File(game.getConfig().getString("KeyBindsFile", "etc/keys.dat"));
        if(!inpFile.exists())
        {
            try
            {
                inpFile.createNewFile();
                game.getLogger().log("Saved key bindings not found, Generating file @ '" + inpFile.getAbsolutePath() + "'!");
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
            try
            {
                //Format: 'functionID:mod:action:key'
                if(inpFile.length() < 1)
                {
                    bindKeysToDefaults();
                    return;
                }
                
                game.getLogger().log("Reading key bindings from file '" + inpFile.getAbsolutePath() + "'!");
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
                            game.getLogger().log("Unknown function '" + functionID + "' while loading key binds from file '" + inpFile.getAbsolutePath() + "'; Line " + lineNum);
                            continue;
                        }
                        
                        try
                        {
                            keyMod = Integer.parseInt(inputArray[1]);
                        }
                        catch(NumberFormatException e)
                        {
                            game.getLogger().log("Unknown key mod value '" + inputArray[1] + "' while loading key binds from file '" + inpFile.getAbsolutePath() + "'; Line " + lineNum);
                            continue;
                        }
                        
                        action = KeyAction.valueOf(inputArray[2]);
                        if(action == null)
                        {
                            game.getLogger().log("Unknown key action value '" + inputArray[2] + "' while loading key binds from file '" + inpFile.getAbsolutePath() + "'; Line " + lineNum);
                            continue;
                        }
                        
                        try
                        {
                            key = Integer.parseInt(inputArray[3]);
                        }
                        catch(NumberFormatException e)
                        {
                            game.getLogger().log("Unknown key value '" + inputArray[3] + "' while loading key binds from file '" + inpFile.getAbsolutePath() + "'; Line " + lineNum);
                            continue;
                        }
                        
                        bindKey(key, keyMod, action, functionID);
                    }
                }
            }
            catch(IOException e)
            {
                Logger.getErrorLogger().log("Could not load saved keys from file '" + inpFile.getAbsolutePath() + "'! Binding default keys!");
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
        if(cursor != -1) GLFW.glfwSetCursor(game.getWindowManager().getWindowID(), cursor);
        
        GLFW.glfwSetCharCallback(game.getWindowManager().getWindowID(), (charStr = new GLFWCharCallback()
        {
            @Override
            public void invoke(long window, int codepoint)
            {
                game.getEventManager().getInputEventHandler().onCharInput((char) codepoint);
            }
        }));
        
        GLFW.glfwSetCursorPosCallback(game.getWindowManager().getWindowID(), (curStr = new GLFWCursorPosCallback()
        {
            @Override
            public void invoke(long window, double x, double y)
            {
                curx = x;
                cury = y;
                game.getEventManager().getInputEventHandler().onCursorMove(getCursorX(), getCursorY());
            }
        }));
        
        GLFW.glfwSetKeyCallback(game.getWindowManager().getWindowID(), (keyStr = new GLFWKeyCallback()
        {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods)
            {
                if(persMap.containsKey(key) && action == GLFW.GLFW_RELEASE) persMap.remove(key);
                if(keyMap.containsKey(key)) keyMap.get(key).trigger(action, mods);
            }
        }));
        
        GLFW.glfwSetMouseButtonCallback(game.getWindowManager().getWindowID(), (mkeyStr = new GLFWMouseButtonCallback()
        {
            @Override
            public void invoke(long window, int button, int action, int mods)
            {
                if(persMap.containsKey(button) && action == GLFW.GLFW_RELEASE) persMap.remove(button);
                if(keyMap.containsKey(button)) keyMap.get(button).trigger(action, mods);
            }
        }));
        
        GLFW.glfwSetScrollCallback(game.getWindowManager().getWindowID(), (scrStr = new GLFWScrollCallback()
        {
            @Override
            public void invoke(long window, double xoff, double yoff)
            {
                game.getEventManager().getInputEventHandler().onScroll(xoff, yoff);
            }
        }));
    }
    
    /**
     * Saves key bindings (containing saved functions) to pre-configured file "KeyBindsFile".
     */
    public void saveKeys()
    {
        //Format: 'functionID:mod:action:key'
        File inpFile = new File(game.getConfig().getString("KeyBindsFile", "etc/keys.dat"));
        if(!inpFile.exists())
        {
            try
            {
                inpFile.createNewFile();
            }
            catch(IOException ex)
            {
                Logger.getErrorLogger().log("Could not create Key Bindings file! I/O Error Occured!");
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
            Logger.getErrorLogger().log("Could not save key bindings, I/O Error Occured!");
        }
        
        game.getLogger().log("Finished writing key binds data to file '" + inpFile.getAbsolutePath() + "'!");
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
