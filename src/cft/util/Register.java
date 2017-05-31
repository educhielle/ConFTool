/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.util;

/**
 *
 * @author Eduardo
 */
public class Register 
{
    private String name;
    private boolean readable;
    private boolean writable;
    private String type;
    
    /*
     * Constructors
     */
    
    public Register(String name, boolean readable, boolean writable, String type)
    {
        this.name = name;
        this.readable = readable;
        this.writable = writable;
        this.type = type;
    }
    
    public Register()
    {
        this("", false, false, "");
    }
    
    /*
     * Getters
     */
    
    public String getName()
    {
        return name;
    }

    public String getType()
    {
        return type;
    }

    public boolean isReadable()
    {
        return readable;
    }

    public boolean isWritable()
    {
        return writable;
    }
    
    
    /*
     * Setters
     */

    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setReadable(boolean readable)
    {
        this.readable = readable;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }

    public void setWritable(boolean writable)
    {
        this.writable = writable;
    }
}
