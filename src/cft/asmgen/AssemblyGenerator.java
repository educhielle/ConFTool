/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.asmgen;

import cft.config.DB;
import cft.util.Global;
import cft.util.Util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class AssemblyGenerator
{
    public static void generateAll(String fileNameIn, String fileNameOut) throws IOException
    {
        List<String> hideFunctions = findHideFunctions(fileNameIn);
        System.out.println("Not in assembly functions: "+hideFunctions);
        
        while (!hideFunctions.isEmpty())
        {
            for (int i = 0; i < hideFunctions.size(); i++)
            {
                //System.out.println(hideFunctions.get(i));
                String fileNameAux = Global.nextName();
                createAssemblyCode(fileNameIn, fileNameAux, hideFunctions.get(i));
                fileNameIn = fileNameAux;
            }
            
            hideFunctions = findHideFunctions(fileNameIn);
            System.out.println("Not in assembly functions: "+hideFunctions);
        }
        
        Util.fileRename(fileNameIn, fileNameOut);
    }
    
    /*
     * Geração de código assembly
     */
    
    public static void createAssemblyCode(String fileNameIn, String fileNameOut, String functionName) throws IOException
    {
        String dumpFilename = DB.getDumpFilename();
        //Util.getFileTitle(fileNameIn) + "Dumped." + Util.getFileExtension(fileNameIn);
        String fileNameFunction = Global.nextName();
        
        createFunctionCode(dumpFilename, fileNameFunction, functionName);
        
        List<String> fileNames = new ArrayList<String>();
        fileNames.add(fileNameIn);
        fileNames.add(fileNameFunction);
        Util.concatFiles(fileNames, fileNameOut);
    }
    
    public static void createFunctionCode(String dumpFilename, String fileNameOut, String functionName) throws IOException
    {
        FileWriter outputFile = new FileWriter(fileNameOut);
        BufferedWriter outputFileWriter = new BufferedWriter(outputFile);
        outputFileWriter.write(CodeGenerator.getFunctionAssemblyCode(functionName));
        outputFileWriter.close();
    }
    
    /*
     * Busca por funções
     */
    
    public static List<String> findHideFunctions(String fileName) throws IOException
    {
        List<String> hideFunctions = new ArrayList<String>();
        List<String> calledFunctions = findCalledFunctions(fileName);
        List<String> existingFunctions = findExistingFunctions(fileName);
        
        for (int i = 0; i < calledFunctions.size(); i++)
        {
            String functionName = calledFunctions.get(i);
            if ((existingFunctions.indexOf(functionName) == -1) && !functionName.equalsIgnoreCase(DB.getMainLabelName()))
            {
                hideFunctions.add(functionName);
            }
        }
        
        return hideFunctions;
    }
    
    public static List<String> findCalledFunctions(String fileName) throws IOException
    {
        List<String> calledFunctions = new ArrayList<String>();
        BufferedReader in = new BufferedReader(new FileReader(fileName));
        
        for (String line; (line = in.readLine()) != null;)
        {
            if (DB.isFunctionCall(DB.extractInstruction(line)))
            {
                String target = new String();
                try
                {
                    target = DB.getTarget(line).get(0);
                } catch (IndexOutOfBoundsException e)
                {
                    System.out.println("AssemblyGenerator.findCalledFucntions.line: " + line);
                    throw e;
                }
                if (!DB.isRegister(target) && (calledFunctions.indexOf(target) == -1))
                {
                    calledFunctions.add(target);
                }
            }
        }
        
        in.close();
        
        return calledFunctions;
    }
    
    public static List<String> findExistingFunctions(String fileName) throws IOException
    {
        List<String> existingFunctions = new ArrayList<String>();
        BufferedReader in = new BufferedReader(new FileReader(fileName));
        
        for (String line; (line = in.readLine()) != null;)
        {
            if (DB.isLabel(line))
            {
                String label = DB.getLabel(line);
                if (existingFunctions.indexOf(label) == -1) existingFunctions.add(label);
            }
        }
        
        in.close();
        
        return existingFunctions;
    }
}
