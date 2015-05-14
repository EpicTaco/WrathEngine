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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import wrath.client.InstanceRegistry;
import wrath.common.Closeable;
import wrath.common.Matrix4f;
import wrath.common.Vector3f;
import wrath.util.Logger;

/**
 * Class used to describe and load shader programs.
 * @author Trent Spears
 */
public class ShaderProgram implements Closeable
{
    /**
     * Reads the two specified shader files and compiles the shaders into an OpenGL program format.
     * It is recommended that shaders be stored in the 'assets/shaders' directory (which is not present by default).
     * @param vertFile The {@link java.io.File} to read the vert shader from.
     * @param fragFile The {@link java.io.File} to read the frag shader from.
     * @return Returns the ShaderProgram object.
     */
    public static ShaderProgram loadShaderProgram(File vertFile, File fragFile)
    {
        if(!vertFile.exists())
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + vertFile.getAbsolutePath() + "'! File not found!");
            return null;
        }
        
        if(!fragFile.exists())
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + fragFile.getAbsolutePath() + "'! File not found!");
            return null;
        }
        
        String vsrc = "";
        
        try
        {
            String inp;
            try(BufferedReader read = new BufferedReader(new FileReader(vertFile))) 
            {
                while((inp = read.readLine()) != null) vsrc = vsrc + inp + '\n';
            }
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + vertFile.getAbsolutePath() + "'! I/O Error!");
            return null;
        }
        
        String fsrc = "";
        
        try
        {
            String inp;
            try(BufferedReader read = new BufferedReader(new FileReader(fragFile))) 
            {
                while((inp = read.readLine()) != null) fsrc = fsrc + inp + '\n';
            }
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + fragFile.getAbsolutePath() + "'! I/O Error!");
            return null;
        }
        
        int prog = GL20.glCreateProgram();
        int vert = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        int frag = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        
        GL20.glShaderSource(vert, vsrc);
        GL20.glShaderSource(frag, fsrc);
        
        GL20.glCompileShader(vert);
        if(GL20.glGetShaderi(vert, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + vertFile.getAbsolutePath() + "'! Compile Error:");
            Logger.getErrorLogger().log(GL20.glGetShaderInfoLog(vert));
            return null;
        }
        
        GL20.glCompileShader(frag);
        if(GL20.glGetShaderi(frag, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
        {
            Logger.getErrorLogger().log("Could not load shader from file '" + fragFile.getAbsolutePath() + "'! Compile Error:");
            Logger.getErrorLogger().log(GL20.glGetShaderInfoLog(frag));
            return null;
        }
        
        GL20.glAttachShader(prog, vert);
        GL20.glAttachShader(prog, frag);
        
        ShaderProgram ret = new ShaderProgram(prog, vert, frag);
        InstanceRegistry.getGameInstance().addToTrashCleanup(ret);
        //TODO: Get all uniform variables locations.
        return ret;
    }
    
    private boolean finalized = false;
    private final int programID, vertShaderID, fragShaderID;
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    
    private ShaderProgram(int programID, int vertShaderID, int fragShaderID)
    {
        this.programID = programID;
        this.vertShaderID = vertShaderID;
        this.fragShaderID = fragShaderID;
    }
    
    /**
     * Binds an OpenGL attribute to the shader program.
     * This all must be done before the shader program is run.
     * @param attribute The OpenGL attribute ID.
     * @param variable The {@link java.lang.String} form of the attribute variable name.
     */
    public void bindAttribute(int attribute, String variable)
    {
        if(finalized) return;
        GL20.glBindAttribLocation(programID, attribute, variable);
    }
    
    @Override
    public void close()
    {
        stopUse();
        GL20.glDetachShader(programID, vertShaderID);
        GL20.glDetachShader(programID, fragShaderID);
        GL20.glDeleteShader(vertShaderID);
        GL20.glDeleteShader(fragShaderID);
        GL20.glDeleteProgram(programID);
        InstanceRegistry.getGameInstance().removeFromTrashCleanup(this);
    }
    
    /**
     * Gets the integer location of a uniform variable.
     * @param variableName The {@link java.lang.String} name of the Uniform variable.
     * @return Returns the integer location of a uniform variable.
     */
    public int getUniformVariableLocation(String variableName)
    {
        return GL20.glGetUniformLocation(programID, variableName);
    }
    
    /**
     * Sets the value of a uniform variable in the shader.
     * @param variableName The {@link java.lang.String} name of the Uniform variable.
     * @param value The value to set.
     */
    public void setUniformVariable(String variableName, float value)
    {
        setUniformVariable(getUniformVariableLocation(variableName), value);
    }
    
    /**
     * Sets the value of a uniform variable in the shader.
     * @param variableLocation The integer location of the Uniform variable.
     * @param value The value to set.
     */
    public void setUniformVariable(int variableLocation, float value)
    {
        GL20.glUniform1f(variableLocation, value);
    }
    
    /**
     * Sets the value of a uniform variable in the shader.
     * @param variableName The {@link java.lang.String} name of the Uniform variable.
     * @param value The value to set.
     */
    public void setUniformVariable(String variableName, Vector3f value)
    {
        setUniformVariable(getUniformVariableLocation(variableName), value);
    }
    
    /**
     * Sets the value of a uniform variable in the shader.
     * @param variableLocation The integer location of the Uniform variable.
     * @param value The value to set.
     */
    public void setUniformVariable(int variableLocation, Vector3f value)
    {
        GL20.glUniform3f(variableLocation, value.x, value.y, value.z);
    }
    
    /**
     * Sets the value of a uniform variable in the shader.
     * @param variableName The {@link java.lang.String} name of the Uniform variable.
     * @param value The value to set.
     */
    public void setUniformVariable(String variableName, boolean value)
    {
        setUniformVariable(getUniformVariableLocation(variableName), value);
    }
    
    /**
     * Sets the value of a uniform variable in the shader.
     * @param variableLocation The integer location of the Uniform variable.
     * @param value The value to set.
     */
    public void setUniformVariable(int variableLocation, boolean value)
    {
        GL20.glUniform1f(variableLocation, value ? 1f : 0f);
    }
    
    /**
     * Sets the value of a uniform variable in the shader.
     * @param variableName The {@link java.lang.String} name of the Uniform variable.
     * @param value The value to set.
     */
    public void setUniformVariable(String variableName, Matrix4f value)
    {
        setUniformVariable(getUniformVariableLocation(variableName), value);
    }
    
    /**
     * Sets the value of a uniform variable in the shader.
     * @param variableLocation The integer location of the Uniform variable.
     * @param value The value to set.
     */
    public void setUniformVariable(int variableLocation, Matrix4f value)
    {
        //TODO: Make Matrix4f class and put the Matrix into the FloatBuffer.
        GL20.glUniformMatrix4(variableLocation, false, matrixBuffer);
    }
    
    /**
     * Sets the shader program to the 'current' shader program.
     */
    public void startUse()
    {
        if(!finalized) finish();
        GL20.glUseProgram(programID);
    }
    
    /**
     * Clears the 'current' shader.
     */
    public void stopUse()
    {
        GL20.glUseProgram(0);
    }
    
    private void finish()
    {
        GL20.glLinkProgram(programID);
        GL20.glValidateProgram(programID);
        finalized = true;
    }
}
