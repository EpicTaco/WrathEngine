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
 * Interface to handle general {@link wrath.common.scheduler.Scheduler} events.
 * To be implemented by the game developer to receive events.
 * @author Trent Spears
 */
public interface SchedulerEventHandler
{
    /**
     * Called when a {@link wrath.common.scheduler.Task} is executed by a {@link wrath.common.scheduler.Scheduler}
     * @param scheduler The {@link wrath.common.scheduler.Scheduler} that executed the {@link wrath.common.scheduler.Task}.
     * @param task The {@link wrath.common.scheduler.Task} that was executed.
     */
    public void onTaskRun(Scheduler scheduler, Task task);
    
    /**
     * Called when a {@link wrath.common.scheduler.Task} is scheduled for execution.
     * @param scheduler The {@link wrath.common.scheduler.Scheduler} that scheduled the {@link wrath.common.scheduler.Task}.
     * @param task The {@link wrath.common.scheduler.Task} that was scheduled.
     * @param tick The tick the {@link wrath.common.scheduler.Task} is scheduled to execute on.
     */
    public void onTaskSchedule(Scheduler scheduler, Task task, long tick);
}
