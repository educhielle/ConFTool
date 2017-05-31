/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.techniques;

import cft.config.DB;
import cft.util.BasicBlock;
import cft.util.DumpFileDBAnalyser;
import cft.util.Instruction;
import cft.util.Network;
import cft.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo, Gennaro
 */
public class HETA
{
    public static String apply(String fileNameIn) throws IOException 
    {
        String fileNameOut = Util.getFileTitle(fileNameIn) + "HETA." + Util.getFileExtension(fileNameIn);
        apply(fileNameIn, fileNameOut);
        
        return fileNameOut;
    }
    
    public static void apply(String fileNameIn, String fileNameOut) throws IOException
    {
        //Get the code
        List<String> code = Util.getCode(fileNameIn, DB.getCommentTag());
        
        //List<BasicBlock> basicBlocks = Util.getBasicBlocks(code);
        
        //Create Basic Blocks
        List<BasicBlock> basicBlocks = createBasicBlocksFromAssembly(code);
                
        //set NS Signatures from Dump file
        basicBlocks = setNSSignatures(basicBlocks);
        
        //Create list of successors of each basic block
        basicBlocks = createSuccList(code, basicBlocks);
        
        //Create list of predecessors of each basic block
        basicBlocks = createPredList(basicBlocks);
        
        //Atribute type 'A' or 'X' to the basic blocks
        basicBlocks = attributeType(basicBlocks);
        
        //Create list of networks. Each network has one or more basic blocks and each basic block belong to one and only one network.
        List<Network> networks = createNetworks(basicBlocks);
        
        //Create predecessor network to each network if it has one
        networks = createPredNetwork(networks);
                
        //Generates NIS and NES Signatures
        setSignatures(networks);
        
        //Show information
        showInformation(basicBlocks, networks);
        
        //Insert signatures in the program's source code
        code = insertSignatures(code, basicBlocks);
        
        //Insert NOP before calls to subroutines to avoid double execution of NES' XORs
        code = insertNoOperationBeforeFunctionCalls(code);
        
        //Write the code to a new file
        Util.write(code, fileNameOut);
    }
    
