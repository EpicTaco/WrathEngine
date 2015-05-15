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
 * Class used to represent a group of x, y and z coordinates.
 * @author Trent Spears
 */
public class Vector3f implements Serializable
{
    public float x,y,z;
    
    public Vector3f()
    {
        this(0f, 0f, 0f);
    }
    
    public Vector3f(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public float lengthSquared()
    {
        return (x*x) + (y*y) + (z*z);
    }
    
    public void negate()
    {
        this.x = -x;
        this.y = -y;
        this.z = -z;
    }
    
    public void set(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
    
    public void set(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public void set(Vector3f vec)
    {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }
    
    public void set(Vector2f vec)
    {
        this.x = vec.x;
        this.y = vec.y;
    }
    
    public void set(Vector2f vec, float z)
    {
        this.x = vec.x;
        this.y = vec.y;
        this.z = z;
    }
    
    public void translate(float x, float y, float z)
    {
        this.x += x;
        this.y += y;
        this.z += z;
    }
    
    
    // Static methods
    
    public static Vector3f add(Vector3f left, Vector3f right)
    {
        return new Vector3f(left.x + right.x, left.y + right.y, left.z + right.z);
    }
    
    public static Vector3f add(Vector3f left, Vector3f right, Vector3f returnValue)
    {
        if(returnValue == null) return new Vector3f(left.x + right.x, left.y + right.y, left.z + right.z);
        else
        {
            returnValue.set(left.x + right.x, left.y + right.y, left.z + right.z);
            return returnValue;
        }
    }
    
    public static Vector3f sub(Vector3f left, Vector3f right)
    {
        return new Vector3f(left.x - right.x, left.y - right.y, left.z - right.z);
    }
    
    public static Vector3f sub(Vector3f left, Vector3f right, Vector3f returnValue)
    {
        if(returnValue == null) return new Vector3f(left.x - right.x, left.y - right.y, left.z - right.z);
        else
        {
            returnValue.set(left.x - right.x, left.y - right.y, left.z - right.z);
            return returnValue;
        }
    }
    
    public static Vector3f cross(Vector3f left, Vector3f right, Vector3f returnValue)
    {
        if(returnValue == null) return new Vector3f(left.y * right.z - left.z * right.y,
                                                    right.x * left.z - right.z * left.x,
                                                    left.x * right.y - left.y * right.x);
        else
        {
            returnValue.set(left.y * right.z - left.z * right.y,
                            right.x * left.z - right.z * left.x,
                            left.x * right.y - left.y * right.x);
            return returnValue;
        }
    }
}
