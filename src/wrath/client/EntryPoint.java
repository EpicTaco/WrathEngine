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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * This is the entry class of the engine.
 * Refer to ExplainingInitFile.txt in the assets folder for more information.
 * @author Trent Spears
 */
public class EntryPoint
{
    public static void main(String[] args)
    {
        if(!new File("assets/init").exists())
            ClientUtils.throwInternalError("assets/init file is missing!", true);
        
        try
        {
            BufferedReader in = new BufferedReader(new FileReader("assets/init"));
            String fline = in.readLine();
            in.close();
            
            if(fline == null) ClientUtils.throwInternalError("assets/init could not be read! I/O Error!", true);
            
            try
            {
                EntryObject obj = (EntryObject) EntryObject.class.getClassLoader().loadClass(fline).newInstance();
                obj.init(args);
            }
            catch(InstantiationException | IllegalAccessException e)
            {
                ClientUtils.throwInternalError("Failed to load EntryObject!", true);
            }
            catch(ClassNotFoundException e)
            {
                ClientUtils.throwInternalError("Invalid EntryObject denoted as '" + fline + "'!", true);
            }
        }
        catch(IOException e)
        {
            ClientUtils.throwInternalError("assets/init could not be read! I/O Error!", true);
        }
    }
}
