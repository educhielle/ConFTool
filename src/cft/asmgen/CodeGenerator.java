/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cft.asmgen;

import cft.config.Dump;
import cft.config.DB;
import cft.error.ErrorManager;
import cft.util.Instruction;
import cft.util.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Angelo, Eduardo
 */
public class CodeGenerator
{
    private static Dump config = DB.getDump();
    private static String inputCodeFilename = DB.getDumpFilename();
    private static String functionName;
    private static Vector<String> labelAddresses = new Vector<String>();   // armazena os endereços de labels
    private static Vector<String> labelNames = new Vector<String>();       // aramazena os nomes de labels
    private static String functionAssemblyCode;
    private static List<String> emptyList = new ArrayList<String>();

    // Extrai do arquivo de entrada o código de máquina da função desejada
    private static String extractFunction() throws FileNotFoundException, IOException {
        File codeFile = new File(inputCodeFilename);
        if (!codeFile.exists())
        {
            ErrorManager.error(ErrorManager.INPUTFILENOTFOUND, "");
        }

        Pattern pattern = Pattern.compile(getMachineCodeRegex(config.getLabelFormat(), 't'));
        Matcher matcher = pattern.matcher("");
        
        BufferedReader codeReader = new BufferedReader(new FileReader(codeFile));
        String line;
        boolean functionFound = false;
        System.out.println(functionName);
        line = codeReader.readLine();
        while (line != null && !functionFound)
        {
            matcher.reset(line);
            if (matcher.matches())
            {
                if (matcher.group(1).equals(functionName))   // lê o arquivo até encontrar o label da função desejada
                {
                    functionFound = true;
                }
            }
            line = codeReader.readLine();
        }

        if (!functionFound)
            ErrorManager.error(ErrorManager.FUNCTIONNOTFOUND, "");
       
        String functionCode = new String();
        boolean endOfFunction = false;
        
        while (line != null && !endOfFunction) {
            matcher.reset(line);
            if (matcher.matches()) // se encontrou label
               endOfFunction = true;
            else
               functionCode = functionCode.concat(line + "\n");   // grava as linhas do código até o início de outra função ou fim de arquivo
            line = codeReader.readLine();
        }
        
        for (int i = 0; i < config.getInstructionsToReplace().size(); i++) 
        {
            if (config.getInstructionsToReplace().get(i).size() > 1)
            {
                functionCode = Util.replaceAll(functionCode, config.getInstructionsToReplace().get(i).get(0).toString(), config.getInstructionsToReplace().get(i).get(1).toString());
            }
        }
        
        return functionCode;
    }

    // Analisa o código de máquina dado e constrói vetores que indicam os nomes e os respectivos endereços de cada label
    private static void parseLabels(String inFunctionCode)
    {
        String[] functionLine = inFunctionCode.split("\n");
        //int labelID = 1;
        
        for (int i = 0; i < functionLine.length; i++)
        {
            String instruction = getMachineCodeInstruction(functionLine[i]);
            String[] operands = getMachineCodeOperands(functionLine[i]);
           
            // procura por branches para encontrar os labels necessários.
            // procura por jumps com destino endereçado diretamente para encontrar os labels necessários.
            if (config.isBranch(instruction) || config.isJumpToMemory(instruction))
            {
                //System.out.println(instruction + "\t" + operands[operands.length-1]);
                String labelAddress = removeMachineCodeCommentaries(operands[operands.length - 1]); //Considera a target como sendo o último operando
                String labelName = Util.replaceAll(Util.getBetween(operands[operands.length - 1], "<", ">"), "+", "_");
               
                // verifica se o label já foi identificado
                if (!labelAddresses.contains(labelAddress))
                {
                    //String labelName = functionName + labelID;
                    //labelID++;
                    labelAddresses.add(labelAddress);
                    labelNames.add(labelName);
                    //System.out.println(labelAddresses.get(labelAddresses.size()-1) + "\t" + labelNames.get(labelNames.size() - 1));
                }
            }
/*
           else if (config.isJumpToMemory(instruction)) {   // procura por jumps com destino endereçado diretamente para encontrar os labels necessários.
               
               String labelAddress = removeMachineCodeCommentaries(operands[operands.length - 1]);   // NÃO GENERALIZADO, destino sempre será o primeiro operando?
               if (!labelAddresses.contains(labelAddress)) {   // verifica se o label já foi identificado
                  String labelName = functionName + labelID;
                  labelID++;
                  labelAddresses.add(labelAddress);
                  labelNames.add(labelName);
               }
           }*/    
        }
    
        //System.out.println(labelNames);
        //System.out.println(labelAddresses);
    }

