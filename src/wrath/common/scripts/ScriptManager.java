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
package wrath.common.scripts;

import java.io.File;
import java.util.ArrayList;
import wrath.util.Config;
import wrath.util.Logger;

/**
 * Class to manage {@link wrath.common.scripts.Script} and all-things scripting.
 * @author Trent Spears
 */
public abstract class ScriptManager
{
    public static final Logger SCRIPT_LOGGER = new Logger("scripts");
    public static final Config SCRIPT_CONFIG = new Config("scriping");
    private static final ArrayList<ScriptManager> mgrList = new ArrayList<>();
    
    /**
     * Closes all resources associated with scripting.
     * Scripting can not occur after this is called.
     */
    public static void closeScripting()
    {
        SCRIPT_LOGGER.close();
        SCRIPT_CONFIG.save();
        mgrList.stream().forEach((m) -> 
        {
            m.close();
        });
    }
    
    /**
     * Gets the {@link wrath.util.Config} associated with all Script Managers.
     * @return Returns the {@link wrath.util.Config} associated with all Script Managers.
     */
    public static Config getScriptConfig()
    {
        return SCRIPT_CONFIG;
    }
    
    /**
     * Gets the {@link wrath.util.Logger} associated with all Script Managers.
     * @return Returns the {@link wrath.util.Logger} associated with all Script Managers.
     */
    public static Logger getScriptLogger()
    {
        return SCRIPT_LOGGER;
    }
    
    
    protected final String fileExtension;
    protected final Object parentObject;
    private final RootScriptEventHandler rootHandler;
    private final ArrayList<ScriptEventHandler> handlers = new ArrayList<>();
    
     /**
     * Constructor.
     * @param parentObject The object scripts should refer to. 
     * @param fileExtension The file extension of scripts that should be detected. For example, Python would use '.py'.
     */
    protected ScriptManager(Object parentObject, String fileExtension)
    {
        this.fileExtension = fileExtension;
        this.parentObject = parentObject;
        this.rootHandler = new RootScriptEventHandler();
        
        autoLoadScripts();
    }
    
    /**
     * Adds a {@link wrath.common.scripts.ScriptEventHandler} to the list of handlers to respond to events.
     * @param handler The {@link wrath.common.scripts.ScriptEventHandler} to add to the listeners list.
     */
    public void addScriptEventHandler(ScriptEventHandler handler)
    {
        handlers.add(handler);
    }
    
    private void autoLoadScripts()
    {
        mgrList.add(this);
        if(SCRIPT_CONFIG.getBoolean("AutoLoadFromDirectory", true)) 
            loadScriptsFromDirectory(new File(SCRIPT_CONFIG.getString("AutoLoadDirectory", "etc/scripts/autoexec")), true, true);
    }
    
    /**
     * Compiled a script into a compiled {@link java.lang.Object} that can be executed.
     * @param script The {@link wrath.common.scripts.Script} to compile.
     * @return Returns the compiled {@link java.lang.Object}.
     */
    public Object compileScript(Script script){return null;}
    
    /**
     * Called to close and deallocate all resources associated with this Script Manager.
     */
    public void close(){}
    
    /**
     * Executes specified code in the scripting language's interactive console.
     * @param code The code to execute in console.
     */
    public void executeCode(String[] code){}
    
    /**
     * Executes an executable object into the language's interpreter.
     * @param compiledObject The compiled {@link java.lang.Object} of a previously-compiled {@link wrath.common.scripts.Script}.
     * @param dedicatedThread If true, a new {@link java.lang.Thread} is made to execute the script.
     */
    protected void executeObject(Object compiledObject, boolean dedicatedThread){}
    
    /**
     * Loads a {@link wrath.common.scripts.Script} into an object.
     * @param scriptFile The {@link java.io.File} where the script is located.
     * @param autoCompile If true, the script will automatically be compiled.
     * @param autoExecute If true, the script will be executed as soon as it is compiled.
     * @return Returns the {@link wrath.common.scripts.Script} object.
     */
    public Script loadScript(File scriptFile, boolean autoCompile, boolean autoExecute)
    {
        Script ret = new Script(this, scriptFile);
        this.getScriptEventHandler().onScriptLoad(this, ret);
        if(autoCompile) ret.compile();
        if(autoExecute) ret.execute(false);
        return ret;
    }
    
    /**
     * Loads {@link wrath.common.scripts.ScriptManager#loadScript(java.io.File, boolean, boolean)} for every script file in specified directory.
     * @param directory The directory to load all scripts in.
     * @param autoCompile If true, all scripts are compiled upon loading.
     * @param autoExecute If true, all scripts are executed upon compiling.
     * @return Returns an array of {@link wrath.common.scripts.Script}s that were loaded.
     */
    public Script[] loadScriptsFromDirectory(File directory, boolean autoCompile, boolean autoExecute)
    {
        if(!directory.exists() || !directory.isDirectory())
        {
            getScriptLogger().log("Could not load scripts from directory '" + directory.getAbsolutePath() + "', directory does not exist!");
            return null;
        }
        
        ArrayList<Script> scr = new ArrayList<>();
        
        for(File f : directory.listFiles())
            if(f.getName().toLowerCase().endsWith(fileExtension.toLowerCase()))
                scr.add(loadScript(f, autoCompile, autoExecute));
            
        
        Script[] ret = new Script[scr.size()];
        scr.toArray(ret);
        return ret;
    }
    
    /**
     * Gets the root {@link wrath.common.scripts.ScriptEventHandler} to report events occuring.
     * @return Returns the root {@link wrath.common.scripts.ScriptEventHandler} to report events occuring.
     */
    public ScriptEventHandler getScriptEventHandler()
    {
        return rootHandler;
    }
    
    
    
    private class RootScriptEventHandler implements ScriptEventHandler
    {
        @Override
        public void onScriptCompile(ScriptManager scriptManager, Script script)
        {
            handlers.stream().forEach((han) -> 
            {
                han.onScriptCompile(scriptManager, script);
            });
        }

        @Override
        public void onScriptExecute(ScriptManager scriptManager, Script script)
        {
            handlers.stream().forEach((han) -> 
            {
                han.onScriptExecute(scriptManager, script);
            });
        }

        @Override
        public void onScriptLoad(ScriptManager scriptManager, Script script)
        {
            handlers.stream().forEach((han) -> 
            {
                han.onScriptExecute(scriptManager, script);
            });
        }
    }
}
