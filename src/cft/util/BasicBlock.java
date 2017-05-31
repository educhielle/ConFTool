/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.util;

import cft.config.DB;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class BasicBlock
{
    public static final int SYSTEM_BB_ID = 0; //do not change. It has to be zero!
    private HetaSign nis = new HetaSign();
    private HetaSign ns  = new HetaSign();
    private HetaSign nes = new HetaSign();
    private String method = new String();
    private char type;
    private int posStart;
    private int posEnd;
    private int numInstructions;
    private int id;
    private boolean protect;
    private boolean startingBB;
    private boolean verify;
    private List<BasicBlock> succBasicBlocks = new ArrayList<BasicBlock>();
    private List<BasicBlock> predBasicBlocks = new ArrayList<BasicBlock>();
    List<String> listRD; // list of destiny registers
    List<String> listRS; // list of source registers
    List<String> listRegs; // list of all registers in use

    public BasicBlock(int id, int posStart, int posEnd)
    {
        this.listRS = new ArrayList<String>();
        this.listRD = new ArrayList<String>();
        this.listRegs = new ArrayList<String>();
        this.id = id;
        this.posStart = posStart;
        this.posEnd = posEnd;
        this.protect = true;
        this.verify = false;
        this.succBasicBlocks = new ArrayList<BasicBlock>();
        this.predBasicBlocks = new ArrayList<BasicBlock>();
    }
    
    public void addSuccBasicBlock(BasicBlock basicBlock)
    {
        if (!this.succBasicBlocks.contains(basicBlock))
        {
            this.succBasicBlocks.add(basicBlock);
        }
    }
    
    public void addPredBasicBlock(BasicBlock basicBlock)
    {
        if (!this.predBasicBlocks.contains(basicBlock))
        {
            this.predBasicBlocks.add(basicBlock);
        }
    }
    
    public void clearPredBasicBlocks()
    {
        this.predBasicBlocks = new ArrayList<BasicBlock>();
    }
    
    public void clearSuccBasicBlocks()
    {
        this.succBasicBlocks = new ArrayList<BasicBlock>();
    }
    
        /*
     * Create basic blocks
     */
    public static List<BasicBlock> createBasicBlocks(List<String> code) throws IOException
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
    
    public void removePredBasicBlock(BasicBlock basicBlock)
    {
        this.predBasicBlocks.remove(basicBlock);
    }
    
    public void removeSuccBasicBlock(BasicBlock basicBlock)
    {
        this.succBasicBlocks.remove(basicBlock);
    }
    
    public BasicBlock getSuccBasicBlock(int index)
    {
        return this.succBasicBlocks.get(index);
    }
    
    public int getSuccBasicBlockId(int index)
    {
        return this.succBasicBlocks.get(index).getId();
    }
    
    public BasicBlock getPredBasicBlock(int index)
    {
        return this.predBasicBlocks.get(index);
    }
    
    public int getPredBasicBlockId(int index)
    {
        return this.predBasicBlocks.get(index).getId();
    }
    
    public List<BasicBlock> getSuccBasicBlocks()
    {
        return this.succBasicBlocks;
    }
    
    public List<Integer> getSuccBasicBlocksIds()
    {
        List<Integer> basicBlocksIds = new ArrayList<Integer>();
        
        for (int i = 0; i < this.succBasicBlocks.size(); i++)
        {
            basicBlocksIds.add(this.succBasicBlocks.get(i).getId());
        }
        
        return basicBlocksIds;
    }
    
    public List<BasicBlock> getPredBasicBlocks()
    {
        return this.predBasicBlocks;
    }
    
    public List<Integer> getPredBasicBlocksIds()
    {
        List<Integer> basicBlocksIds = new ArrayList<Integer>();
        
        for (int i = 0; i < this.predBasicBlocks.size(); i++)
        {
            basicBlocksIds.add(this.predBasicBlocks.get(i).getId());
        }
        
        return basicBlocksIds;
    }

  
  /* TODO  
    public void setNis(int num)
    {
        nis.setSign(num);
    }

    public void setNs(int num)
    {
        ns.setSign(num);
    }

    public void setNes(int num)
    {
        nes.setSign(num);
    }
*/
    
    public void setType(char type)
    {
        this.type = type;
    }

    public void setPosStart(int posStart)
    {
        this.posStart = posStart;
    }

    public void setPosEnd(int posEnd)
    {
        this.posEnd = posEnd;
    }

    public void setId(int id)
    {
        this.id = id;
    }
    
    public void setProtect(boolean protect)
    {
        this.protect = protect;
    }
    
    public void setStartingBB(boolean startingBB)
    {
        this.startingBB = startingBB;
    }
    
    public void setVerify(boolean verify)
    {
        this.verify = verify;
    }
    
    public HetaSign getHetaNs()
    {
        return ns;
    }
    
    public HetaSign getHetaNes()
    {
        return nes;
    }
    
    public HetaSign getHetaNis()
    {
        return nis;
    }
       
    public int getNis()
    {
        return nis.getSign();
    }
    
    public int getNs()
    {
        return ns.getSign();
    }

    public int getNes()
    {
        return nes.getSign();
    }

    public char getType()
    {
        return type;
    }
    
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getPosStart()
    {
        return posStart;
    }

    public int getPosEnd()
    {
        return posEnd;
    }
    
    public boolean isStartingBB()
    {
        return startingBB;
    }

    public int getId()
    {
        return id;
    }
    
    public int getNisConstant()
    {
        HetaSign constant = new HetaSign();
        List<Integer> lowerHalf = new ArrayList<Integer>();
        List<Integer> higherHalf = new ArrayList<Integer>();
        //boolean hasBB0asPred = false;
         /*
        for (int i = 0; i < predBasicBlocks.size(); i++)
        {
            if (predBasicBlocks.get(i).getId() == SYSTEM_BB_ID)
            {
                return nis.getSign();
            }
        }*/
        
        if (type == 'X')
        {
            if (predBasicBlocks.isEmpty())
                return nis.getSign(); // zero ^ nis = nis. The first block hasnt any predecessor.
            else
                return predBasicBlocks.get(0).getHetaNes().getSign() ^ nis.getSign(); // the compairison is a XOR like the other ones
        }
        else 
        {
            for (int i = 0; i < nis.getHigherSignSize(); i++)
                higherHalf.add(1); // the higher half is full of 1's
            
            constant.setHigherSign(higherHalf);
            constant.setLowerSign(nis.getLowerSignList()); // the lower half is the same as the NIS.lh
            return constant.getSign();
        }
         
        
    }
    
    public int getNsConstant()
    {
       int constant;
       
       constant = nis.getSign() ^ ns.getSign();
        
       return constant;
    }
    
    public int getNesConstant()
    {
       int constant;
       
       if (method.equals("HETA")) // when processing SETA
       constant = ns.getSign() ^ nes.getSign();
       else
           constant = nis.getSign() ^ nes.getSign();
       
       return constant;
    }
    
    public boolean getProtect()
    {
        return protect;
    }
    
    public boolean getVerify()
    {
        return verify;
    }
    
    public int getNumInstructions() {
        return numInstructions;
    }

    public void setNumInstructions(int numInstructions) {
        this.numInstructions = numInstructions;
    }
    
    public List<String> getListRD() {
        return listRD;
    }

    public void setListRD(List<String> listRD) {
        this.listRD = listRD;
    }

    public List<String> getListRS() {
        return listRS;
    }

    public void setListRS(List<String> listRS) {
        this.listRS = listRS;
    }
    
    public List<String> getListRegs() {
        return listRegs;
    }

    public void setListRegs(List<String> listRegs) {
        this.listRegs = listRegs;
    }
    
}
