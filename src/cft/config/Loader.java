/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.config;

import cft.util.Instruction;
import cft.util.Register;
import cft.util.Util;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class Loader
{
    public static final String CONFIG_DIR = "./config/";
    public static String mainConfigFilename = "config.ini";
    public static String insConfigFilename = "instructions.ini";
    public static String regConfigFilename = "registers.ini";
    public static String dumpConfigFilename = "dump.ini";
    public static String techniquesConfigFilename = "techniques.ini";
    public static String configCommentTag = "//";
    public static String[] parameters;
    
    public static String getParameter(String targetParameter)
    {
        for (String parameter : parameters)
        {
            if (parameter.contains(targetParameter))
            {
                return parameter.split("=")[1].trim();
            }
        }
        
        return "";
    }
    
    public static void loadConfig(String[] parameters) throws IOException
    {
        setParameters(parameters);
        setTargetProcessor(getParameter("targetProcessor"));
        loadConfig();
    }
    
    public static void loadConfig() throws IOException
    {
        updatePaths();
        loadMainConfig();
        loadRegisters();
        loadInstructions();
        loadDumpConfig();
        //loadExtra();
        loadTechniquesConfig();
        loadParameters();
    }
    
    /*
     * Carregar configurações gerais
     */
    
    public static void loadMainConfig() throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(mainConfigFilename));
        String mark = new String();
        
        for (String line; (line = in.readLine()) != null; )
        {
            //Formatar linha
            line = Util.getBefore(line, configCommentTag);
            line = Util.removeWhitespacesFromEnd(line);
            
            //Se for uma linha válida (ignora linhas em branco)
            if (line.length() > 0)
            {
                if (line.equalsIgnoreCase("[LABEL_FORMAT]"))
                {
                    mark = "labelFormat";
                }
                else if (line.equalsIgnoreCase("[ENTRY_POINT]"))
                {
                    mark = "entryPoint";
                }
                else if (line.equalsIgnoreCase("[COMMENT_TAG]"))
                {
                    mark = "commentTag";
                }
                else if (line.equalsIgnoreCase("[OPERANDS_SEPARATOR]"))
                {
                    mark = "operandsSeparator";
                }
                else if (line.equalsIgnoreCase("[EQUALITY_COMPARISON_INSTRUCTION]"))
                {
                    mark = "equalityComparisonInstruction";
                }
                else if (line.equalsIgnoreCase("[NON_EQUALITY_COMPARISON_INSTRUCTION]"))
                {
                    mark = "nonEqualityComparisonInstruction";
                }
                else if (line.equalsIgnoreCase("[JUMP_INSTRUCTION]"))
                {
                    mark = "jumpInstruction";
                }
                else if (line.equalsIgnoreCase("[LOAD_INSTRUCTION]"))
                {
                    mark = "loadInstruction";
                }
                else if (line.equalsIgnoreCase("[MOVE_INSTRUCTION]"))
                {
                    mark = "moveInstruction";
                }
                else if (line.equalsIgnoreCase("[STORE_INSTRUCTION]"))
                {
                    mark = "storeInstruction";
                }
                else if (line.equalsIgnoreCase("[PRE-INITIALIZED_REGISTERS]"))
                {
                    mark = "preInitializedRegisters";
                }
                else if (line.equalsIgnoreCase("[HIGHER_HALF_IMMEDIATE_ASSIGNMENT_INSTRUCTION]"))
                {
                    mark = "higherHalfImmediateAssignmentInstruction";
                }
                else if (line.equalsIgnoreCase("[IMMEDIATE_ASSIGNMENT_INSTRUCTION]"))
                {
                    mark = "immediateAssignmentInstruction";
                }
                else if (line.equalsIgnoreCase("[LOWER_HALF_IMMEDIATE_ASSIGNMENT_INSTRUCTION]"))
                {
                    mark = "lowerHalfImmediateAssignmentInstruction";
                }
                else if (line.equalsIgnoreCase("[INVERTER_INSTRUCTIONS]"))
                {
                    mark = "inverterInstructions";
                }
                else if (line.equalsIgnoreCase("[BRANCH_NOT_EQUAL_ZERO]"))
                {
                    mark = "branchNotEqualZero";
                }
                else if (line.equalsIgnoreCase("[ADD_IMMEDIATE_INSTRUCTION]"))
                {
                    mark = "addImmediateInstruction";
                }
                else if (line.equalsIgnoreCase("[ADD_REGISTERS_INSTRUCTION]"))
                {
                    mark = "addRegistersInstruction";
                }
                else if (line.equalsIgnoreCase("[AND_IMMEDIATE_INSTRUCTION]"))
                {
                    mark = "andImmediateInstruction";
                }
                else if (line.equalsIgnoreCase("[SUB_IMMEDIATE_INSTRUCTION]"))
                {
                    mark = "subImmediateInstruction";
                }
                else if (line.equalsIgnoreCase("[SHIFT_LEFT_IMMEDIATE_INSTRUCTION]"))
                {
                    mark = "shiftLeftImmediateInstruction";
                }
                else if (line.equalsIgnoreCase("[SHIFT_RIGHT_IMMEDIATE_INSTRUCTION]"))
                {
                    mark = "shiftRightImmediateInstruction";
                }
                else if (line.equalsIgnoreCase("[SHIFT_RIGHT_ARITHMETIC_IMMEDIATE_INSTRUCTION]"))
                {
                    mark = "shiftRightArithmeticImmediateInstruction";
                }
                else if (line.equalsIgnoreCase("[XOR_IMMEDIATE_INSTRUCTION]"))
                {
                    mark = "xorImmediateInstruction";
                }
                else if (line.equalsIgnoreCase("[COMPARISON_INSTRUCTION]"))
                {
                    mark = "comparisonInstruction";
                }
                else if (line.equalsIgnoreCase("[COMPARISON_WITH_IMMEDIATE_INSTRUCTION]"))
                {
                    mark = "comparisonWithImmediateInstruction";
                }
                else if (line.equalsIgnoreCase("[NO_OPERATION_INSTRUCTION]"))
                {
                    mark = "nopInstruction";
                }
                else if (line.equalsIgnoreCase("[BRANCH_DELAY_SLOT]"))
                {
                    mark = "branchDelaySlot";
                }
                else if (line.equalsIgnoreCase("[REGISTER_ZERO]"))
                {
                    mark = "registerZero";
                }
                else if (line.equalsIgnoreCase("[DATA_PATTERN]"))
                {
                    mark = "dataPattern";
                }
                else if (line.equalsIgnoreCase("[ASSEMBLY_FILENAME]"))
                {
                    mark = "assemblyFilename";
                }
                else if (line.equalsIgnoreCase("[DUMP_FILENAME]"))
                {
                    mark = "dumpFileName";
                }
                else if (line.equalsIgnoreCase("[SHIFT_WINDOW_REGISTERS_INSTRUCTIONS]"))
                {
                    mark = "shiftWindowRegistersInstructions";
                }
                else if (line.equalsIgnoreCase("[WORD_SIZE]"))
                {
                    mark = "wordSize";
                }
                else
                {
                    if (mark.equals("labelFormat"))
                    {
                        DB.setLabelFormat(line);
                    }
                    else if (mark.equals("entryPoint"))
                    {
                        DB.setEntryPoint(line.trim());
                    }
                    else if (mark.equals("commentTag"))
                    {
                        DB.setCommentTag(line);
                    }
                    else if (mark.equals("operandsSeparator"))
                    {
                        DB.setOperandsSeparator(line);
                    }
                    else if (mark.equals("equalityComparisonInstruction"))
                    {
                        DB.setEqualityComparisonInstructionName(line.trim());
                    }
                    else if (mark.equals("nonEqualityComparisonInstruction"))
                    {
                        DB.setNonEqualityComparisonInstructionName(line.trim());
                    }
                    else if (mark.equals("jumpInstruction"))
                    {
                        DB.setJumpInstructionName(line.trim());
                    }
                    else if (mark.equals("loadInstruction"))
                    {
                        DB.setLoadInstructionName(line.trim());
                    }
                    else if (mark.equals("moveInstruction"))
                    {
                        DB.setMoveInstructionName(line.trim());
                    }
                    else if (mark.equals("storeInstruction"))
                    {
                        DB.setStoreInstructionName(line.trim());
                    }
                    else if (mark.equals("registerZero"))
                    {
                        DB.setRegisterZeroName(line.trim());
                    }
                    else if (mark.equals("preInitializedRegisters"))
                    {
                        DB.setPreInitializedRegisters(Arrays.asList(Util.splitTrim(line, ",")));
                    }
                    else if (mark.equals("higherHalfImmediateAssignmentInstruction"))
                    {
                        DB.setHigherHalfImmediateAssignmentInstructionName(line.trim());
                    }
                    else if (mark.equals("immediateAssignmentInstruction"))
                    {
                        DB.setImmediateAssignmentInstructionName(line.trim());
                    }
                    else if (mark.equals("lowerHalfImmediateAssignmentInstruction"))
                    {
                        DB.setLowerHalfImmediateAssignmentInstructionName(line.trim());
                    }
                    else if (mark.equals("inverterInstructions"))
                    {
                        DB.setInverterInstructions(Arrays.asList(Util.splitTrim(line, ",")));
                    }
                    else if (mark.equals("branchNotEqualZero"))
                    {
                        DB.setBranchNotEqualZeroInstructionName(line.trim());
                    }
                    else if (mark.equals("addImmediateInstruction"))
                    {
                        DB.setAddImmediateInstructionName(line.trim());
                    }
                    else if (mark.equals("addRegistersInstruction"))
                    {
                        DB.setAddRegistersInstructionName(line.trim());
                    }
                    else if (mark.equals("andImmediateInstruction"))
                    {
                        DB.setAndImmediateInstructionName(line.trim());
                    }
                    else if (mark.equals("subImmediateInstruction"))
                    {
                        DB.setSubImmediateInstructionName(line.trim());
                    }
                    else if (mark.equals("shiftLeftImmediateInstruction"))
                    {
                        DB.setShiftLeftImmediateInstructionName(line.trim());
                    }
                    else if (mark.equals("shiftRightImmediateInstruction"))
                    {
                        DB.setShiftRightImmediateInstructionName(line.trim());
                    }
                    else if (mark.equals("shiftRightArithmeticImmediateInstruction"))
                    {
                        DB.setShiftRightArithmeticImmediateInstructionName(line.trim());
                    }
                    else if (mark.equals("xorImmediateInstruction"))
                    {
                        DB.setXorImmediateInstructionName(line.trim());
                    }
                    else if (mark.equals("comparisonInstruction"))
                    {
                        DB.setComparisonInstructionName(line.trim());
                    }
                    else if (mark.equals("comparisonWithImmediateInstruction"))
                    {
                        DB.setComparisonWithImmediateInstructionName(line.trim());
                    }
                    else if (mark.equals("nopInstruction"))
                    {
                        DB.setNoOperationInstructionName(line.trim());
                    }
                    else if (mark.equals("dataPattern"))
                    {
                        line = Util.formatToRegex(line.trim());
                        DB.setDataPattern(line);
                    }                    
                    else if (mark.equals("assemblyFilename"))
                    {
                        DB.setAssemblyFilename(line.trim());
                    }
                    else if (mark.equals("dumpFileName"))
                    {
                        DB.setDumpFilename(line.trim());
                    }
                    else if (mark.equals("shiftWindowRegistersInstructions"))
                    {
                        DB.setShiftWindowRegistersInstructionsNames(Arrays.asList(Util.splitTrim(line.trim(), ",")));
                    }
                    else if (mark.equals("wordSize"))
                    {
                        DB.setWordSize(Integer.parseInt(line.trim()));
                    }
                    else if (mark.equals("branchDelaySlot"))
                    {
                        int branchDelaySlot;
                        try
                        {
                            branchDelaySlot = Integer.parseInt(line.trim());
                        }
                        catch (Exception error)
                        {
                            branchDelaySlot = 0;
                        }
                        DB.setBranchDelaySlot(branchDelaySlot);
                    }
                    mark = "";
                }
            }
        }
        
        in.close();
    }
    
    /*
     * Carregar configurações referentes às instruções
     */
    
    public static void loadInstructions() throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(insConfigFilename));
        String mark = new String();
        String instructions[] = new String[0];
        String format = new String();
        String type = new String();
        String hiddenRD[] = new String[1];
        String hiddenRS[] = new String[1];
        
        for (String line; (line = in.readLine()) != null; )
        {
            //Formatar linha
            line = Util.getBefore(line, configCommentTag);
            line = Util.removeWhitespacesFromEnd(line);
            
            //Se for uma linha válida (ignora linhas em branco)
            if (line.length() > 0)
            {
                if (line.equalsIgnoreCase("[GROUP]"))
                {
                    mark = "group";
                    
                    if ((instructions.length > 0) && (format.length() > 0) && (type.length() > 0))
                    {
                        addInstructions(instructions, format, type, hiddenRD, hiddenRS);
                    }
                    
                    instructions = new String[0];
                    format = "";
                    type = "";
                    hiddenRD = new String[0];
                    hiddenRS = new String[0];
                }
                else if (line.equalsIgnoreCase("{INSTRUCTIONS}"))
                {
                    mark = "instructions";
                }
                else if (line.equalsIgnoreCase("{FORMAT}"))
                {
                    mark = "format";
                }
                else if (line.equalsIgnoreCase("{TYPE}"))
                {
                    mark = "type";
                }
                else if (line.equalsIgnoreCase("{HIDDEN_REGISTERS}"))
                {
                    mark = "hidden";
                }
                else
                {
                    if (mark.equals("instructions"))
                    {
                        instructions = Util.splitTrim(line, ",");
                    }
                    else if (mark.equals("format"))
                    {
                        format = line;
                    }
                    else if (mark.equals("type"))
                    {
                        type = line.trim().toLowerCase();
                    }
                    else if (mark.equals("hidden"))
                    {
                        String str[] = line.split(":");
                        String regType = str[0].trim();
                        
                        if (regType.equalsIgnoreCase("rd"))
                        {
                            hiddenRD = Util.splitTrim(str[1], ",");
                        }
                        else if(regType.equalsIgnoreCase("rs"))
                        {
                            hiddenRS = Util.splitTrim(str[1], ",");
                        }
                    }
                }
            }   
        }
        
        //Adicionar último grupo de instruções à arquitetura
        if (mark.length() > 0)
        {
            addInstructions(instructions, format, type, hiddenRD, hiddenRS);
        }
        
        in.close();
    }
    
    public static void addInstructions(String[] instructions, String format, String type, String[] hiddenRD, String[] hiddenRS)
    {        
        for (int i = 0; i < instructions.length; i++)
        {
            addInstruction(instructions[i], format, type, hiddenRD, hiddenRS);
        }
    }
    
    public static void addInstruction(String instruction, String format, String type, String[] hiddenRD, String[] hiddenRS)
    {
        DB.addInstruction(new Instruction(instruction, format, type, hiddenRD, hiddenRS));
    }
    
    /*
     * Carregar configurações referentes aos registradores
     */
    
    public static void loadRegisters() throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(regConfigFilename));
        String mark = new String();
        String registers[] = new String[0];
        String type = new String();
        boolean readable = false;
        boolean writable = false;
        
        for (String line; (line = in.readLine()) != null; )
        {
            //Formatar linha
            line = Util.getBefore(line, configCommentTag);
            line = Util.removeWhitespacesFromEnd(line);
            
            //Se for uma linha válida (ignora linhas em branco)
            if (line.length() > 0)
            {
                if (line.equalsIgnoreCase("[GROUP]"))
                {
                    mark = "group";
                    if (registers.length > 0)
                    {
                        addRegisters(registers, readable, writable, type);
                    }
                    
                    registers = new String[0];
                    readable = false;
                    writable = false;
                    type = new String();
                }
                else if (line.equalsIgnoreCase("{REGISTERS}"))
                {
                    mark = "registers";
                }
                else if (line.equalsIgnoreCase("{READABLE}"))
                {
                    mark = "readable";
                }
                else if (line.equalsIgnoreCase("{WRITABLE}"))
                {
                    mark = "writable";
                }
                else if (line.equalsIgnoreCase("{TYPE}"))
                {
                    mark = "type";
                }
                else
                {
                    if (mark.equals("registers"))
                    {
                        registers = Util.splitTrim(line, ",");
                    }
                    else if (mark.equals("readable"))
                    {
                        readable = line.trim().equalsIgnoreCase("YES");
                    }
                    else if (mark.equals("writable"))
                    {
                        writable = line.trim().equalsIgnoreCase("YES");
                    }
                    else if (mark.equals("type"))
                    {
                        type = line.trim().toLowerCase();
                    }
                }
            }
        }
        
        //Adicionar último grupo de registradores à arquitetura
        if (mark.length() > 0)
        {
            addRegisters(registers, readable, writable, type);
        }
        
        in.close();
    }
    
    public static void addRegisters(String[] registers, boolean readable, boolean writable, String type)
    {
        for (int i = 0; i < registers.length; i++)
        {
            addRegister(registers[i], readable, writable, type);
        }
    }
    
    public static void addRegister(String register, boolean readable, boolean writable, String type)
    {
        DB.addRegister(new Register(register, readable, writable, type));
    }
    
    /*
     * Load configs of dump file
     */
    public static void loadDumpConfig() throws IOException
    {
        DB.setDump(new Dump(dumpConfigFilename));
    }
    
    /*
     * Carregar configurações das técnicas de detecção a serem aplicadas
     */
    
    public static void loadTechniquesConfig() throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(techniquesConfigFilename));
        String mark = new String();
        
        for (String line; (line = in.readLine()) != null; )
        {
            //Formatar linha
            line = Util.getBefore(line, configCommentTag);
            line = Util.removeWhitespacesFromEnd(line);
            
            //Se for uma linha válida (ignora linhas em branco)
            if (line.length() > 0)
            {
                if (line.equalsIgnoreCase("[TECHNIQUES]"))
                {
                    mark = "techniques";
                }
                else if (line.equalsIgnoreCase("[OFFSET]"))
                {
                    mark = "offset";
                }
                else if (line.equalsIgnoreCase("[HETA_OFFSET]"))
                {
                    mark = "hetaOffset";
                }
                else if (line.equalsIgnoreCase("[ERROR_REGISTER]"))
                {
                    mark = "errorRegister";
                }
                else if (line.equalsIgnoreCase("[ERROR_VALUE]"))
                {
                    mark = "errorValue";
                }
                else if (line.equalsIgnoreCase("[PRIORITY_MODE]"))
                {
                    mark = "priorityMode";
                }
                else if (line.equalsIgnoreCase("[SELECTED_REGISTERS]"))
                {
                    mark = "selectedRegisters";
                }
                else if (line.equalsIgnoreCase("[SETA_TUNNEL_EFFECT_SELECTION_BY_PERCENTAGE]"))
                {
                    mark = "setaTunnelEffectSelectionByPercentage";
                }
                else if (line.equalsIgnoreCase("[SETA_TUNNEL_EFFECT_PERCENTAGE]"))
                {
                    mark = "setaTunnelEffectPercentage";
                }
                else if (line.equalsIgnoreCase("[SETA_TUNNEL_EFFECT_MIN_NUMBER_OF_INSTRUCTIONS]"))
                {
                    mark = "setaTunnelEffectMinNumberOfInstrucions";
                }
                else if (line.equalsIgnoreCase("[SETA_PRIORITY_METHOD]"))
                {
                    mark = "setaPriorityMethod";
                }
                else if (line.equalsIgnoreCase("[SETA_HIGHER_PRIORITY]"))
                {
                    mark = "setaHigherPriority";
                }
                else if (line.equalsIgnoreCase("[SETA_INSERT_NOPS]"))
                {
                    mark = "setaInsertNops";
                }
                else if (line.equalsIgnoreCase("[SETA_INSERT_NOPS_AFTER_BRANCHING]"))
                {
                    mark = "setaInsertNopsAfterBranching";
                }
                else if (line.equalsIgnoreCase("[SETA_CHECKERS_PERCENTAGE_TO_VERIFY]"))
                {
                    mark = "setaCheckersPercentageToVerify";
                }
                else if (line.equalsIgnoreCase("[REGISTERS_BY_PRIORITY]"))
                {
                    mark = "registersByPriority";
                }   
                else if (line.equalsIgnoreCase("[RESO_SHIFT]"))
                {
                    mark = "resoShift";
                }
                else
                {
                    if (mark.equals("techniques"))
                    {
                        List<String> techniques = Arrays.asList(Util.splitTrim(line.toUpperCase(), ","));
                        DB.setTechniques(techniques);
                    }
                    else if (mark.equals("offset"))
                    {
                        DB.setOffset(Integer.parseInt(line.trim()));
                    }
                    else if (mark.equals("hetaOffset"))
                    {
                        DB.setHetaOffset(Integer.parseInt(line.trim()));
                    }
                    else if (mark.equals("errorRegister"))
                    {
                        DB.setErrorRegister(line.trim());
                    }
                    else if (mark.equals("errorValue"))
                    {
                        DB.setErrorValue(line.trim());
                    }
                    else if (mark.equals("priorityMode"))
                    {
                        DB.setPriorityMode(line.trim().toLowerCase());
                    }
                    else if (mark.equals("selectedRegisters"))
                    {
                        DB.setSelectedRegisters(Arrays.asList(Util.splitTrim(line.trim().toUpperCase(), ",")));
                    }
                    else if (mark.equals("setaTunnelEffectSelectionByPercentage"))
                    {
                        line = line.toUpperCase();
                        if (line.equals("TRUE") || line.equals("T") || line.equals("YES") || line.equals("Y")
                        || line.equals("SIM") ||  line.equals("SI") || line.equals("S"))
                        {
                            DB.setSetaTunnelEffectSelectionByPercentage(true);
                        }
                        else
                        {
                            DB.setSetaTunnelEffectSelectionByPercentage(false);
                        }
                    }
                    else if (mark.equals("setaTunnelEffectPercentage"))
                    {
                        DB.setSetaTunnelEffectPercentage(Double.parseDouble(line.trim().toLowerCase()));
                    }
                    else if (mark.equals("setaTunnelEffectMinNumberOfInstrucions"))
                    {
                        DB.setSetaMinNumberOfInstructions(Integer.parseInt(line.trim().toLowerCase()));
                    }
                    else if (mark.equals("setaPriorityMethod"))
                    {
                        DB.setSetaPriorityMethod(line.trim().toLowerCase());
                    }
                    else if (mark.equals("setaHigherPriority"))
                    {
                        line = line.toUpperCase();
                        if (line.equals("TRUE") || line.equals("T") || line.equals("YES") || line.equals("Y")
                        || line.equals("SIM") ||  line.equals("SI") || line.equals("S"))
                        {
                            DB.setSetaHigherPriority(true);
                        }
                        else
                        {
                            DB.setSetaHigherPriority(false);
                        }
                    }
                    else if (mark.equals("setaInsertNops"))
                    {
                        line = line.toUpperCase();
                        if (line.equals("TRUE") || line.equals("T") || line.equals("YES") || line.equals("Y")
                        || line.equals("SIM") ||  line.equals("SI") || line.equals("S"))
                        {
                            DB.setSetaInsertNops(true);
                        }
                        else
                        {
                            DB.setSetaInsertNops(false);
                        }
                    }
                    else if (mark.equals("setaInsertNopsAfterBranching"))
                    {
                        line = line.toUpperCase();
                        DB.setSetaInsertNopsAfterBranching((line.equals("TRUE") || line.equals("T") || line.equals("YES") || line.equals("Y")
                        || line.equals("SIM") ||  line.equals("SI") || line.equals("S")));
                    }
                    else if (mark.equals("setaCheckersPercentageToVerify"))
                    {
                        DB.setSetaPercentageToVerify(Double.parseDouble(line.trim()));
                    }
                    else if (mark.equals("registersByPriority"))
                    {
                        DB.setRegistersByPriority(Arrays.asList(Util.splitTrim(line.trim(), ",")));
                    }
                    else if (mark.equals("resoShift"))
                    {
                        DB.setResoShift(Integer.parseInt(line.trim()));
                    }
                    mark = "";
                }
            }
        }
        
        in.close();
    }
    
    public static void loadParameters() throws IOException
    {
        for (String parameter : parameters)
        {
            String arg[] = parameter.trim().split("=");
            parameter = arg[0].trim().substring(1);
            
            String argument;
            
            try
            {
                argument = arg[1].trim();
            }
            catch (Exception e)
            {
                continue;
            }
            
            switch (parameter)
            {
                case "assemblyFilename": DB.setAssemblyFilename(argument); break;
                case "dumpFilename": DB.setDumpFilename(dumpConfigFilename); break;
                case "techniques": DB.setTechniques(Arrays.asList(Util.splitTrim(argument.toUpperCase(), ","))); break;
                case "targetProcessor": DB.setTargetProcessor(argument); break;
                case "errorRegister": DB.setErrorRegister(argument); break;
                case "errorValue": DB.setErrorValue(argument); break;
                case "selectedRegisters": DB.setSelectedRegisters(Arrays.asList(Util.splitTrim(argument.toUpperCase(), ","))); break;
                case "priorityMode": DB.setPriorityMode(argument); break;
                case "registerByPriority": DB.setRegistersByPriority(Arrays.asList(Util.splitTrim(argument.toUpperCase(), ","))); break;
                case "offset": DB.setOffset(Integer.parseInt(argument)); break;
                case "setaPriorityMethod": DB.setSetaPriorityMethod(argument); break;
                case "setaHigherPriority": DB.setSetaHigherPriority(argument.equalsIgnoreCase("TRUE")); break;
                case "setaTunnelEffectSelectionByPercentage": DB.setSetaTunnelEffectSelectionByPercentage(argument.equalsIgnoreCase("TRUE")); break;
                case "setaTunnelEffectPercentage": DB.setSetaTunnelEffectPercentage(Double.parseDouble(argument)); break;
                case "setaTunnelEffectMinNumberOfInstructions": DB.setSetaMinNumberOfInstructions(Integer.parseInt(argument)); break;
                case "setaCheckersPercentageToVerify": DB.setSetaPercentageToVerify(Double.parseDouble(argument)); break;
                case "hetaOffset": DB.setHetaOffset(Integer.parseInt(argument));
            }
        }
    }
    
    public static void setParameters(String[] parameters)
    {
        Loader.parameters = parameters;
    }
    
    public static void setTargetProcessor(String targetProcessor)
    {
        DB.setTargetProcessor(targetProcessor);
    }
    
    private static void updatePaths()
    {
        String targetProcessor = DB.getTargetProcessor();
        targetProcessor = targetProcessor.concat(targetProcessor.isEmpty()?"":"/");
        mainConfigFilename = CONFIG_DIR.concat(targetProcessor).concat(mainConfigFilename);
        insConfigFilename = CONFIG_DIR.concat(targetProcessor).concat(insConfigFilename);
        regConfigFilename = CONFIG_DIR.concat(targetProcessor).concat(regConfigFilename);
        dumpConfigFilename = CONFIG_DIR.concat(targetProcessor).concat(dumpConfigFilename);
        techniquesConfigFilename = CONFIG_DIR.concat(targetProcessor).concat(techniquesConfigFilename);
    }
}
