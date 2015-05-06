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
* Enumerator describing the display mode of the Window.
* @author Trent Spears
*/
public enum WindowState
{
    /**
     * A truly fullscreen window.
     */
    FULLSCREEN,
    /**
     * A "Fullscreen" window that is really windowed, but covers the entire monitor.
     */
    FULLSCREEN_WINDOWED,
    /**
     * Standard Windowed application.
     */
    WINDOWED,
    /**
     * A window with no border (close widget).
     */
    WINDOWED_UNDECORATED;
}