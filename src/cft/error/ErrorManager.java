/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cft.error;

/**
 *
 * @author Angelo
 */
public class ErrorManager {

    public static final int CONFIGFILENOTFOUND = 1;
    public static final int INVALIDCONFIGFILE = 2;
    public static final int INVALIDREGISTER = 3;
    public static final int INVALIDNUMBEROFARGS = 4;
    public static final int INPUTFILENOTFOUND = 5;
    public static final int INVALIDGROUPCODE = 6;
    public static final int FUNCTIONNOTFOUND = 7;

    public static void error(int type, String info) {
        switch (type) {
            
            case CONFIGFILENOTFOUND:
               System.out.println("Error - Configuration file not found! " + info);
               System.exit(-1);
            break;

            case INVALIDCONFIGFILE:
               System.out.println("Error - Invalid configuration file! " + info);
               System.exit(-1);
            break;

            case INVALIDREGISTER:
                System.out.println("Error - Invalid Register! " + info);
                System.exit(-1);
            break;

            case INVALIDNUMBEROFARGS:
                System.out.println("Error - Invalid number of args! Input code filename and function to be extracted must be defined. " + info);
                System.exit(-1);
            break;

            case INPUTFILENOTFOUND:
                System.out.println("Error - Input code file not found! " + info);
                System.exit(-1);
            break;

            case INVALIDGROUPCODE:
                System.out.println("Error - Invalid capturing group code! " + info);
                System.exit(-1);
            break;

            case FUNCTIONNOTFOUND:
                System.out.println("Error - Desired function was not found! " + info);
                System.exit(-1);
            break;
        }
    }  
}
