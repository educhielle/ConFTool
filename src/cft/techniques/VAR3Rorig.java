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
public class VAR3Rorig
{
    private static List<String> usedRegisters = new ArrayList<String>();
    private static List<String> freeRegisters = new ArrayList<String>();
    private static List<String> copyRegisters = new ArrayList<String>();
    private static List<String> registerOffset = new ArrayList<String>();
    private static List<String> emptyList = new ArrayList<String>();
    private static List<String> addressRegisterList = new ArrayList<String>();
    
    /*
     * List of instructions
     */
    private static Instruction bne = DB.getNonEqualityComparisonInstruction();
    private static Instruction beq = DB.getEqualityComparisonInstruction();
    private static Instruction cmp = DB.getComparisonInstruction();
    private static Instruction jump = DB.getJumpInstruction();
    private static Instruction move = DB.getMoveInstruction();
    private static Instruction moveImm = DB.getImmediateAssignmentInstruction();
    private static Instruction moveHigh = DB.getHigherHalfImmediateAssignmentInstruction();
    private static Instruction moveLow = DB.getLowerHalfImmediateAssignmentInstruction();
    private static Instruction addImm  = DB.getAddImmediateInstruction();
    private static Instruction addRegs  = DB.getAddRegistersInstruction();
    
    private static Instruction nop = DB.getNoOperationInstruction();
    private static Instruction store = DB.getStoreInstruction();
    private static Instruction load = DB.getLoadInstruction();
    
    private static String addressRegister;
    
    private static final String PC_NAME = "pc";
    private static final String PC_OFFSET = "0";
    private static final String HIGHER_HALF_ADDRESS = "8192"; //0x0000
    private static final String LOWER_HALF_ADDRESS = "57088"; //0xFFBF
    private static final String HIGHER_OFFSET_2ND = "0"; //0x0000
    private static final String LOWER_OFFSET_2ND = "0"; //0x4000
    private static final String HIGHER_OFFSET_3RD = "0"; //0x0000
    private static final String LOWER_OFFSET_3RD = "0"; //0x4000
    private static List<String> pcList = Util.createList(PC_NAME);
    private static List<String> pcOffsetList = Util.createList(PC_OFFSET);
    
    private static List<String> higherHalfAddressList = Util.createList(HIGHER_HALF_ADDRESS);
    private static List<String> lowerHalfAddressList = Util.createList(LOWER_HALF_ADDRESS);
    private static List<String> higherOffsetList2nd = Util.createList(HIGHER_OFFSET_2ND);
    private static List<String> lowerOffsetList2nd = Util.createList(LOWER_OFFSET_2ND);
    private static List<String> higherOffsetList3rd = Util.createList(HIGHER_OFFSET_3RD);
    private static List<String> lowerOffsetList3rd = Util.createList(LOWER_OFFSET_3RD);
    
