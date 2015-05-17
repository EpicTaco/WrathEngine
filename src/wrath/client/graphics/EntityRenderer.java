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
    private Model model;
    private float rx, ry, rz;
    private float scale = 1f;
    private final Vector3f screenPosition = new Vector3f(0f,0f,0f);
    
    /**
     * Constructor.
     * @param entity The {@link wrath.common.entities.Entity} to be rendered.
     */
    public EntityRenderer(Entity entity)
    {
        this.entity = entity;
        this.rx = 0f;
        this.ry = 0f;
        this.rz = 0f;
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
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }
    
    /**
     * Sets the scale of the model.
     * @param scale The scale multiplier of the model, 1.0f being the model's natural size.
     */
    public void setScale(float scale)
    {
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
        rx += drx;
        ry += dry;
        rz += drz;
    }
    
    /**
     * Increments the position of the model by the specified amount.
     * @param dx The amount to increase the position on the X-Axis.
     * @param dy The amount to increase the position on the Y-Axis.
     * @param dz The amount to increase the position on the Z-Axis.
     */
    public void transformScreenPosition(float dx, float dy, float dz)
    {
        screenPosition.x += dx;
        screenPosition.y += dy;
        screenPosition.z += dz;
    }
    
    /**
     * Changes the shader's Matrix to fit the current settings.
     */
    public void updateTransformationMatrix()
    {
        if(model.getShader() != null) model.getShader().setTransformationMatrix(ClientUtils.createTransformationMatrix(screenPosition, rx, ry, rz, scale));
    }
}
