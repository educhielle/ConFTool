/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cft.util;

import java.util.Comparator;

/**
 *
 * @author Eduardo
 */
public class BasicBlockSizeComparator implements Comparator<BasicBlock>
{
    @Override
    public int compare(BasicBlock bbOne, BasicBlock bbTwo)
    {
        if (bbOne.getNumInstructions() < bbTwo.getNumInstructions()) return 1;
        
        if (bbOne.getNumInstructions() > bbTwo.getNumInstructions()) return -1;
        
        return 0;
    }
}
