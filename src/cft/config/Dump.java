/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cft.config;

import cft.error.ErrorManager;
import cft.util.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Angelo, Eduardo
 */
public class Dump {
    public final String BASE[] = {"a", "i", "X", "o"};
    private String configFilename = "./config/dump.ini";       // nome do arquivo de configuração
    private String labelFormat;
    private String instructionFormat;
    private String operandSeparator;
    private String commentaryFormat;
    private Vector<String[]> registers = new Vector<String[]>();     // registradores de máquina e seus equivalentes em assembly
    private Vector<String> jumpsToRegister = new Vector<String>();   // jumps com destino endereçado por registrador
    private Vector<String> jumpsToMemory = new Vector<String>();     // jumps com destino endereçado diretamemte
    private Vector<String> branches = new Vector<String>();
    private int shift;                                               // número de deslocamentos para branches e jumps
    private List<List> instructionsToReplace = new ArrayList<List>();

    // Constutor: inicializa todos os atributos
    public Dump(String configFilename) throws FileNotFoundException, IOException {
        this.configFilename = configFilename;
        labelFormat = readLabelFormat();
        instructionFormat = readInstructionFormat();
        operandSeparator = readOperandSeparator();
        commentaryFormat = readCommentaryFormat();
        registers = readRegisters();
        jumpsToRegister = readJumpsToRegister();
        System.out.println("Dump.JumpsToRegister: "+jumpsToRegister);
        jumpsToMemory = readJumpsToMem();
        System.out.println("Dump.JumpstoMemory: "+jumpsToMemory);
        branches = readBranches();
        System.out.println("Dump.Branches: "+branches);
        shift = readShift();
        instructionsToReplace = readInstructionsToReplace();
    }
    
    public Dump() throws FileNotFoundException, IOException
    {
        labelFormat = readLabelFormat();
        instructionFormat = readInstructionFormat();
        operandSeparator = readOperandSeparator();
        commentaryFormat = readCommentaryFormat();
        registers = readRegisters();
        jumpsToRegister = readJumpsToRegister();
        jumpsToMemory = readJumpsToMem();
        branches = readBranches();
        shift = readShift();
        instructionsToReplace = readInstructionsToReplace();
    }

    // Lê o formato de label do arquivo de configuração
    private String readLabelFormat() throws FileNotFoundException, IOException {
        String format;
        BufferedReader inConfig = getTagReader("[LABEL_FORMAT]");
        format = readLine(inConfig);
        inConfig.close();
        return format;
    }

    // Lê o formato de instrução do arquivo de configuração
    private String readInstructionFormat() throws FileNotFoundException, IOException {
        String format;
        BufferedReader inConfig = getTagReader("[INSTRUCTION_FORMAT]");
        format = readLine(inConfig);
        inConfig.close();
        return format;
    }
    
    private List<List> readInstructionsToReplace() throws IOException
    {
        List<List> list = new ArrayList<List>();
        BufferedReader inConfig = getTagReader("[INSTRUCTIONS_TO_REPLACE]");
        
        if (inConfig.ready())
        {
            String line = readLine(inConfig);
            while (line != null && !line.trim().equals(""))
            {
                list.add(Arrays.asList(Util.splitTrim(line.trim(), " ")));
                line = readLine(inConfig);
            }
        }
        
        return list;
    }    

    // Lê o separador de operandos do arquivo de configuração
    private String readOperandSeparator() throws FileNotFoundException, IOException {
        String separator;
        BufferedReader inConfig = getTagReader("[OPERAND_SEPARATOR]");
        separator = readLine(inConfig);
        inConfig.close();
        return separator;
    }

    // Lê o formato de comentários do arquivo de configuração
    private String readCommentaryFormat() throws FileNotFoundException, IOException {
        String format;
        BufferedReader inConfig = getTagReader("[COMMENTARY_FORMAT]");
        format = readLine(inConfig);
        inConfig.close();
        return format;
    }

    // Lê os registradores de máquina e seus equivalentes em assembly do arquivo de configuração
    private Vector<String[]> readRegisters() throws FileNotFoundException, IOException {
        Vector<String[]> regs = new Vector<String[]>();
        BufferedReader inConfig = getTagReader("[REGISTERS]");

        if (inConfig.ready()) {
           String line = readLine(inConfig);
           while(line != null && !line.equals("")) {   // lê todas as linhas do arquivo de configuração até o final do arquivo ou uma linha em branco
              regs.add(line.split(","));
              line = inConfig.readLine();   // não se pode usar readLine() pois final de arquivo não pode ser considerado erro.
           }
        }

        inConfig.close();
        return regs;
    }

