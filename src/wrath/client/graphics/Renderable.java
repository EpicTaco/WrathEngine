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

/**
 * Interface to standardize renderable objects in the pipeline.
 * @author Trent Spears
 */
public interface Renderable
{
    /**
     * Called to render the said object.
     * @param consolidated If true, the code will run properly without any additional steps, meaning renderSetup() and renderStop() are called automatically.
     */
    public void render(boolean consolidated);
    
    /**
     * Prepares the renderer to draw this object.
     */
    public void renderSetup();
    
    /**
     * Cleans up all resources associated with drawing this object.
     */
    public void renderStop();
}
