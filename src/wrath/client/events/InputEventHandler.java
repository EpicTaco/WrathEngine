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
package wrath.client.events;

/**
 * Interface to handle general {@link wrath.client.input.InputManager} events.
 * To be implemented by the game developer to receive events.
 * @author Trent Spears
 */
public interface InputEventHandler
{
    /**
     * Method that is called when a key is pressed.
     * Used primarily for text-chat purposes.
     * @param c The character that was entered.
     */
    public void onCharInput(char c);
    
    /**
     * Method that is called when the cursor is moved.
     * @param x The new X coordinate that the cursor is located.
     * @param y The new Y coordinate that the cursor is located.
     */
    public void onCursorMove(double x, double y);
    
    /**
     * Method that is called when the mouse scroll wheel is moved.
     * @param xoffset Left to Right movement. Most mouses will not support this movement. 
     * @param yoffset Standard up and down scroll wheel movement. 1.0 is one unit upwards, -1.0 is one unit downwards.
     */
    public void onScroll(double xoffset, double yoffset);
}
