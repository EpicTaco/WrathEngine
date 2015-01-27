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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
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
    
    private transient double csize;
    private final File file;
    private final Tile[][] grid;
    private transient int size;
    
    private transient double[] lines;
    
    public TempWorld(int dimension, File save)
    {
        size = dimension;
        grid = new Tile[dimension][dimension];
        calculate();
        for(int x = 0; x< dimension; x++)
            for(int y = 0; y < dimension; y++) grid[x][y] = new Tile(x, y);
        
        file = save;
    }
    
    private void calculate()
    {
        size = grid.length;
        csize = (double) 2 / size;
        
        lines = new double[size];
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
                int id = grid[x][y].id;
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
        return grid[x][y].id;
    }
    
    public int[] getBounds(double screenx, double screeny)
    {
        if(screenx > 1 || screenx < -1 || screeny > 1 || screeny < -1) return new int[0];
        
        for(int x = 0; x < size; x++)
            for(int y = 0; y < size; y++)
            {
                Tile t = grid[x][y];
                if(screenx < t.x || screeny < t.y) continue;
                else
                {
                    if(screenx >= t.x + csize || screeny >= t.y + csize) continue;
                    else return new int[]{x,y};
                }
            }
        
        return new int[0];
    }
    
    public void setTile(int x, int y, int tileId)
    {
        grid[x][y].id = tileId;
    }
    
    public void save()
    {
        FileOutputStream fos = null;
        GZIPOutputStream gout = null;
        ObjectOutputStream out = null;
        
        try
        {
            out = new ObjectOutputStream((gout = new GZIPOutputStream((fos = new FileOutputStream(file)))));
            out.writeObject(this);
            gout.finish();
            out.flush();
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not save World! I/O Error!");
        }
        
        if(out == null || fos == null || gout == null) return;
        
        try
        {
            out.close();
            gout.close();
            fos.close();
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not close World streams! I/O Error!");
        }
    }
    
    public static TempWorld load(File file)
    {
        TempWorld ret = null;
        FileInputStream fis = null;
        GZIPInputStream in = null;
        ObjectInputStream is = null;
        
        try
        {
            is = new ObjectInputStream((in = new GZIPInputStream(new FileInputStream(file))));
            Object genObj = is.readObject();
            if(genObj instanceof TempWorld) ret = (TempWorld) genObj;
        }
        catch(IOException | ClassNotFoundException e)
        {
            Logger.getErrorLogger().log("Could not load World! I/O Error!");
        }
        
        if(ret != null) ret.calculate();
        
        if(in == null || fis == null || is == null) return ret;
        
        try
        {
            is.close();
            in.close();
            fis.close();
        }
        catch(IOException e)
        {
            Logger.getErrorLogger().log("Could not close World streams! I/O Error!");
        }
        
        return ret;
    }
    
    private class Tile implements Serializable
    {
        public Tile(int arrx, int arry)
        {
            x = (double) arrx * csize - 1.0;
            y = (double) arry * csize - 1.0;
        }
        
        public final double x,y;
        public int id = AIR;
    }
}
