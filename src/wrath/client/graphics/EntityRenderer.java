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

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import wrath.client.ClientUtils;
import wrath.common.entities.Entity;

/**
 * Class exclusive to the client to describe Render-able entities.
 * @author Trent Spears
 */
public class EntityRenderer implements Renderable
{
    private final Entity entity;
    private Matrix4f mat;
    private Model model;
    private final Vector3f rotation;
    private float scale = 1f;
    private final Vector3f screenPosition = new Vector3f(0f,0f,0f);
    private boolean updateMat = true;
    
    private boolean tmpBool = true;
    
    /**
     * Constructor.
     * @param entity The {@link wrath.common.entities.Entity} to be rendered.
     */
    public EntityRenderer(Entity entity)
    {
        this.entity = entity;
        this.rotation = new Vector3f(0f,0f,0f);
    }
    
    /**
     * Links a {@link wrath.client.graphics.Model} to the described entity.
     * @param model The {@link wrath.client.graphics.Model} to link with the entity.
     */
    public void bindModel(Model model)
    {
        this.model = model;
    }
    
    /**
     * Gets the {@link wrath.common.entities.Entity} linked to this renderer.
     * @return Returns the {@link wrath.common.entities.Entity} linked to this renderer.
     */
    public Entity getEntity()
    {
        return entity;
    }
    
    /**
     * Gets the linked {@link wrath.client.graphics.Model}.
     * @return Returns the linked {@link wrath.client.graphics.Model}.
     */
    public Model getModel()
    {
        return model;
    }
    
    /**
     * Gets the 3-dimensional position of the model to the screen.
     * @return Returns a {@link org.lwjgl.util.vector.Vector3f} object representing the 3-dimensional position of the model to the screen.
     */
    public Vector3f getScreenPosition()
    {
        return screenPosition;
    }
    
    @Override
    public void render()
    {
        updateTransformationMatrix();
        model.render();
    }
    
    /**
     * Changes the rotation of the model.
     * @param rx The X-axis rotation (in radians).
     * @param ry The Y-axis rotation (in radians).
     * @param rz The Z-axis rotation (in radians).
     */
    public void setRotation(float rx, float ry, float rz)
    {
        updateMat = true;
        this.rotation.x = rx;
        this.rotation.y = ry;
        this.rotation.z = rz;
    }
    
    /**
     * Sets the scale of the model.
     * @param scale The scale multiplier of the model, 1.0f being the model's natural size.
     */
    public void setScale(float scale)
    {
        updateMat = true;
        this.scale = scale;
    }
    
    /**
     * Sets the model's position on the screen.
     * @param x The X-coordinate of the model on the screen.
     * @param y The Y-coordinate of the model on the screen.
     * @param z The Z-coordinate of the model on the screen.
     */
    public void setScreenPosition(float x, float y, float z)
    {
        updateMat = true;
        screenPosition.x = x;
        screenPosition.y = y;
        screenPosition.z = z;
    }
    
    /**
     * Increments the rotation by the specified amount.
     * @param drx The amount to increase the rotation on the X-Axis.
     * @param dry The amount to increase the rotation on the Y-Axis.
     * @param drz The amount to increase the rotation on the Z-Axis.
     */
    public void transformRotation(float drx, float dry, float drz)
    {
        updateMat = true;
        rotation.x += drx;
        rotation.y += dry;
        rotation.z += drz;
    }
    
    /**
     * Increments the position of the model by the specified amount.
     * @param dx The amount to increase the position on the X-Axis.
     * @param dy The amount to increase the position on the Y-Axis.
     * @param dz The amount to increase the position on the Z-Axis.
     */
    public void transformScreenPosition(float dx, float dy, float dz)
    {
        updateMat = true;
        screenPosition.x += dx;
        screenPosition.y += dy;
        screenPosition.z += dz;
    }
    
    /**
     * Changes the shader's Matrix to fit the current settings.
     */
    public void updateTransformationMatrix()
    {
        if(model.getShader() != null)
        {
            if(tmpBool)
            {
                updateMat = true;
                tmpBool = false;
            }
            
            if(updateMat)
            {
                mat = ClientUtils.createTransformationMatrix(screenPosition, rotation.x, rotation.y, rotation.z, scale);
                updateMat = false;
            }
            model.getShader().setTransformationMatrix(mat);
        }
        
    }
    
    /**
     * Renders an entity without creating an EntityRenderer object.
     * @param entity The {@link wrath.common.entities.Entity} that is attached to the {@link wrath.client.graphics.Model}.
     * @param model The {@link wrath.client.graphics.Model} to render.
     * @param transformationMatrix A {!link org.lwjgl.util.vector.Matrix4f} containing positional data. Should be created with {@link wrath.client.ClientUtils#createTransformationMatrix(org.lwjgl.util.vector.Vector3f, float, float, float, float)}.
     */
    public static void renderEntity(Entity entity, Model model, Matrix4f transformationMatrix)
    {
        if(model.getShader() != null) model.getShader().setTransformationMatrix(transformationMatrix);
        model.render();
    }
}
