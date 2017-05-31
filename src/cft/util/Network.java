/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class Network
{
    private List<BasicBlock> basicBlocks = new ArrayList<BasicBlock>();
    private Network predNetwork = null;
    
    public Network()
    {
        this.basicBlocks = new ArrayList<BasicBlock>();
        this.predNetwork = null;
    }
    
    /*
     * Gets size of the Network
     */
    public int getSize()
    {
        return basicBlocks.size();
    }
    /*
     * 
     */
    
    
    
    public void addBasicBlock(BasicBlock basicBlock)
    {
        this.basicBlocks.add(basicBlock);
    }
    
    public void addBasicBlocks(List<BasicBlock> basicBlocks)
    {
        this.basicBlocks.addAll(basicBlocks);
    }
    
    public List<BasicBlock> getBasicBlocks()
    {
        return this.basicBlocks;
    }
    
    public boolean contains(BasicBlock basicBlock)
    {
        if (basicBlocks.contains(basicBlock))
        {
            return true;
        }
        
        return false;
    }
    
    public boolean contains(int id)
    {
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            if (basicBlocks.get(i).getId() == id)
            {
                return true;
            }
        }
        
        return false;
    }
    
    public BasicBlock getBasicBlock(int index)
    {
        return this.basicBlocks.get(index);
    }
    
    public List<BasicBlock> getPredBasicBlocks()
    {
        List<BasicBlock> predBasicBlocks = new ArrayList<BasicBlock>();
        
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            List<BasicBlock> basicBlockPred = basicBlocks.get(i).getPredBasicBlocks();
            
            for (int j = 0; j < basicBlockPred.size(); j++)
            {
                if (!predBasicBlocks.contains(basicBlockPred.get(j)) && !basicBlocks.contains(basicBlockPred.get(j)))
                {
                    predBasicBlocks.add(basicBlockPred.get(j));
                }
            }   
        }
        
        return predBasicBlocks;
    }
    
    /*public List<Integer> getPredBasicBlocksIds()
    {
        List<Integer> predBasicBlocksIds = new ArrayList<Integer>();
        List<Integer> 
        
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            List<Integer> basicBlockPredIds = basicBlocks.get(i).getPredBasicBlocksIds();
            
            for (int j = 0; j < basicBlockPredIds.size(); j++)
            {
                if (!predBasicBlocksIds.contains(basicBlockPredIds.get(j)) && !basicBlocks.contains(basicBlockPred.get(j)))
                {
                    predBasicBlocksIds.add(basicBlockPredIds.get(j));
                }
            }   
        }
        
        return predBasicBlocksIds;
    }*/
    
    //get the basic blocks that not contain basic block "id" as predecessor
   /*public List<BasicBlock> getNonSuccBasicBlocks(int id)
    {
        List<BasicBlock> nonSuccBasicBlocks = new ArrayList<BasicBlock>();
        
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            if (!basicBlocks.get(i).getPred().contains(id))
            {
                nonSuccBasicBlocks.add(basicBlocks.get(i));
            }
        }
        
        return nonSuccBasicBlocks;
    }
    
    public List<Integer> getNonSuccBasicBlocksID(int id)
    {
        List<Integer> nonSuccBasicBlocksID = new ArrayList<Integer>();
        
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            if (!basicBlocks.get(i).getPred().contains(id))
            {
                nonSuccBasicBlocksID.add(basicBlocks.get(i).getId());
            }
        }
        
        return nonSuccBasicBlocksID;
    }*/
    
    public Network getPredNetwork()
    {
        return predNetwork;
    }
    
    public void setPredNetwork(Network predNetwork)
    {
        this.predNetwork = predNetwork;
    }
    
}
