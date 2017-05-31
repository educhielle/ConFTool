/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cft.techniques;

import cft.config.DB;
import cft.util.Instruction;
import cft.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class BRA
{
    public static String apply(String fileNameIn) throws IOException 
    {
        String fileNameOut = Util.getFileTitle(fileNameIn) + "BRA." + Util.getFileExtension(fileNameIn);
        apply(fileNameIn, fileNameOut);
        
        return fileNameOut;
    }
    
    public static void apply(String fileNameIn, String fileNameOut) throws IOException
    {
        Instruction jump = DB.getJumpInstruction();
        Instruction nop = DB.getNoOperationInstruction();
        Instruction comparison = DB.getComparisonInstruction();
        int counter = 0;
        List<String> code = Util.getCode(fileNameIn, DB.getCommentTag());
        List<String> emptyList = new ArrayList<String>();
        List<String> errorLabelName = new ArrayList<String>();
        errorLabelName.add(DB.ERROR_LABEL_NAME);
        
        for (int i = 0; i < code.size(); i++)
        {
            if (DB.isInstruction(code.get(i)))
            {
                Instruction instruction = DB.getInstruction(code.get(i));
                //Se for uma instrução do tipo "branch_to_target"
                if (DB.isBranch(instruction))
                {
                    //Extrair a target
                    List<String> target = DB.getTarget(code.get(i), instruction.getFormat());
                    
                    //Se não for um teste de alguma técnica de proteção
                    if (!target.get(0).equalsIgnoreCase(DB.ERROR_LABEL_NAME))
                    {
                        List<String> newTarget = new ArrayList<String>();
                        newTarget.add(target.get(0) + "_" + counter++);
                        
                        List<String> rd = DB.getRD(code.get(i), instruction.getFormat());
                        List<String> rs = DB.getRS(code.get(i), instruction.getFormat());
                        List<String> imm = DB.getImmediate(code.get(i), instruction.getFormat());
                        List<String> offset = DB.getOffset(code.get(i), instruction.getFormat());
                        
                        //Substituir instrução atual pela instrução com a target para o bloco de tratamento
                        code.remove(i);
                        code.add(i, Util.generateCommand(instruction.getName(), rd, rs, imm, offset, newTarget, instruction.getFormat()));
                        //As instruções inversas devem ser informadas
                        
                        
                        //Caso exista, inserir instrução de comparação independente
                        int pos = i + DB.getBranchDelaySlot() + 1;
                        //if (DB.hasComparisonInstruction())
                        //{
                        //    code.add(pos++, Util.generateCommand(comparison.getName(), rd, rs, imm, offset, errorLabelName, comparison.getFormat()));
                        //}
                        
                        //Inserir comparador logicamente igual ao encontrado com desvio para o tratamento de erro
                        code.add(pos++, Util.generateCommand(instruction.getName(), rd, rs, imm, offset, errorLabelName, instruction.getFormat()));
                        
                        //Inserir instruções NOPs equivalentes ao valor de "Branch Delay Slot" após comparador
                        for (int aux = 0; aux < DB.getBranchDelaySlot(); aux++)
                        {
                            code.add(pos + aux, Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                        }
                        
                        //Localizar label e inserir os blocos de verificação
                        for (int j = 0; j < code.size(); j++)
                        {
                            if ((DB.isLabel(code.get(j))) && (DB.getLabel(code.get(j)).equalsIgnoreCase(target.get(0))))
                            {
                                //Instrução para manter a corretude do código
                                code.add(j++, Util.generateCommand(jump.getName(), emptyList, emptyList, emptyList, emptyList, target, jump.getFormat()));
                                for (int k = 0; k < DB.getBranchDelaySlot(); k++)
                                {
                                    code.add(j++, Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                                }
                                
                                //Bloco de tratamento
                                //Label
                                code.add(j++, Util.replaceFirst(DB.getLabelFormat(), "label", newTarget.get(0)));
                                
                                //Instrução logicamente inversa
                                Instruction inverse = DB.getInvertedInstruction(instruction);
                                code.add(j++, Util.generateCommand(inverse.getName(), rd, rs, imm, offset, errorLabelName, inverse.getFormat()));
                                
                                //Inserir instruções NOPs equivalentes ao valor de "Branch Delay Slot" após comparador
                                for (int k = 0; k < DB.getBranchDelaySlot(); k++)
                                {
                                    code.add(j++, Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                                }
                                
                                //Salto para manter a corretude
                                code.add(j++, Util.generateCommand(jump.getName(), emptyList, emptyList, emptyList, emptyList, target, jump.getFormat()));
                                for (int k = 0; k < DB.getBranchDelaySlot(); k++)
                                {
                                    code.add(j++, Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                                }
                                
                                //Incrementar o contador em caso de inserir instruções anteriormente ao desvio tratado
                                if (j < i) i += 4 + 3 * DB.getBranchDelaySlot();
                                
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        Util.write(code, fileNameOut);
    }
}