    /*
     * Create basic blocks
     */
    private static List<BasicBlock> createBasicBlocksFromAssembly(List<String> code) throws IOException
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
    
        
    private static List<BasicBlock> setNSSignatures(List<BasicBlock> basicBlocks) throws IOException
    {
   
        DumpFileDBAnalyser DumpAnalyser =  new DumpFileDBAnalyser();
        
        List<BasicBlock> DumpedBasicBlocks = DumpAnalyser.getBasicBlocks();
   
        for (int i = 0; i < basicBlocks.size(); i++)
            basicBlocks.get(i).getHetaNs().setSign(DumpedBasicBlocks.get(i).getHetaNs().getSign());
        
//        for (int i = 0; i < basicBlocks.size(); i++)
//            System.out.println("NS do " + i + " : " + (int)basicBlocks.get(i).getNs());
        
        return basicBlocks;
        
    }
    
    
    
    
    /*
     * Create list of successors to each basic block
     */
    private static List<BasicBlock> createSuccList(List<String> code, List<BasicBlock> basicBlocks)
    {
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            BasicBlock basicBlock = basicBlocks.get(i);
            int id = basicBlock.getId();
            int posStart = basicBlock.getPosStart();
            int posEnd = basicBlock.getPosEnd();
            Instruction lastBasicBlockInstruction = DB.getInstruction(code.get(posEnd));

            if (DB.isBranch(lastBasicBlockInstruction))
            {
                //Successor basic block can be the next
                if (basicBlocks.size() > id + 1)
                {
                    basicBlock.addSuccBasicBlock(basicBlocks.get(id + 1));
                }
                
                //Or it can be the target of the branch
                List<String> target = DB.getTarget(code.get(posEnd), lastBasicBlockInstruction.getFormat());
                List<Integer> succPosStartList = new ArrayList<Integer>();
                
                //Look for posStart of the called basic block
                for (int j = 0; j < code.size(); j++)
                {
                    if (DB.isLabel(code.get(j)))
                    {
                        String label = DB.getLabel(code.get(j));
                        
                        for (int k = 0; k < target.size(); k++)
                        {
                            if (target.get(k).equals(label))
                            {
                                succPosStartList.add(j);
                                target.remove(k);
                            }
                        }
                    }
                }
                
                //Add id of the called basic block to succ list
                for (int j = 0; j < basicBlocks.size(); j++)
                {
                    if (succPosStartList.contains(basicBlocks.get(j).getPosStart()) && (!basicBlock.getSuccBasicBlocks().contains(basicBlocks.get(j))))
                    {
                        basicBlock.addSuccBasicBlock(basicBlocks.get(j));
                    }
                }
            }
            else if (DB.isJump(lastBasicBlockInstruction))
            {
                boolean isRegister = false;
                String register = new String();
                List<String> rsList = DB.getRS(code.get(posEnd), lastBasicBlockInstruction.getFormat());
                for (int j = 0; j < rsList.size(); j++)
                {
                    register = rsList.get(j);
                    if (DB.isRegister(register))
                    {
                        isRegister = true;
                        break;
                    }
                }
                
                if (posEnd == 558)
                {
                    System.out.println(code.get(posEnd));
                    System.out.println(lastBasicBlockInstruction.getFormat());
                    System.out.println(rsList);
                    //System.exit(0);
                }

                if (isRegister)
                {
                    List<Integer> posAllFunctionCalls = DB.getAllFunctionCallsPositions(code, register);
                    List<String> functionCallsList = getAll(code, posAllFunctionCalls);
                    System.out.println("XXX:\t"+functionCallsList);
                      //System.exit(0);
                    //Find subroutine name
                    int posLabel = DB.getPreviousLabelPosition(code, posStart);
                    String labelName = new String();
                    while (posLabel >= 0)
                    {
                        labelName = DB.getLabel(code.get(posLabel));
                        if (functionCallsList.contains(labelName))
                        {
                            break;
                        }
                        posLabel = DB.getPreviousLabelPosition(code, --posLabel);
                    }
                    //Find the subroutine's returns (calls to the subroutine plus one)
                    if (functionCallsList.contains(labelName))
                    {
                        List<Integer> posSubroutineReturns = new ArrayList<Integer>();
                        for (int j = 0; j < functionCallsList.size(); j++)
                        {
                            if (functionCallsList.get(j).equals(labelName))
                            {
                                posSubroutineReturns.add(posAllFunctionCalls.get(j)+1);
                            }
                        }
                        
                        //Add successors BBs
                        for (int j = 0; j < posSubroutineReturns.size(); j++)
                        {
                            for (int k = 0; k < basicBlocks.size(); k++)
                            {
                                BasicBlock bb = basicBlocks.get(k);
                                if ((bb.getPosStart() == posSubroutineReturns.get(j)) && !basicBlock.getSuccBasicBlocks().contains(bb))
                                {
                                    basicBlock.addSuccBasicBlock(bb);
                                }
                            }
                        }
                    }
                }
                else
                {
                    //The successor basic block is the target of the jump
                    List<String> target = DB.getTarget(code.get(posEnd), lastBasicBlockInstruction.getFormat());
                    List<Integer> succPosStartList = new ArrayList<Integer>();

                    //Look for posStart of the called basic block
                    for (int j = 0; j < code.size(); j++)
                    {
                        if (DB.isLabel(code.get(j)))
                        {
                            String label = DB.getLabel(code.get(j));

                            for (int k = 0; k < target.size(); k++)
                            {
                                if (target.get(k).equals(label))
                                {
                                    succPosStartList.add(j);
                                    target.remove(k);
                                }
                            }
                        }
                    }

                    //Add id of the called basic block to succ list
                    for (int j = 0; j < basicBlocks.size(); j++)
                    {
                        if (succPosStartList.contains(basicBlocks.get(j).getPosStart()) && (!basicBlock.getSuccBasicBlocks().contains(basicBlocks.get(j))))
                        {
                            basicBlock.addSuccBasicBlock(basicBlocks.get(j));
                        }
                    }
                }
            }   
            else
            {
                //Successor basic block is the next
                if (basicBlocks.size() > id + 1) 
                {
                    basicBlock.addSuccBasicBlock(basicBlocks.get(id + 1));
                }
            }
            
            //Update basic block
            basicBlocks.remove(i);
            basicBlocks.add(i, basicBlock);
        }
        
