/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.techniques;

import cft.config.DB;
import cft.util.Global;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class Manager
{
    //private static List<String> usedRegisters = new ArrayList<String>();
    //private static List<String> copyRegisters = new ArrayList<String>();
    
    public static void applyTechnique(String fileNameIn, String fileNameOut, String technique) throws IOException
    {
        if (technique.equals("BRA")) BRA.apply(fileNameIn, fileNameOut);
        else if (technique.equals("HETA")) HETA.apply(fileNameIn, fileNameOut);
        else if (technique.equals("LTR")) LifetimeReducer.apply(fileNameIn, fileNameOut);
        else if (technique.equals("SETA")) SETA.apply(fileNameIn, fileNameOut);
        else if (technique.equals("SIG")) SIG.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR0")) VAR0.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR0+")) VAR0p.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR1")) VAR1.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR1+")) VAR1p.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR1++")) VAR1pp.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR2")) VAR2.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR2+")) VAR2p.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR2++")) VAR2pp.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR3")) VAR3.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR3+")) VAR3p.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR3++")) VAR3pp.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR4")) VAR4.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR4+")) VAR4p.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR4++")) VAR4pp.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR5")) VAR5.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR5+")) VAR5p.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR5++")) VAR5pp.apply(fileNameIn, fileNameOut);
        else if (technique.equals("BBCOUNTER")) BBCounter.apply(fileNameIn, fileNameOut);
        else if (technique.equals("REGCOUNTER")) REGCounter.apply(fileNameIn);
        else if (technique.equals("LWSWCOUNTER")) LwSwCounter.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR3R")) VAR3R.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR1+RESO")) VAR1pRESO.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR3+RESO")) VAR3pRESO.apply(fileNameIn, fileNameOut);
        else if (technique.equals("VAR3+RESOI")) VAR3pRESOi.apply(fileNameIn, fileNameOut);
    }
    
    public static void applyTechniques(String fileNameIn, String fileNameOut) throws IOException
    {
        applyTechniques(fileNameIn, fileNameOut, DB.getTechniques());
    }
    
    public static void applyTechniques(String fileNameIn, String fileNameOut, List<String> techniques) throws IOException
    {
        String fileNameAux = Global.nextName();
        ErrorTreatment.replaceErrorRegister(fileNameIn, fileNameAux);
        fileNameIn = fileNameAux;
        
        for (int i = 0; i < techniques.size(); i++)
        {
            fileNameAux = Global.nextName();
            applyTechnique(fileNameIn, fileNameAux, techniques.get(i));
            fileNameIn = fileNameAux;
        }
        
        ErrorTreatment.addErrorTreatmentSubroutine(fileNameIn, fileNameOut);                
        //Util.fileRename(fileNameIn, fileNameOut);
    }/*

    public static List<String> getCopyRegisters()
    {
        return copyRegisters;
    }
    
    public static List<String> getCopyRegisters(List<String> registers)
    {
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < registers.size(); i++)
        {
            list.add(copyRegisters.get(usedRegisters.indexOf(registers.get(i))));
        }
        
        return list;
    }

    public static List<String> getUsedRegisters()
    {
        return usedRegisters;
    }

    public static void setCopyRegisters(List<String> copyRegisters)
    {
        Manager.copyRegisters = copyRegisters;
    }

    public static void setUsedRegisters(List<String> usedRegisters)
    {
        Manager.usedRegisters = usedRegisters;
    }*/
}
