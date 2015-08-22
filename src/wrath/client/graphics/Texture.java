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
import org.lwjgl.opengl.GL30;
import wrath.client.ClientUtils;
import wrath.client.Game;
import wrath.common.Closeable;
import wrath.common.Reloadable;

/**
 * Class to describe and load Textures (both 2D and 3D).
 * @author Trent Spears
 */
public class Texture implements Closeable, Reloadable
{
    private static final HashMap<File, Texture> preLoadedTex = new HashMap<>();
    
    /**
     * Loads a Texture object.
     * @param textureFile The image {@link java.io.File} to load the texture from.
     * @return Returns the loaded {@link wrath.client.graphics.Texture} object.
     */
    public static Texture loadTexture(File textureFile)
    {
        if(preLoadedTex.containsKey(textureFile)) return preLoadedTex.get(textureFile);
        else
        {
            Texture t = new Texture(textureFile);
            preLoadedTex.put(textureFile, t);
            return t;
        }
    }
    
    // Object
    
    private final File file;
    private int texID;
    
    /**
     * Constructor.
     * @param textureFile The image {@link java.io.File} to load the texture from.
     */
    protected Texture(File textureFile)
    {
        this.file = textureFile;
        this.texID = ClientUtils.getTexture(ClientUtils.loadImageFromFile(textureFile));
        Game.getCurrentInstance().getLogger().log("Created texture ID '" + texID + "' from file '" + file.getName() + "'!");
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
        if(Game.getCurrentInstance().getConfig().getBoolean("TexureMipmapping", true)) GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        if(Game.getCurrentInstance().getConfig().getBoolean("AntiAliasingTexture", true))
        {
            if(Game.getCurrentInstance().getConfig().getBoolean("TexureMipmapping", true))
            {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            }
            else
            {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            }
            
        }
        Texture.unbindTextures();
        afterConstructor();
    }
    
    private void afterConstructor()
    {
        Game.getCurrentInstance().addToTrashCleanup(this);
        Game.getCurrentInstance().addToRefreshList(this);
    }
    
    /**
     * Sets the OpenGL Texture state to point to this texture.
     * This should not be called by the developer as it is done automatically.
     */
    public void bindTexture()
    {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
    }
    
    /**
     * Don't run this method, instead use {@link #destroyTexture()}!
     */
    @Override
    public void close()
    {
        GL11.glDeleteTextures(texID);
    }
    
    /**
     * Permanently removes the Texture from the game assets.
     */
    public void destroyTexture()
    {
        unbindTextures();
        close();
        Game.getCurrentInstance().removeFromTrashCleanup(this);
        Game.getCurrentInstance().removeFromRefreshList(this);
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
    
    @Override
    public void reload()
    {
        this.texID = ClientUtils.getTexture(ClientUtils.loadImageFromFile(file));
        Game.getCurrentInstance().getLogger().log("Created texture ID '" + texID + "' from file '" + file.getName() + "'!");
    }
    
    /**
     * Toggles Anti-Aliasing filters for this Texture.
     * @param enabled If true, enables Anti-Aliasing.
     */
    public void setAntiAliasing(boolean enabled)
    {
        bindTexture();
        if(enabled)
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        }
        else
        {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, 0);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, 0);
        }
        unbindTexture();
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
