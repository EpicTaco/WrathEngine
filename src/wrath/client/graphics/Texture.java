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
import org.lwjgl.opengl.GL11;
import wrath.client.ClientUtils;
import wrath.client.Game;
import wrath.client.enums.RenderMode;
import wrath.common.Closeable;

/**
 * Class to describe and load Textures (both 2D and 3D).
 * @author Trent Spears
 */
public class Texture implements Closeable
{  
    private final File file;
    private final int texID;
    
    /**
     * Constructor.
     * @param textureFile The image {@link java.io.File} to load the texture from.
     */
    public Texture(File textureFile)
    {
        this.file = textureFile;
        this.texID = ClientUtils.get2DTexture(ClientUtils.loadImageFromFile(textureFile));
        Game.getCurrentInstance().getLogger().log("Created texture ID '" + texID + "' from file '" + file.getName() + "'!");
        afterConstructor();
    }
    
    private void afterConstructor()
    {
        Game.getCurrentInstance().addToTrashCleanup(this);
    }
    
    /**
     * Sets the OpenGL Texture state to point to this texture.
     * This should not be called by the developer as it is done automatically.
     */
    public void bindTexture()
    {
        if(Game.getCurrentInstance().getRenderMode() == RenderMode.Mode2D) GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
        else GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
    }
    
    @Override
    public void close()
    {
        GL11.glDeleteTextures(texID);
        Game.getCurrentInstance().removeFromTrashCleanup(this);
    }
    
    /**
     * Gets the {@link java.io.File} that the texture was loaded from.
     * @return Returns the {@link java.io.File} that the texture was loaded from.
     */
    public File getTextureFile()
    {
        return file;
    }
    
    /**
     * Gets the OpenGL integer ID of the texture.
     * @return Returns the OpenGL integer ID of the texture.
     */
    public int getTextureID()
    {
        return texID;
    }
    
    /**
     * Sets the current OpenGL texture to point to nothing.
     * This should not be called by the developer as it is done automatically.
     */
    public void unbindTexture()
    {
        unbindTextures();
    }
    
    /**
     * Sets all current OpenGL textures to point to nothing.
     * This should not be called by the developer as it is done automatically.
     */
    public static void unbindTextures()
    {
        GL11.glBindTexture(GL11.GL_TEXTURE_1D, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
}
