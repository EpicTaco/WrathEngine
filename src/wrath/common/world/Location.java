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

import java.io.Serializable;

/**
 * Class to keep track of {@link wrath.common.world.World} coordinates.
 * @author Trent Spears
 */
public class Location implements Serializable
{
    private double x,y,z;
    private World world;
    
    /**
     * 2D Descriptor for a Location.
     * @param x The x coordinate (Across top edge).
     * @param y The Y coordinate (Down the side).
     * @param world The {@link wrath.common.world.World} object the Location is linked to, can be null.
     */
    public Location(double x, double y, World world)
    {
        this.x = x;
        this.y = y;
        z = -1337666420.1337666420133766642013376664201337666420;
        
        this.world = world;
    }
    
    /**
     * 3D Descriptor for a Location.
     * @param x The x coordinate (Left/Right).
     * @param y The Y coordinate (Forward/Backwards/Depth).
     * @param z The Z coordinate (Height).
     * @param world The {@link wrath.common.world.World} object the Location is linked to, can be null.
     */
    public Location(double x, double y, double z, World world)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        
        this.world = world;
    }
    
    /**
     * Gets the distance as a double between this location and the one specified.
     * @param compareTo The Location to calculate the distance to.
     * @return Returns the distance of the two Locations in double.
     */
    public double getDistance(Location compareTo)
    {
        if(getZ() == -1337666420.1337666420133766642013376664201337666420 && compareTo.getZ() == -1337666420.1337666420133766642013376664201337666420)
            return Math.sqrt(Math.pow((compareTo.getX() - getX()), 2) + Math.pow((compareTo.getY() - getY()), 2));
        else if(getZ() != -1.337 && compareTo.getZ() != -1.1337)
            return Math.sqrt(Math.pow((compareTo.getX() - getX()), 2) + Math.pow((compareTo.getY() - getY()), 2) + Math.pow((compareTo.getZ() - getZ()), 2));
        else return -1.0;
    }
    
    /**
     * Gets the {@link wrath.common.world.World} object attached to the Location.
     * This can be null.
     * @return Returns the {@link wrath.common.world.World} object attached to the Location.
     */
    public World getWorld()
    {
        return world;
    }
    
    /**
     * Gets the X Coordinate attached to the Location.
     * @return Returns the X Coordinate attached to the Location.
     */
    public double getX()
    {
        return x;
    }
    
    /**
     * Gets the Y Coordinate attached to the Location.
     * @return Returns the Y Coordinate attached to the Location.
     */
    public double getY()
    {
        return y;
    }
    
    /**
     * Gets the Z Coordinate attached to the Location (3D Only).
     * @return Returns the Z Coordinate attached to the Location.
     */
    public double getZ()
    {
        return z;
    }
    
    /**
     * Adds specified amount to the X coordinate and returns the new value of X.
     * @param add The amount to add to the X coordinate.
     * @return Returns the new value of the X coordinate.
     */
    public double incrementX(double add)
    {
        x+=add;
        return x;
    }
    
    /**
     * Adds specified amount to the Y coordinate and returns the new value of Y.
     * @param add The amount to add to the Y coordinate.
     * @return Returns the new value of the Y coordinate.
     */
    public double incrementY(double add)
    {
        y+=add;
        return y;
    }
    
    /**
     * Adds specified amount to the Z coordinate and returns the new value of Z.
     * @param add The amount to add to the Z coordinate.
     * @return Returns the new value of the Z coordinate.
     */
    public double incrementZ(double add)
    {
        z+=add;
        return z;
    }
    
    /**
     * 2D Method to update coordinates.
     * @param x The x coordinate (Across top edge).
     * @param y The Y coordinate (Down the side).
     */
    public void setLocation(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    
    /**
     * 3D Method to update coordinates.
     * @param x The x coordinate (Left/Right).
     * @param y The Y coordinate (Forward/Backwards/Depth).
     * @param z The Z coordinate (Height).
     */
    public void setLocation(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Sets the {@link wrath.common.world.World} object to be linked to this Location.
     * @param world The {@link wrath.common.world.World} object the Location is linked to.
     */
    public void setWorld(World world)
    {
        this.world = world;
    }
}
