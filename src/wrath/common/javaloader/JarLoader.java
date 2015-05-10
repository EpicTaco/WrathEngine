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
package wrath.common.javaloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import wrath.util.Logger;

/**
 * This class is made to easily add new Java files into the current environment.
 * @author Trent Spears
 */
public class JarLoader
{
    private static final JarLoader inst = new JarLoader();
    public static final Logger JAVA_LDR_LOGGER = new Logger("jarloader");
    
    private JarLoader(){}
    
    /**
     * Closes the standard java-loader java and saves the standard java-loader config.
     */
    public static void closeJavaPlugins()
    {
        JAVA_LDR_LOGGER.close();
    }
    
    /**
     * Loads a {@link java.lang.Class} instance that represents a class located in the specified Jar file.
     * @param jarFile The Jar File to read the class from.
     * @param classPath The package.file path to the Class located in the Jar File.
     * @return Returns a {@link java.lang.Class} instance that represents the class specified in the path.
     */
    public static Class loadClass(File jarFile, String classPath)
    {
        Class ret = null;
        
        try 
        {
            URLClassLoader load = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, inst.getClass().getClassLoader());
            ret = Class.forName(classPath, true, load);
        }
        catch(ClassNotFoundException e)
        {
            JAVA_LDR_LOGGER.log("Could not read class '" + classPath+ "' from Jar file '" + jarFile.getAbsolutePath() + "'!");
        }
        catch(MalformedURLException ex) 
        {
            JAVA_LDR_LOGGER.log("Could not read Jar file '" + jarFile.getAbsolutePath() + "'!");
        }
        return ret;
    }
    
    /**
     * Detects Jar files in a directory and loads them, attempting to read the 'init' file in it's default package to find and load its main class.
     * @param pluginsDir The Directory to load the plugins from in {@link java.io.File} object.
     * @return Returns an array of loaded objects.
     */
    public static Object[] loadPluginsDirectory(File pluginsDir)
    {
        if(!pluginsDir.exists() || !pluginsDir.isDirectory())
        {
            JAVA_LDR_LOGGER.log("Could not load jars from directory '" + pluginsDir.getAbsolutePath() + "', directory does not exist!");
            return new Object[0];
        }
        
        ArrayList<Object> obj = new ArrayList<>();
        for(File f : pluginsDir.listFiles())
            if(f.getName().toLowerCase().endsWith(".jar"))
            {
                try
                {
                    URL url = new URL("jar:file:" + f.getAbsolutePath() + "!/init");
                    BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                    String classPath = br.readLine();
                    br.close();
                    Object o = loadObject(f, classPath);
                    if(o != null) obj.add(o);
                }
                catch(IOException e)
                {
                    JAVA_LDR_LOGGER.log("Could not load jar '" + f.getAbsolutePath() + "'! I/O Error!");
                }
            }
        
        Object[] ret = new Object[obj.size()];
        obj.toArray(ret);
        return ret;
    }
    
    /**
     * Loads a {@link java.lang.Class} from the specified Jar file and creates an instance of it.
     * Specified object MUST have an empty constructor.
     * @param jarFile The Jar File to load the object from.
     * @return Returns an {@link java.lang.Object} that was made from the specified class.
     */
    public static Object loadObject(File jarFile)
    {
        try(BufferedReader read = new BufferedReader(new InputStreamReader(new URL("jar:file:" + jarFile.getAbsolutePath() + "!/init").openStream())))
        {
            String classPath = read.readLine();
            return loadObject(jarFile, classPath);
        }
        catch(MalformedURLException e)
        {
            JAVA_LDR_LOGGER.log("Could not load jar plugin '" + jarFile.getName() + "'! File 'init' was not found!");
        }
        catch(IOException e)
        {
            JAVA_LDR_LOGGER.log("Could not load jar plugin '" + jarFile.getName() + "'! I/O Error!");
        }
        
        return null;
    }
    
    /**
     * Loads a {@link java.lang.Class} from the specified Jar file and creates an instance of it.
     * Specified object MUST have an empty constructor.
     * @param jarFile The Jar File to load the object from.
     * @param classPath The package.file path to the Class located in the Jar File.
     * @return Returns an {@link java.lang.Object} that was made from the specified class.
     */
    public static Object loadObject(File jarFile, String classPath)
    {
        Object ret = null;
        Class c = loadClass(jarFile, classPath);
        try
        {
            ret = c.getConstructor().newInstance();
        }
        catch(NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            JAVA_LDR_LOGGER.log("Could not read class '" + classPath+ "' from Jar file '" + jarFile.getAbsolutePath() + "'!\n"
                    + "    No public, empty constructor present!");
        }
        return ret;
    }
}
