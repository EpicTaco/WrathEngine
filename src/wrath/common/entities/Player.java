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
package wrath.common.entities;

import java.util.ArrayList;
import org.lwjgl.util.vector.Vector3f;
import wrath.common.entities.events.PlayerEventHandler;
import wrath.common.world.World;

/**
 * Class to represent a player.
 * @author Trent Spears
 */
public class Player extends Entity
{   
    private static final ArrayList<PlayerEventHandler> plrHandlers = new ArrayList<>();
    private static RootPlayerEventHandler roothandler = null;
    
    public static void addPlayerEventHandler(PlayerEventHandler handler)
    {
        plrHandlers.add(handler);
    }
    
    
    
    
    public Player()
    {
        this(null, null);
    }
    
    public Player(Vector3f worldLocation, World world)
    {
        super(worldLocation, world);
        if(roothandler == null) roothandler = new RootPlayerEventHandler();
    }
    
    
    
    
    
    private class RootPlayerEventHandler implements PlayerEventHandler
    {
        
    }
}
