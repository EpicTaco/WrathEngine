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

/**
 * The object that describes a script.
 * @author Trent Spears
 */
public class Script
{
    private boolean compiled = false;
    private Object compiledObject = null;
    private final ScriptManager man;
    private final File scriptFile;
    
    /**
     * Constructor.
     * @param mgr The {@link wrath.common.scripts.ScriptManager} associated with the Script.
     * @param scriptFile The {@link java.io.File} where the described script is located.
     */
    protected Script(ScriptManager mgr, File scriptFile)
    {
        man = mgr;
        this.scriptFile = scriptFile;
    }
    
    /**
     * Compiles code from the script file.
     * This can only be done once. To re-read and re-compile the Script, use {@link wrath.common.scripts.Script#reloadScript()}
     */
    public void compile()
    {
        if(compiled) return;
        man.getScriptLogger().log("Compiling script '" + getScriptName() + "'!");
        compiledObject = man.compileScript(this);
        man.getScriptEventHandler().onScriptCompile(man, this);
        compiled = true;
    }
    
    /**
     * Executes the Script's code.
     */
    public void execute()
    {
        execute(false);
    }
    
    /**
     * Executes the Script's code.
     * @param dedicatedThread If true, a new {@link java.lang.Thread} is made to execute the script.
     */
    public void execute(boolean dedicatedThread)
    {
        if(!compiled) return;
        
        man.getScriptLogger().log("Executing script '" + getScriptName() + "'!");
        man.executeObject(compiledObject, dedicatedThread);
        man.getScriptEventHandler().onScriptCompile(man, this);
    }
    
    /**
     * Gets the {@link java.io.File} the script is from.
     * @return Returns the {@link java.io.File} the script is from.
     */
    public File getScriptFile()
    {
        return scriptFile;
    }
    
    /**
     * Gets the 'user-friendly' script name, which is simply retrieved with {@link java.io.File#getName()}.
     * @return Returns the file name of the script.
     */
    public String getScriptName()
    {
        return scriptFile.getName();
    }
    
    /**
     * If true, the script is compiled and ready to be executed.
     * @return If true, the script is compiled and ready to be executed.
     */
    public boolean isCompiled()
    {
        return compiled;
    }
    
    /**
     * Deletes previously obtained objects and data and re-reads from the script file.
     */
    public void reloadScript()
    {
        compiled = false;
        compile();
    }
}
