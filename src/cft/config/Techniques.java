/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cft.config;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class Techniques
{
    private int hetaOffset = 1000;
    private int offset = 0;
    private List<String> registersByPriority = new ArrayList<String>();
    private List<String> techniques;
    private String errorRegister = "";
    private String errorValue = "1";
    private String priorityMode = "all";
    private int resoShift = 1;
    private int setaMinNumberOfInstructions = 0;
    private boolean setaHigherPriority = false;
    private boolean setaInsertNops = false;
    private boolean setaInsertNopsAfterBranching = true;
    private Double setaPercentageToVerify = 1.0;
    private String setaPriorityMethod = "both";
    private Double setaTunnelEffectPercentage = 1.0;
    private boolean setaTunnelEffectSelectionByPercentage = false;
    private String signatureRegister = "";
    
    /*
     * Constructors
     */

    public Techniques(List<String> techniques)
    {
        this.techniques = techniques;
    }
    
    
    
    public Techniques()
    {
        this(new ArrayList<String>());
    }
    
    /*
     * Adders
     */
    
    public void addTechnique(String technique)
    {
        this.techniques.add(technique);
    }
    
    /*
     * Getters
     */

    public String getErrorRegister()
    {
        return errorRegister;
    }

    public String getErrorValue()
    {
        return errorValue;
    }
    
    public int getHetaOffset()
    {
        return hetaOffset;
    }
    
    public int getOffset()
    {
        return offset;
    }

    public String getPriorityMode()
    {
        return priorityMode;
    }

    public List<String> getRegistersByPriority()
    {
        return registersByPriority;
    }
    
    public int getResoShift()
    {
        return resoShift;
    }
    
    public boolean getSetaHigherPriority()
    {
        return setaHigherPriority;
    }
    
    public boolean getSetaInsertNops()
    {
        return setaInsertNops;
    }
    
    public boolean getSetaInsertNopsAfterBranching()
    {
        return setaInsertNopsAfterBranching;
    }

    public int getSetaMinNumberOfInstructions()
    {
        return setaMinNumberOfInstructions;
    }
    
    public Double getSetaPercentageToVerify()
    {
        return setaPercentageToVerify;
    }
    
    public String getSetaPriorityMethod()
    {
        return setaPriorityMethod;
    }
    
    public Double getSetaTunnelEffectPercentage()
    {
        return setaTunnelEffectPercentage;
    }
    
    public boolean getSetaTunnelEffectSelectionByPercentage()
    {
        return setaTunnelEffectSelectionByPercentage;
    }
    
    public String getSignatureRegister()
    {
        return signatureRegister;
    }
    
    public List<String> getTechniques()
    {
        return techniques;
    }
    
    /*
     * Setters
     */

    public void setErrorRegister(String errorRegister)
    {
        this.errorRegister = errorRegister;
    }

    public void setErrorValue(String errorValue)
    {
        this.errorValue = errorValue;
    }
    
    public void setHetaOffset(int hetaOffset)
    {
        this.hetaOffset = hetaOffset;
    }
    
    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public void setPriorityMode(String priorityMode)
    {
        this.priorityMode = priorityMode.toLowerCase();
    }

    public void setRegistersByPriority(List<String> registersByPriority)
    {
        this.registersByPriority = registersByPriority;
    }
    
    public void setResoShift(int resoShift)
    {
        this.resoShift = resoShift;
    }
    
    public void setSetaHigherPriority(boolean setaHigherPriority)
    {
        this.setaHigherPriority = setaHigherPriority;
    }
    
    public void setSetaInsertNops(boolean setaInsertNops)
    {
        this.setaInsertNops = setaInsertNops;
    }
    
    public void setSetaInsertNopsAfterBranching(boolean setaInsertNopsAfterBranching)
    {
        this.setaInsertNopsAfterBranching = setaInsertNopsAfterBranching;
    }

    public void setSetaMinNumberOfInstructions(int setaMinNumberOfInstructions)
    {
        this.setaMinNumberOfInstructions = (setaMinNumberOfInstructions>=0 ? setaMinNumberOfInstructions : 0);
    }
    
    public void setSetaPercentageToVerify(Double setaPercentageToVerify)
    {
        if (setaPercentageToVerify > 100.0)
        {
            setaPercentageToVerify = 100.0;
        }
        else if (setaPercentageToVerify < 0.0)
        {
            setaPercentageToVerify = 0.0;
        }
        
        this.setaPercentageToVerify = setaPercentageToVerify;
    }
    
    public void setSetaPriorityMethod(String setaPriorityMethod)
    {
        this.setaPriorityMethod = setaPriorityMethod;
    }
    
    public void setSetaTunnelEffectPercentage(double setaTunnelEffectPercentage)
    {
        this.setaTunnelEffectPercentage = setaTunnelEffectPercentage;
    }
    
    public void setSetaTunnelEffectSelectionByPercentage(boolean setaTunnelEffectSelectionByPercentage)
    {
        this.setaTunnelEffectSelectionByPercentage = setaTunnelEffectSelectionByPercentage;
    }
    
    public void setSignatureRegister(String signatureRegister)
    {
        this.signatureRegister = signatureRegister;
    }
    
    public void setTechniques(List<String> techniques)
    {
        this.techniques = techniques;
    }
}