        return basicBlocks;
    }
    
    /*
     * Create list of predecessors to each basic block
     */
    private static List<BasicBlock> createPredList(List<BasicBlock> basicBlocks)
    {
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            for (int j = 0; j < basicBlocks.size(); j++)
            {
                for (int k = 0; k < basicBlocks.get(j).getSuccBasicBlocks().size(); k++)
                {
                    if (basicBlocks.get(j).getSuccBasicBlock(k).equals(basicBlocks.get(i)))
                    {
                        if (!basicBlocks.get(i).getPredBasicBlocks().contains(basicBlocks.get(j)))
                        {
                            basicBlocks.get(i).addPredBasicBlock(basicBlocks.get(j));
                            break;
                        }
                    }
                }
            }
        }
        
        return basicBlocks;
    }
    
    /*
     * Assign types to the basic blocks
     */
    private static List<BasicBlock> attributeType(List<BasicBlock> basicBlocks)
    {
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            if (basicBlocks.get(i).getPredBasicBlocks().size() > 1)
            {
                boolean typeA = false;
                for (int j = 0; (j < basicBlocks.get(i).getPredBasicBlocks().size()) && (!typeA); j++)
                {
                    if (basicBlocks.get(i).getPredBasicBlock(j).getSuccBasicBlocks().size() > 1)
                    {
                        typeA = true;
                    }
                }
                
                if (typeA)
                {
                    basicBlocks.get(i).setType('A');
                }
                else
                {
                    basicBlocks.get(i).setType('X');
                }
            }
            else
            {
                basicBlocks.get(i).setType('X');
            }
        }
        
        return basicBlocks;
    }
    
    /*
     * Create networks
     */
    private static List<Network> createNetworks(List<BasicBlock> basicBlocks)
    {
        List<Network> networks = new ArrayList<Network>();
        
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            /*
             * get basicBlocks.get(i).getPred()
             * look if there is a network that have a pred similar to at least one of the basicBlocks.get(i)
             * if it does, add bb to this network
             * else add the bb to a new network
             */
            //Find if the basic block should belong to one or more existing networks
            List<Integer> posList = findNetworkPosition(networks, basicBlocks.get(i));
            Network network = new Network();
            
            for (int j = posList.size() - 1; j >= 0; j--)
            {
                network.addBasicBlocks(networks.get(posList.get(j)).getBasicBlocks());
                networks.remove((int)(posList.get(j)));
            }
            
            network.addBasicBlock(basicBlocks.get(i));
            networks.add(network);
        }
        
        return networks;
    }
    
    private static List<Network> createPredNetwork(List<Network> networks)
    {
        for (int i = 0; i < networks.size(); i++)
        {
            List<BasicBlock> predBasicBlocks = networks.get(i).getPredBasicBlocks();
            Network predNetwork = new Network();
            predNetwork.addBasicBlocks(predBasicBlocks);
            networks.get(i).setPredNetwork(predNetwork);
        }
        
        return networks;
    }
    
    /*
     * Other functions
     */
    
    /*
     * Find the positions of the network that has basic block predecessors match with some of the predecessors of the network
     */
    private static List<Integer> findNetworkPosition(List<Network> networks, BasicBlock basicBlock)
    {
        List<Integer> posList = new ArrayList<Integer>();
        for (int i = 0; i < networks.size(); i++)
        {
            List<BasicBlock> predBasicBlocks = networks.get(i).getPredBasicBlocks();
            for (int j = 0; j < basicBlock.getPredBasicBlocks().size(); j++)
            {
                if (!posList.contains(i) && predBasicBlocks.contains(basicBlock.getPredBasicBlock(j)))
                {
                    posList.add(i);
                }
            }
        }
        
        return posList;
    }
    
    
    
    /*
    private static List<Integer> functionCallsPositions(List<String> code, String target, String register)
    {
        List<Integer> posFunctionCallList = new ArrayList<Integer>();
        
        for (int i = 0; i < code.size(); i++)
        {
            if (DB.isInstruction(code.get(i)))
            {
                Instruction instruction = DB.getInstruction(code.get(i));
                if (DB.isFunctionCall(instruction.getName()))
                {
                    String[] hiddenRD = instruction.getHiddenRD();
                    boolean hasHiddenRD = false;
                    for (int j = 0; j < hiddenRD.length; j++)
                    {
                        if (hiddenRD[j].equals(register))
                        {
                            hasHiddenRD = true;
                            break;
                        }
                    }
                    
                    List<String> targetList = DB.getTarget(code.get(i), instruction.getFormat());
                    for (int j = 0; (j < targetList.size()) && hasHiddenRD; j++)
                    {
                        if (targetList.get(j).equals(target))
                        {
                            posFunctionCallList.add(i);
                            break;
                        }
                    }
                }
            }
        }
        
        return posFunctionCallList;
    }
    */
    
    private static List<Integer> allTargetsToLabelPositions(List<String> code)
    {
        List<Integer> posAllTargetToLabelList = new ArrayList<Integer>();
        
        for (int i = 0; i < code.size(); i++)
        {
            if (DB.isInstruction(code.get(i)))
            {
                Instruction instruction = DB.getInstruction(code.get(i));
                if (DB.isJumpToTarget(instruction) || DB.isBranch(instruction))
                {
                    posAllTargetToLabelList.add(i);
                }
            }
        }
        
        return posAllTargetToLabelList;
    }
    
    /*
    private static List<Integer> targetsToLabelPositions(List<String> code, String label)
    {
        List<Integer> posTargetToLabelList = new ArrayList<Integer>();
        
        for (int i = 0; i < code.size(); i++)
        {
            if (DB.isInstruction(code.get(i)))
            {
                Instruction instruction = DB.getInstruction(code.get(i));
                if (DB.isJumpToTarget(instruction) || DB.isBranch(instruction))
                {
                    List<String> targetList = DB.getTarget(code.get(i), instruction.getFormat());
                    for (int j = 0; j < targetList.size(); j++)
                    {
                        if (targetList.get(j).equals(label))
                        {
                            posTargetToLabelList.add(i);
                            break;
                        }
                    }
                }
            }
        }
        
        return posTargetToLabelList;
    }
    */
    
    private static List<String> getAll(List<String> code, List<Integer> pos)
    {
        List<String> targets = new ArrayList<String>();
        
        for (int i = 0; i < pos.size(); i++)
        {
            if (DB.isInstruction(code.get(pos.get(i))))
            {
                Instruction instruction = DB.getInstruction(code.get(pos.get(i)));
                List<String> targetList = DB.getTarget(code.get(pos.get(i)), instruction.getFormat());
                if (!targetList.isEmpty())
                {
                    targets.add(targetList.get(0));
                }
                else
                {
                    targets.add("");
                }
            }
        }
        
        return targets;
    }
    
    private static List<Integer> positionsByLabel(List<String> functionCalls, List<Integer> pos, String label)
    {
        List<Integer> filteredPos = new ArrayList<Integer>();
        
        for (int i = 0; i < functionCalls.size(); i++)
        {
            if (functionCalls.get(i).equals(label))
            {
                filteredPos.add(pos.get(i));
            }
        }
        
        return filteredPos;
    }


    private static void setSignatures(List<Network> networks)
    {
        
        List<Network> nets = networks;
        int upperHalfSize = (int)Math.ceil(Math.log(nets.size() + 1)/Math.log(2));
        int nisLowerHalf = 0;
        int nesLowerHalf = 0;
        
        // sets the size of the higher parts and the method being used
        for (int netId = 0; netId < nets.size() ; netId++)
         {
            Network network = nets.get(netId);
            for(int j = 0; j < network.getBasicBlocks().size(); j++)
            {
                BasicBlock bb = network.getBasicBlock(j);
             
                bb.getHetaNis().setHigherSignSize(upperHalfSize);
                bb.getHetaNes().setHigherSignSize(upperHalfSize);
                bb.setMethod("HETA"); 
            } 
         }
         
         
         // NIS.Higher = Net's ID; NES.lh = rand(); 
         for (int netId = 0; netId < nets.size() ; netId++)
         {
            Network network = nets.get(netId);
            
            
            for(int j = 0; j < network.getBasicBlocks().size(); j++)
            {
                BasicBlock bb = network.getBasicBlock(j);             
                bb.getHetaNis().setHigherSign(netId);
                bb.getHetaNes().setHigherSign(nets.size() + 1); // those who point to nothing (to ghost network)
                bb.getHetaNes().setLowerSign((int)(Math.pow(2, (bb.getHetaNes().getSignatureLength() - upperHalfSize)) * Math.random()));
            }
            
         }
         
         
        
        // NES.uh = succesor's NIS.uh; 
        for(int i = 0; i < nets.size(); i++)
        {        
            List<BasicBlock> bblocks = nets.get(i).getBasicBlocks();  
                    
            for(int j = 0; j < bblocks.size(); j++)
            {     
                BasicBlock block = bblocks.get(j);
                
                for(int k = 0; k < block.getPredBasicBlocks().size(); k++)    
                {
                     BasicBlock predBlock = block.getPredBasicBlocks().get(k);
                     predBlock.getHetaNes().setHigherSign(block.getHetaNis().getHigherSign());
                }                    
            }
        }

  
        // NIS.lh = & of all father's NES.lh if type A and random if type X
         for (int netId = 0; netId < nets.size() ; netId++)
         {
            Network network = nets.get(netId);
            
            for(int j = 0; j < network.getBasicBlocks().size(); j++)
            {
                BasicBlock bb = network.getBasicBlock(j);             
        
                if (bb.getType() == 'A') // if type A does the AND operations
                {
                    for(int k = 0; k < bb.getPredBasicBlocks().size(); k++) 
                    {
                        BasicBlock pred = bb.getPredBasicBlocks().get(k);
                        if(k == 0)
                            nisLowerHalf = pred.getHetaNes().getLowerSign();
                            else
                                nisLowerHalf = nisLowerHalf & pred.getHetaNes().getLowerSign();   
                    }   
                    bb.getHetaNis().setLowerSign(nisLowerHalf);
                }
                    else // if type X, it's random
                        bb.getHetaNis().setLowerSign((int)(Math.pow(2, (bb.getHetaNis().getSignatureLength() - upperHalfSize)) * Math.random()));  
            }
         }
         
         
         //if a block of type X has more than 1 father, all of it's fathers should have the same NES:
         for (int netId = 0; netId < nets.size() ; netId++)
         {
            Network network = nets.get(netId);
            
            for(int j = 0; j < network.getBasicBlocks().size(); j++)
            {
                BasicBlock bb = network.getBasicBlock(j);             
        
                if (bb.getType() == 'X') // if type A does the AND operations
                {
                    if(bb.getPredBasicBlocks().size() > 1)
                    {
                       nesLowerHalf = bb.getPredBasicBlocks().get(0).getHetaNes().getLowerSign();
                       for(int k = 0; k < bb.getPredBasicBlocks().size(); k++) 
                       {
                           BasicBlock pred = bb.getPredBasicBlocks().get(k);
                           pred.getHetaNes().setLowerSign(nesLowerHalf);
                       }   
                    }
                }  
            }
         }
        
   
    }


    private static List<String> insertSignatures(List<String> code, List<BasicBlock> basicBlocks)
    {
        //Check if there is any available register
        List<String> usedRegisters = DB.getUsedRegisters(code);
        List<String> freeRegisters = Util.complement(DB.getGeneralPurposeRegisters(), usedRegisters);
        freeRegisters.removeAll(DB.getPreInitializedRegisters());
        if (freeRegisters.isEmpty()) return code;
        
        //Use first available register as signature register
        List<String> signatureRegister = new ArrayList<String>();
        signatureRegister.add(freeRegisters.get(0));
        
        List<String> zeroRegister = new ArrayList<String>();
        zeroRegister.add(DB.getRegisterZero().getName());
        
        //Output registers
        List<String> outputRegisters = new ArrayList<String>();
        outputRegisters.add(signatureRegister.get(0));
        outputRegisters.add(DB.getRegisterZero().getName());
        
        //Memory offset
        List<String> offset = new ArrayList<String>();
        offset.add(String.valueOf(DB.getHetaOffset()));

        //Code out
        List<String> codeOut = new ArrayList<String>();
        
        //Instructions and useful stuff
        Instruction xor = DB.getXorImmediateInstruction();
        Instruction and = DB.getAndImmediateInstruction();
        Instruction store = DB.getStoreInstruction();
        Instruction li = DB.getImmediateAssignmentInstruction();
        Instruction branchNotEqual = DB.getNonEqualityComparisonInstruction();
        Instruction nop = DB.getNoOperationInstruction();
        List<String> emptyList = new ArrayList<String>();
        List<String> constant;
        List<String> errorLabelName = new ArrayList<String>();
        errorLabelName.add(DB.ERROR_LABEL_NAME);
        
        int previousPosEnd = -1;
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            //Get information about the current basic block
            BasicBlock bb = basicBlocks.get(i);
            int posStart = bb.getPosStart();
            int posEnd = bb.getPosEnd();
            //System.out.println(posStart + " : " + posEnd + "\t\"" + code.get(posStart).trim() + "\":\"" + code.get(posEnd).trim() + "\"");
            
            //Insert instructions that do not belong to any basic block
            for (previousPosEnd++; previousPosEnd < posStart; previousPosEnd++)
            {
                codeOut.add(code.get(previousPosEnd));
            }
            previousPosEnd = posEnd;
            
            //Get first instruction line after posStart (skip labels and other useless things)
            while (!DB.isInstruction(code.get(posStart)))
            {
                codeOut.add(code.get(posStart++));
            }
            
            //Add NIS and NS
            if (bb.getType() == 'X')
            {
                /*
                 * NIS' xor and NS' xor can be combined to optimize code
                 * constant = constantNIS xor constantNS
                 * globalRegister = globalRegister xor constant
                */ 
                constant = new ArrayList<String>();
                int invariant = bb.getNisConstant() ^ bb.getNsConstant();
                constant.add(String.valueOf(invariant));
                codeOut.add(Util.generateCommand(xor.getName(), signatureRegister, signatureRegister, constant, emptyList, emptyList, xor.getFormat()));
            }
            else
            {
                /*
                 * NIS' add and NS' xor can NOT be combined
                 * constant = constantNIS and constantNS
                 * globalRegister = globalRegister xor constant
                */ 
                constant = new ArrayList<String>();
                constant.add(String.valueOf(bb.getNisConstant()));
                codeOut.add(Util.generateCommand(and.getName(), signatureRegister, signatureRegister, constant, emptyList, emptyList, and.getFormat()));
                
                constant = new ArrayList<String>();
                constant.add(String.valueOf(bb.getNsConstant()));
                codeOut.add(Util.generateCommand(xor.getName(), signatureRegister, signatureRegister, constant, emptyList, emptyList, xor.getFormat()));
            }
            
            //Copy basic block code
            for (int j = posStart; j < posEnd; j++)
            {
                codeOut.add(code.get(j));
            }
            
            Instruction lastInstruction = DB.getInstruction(code.get(posEnd));
            boolean isJumpOrBranch = DB.isJump(lastInstruction) || DB.isBranch(lastInstruction);
            
            //Add last instruction before NES' xor if it is not a jump or a branch
            if (!isJumpOrBranch) codeOut.add(code.get(posEnd));
            
            //Add memory sync operation
            codeOut.add(Util.generateCommand(store.getName(), emptyList, outputRegisters, emptyList, offset, emptyList, store.getFormat()));
            
            //Add NES' xor
            constant = new ArrayList<String>();
            constant.add(String.valueOf(bb.getNesConstant()));
            codeOut.add(Util.generateCommand(xor.getName(), signatureRegister, signatureRegister, constant, emptyList, emptyList, xor.getFormat()));
            
            //Add last instruction after NES' xor if it is a jump or a branch
            if (isJumpOrBranch) codeOut.add(code.get(posEnd));
            
            //System.out.println();
        }
        
        //Add instructions after last basic block
        if (!basicBlocks.isEmpty())
        {
            int lastBBPosEnd = basicBlocks.get(basicBlocks.size()-1).getPosEnd();
            for (int i = lastBBPosEnd+1; i < code.size(); i++)
            {
                codeOut.add(code.get(i));
            }
        }
        return codeOut;
    }

    private static void showInformation(List<BasicBlock> basicBlocks, List<Network> networks)
    {
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            String text = "ID:\t" + basicBlocks.get(i).getId() + "\tSTART: " + (basicBlocks.get(i).getPosStart()+1) + "\tEND: " + (basicBlocks.get(i).getPosEnd()+1) + "\tTYPE: " + basicBlocks.get(i).getType() + "\t\tSUCCs: ";
            for (int j = 0; j < basicBlocks.get(i).getSuccBasicBlocks().size(); j++)
            {
                text += basicBlocks.get(i).getSuccBasicBlockId(j) + ",";
            }
            if (text.charAt(text.length()-1) == ',')
            {
                text = text.substring(0, text.length()-1);
            }
            else
            {
                text += "\t";
            }
            text += "\tPREDs: ";
            for (int j = 0; j < basicBlocks.get(i).getPredBasicBlocks().size(); j++)
            {
                text += basicBlocks.get(i).getPredBasicBlockId(j) + ",";
            }
            if (text.charAt(text.length()-1) == ',')
            {
                text = text.substring(0, text.length()-1);
            }
            else
            {
                text += "\t";
            }
            text += "\tNetwork: ";
            for (int j = 0; j < networks.size(); j++)
            {
                if (networks.get(j).contains(basicBlocks.get(i)))
                {
                    text += j + ",";
                }
            }
            text = text.substring(0, text.length()-1) + "\tNS: " + basicBlocks.get(i).getNs() + " (" + basicBlocks.get(i).getNsConstant() + ")" + "\tNIS: " + basicBlocks.get(i).getNis() + " (" + basicBlocks.get(i).getNisConstant() + ")" + "\tNES: " + basicBlocks.get(i).getNes() + " (" + basicBlocks.get(i).getNesConstant() + ")";
            System.out.println(text);
        }
        
        for (int i = 0; i < networks.size(); i++)
        {
            String text = "Network: " + i + "\tPredNetworkBBs: ";
            
            for (int j = 0; j < networks.get(i).getPredNetwork().getBasicBlocks().size(); j++)
            {
                text += networks.get(i).getPredNetwork().getBasicBlock(j).getId() + ",";
            }
            text = text.substring(0, text.length() - 1);
            
            System.out.println(text);
        }
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
            if (DB.isInstruction(line))
            {
                if (callsToSubroutines.contains(DB.extractInstruction(line)))
                {
                    codeOut.add(Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                }
            }
            codeOut.add(code.get(i));
        }
        
        return codeOut;
    }
}
