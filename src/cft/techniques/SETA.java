/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.techniques;

import cft.config.DB;
import cft.util.BasicBlock;
import cft.util.BasicBlockConnectionsComparator;
import cft.util.BasicBlockPredsComparator;
import cft.util.BasicBlockSizeComparator;
import cft.util.BasicBlockSuccsComparator;
import cft.util.Instruction;
import cft.util.Network;
import cft.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Eduardo, Gennaro
 */
public class SETA
{
    private static final int minNumberOfInstructions = DB.getSetaMinNumberOfInstructions();
    private static final double tunnelEffectPercentage = DB.getSetaTunnelEffectPercentage();
    private static final int SYSTEM_BB_ID = BasicBlock.SYSTEM_BB_ID; //do not change. It has to be zero!
    private static boolean firstBBHasLoop = false;
    
    public static String apply(String fileNameIn) throws IOException 
    {
        String fileNameOut = Util.getFileTitle(fileNameIn) + "SETA." + Util.getFileExtension(fileNameIn);
        apply(fileNameIn, fileNameOut);
        
        return fileNameOut;
    }
    
    public static void apply(String fileNameIn, String fileNameOut) throws IOException
    {
        //Get the code
        List<String> code = Util.getCode(fileNameIn, DB.getCommentTag());
        List<String> AuxCode = Util.getCode(fileNameIn, DB.getCommentTag());
        
        //Create Basic Blocks
        //List<BasicBlock> basicBlocks = createBasicBlocks(code);
        List<BasicBlock> basicBlocks = BasicBlock.createBasicBlocks(code);
        
        //Create list of successors of each basic block
        basicBlocks = createSuccList(code, basicBlocks);
        
        //Create list of predecessors of each basic block
        basicBlocks = createPredList(basicBlocks);
        
        //Sets the number of instructions on each basic block
        basicBlocks = setNumberOfInstructions(basicBlocks, code);

        //Set the basic blocks that must be protected based on their number of instructions
        basicBlocks = defineProtectStatus(code, basicBlocks);
        
        //Rearrange the precedessor and successor lists taking into account the protect status
        basicBlocks = rearrangePredAndSuccLists(basicBlocks);
        
        //Include ghost BB (BB0) if necessary
        basicBlocks = includeGhostBasicBlock(basicBlocks);
        //firstBBHasLoop = verifyLoop(basicBlocks);
        
        //Atribute type 'A' or 'X' to the basic blocks
        basicBlocks = attributeType(basicBlocks);
        
        //Define BBs that will have a checker
        basicBlocks = defineVerifyStatus(basicBlocks);
        
        //Create list of networks. Each network has one or more basic blocks and each basic block belong to one and only one network.
        List<Network> networks = createNetworks(basicBlocks);
        
        //Create predecessor network to each network if it has one
        networks = createPredNetwork(networks);
        
        //Generates Signatures
        setSignatures(networks);
        
        //Show information
        System.out.println("Final:");        
        showInformation(basicBlocks, networks);
        
        //Insert signatures in the program's source code
        code = insertSignatures(code, basicBlocks);
        
        //Insert NOP before calls to subroutines to avoid double execution of NES' XORs
        code = (DB.getSetaInsertNops()?insertNoOperationBeforeFunctionCalls(code):code);
        
        //Insert NOP after branches and jumps
        code = (DB.getSetaInsertNopsAfterBranching()?insertNoOperationAfterBranching(code):code);
                
        //Write the code to a new file
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
        
        if (basicBlockStarts.get(basicBlockStarts.size()-1) > basicBlockEnds.get(basicBlockEnds.size()-1))
        {
            basicBlockEnds.add(code.size()-1);
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
        
        //Create basic blocks
        List<BasicBlock> basicBlocks = new ArrayList<BasicBlock>();
        for (int i = 0; i < basicBlockStarts.size(); i++)
        {
            BasicBlock basicBlock = new BasicBlock(i, basicBlockStarts.get(i), basicBlockEnds.get(i));
            basicBlocks.add(basicBlock);
        }
        
        //Define startingBBs -- at this point it is always and only the BB starting with the label "main"
        int posMain = DB.getMainPosition(code);
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            if (posMain == basicBlocks.get(i).getPosStart())
            {
                basicBlocks.get(i).setStartingBB(true);
            }
        }
        
        return basicBlocks;
    }
    
