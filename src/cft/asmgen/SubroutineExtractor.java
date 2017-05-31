/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.asmgen;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class SubroutineExtractor
{
    private static List<List> code = new ArrayList<List>();
    
    public static void extractSubroutine(String dumpFilename, String subroutineFilename, String subroutineName)
    {
        readFunction(subroutineFilename, subroutineName);
    }
    
    public static void readFunction(String subroutineFilename, String subroutineName)
    {
        
    }
}
