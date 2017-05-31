/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.techniques;

import cft.config.DB;
import cft.util.Instruction;
import cft.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class LwSwCounter
{
    public static String apply(String fileNameIn) throws IOException 
    {
        String fileNameOut = Util.getFileTitle(fileNameIn) + "LwSwCounter." + Util.getFileExtension(fileNameIn);
        apply(fileNameIn, fileNameOut);
        
        return fileNameOut;
    }
    
    public static void apply(String fileNameIn, String fileNameOut) throws IOException
    {
        //Get the code
        List<String> code = Util.getCode(fileNameIn, DB.getCommentTag());
        
        code = insertCounters(code);
        
        //Write the code to a new file
        Util.write(code, fileNameOut);
    }
    
    private static List<String> insertCounters(List<String> code)
    {
        //Check if there is any available register
        List<String> codeOut = new ArrayList<String>(code);
        
        List<String> usedRegisters = DB.getUsedRegisters(codeOut);
        List<String> freeRegisters = Util.complement(DB.getGeneralPurposeRegisters(), usedRegisters);
        freeRegisters.removeAll(DB.getPreInitializedRegisters());
        
        Instruction li = DB.getImmediateAssignmentInstruction();
        Instruction add = DB.getAddImmediateInstruction();
        
        List<String> emptyList = new ArrayList<String>();
        List<String> one = Util.createList("1");
        List<String> zero = Util.createList("0");
        
        List<String> lwRegister = Util.createList(freeRegisters.get(0));
        List<String> swRegister = Util.createList(freeRegisters.get(1));
        
        //add counters increment
        for (int i = codeOut.size() - 1; i >= 0; i--)
        {
            String line = codeOut.get(i);
            
            if (DB.isInstruction(line))
            {
                Instruction instruction = DB.getInstruction(line);
                
                if (DB.isLoad(instruction))
                {
                    //increment LW counter
                    codeOut.add(i, Util.generateCommand(add.getName(), lwRegister, lwRegister, one, emptyList, emptyList, add.getFormat()));
                }
                else if (DB.isStore(instruction))
                {
                    //increment SW counter
                    codeOut.add(i, Util.generateCommand(add.getName(), swRegister, swRegister, one, emptyList, emptyList, add.getFormat()));
                }
            }
        }
        
        //reset counters
        int mainPos = DB.getMainPosition(codeOut);
        codeOut.add(mainPos+1, Util.generateCommand(li.getName(), lwRegister, emptyList, zero, emptyList, emptyList, li.getFormat()));
        codeOut.add(mainPos+1, Util.generateCommand(li.getName(), swRegister, emptyList, zero, emptyList, emptyList, li.getFormat()));
        
        codeOut = (DB.getSetaInsertNops()?insertNoOperationBeforeFunctionCalls(codeOut):codeOut);
        
        return codeOut;
    }
    
    private static List<String> insertNoOperationBeforeFunctionCalls(List<String> code)
    {
        List<String> codeOut = new ArrayList<String>();
        List<String> callsToSubroutines = DB.getCallsToSubroutine(code);
        List<String> emptyList = new ArrayList<String>();
        Instruction nop = DB.getNoOperationInstruction();
        //System.out.println(callsToSubroutines);
        for (int i = 0; i < code.size(); i++)
        {
            String line = code.get(i);
            String previousLine = (i>0?code.get(i-1):"");
            if (DB.isInstruction(line))
            {
                if (callsToSubroutines.contains(DB.extractInstruction(line)) && !DB.extractInstruction(previousLine).equals(nop.getName()))
                {
                    codeOut.add(Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                }
            }
            codeOut.add(code.get(i));
        }
        
        return codeOut;
    }
}