    /*
     * Create list of successors to each basic block
     */
    private static List<BasicBlock> createSuccList(List<String> code, List<BasicBlock> basicBlocks)
    {
        for (int i = (firstBBHasLoop?1:0); i < basicBlocks.size(); i++)
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
                System.out.println(target);
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
                                System.out.println(label);
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
                
                if (isRegister)
                {
                    List<Integer> posAllFunctionCalls = DB.getAllFunctionCallsPositions(code, register);
                    List<String> functionCallsList = getAll(code, posAllFunctionCalls);
                    
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
        
        for (int i = basicBlocks.size()-1; i >= 0; i--)
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
    
    
    private static void setSignatures(List<Network> networks)
    {
        
        List<Network> nets = networks;
        int upperHalfSize = (int)Math.ceil(Math.log(nets.size() + 1)/Math.log(2));
        int nisLowerHalf = 0;
        int nesLowerHalf;
        
        // sets the size of the higher parts
        for (int netId = 0; netId < nets.size() ; netId++)
        {
            Network network = nets.get(netId);
            for(int j = 0; j < network.getBasicBlocks().size(); j++)
            {
                BasicBlock bb = network.getBasicBlock(j);
             
                bb.getHetaNis().setHigherSignSize(upperHalfSize);
                bb.getHetaNes().setHigherSignSize(upperHalfSize);
                bb.setMethod("SETA");
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
                bb.getHetaNes().setHigherSign(nets.size()); // those who point to nothing (to ghost network)
                //bb.getHetaNes().setHigherSign(nets.size() + 1); // those who point to nothing (to ghost network)
                //mudei porque os números válidos vão de 0 a N e não de a N+1.
                
                int n = (int)(Math.pow(2, (bb.getHetaNes().getSignatureLength() - upperHalfSize)) * Math.random());
                bb.getHetaNes().setLowerSign(n);
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
                {
                    int n = (int)(Math.pow(2, (bb.getHetaNis().getSignatureLength() - upperHalfSize)) * Math.random());
                    bb.getHetaNis().setLowerSign(n);
                }  
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
        if ((freeRegisters.size() < 1) || (!DB.hasComparisonWithImmediateInstruction() && (freeRegisters.size() < 2)))
        {
            return code;
        }
        
        //Use first available register as signature register
        List<String> signatureRegister = new ArrayList<String>();
        signatureRegister.add(freeRegisters.get(0));
        
        List<String> auxRegister = new ArrayList<String>();
        if (freeRegisters.size() >= 2) auxRegister.add(freeRegisters.get(1));
        
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
        Instruction comparision = DB.getComparisonInstruction();
        Instruction comparisionWithImmediate = DB.getComparisonWithImmediateInstruction();
        Instruction nop = DB.getNoOperationInstruction();
        List<String> emptyList = new ArrayList<String>();
        List<String> constant;
        List<String> errorLabelName = new ArrayList<String>();
        errorLabelName.add(DB.ERROR_LABEL_NAME);
        
        int previousPosEnd = -1;
        for (int i = (firstBBHasLoop?1:0); i < basicBlocks.size(); i++)
        {
            //Get information about the current basic block
            BasicBlock bb = basicBlocks.get(i);
            int posStart = bb.getPosStart();
            int posEnd = bb.getPosEnd();
            
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
            if (bb.getProtect())
            {
                if (bb.getType() == 'X')
                {
                    /*
                     * NIS' xor and NS' xor can be combined to optimize code
                     * constant = constantNIS xor constantNS
                     * globalRegister = globalRegister xor constant
                    */ 
                    constant = new ArrayList<String>();
                    int invariant = bb.getNisConstant();
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
                }
            }
            
            //Copy basic block code
            String line = code.get(posEnd);
            int posBreak = posEnd;
            if (DB.isInstruction(line))
            {
                Instruction instruction = DB.getInstruction(line);
                if (DB.isBranch(instruction) && !(DB.getTarget(line, instruction.getFormat()).get(0).equals(DB.ERROR_LABEL_NAME)))
                {
                    if (DB.hasComparisonWithImmediateInstruction() || DB.hasComparisonInstruction())
                    {
                        for (posBreak = posEnd - 1; posBreak >= posStart; posBreak--)
                        {
                            line = code.get(posBreak);
                            if (DB.isInstruction(line))
                            {
                                if (DB.isComparisonInstruction(instruction))
                                {
                                    break;
                                }
                            }
                        }
                        
                        if (posBreak < posStart) posBreak = posEnd - 2;
                    }
                    else
                    {
                        posBreak = posEnd - 1;
                    }
                }
                else if (DB.isJump(instruction))
                {
                    posBreak = posEnd - 1;
                }
            }
            int posStop = 0;
            for (posStop = posStart; posStop <= posBreak; posStop++)
            {
                codeOut.add(code.get(posStop));
            }
            
            //Software error detection
            if (bb.getProtect() && bb.getVerify())
            {
                List<String> immList = new ArrayList<String>();
                immList.add(String.valueOf(bb.getNis()));
                List<String> rsList = new ArrayList<String>();
                rsList.add(signatureRegister.get(0));
                
                if (DB.hasComparisonWithImmediateInstruction())
                {
                    codeOut.add(Util.generateCommand(comparisionWithImmediate.getName(), emptyList, rsList, immList, emptyList, emptyList, comparisionWithImmediate.getFormat()));
                    codeOut.add(Util.generateCommand(branchNotEqual.getName(), emptyList, emptyList, emptyList, emptyList, errorLabelName, branchNotEqual.getFormat()));
                }
                else if (DB.hasComparisonInstruction())
                {
                    codeOut.add(Util.generateCommand(li.getName(), auxRegister, emptyList, immList, emptyList, emptyList, li.getFormat()));
                    rsList.add(auxRegister.get(0));
                    codeOut.add(Util.generateCommand(comparision.getName(), emptyList, rsList, emptyList, emptyList, emptyList, comparision.getFormat()));
                    codeOut.add(Util.generateCommand(branchNotEqual.getName(), emptyList, emptyList, emptyList, emptyList, errorLabelName, branchNotEqual.getFormat()));
                }
                else
                {
                    codeOut.add(Util.generateCommand(li.getName(), auxRegister, emptyList, immList, emptyList, emptyList, li.getFormat()));
                    rsList.add(auxRegister.get(0));
                    codeOut.add(Util.generateCommand(branchNotEqual.getName(), emptyList, rsList, emptyList, emptyList, errorLabelName, branchNotEqual.getFormat()));
                }
            }
            //NOP?
            
            //Add NES' xor
            if (bb.getProtect())
            {
                constant = new ArrayList<String>();
                constant.add(String.valueOf(bb.getNesConstant()));
                codeOut.add(Util.generateCommand(xor.getName(), signatureRegister, signatureRegister, constant, emptyList, emptyList, xor.getFormat()));
            }
            
            //Add last instructions after NES' xor if it is a jump, a branch or a comparison
            for (; posStop <= posEnd; posStop++)
            {
                codeOut.add(code.get(posStop));
            }
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
        
        //Add initialization for signature register when firstBBHasLoop
        if (firstBBHasLoop)
        {
            int position = DB.getMainPosition(codeOut);
            List<String> immList = new ArrayList<String>();
            immList.add(String.valueOf(basicBlocks.get(0).getNes()));
            List<String> rdList = new ArrayList<String>();
            rdList.add(signatureRegister.get(0));
            codeOut.add(++position, Util.generateCommand(li.getName(), rdList, emptyList, immList, emptyList, emptyList, li.getFormat()));
        }
        
        
        return codeOut;
    }

    private static void showInformation(List<BasicBlock> basicBlocks, List<Network> networks)
    {
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            String text = "ID:\t" + basicBlocks.get(i).getId() + "\tSTART: " +
                    (basicBlocks.get(i).getPosStart()+1) + "\tEND: " + 
                    (basicBlocks.get(i).getPosEnd()+1) + "\tNINST: " + 
                    (basicBlocks.get(i).getNumInstructions()) + "\tTYPE: " + 
                    basicBlocks.get(i).getType() + "\t\tSUCCs: ";
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
            text = text.substring(0, text.length()-1) + "\tNS: " + basicBlocks.get(i).getNs() + 
                    " (" + basicBlocks.get(i).getNsConstant() + ")" + "\tNIS: " + basicBlocks.get(i).getNis() + 
                    " (" + basicBlocks.get(i).getNisConstant() + ")" + "\tNES: " + basicBlocks.get(i).getNes() + 
                    " (" + basicBlocks.get(i).getNesConstant() + ")" + "\tVerify: " + basicBlocks.get(i).getVerify() + 
                    "\tProtect: " + basicBlocks.get(i).getProtect() + "\tStartingBB: " + basicBlocks.get(i).isStartingBB();
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
    
    private static List<String> insertNoOperationAfterBranching(List<String> code)
    {
        List<String> codeOut = new ArrayList<String>();
        List<String> emptyList = new ArrayList<String>();
        Instruction nop = DB.getNoOperationInstruction();
        for (int i = 0; i < code.size(); i++)
        {
            codeOut.add(code.get(i));
            
            String line = code.get(i);
            if (DB.isInstruction(line))
            {
                Instruction instruction = DB.getInstruction(line);
                List<String> target = DB.getTarget(code.get(i), instruction.getFormat());
                if ((DB.isBranch(instruction) || DB.isJump(instruction)) && !target.get(0).equalsIgnoreCase(DB.ERROR_LABEL_NAME))
                {
                    codeOut.add(Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                }
            }
        }
        
        return codeOut;
    }


    public static List<BasicBlock> setNumberOfInstructions(List<BasicBlock> basicBlocks, List<String> code)
    {

        for (int i = 0; i < basicBlocks.size(); i++)
        {
            
            BasicBlock bb = basicBlocks.get(i);
            
            int numInstructions = 0;
            
            for (int j = bb.getPosStart(); j <= bb.getPosEnd(); j++)
            {
                if (DB.isInstruction(code.get(j)))
                    numInstructions ++;
            }
            
            bb.setNumInstructions(numInstructions);
            
        }
        
        return basicBlocks;
    }

    public static List<BasicBlock> defineProtectStatus(List<String> code, List<BasicBlock> basicBlocks)
    {
        //NOTE: Protect Status is only based on the BB's size. Improvement for more flexibility may be necessary.
        if (DB.getSetaTunnelEffectSelectionByPercentage())
        {
            System.out.println("Tunnel Effect Percentage: " + tunnelEffectPercentage);
            Object bbArray[] = basicBlocks.toArray();
            
            for (int i = 0; i < bbArray.length - 1; i++)
            {
                BasicBlock bbi = (BasicBlock) bbArray[i];
                int pos = i;
                for (int j = i + 1; j < bbArray.length; j++)
                {
                    BasicBlock bbj = (BasicBlock) bbArray[j];
                    if (bbj.getNumInstructions() > bbi.getNumInstructions()) pos = j;
                }
                
                bbArray[i] = bbArray[pos];
                bbArray[pos] = bbi;
            }
            
            int limit = (int) (tunnelEffectPercentage * basicBlocks.size());
            System.out.println("Limit: "+limit);
            for (int i = limit; i < bbArray.length; i++)
            {
                BasicBlock bb = (BasicBlock) bbArray[i];
                bb.setProtect(false);
            }
        }
        else
        {
            System.out.println("Tunnel Effect MinSize: " + minNumberOfInstructions);
            for (int i = 0; i < basicBlocks.size(); i++)
            {
                BasicBlock bb = basicBlocks.get(i);
                if (bb.getNumInstructions() < minNumberOfInstructions) bb.setProtect(false);
            }
        }
        
        return basicBlocks;
    }
    
    public static List<BasicBlock> defineVerifyStatus(List<BasicBlock> basicBlocks)
    {
        String setaPriorityMethod = DB.getSetaPriorityMethod(); //can be "successor", "predecessor", "both" or "size"
        Double percentage = DB.getSetaPercentageToVerify(); //the percentage of verified BBs has to be the maximum possible that does not exceed the set value
        
        List<BasicBlock> mostSizeList = new ArrayList<BasicBlock>(); // list of BasicBlocks w/ descending order by number of instructions;
        List<BasicBlock> mostSuccsList = new ArrayList<BasicBlock>(); // list of BasicBlocks w/ descending order of successors;
        List<BasicBlock> mostPredsList = new ArrayList<BasicBlock>(); // list of BasicBlocks w/ descending order of predecessors;
        List<BasicBlock> mostConnectionsList = new ArrayList<BasicBlock>(); //list of BasicBlocks w/ descending order of connections;
        
        List<BasicBlock> base = new ArrayList<BasicBlock>();
        base.addAll(basicBlocks);
        
        mostSizeList.addAll(base);
        mostPredsList.addAll(base);
        mostSuccsList.addAll(base);
        mostConnectionsList.addAll(base);
        Collections.sort(mostSizeList, new BasicBlockSizeComparator());
        Collections.sort(mostPredsList, new BasicBlockPredsComparator()); // orders by number of predecessors (descending)
        Collections.sort(mostSuccsList, new BasicBlockSuccsComparator()); // orders by number of successors (descending)
        Collections.sort(mostConnectionsList, new BasicBlockConnectionsComparator());
        
        int numVerifiedBBs = (int) (percentage * mostPredsList.size());
        if (setaPriorityMethod.equalsIgnoreCase("size"))
        {
            for (int i = 0; i < numVerifiedBBs; i++) mostPredsList.get(i).setVerify(true);
        }
        else if (setaPriorityMethod.equalsIgnoreCase("predecessor")) {
            for (int i = 0; i < numVerifiedBBs; i++) {
                mostPredsList.get(i).setVerify(true);
            }
        } else if (setaPriorityMethod.equalsIgnoreCase("successor")) {
            for (int i = 0; i < numVerifiedBBs; i++) {
                mostSuccsList.get(i).setVerify(true);
            }
        } else if (setaPriorityMethod.equalsIgnoreCase("first")) {
            for (int i = 1; i < numVerifiedBBs; i++) {
                basicBlocks.get(i).setVerify(true);
            }
        } else if (setaPriorityMethod.equalsIgnoreCase("last")) {
            for (int i = 0; i < numVerifiedBBs; i++) {
                basicBlocks.get(basicBlocks.size()-1-i).setVerify(true);
            }
        } else { /* Default Method: both */
            for (int i = 0; i < numVerifiedBBs; i++) {
                mostConnectionsList.get(i).setVerify(true);
            }
        }
        
        return basicBlocks;
    }
    
    // returns number of BBs that need to be protect
    public static int getNumBlocksToProtect(List<BasicBlock> basicBlocks)
    {
        int num = 0;

        for (int i = 0; i < basicBlocks.size(); i++) 
        {
            BasicBlock bb = basicBlocks.get(i);
            if (bb.getProtect()) {
                num++;
            }
        }
        return num;
    }
    
    public static List<BasicBlock> rearrangePredAndSuccLists(List<BasicBlock> basicBlocks)
    {
        System.out.println("\nDefault:");
        showInformation(basicBlocks, new ArrayList<Network>());
        
        for (BasicBlock bb : basicBlocks)
        {
            if (bb.getProtect())
            {
                //Tunnel effect to predecessors
                List<BasicBlock> visited = new ArrayList<BasicBlock>();
                for (int i = 0; i < bb.getPredBasicBlocks().size(); i++)
                {
                    /*
                        Add predecessors of a protect false predecessor (predBB) to predecessor list
                        Remove predBB from predList
                        Mark predBB as visited
                    */
                    BasicBlock predBB = bb.getPredBasicBlock(i);
                    if (!predBB.getProtect())
                    {
                        visited.add(predBB);
                        for (BasicBlock predPredBB : predBB.getPredBasicBlocks())
                        {
                            if (!visited.contains(predPredBB)) bb.addPredBasicBlock(predPredBB);
                        }
                        if (predBB.isStartingBB()) bb.setStartingBB(true);
                        bb.removePredBasicBlock(predBB);
                        i = -1;
                    }
                }
                
                //Tunnel effect to successors
                visited.clear();
                for (int i = 0; i < bb.getSuccBasicBlocks().size(); i++)
                {
                    /*
                        Add predecessors of a protect false predecessor (predBB) to predecessor list
                        Remove predBB from predList
                        Mark predBB as visited
                    */
                    BasicBlock succBB = bb.getSuccBasicBlock(i);
                    if (!succBB.getProtect())
                    {
                        visited.add(succBB);
                        for (BasicBlock succSuccBB : succBB.getSuccBasicBlocks())
                        {
                            if (!visited.contains(succSuccBB)) bb.addSuccBasicBlock(succSuccBB);
                        }
                        bb.removeSuccBasicBlock(succBB);
                        i = -1;
                    }
                }
            }
        }
        System.out.println();
        showInformation(basicBlocks, new ArrayList<Network>());
        System.out.println();
        
        //Remove BBs that wont be protected from SUCC and PRED lists
        for (BasicBlock bb : basicBlocks)
        {
            if (bb.getProtect())
            {
                for (int j = 0; j < bb.getPredBasicBlocks().size(); j++)
                {
                    BasicBlock bbIn = bb.getPredBasicBlocks().get(j);
                    if (!bbIn.getProtect())
                    {
                        bb.removePredBasicBlock(bbIn);
                        j--;
                    }
                }
                
                for (int j = 0; j < bb.getSuccBasicBlocks().size(); j++)
                {
                    BasicBlock bbIn = bb.getSuccBasicBlocks().get(j);
                    if (!bbIn.getProtect())
                    {
                        bb.removeSuccBasicBlock(bbIn);
                        j--;
                    }
                }
            }
            else
            {
                bb.clearPredBasicBlocks();
                bb.clearSuccBasicBlocks();
                bb.setStartingBB(false);
            }
        }
        
        return basicBlocks;
    }
    
    //Include Ghost BB if necessary
    private static List<BasicBlock> includeGhostBasicBlock(List<BasicBlock> basicBlocks)
    {
        firstBBHasLoop = verifyLoop(basicBlocks);
        if (firstBBHasLoop)
        {
            /*
                Create GhostBB
                Include startingBBs as GhostBB' successors
                Include GhostBB as Ghost' sucessors predecessors
                Update their IDs
            */
             
            BasicBlock ghostBB = new BasicBlock(SYSTEM_BB_ID, -1, -2);
            basicBlocks.add(0, ghostBB);
            
            for (int i = 1; i < basicBlocks.size(); i++)
            {
                BasicBlock bb = basicBlocks.get(i);
                bb.setId(i);
                if (bb.isStartingBB())
                {
                    ghostBB.addSuccBasicBlock(bb);
                    bb.addPredBasicBlock(ghostBB);
                }
            }
        }
        
        return basicBlocks;
    }
    
    // Verifies if a basic block (of index 0 in the BB list) has a loop
    private static boolean verifyLoop(List<BasicBlock> basicBlocks) 
    {
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            BasicBlock bb = basicBlocks.get(i);
            if (bb.isStartingBB() && bb.getPredBasicBlocks().contains(bb))
            {
                return true;
            }
        }
        
        return false;
        //return true;
    }
    
    //------------------------------------------------------------------------//
    /* Obsolete Methods */
    
    //Sets the signatures of the new Init BB
    private static void defineInitBlockSignatures(List<BasicBlock> basicBlocks) 
    {
        BasicBlock SuccBB;
        SuccBB = basicBlocks.get(SYSTEM_BB_ID).getSuccBasicBlock(0);
        
       if (firstBBHasLoop)
           if(SuccBB.getType() == 'A')
               basicBlocks.get(SYSTEM_BB_ID).getHetaNes().setSign(SuccBB.getNis());
           else
               basicBlocks.get(SYSTEM_BB_ID).getHetaNes().setSign(SuccBB.getNis() ^ SuccBB.getNisConstant());

       
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
}

