/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.techniques;

import cft.config.DB;
import static cft.techniques.SETA.apply;
import static cft.techniques.SETA.defineProtectStatus;
import static cft.techniques.SETA.defineVerifyStatus;
import static cft.techniques.SETA.rearrangePredAndSuccLists;
import static cft.techniques.SETA.setNumberOfInstructions;
import cft.util.BasicBlock;
import cft.util.Instruction;
import cft.util.Network;
import cft.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class BBCounter 
{
    public static String apply(String fileNameIn) throws IOException 
    {
        String fileNameOut = Util.getFileTitle(fileNameIn) + "BBCounter." + Util.getFileExtension(fileNameIn);
        apply(fileNameIn, fileNameOut);
        
        return fileNameOut;
    }
    
    public static void apply(String fileNameIn, String fileNameOut) throws IOException
    {
        //Get the code
        List<String> code = Util.getCode(fileNameIn, DB.getCommentTag());
        
        //Create Basic Blocks
        //List<BasicBlock> basicBlocks = createBasicBlocks(code);
        List<BasicBlock> basicBlocks = BasicBlock.createBasicBlocks(code);
        
        //Insert signatures in the program's source code
        code = insertCounters(code, basicBlocks);
        
        //Insert NOP before calls to subroutines to avoid double counting
        code = (DB.getSetaInsertNops()?insertNoOperationBeforeFunctionCalls(code):code);
        
        //Write the code to a new file
        Util.write(code, fileNameOut);
    }
    
    private static List<String> insertCounters(List<String> code, List<BasicBlock> basicBlocks)
    {
        //Check if there is any available register
        List<String> usedRegisters = DB.getUsedRegisters(code);
        List<String> freeRegisters = Util.complement(DB.getGeneralPurposeRegisters(), usedRegisters);
        freeRegisters.removeAll(DB.getPreInitializedRegisters());
        
        Instruction li = DB.getImmediateAssignmentInstruction();
        Instruction add = DB.getAddImmediateInstruction();
        List<String> emptyList = new ArrayList<String>();
        List<String> register;
        List<String> one = new ArrayList<String>();
        one.add(String.valueOf(1));
        List<String> zero = new ArrayList<String>();
        zero.add(String.valueOf(0));
        int nextRegister = 0;
        
        for (int i = basicBlocks.size() - 1; i >= 0; i--)
        {
            register = new ArrayList<String>();
            register.add(freeRegisters.get(nextRegister));
            nextRegister = (nextRegister+1)%freeRegisters.size();
            int posStart = basicBlocks.get(i).getPosStart();
            if (DB.isLabel(code.get(posStart))) posStart++;
            code.add(posStart, Util.generateCommand(add.getName(), register, register, one, emptyList, emptyList, add.getFormat()));
        }
        
        int mainPos = DB.getMainPosition(code);
        for (int i = 0; i < freeRegisters.size(); i++)
        {
            register = new ArrayList<String>();
            register.add(freeRegisters.get(i));
            code.add(mainPos+i+1, Util.generateCommand(li.getName(), register, emptyList, zero, emptyList, emptyList, li.getFormat()));
        }
        
        return code;
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
