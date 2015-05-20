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
package wrath.client;

import java.io.File;
import wrath.common.scripts.PythonScriptManager;
import wrath.common.scripts.ScriptManager;

/**
 * The class to launch the game off of a python script called 'initscript.py' in the 'assets' folder.
 * @author Trent Spears
 */
public class PythonEntryObject implements EntryObject
{   
    @Override
    public void init(String[] args)
    {
        ScriptManager.SCRIPT_CONFIG.setProperty("AutoLoadFromDirectory", false);
        ScriptManager.SCRIPT_CONFIG.setProperty("AssignParentObject", false);
        PythonScriptManager scripts = new PythonScriptManager(this);
        scripts.loadScript(new File("assets/initscript.py"), true, false).execute();
    }
}
