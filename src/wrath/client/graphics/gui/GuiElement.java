/**
 *  Wrath Engine
 *  Copyright (C) 2015 Trent Spears
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
package wrath.client.graphics.gui;

import org.lwjgl.opengl.GL11;
import wrath.client.graphics.Color;
import wrath.client.graphics.Renderable;
import wrath.client.graphics.Texture;

/**
 * Class to describe elements of the GUI.
 * @author Trent Spears
 */
public abstract class GuiElement implements Renderable
{
    private Color color = Color.WHITE;
    private float w = 0, h = 0, x = 0, y = 0;
    private boolean resizable;
    private Texture texture;
    
    /**
     * Constructor of the GUI Element.
     * @param width The width, measured in OpenGL screen scale, of this GUI element.
     * @param height The height, measured in OpenGL screen scale, of this GUI element.
     * @param initx The X-coordinate of the top-left corner of this GUI element.
     * @param inity The Y-coordinate of the top-left corner of this GUI element.
     * @param resizable If true, the element can be resized by dragging a mouse.
     * @param texture The {@link wrath.client.graphics.Texture} to render over this GUI element. Can be null.
     */
    protected GuiElement(float width, float height, float initx, float inity, boolean resizable, Texture texture)
    {
        this(width, height, initx, inity, resizable, texture, Color.WHITE);
    }
    
    /**
     * Constructor of the GUI Element.
     * @param width The width, measured in OpenGL screen scale, of this GUI element.
     * @param height The height, measured in OpenGL screen scale, of this GUI element.
     * @param initx The X-coordinate of the top-left corner of this GUI element.
     * @param inity The Y-coordinate of the top-left corner of this GUI element.
     * @param resizable If true, the element can be resized by dragging a mouse.
     * @param texture The {@link wrath.client.graphics.Texture} to render over this GUI element. Can be null.
     * @param color The {@link wrath.client.graphics.Color} of the element.
     */
    protected GuiElement(float width, float height, float initx, float inity, boolean resizable, Texture texture, Color color)
    {
        this.w = width;
        this.h = height;
        this.x = initx;
        this.y = inity;
        this.resizable = resizable;
        if(color != null) this.color = color;
        this.texture = texture;
    }
    
    /**
     * Gets the {@link wrath.client.graphics.Color} of the element.
     * @return Returns the {@link wrath.client.graphics.Color} of the element.
     */
    public Color getColor()
    {
        return color;
    }
    
    /**
     * Gets the height, measured in OpenGL screen scale, of this GUI element.
     * @return Returns the height, measured in OpenGL screen scale, of this GUI element.
     */
    public float getHeight()
    {
        return h;
    }
    
    /**
     * Gets the X-coordinate of the top-left corner of this GUI element.
     * @return Returns the X-coordinate of the top-left corner of this GUI element.
     */
    public float getTopLeftX()
    {
        return x;
    }
    
    /**
     * Gets the Y-coordinate of the top-left corner of this GUI element.
     * @return Returns the Y-coordinate of the top-left corner of this GUI element.
     */
    public float getTopLeftY()
    {
        return y;
    }
    
    /**
     * Gets the width, measured in OpenGL screen scale, of this GUI element.
     * @return Returns the width, measured in OpenGL screen scale, of this GUI element.
     */
    public float getWidth()
    {
        return w;
    }
    
    /**
     * Checks if the specified coordinates are within the bounds of this GUI element.
     * @param x The X coordinate of the screen.
     * @param y The Y coordinate of the screen.
     * @return If true, the specified coordinates are within this GUI element.
     */
    public boolean isInBounds(float x, float y)
    {
        if(x < this.x || y > this.y) return false;
        if(x > this.x + this.w || y < this.y - this.h) return false;
        
        return true;
    }
    
    /**
     * Gets whether or not this element should be resize-able via dragging the mouse.
     * @return If true, the element can be resized by dragging a mouse.
     */
    public boolean isResizable()
    {
        return resizable;
    }
    
    @Override
    public void renderSetup()
    {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST | GL11.GL_BLEND);
        GL11.glDepthMask(false);
        if(texture != null) texture.bindTexture();
        GL11.glBegin(GL11.GL_QUADS);
    }
    
    @Override
    public void render(boolean consolidated)
    {
        if(consolidated) renderSetup();
        color.bindColor();

        GL11.glTexCoord2f(0, 1);
        GL11.glVertex3f(x, y - h, 0);

        GL11.glTexCoord2f(1, 1);
        GL11.glVertex3f(x + w, y - h, 0);

        GL11.glTexCoord2f(1, 0);
        GL11.glVertex3f(x + w, y, 0);

        GL11.glTexCoord2f(0, 0);
        GL11.glVertex3f(x, y, 0);
        if(consolidated) renderStop();
    }
    
    @Override
    public void renderStop()
    {
        GL11.glEnd();
        color.unbindColor();
        Texture.unbindTextures();
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }
    

    /**
     * Sets the {@link wrath.client.graphics.Color} of the GUI element.
     * @param color The {@link wrath.client.graphics.Color} to render the GUI component in.
     */
    public void setColor(Color color)
    {
        this.color = color;
    }
    
    /**
     * Changes the measurements of this GUI element.
     * @param width The width, measured in OpenGL screen scale, of this GUI element.
     * @param height The height, measured in OpenGL screen scale, of this GUI element.
     */
    public void setDimensions(float width, float height)
    {
        this.w = width;
        this.h = height;
    }
    
    /**
     * Changes the position of this GUI element.
     * @param x The X coordinate of the screen.
     * @param y The Y coordinate of the screen.
     */
    public void setPosition(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
}
