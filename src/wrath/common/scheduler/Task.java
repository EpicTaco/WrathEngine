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

package wrath.common.scheduler;

/**
 * Class to extend and override run(), used with Scheduler.
 * @author Trent Spears
 */
public class Task
{   
    private boolean active = true;
    private long delay = 0;
    private boolean repeating = false;
    
    /**
     * Constructor, no arguments needed.
     */
    public Task(){}
    
    /**
     * Cancels the execution of the task.
     */
    public void cancelTask()
    {
        active = false;
    }
    
    /**
     * If the task is repeating, gets the set delay in ticks.
     * @return Returns the number of ticks until the task will be run again, 0 if not repeating.
     */
    public long getDelay()
    {
        return delay;
    }
    
    /**
     * Returns true if task is to execute, otherwise false.
     * @return Returns whether or not the task has been canceled. True is alive, false if canceled.
     */
    public boolean isActive()
    {
        return active;
    }
    
    /**
     * Returns true if the task is repeating, otherwise false.
     * @return Returns whether or not the task will repeat once executed.
     */
    public boolean isRepeating()
    {
        return repeating;
    }
    
    /**
     * Override this method to execute tasks within the internal scheduler.
     */
    public void run() {}
    
    /**
     * Set the task to be repeating every x ticks. Equivalent to runRepeatingTask() in Scheduler.
     * @param ticksDelay The amount of ticks to wait to run again.
     */
    public void triggerIsRepeating(long ticksDelay)
    {
        repeating = true;
        delay = ticksDelay;
    }
}
