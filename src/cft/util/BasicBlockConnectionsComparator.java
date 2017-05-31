/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cft.util;

import java.util.Comparator;

/**
 *
 * @author Gennaro
 */
public class BasicBlockConnectionsComparator implements Comparator<BasicBlock>{
    
    public int compare(BasicBlock bbOne, BasicBlock bbTwo)
    {
        if((bbOne.getSuccBasicBlocks().size() + bbOne.getPredBasicBlocks().size()) 
                == (bbTwo.getSuccBasicBlocks().size() + bbTwo.getPredBasicBlocks().size()))
            return 0;
        
        if((bbOne.getSuccBasicBlocks().size() + bbOne.getPredBasicBlocks().size()) 
                > (bbTwo.getSuccBasicBlocks().size() + bbTwo.getPredBasicBlocks().size()))
        return -1;
        else
            return 1;
    }
}
