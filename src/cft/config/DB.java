/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.config;

import cft.util.Instruction;
import cft.util.Register;
import cft.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Eduardo
 */
public class DB
{
    public static String BASE[] = Architecture.BASE;
    public static String BASE_PATTERNS[] = Architecture.BASE_PATTERNS;
    public static final String ERROR_LABEL_NAME = "error";
    private static String assemblyFilename = "assembly.s";
    private static String dumpFilename = "dump.s";
    private static Architecture arch = new Architecture();
    private static Techniques tech = new Techniques();
    private static Dump dump;
    
    public static void showInstructions()
    {
        arch.showInstructions();
    }
    
    /*
     * Informações sobre a arquitetura e organização do processador
     */
    
    public static void addInstruction(Instruction instruction)
    {
        arch.addInstruction(instruction);
    }
    
    public static void addRegister(Register register)
    {
        arch.addRegister(register);
    }
    
    /*
     * Getters
     */
    
    public static String extractInstruction(String line)
    {
        String instruction = new String();
        Pattern pattern = Pattern.compile("(\\S+)(:?(?:\\s+.+)*)");
        line = line.trim();
        Matcher matcher = pattern.matcher(line);
        
        if (matcher.matches())
        {
            //pattern = Pattern.compile("\\w+");
            //matcher = pattern.matcher(line);
            //if (matcher.find())
            //{
            if (matcher.groupCount() > 1)
            {
                instruction = matcher.group(1);
            }
            //}
        }

        return instruction;
    }
    
    
    public static List<String> generateListOfMoveCommands(List<String> rd, List<String> rs)
    {
        Instruction move = getMoveInstruction();
        List<String> emptyList = new ArrayList<String>();
        List<String> list = new ArrayList<String>();
        
        for (int i = 0; i < rd.size(); i++)
        {
            List<String> newRD = new ArrayList<String>();
            List<String> newRS = new ArrayList<String>();
            newRD.add(rd.get(i));
            newRS.add(rs.get(i));
            list.add(Util.generateCommand(move.getName(), newRD, newRS, emptyList, emptyList, emptyList, move.getFormat()));
        }
        
        return list;
    }
    
    public static Instruction getAddImmediateInstruction()    
    {
        return arch.getAddImmediateInstruction();
    }
    
    public static Instruction getAddRegistersInstruction()    
    {
        return arch.getAddRegistersInstruction();
    }
    
    public static Instruction getAndImmediateInstruction()    
    {
        return arch.getAndImmediateInstruction();
    }
    
    public static List<Integer> getAllFunctionCallsPositions(List<String> code, String register)
    {
        List<Integer> posAllFunctionCallList = new ArrayList<Integer>();
        
        for (int i = 0; i < code.size(); i++)
        {
            if (DB.isInstruction(code.get(i)))
            {
                Instruction instruction = DB.getInstruction(code.get(i));
                if (DB.isFunctionCall(instruction.getName()))
                {
                    String[] hiddenRD = instruction.getHiddenRD();
                    for (int j = 0; j < hiddenRD.length; j++)
                    {
                        if (hiddenRD[j].equals(register))
                        {
                            posAllFunctionCallList.add(i);
                            break;
                        }
                    }
                }
            }
        }
        
        return posAllFunctionCallList;
    }
    
    public static Architecture getArchitecture()
    {
        return arch;
    }
    
    public static String getAssemblyFilename()
    {
        return assemblyFilename;
    }
    
    public static String getAssemblyFilenameOut()
    {
        StringBuilder assemblyFilenameOut = new StringBuilder();
        String title = Util.getFileTitle(assemblyFilename);
        String extension = Util.getFileExtension(assemblyFilename);
        List<String> techniques = getTechniques();
        int pos = title.lastIndexOf('_');
        if (pos != -1)
        {
            assemblyFilenameOut.append(title.substring(0, pos));
            if (techniques.isEmpty())
            {
                assemblyFilenameOut.append("None");
            }
            else
            {
                for (int i = 0; i < techniques.size(); i++)
                {
                    assemblyFilenameOut.append(techniques.get(i).replace('+', 'p'));
                    
                    if (techniques.get(i).equals("SETA"))
                    {
                        if (DB.getSetaTunnelEffectSelectionByPercentage())
                        {
                            if (DB.getSetaTunnelEffectPercentage() < 1) assemblyFilenameOut.append('T').append(DB.getSetaTunnelEffectPercentage());
                        }
                        else
                        {
                            if (DB.getSetaMinNumberOfInstructions() > 1) assemblyFilenameOut.append('M').append(DB.getSetaMinNumberOfInstructions());
                        }
                        
                        if (DB.getSetaPercentageToVerify() < 1) assemblyFilenameOut.append('V').append(DB.getSetaPercentageToVerify());
                    }
                }
            }
            assemblyFilenameOut.append(title.substring(pos));
        }
        else
        {
            assemblyFilenameOut.append(title).append("None");
        }
        return assemblyFilenameOut.append('.').append(extension).toString();
    }
    