    // Converte o código de máquina dado para assembly
    private static String getAssemblyFormat(String inFunctionCode) {
        String functionCode = functionName + ":\n";
        String[] functionLine = replaceRegisters(inFunctionCode).split("\n");

        for (int i = 0; i < functionLine.length; i++) {   // define os labels e escreve cada instrução em formato assembly
           String address = getMachineCodeAddress(functionLine[i]);
           String instruction = getMachineCodeInstruction(functionLine[i]);
           String[] operands = getMachineCodeOperands(functionLine[i]);
           //System.out.println(address);
           //System.out.println(functionLine[i]);
           
           if (labelAddresses.contains(address))   // verifica início de label e o define se necessário
           {
               //System.out.println(functionLine[i]);
               functionCode = functionCode.concat(labelNames.elementAt(labelAddresses.indexOf(address)) + ":\n");
           }

           functionCode = functionCode.concat("\t" + instruction + "\t");
           
           if (config.isBranch(instruction)) {   // NÃO GENERALIZADO, destino sempre será o segundo operando?
              String labelName = labelNames.elementAt(labelAddresses.indexOf(removeMachineCodeCommentaries(operands[operands.length - 1])));
              functionCode = functionCode.concat((operands.length > 1?operands[0] + ",":"") + labelName + "\n");
           }

           else if (config.isJumpToMemory(instruction)) {   // NÃO GENERALIZADO, destino sempre será o primeiro operando?
              String labelName = labelNames.elementAt(labelAddresses.indexOf(removeMachineCodeCommentaries(operands[operands.length - 1])));
              functionCode = functionCode.concat(labelName + "\n");
           }

           else {
              if (operands != null) {
                 functionCode = functionCode.concat(operands[0]);
                 for (int j = 1; j < operands.length; j++)
                    functionCode = functionCode.concat("," + operands[j]);
              }
              functionCode = functionCode.concat("\n");
           }
        }
        
        functionCode = shiftBranchesAndJumps(functionCode);
        functionCode = removeNops(functionCode);
        
        return functionCode;
    }

    // Retorna o código de máquina dado com os registradores substituídos pelos seus equivalentes em assembly
    private static String replaceRegisters(String inFunctionCode) {
        String functionCode = new String();
        String[] functionLine = inFunctionCode.split("\n");

        Pattern pattern = Pattern.compile(getMachineCodeRegex(config.getInstructionFormat(), 'o'));
        Matcher matcher = pattern.matcher("");

        for (int i = 0; i < functionLine.length; i++) {
           matcher.reset(functionLine[i]);
           if (matcher.matches()) {
               String[] operands = matcher.group(1).split("[^\\w]");   // isola operando, remove parêntesis, chaves, ...
               Set<String> set = new HashSet<String>(Arrays.asList(operands));   // remove operandos duplicados
               set.toArray(operands);

               for (int j = 0; j < operands.length; j++)   // substitui todos os registradores por seus equivalentes em assembly
                  if (config.isRegister(operands[j]))
                     functionLine[i] = functionLine[i].replace(operands[j], config.getEquivalentAssemblyRegister(operands[j]));
           }
           functionCode = functionCode.concat(functionLine[i] + "\n");
        }

        return functionCode;
    }

