package cft.util;

/**
 *
 * @author Eduardo Chielle
 */
public class Instruction
{
    private String name;
    private String format;
    private String type;
    private String[] hiddenRD;
    private String[] hiddenRS;
    
    /*
     * Constructors
     */
    
    public Instruction(String name, String format, String type, String[] hiddenRD, String[] hiddenRS)
    {
        this.name = name;
        this.format = format;
        this.type = type;
        this.hiddenRD = hiddenRD;
        this.hiddenRS = hiddenRS;
    }
    
    public Instruction(String name, String format, String type)
    {
        this(name, format, type, new String[0], new String[0]);
    }
    
    public Instruction()
    {
        this("", "", "", new String[0], new String[0]);
    }
    
    /*
     * Getters
     */
    /*
    public boolean equals(Instruction instruction)
    {
        return (instruction.getName().equals(this.name) && instruction.getFormat().equals(this.format) && instruction.getType().equals(this.type));
    }
    */
    public String getFormat()
    {
        return format;
    }

    public String[] getHiddenRD() 
    {
        return hiddenRD;
    }

    public String[] getHiddenRS()
    {
        return hiddenRS;
    }
    
    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }
    
    /*
     * Setters
     */
    
    public void setFormat(String format)
    {
        this.format = format;
    }
    
    public void setHiddenRD(String[] hiddenRD)
    {
        this.hiddenRD = hiddenRD;
    }

    public void setHiddenRS(String[] hiddenRS)
    {
        this.hiddenRS = hiddenRS;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
}
