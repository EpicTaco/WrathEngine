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
package wrath.test.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.lwjgl.opengl.GL11;
import wrath.util.Logger;

/**
 * Temporary version of a 2D World.
 * THIS IS NOT HOW WORLDS WILL WORK IN RELEASE!
 * @author Trent Spears
 */
public class TempWorld implements Serializable
{
    public static final int
            AIR = 0,
            GRASS = 1,
            STONE = 2;
    
    private final double csize;
    private final File file;
    private final int[][] grid;
    private final int size;
    
    private final double[] lines;
    
    public TempWorld(int dimension, File save)
    {
        size = dimension;
        grid = new int[dimension][dimension];
        for(int x = 0; x<dimension; x++)
            for(int y = 0; y < dimension; y++) grid[x][y] = AIR;
        
        file = save;
        lines = new double[size];
        csize = (double) 2 / size;
        
        for(int x = 0; x < size; x++)
        {
            lines[x] = (csize * (x+1)) - 1;
        }
    }
    
    public void drawWorld()
    {
        GL11.glBegin(GL11.GL_QUADS);
        for(int x = 0; x < size; x++)
            for(int y = 0; y < size; y++)
            {
                int id = grid[x][y];
                if(id == AIR) GL11.glColor4f(0, .3f, .8f, 0);
                else if(id == GRASS) GL11.glColor4f(0, .8f, .3f, 0);
                else if(id == STONE) GL11.glColor4f(.8f, .8f, .8f, 0);
                
                
                GL11.glVertex2d(x * csize - 1, y * csize - 1);
                GL11.glVertex2d(((x * csize) + csize) - 1, y * csize - 1);
                GL11.glVertex2d(((x * csize) + csize) - 1, (y * csize + csize) - 1);
                GL11.glVertex2d(x * csize - 1, (y * csize + csize) - 1);
                
            }
        GL11.glEnd();
        
        GL11.glColor4f(1, 1, 1, 0);
        
        GL11.glBegin(GL11.GL_LINES);
        for(double c : lines)
        {
            GL11.glVertex2d(c, -1);
            GL11.glVertex2d(c, 1);

            GL11.glVertex2d(-1, c);
            GL11.glVertex2d(1, c);
        }
        GL11.glEnd();
        
    }
    
    public int getTile(int x, int y)
    {
        return grid[x][y];
    }
    
    public int getXBound(float screenx, float screeny)
    {
        
    }
    
    public int getYBound(float screenx, float screeny)
    {
        
    }
    
    public void setTile(int x, int y, int tile)
    {
        grid[x][y] = tile;
    }
    
    public void save()
    {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        
        try
        {
            out = new ObjectOutputStream((fos = new FileOutputStream(file)));
            out.writeObject(this);
            out.flush();
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not save TempWorld! I/O Error!");
        }
        
        if(out == null || fos == null) return;
        
        try
        {
            out.close();
            fos.close();
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not close TempWorld streams! I/O Error!");
        }
    }
    
    public static TempWorld load(File file)
    {
        TempWorld ret = null;
        FileInputStream fis = null;
        ObjectInputStream in = null;
        
        try
        {
            in = new ObjectInputStream((fis = new FileInputStream(file)));
            Object genObj = in.readObject();
            if(genObj instanceof TempWorld) ret = (TempWorld) genObj;
        }
        catch(IOException | ClassNotFoundException e)
        {
            Logger.getErrorLogger().log("Could not load TempWorld! I/O Error!");
        }
        
        if(in == null || fis == null) return null;
        
        try
        {
            in.close();
            fis.close();
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not close TempWorld streams! I/O Error!");
        }
        
        return ret;
    }
}
