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
 * Interface to handle general {@link wrath.client.Game} events.
 * To be implemented by the game developer to receive events.
 * @author Trent Spears
 */
public interface GameEventHandler
{
    /**
     * Method that is called when the game is closed (as soon as the close is requested).
     */
    public void onGameClose();
    
    /**
     * Method that is called when the game is opened (right before the loop initializes.
     */
    public void onGameOpen();
  
    /**
     * Method that is called every time the game's logic is supposed to update.
     */
    public void onTick();
    
    /**
     * Called when the window is opened.
     */
    public void onWindowOpen();
    
    /**
     * Called when the window's resolution is changed.
     * @param oldWidth The previous width of the resolution.
     * @param oldHeight The previous height of the resolution.
     * @param newWidth The new width of the resolution.
     * @param newHeight The new height of the resolution.
     */
    public void onResolutionChange(int oldWidth, int oldHeight, int newWidth, int newHeight);
}
