/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wrath.client;

import java.io.Serializable;

/**
 * {@link java.io.Serializable} version of {@link java.lang.Runnable} to be used to respond to key tap events.
 * @author Trent Spears
 */
public interface KeyRunnable extends Serializable
{
    public void run();
}
