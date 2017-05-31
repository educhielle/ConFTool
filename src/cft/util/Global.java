/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.util;

/**
 *
 * @author Eduardo
 */
public class Global
{
    private static int counter = 0;
    
    public static String nextName()
    {
        return "tmp" + (counter++) + ".s";
    }
}
