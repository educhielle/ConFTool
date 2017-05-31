/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft;

import cft.config.DB;
import cft.util.Global;
import cft.util.Util;
import java.io.IOException;

/**
 *
 * @author Eduardo
 */
public class CFT
{
    public static String filenameIn, filenameFinal;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException
    {
        Util.removeTempFiles();
        
        System.out.println("args (length: "+args.length+")\n{");
        for (int i = 0; i < args.length; i++)
        {
            System.out.println("\t["+i+"]: "+args[i]);
        }
        System.out.println("}\n");
                
        /* Carregar configuração */
        cft.config.Loader.loadConfig(args);
        filenameIn = cft.config.DB.getAssemblyFilename();
        filenameFinal = cft.config.DB.getAssemblyFilenameOut();
        
        DB.showInstructions();
        
        /* Formatar arquivo */
        String filenameOut = Global.nextName();
        cft.verif.Format.format(filenameIn, filenameOut);
        
        /* Verificar código e arquitetura */
        filenameIn = filenameOut;
        cft.verif.Checker.verifyCode(filenameIn);
        
        /* Gerar código assembly */
        filenameOut = Global.nextName();
        cft.asmgen.AssemblyGenerator.generateAll(filenameIn, filenameOut);
        filenameIn = filenameOut;
        
        /* Aplicar técnicas de proteção */
        filenameOut = Global.nextName();
        cft.techniques.Manager.applyTechniques(filenameIn, filenameOut);
        
        /* Renomear arquivo para nome final */
        Util.fileRename(filenameOut, filenameFinal);
        Util.removeTempFiles();
    }
}
