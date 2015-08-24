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
package wrath.client.graphics;

import wrath.common.world.TerrainTile;
import wrath.common.world.WorldType;

/**
 * Class to render specific World tiles.
 * @author Trent Spears
 */
public class TileRenderer implements Renderable
{
    private final TerrainTile tile;
    private Model model;
    
    /**
     * Constructor.
     * @param tile The {@link wrath.common.world.TerrainTile} containing the vertex data for this renderer.
     */
    public TileRenderer(TerrainTile tile)
    {
        this.tile = tile;
        String modelName = "tile_" + tile.getTileLocation().x + "_" + tile.getTileLocation().y + "_" + tile.getTileLocation().z;
        if(tile.getWorld() != null && tile.getWorld().getWorldType() != WorldType.DYNAMIC) modelName = modelName + "__" + tile.getWorld().getName();
        //this.model = Model.createModel(modelName, verticies, indicies, normals, false);
        //this.model.attachShader(ShaderProgram.DEFAULT_TERRAIN_SHADER);
    }
    
    /**
     * Gets the {@link wrath.common.world.TerrainTile} containing the vertex data for this renderer.
     * @return Returns the {@link wrath.common.world.TerrainTile} containing the vertex data for this renderer.
     */
    public TerrainTile getTile()
    {
        return tile;
    }
    
    /**
     * Gets the {@link wrath.client.graphics.Model} associated with this renderer.
     * @return Returns the {@link wrath.client.graphics.Model} associated with this renderer.
     */
    public Model getTileModel()
    {
        return model;
    }
    
    @Override
    public void renderSetup()
    {
        update();
    }
    
    @Override
    public void render(boolean consolidated) 
    {
        if(consolidated) renderSetup();
        if(consolidated) renderStop();
    }
    
    @Override
    public void renderStop()
    {
        
    }
    
    /**
     * Updates the matricies necessary in the Shader program.
     */
    public void update()
    {
        
    }
}