    // Descloca branches e jumps conforme determinado pelo arquivo de configuração
    private static String shiftBranchesAndJumps(String inFunctionCode)
    {
        List<String> code = new ArrayList<String>(); //Arrays.asList(inFunctionCode.split("\n"));
        String[] functionLine = inFunctionCode.split("\n");
        String functionCode = new String();
        
        for (int i = 0; i < functionLine.length; i++)
        {
            String instruction = getAssemblyCodeInstruction(functionLine[i]);
            if (config.isBranch(instruction) || config.isJump(instruction))
            {
                boolean canContinue = true;
                int j;
                for (j = 0; j < config.getShift() && canContinue; j++)
                {
                    if (i + 1 + j < functionLine.length && getAssemblyCodeInstruction(functionLine[i + 1 + j]) != null)
                    {
                        String buffer = functionLine[i + 1 + j];
                        functionLine[i + 1 + j] = functionLine[i + j];
                        functionLine[i + j] = buffer;
                    }
                    else
                        canContinue = false;
                }
                i = i + j;
            }
        }
        
        for (int i = 0; i < functionLine.length; i++)
        {
            code.add(functionLine[i]);
            
            String instruction = getAssemblyCodeInstruction(functionLine[i]);
            
            if (config.isBranch(instruction) || config.isJump(instruction))
            {
                
                for (int j = 0; j < DB.getBranchDelaySlot(); j++)
                {
                    code.add(Util.generateCommand(DB.getNoOperationInstruction().getName(), emptyList, emptyList, emptyList, emptyList, emptyList, DB.getNoOperationInstruction().getFormat()));
                }
            }
        }

        /*for (int i = 0; i < functionLine.length; i++)
        {
            functionCode = functionCode.concat(functionLine[i] + "\n");
        }*/
        
        for (int i = 0; i < code.size(); i++)
        {
            functionCode = functionCode.concat(code.get(i) + "\n");
        }
        //System.out.println(functionCode);
        
        return functionCode;
    }

    private static String removeNops(String inFunctionCode)
    {
        String functionCode = new String();
        String[] functionLine = inFunctionCode.split("\n");

        for (int i = 0; i < functionLine.length; i++)
        {
            String instruction = getAssemblyCodeInstruction(functionLine[i]);
            
            if (config.isBranch(instruction) || config.isJump(instruction))
            {
                functionCode = functionCode.concat(functionLine[i] + "\n");
                for (int j = 0; j < DB.getBranchDelaySlot(); j++)
                {
                    functionCode = functionCode.concat(functionLine[++i] + "\n");
                }
            }
            else if (instruction == null || !instruction.equals("nop"))
            {
                functionCode = functionCode.concat(functionLine[i] + "\n");
            }
        }

        return functionCode;
    }
    
    private static String removeMachineCodeCommentaries(String codeLine) {
        Vector<String> commentaries = getMachineCodeCommentaries(codeLine);
        String noCommentaries = codeLine;

        for (int i = 0; i < commentaries.size(); i++) {
           String commentary = config.getCommentaryFormat().replace("t", commentaries.elementAt(i));   // formata o comentário assim como especificado no arquivo de configuração
           noCommentaries = noCommentaries.replace(commentary, "");   // remove o comentário
        }

        return noCommentaries.trim();
    }

    // Retorna os comentários da linha de código de máquina dada
    private static Vector<String> getMachineCodeCommentaries(String codeLine) {
        Vector<String> commentaries = new Vector<String>();

        String inputString = codeLine;
        Pattern pattern = Pattern.compile("(?:.*?)" + getMachineCodeRegex(config.getCommentaryFormat(), 't') + "(?:.*?)");
        Matcher matcher = pattern.matcher(inputString);

        int i = 0;
        while (matcher.matches()) {   // a cada iteração procura por um comentário, o adiciona ao vetor e o remove da String de entrada
           commentaries.add(matcher.group(1));
           String commentary = config.getCommentaryFormat().replace("t", commentaries.elementAt(i));
           inputString = inputString.replace(commentary, "");
           matcher.reset(inputString);
           i++;
        }

        return commentaries;
    }

