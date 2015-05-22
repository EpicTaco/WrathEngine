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
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import wrath.client.Game;
import wrath.common.Closeable;
import wrath.util.Logger;

/**
 * Class to represent a Model (both 2D and 3D).
 * @author Trent Spears
 */
public class Model implements Renderable, Closeable
{
    private static final int NORMALS_ATTRIB_INDEX = 2;
    private static final int TEXTURE_ATTRIB_INDEX = 1;
    private static final int VERTICIES_ATTRIB_INDEX = 0;
    
    /**
     * Creates a 2D or 3D model from a list of verticies.
     * Models are always assumed to be made with triangles, and will be rendered as such.
     * @param verticies The list of verticies in the model. One point is represented by (x, y, z), and there must be at least 3 points.
     * @param indicies The list of points to connect for OpenGL. Look up indicies in OpenGL for reference.
     * @param normals The list of 3 float vectors describing the normal vector of the model's surface.
     * @return Returns the {@link wrath.client.graphics.Model} object of your model.
     */
    public static Model createModel(float[] verticies, int[] indicies, float[] normals)
    {
        return createModel(verticies, indicies, normals, true);
    }
    
    /**
     * Creates a 2D or 3D model from a list of verticies.
     * Models are always assumed to be made with triangles, and will be rendered as such.
     * @param verticies The list of verticies in the model. One point is represented by (x, y, z), and there must be at least 3 points.
     * @param indicies The list of points to connect for OpenGL. Look up indicies in OpenGL for reference.
     * @param normals The list of 3 float vectors describing the normal vector of the model's surface.
     * @param useDefaultShaders If true, shaders will be set up automatically.
     * @return Returns the {@link wrath.client.graphics.Model} object of your model.
     */
    public static Model createModel(float[] verticies, int[] indicies, float[] normals, boolean useDefaultShaders)
    {
        // Generating VAO
        int vaoid = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoid);
        
