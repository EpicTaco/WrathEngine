/**
 * Wrath Engine Copyright (C) 2015 Trent Spears
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

import org.lwjgl.util.vector.Vector3f;
import wrath.client.enums.RenderMode;
import wrath.client.events.GameEventHandler;
import wrath.client.graphics.EntityRenderer;
import wrath.common.entities.EntityDescriptor;
import wrath.common.entities.GenericEntity;

/**
 * A class used to internally test the Game Engine.
 * @author Trent Spears
 */
public class TestGameInst extends Game implements GameEventHandler
{
    EntityRenderer render;
    
    public TestGameInst()
    {
        super("Test Client", "INDEV", 60f, RenderMode.Mode3D);
        getEventManager().addGameEventHandler(this);
    }

    @Override
    public void onGameClose() 
    {
        getInputManager().setEngineKeysToDefault();
    }

    @Override
    public void onGameOpen() 
    {
        render = new EntityRenderer(new GenericEntity(new Vector3f(0.0f, -1.0f, -10.0f), null, new EntityDescriptor("body.obj", "white_texture.png", null, 0.2f, 1.0f, 0.0f)));
    }

    @Override
    public void onLoadJavaPlugin(Object loadedObject) 
    {
        
    }

    @Override
    public void onTick() 
    {
        render.getEntity().translateOrientation(0f, 1f, 0f);
    }

    @Override
    public void onWindowOpen() 
    {
        
    }

    @Override
    public void render()
    {
        render.render();
    }
    
    @Override
    public void onResolutionChange(int oldWidth, int oldHeight, int newWidth, int newHeight) 
    {
        
    }
}