    public static String apply(String fileNameIn) throws IOException
    {
        String fileNameOut = Util.getFileTitle(fileNameIn) + "VAR3R." + Util.getFileExtension(fileNameIn);
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
        System.out.println("freeRegisters: "+freeRegisters);
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
        
        addressRegister = freeGlobalRegisters.get(0);
        freeGlobalRegisters.remove(0);
        
        addressRegisterList = Util.createList(addressRegister);
        
        //Definir registradores cópia e reordenar registrador de registradores usados
        usedRegisters = new ArrayList<String>();
        int globalLength = (usedGlobalRegisters.size()<freeGlobalRegisters.size()?usedGlobalRegisters.size():freeGlobalRegisters.size());
        for (int i = 0; i < globalLength; i++)
        {
            copyRegisters.add(freeGlobalRegisters.get(i));
            usedRegisters.add(usedGlobalRegisters.get(i));
            registerOffset.add(String.valueOf(4*(i+1)));
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
                    
                    if (instruction.getName().equals(cmp.getName()))
                    {
                        freeComparisonArea = false;
                    }
                    
                    if (DB.isArithmetic(instruction))
                    {
                        //Instruções de verificação
                        if (!rsIntercalate.isEmpty()) code.add(i++,savePC());
                        for (int j = 0; j < rsIntercalate.size(); j++)
                        {
                            if (freeComparisonArea && !rsIntercalate.get(j).get(0).equals(rsIntercalate.get(j).get(1)))
                            {
                                if (DB.hasComparisonInstruction())
                                {
                                    code.add(i++, Util.generateCommand(cmp.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, emptyList, cmp.getFormat()));
                                }
                                code.add(i++, Util.generateCommand(bne.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, getErrorLabelName(rsIntercalate.get(j)), bne.getFormat()));

                                for (int k = 0; k < DB.getBranchDelaySlot(); k++)
                                {
                                    code.add(i++, Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                                }
                            }
                        }
                        
                        //Instrução cópia
                        if (!rd.equals(rdCopy))
                        {
                            code.add(++i, Util.generateCommand(instruction.getName(), rdCopy, rsCopy, imm, offsetCopy, target, instruction.getFormat()));                        
                            code.add(++i, saveThirdRegister(rd.get(0)));
                        }
                    }
                    else if (DB.isLoad(instruction))
                    {
                        //Instruções de verificação
                        code.add(i++,savePC());
                        for (int j = 0; j < rsIntercalate.size(); j++)
                        {
                            if (freeComparisonArea && !rsIntercalate.get(j).get(0).equals(rsIntercalate.get(j).get(1)))
                            {
                                if (DB.hasComparisonInstruction())
                                {
                                    code.add(i++, Util.generateCommand(cmp.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, emptyList, cmp.getFormat()));
                                }
                                code.add(i++, Util.generateCommand(bne.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, getErrorLabelName(rsIntercalate.get(j)), bne.getFormat()));

                                for (int k = 0; k < DB.getBranchDelaySlot(); k++)
                                {
                                    code.add(i++, Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                                }
                            }
                        }
                        
                        //Instrução cópia
                        if (!rd.equals(rdCopy))
                        {
                            i = prepareCopyAddressRegisters(code, ++i, rsCopy);
                            i--;
                            
                            code.add(++i, Util.generateCommand(instruction.getName(), rdCopy, rsCopy, emptyList, offset, emptyList, instruction.getFormat()));
                            code.add(++i, Util.generateCommand(instruction.getName(), addressRegisterList, addressRegisterList, emptyList, offset, emptyList, instruction.getFormat()));
                            
                            i = loadTripleCheck(code, ++i, rd, rdCopy, rs, rsCopy, offset);
                            i--;
                            
                            i = returnCopyAddressRegisters(code, ++i, rsCopy);
                            i--;
                            
                            code.add(++i, saveThirdRegister(rd.get(0)));
                        }
                    }
                    else if (DB.isLoadFromStack(instruction))
                    {
                        //Checking Instructions
                    code.    add(i++,savePC());
                        for (List currentRS : rsIntercalate)
                        {
                            if (freeComparisonArea && !currentRS.get(0).equals(currentRS.get(1)))
                            {
                                //i = assignRegisterAddress(code, i);
                                if (DB.hasComparisonInstruction())
                                {
                                    code.add(i++, Util.generateCommand(cmp.getName(), emptyList, currentRS, emptyList, emptyList, emptyList, cmp.getFormat()));
                                }
                                code.add(i++, Util.generateCommand(bne.getName(), emptyList, currentRS, emptyList, emptyList, getErrorLabelName(currentRS), bne.getFormat()));
                            }
                        }
                        
                        //Copy instructions -- it has to be moves
                        for (List<String> currentRD : rdIntercalate)
                        {
                            if (!currentRD.get(0).equals(currentRD.get(1)))
                            {
                                List<String> currentOriginalRD = new ArrayList<String>();
                                currentOriginalRD.add(currentRD.get(0));
                                List<String> currentCopyRD = new ArrayList<String>();
                                currentCopyRD.add(currentRD.get(1));
                                code.add(++i, Util.generateCommand(move.getName(), currentCopyRD, currentOriginalRD, emptyList, emptyList, emptyList, move.getFormat()));
                                                         
                                code.add(++i, saveThirdRegister(currentOriginalRD.get(0)));
                            }
                        }
                    }
                    else if (DB.isStore(instruction))
                    {
                        //Instruções de verificação
                        code.add(i++,savePC());
                        for (int j = 0; j < rsIntercalate.size(); j++)
                        {
                            if (freeComparisonArea && !rsIntercalate.get(j).get(0).equals(rsIntercalate.get(j).get(1)))
                            {
                                if (DB.hasComparisonInstruction())
                                {
                                    code.add(i++, Util.generateCommand(cmp.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, emptyList, cmp.getFormat()));
                                }
                                code.add(i++, Util.generateCommand(bne.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, getErrorLabelName(rsIntercalate.get(j)), bne.getFormat()));

                                for (int k = 0; k < DB.getBranchDelaySlot(); k++)
                                {
                                    code.add(i++, Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                                }
                            }
                        }
                        
                        //Instrução cópia
                        if (!rs.equals(rsCopy))
                        {
                            i = prepareCopyAddressRegisters(code, ++i, rsCopy.get(1));
                            i--;
                            
                            List<String> rsThird = Util.createList(rs.get(0), addressRegister);
                            code.add(++i, Util.generateCommand(instruction.getName(), emptyList, rsCopy, emptyList, offsetCopy, emptyList, instruction.getFormat()));
                            code.add(++i, Util.generateCommand(instruction.getName(), emptyList, rsThird, emptyList, offsetCopy, emptyList, instruction.getFormat()));
                            
                            i = returnCopyAddressRegisters(code, ++i, rsCopy.get(1));
                            i--;
                        }
                    }
                    else if (DB.isStoreToStack(instruction))
                    {
                        //Checking Instructions
                        code.add(i++,savePC());
                        for (List currentRS : rsIntercalate)
                        {
                            if (DB.hasComparisonInstruction())
                            {
                                {
                                    code.add(i++, Util.generateCommand(cmp.getName(), emptyList, currentRS, emptyList, emptyList, emptyList, cmp.getFormat()));
                                }
                                code.add(i++, Util.generateCommand(bne.getName(), emptyList, currentRS, emptyList, emptyList, getErrorLabelName(currentRS), bne.getFormat()));
                            }
                        }
                    }
                    else if (DB.isJump(instruction))
                    {
                        //Instruções de verificação
                        if (target.isEmpty()) System.out.println("Empty list.");
                        else System.out.println(target.get(0));
                        if (target.isEmpty() || !target.get(0).equalsIgnoreCase(DB.ERROR_LABEL_NAME))
                        {
                            for (int j = 0; j < rsIntercalate.size(); j++)
                            {
                                if (freeComparisonArea && !rsIntercalate.get(j).get(0).equals(rsIntercalate.get(j).get(1)))
                                {
                                    code.add(i++,savePC());
                                    if (DB.hasComparisonInstruction())
                                    {
                                        code.add(i++, Util.generateCommand(cmp.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, emptyList, cmp.getFormat()));
                                    }
                                    code.add(i++, Util.generateCommand(bne.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, getErrorLabelName(rsIntercalate.get(j)), bne.getFormat()));

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
                                    code.add(i++,savePC());
                                    //Se usar comparação independente, não haverá registradores na instrução de desvio, logo, nunca vai entrar nesse if
                                    if (DB.hasComparisonInstruction())
                                    {
                                        code.add(i++, Util.generateCommand(cmp.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, emptyList, cmp.getFormat()));
                                    }
                                    code.add(i++, Util.generateCommand(bne.getName(), emptyList, rsIntercalate.get(j), emptyList, emptyList, getErrorLabelName(rsIntercalate.get(j)), bne.getFormat()));

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
                            code.add(++i, Util.generateCommand(move.getName(), rdCopy, rd, emptyList, emptyList, emptyList, move.getFormat()));                         
                            code.add(++i, saveThirdRegister(rd.get(0)));
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
                            code.add(++i, Util.generateCommand(move.getName(), newRD, newRS, emptyList, emptyList, emptyList, move.getFormat()));
                            code.add(++i, saveThirdRegister(newRS.get(0)));
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
                    i = assignRegisterAddress(code, ++i);
                    i--;
                    
                    for (int j = 0; j < preInitializedRegisters.size(); j++)
                    {
                        if (usedRegisters.contains(preInitializedRegisters.get(j)))
                        {
                            List<String> rs = new ArrayList<String>();
                            rs.add(preInitializedRegisters.get(j));

                            List<String> rd = getCopyRegisters(rs);

                            if (!rd.equals(rs))
                            {
                                code.add(++i, Util.generateCommand(move.getName(), rd, rs, emptyList, emptyList, emptyList, move.getFormat()));
                                code.add(++i, saveThirdRegister(rs.get(0)));
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
                        code.add(++i, Util.generateCommand(move.getName(), newRD, newRS, emptyList, emptyList, emptyList, move.getFormat()));
                        listOfTargetsWithHiddenRD.remove(j);
                        code.add(++i, saveThirdRegister(newRS.get(0)));
                    }
                }
            }
        }
        
        code = insertErrorRecoverySubroutines(code);

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
    
    public static String getRegisterOffset(String register)
    {
        List<String> registerList = Util.createList(register);
        return getRegisterOffset(registerList).get(0);
    }
    
    public static List<String> getRegisterOffset(List<String> registers)
    {
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < registers.size(); i++)
        {
            try
            {
                list.add(registerOffset.get(usedRegisters.indexOf(registers.get(i))));
            }
            catch (Exception error)
            {
                list.add(null);
            }
        }
        
        return list;
    }
    
    public static List<String> getErrorLabelName(List<String> register)
    {
        List<String> list = new ArrayList<String>();
        
        try
        {
            list.add(DB.ERROR_LABEL_NAME + getRegisterOffset(register).get(0));
        }
        catch (Exception error)
        {
            list.add(DB.ERROR_LABEL_NAME);
        }
        
        return list;
    }
    
    private static int savePC(List<String> code, int i)
    {
        code.add(i++, savePC());
        
        return i;
    }
    
    private static String savePC()
    {
        List<String> offset = Util.createList(PC_OFFSET); //standard pc offset set to zero
        //List<String> rs = Util.createList(DB.getPCRegisterName());
        List<String> rs = Util.createList(PC_NAME, addressRegister);
        
        return Util.generateCommand(store.getName(), emptyList, rs, emptyList, offset, emptyList, store.getFormat());
    }
    
    private static String saveThirdRegister(String register)
    {
        List<String> storeRS = Util.createList(register, addressRegister);
        List<String> storeOffset = Util.createList(getRegisterOffset(register));
        
        return Util.generateCommand(store.getName(), emptyList, storeRS, emptyList, storeOffset, emptyList, store.getFormat());
    }
    
    private static int loadTripleCheck(List<String> code, int i, List<String> rd, List<String> rdCopy, List<String> rs, List<String> rsCopy, List<String> offset)
    {
        int index = i;
        String labelFormat = DB.getLabelFormat();
        
        try
        {
            Integer.valueOf(offset.get(0));
        }
        catch (Exception error)
        {
            offset = Util.createList("0");
        }
        
        
        //CMP Rod, Ra
        //BEQ label_load_end_I
        List<String> listRodRa = Util.createList(rd.get(0), addressRegister);
        String targetEnd = "label_load_end_"+index;
        String labelEnd = labelFormat.replace("label", targetEnd);
        List<String> targetEndList = Util.createList(targetEnd);
        if (DB.hasComparisonInstruction())
        {
            code.add(i++, Util.generateCommand(cmp.getName(), emptyList, listRodRa, emptyList, emptyList, emptyList, cmp.getFormat()));
        }
        code.add(i++, Util.generateCommand(beq.getName(), emptyList, listRodRa, emptyList, emptyList, targetEndList, beq.getFormat()));
        
        //CMP Rod, Rcd
        //BNE label_load_issue_I
        List<String> listRodRcd = Util.createList(rd.get(0), rdCopy.get(0));
        String targetIssue = "label_load_issue_"+index;
        String labelIssue = labelFormat.replace("label", targetIssue);
        List<String> targetIssueList = Util.createList(targetIssue);
        if (DB.hasComparisonInstruction())
        {
            code.add(i++, Util.generateCommand(cmp.getName(), emptyList, listRodRcd, emptyList, emptyList, emptyList, cmp.getFormat()));
        }
        code.add(i++, Util.generateCommand(bne.getName(), emptyList, listRodRcd, emptyList, emptyList, targetIssueList, bne.getFormat()));
        
        //ADD Ra Rc LOWER_OFFSET_3RD
        code.add(i++, Util.generateCommand(addImm.getName(), addressRegisterList, rsCopy, lowerOffsetList3rd, emptyList, emptyList, addImm.getFormat()));
        //if second and third are not in the same memory block
        if (!HIGHER_OFFSET_2ND.equals(HIGHER_OFFSET_3RD))
        {
            //MOVT Ra HIGHER_OFFSET_3RD
            code.add(i++, Util.generateCommand(moveHigh.getName(), addressRegisterList, emptyList, higherOffsetList3rd, emptyList, emptyList, moveHigh.getFormat()));
        }
        
        //STR Rod, [Ra, #OFFSET]
        code.add(i++, Util.generateCommand(store.getName(), emptyList, listRodRa, emptyList, offset, emptyList, store.getFormat()));
        
        //B label_load_end_I
        code.add(i++, Util.generateCommand(jump.getName(), emptyList, emptyList, emptyList, emptyList, targetEndList, jump.getFormat()));
        
        //label_load_issue_I:
        code.add(i++, labelIssue);
        
        //CMP Rcd, Ra
        //BNE label_load_guess_I
        List<String> listRcdRa = Util.createList(rdCopy.get(0), addressRegister);
        String targetGuess = "label_load_guess_"+index;
        String labelGuess = labelFormat.replace("label", targetGuess);
        List<String> targetGuessList = Util.createList(targetGuess);
        if (DB.hasComparisonInstruction())
        {
            code.add(i++, Util.generateCommand(cmp.getName(), emptyList, listRcdRa, emptyList, emptyList, emptyList, cmp.getFormat()));
        }
        code.add(i++, Util.generateCommand(bne.getName(), emptyList, listRcdRa, emptyList, emptyList, targetGuessList, bne.getFormat()));
        
        //MOV Rod, Rcd
        code.add(i++, Util.generateCommand(move.getName(), rd, rdCopy, emptyList, emptyList, emptyList, move.getFormat()));
        
        //STR Rod, [Ro, #OFFSET]
        List<String> listRodRd = Util.createList(rd.get(0), rs.get(0));
        code.add(i++, Util.generateCommand(store.getName(), emptyList, listRodRd, emptyList, offset, emptyList, store.getFormat()));
        
        //B label_load_end_I
        code.add(i++, Util.generateCommand(jump.getName(), emptyList, emptyList, emptyList, emptyList, targetEndList, jump.getFormat()));
        
        //label_load_guess_I:
        code.add(i++, labelGuess);
        
        //MOV Rcd, Rod
        code.add(i++, Util.generateCommand(move.getName(), rdCopy, rd, emptyList, emptyList, emptyList, move.getFormat()));
        
        //ADD Ra Rc LOWER_OFFSET_3RD
        code.add(i++, Util.generateCommand(addImm.getName(), addressRegisterList, rsCopy, lowerOffsetList3rd, emptyList, emptyList, addImm.getFormat()));
        //if second and third are not in the same memory block
        if (!HIGHER_OFFSET_2ND.equals(HIGHER_OFFSET_3RD))
        {
            //MOVT Ra HIGHER_OFFSET_3RD
            code.add(i++, Util.generateCommand(moveHigh.getName(), addressRegisterList, emptyList, higherOffsetList3rd, emptyList, emptyList, moveHigh.getFormat()));
        }
        
        //STR Rcd, [Rc, #OFFSET]
        List<String> listRcdRc = Util.createList(rdCopy.get(0), rsCopy.get(0));
        code.add(i++, Util.generateCommand(store.getName(), emptyList, listRcdRc, emptyList, offset, emptyList, store.getFormat()));
        
        //STR Rod, [Ra, #OFFSET]
        code.add(i++, Util.generateCommand(store.getName(), emptyList, listRodRa, emptyList, offset, emptyList, store.getFormat()));
        
        //label_load_end_I:
        code.add(i++, labelEnd);
        
        return i;
    }
    
    private static int prepareCopyAddressRegisters(List<String> code, int i, List<String> copyRegisterList)
    {
        int index = copyRegisters.indexOf(copyRegisterList.get(0));
        String offsetN = registerOffset.get(index);
        List<String> offsetNList = Util.createList(offsetN);
        
        //ADD Rc Rc LOWER_OFFSET_2ND
        code.add(i++, Util.generateCommand(addImm.getName(), copyRegisterList, copyRegisterList, lowerOffsetList2nd, emptyList, emptyList, addImm.getFormat()));
        
        //ADD Ra Rc LOWER_OFFSET_3RD
        code.add(i++, Util.generateCommand(addImm.getName(), addressRegisterList, copyRegisterList, lowerOffsetList3rd, emptyList, emptyList, addImm.getFormat()));
        
        /*
        //MOV Ra LOWER_OFFSET_2ND
        code.add(i++, Util.generateCommand(moveImm.getName(), addressRegisterList, emptyList, lowerOffsetList2nd, emptyList, emptyList, moveImm.getFormat()));
        
        //ADD Rc Rc Ra
        List<String> listRcRa = Util.createList(copyRegisterList.get(0), addressRegister);
        code.add(i++, Util.generateCommand(addRegs.getName(), copyRegisterList, listRcRa, emptyList, emptyList, emptyList, addRegs.getFormat()));
        
        //MOV Ra Rc
        code.add(i++, Util.generateCommand(move.getName(), addressRegisterList, copyRegisters, emptyList, emptyList, emptyList, move.getFormat()));
        
        //ADD Ra Ra LOWER_OFFSET_3RD
        code.add(i++, Util.generateCommand(addImm.getName(), addressRegisterList, addressRegisterList, lowerOffsetList3rd, emptyList, emptyList, addImm.getFormat()));
        */
        /*
        //MOVW Rc HIGHER_OFFSET_2ND
        code.add(i++, Util.generateCommand(moveHigh.getName(), copyRegisterList, emptyList, higherOffsetList2nd, emptyList, emptyList, moveHigh.getFormat()));
        
        //ADD Rc Rc LOWER_OFFSET_2ND
        code.add(i++, Util.generateCommand(addImm.getName(), copyRegisterList, copyRegisterList, lowerOffsetList2nd, emptyList, emptyList, addImm.getFormat()));
        
        //ADD Ra Rc LOWER_OFFSET_3RD
        code.add(i++, Util.generateCommand(addImm.getName(), addressRegisterList, copyRegisterList, lowerOffsetList3rd, emptyList, emptyList, addImm.getFormat()));
        
        //if second and third are not in the same memory block
        if (!HIGHER_OFFSET_2ND.equals(HIGHER_OFFSET_3RD))
        {
            //MOVW Ra HIGHER_OFFSET_3RD
            code.add(i++, Util.generateCommand(moveHigh.getName(), addressRegisterList, emptyList, higherOffsetList3rd, emptyList, emptyList, moveHigh.getFormat()));
        }
        */
        return i;
    }
    
    private static int prepareCopyAddressRegisters(List<String> code, int i, String copyRegister)
    {
        List<String> copyRegisterList = Util.createList(copyRegister);
        
        return prepareCopyAddressRegisters(code, i, copyRegisterList);
    }
    
    private static int returnCopyAddressRegisters(List<String> code, int i, List<String> copyRegisterList)
    {
        int index = copyRegisters.indexOf(copyRegisterList.get(0));
        List<String> originalRegisterList = Util.createList(usedRegisters.get(index));
        
        //MOV Rc, Ro
        code.add(i++, Util.generateCommand(move.getName(), copyRegisterList, originalRegisterList, emptyList, emptyList, emptyList, move.getFormat()));
        
        //MOV Ra, #ADDRESS
        i = assignRegisterAddress(code, i);
        
        return i;
    }
    
    private static int returnCopyAddressRegisters(List<String> code, int i, String copyRegister)
    {
        List<String> copyRegisterList = Util.createList(copyRegister);
        
        return returnCopyAddressRegisters(code, i, copyRegisterList);
    }
    
    private static int assignRegisterAddress(List<String> code, int i)
    {
        //MOV Ra, #ADDRESS
        code.add(i++, Util.generateCommand(moveLow.getName(), addressRegisterList, emptyList, lowerHalfAddressList, emptyList, emptyList, moveLow.getFormat()));
        code.add(i++, Util.generateCommand(moveHigh.getName(), addressRegisterList, emptyList, higherHalfAddressList, emptyList, emptyList, moveHigh.getFormat()));
        
        return i;
    }
    
    private static void assignRegisterAddress(List<String> code)
    {
        assignRegisterAddress(code, code.size());
    }
    
    private static List<String> insertErrorRecoverySubroutines(List<String> code)
    {
        for (int i = 0; i < registerOffset.size(); i++)
        {
            code = insertErrorRecoverySubroutine(code, usedRegisters.get(i));
        }
        
        return code;
    }
    
    private static List<String> insertErrorRecoverySubroutine(List<String> code, String register)
    {
        String offset, copyRegister;
        try
        {
            int index = usedRegisters.indexOf(register);
            offset = registerOffset.get(index);
            copyRegister = copyRegisters.get(index);
            
            List<String> registerList = Util.createList(register);
            List<String> copyRegisterList = Util.createList(copyRegister);
            List<String> offsetList = Util.createList(offset);
            List<String> errorLabelNameList = getErrorLabelName(registerList);
            
            //generateCommand(String instruction, List<String> rd, List<String> rs, List<String> imm, List<String> offset, List<String> target, String format)
            
            //errorN:
            String labelFormat = DB.getLabelFormat();
            String labelErrorN = labelFormat.replace("label", errorLabelNameList.get(0));
            code.add(labelErrorN);
            
            //MOV Ra, #ADDRESS
            assignRegisterAddress(code);
            
            //LDR Ra [Ra, #N]
            code.add(Util.generateCommand(load.getName(), addressRegisterList, addressRegisterList, emptyList, offsetList, emptyList, load.getFormat()));
            
            //CMP Rc, Ra
            //BNE errorN_if
            List<String> rs = Util.createList(copyRegister, addressRegister);
            String targetN_if = errorLabelNameList.get(0) + "_if";
            List<String> targetN_ifList = Util.createList(targetN_if);
            if (DB.hasComparisonInstruction())
            {
                code.add(Util.generateCommand(cmp.getName(), emptyList, rs, emptyList, emptyList, emptyList, cmp.getFormat()));
            }
            code.add(Util.generateCommand(bne.getName(), emptyList, rs, emptyList, emptyList, targetN_ifList, bne.getFormat()));
            //code.add(Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
            
            //MOV Ro, Rc
            code.add(Util.generateCommand(move.getName(), registerList, copyRegisterList, emptyList, emptyList, emptyList, move.getFormat()));
            
            //B errorN_end
            String targetN_end = errorLabelNameList.get(0) + "_end";
            List<String> targetN_endList = Util.createList(targetN_end);
            code.add(Util.generateCommand(jump.getName(), emptyList, emptyList, emptyList, emptyList, targetN_endList, jump.getFormat()));
            //code.add(Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
            
            //errorN_if: (label)
            String labelErrorN_if = labelFormat.replace("label", targetN_if);
            code.add(labelErrorN_if);
            
            //MOV Ro, Rc
            code.add(Util.generateCommand(move.getName(), copyRegisterList, registerList, emptyList, emptyList, emptyList, move.getFormat()));
            
            //errorN_end: (label)
            String labelErrorN_end = labelFormat.replace("label", targetN_end);
            code.add(labelErrorN_end);
            
            //MOV Ra, #ADDRESS
            assignRegisterAddress(code);
            
            //LDR PC, [Ra, #0]
            code.add(Util.generateCommand(load.getName(), pcList, addressRegisterList, emptyList, pcOffsetList, emptyList, load.getFormat()));
            //code.add(Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
        }
        catch (Exception error) { }
        
        return code;
    }
}
