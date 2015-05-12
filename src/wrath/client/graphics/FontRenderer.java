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

import java.io.File;
import wrath.client.ClientUtils;
import wrath.client.Game;
import wrath.util.Logger;
import static org.lwjgl.opengl.GL11.*;

/**
 * Class to load fonts and manage font rendering.
 * @author Trent Spears
 */
public class FontRenderer
{
    private Color color;
    private final File file;
    private float fontSize;
    private int fontTex;
    private final Game game;
    
    /**
     * Creates a renderer to render the specified PNG font {@link java.io.File}.
     * @param ttfFile The {@link java.io.File} containing the font to render.
     * @param fontSize The size of the font. This is NOT standardized!
     * @param game The {@link wrath.client.Game} object that parents this. This is needed to access the game's {@link wrath.util.Logger}.
     */
    public FontRenderer(File ttfFile, float fontSize, Game game)
    {
        this.file = ttfFile;
        color = new Color(1, 1, 1, 1);
        
        this.fontSize = fontSize;
        fontTex = ClientUtils.get2DTexture(ClientUtils.loadImageFromFile(ttfFile));
        this.game = game;
        if(fontTex != 0)
            game.getLogger().log("Loaded font from '" + ttfFile.getName() + "'!");
        else
            Logger.getErrorLogger().log("Could not load font from '" + ttfFile.getName() + "'! Unknown error!");
    }
    
    /**
     * Gets the current {@link wrath.client.graphics.Color} of the font.
     * @return Returns the current {@link wrath.client.graphics.Color} of the font.
     */
    public Color getColor()
    {
        return color;
    }
    
    /**
     * Gets the current Font size.
     * @return Returns the current Font size.
     */
    public float getFontSize()
    {
        return fontSize;
    }
    
    /**
     * If the window is to close and re-open, this will be called automatically to re-generate textures.
     */
    public void refreshRenderer()
    {
        fontTex = ClientUtils.get2DTexture(ClientUtils.loadImageFromFile(file));
        if(fontTex != 0)
            game.getLogger().log("Loaded font from '" + file.getName() + "'!");
        else
            Logger.getErrorLogger().log("Could not load font from '" + file.getName() + "'! Unknown error!");
    }
    
    /**
     * Draws the specified string onto the screen at the specified points.
     * @param string The {Link java.lang.String} to write to the screen.
     * @param x The X-coordinate of the bottom left of the message.
     * @param y The Y-coordinate of the bottom left of the message.
     */
    public void renderString(String string, float x, float y)
    {
        renderString(string, x, y, 6.8f);
    }
    
    /**
     * Draws the specified string onto the screen at the specified points.
     * @param string The {Link java.lang.String} to write to the screen.
     * @param x The X-coordinate of the top left of the message.
     * @param y The Y-coordinate of the top left of the message.
     * @param widthOffset The divisor to lower space between characters. The higher the offset, the closer the characters are to each other.
     */
    public void renderString(String string, float x, float y, float widthOffset) 
    {
        final int gridSize = 16;
        float characterWidth = fontSize * 0.01f;
        final float characterHeight = characterWidth * 0.75f;
        
        glPushAttrib(GL_TEXTURE_BIT | GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT);
        glEnable(GL_CULL_FACE);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, fontTex);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        glPushMatrix();
        glTranslatef(x, 0, 0);
        glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        glBegin(GL_QUADS);
        float curPos = 0f;
        for(int i = 0; i < string.length(); i++) 
        {
            int ascii = (int) string.charAt(i);
            final float cellSize = 1.0f / gridSize;
            float cellX = ((int) ascii % gridSize) * cellSize;
            float cellY = ((int) ascii / gridSize) * cellSize;
            //(0, 1) bottom left
            glTexCoord2f(cellX, cellY + cellSize);
            glVertex2f(curPos, y - characterHeight);
            
            //(1, 1) bottom right
            glTexCoord2f(cellX + cellSize, cellY + cellSize);
            glVertex2f(curPos + characterWidth / 2, y - characterHeight);
            
            //(1, 0) top right 
            glTexCoord2f(cellX + cellSize, cellY);
            glVertex2f(curPos + characterWidth / 2, y);
            
            //(0, 0) top left
            glTexCoord2f(cellX, cellY);
            glVertex2f(curPos, y);
            
            if(string.charAt(i) == 'l' || string.charAt(i) == 'i' || string.charAt(i) == '!' || string.charAt(i) == '|')
                curPos = (((i + 1) * characterWidth / widthOffset) + ((i) * characterWidth / widthOffset)) / 1.96f;
            else curPos = (i + 1) * characterWidth / widthOffset;
        }
        glEnd();
        glPopMatrix();
        glPopAttrib();
        
        glColor4f(1, 1, 1, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    
    /**
     * Sets the {@link wrath.client.graphics.Color} object describing the color of the text.
     * @param color The {@link wrath.client.graphics.Color} to make any rendered text.
     */
    public void setColor(Color color)
    {
        this.color = color;
    }
    
    /**
     * Sets the size of the font.
     * This does NOT use the 'pt' unit!
     * @param fontSize The size to set the font.
     */
    public void setFontSize(float fontSize)
    {
        this.fontSize = fontSize;
    }
}
