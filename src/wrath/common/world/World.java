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
package wrath.common.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import wrath.util.Logger;

/**
 * Class to track Worlds and a convenient class to carry/save data.
 * @author Trent Spears
 */
public class World implements Serializable
{
    private File file;
    private transient WorldEventHandler handler;
    
    public World(File file)
    {
        if(!file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch(IOException e){}
        }
        else
        {
            //Load world.
        }
    }
    
    /**
     * Saves the World and all data in a compressed format to the previously specified {java.io.File}.
     */
    public void save()
    {
        FileOutputStream fos = null;
        GZIPOutputStream gout = null;
        ObjectOutputStream out = null;
        
        try
        {
            out = new ObjectOutputStream((gout = new GZIPOutputStream((fos = new FileOutputStream(file)))));
            out.writeObject(this);
            gout.finish();
            out.flush();
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not save World! I/O Error!");
        }
        
        if(out == null || fos == null || gout == null) return;
        
        try
        {
            out.close();
            gout.close();
            fos.close();
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not close World streams! I/O Error!");
        }
    }
    
    /**
     * Sets this World's {@link wrath.common.world.WorldEventHandler}.
     * Does not carry over after a previously saved World is loaded.
     * @param handler The {@link wrath.common.world.WorldEventHandler} to link to this World.
     */
    public void setEventHandler(WorldEventHandler handler)
    {
        this.handler = handler;
    }
    
    //Static Methods
    
    /**
     * Reads and returns all World data from the specified {java.io.File}.
     * Returns null if corrupt/invalid.
     * @param file The file to read world data from.
     * @return Returns {@link wrath.common.world.World} from the specified {java.io.File}, null if invalid or corrupt.
     */
    public static World load(File file)
    {
        World ret = null;
        FileInputStream fis = null;
        GZIPInputStream in = null;
        ObjectInputStream is = null;
        
        try
        {
            is = new ObjectInputStream((in = new GZIPInputStream(new FileInputStream(file))));
            Object genObj = is.readObject();
            if(genObj instanceof World) ret = (World) genObj;
        }
        catch(IOException | ClassNotFoundException e)
        {
            Logger.getErrorLogger().log("Could not load World! I/O Error!");
        }
        
        if(in == null || fis == null || is == null) return ret;
        
        try
        {
            is.close();
            in.close();
            fis.close();
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not close World streams! I/O Error!");
        }
        
        return ret;
    }
}