    // Lê os jumps com destino endereçado por registrador do arquivo de configuração
    private Vector<String> readJumpsToRegister() throws IOException {
        Vector<String> jmps = new Vector<String>();
        BufferedReader inConfig = getTagReader("[JUMPS_TO_REGISTER]");

        if (inConfig.ready()) {
           String line = readLine(inConfig);
           String[] splittedJumps = line.split(",");
           for (int i = 0; i < splittedJumps.length; i++)
              jmps.add(splittedJumps[i]);
        }

        inConfig.close();
        return jmps;
    }

    // Lê os jumps com destino endereçado diretamente do arquivo de configuração
    private Vector<String> readJumpsToMem() throws IOException {
        Vector<String> jmps = new Vector<String>();
        BufferedReader inConfig = getTagReader("[JUMPS_TO_MEMORY]");

        if (inConfig.ready()) {
           String line = readLine(inConfig);
           String[] splittedJumps = line.split(",");
           for (int i = 0; i < splittedJumps.length; i++)
              jmps.add(splittedJumps[i]);
        }

        inConfig.close();
        return jmps;
    }

    // Lê os branches do arquivo de configuração
    private Vector<String> readBranches() throws IOException {
        Vector<String> bchs = new Vector<String>();
        BufferedReader inConfig = getTagReader("[BRANCHES]");

        if (inConfig.ready()) {
           String line = readLine(inConfig);
           String[] splittedJumps = line.split(",");
           for (int i = 0; i < splittedJumps.length; i++)
              bchs.add(splittedJumps[i]);
        }

        inConfig.close();
        return bchs;
    }

    // Lê o número de deslocamentos para branches e jumps do arquivo de configuração
    private int readShift() throws IOException {
        int shft = 0;
        BufferedReader inConfig = getTagReader("[SHIFT]");

        if (inConfig.ready()) {
           String line = readLine(inConfig);
           shft = Integer.parseInt(line);
           if (shft < 0)
              ErrorManager.error(ErrorManager.INVALIDCONFIGFILE, "Shift value must be positive!");
        }

        inConfig.close();
        return shft;
    }

    // Retorna um BufferedReader posicionado uma linha abaixo ao tag dado
    private BufferedReader getTagReader(String tag) throws FileNotFoundException, IOException {
        File configFile = new File(configFilename);
        
        if (!configFile.exists())
           ErrorManager.error(ErrorManager.CONFIGFILENOTFOUND, "");
        
        BufferedReader inConfig = new BufferedReader(new FileReader(configFile));

        if (inConfig.ready()) {
           String line;
           do {
              line = readLine(inConfig);
           } while(!line.equals(tag));
        }

        return inConfig;
    }

    // Retorna a linha lida pelo BufferedReader dado. Considera fim de arquivo um erro
    private String readLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();

        if (line == null) {
                 reader.close();
                 ErrorManager.error(ErrorManager.INVALIDCONFIGFILE, "");
         }

        return line.trim();
    }

    public String getLabelFormat() {
        return labelFormat;
    }

    public String getInstructionFormat() {
        return instructionFormat;
    }

    public String getOperandSeparator() {
        return operandSeparator;
    }

    public String getCommentaryFormat() {
        return commentaryFormat;
    }

    // Retorna o registrador assembly equivalente
    public String getEquivalentAssemblyRegister(String inRegister) {
        for (int i = 0; i < registers.size(); i++)
            if (registers.elementAt(i)[0].equals(inRegister))
               return registers.elementAt(i)[1];  // os elementos da segunda coluna do vetor são os registradores assembly

        ErrorManager.error(ErrorManager.INVALIDREGISTER, "");
        return null;
    }
    
    public String getEquivalentDumpRegister(String register)
    {
        for (int i = 0; i < registers.size(); i++)
        {
            if (registers.elementAt(i)[1].equals(register))
            {
                return registers.elementAt(i)[0];
            }
        }
        
        ErrorManager.error(ErrorManager.INVALIDREGISTER, "");
        return new String();
    }

    public int getShift() {
        return shift;
    }

    public List<List> getInstructionsToReplace()
    {
        return instructionsToReplace;
    }

    // Retorna se a String dada representa um registrador
    public boolean isRegister(String inRegister) {
        for (int i = 0; i < registers.size(); i++)
           if (registers.elementAt(i)[0].equals(inRegister) || registers.elementAt(i)[1].equals(inRegister))
              return true;

        return false;
    }

    // Retorna se a String dada representa um jump
    public boolean isJump(String inJump) {
        return isJumpToRegister(inJump) || isJumpToMemory(inJump);
    }

    // Retorna se a String dada representa um jump com destino endereçado por registrador
    public boolean isJumpToRegister(String inJump) {
        if (jumpsToRegister.contains(inJump))
            return true;

        return false;
    }

    // Retorna se a String dada representa um jump com destino endereçado diretamente
    public boolean isJumpToMemory(String inJump) {
        if (jumpsToMemory.contains(inJump))
            return true;
        
        return false;
    }

    // Retorna se a String dada representa um branch
    public boolean isBranch(String inBranch) {
        if (branches.contains(inBranch))
            return true;

        return false;
    }    
}
