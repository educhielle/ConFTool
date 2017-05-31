/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cft.util;


import cft.config.Dump;
import cft.error.ErrorManager;

import cft.config.DB;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Gennaro
 */
public class DumpFileDBAnalyser {
        
    private static final Dump config = DB.getDump();
    private static final String inputCodeFilename = DB.getDumpFilename();
          
    private static final List<String> labelNames = new ArrayList<String>();       // aramazena os nomes de labels
    private static final List<String> labelAddresses = new ArrayList<String>();   // armazena os endereços de labels
    private static final List<String> emptyList = new ArrayList<String>();
    private static final List<String> extractedLabels = new ArrayList<String>();    // lista de labels extraidas do codigo

    private static final List<Integer> basicBlockStarts = new ArrayList<Integer>();
    private static final List<Integer> basicBlockEnds = new ArrayList<Integer>();
    
    private static final List<BasicBlock> basicBlocks = new ArrayList<BasicBlock>();


    public DumpFileDBAnalyser() throws IOException {
     
        /* Code extraction  */
        String code = extractCode("main"); // exctracts the main code
//        System.out.println("code: \n" +  code);
        code = parseFunctions(code);
//        System.out.println("Parsed code: \n" +  code);
//        System.out.println("Extracted labels:" +  extractedLabels);
        code = removeBlanks(code);
//        System.out.println("Unblanked code: \n" +  code);
        code = ordenateCode(code); 
//        System.out.println("Ordened code: \n" +  code);
        
        
        /* Code Analysis */
        String codeWithoutNops = removeNops(code);
        parseLabels(codeWithoutNops); // labels must be parsed before and without nops
        code = shiftBranchesAndJumps(code); // shifts must take place BEFORE removing nops
        code = removeNops(code); 
//        System.out.println("Dump Code fully manipulated:\n" +  code);
        parseBlocks(code);
        
        System.out.println("Dump Starts:" + basicBlockStarts);
        System.out.println("Dump Ends:" + basicBlockEnds);
        
        correctBBInconsistances();
        createBasicBlocks();


        assignNSSignature(code);
        
//        System.out.println("Dump Labels:" + labelNames);
//        System.out.println("Dump Labels' addrs:" + labelAddresses);
//        System.out.println("Dump Starts:" + basicBlockStarts);
//        System.out.println("Dump Ends:" + basicBlockEnds);
    }
 
    
    // adiciona ao código extraído os códigos das funções chamadas pelo código já extraido
    private String parseFunctions(String inFunctionCode)
    {
        
        String moreCode = new String();
        
        boolean foundFunc =  true;
        
        while (foundFunc == true) 
        {
            foundFunc = false;
            
            String[] functionLine = inFunctionCode.split("\n");

            for (int i = 0; i < functionLine.length; i++) // percorre o codigo inteiro
            {
                
                
                String instruction = getMachineCodeInstruction(functionLine[i]);
                String[] operands = getMachineCodeOperands(functionLine[i]);

 
                // procura por branches e jumps para encontrar os labels necessários.
                if (config.isBranch(instruction) || config.isJumpToMemory(instruction)) 
                {
                    String labelAddress = removeMachineCodeCommentaries(operands[operands.length - 1]); //Considera a target como sendo o último operando
                    String labelName = Util.replaceAll(Util.getBetween(operands[operands.length - 1], "<", ">"), "+", "_");

                    boolean found = false;

                    //verifies if the labelAddress is inside the already extracted code:
                    for (int j = 0; j < functionLine.length; j++) 
                    {
                        if (!functionLine[j].isEmpty()) {
                            String instructionAddr = getMachineCodeAddress(functionLine[j]);

                            if (instructionAddr.equals(labelAddress)) {
                                found = true;
                            }
                        }

                    }
                    if (found == false) 
                    { // if the label was not found in the already extracted code...
                        try {
                            inFunctionCode = inFunctionCode.substring(0, inFunctionCode.length() - 1);
                            moreCode = extractCode(labelName); 
                            inFunctionCode = inFunctionCode.concat(moreCode); // adds the function code
                            foundFunc = true;
                            break;
                        } catch (IOException ex) {
                            Logger.getLogger(DumpFileDBAnalyser.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                   
                }
            }
           
            
        }
        return inFunctionCode;
        
    }
    
    // analisa o codigo em relação às labels
    private static void parseLabels(String inFunctionCode)
    {
        String[] functionLine = inFunctionCode.split("\n");
        
        for (int i = 0; i < functionLine.length; i++) // percorre o codigo inteiro
        {
            String instruction = getMachineCodeInstruction(functionLine[i]);
            String[] operands = getMachineCodeOperands(functionLine[i]);
            
            int instructionAddr = Integer.parseInt(getMachineCodeAddress(functionLine[i]),16);
            
            
            if (basicBlockStarts.isEmpty()) // the first instruction should be the start of the first BB
                basicBlockStarts.add(i);


            // procura por branches para encontrar os labels necessários.
            // procura por jumps com destino endereçado diretamente para encontrar os labels necessários.
            if (config.isBranch(instruction) || config.isJumpToMemory(instruction) || config.isJumpToRegister(instruction) || config.isJumpToMemory(instruction))
            {

                String labelAddress = new String();
                
               if (config.isJumpToMemory(instruction))
                   labelAddress = removeMachineCodeCommentaries(operands[0]);
               else
                   labelAddress = removeMachineCodeCommentaries(operands[operands.length - 1]); //Considera a target como sendo o último operando
                
                String labelName = Util.replaceAll(Util.getBetween(operands[operands.length - 1], "<", ">"), "+", "_");
                
                
               
                // verifica se o label já foi identificado
                if (!labelAddresses.contains(labelAddress) && !config.isJumpToRegister(instruction))
                {
                    //String labelName = functionName + labelID;
                    //labelID++;
                    labelAddresses.add(labelAddress);
                    labelNames.add(labelName);
                   
                    
                    /* remember that branches and jumps were shifted,  but the instructions' addrs are still the same (they must be, because of the
                    jumps and branches addresses), so now we may have something like:
                    100: instr ...
                    108: instr ...
                    104: instr ...
                    */
                    int instAtLabel = -1;
                    int instBeforeLabel = -1;
                    
                    // verifies endings according to labels
                    for (int j = 0; j < functionLine.length; j++) // percorre o codigo inteiro
                    {
                        int findAddr = Integer.parseInt(getMachineCodeAddress(functionLine[j]), 16);
                        
                        if (findAddr == (Integer.parseInt(labelAddress, 16))){ // found the label location on the code
                            instAtLabel = j;
                            instBeforeLabel = j - 1;
                        }
                        
                    }
     
                    if (instAtLabel >= 0) {
                        if (!basicBlockStarts.contains(instAtLabel)) {
                            basicBlockStarts.add(instAtLabel);
                        }
                    }

                    if (instBeforeLabel >= 0) {
                        if (!basicBlockEnds.contains(instBeforeLabel)) {
                            basicBlockEnds.add(instBeforeLabel);
                        }
                    }
                        
                        
                }   

            }

        }
        
        Collections.sort(basicBlockStarts);
        Collections.sort(basicBlockEnds);
       
    }
    
     // Analisa o código de máquina dado e constrói vetores que indicam os nomes e os respectivos endereços de cada label
     private static void parseBlocks(String inFunctionCode)
     {
        String[] functionLine = inFunctionCode.split("\n");
        
        for (int i = 0; i < functionLine.length; i++) // percorre o codigo inteiro
        {
            String instruction = getMachineCodeInstruction(functionLine[i]);
            String[] operands = getMachineCodeOperands(functionLine[i]);
            
            int instructionAddr = Integer.parseInt(getMachineCodeAddress(functionLine[i]),16);
            
            
            if (basicBlockStarts.isEmpty()) // the first instruction should be the start of the first BB
                basicBlockStarts.add(i);


            // procura por branches para encontrar os labels necessários.
            // procura por jumps com destino endereçado diretamente para encontrar os labels necessários.
            if (config.isBranch(instruction) || config.isJumpToMemory(instruction) || config.isJumpToRegister(instruction) || config.isJumpToMemory(instruction))
            {

                String labelAddress = new String();
                
               if (config.isJumpToMemory(instruction))
                   labelAddress = removeMachineCodeCommentaries(operands[0]);
               else
                   labelAddress = removeMachineCodeCommentaries(operands[operands.length - 1]); //Considera a target como sendo o último operando
                
                String labelName = Util.replaceAll(Util.getBetween(operands[operands.length - 1], "<", ">"), "+", "_");
                
       
                 // if finds a branch or jump, that must be the >end< of a BB
                if (!basicBlockEnds.contains(i)) 
                    basicBlockEnds.add(i); // adds to the list of BB endings.
                

                if((i + 1) < functionLine.length)
                    if (!basicBlockStarts.contains(i+1)) 
                        basicBlockStarts.add(i+1); // as i was the end of a BB, i+1 should be the start of a new one
                    

            }

        }
        
        Collections.sort(basicBlockStarts);
        Collections.sort(basicBlockEnds);
       
    }
     
     //Correct inconsistances in basicBlockDivisions
     private static void correctBBInconsistances()
     {
                
        //If there is no END between STARTS, remove the first START from basicBlockStarts
        for (int i = 0; i < basicBlockStarts.size() - 1; i++)
        {
            boolean ok = false;
            for (int j = 0; j < basicBlockEnds.size(); j++)
            {
                if ((basicBlockEnds.get(j) >= basicBlockStarts.get(i)) && (basicBlockEnds.get(j) < basicBlockStarts.get(i+1)))
                {
                    ok = true;
                    break;
                }
            }
            
            if (!ok)
            {
                basicBlockStarts.remove(i--);
            }
        }
        
        if (basicBlockStarts.get(basicBlockStarts.size() - 1) >= basicBlockEnds.get(basicBlockEnds.size() - 1))
        {
            basicBlockStarts.remove(basicBlockStarts.size() - 1);
        }
         
     }
     
    // Descloca branches e jumps conforme determinado pelo arquivo de configuração
    private static String shiftBranchesAndJumps(String inFunctionCode)
    {
        List<String> code = new ArrayList<String>(); //Arrays.asList(inFunctionCode.split("\n"));
        String[] functionLine = inFunctionCode.split("\n");
        String functionCode = new String();
        
        for (int i = 0; i < functionLine.length; i++)
        {
            String instruction = getMachineCodeInstruction(functionLine[i]);

            if (config.isBranch(instruction) || config.isJump(instruction))
            {
                boolean canContinue = true;
                int j;
                for (j = 0; j < config.getShift() && canContinue; j++)
                {
                    if (i + 1 + j < functionLine.length && getMachineCodeInstruction(functionLine[i + 1 + j])!= null)
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
            
            String instruction = getMachineCodeInstruction(functionLine[i]);
            
            if (config.isBranch(instruction) || config.isJump(instruction))
            {
                
                for (int j = 0; j < DB.getBranchDelaySlot(); j++)
                {
                    code.add(Util.generateCommand(DB.getNoOperationInstruction().getName(), emptyList, emptyList, emptyList, emptyList, emptyList, DB.getNoOperationInstruction().getFormat()));
                }
            }
        }

        for (int i = 0; i < code.size(); i++)
        {
            functionCode = functionCode.concat(code.get(i) + "\n");
        }
        
        return functionCode;
    }

    // Retorna a instrução da linha de código de máquina dada.
    private static String getMachineCodeInstruction(String codeLine) 
    {
        Pattern pattern = Pattern.compile(getMachineCodeRegex(config.getInstructionFormat(), 'i'));
        Matcher matcher = pattern.matcher(codeLine.trim());


        if (codeLine.trim().contains("nop"))
            return "nop";

        if (matcher.matches())
           return matcher.group(1).trim();
        else
        {
            String instructionFormat = Util.removeExtraWhitespaces(Util.replaceAll(config.getInstructionFormat(), "o", ""));
            String base[] = {"a", "i", "X", "o"};
            
            
            if(! Util.getIt(codeLine, instructionFormat, "i", base).isEmpty())
                return Util.getIt(codeLine, instructionFormat, "i", base).get(0);
            else
                return "blank"; //in case of blank line
        }
           
    }
    
    // Extrai do arquivo de entrada o código de máquina da função desejada (delimitada por uma label)
    private static String extractCode(String functionName) throws FileNotFoundException, IOException 
    {
        
        if(extractedLabels.contains(functionName))
            return "";
        
        
        String lastInstruction = new String();
        String beforeLastInstruction = new String();
        
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
        line = codeReader.readLine();
        
        while (line != null && !functionFound)
        {
            matcher.reset(line);
            if (matcher.matches())
            {
                if (matcher.group(1).equals(functionName))   // lê o arquivo até encontrar o label da função desejada
                {
                    functionFound = true;
                    extractedLabels.add(functionName);
                }
            }
            line = codeReader.readLine();
        }

        if (!functionFound)
            ErrorManager.error(ErrorManager.FUNCTIONNOTFOUND, "");
       
        String functionCode = new String();
        boolean endOfFunction = false;
        
        while (line != null && endOfFunction == false) 
        {
            matcher.reset(line);
            if (matcher.matches()) // se encontrou label
            {
                if(functionName.equals("main"))
                    endOfFunction = true;
                else 
                {
                    if (extractedLabels.contains(matcher.group(1))) 
                    {
                        endOfFunction = true;
                    } 
                    else 
                    {            
                        if(config.isJumpToMemory(beforeLastInstruction) || config.isJumpToRegister(beforeLastInstruction))
                            endOfFunction = true;
                    }
                }
               
            }
            else
            {
 
                if (!getMachineCodeInstruction(line).equals("blank")) 
                {
                    beforeLastInstruction = lastInstruction;
                    lastInstruction = getMachineCodeInstruction(line);
                }

                
                functionCode = functionCode.concat(line + "\n");
                
            }
            
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
          
    // Retorna os operandos da linha de código de máquina dada.
    private static String[] getMachineCodeOperands(String codeLine) {
        Pattern pattern = Pattern.compile(getMachineCodeRegex(config.getInstructionFormat(), 'o'));
        Matcher matcher = pattern.matcher(codeLine);

        if (matcher.matches())
           return matcher.group(1).split(config.getOperandSeparator());
        else
           return null;
    }

    private static String removeMachineCodeCommentaries(String codeLine) {
        List<String> commentaries = getMachineCodeCommentaries(codeLine);
        String noCommentaries = codeLine;

        for (int i = 0; i < commentaries.size(); i++) {
           String commentary = config.getCommentaryFormat().replace("t", commentaries.get(i));   // formata o comentário assim como especificado no arquivo de configuração
           noCommentaries = noCommentaries.replace(commentary, "");   // remove o comentário
        }

        return noCommentaries.trim();
    }
   
    // Retorna os comentários da linha de código de máquina dada
    private static List<String> getMachineCodeCommentaries(String codeLine) {
        List<String> commentaries = new ArrayList<String>();

        String inputString = codeLine;
        Pattern pattern = Pattern.compile("(?:.*?)" + getMachineCodeRegex(config.getCommentaryFormat(), 't') + "(?:.*?)");
        Matcher matcher = pattern.matcher(inputString);

        int i = 0;
        while (matcher.matches()) {   // a cada iteração procura por um comentário, o adiciona ao vetor e o remove da String de entrada
           commentaries.add(matcher.group(1));
           String commentary = config.getCommentaryFormat().replace("t", commentaries.get(i));
           inputString = inputString.replace(commentary, "");
           matcher.reset(inputString);
           i++;
        }

        return commentaries;
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
            if (! Util.getIt(codeLine, instructionFormat, "a", base).isEmpty())
                return Util.getIt(codeLine, instructionFormat, "a", base).get(0);
            else
                return "-1";
        }
    }
    
    private static String removeNops(String inFunctionCode)
    {
        String functionCode = new String();
        String[] functionLine = inFunctionCode.split("\n");

        for (int i = 0; i < functionLine.length; i++)
        {
            String instruction = getMachineCodeInstruction(functionLine[i]);
            
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
    
    private static String removeBlanks(String inFunctionCode)
    {
        String functionCode = new String();
        String[] functionLine = inFunctionCode.split("\n");

        for (int i = 0; i < functionLine.length; i++)
        {
            String instruction = getMachineCodeInstruction(functionLine[i]);
            
            if (instruction == null || !instruction.equals("blank"))
            {
                functionCode = functionCode.concat(functionLine[i] + "\n");
            }
        }

        return functionCode;
    }
         
    // puts instructions in their right order (by sorting by theis addresses°
    private String ordenateCode(String inFunctionCode) 
    {
        
        String code = new String();
        String[] functionLine = inFunctionCode.split("\n");
        List<Integer> addrsList = new ArrayList<Integer>();
        
          
        for (int i = 0; i < functionLine.length; i++) // percorre o codigo inteiro
        {
            int instAddr = Integer.parseInt(getMachineCodeAddress(functionLine[i]),16);
            addrsList.add(instAddr);
        }
        
        Collections.sort(addrsList); 
        
        for (int i = 0; i < addrsList.size(); i++) // percorre o codigo inteiro
        {
            for (int j = 0; j < functionLine.length; j++)
            {
                int instructionAddr = Integer.parseInt(getMachineCodeAddress(functionLine[j]),16);

                if(instructionAddr == addrsList.get(i))
                    code = code.concat(functionLine[j] + "\n");
            }
            
        }
        
        return code;
    }
   
    public static String getHexa(String dumpLine) 
    {
        String hexa = new String();
        List<String> items = Util.getIt(dumpLine, DB.getDump().getInstructionFormat(), "XXXXXXXX", DB.getDump().BASE);
      
        if(!items.isEmpty())
            hexa = items.get(0);
       
        
        return hexa;
    }
        
    public List<String> getLabelAddresses() {
        return labelAddresses;
    }

    public List<String> getLabelNames() {
        return labelNames;
    }

    public List<Integer> getBasicBlockStarts() {
        return basicBlockStarts;
    }

    public List<Integer> getBasicBlockEnds() {
        return basicBlockEnds;
    }
    
    public List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }
    
    // creates the BBs
    private void createBasicBlocks() {
        
        for (int i = 0; i < basicBlockStarts.size(); i++)
        {
            BasicBlock basicBlock = new BasicBlock(i, basicBlockStarts.get(i), basicBlockEnds.get(i));
            basicBlocks.add(basicBlock);
        }
 
    
    }

    // insert their NS signatures (others are not important in this class)
    private void assignNSSignature(String inFunctionCode) {
        
        String[] functionLine = inFunctionCode.split("\n");
     
        int hexa = 0;
        int newHexa;
        String hexaWord = new String();
        int highHexa; // divides the hexa word in 2 leser words and XORs them, this way avoiding the use of a long variable.
        int lowHexa;
        
        for (int i = 0; i < basicBlocks.size(); i++)
        {
            hexa = 0;
            
            for (int j = basicBlocks.get(i).getPosStart(); j < basicBlocks.get(i).getPosEnd(); j++ )
            {    
                
                if(!getHexa(functionLine[j]).isEmpty())
                {/*
                  *Divides the hexa word in 2 leser words and XORs them, 
                    this way avoiding the use of a long variable. The use of a 
                    long variable would comprimise the 
                  */
                    hexaWord = getHexa(functionLine[j]);
                    highHexa = Integer.parseInt(hexaWord.substring(0,hexaWord.length()/2), 16);
                    lowHexa = Integer.parseInt(hexaWord.substring(hexaWord.length()/2, hexaWord.length()), 16);
                    newHexa = highHexa ^ lowHexa;
                    hexa = hexa ^ newHexa;
                } 
                else
                    hexa = hexa ^ 0;
            }
            
            basicBlocks.get(i).getHetaNs().setSign((int) hexa);    
        }   
    }   
}