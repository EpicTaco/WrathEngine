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
package wrath.common.math;

import java.io.Serializable;

/**
 * Class used to represent a group of x and y coordinates.
 * @author Trent Spears
 */
public class Vector2f implements Serializable
{
    public float x,y;
    
    public Vector2f()
    {
        this(0f, 0f);
    }
    
    public Vector2f(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
    
    public float lengthSquared()
    {
        return (x*x) + (y*y);
    }
    
    public void set(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
    
    public void set(Vector2f vec)
    {
        this.x = vec.x;
        this.y = vec.y;
    }
}
