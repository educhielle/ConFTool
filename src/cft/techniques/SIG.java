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
public class SIG
{
    public static String apply(String fileNameIn) throws IOException 
    {
        String fileNameOut = Util.getFileTitle(fileNameIn) + "SIG." + Util.getFileExtension(fileNameIn);
        apply(fileNameIn, fileNameOut);
        
        return fileNameOut;
    }
    
    public static void apply(String fileNameIn, String fileNameOut) throws IOException
    {
        List<String> code = Util.getCode(fileNameIn, DB.getCommentTag());
        List<String> usedRegisters = DB.getUsedRegisters(code);
        List<String> freeRegisters = Util.complement(DB.getGeneralPurposeRegisters(), usedRegisters);
        
        if (freeRegisters.size() > 0)
        {
            List<String> signatureRegister = new ArrayList<String>();
            signatureRegister.add(freeRegisters.get(0));
            DB.setSignatureRegister(signatureRegister.get(0));
            Instruction branchComparison = DB.getNonEqualityComparisonInstruction();
            Instruction branchNotEqualZero = DB.getBranchNotEqualZeroInstruction();
            Instruction comparison = DB.getComparisonInstruction();
            Instruction comparisonWithImmediate = DB.getComparisonWithImmediateInstruction();
            Instruction loadImmediate = DB.getImmediateAssignmentInstruction();
            Instruction nop = DB.getNoOperationInstruction();
            Instruction subImmediate = DB.getSubImmediateInstruction();
            List<String> emptyList = new ArrayList<String>();
            List<String> errorLabelName = new ArrayList<String>();
            errorLabelName.add(DB.ERROR_LABEL_NAME);
            //boolean afterMain = true;
            
            boolean ignoreStep, isLabel, isBranch, isJump, isRegularInstruction, isComparisonInstruction, regularInstructionFirst;
            ignoreStep = isLabel = isBranch = isJump = isRegularInstruction = isComparisonInstruction = regularInstructionFirst = false;

            boolean freeComparisonArea = true, nextFreeComparisonArea = true;

            for (int i = 1, signatureValue = 0; i < code.size(); i++)
            {
                //Definir estado
                if (DB.isLabel(code.get(i)))
                {
                    isLabel = true;
                    isBranch = false;
                    isJump = false;
                    isComparisonInstruction = false;
                    freeComparisonArea = true;
                    nextFreeComparisonArea = true;
                }
                else if (DB.isInstruction(code.get(i)))
                {
                    Instruction instruction = DB.getInstruction(code.get(i));

                    if (DB.isBranch(instruction))
                    {
                        isBranch = true;
                        isJump = false;
                        isLabel = false;
                        isComparisonInstruction = false;
                        nextFreeComparisonArea = true;
                    }
                    else if (DB.isJump(instruction))
                    {
                        isBranch = false;
                        isJump = true;
                        isLabel = false;
                        isComparisonInstruction = false;
                        nextFreeComparisonArea = true;
                    }
                    else if (DB.isShiftWindowRegistersInstruction(instruction) && DB.isLocalRegister(DB.getSignatureRegister()))
                    {
                        isLabel = true;
                        isBranch = false;
                        isJump = false;
                        isComparisonInstruction = false;
                        freeComparisonArea = true;
                        nextFreeComparisonArea = true;
                    }
                    else if (DB.isComparisonInstruction(instruction))
                    {
                        isBranch = false;
                        isJump = false;
                        isLabel = false;
                        isComparisonInstruction = true;
                        freeComparisonArea = false;
                        nextFreeComparisonArea = false;
                    }
                    else
                    {
                        isRegularInstruction = true;
                        regularInstructionFirst = !(isLabel || isBranch || isJump || isComparisonInstruction);
                    }
                }
                
                //Aplicar técnica
                if (isLabel && isRegularInstruction)
                {
                    if (regularInstructionFirst)
                    {
                        //end
                        List<String> imm = new ArrayList<String>();
                        imm.add(String.valueOf(signatureValue));

                        if (DB.hasComparisonWithImmediateInstruction())
                        {
                            code.add(i++, Util.generateCommand(comparisonWithImmediate.getName(), emptyList, signatureRegister, imm, emptyList, emptyList, comparisonWithImmediate.getFormat()));
                            code.add(i++, Util.generateCommand(branchComparison.getName(), emptyList, signatureRegister, emptyList, emptyList, errorLabelName, branchComparison.getFormat()));
                        }
                        else
                        {
                            code.add(i++, Util.generateCommand(subImmediate.getName(), signatureRegister, signatureRegister, imm, emptyList, emptyList, subImmediate.getFormat()));
                            code.add(i++, Util.generateCommand(branchNotEqualZero.getName(), emptyList, signatureRegister, emptyList, emptyList, errorLabelName, branchNotEqualZero.getFormat()));
                        }
                        for (int j = 0; j < DB.getBranchDelaySlot(); j++)
                        {
                            code.add(i++, Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                        }

                        isRegularInstruction = false;
                        regularInstructionFirst = false;
                    }
                    else
                    {
                        //begin
                        List<String> imm = new ArrayList<String>();
                        imm.add(String.valueOf(++signatureValue));
                        code.add(i++, Util.generateCommand(loadImmediate.getName(), signatureRegister, emptyList, imm, emptyList, emptyList, loadImmediate.getFormat()));
                        isLabel = false;
                        regularInstructionFirst = true;
                    }
                }
                else if (isBranch || isJump)
                {
                    if (isRegularInstruction)
                    {
                        if (regularInstructionFirst)
                        {
                            if (freeComparisonArea)
                            {
                                //end
                                List<String> imm = new ArrayList<String>();
                                imm.add(String.valueOf(signatureValue));

                                if (DB.hasComparisonWithImmediateInstruction())
                                {
                                    code.add(i++, Util.generateCommand(comparisonWithImmediate.getName(), emptyList, signatureRegister, imm, emptyList, emptyList, comparisonWithImmediate.getFormat()));
                                    code.add(i++, Util.generateCommand(branchComparison.getName(), emptyList, signatureRegister, emptyList, emptyList, errorLabelName, branchComparison.getFormat()));
                                }
                                else
                                {
                                    code.add(i++, Util.generateCommand(subImmediate.getName(), signatureRegister, signatureRegister, imm, emptyList, emptyList, subImmediate.getFormat()));
                                    code.add(i++, Util.generateCommand(branchNotEqualZero.getName(), emptyList, signatureRegister, emptyList, emptyList, errorLabelName, branchNotEqualZero.getFormat()));
                                }
                                for (int j = 0; j < DB.getBranchDelaySlot(); j++)
                                {
                                    code.add(i++, Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                                }
                            }
                            
                            isRegularInstruction = false;
                            regularInstructionFirst = false;
                            i += DB.getBranchDelaySlot();
                        }
                        else
                        {
                            //begin
                            List<String> imm = new ArrayList<String>();
                            imm.add(String.valueOf(++signatureValue));
                            code.add(i++, Util.generateCommand(loadImmediate.getName(), signatureRegister, emptyList, imm, emptyList, emptyList, loadImmediate.getFormat()));
                            isBranch = false;
                            isJump = false;
                            regularInstructionFirst = true;
                        }
                    }
                    else
                    {
                        i += DB.getBranchDelaySlot();
                    }
                }
                else if (isComparisonInstruction)
                {
                    if (isRegularInstruction)
                    {
                        if (regularInstructionFirst)
                        {
                            //end
                            List<String> imm = new ArrayList<String>();
                            imm.add(String.valueOf(signatureValue));

                            if (DB.hasComparisonWithImmediateInstruction())
                            {
                                code.add(i++, Util.generateCommand(comparisonWithImmediate.getName(), emptyList, signatureRegister, imm, emptyList, emptyList, comparisonWithImmediate.getFormat()));
                                code.add(i++, Util.generateCommand(branchComparison.getName(), emptyList, signatureRegister, emptyList, emptyList, errorLabelName, branchComparison.getFormat()));
                            }
                            else
                            {
                                code.add(i++, Util.generateCommand(subImmediate.getName(), signatureRegister, signatureRegister, imm, emptyList, emptyList, subImmediate.getFormat()));
                                code.add(i++, Util.generateCommand(branchNotEqualZero.getName(), emptyList, signatureRegister, emptyList, emptyList, errorLabelName, branchNotEqualZero.getFormat()));
                            }
                            for (int j = 0; j < DB.getBranchDelaySlot(); j++)
                            {
                                code.add(i++, Util.generateCommand(nop.getName(), emptyList, emptyList, emptyList, emptyList, emptyList, nop.getFormat()));
                            }

                            isRegularInstruction = false;
                            regularInstructionFirst = false;
                            isComparisonInstruction = false;
                        }
                        else
                        {
                            regularInstructionFirst = true;
                            isComparisonInstruction = false;
                        }
                    }
                }
                
                freeComparisonArea = nextFreeComparisonArea;
            }
        }
        
        Util.write(code, fileNameOut);
    }
}
