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
import wrath.common.javaloader.JarLoader;


/**
 * The class to launch the game off of a Java jar called 'game.jar' in the 'assets' folder.
 * Do not implement this class. This is done automatically.
 * @author Trent Spears
 */
public class JavaEntryObject implements EntryObject
{   
    private final String entryClassPath;
    
    protected JavaEntryObject(String entryClassPath)
    {
        this.entryClassPath = entryClassPath;
    }
    
    @Override
    public void init(String[] args)
    {
        //Load 'game.jar' from assets folder
        EntryObject obj = (EntryObject) JarLoader.loadObject(new File("assets/game.jar"), entryClassPath);
        obj.init(args);
    }
}
