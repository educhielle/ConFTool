/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.techniques;

import cft.CFT;
import cft.config.DB;
import static cft.techniques.SETA.apply;
import cft.util.BasicBlock;
import cft.util.Instruction;
import cft.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Eduardo
 */
public class REGCounter
{
    public static String apply(String fileNameIn) throws IOException 
    {
        String filenameOut;
        if (CFT.filenameFinal.contains("_"))
        {
            int index = CFT.filenameFinal.lastIndexOf('_');
            filenameOut = CFT.filenameFinal.substring(0,index) + "#REG#" + CFT.filenameFinal.substring(index);
        }
        else
        {
            filenameOut = Util.getFileTitle(CFT.filenameFinal) + "#REG#." + Util.getFileExtension(CFT.filenameFinal);
        }
        
        apply(fileNameIn, filenameOut);
        
        return filenameOut;
    }
    
    public static void apply(String fileNameIn, String fileNameOut) throws IOException
    {
        //Get the code
        List<String> code = Util.getCode(fileNameIn, DB.getCommentTag());
        
        writeAllCounters(code, fileNameOut);
        
        Util.removeTempFiles();
        System.exit(0);
    }
    
    private static void writeAllCounters(List<String> code, String fileNameOut) throws IOException
    {
        //Check if there is any available register
        List<String> usedRegisters = DB.getUsedRegisters(code);
        
        for (String register : usedRegisters)
        {
            String filenameAux = fileNameOut.replaceAll("#REG#", register.substring(1));
            List<String> codeOut = insertCounters(code, register);
            Util.write(codeOut, filenameAux);
        }
    }
    
    private static List<String> insertCounters(List<String> code, String register)
    {
        //Check if there is any available register
        List<String> codeOut = new ArrayList<String>(code);
        
        List<String> usedRegisters = DB.getUsedRegisters(codeOut);
        List<String> freeRegisters = Util.complement(DB.getGeneralPurposeRegisters(), usedRegisters);
        freeRegisters.removeAll(DB.getPreInitializedRegisters());
        
        Instruction li = DB.getImmediateAssignmentInstruction();
        Instruction add = DB.getAddImmediateInstruction();
        
        List<String> emptyList = new ArrayList<String>();
        List<String> one = new ArrayList<String>();
        one.add(String.valueOf(1));
        List<String> zero = new ArrayList<String>();
        zero.add(String.valueOf(0));
        
        List<String> destinationRegister;
        destinationRegister = new ArrayList<String>();
        destinationRegister.add(freeRegisters.get(0));
        
        List<String> sourceRegister;
        sourceRegister = new ArrayList<String>();
        sourceRegister.add(freeRegisters.get(1));
        
        //add counters increment
        for (int i = codeOut.size() - 1; i >= 0; i--)
        {
            String line = codeOut.get(i);
            
            if (DB.isInstruction(line))
            {
                Instruction instruction = DB.getInstruction(line);
                List<String> rd = DB.getRD(codeOut.get(i), instruction.getFormat());
                List<String> rs = DB.getRS(codeOut.get(i), instruction.getFormat());
                
                String[] hiddenRS = instruction.getHiddenRS();
                for (String regI : hiddenRS) rs.add(regI);
                
                String[] hiddenRD = instruction.getHiddenRD();
                for (String regI : hiddenRD) rd.add(regI);
                
                //increment RS counter
                for (String regI : rs)
                {
                    if (regI.equals(register))
                    {
                        codeOut.add(i, Util.generateCommand(add.getName(), sourceRegister, sourceRegister, one, emptyList, emptyList, add.getFormat()));
                    }
                }
                
                //increment RD counter
                for (String regI : rd)
                {
                    if (regI.equals(register))
                    {
                        codeOut.add(i, Util.generateCommand(add.getName(), destinationRegister, destinationRegister, one, emptyList, emptyList, add.getFormat()));
                    }
                }
            }
        }
        
        //reset counters
        int mainPos = DB.getMainPosition(codeOut);
        codeOut.add(mainPos+1, Util.generateCommand(li.getName(), sourceRegister, emptyList, zero, emptyList, emptyList, li.getFormat()));
        codeOut.add(mainPos+1, Util.generateCommand(li.getName(), destinationRegister, emptyList, zero, emptyList, emptyList, li.getFormat()));
        
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
