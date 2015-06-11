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
package wrath.client.graphics;

import org.lwjgl.util.vector.Vector3f;

/**
 * Class to represent a light source.
 * @author Trent Spears
 */
public class Light
{
    private final Vector3f position;
    private Color color;
    
    /**
     * Creates a light source for models to reflect upon.
     * This will be updated in the near future...
     * @param position The {@link org.lwjgl.util.vector.Vector3f} representation of the light's location.
     * @param color The {@link wrath.client.graphics.Color} object describing the light's color.
     */
    public Light(Vector3f position, Color color)
    {
        this.position = position;
        this.color = color;
    }
    
    /**
     * Gets the {@link wrath.client.graphics.Color} of the light.
     * @return Returns the {@link wrath.client.graphics.Color} of the light.
     */
    public Color getColor()
    {
        return color;
    }
    
    /**
     * Gets the {@link org.lwjgl.util.vector.Vector3f} representation of the light's location.
     * @return Returns {@link org.lwjgl.util.vector.Vector3f} representation of the light's location.
     */
    public Vector3f getPosition()
    {
        return position;
    }
    
    /**
     * Sets the {@link wrath.client.graphics.Color} of the light.
     * @param color The new {@link wrath.client.graphics.Color} of the light.
     */
    public void setColor(Color color)
    {
        this.color = color;
    }
    
    /**
     * Sets the light's position on the screen.
     * @param x The X-coordinate of the light on the screen.
     * @param y The Y-coordinate of the light on the screen.
     * @param z The Z-coordinate of the light on the screen.
     */
    public void setPosition(float x, float y, float z)
    {
        position.x = x;
        position.y = y;
        position.z = z;
    }
    
    /**
     * Increments the position of the light by the specified amount.
     * @param dx The amount to increase the position on the X-Axis.
     * @param dy The amount to increase the position on the Y-Axis.
     * @param dz The amount to increase the position on the Z-Axis.
     */
    public void transformPosition(float dx, float dy, float dz)
    {
        position.x += dx;
        position.y += dy;
        position.z += dz;
    }
}
