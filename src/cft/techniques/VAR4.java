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
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class VAR4
{
    private static List<String> usedRegisters = new ArrayList<String>();
    private static List<String> freeRegisters = new ArrayList<String>();
    private static List<String> copyRegisters = new ArrayList<String>();
    
    public static String apply(String fileNameIn) throws IOException
    {
        String fileNameOut = Util.getFileTitle(fileNameIn) + "VAR3." + Util.getFileExtension(fileNameIn);
        apply(fileNameIn, fileNameOut);
        
        return fileNameOut;
    }
    
    public static void apply(String fileNameIn, String fileNameOut) throws IOException
    {
        List<String> code = Util.getCode(fileNameIn, DB.getCommentTag());
        List<String> generalPurposeRegisters = DB.getGeneralPurposeRegisters();
        //O segredo para implementar as técnicas de seleção dos registradores está no método DB.getUsedRegisters(List<String>)
        usedRegisters = DB.getUsedRegisters(code);
        
        usedRegisters.remove(DB.getSignatureRegister());
        generalPurposeRegisters.remove(DB.getSignatureRegister());

        usedRegisters.remove(DB.getErrorRegister());
        generalPurposeRegisters.remove(DB.getErrorRegister());

        //Remover registradores IO da lista de registradores utilizados para não serem copiados
        for (int i = usedRegisters.size() - 1; i >= 0; i--)
        {
            if (DB.isIORegister(usedRegisters.get(i)))
            {
                usedRegisters.remove(usedRegisters.get(i));
            }
        }
        
        freeRegisters = Util.complement(generalPurposeRegisters, usedRegisters);
        List<String> preInitializedRegisters = DB.getPreInitializedRegisters();
        freeRegisters.removeAll(preInitializedRegisters);
        
        if (DB.getSetaHigherPriority() && DB.getTechniques().contains("SETA"))
        {
            if (DB.hasComparisonWithImmediateInstruction() && (freeRegisters.size() >= 1))
            {
                freeRegisters.remove(0);
            }
            else if (DB.getSetaHigherPriority() && (freeRegisters.size() >= 2))
            {
                freeRegisters.remove(0);
                freeRegisters.remove(0);
            }
        }
        //System.out.println(freeRegisters);
 
        //Separar registradores usados em globais e locais
        List<String> usedGlobalRegisters = new ArrayList<String>();
        List<String> usedLocalRegisters = new ArrayList<String>();
        for (int i = 0; i < usedRegisters.size(); i++)
        {
            if (DB.isGlobalRegister(usedRegisters.get(i)))
            {
                usedGlobalRegisters.add(usedRegisters.get(i));
            }
            else if (DB.isLocalRegister(usedRegisters.get(i)))
            {
                usedLocalRegisters.add(usedRegisters.get(i));
            }
        }

        //Separar registradores disponíveis em globais e locais
        List<String> freeGlobalRegisters = new ArrayList<String>();
        List<String> freeLocalRegisters = new ArrayList<String>();
        for (int i = 0; i < freeRegisters.size(); i++)
        {
            if (DB.isGlobalRegister(freeRegisters.get(i)))
            {
                freeGlobalRegisters.add(freeRegisters.get(i));
            }
            else if (DB.isLocalRegister(freeRegisters.get(i)))
            {
                freeLocalRegisters.add(freeRegisters.get(i));
            }
        }
        
        //Definir registradores cópia e reordenar registrador de registradores usados
        usedRegisters = new ArrayList<String>();
        int globalLength = (usedGlobalRegisters.size()<freeGlobalRegisters.size()?usedGlobalRegisters.size():freeGlobalRegisters.size());
        for (int i = 0; i < globalLength; i++)
        {
            copyRegisters.add(freeGlobalRegisters.get(i));
            usedRegisters.add(usedGlobalRegisters.get(i));
        }
        int localLength = (usedLocalRegisters.size()<freeLocalRegisters.size()?usedLocalRegisters.size():freeLocalRegisters.size());
        for (int i = 0; i < localLength; i++)
        {
            copyRegisters.add(freeLocalRegisters.get(i));
            usedRegisters.add(usedLocalRegisters.get(i));
        }
        
        //Completar lista de registradores usados com os registradores que não terão cópias
        for (int i = globalLength; i < usedGlobalRegisters.size(); i++)
        {
            usedRegisters.add(usedGlobalRegisters.get(i));
        }
        for (int i = localLength; i < usedLocalRegisters.size(); i++)
        {
            usedRegisters.add(usedLocalRegisters.get(i));
        }

        System.out.println(usedRegisters);
        System.out.println(copyRegisters);

        Instruction branchComparison = DB.getNonEqualityComparisonInstruction();
        Instruction comparison = DB.getComparisonInstruction();
        Instruction moveInstruction = DB.getMoveInstruction();
        Instruction nop = DB.getNoOperationInstruction();
        List<String> emptyList = new ArrayList<String>();
        List<String> errorLabelName = new ArrayList<String>();
        List<String> listOfTargetsWithHiddenRD = new ArrayList<String>();
        errorLabelName.add(DB.ERROR_LABEL_NAME);
        
        //Controle de área após instrução de comparação e antes de intrução de desvio, proibir inserção de verificadores nessa área
        boolean freeComparisonArea = true;
        for (int i = 0; i < code.size(); i++)
        {
            if (DB.isInstruction(code.get(i)))
            {
                Instruction instruction = DB.getInstruction(code.get(i));
                List<String> rd = DB.getRD(code.get(i), instruction.getFormat());
                List<String> rs = DB.getRS(code.get(i), instruction.getFormat());
                
                if (!rd.contains(DB.getSignatureRegister()) && !rs.contains(DB.getSignatureRegister()) && !Util.containsAtLeastOne(freeRegisters, rd) && !Util.containsAtLeastOne(freeRegisters, rs))
                {
                    String[] hiddenRS = instruction.getHiddenRS();
                    
                    for (int j = 0; j < hiddenRS.length; j++)
                    {
                        if (!rs.contains(hiddenRS[j]))
                        {
                            rs.add(hiddenRS[j]);
                        }
                    }
                    
                    List<String> hiddenRD = Arrays.asList(instruction.getHiddenRD());
                    
                    List<String> rdCopy = getCopyRegisters(rd);
                    List<String> rsCopy = getCopyRegisters(rs);
                    List<String> hiddenRDCopy = getCopyRegisters(hiddenRD);
                    
                    List<String> imm = DB.getImmediate(code.get(i), instruction.getFormat());
                    List<String> offset = DB.getOffset(code.get(i), instruction.getFormat());
                    List<String> offsetCopy = getCopyOffset(offset);
                    List<String> target = DB.getTarget(code.get(i), instruction.getFormat());

                    List<List> rdIntercalate = Util.splitIntercalate(rd, rdCopy, 2);
                    List<List> rsIntercalate = Util.splitIntercalate(rs, rsCopy, 2);
                    
                    if (instruction.getName().equals(comparison.getName()))
                    {
                        freeComparisonArea = false;
                    }
                    
                    if (DB.isArithmetic(instruction))
                    {
                        //Instrução cópia
                        if (!rd.equals(rdCopy))
                        {
                            code.add(++i, Util.generateCommand(instruction.getName(), rdCopy, rsCopy, imm, offsetCopy, target, instruction.getFormat()));
                        }
                    }
                    else if (DB.isLoad(instruction))
                    {
                        //Instrução cópia
                        if (!rd.equals(rdCopy))
                        {
                            code.add(++i, Util.generateCommand(instruction.getName(), rdCopy, rsCopy, imm, offsetCopy, target, instruction.getFormat()));
                        }
                    }
                    else if (DB.isLoadFromStack(instruction))
                    {
                        //Copy instructions -- it has to be moves
                        for (List<String> currentRD : rdIntercalate)
                        {
                            if (!currentRD.get(0).equals(currentRD.get(1)))
                            {
                                List<String> currentOriginalRD = new ArrayList<String>();
                                currentOriginalRD.add(currentRD.get(0));
                                List<String> currentCopyRD = new ArrayList<String>();
                                currentCopyRD.add(currentRD.get(1));
                                code.add(++i, Util.generateCommand(moveInstruction.getName(), currentCopyRD, currentOriginalRD, emptyList, emptyList, emptyList, moveInstruction.getFormat()));
                            }
                        }
                    }
                    else if (DB.isStore(instruction))
                    {
                        //Instruções de verificação
                        for (int j = 0; j < rsIntercalate.size(); j++)
                        {
                            if (freeComparisonArea && !rsIntercalate.get(j).get(0).equals(rsIntercalate.get(j).get(1)))
                            {
                                if (DB.hasComparisonInstruction())
                                {
                                    code.add(i++, Util.generateCommand(comparison.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, emptyList, comparison.getFormat()));
                                }
                                code.add(i++, Util.generateCommand(branchComparison.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, errorLabelName, branchComparison.getFormat()));

                                for (int k = 0; k < DB.getBranchDelaySlot(); k++)
                                {
                                    code.add(i++, Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                                }
                            }
                        }
                        
                        //Instrução cópia
                        if (!rs.equals(rsCopy))
                        {
                            code.add(++i, Util.generateCommand(instruction.getName(), rdCopy, rsCopy, imm, offsetCopy, target, instruction.getFormat()));
                        }
                    }
                    else if (DB.isStoreToStack(instruction))
                    {
                        //Checking Instructions
                        for (List currentRS : rsIntercalate)
                        {
                            if (DB.hasComparisonInstruction())
                            {
                                {
                                    code.add(i++, Util.generateCommand(comparison.getName(), emptyList, currentRS, emptyList, emptyList, emptyList, comparison.getFormat()));
                                }
                                code.add(i++, Util.generateCommand(branchComparison.getName(), emptyList, currentRS, emptyList, emptyList, errorLabelName, branchComparison.getFormat()));
                            }
                        }
                    }
                    else if (DB.isJump(instruction))
                    {
                        //Instruções de verificação
                        if (target.isEmpty() || !target.get(0).equalsIgnoreCase(DB.ERROR_LABEL_NAME))
                        {
                            for (int j = 0; j < rsIntercalate.size(); j++)
                            {
                                if (freeComparisonArea && !rsIntercalate.get(j).get(0).equals(rsIntercalate.get(j).get(1)))
                                {
                                    if (DB.hasComparisonInstruction())
                                    {
                                        code.add(i++, Util.generateCommand(comparison.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, emptyList, comparison.getFormat()));
                                    }
                                    code.add(i++, Util.generateCommand(branchComparison.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, errorLabelName, branchComparison.getFormat()));

                                    for (int k = 0; k < DB.getBranchDelaySlot(); k++)
                                    {
                                        code.add(i++, Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                                    }
                                }
                            }
                        }
                    }
                    else if (DB.isBranch(instruction))
                    {
                        //Instruções de verificação
                        if (!target.get(0).equalsIgnoreCase(DB.ERROR_LABEL_NAME))
                        {
                            for (int j = 0; j < rsIntercalate.size(); j++)
                            {
                                if (freeComparisonArea && !rsIntercalate.get(j).get(0).equals(rsIntercalate.get(j).get(1)))
                                {
                                    //Se usar comparação independente, não haverá registradores na instrução de desvio, logo, nunca vai entrar nesse if
                                    if (DB.hasComparisonInstruction())
                                    {
                                        code.add(i++, Util.generateCommand(comparison.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, emptyList, comparison.getFormat()));
                                    }
                                    code.add(i++, Util.generateCommand(branchComparison.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, errorLabelName, branchComparison.getFormat()));

                                    for (int k = 0; k < DB.getBranchDelaySlot(); k++)
                                    {
                                        code.add(i++, Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                                    }
                                }
                            }
                        }
                        freeComparisonArea = true;
                    }
                    else if (DB.isOther(instruction))
                    {
                        if (!rd.equals(rdCopy))
                        {
                            code.add(++i, Util.generateCommand(moveInstruction.getName(), rdCopy, rd, emptyList, emptyList, emptyList, moveInstruction.getFormat()));
                        }
                    }
                    
                    //Atribuição à cópia em caso de RD oculto
                    boolean jumped = false;
                    for (int j = 0; j < hiddenRD.size(); j++)
                    {
                        List<String> newRD = new ArrayList<String>();
                        newRD.add(hiddenRDCopy.get(j));
                        List<String> newRS = new ArrayList<String>();
                        newRS.add(hiddenRD.get(j));
                        if (!newRD.equals(newRS))
                        {
                            if ((DB.isBranch(instruction) || DB.isJump(instruction)) && !jumped)
                            {
                                i += DB.getBranchDelaySlot();
                                jumped = true;
                            }
                            code.add(++i, Util.generateCommand(moveInstruction.getName(), newRD, newRS, emptyList, emptyList, emptyList, moveInstruction.getFormat()));
                        }
                    }
                    
                    //Adicionar target e RD oculto à listOfTargetsWithHiddenRD
                    if (!target.isEmpty() && (!target.get(0).equalsIgnoreCase(DB.ERROR_LABEL_NAME)))
                    {
                        for (int j = 0; j < hiddenRD.size(); j++)
                        {
                            String targetAndHiddenRD = target.get(0)+":"+hiddenRD.get(j)+":"+hiddenRDCopy.get(j);
                            if (!listOfTargetsWithHiddenRD.contains(targetAndHiddenRD) && !hiddenRD.get(j).equals(hiddenRDCopy.get(j)))
                            {
                                listOfTargetsWithHiddenRD.add(targetAndHiddenRD);
                            }
                        }
                    }
                }
            }
            else if (DB.isLabel(code.get(i)))
            {
                //Se for o "main", copiar valores dos registradores pré-inicializados para as cópias
                if (DB.getMainLabelName().equalsIgnoreCase(DB.getLabel(code.get(i))))
                {
                    for (int j = 0; j < preInitializedRegisters.size(); j++)
                    {
                        if (usedRegisters.contains(preInitializedRegisters.get(j)))
                        {
                            List<String> rs = new ArrayList<String>();
                            rs.add(preInitializedRegisters.get(j));

                            List<String> rd = getCopyRegisters(rs);

                            if (!rd.equals(rs))
                            {
                                code.add(++i, Util.generateCommand(moveInstruction.getName(), rd, rs, emptyList, emptyList, emptyList, moveInstruction.getFormat()));
                            }
                        }
                    }
                }
            }
        }
        
        //Atribuição à cópia em caso de RD oculto nos destinos dos targets
        for (int i = 0; i < code.size(); i++)
        {
            if (DB.isLabel(code.get(i)))
            {
                String label = DB.getLabel(code.get(i));
                for (int j = listOfTargetsWithHiddenRD.size() - 1; j >= 0; j--)
                {
                    String[] vector = listOfTargetsWithHiddenRD.get(j).split(":");
                    if (label.equalsIgnoreCase(vector[0]))                    
                    {
                        List<String> newRD = new ArrayList<String>();
                        newRD.add(vector[2]);
                        List<String> newRS = new ArrayList<String>();
                        newRS.add(vector[1]);
                        code.add(++i, Util.generateCommand(moveInstruction.getName(), newRD, newRS, emptyList, emptyList, emptyList, moveInstruction.getFormat()));
                        listOfTargetsWithHiddenRD.remove(j);
                    }
                }
            }
        }

        //Escrever código com a técnica aplicada no arquivo de saída
        Util.write(code, fileNameOut);
    }

    public static List<String> getCopyOffset(List<String> offset)
    {
        List<String> copyOffset = new ArrayList<String>();

        for (int i = 0; i < offset.size(); i++)
        {
            copyOffset.add(String.valueOf(Integer.parseInt(offset.get(i)) + DB.getOffset()));
        }

        return copyOffset;
    }

    public static List<String> getCopyRegisters(List<String> registers)
    {
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < registers.size(); i++)
        {
            try
            {
                list.add(copyRegisters.get(usedRegisters.indexOf(registers.get(i))));
            }
            catch (Exception error)
            {
                list.add(registers.get(i));
            }
        }
        
        return list;
    }
}