    // Retorna a expressão regular para o formato e o grupo de captura dados
    private static String getMachineCodeRegex(String format, Character capturingGroupCode) {
        String regex = format;

        if (!capturingGroupCode.equals('x') && !capturingGroupCode.equals('t') && !capturingGroupCode.equals('a') && !capturingGroupCode.equals('i') && !capturingGroupCode.equals('o'))
           ErrorManager.error(ErrorManager.INVALIDGROUPCODE, "");

        // define grupo a ser capturado
        regex = regex.replaceAll("[" + Character.toUpperCase(capturingGroupCode) + "]+", "(.*?)");
        regex = regex.replaceAll("[" + capturingGroupCode + "]", "(.*?)");

        // define grupos que não serão capturados
        if (!capturingGroupCode.equals('x')) {
            regex = regex.replace("X", "\\w");
            regex = regex.replaceAll("[x]", "(?:.*?)");
        }

        if (!capturingGroupCode.equals('t')) {
            regex = regex.replace("T", ".");
            regex = regex.replaceAll("[t]", "(?:.*?)");
        }

        if (!capturingGroupCode.equals('a')) {
            regex = regex.replace("A", "\\w");
            regex = regex.replaceAll("[a]", "(?:.*?)");
        }

        if (!capturingGroupCode.equals('i')) {
            regex = regex.replaceAll("I", "\\w");
            regex = regex.replaceAll("[i]", "(?:.*?)");
        }

        if (!capturingGroupCode.equals('o')) {
            regex = regex.replaceAll("O", ".");
            regex = regex.replaceAll("[o]", "(?:.*?)");
        }

        return regex;
    }

    // Retorna o endereço da linha de código de máquina dada.
    private static String getMachineCodeAddress(String codeLine)
    {
        Pattern pattern = Pattern.compile(getMachineCodeRegex(config.getInstructionFormat(), 'a'));
        Matcher matcher = pattern.matcher(codeLine);

        if (matcher.matches())
        {
            return matcher.group(1).trim();
        }
        else
        {
            String instructionFormat = Util.removeExtraWhitespaces(Util.replaceAll(config.getInstructionFormat(), "o", ""));
            String base[] = {"a", "i", "X", "o"};
            
            List<String> retorno = Util.getIt(codeLine, instructionFormat, "a", base);
            
            if (retorno.isEmpty())
            {
                System.out.println(codeLine);
                return "";
            }
            
            //return Util.getIt(codeLine, instructionFormat, "i", base).get(0);
            
            return retorno.get(0);
        }
    }

    // Retorna a instrução da linha de código de máquina dada.
    private static String getMachineCodeInstruction(String codeLine) {
        Pattern pattern = Pattern.compile(getMachineCodeRegex(config.getInstructionFormat(), 'i'));
        Matcher matcher = pattern.matcher(codeLine.trim());

        //System.out.println(codeLine);
        if (codeLine.trim().contains("nop"))
            return new String("nop");

        if (matcher.matches())
           return matcher.group(1).trim();
        else
        {
            String instructionFormat = Util.removeExtraWhitespaces(Util.replaceAll(config.getInstructionFormat(), "o", ""));
            String base[] = {"a", "i", "X", "o"};
            
            List<String> retorno = Util.getIt(codeLine, instructionFormat, "i", base);
            
            if (retorno.isEmpty())
            {
                System.out.println(codeLine);
                return "";
            }
            
            //return Util.getIt(codeLine, instructionFormat, "i", base).get(0);
            
            return retorno.get(0);
        }
           
    }