    public static int getBranchDelaySlot()
    {
        return arch.getBranchDelaySlot();
    }
    
    public static Instruction getBranchNotEqualZeroInstruction()    
    {
        return arch.getBranchNotEqualZeroInstruction();
    }
    
    
    
    public static List<String> getCallsToSubroutine(List<String> code)
    {
        List<String> list = new ArrayList<String>();
        
        for (int i = 0; i < code.size(); i++)
        {
            if (DB.isInstruction(code.get(i)))
            {
                Instruction instruction = DB.getInstruction(code.get(i));
                if (DB.isFunctionCall(instruction.getName()))
                {
                    if ((instruction.getHiddenRD().length > 0) && (!list.contains(instruction.getName())))
                    {
                        list.add(instruction.getName());
                        break;
                    }
                }
            }
        }
        
        return list;
    }
    
    public static String getCommentTag()
    {
        return arch.getCommentTag();
    }
    
    public static Instruction getComparisonInstruction()
    {
        return arch.getComparisonInstruction();
    }
    
    public static Instruction getComparisonWithImmediateInstruction()
    {
        return arch.getComparisonWithImmediateInstruction();
    }
    
    public static String getDataPattern()
    {
        return arch.getDataPattern();
    }
    
    public static Dump getDump()
    {
        return dump;
    }
    
    public static String getDumpFilename()
    {
        return dumpFilename;
    }
    
    public static Instruction getEqualityComparisonInstruction()    
    {
        return arch.getEqualityComparisonInstruction();
    }
    
