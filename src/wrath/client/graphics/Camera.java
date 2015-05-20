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

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;
import wrath.client.ClientUtils;
import wrath.client.Game;
import wrath.common.entities.Player;

/**
 * Represents the Player Camera for a 2D or 3D game.
 * @author Trent Spears
 */
public class Camera
{
    private final FloatBuffer matrixBuf = BufferUtils.createFloatBuffer(16);
    private final Player player;
    private Vector3f position = new Vector3f(0,0,0);
    //x = pitch, y = yaw, z = roll
    private Vector3f orientation = new Vector3f(0,0,0);
    private boolean updateMat = true;
    
    /**
     * Constructor.
     * @param player The {@link wrath.common.entities.Player} to bind the camera to.
     */
    public Camera(Player player)
    {
        this.player = player;
    }
    
    /**
     * Gets the {@link wrath.common.entities.Player} bound to this Camera.
     * @return Returns the {@link wrath.common.entities.Player} bound to this Camera.
     */
    public Player getPlayer()
    {
        return player;
    }
    
    /**
     * Gets the Camera's World position.
     * @return Returns the camera's world position.
     */
    public Vector3f getPosition()
    {
        return position;
    }
    
    /**
     * Gets the 3-dimensional orientation of the Camera.
     * The X variable represents the Camera's Pitch.
     * The Y variable represents the Camera's Yaw.
     * The Z variable represents the Camera's Roll.
     * @return Returns the {@link org.lwjgl.util.vector.Vector3f} object representing the Camera's orientation.
     */
    public Vector3f getOrientation()
    {
        return orientation;
    }
    
    /**
     * Sets the Camera's Pitch, Yaw and Roll.
     * @param pitch The Camera's Pitch.
     * @param yaw The Camera's Yaw.
     * @param roll The Camera's Roll.
     */
    public void setOrientation(float pitch, float yaw, float roll)
    {
        updateMat = true;
        orientation.x = pitch;
        orientation.y = yaw;
        orientation.z = roll;
    }
    
    /**
     * Sets the 3-dimensional orientation of the Camera.
     * The X variable represents the Camera's Pitch.
     * The Y variable represents the Camera's Yaw.
     * The Z variable represents the Camera's Roll.
     * @param orientation The 3-dimensional orientation of the Camera.
     */
    public void setOrientation(Vector3f orientation)
    {
        updateMat = true;
        this.orientation = orientation;
    }
    
    /**
     * Sets the camera's position.
     * @param x The X-coordinate of the camera.
     * @param y The Y-coordinate of the camera.
     * @param z The Z-coordinate of the camera.
     */
    public void setPosition(float x, float y, float z)
    {
        updateMat = true;
        position.x = x;
        position.y = y;
        position.z = z;
    }
    
    /**
     * Sets the Camera's position.
     * @param position The {@link org.lwjgl.util.vector.Vector3f} object representing the Camera's orientation.
     */
    public void setPosition(Vector3f position)
    {
        updateMat = true;
        this.position = position;
    }
    
    /**
     * Increases all of the orientation's values by the set amount.
     * @param dp The amount to increase the pitch by.
     * @param dy The amount to increase the yaw by.
     * @param dr The amount to increase the roll by.
     */
    public void transformOrientation(float dp, float dy, float dr)
    {
        updateMat = true;
        orientation.x += dp;
        orientation.y += dy;
        orientation.z += dr;
    }
    
    /**
     * Increments the position of the camera by the specified amount.
     * @param dx The amount to increase the position on the X-Axis.
     * @param dy The amount to increase the position on the Y-Axis.
     * @param dz The amount to increase the position on the Z-Axis.
     */
    public void transformPosition(float dx, float dy, float dz)
    {
        updateMat = true;
        position.x += dx;
        position.y += dy;
        position.z += dz;
    }
    
    /**
     * Is called automatically, this method is used to update the specified shader's View Matrix, if need be.
     * @param shader The {@link wrath.client.graphics.ShaderProgram} to update.
     */
    public void updateViewMatrix(ShaderProgram shader)
    {
        if(updateMat == true)
        {
            ClientUtils.createViewMatrix(Game.getCurrentInstance().getPlayerCamera()).store(matrixBuf);
            matrixBuf.flip();
            updateMat = false;
        }
        GL20.glUniformMatrix4(shader.getUniformVariableLocation("viewMatrix"), false, matrixBuf);
        GL20.glUseProgram(0);
    }
}
