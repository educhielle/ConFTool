/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cft.techniques;

import cft.config.DB;
import static cft.techniques.SETA.apply;
import cft.util.BasicBlock;
import cft.util.Instruction;
import cft.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class LifetimeReducer
{
    public static final int BASE = 1000000;
    
    public static String apply(String fileNameIn) throws IOException 
    {
        String fileNameOut = Util.getFileTitle(fileNameIn) + "LTR." + Util.getFileExtension(fileNameIn);
        apply(fileNameIn, fileNameOut);
        
        return fileNameOut;
    }

    public static void apply(String fileNameIn, String fileNameOut) throws IOException
    {
        //Get the code
        List<String> code = Util.getCode(fileNameIn, DB.getCommentTag());
        
        //List<BasicBlock> basicBlocks = Util.getBasicBlocks(code);
        
        //Create Basic Blocks
        List<BasicBlock> basicBlocks = createBasicBlocks(code);
        
        basicBlocks = GetRegistersLists(code, basicBlocks);
        
        PrintRegistersLists(basicBlocks);
        
        Util.write(code, fileNameOut);
    }
    
        /*
     * Create basic blocks
     */
    private static List<BasicBlock> createBasicBlocks(List<String> code) throws IOException
    {
        /*
         * Find all positions where a basic block starts or it ends and add it to a list.
         */
        List<Integer> basicBlockStarts = new ArrayList<Integer>();
        List<Integer> basicBlockEnds = new ArrayList<Integer>();
        
        for (int i = 0; i < code.size(); i++)
        {
            if (DB.isLabel(code.get(i))) 
            {
                if (!basicBlockStarts.contains(i))
                {
                    basicBlockStarts.add(i);
                }
                
                for (int j = i - 1; j >= 0; j--)
                {
                    if (DB.isLabel(code.get(j)))
                    {
                        break;
                    }
                    else if (DB.isInstruction(code.get(j)))
                    {
                        if (!basicBlockEnds.contains(j))
                        {
                            basicBlockEnds.add(j);
                        }
                        break;
                    }
                }
            }
            else if (DB.isInstruction(code.get(i)))
            {
                Instruction instruction = DB.getInstruction(code.get(i));
                
                if (DB.isJump(instruction) || (DB.isBranch(instruction) && !(DB.getTarget(code.get(i), instruction.getFormat()).get(0).equals(DB.ERROR_LABEL_NAME))))
                {
                    for (int j = i + 1; j < code.size(); j++)
                    {
                        if (DB.isLabel(code.get(j)) || DB.isInstruction(code.get(j)))
                        {
                            if (!basicBlockStarts.contains(j))
                            {
                                basicBlockStarts.add(j);
                            }
                            break;
                        }
                    }
                    
                    if (!basicBlockEnds.contains(i))
                    {
                        basicBlockEnds.add(i);
                    }
                }
            }
        }
        
        //Correct inconsistances in basicBlockDivisions
        //If there is no END between STARTS, remove the first START from basicBlockStarts
        for (int i = 0; i < basicBlockStarts.size() - 1; i++)
        {
            boolean ok = false;
            for (int j = 0; j < basicBlockEnds.size(); j++)
            {
                if ((basicBlockEnds.get(j) >= basicBlockStarts.get(i)) && (basicBlockEnds.get(j) < basicBlockStarts.get(i+1)))
                {
                    ok = true;
                    break;
                }
            }
            
            if (!ok)
            {
                basicBlockStarts.remove(i--);
            }
        }
        if (basicBlockStarts.get(basicBlockStarts.size() - 1) >= basicBlockEnds.get(basicBlockEnds.size() - 1))
        {
            basicBlockStarts.remove(basicBlockStarts.size() - 1);
        }
                
        /*
         * Create basic blocks
         */
        List<BasicBlock> basicBlocks = new ArrayList<BasicBlock>();
        for (int i = 0; i < basicBlockStarts.size(); i++)
        {
            BasicBlock basicBlock = new BasicBlock(i, basicBlockStarts.get(i), basicBlockEnds.get(i));
            basicBlocks.add(basicBlock);
        }
        
        return basicBlocks;
    }
    
    
    
     /*
     * Create list of registers used at the BasicBlock
     */
    private static List<BasicBlock> createRegsList(List<String> code, List<BasicBlock> basicBlocks)
    {
        
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            
            BasicBlock basicBlock = basicBlocks.get(i);
            int id = basicBlock.getId();
            int posStart = basicBlock.getPosStart();
            int posEnd = basicBlock.getPosEnd();
            List<String> usedRegisters = new ArrayList<String>();
            String register = new String();

            for (int j = posStart; j <= posEnd; j++) 
            {
                List<String> regsList = DB.getRS(code.get(j));
                regsList.addAll(DB.getRD(code.get(j)));

                for (int k = 0; k < regsList.size(); k++) 
                {
                    register = regsList.get(j);
                    if (DB.isRegister(register))
                    {
                        if (!usedRegisters.contains(register))
                            usedRegisters.add(register);
                    }
                }

            }
            
            basicBlock.setListRegs(usedRegisters);
            
        }

        return basicBlocks;
    }
    
     private static List<BasicBlock> GetRegistersLists(List<String> code, List<BasicBlock> basicBlocks)
     {
         
        List<String> usedRegisters = new ArrayList<String>();
        
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            
            BasicBlock basicBlock = basicBlocks.get(i);
            int id = basicBlock.getId();
            int posStart = basicBlock.getPosStart();
            int posEnd = basicBlock.getPosEnd();
            String register = new String();
            
            List<String> listRD = new ArrayList<String>();
            List<String> listRS = new ArrayList<String>();

            for (int j = posStart; j <= posEnd; j++) 
            {
                List<String> rsListAtInstruction = DB.getRS(code.get(j));
                List<String> rdListAtInstruction = DB.getRD(code.get(j));

                for (int k = 0; k < rsListAtInstruction.size(); k++) 
                {
                    register = rsListAtInstruction.get(k);
                    if (DB.isRegister(register)&& !listRD.contains(register) && !listRS.contains(register))
                    {
                        listRS.add(register);
                    }
                }
                
                for (int k = 0; k < rdListAtInstruction.size(); k++) 
                {
                    register = rdListAtInstruction.get(k);
                    if (DB.isRegister(register)&& !listRD.contains(register))
                    {
                        listRD.add(register);
                    }
                }
            }
            
            basicBlock.setListRS(listRS);
            basicBlock.setListRD(listRD);

        }
        
        return basicBlocks;
     }
             
             
    private static void PrintRegistersLists(List<BasicBlock> basicBlocks)
    { 
        for (int i = 0; i < basicBlocks.size(); i++) 
        {
            BasicBlock basicBlock = basicBlocks.get(i);
            int id = basicBlock.getId();
            System.out.println("Lists for BB" + id + ":");
            System.out.println("RD:");
            System.out.println(basicBlock.getListRD());
            System.out.println("RS:");
            System.out.println(basicBlock.getListRS());
            System.out.println("__________________________");
        }
    }
    
}
