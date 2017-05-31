/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cft.config;

import cft.util.Instruction;
import cft.util.Register;
import cft.util.Util;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eduardo
 */
public class Architecture
{
    private int branchDelaySlot = 0;
    private int wordSize = 32; //Default value for the word size
    private List<Instruction> instructions;
    private List<Register> registers;
    private List<String> invertedInstructions = new ArrayList<String>();
    private List<String> inverterInstructions = new ArrayList<String>();
    private List<String> preInitializedRegisters = new ArrayList<String>();
    private List<String> shiftWindowRegisterInstructionsNames = new ArrayList<String>();
    private String addImmediateInstructionName;
    private String addRegistersInstructionName;
    private String andImmediateInstructionName;
    private String branchNotEqualZeroInstructionName;
    private String commentTag;
    private String comparisonInstructionName;
    private String comparisonWithImmediateInstructionName;
    private String dataPattern;
    private String equalityComparisonInstructionName;
    private String higherHalfImmediateAssignmentInstructionName;
    private String immediateAssignmentInstructionName;
    private String jumpInstructionName;
    private String labelFormat;
    private String loadInstructionName;
    private String lowerHalfImmediateAssignmentInstructionName;
    private String mainLabelName = "main";
    private String moveInstructionName;
    private String nonEqualityComparisonInstructionName;
    private String noOperationInstructionName;
    private String operandsSeparator;
    private String registerZeroName;
    private String shiftLeftImmediateInstructionName;
    private String shiftRightArithmeticImmediateInstructionName;
    private String shiftRightImmediateInstructionName;
    private String storeInstructionName;
    private String subImmediateInstructionName;
    private String targetProcessor = new String();
    private String xorImmediateInstructionName;
    public static String BASE[] = { "ins", "offset", "rd", "rs", "rsd", "imm", "target", "label" };
    public static String BASE_PATTERNS[] = {};
    
    public void showInstructions()
    {
        for (Instruction i : this.instructions)
            System.out.println("Name: "+i.getName()+"\tFormat: "+i.getFormat()+"\tType: "+i.getType());
    }
    
    /*
     * Constructors
     */
    
    public Architecture(String commentTag, String labelFormat, List<Instruction> instructions, List<Register> registers, String operandsSeparator)
    {
        this.commentTag = commentTag;
        this.labelFormat = labelFormat;
        this.instructions = instructions;
        this.registers = registers;
        this.operandsSeparator = operandsSeparator;
    }
    
    public Architecture()
    {
        this("", "", new ArrayList<Instruction>(), new ArrayList<Register>(), "");
    }
    
    /*
     * Adders
     */
    
    public void addInstruction(Instruction instruction)
    {
        instructions.add(instruction);
    }
    
    public void addInstructions(List<Instruction> instructions)
    {
        for (int i = 0; i < instructions.size(); i++)
        {
            addInstruction(instructions.get(i));
        }
    }
    
    public void addRegister(Register register)
    {
        registers.add(register);
    }
    
    /*
     * Getters
     */
    
    public Instruction getAddImmediateInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(addImmediateInstructionName)
                    && (Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rd", BASE).size() == 1)
                    && (Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rs", BASE).size() == 1)
                    && (Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "imm", BASE).size() == 1))
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
    
    public Instruction getAddRegistersInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(addRegistersInstructionName)
                    && (Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rd", BASE).size() == 1)
                    && (Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rs", BASE).size() == 2)
                    && Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "imm", BASE).isEmpty())
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
    
