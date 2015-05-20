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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import org.python.core.PyCode;
import org.python.util.PythonInterpreter;
import wrath.util.Logger;

/**
 * Class to manage Python Scripts in the program.
 * @author Trent Spears
 */
public class PythonScriptManager extends ScriptManager
{
    private static PythonScriptManager CUR_INSTANCE = null;
    private final PythonInterpreter inter = new PythonInterpreter();

    /**
     * Gets the current, most recent {@link wrath.common.scripts.PythonScriptManager} object.
     * @return Returns the current, most recent {@link wrath.common.scripts.PythonScriptManager} object.
     */
    public static PythonScriptManager getCurrentInstance()
    {
        return CUR_INSTANCE;
    }
    
    /**
     * Constructor.
     * @param parentObject The object scripts should refer to. 
     */
    public PythonScriptManager(Object parentObject)
    {
        super(parentObject, ".py");
        if(getScriptConfig().getBoolean("AssignParentObject", true))
            inter.set("parentObject", parentObject);
        inter.set("scriptsManager", this);
        afterConstructor();
    }

    private void afterConstructor()
    {
        CUR_INSTANCE = this;
    }
    
    @Override
    public void close()
    {
        inter.close();
    }

    @Override
    public Object compileScript(Script script)
    {
        ArrayList<String> str = new ArrayList();
        try 
        {
            BufferedReader in = new BufferedReader(new FileReader(script.getScriptFile()));
            Object obj = null;
            
            String inp;
            while ((inp = in.readLine()) != null) 
                str.add(inp);
        }
        catch (IOException e) 
        {
            Logger.getErrorLogger().log("Could not compile script '" + script.getScriptName() + "'! I/O Error!");
        }
        
        String fin = "";
        for(String s : str)
            fin = fin + s + "\n";
        
        return inter.compile(fin, script.getScriptName());
    }

    @Override
    public void executeCode(String[] code)
    {
        for (String line : code) 
            inter.exec(line);
    }

    @Override
    protected void executeObject(Object compiledObject, boolean dedicatedThread)
    {
        if ((compiledObject instanceof PyCode))
        {
            if(dedicatedThread) new Thread(() -> {inter.exec((PyCode) compiledObject);}).start();
            else inter.exec((PyCode) compiledObject);
        }
    }

    /**
     * Sets global variables in the Python script environment.
     * @param variableName The {@link java.lang.String} name of the variable to assign a value to.
     * @param value The {@link java.lang.Object} to assign to the specified value.
     */
    public void setGlobalVariable(String variableName, Object value)
    {
        this.inter.set(variableName, value);
    }
}
