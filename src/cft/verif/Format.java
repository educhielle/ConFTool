/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.verif;

import cft.config.DB;
import cft.util.Util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Eduardo
 */
public class Format
{
    public static void format(String fileNameIn, String fileNameOut) throws IOException
    {
        removeComments(fileNameIn,fileNameOut);
    }
    
    public static void removeComments(String fileNameIn, String fileNameOut) throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(fileNameIn));
        BufferedWriter out = new BufferedWriter(new FileWriter(fileNameOut));
        String commentTag = DB.getArchitecture().getCommentTag();
        Pattern pattern = Pattern.compile(commentTag);
        Matcher matcher;
        String line;

        while ((line = in.readLine()) != null)
        {        
            matcher = pattern.matcher(line);
            StringBuffer str = new StringBuffer();
            if (matcher.find() && (commentTag.length() > 0))
            {
                matcher.appendReplacement(str, "");
                line = String.valueOf(str);
            }
            if (!line.trim().isEmpty())
            {
                out.write(Util.removeWhitespacesFromEnd(line)+"\n");
            }
        }
        
        in.close();
        out.close();
    }
    
    public static String removeComments(String fileNameIn) throws IOException
    {
        String fileNameOut = Util.getFileTitle(fileNameIn) + "NoComments." + Util.getFileExtension(fileNameIn);
        removeComments(fileNameIn, fileNameOut);
        
        return fileNameOut;
    }
    
    /*public static void format(String fileNameIn, String fileNameOut) throws IOException
    {
        String fileNameNoComments = Util.getFileTitle(fileNameIn) + "NoComments." + Util.getFileExtension(fileNameIn);
        removeComments(fileNameIn, fileNameNoComments);
        BufferedReader in = new BufferedReader(new FileReader(fileNameNoComments));
        BufferedWriter out = new BufferedWriter(new FileWriter(fileNameOut));
        String line;
        
        while ((line = in.readLine()) != null)
        {
            if (isLabel(line))
            {
                line = formatLabel(line);
            }
            else if (isInstruction(line))
            {
                //line = formatInstruction(line);
            }
            else
            {
                //instrução desconhecida
            }
        }
        
        in.close();
        out.close();
    }
    
    
    
    /*public static String formatLabel(String str)
    {
        String formatted = new String();
        String labelFormat = DB.getLabelFormat();
        String format = "(?:\\s*)" + labelFormat.replaceAll("l", "(\\\\w+)(?:\\\\s*)") + "(?:\\s*)";
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(str);
        String label = matcher.group();
        System.out.println(label);
        
        return formatted;
    }
    
    public static String formatInstruction(String str)
    {
        String formatted = new String();
        
        return formatted;
    }
    
    public static String formatIt(String str, String format)
    {
        String formatted = new String();
        
        return formatted;
    }*/
}
