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
package wrath.client.enums;

/**
* Enumerator describing whether the game should be run in 2D Mode or 3D Mode.
* @author Trent Spears
*/
public enum RenderMode 
{
    /**
     * Game is rendered in 2 Dimensions.
     */
    Mode2D,
    /**
     * Game is rendered with depth (3 dimensions).
     */
    Mode3D;
}
