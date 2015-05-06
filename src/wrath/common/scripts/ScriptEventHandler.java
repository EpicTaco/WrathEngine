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

/**
 * Interface to handle all events that occur in Script Managers.
 * @author Trent Spears
 */
public interface ScriptEventHandler
{
    /**
     * Called when a script is compiled.
     * @param scriptManager The {@link wrath.common.scripts.ScriptManager} where this event occurs.
     * @param script The {@link wrath.common.scripts.Script} that is affected.
     */
    public abstract void onScriptCompile(ScriptManager scriptManager, Script script);
  
    /**
     * Called when a script is executed.
     * @param scriptManager The {@link wrath.common.scripts.ScriptManager} where this event occurs.
     * @param script The {@link wrath.common.scripts.Script} that is affected.
     */
    public abstract void onScriptExecute(ScriptManager scriptManager, Script script);
  
    /**
     * Called when a script object is created.
     * @param scriptManager The {@link wrath.common.scripts.ScriptManager} where this event occurs.
     * @param script The {@link wrath.common.scripts.Script} that is affected.
     */
    public abstract void onScriptLoad(ScriptManager scriptManager, Script script);
}
