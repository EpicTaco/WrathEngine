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
import java.util.HashMap;
import org.lwjgl.opengl.GL11;
import wrath.client.ClientUtils;
import wrath.client.InstanceRegistry;
import wrath.common.Closeable;
import wrath.util.Logger;

/**
 * Class to load fonts and manage text rendering.
 * @author Trent Spears
 */
public class TextRenderer implements Closeable
{
    private static final HashMap<Character, Float> DEF_SPACE_MAP = new HashMap<>();
    
    private final HashMap<Character, Float> spaceMap = new HashMap<>();
    
    private Color color;
    private final File file;
    private float fontSize;
    private int fontTex;
    
    /**
     * Creates a renderer to render the specified PNG font {@link java.io.File}.
     * @param ttfFile The {@link java.io.File} containing the font to render.
     * @param fontSize The size of the font. This is NOT standardized!
     */
    public TextRenderer(File ttfFile, float fontSize)
    {
        if(DEF_SPACE_MAP.isEmpty()) setDefaultMetrics();
        this.file = ttfFile;
        color = new Color(1, 1, 1, 1);
        
        File metricsFile = new File(ttfFile.getParentFile().getPath() + "/" + ttfFile.getName().split(".png")[0] + ".metrics");
        if(!metricsFile.exists()) spaceMap.putAll(DEF_SPACE_MAP);
        else
        {
            //Fill ratio map.
        }
        
        this.fontSize = fontSize;
        fontTex = ClientUtils.get2DTexture(ClientUtils.loadImageFromFile(ttfFile));
        if(fontTex != 0)
            InstanceRegistry.getGameInstance().getLogger().log("Loaded font from '" + ttfFile.getName() + "'!");
        else
            Logger.getErrorLogger().log("Could not load font from '" + ttfFile.getName() + "'! Unknown error!");
        
        afterConstructor();
    }
    
    private void afterConstructor()
    {
        InstanceRegistry.getGameInstance().addToTrashCleanup(this);
    }
    
    @Override
    public void close()
    {
        GL11.glDeleteTextures(fontTex);
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
        spaceMap.clear();
        setDefaultMetrics();
        fontTex = ClientUtils.get2DTexture(ClientUtils.loadImageFromFile(file));
        File metricsFile = new File(file.getParentFile().getPath() + "/" + file.getName().split(".png")[0] + ".metrics");
        if(!metricsFile.exists()) spaceMap.putAll(DEF_SPACE_MAP);
        else
        {
            //Fill spacing map.
        }
        
        if(fontTex != 0)
            InstanceRegistry.getGameInstance().getLogger().log("Loaded font from '" + file.getName() + "'!");
        else
            Logger.getErrorLogger().log("Could not load font from '" + file.getName() + "'! Unknown error!");
    }
    
    /**
     * Draws the specified string onto the screen at the specified points.
     * @param string The {Link java.lang.String} to write to the screen.
     * @param x The X-coordinate of the top left of the message.
     * @param y The Y-coordinate of the top left of the message.
     */
    public void renderString(String string, float x, float y) 
    {
        renderString(string, x, y, fontSize, color);
    }
    
