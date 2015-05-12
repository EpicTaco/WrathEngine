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
import java.nio.FloatBuffer;
import java.util.HashMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Class to represent a 3D Model.
 * @author Trent Spears
 */
public class Model
{
    private static final HashMap<String, Model> map = new HashMap<>();
    
    public static void clearModels()
    {
        map.values().stream().map((model) -> 
        {
            GL30.glDeleteVertexArrays(model.getPositionsVaoID());
            return model;
        }).forEach((model) -> 
        {
            GL15.glDeleteBuffers(model.getVboID());
        });
    }
    
    public static Model createModel(String modelName, float[] verticies)
    {
        int vaoid = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoid);
        int vboid = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboid);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(verticies.length);
        buffer.put(verticies);
        buffer.flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        
        Model model = new Model(modelName, null, vaoid, vboid, verticies.length/3);
        map.put(modelName, model);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        return model;
    }
    
    public static Model loadModel(File modelFile)
    {
        return loadModel(modelFile.getName(), modelFile);
    }
    
    public static Model loadModel(String modelName, File modelFile)
    {
        //This data would be read from the file.
        float[] data = new float[0];
        
        int vaoid = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoid);
        int vboid = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboid);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        
        Model model = new Model(modelName, modelFile, vaoid, vboid, data.length/3);
        map.put(modelName, model);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        return model;
    }
 
    private final int vao;
    private final int vbo;
    private final int vertexCount;
    
    private Model(String modelName, File modelFile, int vao, int vbo, int vertexCount)
    {
        this.vao = vao;
        this.vbo = vbo;
        this.vertexCount = vertexCount;
    }
    
    public int getPositionsVaoID()
    {
        return vao;
    }
    
    public int getVboID()
    {
        return vbo;
    }
    
    public int getVertexCount()
    {
        return vertexCount;
    }
}
