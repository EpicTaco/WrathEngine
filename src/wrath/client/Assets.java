/**
 * Wrath Engine 
 * Copyright (C) 2015 Trent Spears
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package wrath.client;

import java.io.File;
import wrath.client.graphics.Model;
import wrath.client.graphics.ShaderProgram;
import wrath.client.graphics.Texture;
import wrath.common.world.World;
import wrath.common.world.WorldType;

/**
 * Optional class to load assets.
 * This class assumes that all assets are stored PROPERLY in the 'assets' and 'etc' folders.
 * For examples, all models will be searched for in the 'assets/models' folder and all worlds will be searched for in the 'etc/worlds' folder.
 * @author Trent Spears
 */
public class Assets 
{
    /**
     * Constructor.
     */
    private Assets(){}
    
    /**
     * Loads a 2D or 3D model from specified name using the Entity shader.
     * Models are always assumed to be made with triangles, and will be rendered as such.
     * @param modelName The name of the model to be loaded. This should be the entire file name, e.g. 'model.obj'.
     * @return Returns the {@link wrath.client.graphics.Model} object of your model.
     */
    public static Model loadEntityModel(String modelName)
    {
        return Model.loadModel(modelName);
    }
    
    /**
     * Loads a 2D or 3D model from specified name using the Terrain shader.
     * Models are always assumed to be made with triangles, and will be rendered as such.
     * @param modelName The name of the model to be loaded. This should be the entire file name, e.g. 'model.obj'.
     * @return Returns the {@link wrath.client.graphics.Model} object of your model.
     */
    public static Model loadTerrainModel(String modelName)
    {
        Model ret = Model.loadModel(modelName, false);
        ret.attachShader(ShaderProgram.DEFAULT_TERRAIN_SHADER);
        return ret;
    }
    
    /**
     * Loads a Texture object.
     * @param textureName The name of the texture. This includes the file extension.
     * @return Returns the loaded {@link wrath.client.graphics.Texture} object.
     */
    public static Texture loadTexture(String textureName)
    {
        return Texture.loadTexture(new File("assets/textures/" + textureName));
    }
    
    /**
     * Reads and returns all World data from the specified World.
     * Returns null if corrupt/invalid.
     * @param worldName The name of the world.
     * @param type The type of World to generate if one is not loaded from a file. This can be null if you know the World already exists.
     * @return Returns {@link wrath.common.world.World} loaded from a file (if present), null if invalid or corrupt.
     */
    public static World loadWorld(String worldName, WorldType type)
    {
        return World.loadWorld(worldName, type);
    }
}
