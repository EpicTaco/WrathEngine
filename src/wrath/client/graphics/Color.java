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

/**
 * Class to help specify RGBA values in one object.
 * @author Trent Spears
 */
public class Color
{
    public static final Color BLACK = new Color(0, 0, 0, 1);
    public static final Color DEFAULT = new Color(1, 1, 1, 0); 
    public static final Color WHITE = new Color(1, 1, 1, 1);
    
    private final float r,g,b,a;
    
    /**
     * Constructor.
     * @param red On a scale of 0.0 (no red) to 1.0 (pure red).
     * @param green On a scale of 0.0 (no green) to 1.0 (pure green).
     * @param blue On a scale of 0.0 (no blue) to 1.0 (pure blue).
     */
    public Color(float red, float green, float blue)
    {
        this(red, green, blue, 1.0f);
    }
    
    /**
     * Constructor.
     * @param red On a scale of 0.0 (no red) to 1.0 (pure red).
     * @param green On a scale of 0.0 (no green) to 1.0 (pure green).
     * @param blue On a scale of 0.0 (no blue) to 1.0 (pure blue).
     * @param alpha On a scale of 0.0 (transparent) to 1.0 (opaque).
     */
    public Color(float red, float green, float blue, float alpha)
    {
        this.r = red;
        this.g = green;
        this.b = blue;
        this.a = alpha;
    }
    
    /**
     * Gets the color's alpha (transparency) value.
     * @return Returns a float on a scale of 0.0 (transparent) to 1.0 (opaque).
     */
    public float getAlpha()
    {
        return a;
    }
    
    /**
     * Gets the color's blue value.
     * @return Returns a float on a scale of 0.0 (no blue) to 1.0 (pure blue).
     */
    public float getBlue()
    {
        return b;
    }
    
    /**
     * Gets the color's green value.
     * @return Returns a float on a scale of 0.0 (no green) to 1.0 (pure green).
     */
    public float getGreen()
    {
        return g;
    }
    
    /**
     * Gets the color's red value.
     * @return Returns a float on a scale of 0.0 (no red) to 1.0 (pure red).
     */
    public float getRed()
    {
        return r;
    }
}
