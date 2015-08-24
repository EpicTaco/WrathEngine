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
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import wrath.client.ClientUtils;
import wrath.common.entities.Entity;
import wrath.common.entities.EntityDescriptor;
import wrath.util.Logger;

/**
 * Class exclusive to the client to describe Render-able entities.
 * @author Trent Spears
 */
public class EntityRenderer implements Renderable
{
    public static final HashMap<String,Model> preLoadedModels = new HashMap<>();
    
    private final Entity entity;
    private Light light = null;
    private Model model = null;
    private float reflectivity = 0f;
    private float shineDampening = 1f;
    
    private Matrix4f mat = new Matrix4f();
    private boolean updateMat = true;
    private boolean tmpBool = true;
    
    /**
     * Constructor.
     * @param entity The {@link wrath.common.entities.Entity} to be rendered.
     */
    public EntityRenderer(Entity entity)
    {
        this.entity = entity;
        
        if(entity.getEntityDescriptor() != null)
        {
            if(preLoadedModels.containsKey(entity.getEntityDescriptor().getModelName() + "," + entity.getEntityDescriptor().getTextureName()))
                this.bindModel(preLoadedModels.get(entity.getEntityDescriptor().getModelName() + "," + entity.getEntityDescriptor().getTextureName()));
            else
            {
                File modelFile = new File("assets/models/" + entity.getEntityDescriptor().getModelName());
                File texture = new File("assets/textures/" + entity.getEntityDescriptor().getTextureName());
                if(modelFile.exists() && texture.exists())
                {
                    Model m = Model.loadModel(entity.getEntityDescriptor().getModelName());
                    m.attachTexture(Texture.loadTexture(texture));
                    this.bindModel(m);
                    preLoadedModels.put(entity.getEntityDescriptor().getModelName() + "," + entity.getEntityDescriptor().getTextureName(), m);
                }
            }
            
            this.reflectivity = entity.getEntityDescriptor().getReflectivity();
            this.shineDampening = entity.getEntityDescriptor().getShineDampening();
        }
    }
    
    /**
     * Links a {@link wrath.client.graphics.Light} to the described entity.
     * @param light The {@link wrath.client.graphics.Light} to link with the entity.
     */
    public void bindLight(Light light)
    {
        this.light = light;
    }
    
    /**
     * Links a {@link wrath.client.graphics.Model} to the described entity.
     * If a valid {@link wrath.common.entities.EntityDescriptor} was provided to this Entity, this method should not be called as it is all handled automatically.
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
     * Gets the amount, in floating point number, the Entity is susceptible to having specular light reflect off of it.
     * @return Returns the amount, in floating point number, the Entity is susceptible to having specular light reflect off of it.
     */
    public float getReflectivity()
    {
        return reflectivity;
    }
    
    /**
     * Gets the distance at which the renderer's camera can no longer see reflected light.
     * @return Returns the distance at which the renderer's camera can no longer see reflected light.
     */
    public float getShineDampening()
    {
        return shineDampening;
    }
    
    @Override
    public void render(boolean consolidated)
    {
        if(consolidated) renderSetup();
        model.render(true);
    }
  
    @Override
    public void renderSetup()
    {
        update();
    }
    
    @Override
    public void renderStop(){}
    
    /**
     * Sets the values of variables used to calculate specular lighting.
     * @param reflectivity The amount, in floating point number, the Entity is susceptible to having specular light reflect off of it. Default 0.
     * @param shineDampening The distance at which the renderer's camera can no longer see reflected light. Default 1.
     */
    public void setSpecularLightingProperties(float reflectivity, float shineDampening)
    {
        this.reflectivity = reflectivity;
        this.shineDampening = shineDampening;
    }
    
    /**
     * Changes the model's shader settings to fit the current settings.
     */
    public void update()
    {
        if(model.getShader() != null)
        {
            if(tmpBool)
            {
                updateMat = true;
                tmpBool = false;
            }
            
            if(entity.changed() || updateMat)
            {
                mat = ClientUtils.createTransformationMatrix(entity.getLocation(), entity.getOrientation().x, entity.getOrientation().y, entity.getOrientation().z, entity.getSizeScale());
                entity.resetChangeTracker();
                updateMat = false;
            }
            model.getShader().setTransformationMatrix(mat);
            model.getShader().setUniformVariable(model.getShader().getUniformVariableLocation("reflectivity"), reflectivity);
            model.getShader().setUniformVariable(model.getShader().getUniformVariableLocation("shineDamper"), shineDampening);
            if(light != null)
            {
                model.getShader().setUniformVariable(model.getShader().getUniformVariableLocation("lightPosition"), light.getPosition());
                model.getShader().setUniformVariable(model.getShader().getUniformVariableLocation("lightColor"), new Vector3f(light.getColor().getRed(), light.getColor().getGreen(), light.getColor().getBlue()));
            }
        }
    }
    
    /**
     * Renders an entity without creating an EntityRenderer object.
     * @param entity The {@link wrath.common.entities.Entity} that is attached to the {@link wrath.client.graphics.Model}.
     * @param model The {@link wrath.client.graphics.Model} to render.
     * @param transformationMatrix A {@link org.lwjgl.util.vector.Matrix4f} containing positional data. Should be created with {@link wrath.client.ClientUtils#createTransformationMatrix(org.lwjgl.util.vector.Vector3f, float, float, float, float)}.
     */
    public static void renderEntity(Entity entity, Model model, Matrix4f transformationMatrix)
    {
        if(model.getShader() != null) model.getShader().setTransformationMatrix(transformationMatrix);
        model.render(true);
    }
    
    /**
     * Renders an entity without creating an EntityRenderer object.
     * @param entity The {@link wrath.common.entities.Entity} that is attached to the {@link wrath.client.graphics.Model}.
     * @param descriptor The {@link wrath.common.entities.EntityDescriptor} describing the assets to render.
     * @param transformationMatrix A {@link org.lwjgl.util.vector.Matrix4f} containing positional data. Should be created with {@link wrath.client.ClientUtils#createTransformationMatrix(org.lwjgl.util.vector.Vector3f, float, float, float, float)}.
     */
    public static void renderEntity(Entity entity, EntityDescriptor descriptor, Matrix4f transformationMatrix)
    {
        Model model;
        if(preLoadedModels.containsKey(entity.getEntityDescriptor().getModelName() + "," + entity.getEntityDescriptor().getTextureName()))
            model = preLoadedModels.get(entity.getEntityDescriptor().getModelName() + "," + entity.getEntityDescriptor().getTextureName());
        else
        {
            File modelF = new File("assets/models/" + entity.getEntityDescriptor().getModelName());
            File texture = new File("assets/textures/" + entity.getEntityDescriptor().getTextureName());
            if(modelF.exists() && texture.exists())
            {
                model = Model.loadModel(entity.getEntityDescriptor().getModelName());
                model.attachTexture(Texture.loadTexture(texture));
            }
            else
            {
                Logger.getErrorLogger().log("Could not render classless Entity with model '" + descriptor.getModelName() + "' and texture '" + descriptor.getTextureName() + "'! Assets Could not load!");
                return;
            }
        }
        
        if(model.getShader() != null) model.getShader().setTransformationMatrix(transformationMatrix);
        model.render(true);
    }
}