    public static List<String> getErrorFunction()
    {
        List<String> errorFunction = new ArrayList<String>();
        List emptyList = new ArrayList();
        
        errorFunction.add(Util.replaceFirst(arch.getLabelFormat(), "label", ERROR_LABEL_NAME));
        
        if (isRegister(getErrorRegister()))
        {
            Instruction loadImmediate = getImmediateAssignmentInstruction();
            List<String> rd = new ArrayList<String>();
            rd.add(getErrorRegister());
            List<String> immediateValue = new ArrayList<String>();
            immediateValue.add(getErrorValue());
            errorFunction.add(Util.generateCommand(loadImmediate.getName(), rd, emptyList, immediateValue, emptyList, emptyList, loadImmediate.getFormat()));
        }
        
        Instruction jumpInstruction = getJumpInstruction();
        List<String> errorLabel = new ArrayList<String>();
        errorLabel.add(ERROR_LABEL_NAME);
        errorFunction.add(Util.generateCommand(jumpInstruction.getName(), emptyList, emptyList, emptyList, emptyList, errorLabel, jumpInstruction.getFormat()));
        
        Instruction nop = getNoOperationInstruction();
        for (int i = 0; i < getBranchDelaySlot(); i++)
        {
            errorFunction.add(Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
        }

        return errorFunction;
    }
    
    public static List<String> getFunctionCalls()
    {
        return arch.getFunctionCalls();
    }
        
    public static List<String> getGeneralPurposeRegisters()
    {
        List<Register> registers = arch.getRegisters();
        List<String> generalPurposeRegisters = new ArrayList<String>();
        
        for (int i = 0; i < registers.size(); i++)
        {
            if (registers.get(i).isWritable() && registers.get(i).isReadable() && (isGlobalRegister(registers.get(i)) || isLocalRegister(registers.get(i))))
            {
                generalPurposeRegisters.add(registers.get(i).getName());
            }
        }
        
        return generalPurposeRegisters;
    }
    
    public static Instruction getHigherHalfImmediateAssignmentInstruction()
    {
        return arch.getHigherHalfImmetiadeAssignmentInstruction();
    }
    
    public static Instruction getImmediateAssignmentInstruction()
    {
        return arch.getImmetiadeAssignmentInstruction();
    }
    
    public static Instruction getInstruction(String line)
    {
        Instruction instruction = new Instruction();
        
        line = Util.removeExtraWhitespaces(Util.getBefore(line, getCommentTag()));
        
        if (isInstruction(line))
        {
            List<Instruction> list = DB.getInstructions(DB.extractInstruction(line));
            for (int i = 0; i < list.size(); i++)
            {
                List<String> rd = Util.getIt(line, list.get(i).getFormat(), "rd", DB.BASE);
                List<String> rs = Util.getIt(line, list.get(i).getFormat(), "rs", DB.BASE);
                int rdComparisonSize = Util.getIt(list.get(i).getFormat(), list.get(i).getFormat(), "rd", DB.BASE).size();
                int rsComparisonSize = Util.getIt(list.get(i).getFormat(), list.get(i).getFormat(), "rs", DB.BASE).size();
                boolean valid = true;
                
                if ((rd.size() != rdComparisonSize) || (rs.size() != rsComparisonSize))
                {
                    valid = false;
                }
                
                for (int j = 0; (j < rd.size()) && valid; j++)
                {
                    if (!isRegister(rd.get(j)))
                    {
                        valid = false;
                        break;
                    }
                }
                
                for (int j = 0; (j < rs.size()) && valid; j++)
                {
                    if (!isRegister(rs.get(j)))
                    {
                        valid = false;
                        break;
                    }
                }
                
                /*if (line.equals("str r3, [fp, #-20]"))
                {
                    System.out.println(list.get(i).getName() + "\t" + list.get(i).getFormat());
                    System.out.println("RD: "+rd+"\tRS: "+rs);
                    System.out.println("Valid: "+valid);
                }*/
                
                /*if (list.get(i).getType().equalsIgnoreCase("load") || list.get(i).getType().equalsIgnoreCase("store"))
                {
                    valid = valid && (Util.numberOfOccurrences(line, '+') == Util.numberOfOccurrences(list.get(i).getFormat(), '+'));
                    valid = valid && (Util.numberOfOccurrences(line, '-') == Util.numberOfOccurrences(list.get(i).getFormat(), '-'));
                }*/
                
                if (valid)
                {
                    instruction = list.get(i);
                    break;
                }
            }
        }
        
        return instruction;
    }
    
    public static List<Instruction> getInstructions(String name)
    {
        return arch.getInstruction(name);
    }
    
    public static List<String> getInstructionFormat()
    {
        return arch.getInstructionFormat();
    }
    
    public static Instruction getInvertedInstruction(Instruction instruction)
    {
        return arch.getInvertedInstruction(instruction).get(0);
    }
    
    public static String getLabel(String line)
    {
        String label = new String();
        List<String> it = Util.getIt(line, arch.getLabelFormat(), "label", BASE);

        if (it.size() > 0)
        {
            label = it.get(0);
        }
        
        return label;
    }
    
    public static String getLabelFormat()
    {
        return arch.getLabelFormat();
    }
    
    public static List<String> getImmediate(String line)
    {
        Instruction instruction = DB.getInstruction(line);
        
        return getImmediate(line, instruction.getFormat());
    }
    
    public static List<String> getImmediate(String line, String format)
    {
        return Util.getIt(line, format, "imm", BASE);
    }
    
    public static Instruction getJumpInstruction()
    {
        return arch.getJumpInstruction();
    }
    
    public static Instruction getLoadInstruction()    
    {
        return arch.getLoadInstruction();
    }
    
    public static Instruction getLowerHalfImmediateAssignmentInstruction()
    {
        return arch.getLowerHalfImmetiadeAssignmentInstruction();
    }
    
    public static String getMainLabelName()
    {
        return arch.getMainLabelName();
    }
    
    public static int getMainPosition(List<String> code)
    {
        String mainLabel = getMainLabelName();
        
        for (int position = 0; position < code.size(); position++)
        {
            String line = code.get(position);
            if (isLabel(line))
            {
                String label = getLabel(line);
                if (label.equals(mainLabel)) return position;
            }
        }
        
        return -1;
    }
    
    public static Instruction getMoveInstruction()
    {
        return arch.getMoveInstruction();
    }
        
    public static Instruction getNonEqualityComparisonInstruction()    
    {
        return arch.getNonEqualityComparisonInstruction();
    }
    
    public static Instruction getNoOperationInstruction()
    {
        return arch.getNoOperationInstruction();
    }
    
    public static List<String> getOffset(String line)
    {
        Instruction instruction = DB.getInstruction(line);
        
        return getOffset(line, instruction.getFormat());
    }
    
    public static List<String> getOffset(String line, String format)
    {
        return Util.getIt(line, format, "offset", BASE);
    }
    
    public static List<String> getOperands(String line)
    {
        List<String> operands = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\w+\\s+.+");
        line = line.trim();
        Matcher matcher = pattern.matcher(line);

        if (matcher.matches())
        {
            pattern = Pattern.compile("\\s.+");
            matcher = pattern.matcher(line);
            if (matcher.find())
            {
                line = Util.removeWhitespaces(matcher.group());
                operands = Arrays.asList(line.split(","));
            }
        }
        
        return operands;
    }
    
    public static List<String> getPreInitializedRegisters()
    {
        List<Register> registers = arch.getPreInitializedRegisters();
        List<String> names = new ArrayList<String>();
        
        for (int i = 0; i < registers.size(); i++)
        {
            names.add(registers.get(i).getName());
        }
        
        return names;
    }
    
    public static int getPreviousLabelPosition(List<String> code, int posStart)
    {
        for (int i = posStart; i >= 0; i--)
        {
            if (DB.isLabel(code.get(i)))
            {
                return i;
            }
        }
        
        return -1;
    }
    
    public static List<String> getRegisters()
    {
        List<Register> registers = arch.getRegisters();
        List<String> names = new ArrayList<String>();
        
        for (int i = 0; i < registers.size(); i++)
        {
            names.add(registers.get(i).getName());
        }
        
        return names;
    }
    
    public static Register getRegisterZero()    
    {
        return arch.getRegisterZero();
    }
    
    public static List<String> getRD(String line)
    {
        Instruction instruction = DB.getInstruction(line);
        
        return getRD(line, instruction.getFormat());
    }
    
    public static List<String> getRD(String line, String format)
    {
        List<String> rdList = Util.getIt(line, format, "rd", BASE);
        List<String> rsdList = Util.getIt(line, format, "rsd", BASE);
        rdList.addAll(rsdList);
        return rdList;
    }
    
    public static List<String> getRS(String line)
    {
        Instruction instruction = DB.getInstruction(line);
        
        return getRS(line, instruction.getFormat());
    }
    
    public static List<String> getRS(String line, String format)
    {
        List<String> rsList = Util.getIt(line, format, "rs", BASE);
        List<String> rsdList = Util.getIt(line, format, "rsd", BASE);
        rsList.addAll(rsdList);
        List<String> targetList = Util.getIt(line, format, "target", BASE);
        
        for (int i = 0; i < targetList.size(); i++)
        {
            String register = targetList.get(i);
            if (DB.isRegister(register) && !rsList.contains(register))
            {
                rsList.add(register);
            }
        }
        
        return rsList;
    }
    
    public static Instruction getShiftLeftImmediateInstruction()    
    {
        return arch.getShiftLeftImmediateInstruction();
    }
    
    public static Instruction getShiftRightArithmeticImmediateInstruction()    
    {
        return arch.getShiftRightArithmeticImmediateInstruction();
    }
    
    public static Instruction getShiftRightImmediateInstruction()    
    {
        return arch.getShiftRightImmediateInstruction();
    }
    
    public static Instruction getStoreInstruction()    
    {
        return arch.getStoreInstruction();
    }
    
    public static Instruction getSubImmediateInstruction()    
    {
        return arch.getSubImmediateInstruction();
    }
    
    /*public static String getTarget(String line)
    {
        List<String> format = arch.getInstructionFormat(extractInstruction(line));
        String target = new String();
        for (int i = 0; i < format.size(); i++)
        {
            List<String> it = Util.getIt(line, format.get(i), "target", BASE);
            for (int j = it.size() - 1; j >= 0; j--)
            {
                target = it.get(j);
                if (target.length() > 0) break;
            }
            if (target.length() > 0) break;
        }
        
        return target;
    }*/
    
    public static List<String> getTarget(String line)
    {
        Instruction instruction = DB.getInstruction(line);
        
        return getTarget(line, instruction.getFormat());
    }
    
    public static List<String> getTarget(String line, String format)
    {
        return Util.getIt(line, format, "target", BASE);
    }
    
    public static String getTargetProcessor()
    {
        return arch.getTargetProcessor();
    }
    
    public static List<String> getUsedRegisters(List<String> code)
    {
        List<String> usedRegisters = new ArrayList<String>();
        List<Integer> rdCounter = new ArrayList<Integer>();
        List<Integer> rsCounter = new ArrayList<Integer>();
        
        //Adicionar registradores utilizados antes do "main" à lista
        //usedRegisters.addAll(getPreInitializedRegisters());
        //if (isRegister(getErrorRegister()) && !usedRegisters.contains(getErrorRegister())) usedRegisters.add(tech.getErrorRegister());
        
        //Para toda linha de código
        for (int i = 0; i < code.size(); i++)
        {
            String line = code.get(i);
            
            if (DB.isInstruction(line))
            {
                //List<Instruction> list = DB.getInstruction(DB.extractInstruction(line));
                //Instruction instruction = list.get(0);
                Instruction instruction = DB.getInstruction(line);
                //System.out.println("LINE:\t"+line);
                //System.out.println("INSTRUCTION:\t"+instruction.getName());
                //System.out.println("FORMAT:\t"+instruction.getFormat());
                List<String> rd = Util.getIt(line, instruction.getFormat(), "rd", DB.BASE);
                List<String> rs = Util.getIt(line, instruction.getFormat(), "rs", DB.BASE);
                List<String> rsd = Util.getIt(line, instruction.getFormat(), "rsd", DB.BASE);
                rd.addAll(rsd);
                rs.addAll(rsd);
                String hiddenRD[] = instruction.getHiddenRD();
                String hiddenRS[] = instruction.getHiddenRS();
                
                for (int j = 0; j < rd.size(); j++)
                {
                    if (!usedRegisters.contains(rd.get(j)))
                    {
                        usedRegisters.add(rd.get(j));
                        rdCounter.add(1);
                        rsCounter.add(0);
                    }
                    else
                    {
                        int pos = usedRegisters.indexOf(rd.get(j));
                        rdCounter.set(pos, rdCounter.get(pos) + 1);
                    }
                }
                
                for (int j = 0; j < rs.size(); j++)
                {
                    if (!usedRegisters.contains(rs.get(j)))
                    {
                        usedRegisters.add(rs.get(j));
                        rdCounter.add(0);
                        rsCounter.add(1);
                    }
                    else
                    {
                        int pos = usedRegisters.indexOf(rs.get(j));
                        rsCounter.set(pos, rsCounter.get(pos) + 1);
                    }
                }
                
                for (int j = 0; j < hiddenRD.length; j++)
                {
                    if (!usedRegisters.contains(hiddenRD[j]))
                    {
                        usedRegisters.add(hiddenRD[j]);
                        rdCounter.add(1);
                        rsCounter.add(0);
                    }
                    else
                    {
                        int pos = usedRegisters.indexOf(hiddenRD[j]);
                        rdCounter.set(pos, rdCounter.get(pos) + 1);
                    }
                }
                
                for (int j = 0; j < hiddenRS.length; j++)
                {
                    if (!usedRegisters.contains(hiddenRS[j]))
                    {
                        usedRegisters.add(hiddenRS[j]);
                        rdCounter.add(0);
                        rsCounter.add(1);
                    }
                    else
                    {
                        int pos = usedRegisters.indexOf(hiddenRS[j]);
                        rsCounter.set(pos, rsCounter.get(pos) + 1);
                    }
                }
            }
        }
        
        //Adicionar registradores utilizados antes do "main" à lista
        /*List<String> preInitializedRegisters = getPreInitializedRegisters();
        for (int i = 0; i < preInitializedRegisters.size(); i++)
        {
            if (!usedRegisters.contains(preInitializedRegisters.get(i)))
            {
                usedRegisters.add(preInitializedRegisters.get(i));
                rdCounter.add(0);
                rsCounter.add(0);
            }
        }*/
        
        if (isRegister(getErrorRegister()) && !usedRegisters.contains(getErrorRegister()))
        {
            usedRegisters.add(tech.getErrorRegister());
            rdCounter.add(0);
            rsCounter.add(0);
        }
        
        List<String> sortedList = new ArrayList<String>();
        String priorityMode = getPriorityMode();
        
        for (int pos = 0; pos < usedRegisters.size(); pos++)
        {
            System.out.println(usedRegisters.get(pos) + " | RS: " + rsCounter.get(pos) + " | RD: " + rdCounter.get(pos));
        }
        
        if (priorityMode.equals("first"))
        {
            sortedList = usedRegisters;
        }
        else if(priorityMode.equals("custom"))
        {
            //ordenar por prioridade

            //lista de registradores por prioridade
            List<String> registersByPriority = getRegistersByPriority();

            //os registradores que não foram informados na lista de prioridades serão ordenados no modo "all"
            List<Integer> allCounter = new ArrayList<Integer>();
            for (int i = 0; i < rdCounter.size(); i++)
            {
                allCounter.add(rdCounter.get(i) + rsCounter.get(i));
            }

            //organizar por prioridade
            for (int i = 0; i < registersByPriority.size(); i++)
            {
                int pos = usedRegisters.indexOf(registersByPriority.get(i));
                if (pos >= 0)
                {
                    sortedList.add(usedRegisters.get(pos));
                    usedRegisters.remove(pos);
                    allCounter.remove(pos);
                }
            }

            //organizar restantes pelo modo all
            while (!usedRegisters.isEmpty())
            {
                int pos = Util.getMaxPosition(allCounter);
                sortedList.add(usedRegisters.get(pos));
                usedRegisters.remove(pos);
                allCounter.remove(pos);
            }
        }
        else if(priorityMode.equals("source"))
        {
            //ordenar por rsCounter
            while (!usedRegisters.isEmpty())
            {
                int pos = Util.getMaxPosition(rsCounter);
                sortedList.add(usedRegisters.get(pos));
                usedRegisters.remove(pos);
                rsCounter.remove(pos);
            }
        }
        else if(priorityMode.equals("target"))
        {
            //ordenar por rdCounter
            while (!usedRegisters.isEmpty())
            {
                int pos = Util.getMaxPosition(rdCounter);
                sortedList.add(usedRegisters.get(pos));
                usedRegisters.remove(pos);
                rdCounter.remove(pos);
            }
        }
        else //all
        {
            //ordenar pela soma de rdCounter e rsCounter
            List<Integer> allCounter = new ArrayList<Integer>();
            for (int i = 0; i < rdCounter.size(); i++)
            {
                allCounter.add(rdCounter.get(i) + rsCounter.get(i));
            }

            while (!usedRegisters.isEmpty())
            {
                int pos = Util.getMaxPosition(allCounter);
                sortedList.add(usedRegisters.get(pos));
                usedRegisters.remove(pos);
                allCounter.remove(pos);
            }
        }
        
        return sortedList;
    }
    
    public static int getWordSize()
    {
        return arch.getWordSize();
    }
    
    public static Instruction getXorImmediateInstruction()    
    {
        return arch.getXorImmediateInstruction();
    }
    
    public static boolean hasComparisonInstruction()
    {
        return arch.hasComparisonInstruction();
    }
    
    public static boolean hasComparisonWithImmediateInstruction()
    {
        return arch.hasComparisonWithImmediateInstruction();
    }
    
    public static boolean isArithmetic(Instruction instruction)
    {
        return arch.isArithmetic(instruction);
    }
    
    public static boolean isBranch(Instruction instruction)
    {
        return arch.isBranch(instruction);
    }
    
    public static boolean isComparison(Instruction instruction)
    {
        return arch.isComparison(instruction);
    }
    
    public static boolean isComparisonInstruction(Instruction instruction)
    {
        return arch.isComparisonInstruction(instruction);
    }
    
    public static boolean isDataPattern(String line)
    {
        return Util.isIt(line, arch.getDataPattern(), BASE_PATTERNS);
    }
    
    public static boolean isFunctionCall(String instruction)
    {
        List<String> functionCalls = getFunctionCalls();
        if (functionCalls.indexOf(instruction) != -1) return true;
        
        return false;
    }
    
    public static boolean isGlobalRegister(Register register)
    {
        return arch.isGlobalRegister(register);
    }
    
    public static boolean isGlobalRegister(String name)
    {
        List<Register> registers = arch.getRegisters();
        for (int i = 0; i < registers.size(); i++)
        {
            if (registers.get(i).getName().equalsIgnoreCase(name) && isGlobalRegister(registers.get(i))) return true;
        }
        
        return false;
    }
    
    public static boolean isInputRegister(Register register)
    {
        return arch.isInputRegister(register);
    }
    
    public static boolean isInputRegister(String name)
    {
        List<Register> registers = arch.getRegisters();
        for (int i = 0; i < registers.size(); i++)
        {
            if (registers.get(i).getName().equalsIgnoreCase(name) && isInputRegister(registers.get(i))) return true;
        }
        
        return false;
    }
    
    public static boolean isIORegister(Register register)
    {
        return (arch.isInputRegister(register) || arch.isOutputRegister(register));
    }
    
    public static boolean isIORegister(String name)
    {
        List<Register> registers = arch.getRegisters();
        for (int i = 0; i < registers.size(); i++)
        {
            if (registers.get(i).getName().equalsIgnoreCase(name) && isIORegister(registers.get(i))) return true;
        }
        
        return false;
    }

    public static boolean isInstruction(String line)
    {
        for (int i = 0; i < arch.getInstructionFormat().size(); i++)
        {
            String instructionFormat = arch.getInstructionFormat().get(i);        
            List<String> list = Util.getIt(line, instructionFormat, "ins", BASE);
            String instruction = (list.size()>0?list.get(0):"");

            if (Util.isIt(line, instructionFormat, BASE) && isInstructionName(instruction))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean isInstructionName(String instruction)
    {
        return arch.isInstructionName(instruction);
    }
    
    public static boolean isJump(Instruction instruction)
    {
        return arch.isJump(instruction);
    }
    
    public static boolean isJumpToTarget(Instruction instruction)
    {
        return arch.isJumpToTarget(instruction);
    }
    
    public static boolean isJumpToRegister(Instruction instruction)
    {
        return arch.isJumpToRegister(instruction);
    }
    
    public static boolean isLabel(String line)
    {
        String labelFormat = arch.getLabelFormat();
        return Util.isIt(line, labelFormat, BASE);
    }
    
    public static boolean isLoad(Instruction instruction)
    {
        return arch.isLoad(instruction);
    }
    
    public static boolean isLoadFromStack(Instruction instruction)
    {
        return arch.isLoadFromStack(instruction);
    }
    
    public static boolean isLocalRegister(Register register)
    {
        return arch.isLocalRegister(register);
    }
    
    public static boolean isLocalRegister(String name)
    {
        List<Register> registers = arch.getRegisters();
        for (int i = 0; i < registers.size(); i++)
        {
            if (registers.get(i).getName().equalsIgnoreCase(name) && isLocalRegister(registers.get(i))) return true;
        }
        
        return false;
    }

    public static boolean isOther(Instruction instruction)
    {
        return arch.isOther(instruction);
    }
    
    public static boolean isOutputRegister(Register register)
    {
        return arch.isOutputRegister(register);
    }
    
    public static boolean isOutputRegister(String name)
    {
        List<Register> registers = arch.getRegisters();
        for (int i = 0; i < registers.size(); i++)
        {
            if (registers.get(i).getName().equalsIgnoreCase(name) && isOutputRegister(registers.get(i))) return true;
        }
        
        return false;
    }
    
    public static boolean isPreInitializedRegister(String register)
    {
        return arch.isPreInitializedRegisterName(register);
    }
    
    public static boolean isRegister(String register)
    {
        return arch.isRegisterName(register);
    }
    
    public static boolean isShiftWindowRegistersInstruction(Instruction instruction)
    {
        return arch.isShiftWindowRegisterInstruction(instruction);
    }
    
    public static boolean isStore(Instruction instruction)
    {
        return arch.isStore(instruction);
    }
    
    public static boolean isStoreToStack(Instruction instruction)
    {
        return arch.isStoreToStack(instruction);
    }
    
    /*
     * Setters
     */
    
    public static void setAddImmediateInstructionName(String name)
    {
        arch.setAddImmediateInstructionName(name);
    }
    
    public static void setAddRegistersInstructionName(String name)
    {
        arch.setAddRegistersInstructionName(name);
    }
    
    public static void setAndImmediateInstructionName(String name)
    {
        arch.setAndImmediateInstructionName(name);
    }
    
    public static void setArchitecture(Architecture arch)
    {
        DB.arch = arch;
    }
    
     public static void setAssemblyFilename(String assemblyFilename)
    {
        DB.assemblyFilename = assemblyFilename;
    }
    
    public static void setBranchDelaySlot(int branchDelaySlot)
    {
        arch.setBranchDelaySlot(branchDelaySlot);
    }
    
    public static void setBranchNotEqualZeroInstructionName(String name)
    {
        arch.setBranchNotEqualZeroInstructionName(name);
    }
    
    public static void setCommentTag(String tag)
    {
        arch.setCommentTag(Util.formatToRegex(tag));
    }
    
    public static void setComparisonInstructionName(String name)
    {
        arch.setComparisonInstructionName(name);
    }
    
    public static void setComparisonWithImmediateInstructionName(String name)
    {
        arch.setComparisonWithImmediateInstructionName(name);
    }
    
    public static void setDataPattern(String dataPattern)
    {
        arch.setDataPattern(dataPattern);
    }
    
    public static void setDump(Dump dump)
    {
        DB.dump = dump;
    }
    
    public static void setDumpFilename(String dumpFilename)
    {
        DB.dumpFilename = dumpFilename;
    }
    
    public static void setEqualityComparisonInstructionName(String name)
    {
        arch.setEqualityComparisonInstructionName(name);
    }
    
    public static void setHigherHalfImmediateAssignmentInstructionName(String name)
    {
        arch.setHigherHalfImmediateAssignmentInstructionName(name);
    }
    
    public static void setImmediateAssignmentInstructionName(String name)
    {
        arch.setImmediateAssignmentInstructionName(name);
    }
    
    public static void setInverterInstructions(List<String> instructions)
    {
        List<String> inverterInstructions = new ArrayList<String>();
        List<String> invertedInstructions = new ArrayList<String>();
        
        for (int i = 0; i < instructions.size(); i++)
        {
            String[] vector = Util.splitTrim(instructions.get(i), ":");
            inverterInstructions.add(vector[0]);
            invertedInstructions.add(vector[1]);
            inverterInstructions.add(vector[1]);
            invertedInstructions.add(vector[0]);
        }
        
        arch.setInverterInstructions(inverterInstructions);
        arch.setInvertedInstructions(invertedInstructions);
    }
    
    public static void setJumpInstructionName(String name)
    {
        arch.setJumpInstructionName(name);
    }
    
    public static void setLabelFormat(String format)
    {
        arch.setLabelFormat(format);
    }
    
    public static void setLoadInstructionName(String name)
    {
        arch.setLoadInstructionName(name);
    }
    
    public static void setLowerHalfImmediateAssignmentInstructionName(String name)
    {
        arch.setLowerHalfImmediateAssignmentInstructionName(name);
    }
    
    public static void setEntryPoint(String name)
    {
        arch.setMainLabelName(name);
    }
    
    public static void setMoveInstructionName(String name)
    {
        arch.setMoveInstructionName(name);
    }
    
    public static void setNonEqualityComparisonInstructionName(String name)
    {
        arch.setNonEqualityComparisonInstructionName(name);
    }
    
    public static void setNoOperationInstructionName(String name)
    {
        arch.setNoOperationInstructionName(name);
    }
    
    public static void setOperandsSeparator(String separator)
    {
        arch.setOperandsSeparator(separator);
    }
    
    public static void setPreInitializedRegisters(List<String> registers)
    {
        arch.setPreInitializedRegisters(registers);
    }
    
    public static void setRegisterZeroName(String name)
    {
        arch.setRegisterZeroName(name);
    }
    
    public static void setSelectedRegisters(List<String> selectedRegisters) throws IOException
    {
        List<String> code = Util.readFile(assemblyFilename);
        List<String> usedRegisters = getUsedRegisters(code);
        
        for (int i = usedRegisters.size()-1; i >= 0; i--)
        {
            String register = usedRegisters.get(i);
            if (!selectedRegisters.contains(register)) arch.removeRegisterByName(register);            
        }
    }
    
    public static void setShiftLeftImmediateInstructionName(String name)
    {
        arch.setShiftLeftImmediateInstructionName(name);
    }
    
    public static void setShiftRightArithmeticImmediateInstructionName(String name)
    {
        arch.setShiftRightArithmeticImmediateInstructionName(name);
    }
    
    public static void setShiftRightImmediateInstructionName(String name)
    {
        arch.setShiftRightImmediateInstructionName(name);
    }
    
    public static void setShiftWindowRegistersInstructionsNames(List<String> names)
    {
        arch.setShiftWindowRegistersInstructionsNames(names);
    }
    
    public static void setStoreInstructionName(String name)
    {
        arch.setStoreInstructionName(name);
    }
    
    public static void setSubImmediateInstructionName(String name)
    {
        arch.setSubImmediateInstructionName(name);
    }
    
    public static void setTargetProcessor(String targetProcessor)
    {
        arch.setTargetProcessor(targetProcessor);
    }
    
    public static void setWordSize(int wordSize)
    {
        arch.setWordSize(wordSize);
    }
    
    public static void setXorImmediateInstructionName(String name)
    {
        arch.setXorImmediateInstructionName(name);
    }
    
    /*
     * Informações sobre as técnicas a serem aplicadas e suas configurações
     */
    
    public static String getErrorRegister()
    {
        return tech.getErrorRegister();
    }
    
    public static String getErrorValue()
    {
        return tech.getErrorValue();
    }
    
    public static int getHetaOffset()
    {
        return tech.getHetaOffset();
    }
    
    public static int getOffset()
    {
        return tech.getOffset();
    }
    
    public static String getPriorityMode()
    {
        return tech.getPriorityMode();
    }
    
    public static List<String> getRegistersByPriority()
    {
        return tech.getRegistersByPriority();
    }
    
    public static int getResoShift()
    {
        return tech.getResoShift();
    }
    
    public static boolean getSetaHigherPriority()
    {
        return tech.getSetaHigherPriority();
    }
    
    public static boolean getSetaInsertNops()
    {
        return tech.getSetaInsertNops();
    }
    
    public static boolean getSetaInsertNopsAfterBranching()
    {
        return tech.getSetaInsertNopsAfterBranching();
    }
    
    public static int getSetaMinNumberOfInstructions()
    {
        return tech.getSetaMinNumberOfInstructions();
    }
        
    public static Double getSetaPercentageToVerify()
    {
        return tech.getSetaPercentageToVerify();
    }
    
    public static String getSetaPriorityMethod()
    {
        return tech.getSetaPriorityMethod();
    }
    
    public static Double getSetaTunnelEffectPercentage()
    {
        return tech.getSetaTunnelEffectPercentage();
    }
    
    public static boolean getSetaTunnelEffectSelectionByPercentage()
    {
        return tech.getSetaTunnelEffectSelectionByPercentage();
    }
    
    public static String getSignatureRegister()
    {
        return tech.getSignatureRegister();
    }
    
    public static List<String> getTechniques()
    {
        return tech.getTechniques();
    }
    
    public static void setErrorRegister(String errorRegister)
    {
        tech.setErrorRegister(errorRegister);
    }
    
    public static void setErrorValue(String errorValue)
    {
        tech.setErrorValue(errorValue);
    }
    
    public static void setHetaOffset(int hetaOffset)
    {
        tech.setHetaOffset(hetaOffset);
    }
    
    public static void setOffset(int offset)
    {
        tech.setOffset(offset);
    }
    
    public static void setPriorityMode(String priorityMode)
    {
        tech.setPriorityMode(priorityMode);
    }
    
    public static void setRegistersByPriority(List<String> registers)
    {
        for (int i = registers.size() - 1; i >= 0; i--)
        {
            if (!isRegister(registers.get(i))) registers.remove(i);
        }
        
        tech.setRegistersByPriority(registers);
    }
    
    public static void setResoShift(int resoShift)
    {
        tech.setResoShift(resoShift);
    }
    
    public static void setSetaHigherPriority(boolean priority)
    {
        tech.setSetaHigherPriority(priority);
    }
    
    public static void setSetaInsertNops(boolean insertNops)
    {
        tech.setSetaInsertNops(insertNops);
    }
    
    public static void setSetaInsertNopsAfterBranching(boolean insertNops)
    {
        tech.setSetaInsertNopsAfterBranching(insertNops);
    }
    
    public static void setSetaMinNumberOfInstructions(int setaMinNumberOfInstructions)
    {
        tech.setSetaMinNumberOfInstructions(setaMinNumberOfInstructions);
    }
    
    public static void setSetaPercentageToVerify(Double setaPercentageToVerify)
    {
        tech.setSetaPercentageToVerify(setaPercentageToVerify);
    }
    
    public static void setSetaPriorityMethod(String setaPriorityMethod)
    {
        tech.setSetaPriorityMethod(setaPriorityMethod);
    }
    
    public static void setSetaTunnelEffectPercentage(double percentage)
    {
        tech.setSetaTunnelEffectPercentage(percentage);
    }
    
    public static void setSetaTunnelEffectSelectionByPercentage(boolean percentage)
    {
        tech.setSetaTunnelEffectSelectionByPercentage(percentage);
    }
    
    public static void setSignatureRegister(String signatureRegister)
    {
        tech.setSignatureRegister(signatureRegister);
    }
    
    public static void setTechniques(List<String> techniques)
    {
        tech.setTechniques(techniques);
    }
}