    public Instruction getAndImmediateInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(andImmediateInstructionName) && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "imm", BASE).isEmpty())
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
    
    public int getBranchDelaySlot()
    {
        return branchDelaySlot;
    }
    
    public Instruction getBranchNotEqualZeroInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(branchNotEqualZeroInstructionName))
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
    
    public String getCommentTag()
    {
        return commentTag;
    }
    
    public Instruction getComparisonInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(comparisonInstructionName)
                    && (Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rs", BASE).size() >= 2))
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
    
    public Instruction getComparisonWithImmediateInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(comparisonWithImmediateInstructionName)
                    && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "imm", BASE).isEmpty())
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
    
    public String getDataPattern()
    {
        return dataPattern;
    }
    
    public Instruction getEqualityComparisonInstruction()
    {
        Instruction equalityComparisonInstruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(equalityComparisonInstructionName))
            {
                equalityComparisonInstruction = instructions.get(i);
                break;
            }
        }
        
        return equalityComparisonInstruction;
    }
    
    public List<String> getFunctionCalls()
    {
        List<String> functionCalls = new ArrayList<String>();

        for (int i = 0; i < instructions.size(); i++)
        {
            String type = instructions.get(i).getType().toLowerCase();
            if (type.equals("branch_to_target") || type.equals("jump_to_target"))
            {
                functionCalls.add(instructions.get(i).getName());
            }
        }

        return functionCalls;
    }
    
    public Instruction getHigherHalfImmetiadeAssignmentInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(higherHalfImmediateAssignmentInstructionName)
                    && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "imm", BASE).isEmpty()
                    && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rd", BASE).isEmpty())
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
    
    public Instruction getImmetiadeAssignmentInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(immediateAssignmentInstructionName)
                    && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "imm", BASE).isEmpty()
                    && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rd", BASE).isEmpty())
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
    
    public List<Instruction> getInstruction(String name)
    {
        List<Instruction> instruction = new ArrayList<Instruction>();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(name))
            {
                instruction.add(instructions.get(i));
            }
        }
        
        return instruction;
    }
    
    public List<String> getInstructionFormat()
    {
        List<String> instructionFormat = new ArrayList<String>();
        
        for (int i = 0; i < instructions.size(); i++)
        {
            String format = instructions.get(i).getFormat();
            if (instructionFormat.indexOf(format) == -1)
            {
                instructionFormat.add(format);
            }
        }
        
        return instructionFormat;
    }
    
    public List<String> getInstructionFormat(String instruction)
    {
        List<String> format = new ArrayList<String>();
        
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equals(instruction))
            {
                format.add(instructions.get(i).getFormat());
            }
        }
        
        return format;
    }
    
    public List<Instruction> getInvertedInstruction(Instruction instruction)
    {
        String invertedName = invertedInstructions.get(inverterInstructions.indexOf(instruction.getName()));
        
        return getInstruction(invertedName);
    }
    
    public Instruction getJumpInstruction()
    {
        Instruction jumpInstruction = new Instruction();        
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(jumpInstructionName))
            {
                jumpInstruction = instructions.get(i);
                break;
            }
        }
        
        return jumpInstruction;
    }
    
    public String getLabelFormat()
    {
        return labelFormat;
    }
    
    public Instruction getLoadInstruction()
    {
        Instruction loadInstruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(loadInstructionName)
                    && (Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rd", BASE).size() == 1)
                    && (Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rs", BASE).size() == 1)
                    && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "offset", BASE).isEmpty())
            {
                loadInstruction = instructions.get(i);
                break;
            }
        }
        
        return loadInstruction;
    }
    
    public Instruction getLowerHalfImmetiadeAssignmentInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(lowerHalfImmediateAssignmentInstructionName)
                    && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "imm", BASE).isEmpty()
                    && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rd", BASE).isEmpty())
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
        
    public Instruction getMoveInstruction()
    {
        Instruction moveInstruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(moveInstructionName)
                    && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rs", BASE).isEmpty()
                    && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rd", BASE).isEmpty())
            {
                moveInstruction = instructions.get(i);
                break;
            }
        }
        
        return moveInstruction;
    }
    
    public String getMainLabelName()
    {
        return mainLabelName;
    }
    
    public Instruction getNonEqualityComparisonInstruction()
    {
        Instruction equalityComparisonInstruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(nonEqualityComparisonInstructionName))
            {
                equalityComparisonInstruction = instructions.get(i);
                break;
            }
        }
        
        return equalityComparisonInstruction;
    }
    
    public Instruction getNoOperationInstruction()
    {
        Instruction instruction = new Instruction();
        
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(noOperationInstructionName))
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
    
    public String getOperandsSeparator()
    {
        return operandsSeparator;
    }
    
    public List<Register> getPreInitializedRegisters()
    {
        List<Register> list = new ArrayList<Register>();
        
        for(int i = 0; i < registers.size(); i++)
        {
            if (preInitializedRegisters.contains(registers.get(i).getName()))
            {
                list.add(registers.get(i));
            }
        }
        
        return list;
    }
    
    public List<Register> getRegisters()
    {
        return registers;
    }
    
    public Register getRegisterZero()
    {
        Register register = new Register();
        for (int i = 0; i < registers.size(); i++)
        {
            if (registers.get(i).getName().equalsIgnoreCase(registerZeroName))
            {
                register = registers.get(i);
                break;
            }
        }
        
        return register;
    }
    
    public Instruction getShiftLeftImmediateInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(shiftLeftImmediateInstructionName) && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "imm", BASE).isEmpty())
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
    
    public Instruction getShiftRightArithmeticImmediateInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(shiftRightArithmeticImmediateInstructionName) && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "imm", BASE).isEmpty())
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
    
    public Instruction getShiftRightImmediateInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(shiftRightImmediateInstructionName) && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "imm", BASE).isEmpty())
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
    
    public Instruction getStoreInstruction()
    {
        Instruction storeInstruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(storeInstructionName)
                    && (Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rs", BASE).size() == 2)
                    && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "offset", BASE).isEmpty())
            {
                storeInstruction = instructions.get(i);
                break;
            }
        }
        
        return storeInstruction;
    }
    
    public Instruction getSubImmediateInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(subImmediateInstructionName) && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "imm", BASE).isEmpty())
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }

    public String getTargetProcessor()
    {
        return targetProcessor;
    }
    
    public int getWordSize()
    {
        return wordSize;
    }
    
    public Instruction getXorImmediateInstruction()
    {
        Instruction instruction = new Instruction();
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(xorImmediateInstructionName) && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "imm", BASE).isEmpty())
            {
                instruction = instructions.get(i);
                break;
            }
        }
        
        return instruction;
    }
    
    public boolean hasComparisonInstruction()
    {
        boolean hasComparisonInstruction = false;
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(comparisonInstructionName)
                    && (Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "rs", BASE).size() >= 2))
            {
                hasComparisonInstruction = true;
                break;
            }
        }
        
        return hasComparisonInstruction;
    }
    
    public boolean hasComparisonWithImmediateInstruction()
    {
        boolean hasComparisonInstruction = false;
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(comparisonWithImmediateInstructionName)
                    && !Util.getIt(instructions.get(i).getFormat(), instructions.get(i).getFormat(), "imm", BASE).isEmpty())
            {
                hasComparisonInstruction = true;
                break;
            }
        }
        
        return hasComparisonInstruction;
    }
    
    public boolean isArithmetic(Instruction instruction)
    {
        if (instruction.getType().equalsIgnoreCase("arithmetic")) return true;
        
        return false;
    }
    
    public boolean isBranch(Instruction instruction)
    {
        if (instruction.getType().equalsIgnoreCase("branch_to_target")) return true;
        
        return false;
    }
    
    public boolean isComparison(Instruction instruction)
    {
        if (instruction.getType().equalsIgnoreCase("comparison")) return true;
        
        return false;
    }
    
    public boolean isComparisonInstruction(Instruction instruction)
    {
        Instruction comparison = getComparisonInstruction();
        Instruction comparisonWithImmediate = getComparisonWithImmediateInstruction();
        
        return (instruction.equals(comparison) || instruction.equals(comparisonWithImmediate));
    }
    
    public boolean isGlobalRegister(Register register)
    {
        if (register.getType().equalsIgnoreCase("global")) return true;
        
        return false;
    }
    
    public boolean isInputRegister(Register register)
    {
        if (register.getType().equalsIgnoreCase("input")) return true;
        
        return false;
    }
    
    public boolean isInstructionName(String name)
    {
        for (int i = 0; i < instructions.size(); i++)
        {
            if (instructions.get(i).getName().equalsIgnoreCase(name)) return true;
        }
        
        return false;
    }
    
    public boolean isJump(Instruction instruction)
    {
        return (isJumpToRegister(instruction) || isJumpToTarget(instruction));
    }
    
    public boolean isJumpToRegister(Instruction instruction)
    {
        if (instruction.getType().equalsIgnoreCase("jump_to_register")) return true;
        
        return false;
    }
    
    public boolean isJumpToTarget(Instruction instruction)
    {
        if (instruction.getType().equalsIgnoreCase("jump_to_target")) return true;
        
        return false;
    }
    
    public boolean isLoad(Instruction instruction)
    {
        if (instruction.getType().equalsIgnoreCase("load")) return true;
        
        return false;
    }
    
    public boolean isLoadFromStack(Instruction instruction)
    {
        if (instruction.getType().equalsIgnoreCase("load_from_stack")) return true;
        
        return false;
    }
    
    public boolean isLocalRegister(Register register)
    {
        if (register.getType().equalsIgnoreCase("local")) return true;
        
        return false;
    }

    public boolean isOther(Instruction instruction)
    {
        if (instruction.getType().equalsIgnoreCase("other")) return true;

        return false;
    }
    
    public boolean isOutputRegister(Register register)
    {
        if (register.getType().equalsIgnoreCase("output")) return true;
        
        return false;
    }
    
    public boolean isPreInitializedRegisterName(String name)
    {
        return preInitializedRegisters.contains(name);
    }
    
    public boolean isRegisterName(String name)
    {
        for (int i = 0; i < registers.size(); i++)
        {
            if (registers.get(i).getName().equalsIgnoreCase(name)) return true;
        }
        
        return false;
    }
    
    public boolean isShiftWindowRegisterInstruction(Instruction instruction)
    {
        for (int i = 0; i < shiftWindowRegisterInstructionsNames.size(); i++)
        {
            if (instruction.getName().equalsIgnoreCase(shiftWindowRegisterInstructionsNames.get(i))) return true;
        }
        
        return false;
    }
    
    public boolean isStore(Instruction instruction)
    {
        if (instruction.getType().equalsIgnoreCase("store")) return true;
        
        return false;
    }
    
    public boolean isStoreToStack(Instruction instruction)
    {
        if (instruction.getType().equalsIgnoreCase("store_to_stack")) return true;
        
        return false;
    }
    
    /*
     * Removers
     */
    public void removeRegisterByName(String registerName)
    {
        for (Register register : registers)
        {
            if (register.getName().equals(registerName))
            {
                registers.remove(register);
                break;
            }
        }
    }
    
    /*
     * Setters
     */
    public void setAddImmediateInstructionName(String addImmediateInstructionName) {
        this.addImmediateInstructionName = addImmediateInstructionName;
    }
    
    public void setAddRegistersInstructionName(String addRegistersInstructionName) {
        this.addRegistersInstructionName = addRegistersInstructionName;
    }
    
    public void setAndImmediateInstructionName(String andImmediateInstructionName) {
        this.andImmediateInstructionName = andImmediateInstructionName;
    }
    
    public void setBranchDelaySlot(int branchDelaySlot)
    {
        this.branchDelaySlot = branchDelaySlot;
    }

    public void setBranchNotEqualZeroInstructionName(String branchNotEqualZeroInstructionName)
    {
        this.branchNotEqualZeroInstructionName = branchNotEqualZeroInstructionName;
    }
    
    public void setCommentTag(String commentTag)
    {
        this.commentTag = commentTag;
    }
    
    public void setComparisonInstructionName(String comparisonInstructionName)
    {
        this.comparisonInstructionName = comparisonInstructionName;
    }

    public void setComparisonWithImmediateInstructionName(String comparisonWithImmediateInstructionName)
    {
        this.comparisonWithImmediateInstructionName = comparisonWithImmediateInstructionName;
    }

    public void setDataPattern(String dataPattern)
    {
        this.dataPattern = dataPattern;
    }
    
    public void setEqualityComparisonInstructionName(String equalityComparisonInstructionName)
    {
        this.equalityComparisonInstructionName = equalityComparisonInstructionName;
    }
    
    public void setHigherHalfImmediateAssignmentInstructionName(String higherHalfImmediateAssignmentInstructionName)
    {
        this.higherHalfImmediateAssignmentInstructionName = higherHalfImmediateAssignmentInstructionName;
    }
    
    public void setImmediateAssignmentInstructionName(String immediateAssignmentInstructionName)
    {
        this.immediateAssignmentInstructionName = immediateAssignmentInstructionName;
    }

    public void setInvertedInstructions(List<String> invertedInstructions)
    {
        this.invertedInstructions = invertedInstructions;
    }

    public void setInverterInstructions(List<String> inverterInstructions)
    {
        this.inverterInstructions = inverterInstructions;
    }
    
    public void setJumpInstructionName(String jumpInstructionName)
    {
        this.jumpInstructionName = jumpInstructionName;
    }
    
    public void setLabelFormat(String labelFormat)
    {
        this.labelFormat = labelFormat;
    }
    
    public void setLoadInstructionName(String loadInstructionName)
    {
        this.loadInstructionName = loadInstructionName;
    }
    
    public void setLowerHalfImmediateAssignmentInstructionName(String lowerHalfImmediateAssignmentInstructionName)
    {
        this.lowerHalfImmediateAssignmentInstructionName = lowerHalfImmediateAssignmentInstructionName;
    }
    
    public void setMainLabelName(String mainLabelName)
    {
        this.mainLabelName = mainLabelName;
    }
    
    public void setMoveInstructionName(String moveInstructionName)
    {
        this.moveInstructionName = moveInstructionName;
    }
    
    public void setNonEqualityComparisonInstructionName(String nonEqualityComparisonInstructionName)
    {
        this.nonEqualityComparisonInstructionName = nonEqualityComparisonInstructionName;
    }

    public void setNoOperationInstructionName(String noOperationInstructionName)
    {
        this.noOperationInstructionName = noOperationInstructionName;
    }
    
    public void setOperandsSeparator(String operandsSeparator)
    {
        this.operandsSeparator = operandsSeparator;
    }
    
    public void setPreInitializedRegisters(List<String> preInitializedRegisters)
    {
        this.preInitializedRegisters = preInitializedRegisters;
    }

    public void setRegisterZeroName(String registerZeroName)
    {
        this.registerZeroName = registerZeroName;
    }
    
    public void setShiftLeftImmediateInstructionName(String shiftLeftImmediateInstructionName)
    {
        this.shiftLeftImmediateInstructionName = shiftLeftImmediateInstructionName;
    }
    
    public void setShiftRightArithmeticImmediateInstructionName(String shiftRightArithmeticImmediateInstructionName)
    {
        this.shiftRightArithmeticImmediateInstructionName = shiftRightArithmeticImmediateInstructionName;
    }
    
    public void setShiftRightImmediateInstructionName(String shiftRightImmediateInstructionName)
    {
        this.shiftRightImmediateInstructionName = shiftRightImmediateInstructionName;
    }
    
    public void setShiftWindowRegistersInstructionsNames(List<String> shiftWindowRegisterInstructionsNames)
    {
        this.shiftWindowRegisterInstructionsNames = shiftWindowRegisterInstructionsNames;
    }
    
    public void setStoreInstructionName(String storeInstructionName)
    {
        this.storeInstructionName = storeInstructionName;
    }
    
    public void setSubImmediateInstructionName(String subImmediateInstructionName)
    {
        this.subImmediateInstructionName = subImmediateInstructionName;
    }

    public void setTargetProcessor(String targetProcessor)
    {
        this.targetProcessor = targetProcessor;
    }
       
    public void setWordSize(int wordSize)
    {
        this.wordSize = wordSize;
    }
    
    public void setXorImmediateInstructionName(String xorImmediateInstructionName)
    {
        this.xorImmediateInstructionName = xorImmediateInstructionName;
    }
}
