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
package wrath.client.graphics.graphics2D;

import org.lwjgl.opengl.GL11;
import wrath.client.graphics.Color;
import wrath.client.graphics.Renderable;
import wrath.client.graphics.Texture;

/**
 * This class describes a 2D rectangular background.
 * @author Trent Spears
 */
public class Background implements Renderable
{
    private Color color = null;
    private Texture tex = null;
    
    /**
     * Constructor.
     * @param texture The {@link wrath.client.graphics.Texture} to render in the background. Can be null. 
     */
    public Background(Texture texture)
    {
        this(texture, Color.DEFAULT);
    }
    
    /**
     * Constructor.
     * @param texture The {@link wrath.client.graphics.Texture} to render in the background. Can be null. 
     * @param color The {@link wrath.client.graphics.Color} to render the background in. Cannot be null.
     */
    public Background(Texture texture, Color color)
    {
        this.tex = texture;
        this.color = color;
    }
    
    @Override
    public void render()
    {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        if(tex != null) tex.bindTexture();
        color.bindColor();
        GL11.glBegin(GL11.GL_QUADS);
        {
            //Top Left
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex3f(-1.0f, 1.0f, 0.0f);
            
            //Top Right
            GL11.glTexCoord2f(1, 0);
            GL11.glVertex3f(1.0f, 1.0f, 0.0f);
            
            //Bottom Right
            GL11.glTexCoord2f(1, 1);
            GL11.glVertex3f(1.0f, -1.0f, 0.0f);
            
            //Bottom Left
            GL11.glTexCoord2f(0, 1);
            GL11.glVertex3f(-1.0f, -1.0f, 0.0f);
        }
        GL11.glEnd();
        color.unbindColor();
        Texture.unbindTextures();
    }
    
    /**
     * Changes the background's color.
     * @param color The {@link wrath.client.graphics.Color} to render the background in.
     */
    public void setColor(Color color)
    {
        this.color = color;
    }
    
    /**
     * Changes the background's Texture (image).
     * @param tex The {@link wrath.client.graphics.Texture} to render for the background.
     */
    public void setTexture(Texture tex)
    {
        this.tex = tex;
    }
}
