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
package wrath.client;

/**
 * Class to act as a portal between external applications and the {@link wrath.client.Game} class.
 * @author Trent Spears
 */
public class ExternalPluginManager
{
    private static Game inst = null;
    
    /**
     * Gets the currently active {@link wrath.client.Game} class.
     * @return Returns the currently active {@link wrath.client.Game} class.
     */
    public static Game getGameInstance()
    {
        return inst;
    }
    
    /**
     * Executed automatically internally, this method links the {@link wrath.client.Game} class to link with external applications.
     * @param game The {@link wrath.client.Game} instance to link with external applications.
     */
    protected static void setGameInstance(Game game)
    {
        inst = game;
    }
}
