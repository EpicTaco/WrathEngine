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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Built in scheduler to schedule tasks within the internal loop.
 * @author Trent Spears
 */
public class Scheduler
{
    private final ArrayList<SchedulerEventHandler> handlerList = new ArrayList<>();
    private final HashMap<Long, TaskList> map = new HashMap<>();
    private final RootSchedulerEventHandler rootHandler = new RootSchedulerEventHandler();
    private long ticks = 0;
    
    /**
     * Adds a {@link wrath.common.scheduler.SchedulerEventHandler} to the list of handlers that responds to events.
     * @param handler The {@link wrath.common.scheduler.SchedulerEventHandler} to add to the handlers list.
     */
    public void addSchedulerEventHandler(SchedulerEventHandler handler)
    {
        handlerList.add(handler);
    }
    
    /**
     * Gets the current tick count of the scheduler.
     * @return Returns the current tick count of the scheduler.
     */
    public long getCurrentTick()
    {
        return ticks;
    }
    
    private Scheduler getThis()
    {
        return this;
    }
    
    /**
     * Runs a task repeatedly every {delayInTicks} ticks.
     * @param task The task to execute.
     * @param delayInTicks The amount of time to wait in ticks, repeating every x ticks.
     */
    public void runRepeatingTask(Task task, long delayInTicks)
    {
        task.triggerIsRepeating(delayInTicks);
        runTaskLater(task, delayInTicks);
    }
    
    /**
     * Runs the task specified after {waitInTicks}.
     * @param task The task to execute.
     * @param waitInTicks The amount of times to wait in ticks.
     */
    public void runTaskLater(Task task, long waitInTicks)
    {
        if(map.containsKey(ticks + waitInTicks)) map.get(ticks + waitInTicks).addTask(task);
        else
        {
            TaskList l = new TaskList();
            l.addTask(task);
            map.put(ticks + waitInTicks, l);
        }
        rootHandler.onTaskSchedule(this, task, ticks + waitInTicks);
    }
    
    /**
     * Runs the specified task next tick.
     * @param task The task to execute.
     */
    public void runTaskNextTick(Task task)
    {
        if(map.containsKey(ticks + 1)) map.get(ticks + 1).addTask(task);
        else
        {
            TaskList l = new TaskList();
            l.addTask(task);
            map.put(ticks + 1, l);
        }
        rootHandler.onTaskSchedule(this, task, ticks + 1);
    }
    
    /**
     * DO NOT run this method! It will completely mess up timings.
     * For internal engine use only. NOT for the game developer.
     */
    public void onTick()
    {
        ticks++;
        if(map.containsKey(ticks))
        {
            map.get(ticks).start();
            map.remove(ticks);
        }
    }
    
     /**
     * Internal class for task scheduler to contain tasks to do on specified tick.
     */
    private class TaskList
    {
        private boolean alive;
        private final ArrayList<Task> tasks = new ArrayList<>();
        
        public TaskList()
        {
            alive = true;
        }
        
        /**
         * Adds a task to the task list.
         * @param t The task to add to the list.
         */
        public void addTask(Task t)
        {
            if(!tasks.contains(t) && alive) tasks.add(t);
        }
        
        /**
         * Returns whether or not this had been run yet.
         * @return Returns if the list has not been executed yet.
         */
        public boolean isAlive()
        {
            return alive;
        }
        
        /**
         * Launches the execution of all the tasks in the list, can only be used once!.
         */
        public void start()
        {   
            if(tasks.size() > 0 && alive)
            {
                tasks.stream().forEach((t) -> 
                {
                    if(t.isActive())
                    {
                        t.run();
                        rootHandler.onTaskRun(getThis(), t);
                        if(t.isRepeating()) runTaskLater(t, t.getDelay());
                    }
                });
            }
            
            tasks.clear();
            alive = false;
        }
    }
    
    private class RootSchedulerEventHandler implements SchedulerEventHandler
    {

        @Override
        public void onTaskRun(Scheduler scheduler, Task task)
        {
            handlerList.stream().forEach((h) -> 
            {
                h.onTaskRun(scheduler, task);
            });
        }

        @Override
        public void onTaskSchedule(Scheduler scheduler, Task task, long tick)
        {
            handlerList.stream().forEach((h) -> 
            {
                h.onTaskSchedule(scheduler, task, tick);
            });
        }
        
    }
}
