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
package wrath.client.input;
    

/**
 * Used to differentiate between whether the action should execute when a key is pressed or released.
 * @author Trent Spears
 */
public enum KeyAction
{
    /**
     * Describing when a key is being held down.
     */
    KEY_HOLD_DOWN,
    /**
     * Describing when a key is pressed.
     */
    KEY_PRESS,
    /**
     * Describing when a key is released.
     */
    KEY_RELEASE;
}