    /**
     * Draws the specified string onto the screen at the specified points independent of variables set in this object.
     * The only variable taken from the object in this method is the font itself.
     * @param string The {Link java.lang.String} to write to the screen.
     * @param x The X-coordinate of the top left of the message.
     * @param y The Y-coordinate of the top left of the message.
     * @param fontSize The size of the font. This is NOT standardized!
     * @param color The {@link wrath.client.graphics.Color} to make the rendered text.
     */
    public void renderString(String string, float x, float y, float fontSize, Color color) 
    {
        if(string.length() == 0) return;
        final int gridSize = 16;
        final float characterWidth = fontSize * 0.1f;
        final float characterHeight = characterWidth * 0.75f;
        
        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT | GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fontTex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        GL11.glPushMatrix();
        GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        GL11.glBegin(GL11.GL_QUADS);
        float curPos = x;
        float charWidth;
        for(int i = 0; i < string.length(); i++)
        {
            int ascii = (int) string.charAt(i);
            final float cellSize = 1.0f / gridSize;
            float cellX = ((int) ascii % gridSize) * cellSize;
            float cellY = ((int) ascii / gridSize) * cellSize;
            //(0, 1) bottom left
            GL11.glTexCoord2f(cellX, cellY + cellSize);
            GL11.glVertex3f(curPos, y - characterHeight, 0);
            
            //(1, 1) bottom right
            GL11.glTexCoord2f(cellX + cellSize, cellY + cellSize);
            GL11.glVertex3f(curPos + characterWidth / 2, y - characterHeight, 0);
            
            //(1, 0) top right 
            GL11.glTexCoord2f(cellX + cellSize, cellY);
            GL11.glVertex3f(curPos + characterWidth / 2, y, 0);
            
            //(0, 0) top left
            GL11.glTexCoord2f(cellX, cellY);
            GL11.glVertex3f(curPos, y, 0);

            if(spaceMap.containsKey(string.charAt(i))) charWidth = spaceMap.get(string.charAt(i));
            else charWidth = 1.0f;
            curPos = curPos + (charWidth * (fontSize / 48f));
            if((i + 1) < string.length() && Character.isLetterOrDigit(string.charAt(i + 1)))
            {
                if(spaceMap.containsKey(string.charAt(i + 1))) curPos = curPos + ((spaceMap.get(string.charAt(i + 1)) * (fontSize / 48f)) / 1.6f);
                else curPos = curPos + (fontSize / 48f) / 3.5f;
            }
        }
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glPopAttrib();
        
        GL11.glColor4f(1, 1, 1, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
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

    private static void setDefaultMetrics()
    {
        DEF_SPACE_MAP.clear();
        DEF_SPACE_MAP.put(' ', 0.95f);
        DEF_SPACE_MAP.put('A', 0.8f);
        DEF_SPACE_MAP.put('a', 0.6f);
        DEF_SPACE_MAP.put('B', 0.765f);
        DEF_SPACE_MAP.put('b', 0.7f);
        DEF_SPACE_MAP.put('C', 0.825f);
        DEF_SPACE_MAP.put('c', 0.6f);
        DEF_SPACE_MAP.put('D', 0.9f);
        DEF_SPACE_MAP.put('d', 0.7f);
        DEF_SPACE_MAP.put('E', 0.825f);
        DEF_SPACE_MAP.put('e', 0.6f);
        DEF_SPACE_MAP.put('F', 0.65f);
        DEF_SPACE_MAP.put('f', 0.45f);
        DEF_SPACE_MAP.put('G', 0.9f);
        DEF_SPACE_MAP.put('g', 0.75f);
        DEF_SPACE_MAP.put('H', 0.8f);
        DEF_SPACE_MAP.put('h', 0.55f);
        DEF_SPACE_MAP.put('I', 0.4f);
        DEF_SPACE_MAP.put('i', 0.3f);
        DEF_SPACE_MAP.put('J', 0.575f);
        DEF_SPACE_MAP.put('j', 0.5f);
        DEF_SPACE_MAP.put('K', 0.85f);
        DEF_SPACE_MAP.put('k', 0.6f);
        DEF_SPACE_MAP.put('L', 0.675f);
        DEF_SPACE_MAP.put('l', 0.35f);
        DEF_SPACE_MAP.put('M', 1.0f);
        DEF_SPACE_MAP.put('m', 0.9f);
        DEF_SPACE_MAP.put('N', 0.75f);
        DEF_SPACE_MAP.put('n', 0.5f);
        DEF_SPACE_MAP.put('O', 0.925f);
        DEF_SPACE_MAP.put('o', 0.55f);
        DEF_SPACE_MAP.put('P', 0.85f);
        DEF_SPACE_MAP.put('p', 0.65f);
        DEF_SPACE_MAP.put('Q', 0.9f);
        DEF_SPACE_MAP.put('q', 0.7f);
        DEF_SPACE_MAP.put('R', 0.7f);
        DEF_SPACE_MAP.put('r', 0.5f);
        DEF_SPACE_MAP.put('S', 0.65f);
        DEF_SPACE_MAP.put('s', 0.5f);
        DEF_SPACE_MAP.put('T', 0.7f);
        DEF_SPACE_MAP.put('t', 0.35f);
        DEF_SPACE_MAP.put('U', 0.78f);
        DEF_SPACE_MAP.put('u', 0.7f);
        DEF_SPACE_MAP.put('V', 0.95f);
        DEF_SPACE_MAP.put('v', 0.8f);
        DEF_SPACE_MAP.put('W', 1.05f);
        DEF_SPACE_MAP.put('w', 0.9f);
        DEF_SPACE_MAP.put('X', 0.95f);
        DEF_SPACE_MAP.put('x', 0.65f);
        DEF_SPACE_MAP.put('Y', 0.8f);
        DEF_SPACE_MAP.put('y', 0.8f);
        DEF_SPACE_MAP.put('Z', 0.9f);
        DEF_SPACE_MAP.put('z', 0.75f);
        
        DEF_SPACE_MAP.put('[', 0.6f);
        DEF_SPACE_MAP.put('%', 1.05f);
        DEF_SPACE_MAP.put('@', 1.1f);
        DEF_SPACE_MAP.put('.', 0.15f);
        DEF_SPACE_MAP.put(',', 0.15f);
        DEF_SPACE_MAP.put('0', 0.7f);
        DEF_SPACE_MAP.put('1', 0.7f);
        DEF_SPACE_MAP.put('2', 0.7f);
        DEF_SPACE_MAP.put('3', 0.7f);
        DEF_SPACE_MAP.put('4', 0.7f);
        DEF_SPACE_MAP.put('5', 0.7f);
        DEF_SPACE_MAP.put('6', 0.7f);
        DEF_SPACE_MAP.put('7', 0.7f);
        DEF_SPACE_MAP.put('8', 0.7f);
        DEF_SPACE_MAP.put('9', 0.7f);
        
    }
}
