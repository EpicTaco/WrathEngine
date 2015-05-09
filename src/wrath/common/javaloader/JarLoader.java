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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import wrath.util.Config;
import wrath.util.Logger;

/**
 * This class is made to easily add new Java files into the current environment.
 * @author Trent Spears
 */
public class JarLoader
{
    public static final Config JAVA_LDR_CONFIG = new Config("JarLoader");
    public static final Logger JAVA_LDR_LOGGER = new Logger("JarLoader");
    
    /**
     * Closes the standard java-loader java and saves the standard java-loader config.
     */
    public static void closeJavaPlugins()
    {
        JAVA_LDR_LOGGER.close();
        JAVA_LDR_CONFIG.save();
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
            ClassLoader load = URLClassLoader.newInstance(new URL[]{jarFile.toURI().toURL()});
            ret = load.loadClass(classPath);
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