    // Retorna os operandos da linha de código de máquina dada.
    private static String[] getMachineCodeOperands(String codeLine) {
        Pattern pattern = Pattern.compile(getMachineCodeRegex(config.getInstructionFormat(), 'o'));
        Matcher matcher = pattern.matcher(codeLine);

        if (matcher.matches())
           return matcher.group(1).split(config.getOperandSeparator());
        else
           return null;
    }

    // Retorna a instrução da linha de código assembly dada.
    private static String getAssemblyCodeInstruction(String codeLine) {
        Pattern pattern = Pattern.compile("\t(\\S+).*");
        Matcher matcher = pattern.matcher(codeLine);

        if (matcher.matches())
           return matcher.group(1).trim();
        else
           return null;
    }

    public static List<String> assemblyToDump(String line)
    {
        //Identify instruction and registers
        Instruction instruction = DB.getInstruction(line);
        List<String> rdList = DB.getRD(line, instruction.getFormat());
        List<String> regList = DB.getRS(line, instruction.getFormat());
        List<String> output = new ArrayList<String>();
        
        //Merge registers' lists in one list
        for (int i = 0; i < rdList.size(); i++)
        {
            if (!regList.contains(rdList.get(i)))
            {
                regList.add(rdList.get(i));
            }
        }
        
        //Replace instruction name if needed
        List<List> instructionsToReplace = DB.getDump().getInstructionsToReplace();
        String instructionName = instruction.getName();
        for (int i = 0; i < instructionsToReplace.size(); i++)
        {
            if (instructionsToReplace.get(i).get(0).toString().equals(instructionName))
            {
                instructionName = instructionsToReplace.get(i).get(1).toString();
                break;
            }
        }
        output.add(instructionName);
        line = Util.replaceAll(line, instruction.getName(), "").trim();
        
        //Replace registers names
        for (int i = 0; i < regList.size(); i++)
        {
            System.out.println("1:\t"+line);
            System.out.println(regList.get(i)+"\t:\t"+DB.getDump().getEquivalentDumpRegister(regList.get(i)));
            line = Util.replaceAll(line, regList.get(i), DB.getDump().getEquivalentDumpRegister(regList.get(i)));
            System.out.println("2:\t"+line);
        }
        output.add(line);
        
        return output;
    }
    
    public static String find(List<String> dumpLine) throws IOException
    {
        List<String> code = Util.readFile(DB.getDumpFilename());
        
        for (int i = 0; i < code.size(); i++)
        {
            String line = code.get(i);
            boolean contains = true;
            for (int j = 0; j < dumpLine.size(); j++)
            {
                if (!line.contains(dumpLine.get(j)))
                {
                    contains = false;
                    break;
                }
            }
            
            if (contains)
            {
                return line;
            }
        }
        
        return new String();
    }
    
    public static String getFunctionAssemblyCode(String functionName)
    {
        initialize();
        CodeGenerator.functionName = functionName;
        
        try
        {
            String functionCode = extractFunction();
            parseLabels(functionCode);
            functionAssemblyCode = getAssemblyFormat(functionCode);
        }
        catch (IOException ioe)
        {
            System.out.println("File not found. (CodeGenerator)");
        }
        
        return CodeGenerator.functionAssemblyCode;
    }
    
    public static String getHexa(String assemblyLine) throws IOException
    {
        String hexa = new String();
        List<String> dumpLine = assemblyToDump(assemblyLine);
        System.out.println(dumpLine);
        String fileLine = find(dumpLine);
        System.out.println(fileLine);
        List<String> items = Util.getIt(fileLine, DB.getDump().getInstructionFormat(), "X", DB.getDump().BASE);
        System.out.println(items);
        System.exit(0);
        return hexa;
    }
    
    public static void initialize()
    {
        CodeGenerator.labelAddresses = new Vector<String>();
        CodeGenerator.labelNames = new Vector<String>();
        CodeGenerator.config = DB.getDump();
        inputCodeFilename = DB.getDumpFilename();
    }
}