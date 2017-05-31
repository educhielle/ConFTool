/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.techniques;

import cft.config.DB;
import cft.util.Instruction;
import cft.util.Util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class ErrorTreatment
{
    public static void addErrorTreatmentSubroutine(String fileNameIn, String fileNameOut) throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(fileNameIn));
        BufferedWriter out = new BufferedWriter(new FileWriter(fileNameOut));
        
        for (String line; (line = in.readLine()) != null; )
        {
            out.write(line+"\n");
        }
        
        List<String> errorFunction = DB.getErrorFunction();
        
        for (int i = 0; i < errorFunction.size(); i++)
        {
            out.write(errorFunction.get(i) + "\n");
        }
        
        in.close();
        out.close();
    }
    
    public static void replaceErrorRegister(String fileNameIn, String fileNameOut) throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(fileNameIn));
        BufferedWriter out = new BufferedWriter(new FileWriter(fileNameOut));
        String errorRegister = DB.getErrorRegister();
        
        if (!errorRegister.isEmpty())
        {
            List<String> codeIn = Util.getCode(fileNameIn, DB.getCommentTag());
            boolean isPreInitializedRegister = DB.isPreInitializedRegister(errorRegister);
            
            List<String> freeRegisters = Util.complement(DB.getGeneralPurposeRegisters(), DB.getUsedRegisters(codeIn));
            
            if (!freeRegisters.isEmpty())
            {
                for (int i = 0; i < codeIn.size(); i++)
                {
                    out.write(Util.replaceAll(codeIn.get(i), errorRegister, freeRegisters.get(0)) + "\n");
                    
                    //Essa parte sobre registrador pré inicializado ser usado como registrador de erro é insana
                    //Aviso recomendando a não utilização de registradores pré inicializados como registrador de erro
                    if (isPreInitializedRegister && DB.isLabel(codeIn.get(i)) && DB.getLabel(codeIn.get(i)).equalsIgnoreCase(DB.getMainLabelName()))
                    {
                        List<String> empty = new ArrayList<String>();
                        List<String> free = new ArrayList<String>();
                        free.add(freeRegisters.get(0));
                        List<String> pre = new ArrayList<String>();
                        pre.add(errorRegister);
                        
                        //Transferir valor contido no registrador pré inicializado para o registrador substituto
                        Instruction instruction = DB.getMoveInstruction();
                        out.write(Util.generateCommand(instruction.getName(), free, pre, empty, empty, empty, instruction.getFormat()) + "\n");
                        
                        //Zerar valor do registrador pré inicializado, que passou a ser o registrador que indica o erro
                        instruction = DB.getImmediateAssignmentInstruction();
                        List<String> zero = new ArrayList<String>();
                        zero.add("0");
                        out.write(Util.generateCommand(instruction.getName(), pre, empty, zero, empty, empty, instruction.getFormat()) + "\n");
                    }
                }
                
                //Substituir lista de registradores pré inicializados - NAO FUNCIONA
                /*List<String> preInitializedRegisters = DB.getPreInitializedRegisters();
                preInitializedRegisters.remove(errorRegister);
                preInitializedRegisters.add(freeRegisters.get(0));
                DB.setPreInitializedRegisters(preInitializedRegisters);*/
            }
            else
            {
                for (int i = 0; i < codeIn.size(); i++)
                {
                    out.write(codeIn.get(i) + "\n");
                }
            }
        }
        else
        {
            for (String line; (line = in.readLine()) != null; )
            {
                out.write(line + "\n");
            }
        }
        
        in.close();
        out.close();
    }
}