        // Generating Verticies VBO
        int vtvboid = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vtvboid);
        FloatBuffer vbuffer = BufferUtils.createFloatBuffer(verticies.length);
        vbuffer.put(verticies);
        vbuffer.flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vbuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(VERTICIES_ATTRIB_INDEX, 3, GL11.GL_FLOAT, false, 0, 0);
        
        // Generating Normals VBO
        int nmvboid = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, nmvboid);
        FloatBuffer nbuffer = BufferUtils.createFloatBuffer(normals.length);
        nbuffer.put(normals);
        nbuffer.flip();
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, nbuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(NORMALS_ATTRIB_INDEX, 3, GL11.GL_FLOAT, false, 0, 0);
        
        // Generating Indicies VBO
        int invboid = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, invboid);
        IntBuffer ibuffer = BufferUtils.createIntBuffer(indicies.length);
        ibuffer.put(indicies);
        ibuffer.flip();
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, ibuffer, GL15.GL_STATIC_DRAW);
        
        // Creating Model Object
        Model model = new Model(null, vaoid, new Integer[]{vtvboid, invboid, nmvboid}, indicies.length);
        Game.getCurrentInstance().getLogger().log("Loaded model from verticies map!");
        if(useDefaultShaders) model.attachShader(ShaderProgram.DEFAULT_SHADER);
        
        // Unbinding OpenGL Objects
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        Game.getCurrentInstance().addToTrashCleanup(model);
        return model;
    }
    
    /**
     * Loads a 2D or 3D model from specified {@link java.io.File}.
     * Models are always assumed to be made with triangles, and will be rendered as such.
     * @param modelFile The .OBJ {@link java.io.File} to read the model data from.
     * @return Returns the {@link wrath.client.graphics.Model} object of your model.
     */
    public static Model loadModel(File modelFile)
    {
        return loadModel(modelFile, true);
    }
    
    /**
     * Loads a 2D or 3D model from specified {@link java.io.File}.
     * Models are always assumed to be made with triangles, and will be rendered as such.
     * @param modelFile The .OBJ {@link java.io.File} to read the model data from.
     * @param useDefaultShaders If true, shaders will be set up automatically.
     * @return Returns the {@link wrath.client.graphics.Model} object of your model.
     */
    public static Model loadModel(File modelFile, boolean useDefaultShaders)
    {
        ArrayList<Vector3f> verticies = new ArrayList<>();
        ArrayList<Vector2f> texCoords = new ArrayList<>();
        ArrayList<Vector3f> normals = new ArrayList<>();
        ArrayList<Integer> indicies = new ArrayList<>();
        float[] varray = null;
        float[] narray = null;
        float[] tarray = null;
        
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(modelFile));
            String inp;
            boolean tmp = true;
            while((inp = in.readLine()) != null)
            {
                String[] buf = inp.split(" ");
                if(inp.startsWith("v ")) verticies.add(new Vector3f(Float.parseFloat(buf[1]), Float.parseFloat(buf[2]), Float.parseFloat(buf[3])));
                else if(inp.startsWith("vt ")) texCoords.add(new Vector2f(Float.parseFloat(buf[1]), Float.parseFloat(buf[2])));
                else if(inp.startsWith("vn ")) normals.add(new Vector3f(Float.parseFloat(buf[1]), Float.parseFloat(buf[2]), Float.parseFloat(buf[3])));
                else if(inp.startsWith("f "))
                {
                    if(tmp)
                    {
                        varray = new float[verticies.size() * 3];
                        narray = new float[verticies.size() * 3];
                        tarray = new float[verticies.size() * 2];
                        tmp = false;
                    }
                    
                    for(int x = 1; x <= 3; x++)
                    {
                        String[] curDat;
                        if(buf[x].contains("//"))
                        {
                            curDat = buf[x].split("//");
                            
                            int ptr = Integer.parseInt(curDat[0]) - 1;
                            indicies.add(ptr);
                            Vector3f norm = normals.get(Integer.parseInt(curDat[1]) - 1);
                            narray[ptr*3] = norm.x;
                            narray[ptr*3 + 1] = norm.y;
                            narray[ptr*3 + 2] = norm.z;
                        }
                        else
                        {   
                            curDat = buf[x].split("/");
                        
                            int ptr = Integer.parseInt(curDat[0]) - 1;
                            indicies.add(ptr);
                            Vector2f tex = texCoords.get(Integer.parseInt(curDat[1]) - 1);
                            tarray[ptr*2] = tex.x;
                            tarray[ptr*2 + 1] = 1 - tex.y;
                            Vector3f norm = normals.get(Integer.parseInt(curDat[2]) - 1);
                            narray[ptr*3] = norm.x;
                            narray[ptr*3 + 1] = norm.y;
                            narray[ptr*3 + 2] = norm.z;
                        }
                    }
                }
            }
            in.close();
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not load model from file '" + modelFile.getName() + "'! I/O Error!");
            return null;
        }
        
        int i = 0;
        for(Vector3f ve : verticies)
        {
            varray[i] = ve.x;
            varray[i + 1] = ve.y;
            varray[i + 2] = ve.z;
            i += 3;
        }
        int[] iarray = new int[indicies.size()];
        for(int z = 0; z < indicies.size(); z++)
            iarray[z] = indicies.get(z);
        
        Model m = createModel(varray, iarray, narray, useDefaultShaders);
        m.textureCoords = tarray;
        Game.getCurrentInstance().getLogger().log("Loaded Model from file '" + modelFile.getName() + "' with " + verticies.size() + " verticies, " + normals.size() + " normals, " + indicies.size() + " indicies and " + texCoords.size() + " texture coordinates.");
        return m;
    }
 
    private final File modelFile;
    private ShaderProgram shader = null;
    private Texture texture = null;
    private float[] textureCoords = null;
    private final int vao;
    private final ArrayList<Integer> vbos = new ArrayList<>();
    private final int vertexCount;
    
    private Model(File modelFile, int vao, Integer[] initVbos, int vertexCount)
    {
        this.modelFile = modelFile;
        this.vao = vao;
        vbos.addAll(Arrays.asList(initVbos));
        this.vertexCount = vertexCount;
    }
    
    /**
     * Applies a {@link wrath.client.graphics.ShaderProgram} to the model to be called every time the model is rendered.
     * Only one can be attached at a time.
     * @param shader The {@link wrath.client.graphics.ShaderProgram} to associate with this model.
     */
    public void attachShader(ShaderProgram shader)
    {
        shader.bindAttribute(VERTICIES_ATTRIB_INDEX, "in_Position");
        shader.bindAttribute(NORMALS_ATTRIB_INDEX, "in_Normals");
        if(texture != null) shader.bindAttribute(TEXTURE_ATTRIB_INDEX, "in_TextureCoord");
        this.shader = shader;
    }
    
    /**
     * Applies a {@link wrath.client.graphics.Texture} to the model to be rendered on top of the Model.
     * Only one can be attached at a time.
     * @param texture The {@link wrath.client.graphics.Texture} to associate with this model.
     */
    public void attachTexture(Texture texture)
    {
        attachTexture(texture, new float[0]);
    }
    
    /**
     * Applies a {@link wrath.client.graphics.Texture} to the model to be rendered on top of the Model.
     * Only one can be attached at a time.
     * @param texture The {@link wrath.client.graphics.Texture} to associate with this model.
     * @param textureCoords The (u, v) coordinates of the texture to the model.
     */
    public void attachTexture(Texture texture, float[] textureCoords)
    {
        this.texture = texture;
        if(this.textureCoords != null) textureCoords = this.textureCoords;
        if(shader == null) Game.getCurrentInstance().getLogger().log("Warning: If no shader is present to pass texture co-ordinates, then the texture will not render!");
        GL30.glBindVertexArray(vao);
        int vboid = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboid);
        FloatBuffer vbuffer = BufferUtils.createFloatBuffer(textureCoords.length);
        vbuffer.put(textureCoords);
        vbuffer.flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vbuffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(TEXTURE_ATTRIB_INDEX, 2, GL11.GL_FLOAT, false, 0, 0);
        
        if(shader != null) shader.bindAttribute(TEXTURE_ATTRIB_INDEX, "in_TextureCoord");
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        vbos.add(vboid);
    }
    
    @Override
    public void close()
    {
        GL30.glDeleteVertexArrays(getVaoID());
        for(Integer i : getVboList())
                GL15.glDeleteBuffers(i);
        Game.getCurrentInstance().removeFromTrashCleanup(this);
    }
    
    /**
     * Gets the {@link wrath.client.graphics.ShaderProgram} attached to this model. 
     * @return Returns the {@link wrath.client.graphics.ShaderProgram} attached to this model.
     */
    public ShaderProgram getShader()
    {
        return shader;
    }
    
    /**
     * Gets the {@link wrath.client.graphics.Texture} attached to this model. 
     * @return Returns the {@link wrath.client.graphics.Texture} attached to this model.
     */
    public Texture getTexture()
    {
        return texture;
    }
    
    /**
     * Gets the OpenGL ID of the model's Vertex Array Object.
     * @return Returns the OpenGL ID of the model's Vertex Array Object.
     */
    public int getVaoID()
    {
        return vao;
    }
    
    /**
     * Gets the OpenGL IDs of the model's Vertex Buffer Objects.
     * @return Returns the OpenGL IDs of the model's Vertex Buffer Objects.
     */
    public Integer[] getVboList()
    {
        Integer[] ret = new Integer[vbos.size()];
        vbos.toArray(ret);
        return ret;
    }
    
    /**
     * Gets the amount of verticies in the model.
     * @return Returns the number of verticies in the model.
     */
    public int getVertexCount()
    {
        return vertexCount;
    }
   
    @Override
    public void render()
    {
        if(!shader.isFinalized()) shader.finish();
        
        GL30.glBindVertexArray(vao);
        GL20.glEnableVertexAttribArray(VERTICIES_ATTRIB_INDEX);
        GL20.glEnableVertexAttribArray(NORMALS_ATTRIB_INDEX);
        if(texture != null)
        {
            GL20.glEnableVertexAttribArray(TEXTURE_ATTRIB_INDEX);
            texture.bindTexture();
        }
        if(shader != null)
        {
            shader.updateViewMatrix();
            GL20.glUseProgram(shader.getProgramID());
        }
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        
        GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0);
        
        GL20.glUseProgram(0);
        if(texture != null) GL20.glDisableVertexAttribArray(TEXTURE_ATTRIB_INDEX);
        Texture.unbindTextures();
        GL20.glDisableVertexAttribArray(VERTICIES_ATTRIB_INDEX);
        GL20.glDisableVertexAttribArray(NORMALS_ATTRIB_INDEX);
        GL30.glBindVertexArray(0);
    }

}
