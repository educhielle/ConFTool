/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.util;

import cft.config.DB;
import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gennaro
 */
public class HetaSign 
{
    private final int signatureLength = DB.getWordSize() / 2;
    private int higherSignSize;
    /*
    The lower and the higher parts of the signature are represented as integer lists
    (acctually only of 0's and 1's). That way we can easily set and modify their sizes.
    */
    private List<Integer> lowerSign;  
    private List<Integer> higherSign;

    
    
   public HetaSign()
   {
        higherSignSize = 0;
        lowerSign = new ArrayList<Integer>();
        higherSign = new ArrayList<Integer>();
    }
   
   public int getSignatureLength()
   {
       return signatureLength;
   }

   public int getHigherSignSize(){
       return higherSignSize;
   }
   
   public int getLowerSignSize(){
       return (signatureLength - higherSignSize);
   }
   
   public void setHigherSignSize(int i){
       higherSignSize = i;
   }
   
   // returns the lower part of the signature
    public int getLowerSign(){
       String joined = Joiner.on("").join(lowerSign);
       return Integer.parseInt(joined, 2);
    }

    // returns the higher part of the signature
    public int getHigherSign(){
        
       String joined = Joiner.on("").join(higherSign);
       return Integer.parseInt(joined, 2);
        
    }
    
    // returns the list of the lower sign (0's and 1's)
    public List<Integer> getLowerSignList(){
        return lowerSign;
    }

    // returns the list of the higher sign (0's and 1's)
    public List<Integer> getHigherSignList(){
        return higherSign;
    }
    
    // sets the lower part of the signature
    public void setLowerSign(int i){
         
        lowerSign.clear();

        String newList = Integer.toBinaryString(i); // passes the integer to a binary string
        List<String> listStr = new ArrayList<String>();
        
        for (int k = 0; k < newList.length(); k++)
            listStr.add(k, newList.substring(k, k+1)); // adds each bit to the list of bits
        
        List<Integer> list = new ArrayList<Integer>();
       
        for(String s : listStr) list.add(Integer.parseInt(s)); // adds each bit to the integer list

        while (list.size() < signatureLength - higherSignSize) // fills the remaining bits w/ zero
            list.add(0, 0);
        
        lowerSign.addAll(list);
      
        
    }

    public void setHigherSign(int i){
        
        higherSign.clear();

        String newList = Integer.toBinaryString(i); // passes the integer to a binary string
        
        List<String> listStr = new ArrayList<String>();
        
        for (int k = 0; k < newList.length(); k++)
            listStr.add(k, newList.substring(k, k+1)); // adds each bit to the list of bits
        
        List<Integer> list = new ArrayList<Integer>();
        for(String s : listStr) list.add(Integer.parseInt(s)); // adds each bit to the integer list

        
        while (list.size() < higherSignSize) // fills the remaining bits w/ zero
            list.add(0, 0);
        
        higherSign.addAll(list);
    }

    public void setLowerSign(List<Integer> list){
        
        lowerSign = list;
        
    }
    
    public void setHigherSign(List<Integer> list){
        
        higherSign = list;
        
    }

    // sets the whole signature (both higher and lower parts)
    public void setSign(int i){
       
        lowerSign.clear();
        higherSign.clear();
        
        String newList = Integer.toBinaryString(i);        
        List<String> listStr = new ArrayList<String>();
        
        //first, fulfills the highersign list.
        for (int k = 0; k < newList.length(); k++)
        {
            
            if (k == higherSignSize) break;  
            listStr.add(k, newList.substring(k, k+1));
          
        }
            
        List<Integer> higherlist = new ArrayList<Integer>();
       
        for(String s1 : listStr) higherlist.add(Integer.parseInt(s1));

        while (higherlist.size() < higherSignSize)
            higherlist.add(0, 0);
        
        higherSign.addAll(higherlist);
        
        //then, then lowersign list.
        listStr.clear();
        int j = 0;
        for (int k = higherSignSize; k < newList.length(); k++)
        {       
            listStr.add(j, newList.substring(k, k+1));
            j++;
        }
            
        List<Integer> lowerlist = new ArrayList<Integer>();
       
        for(String s2 : listStr) lowerlist.add(Integer.parseInt(s2));

        while (lowerlist.size() < signatureLength - higherSignSize)
            lowerlist.add(0, 0);
        
        lowerSign.addAll(lowerlist);
        
        
    }
    

    
    public int getSign(){
                
        List<Integer> signature = new ArrayList<Integer>();
        signature.addAll(higherSign);
        signature.addAll(lowerSign);
        
        String joined = Joiner.on("").join(signature);
        
        if (joined.isEmpty())
            return 0;
        else
            return (int) Long.parseLong(joined, 2);
        
    }

    // sets a bit of the signature as 1
    public void setBit(int bit, int valor){
       
        List<Integer> signature = new ArrayList<Integer>();
        signature.addAll(higherSign);
        signature.addAll(lowerSign);
        
        signature.set(bit, valor);
        
    }

    // returns the value (0 or 1) of a bit of the signature
    public int getBit(int bit){
       
        List<Integer> signature = new ArrayList<Integer>();
        signature.addAll(higherSign);
        signature.addAll(lowerSign);
        
        return signature.get(bit);
        
    }

    // returns if the bit of the signature is being used or not (as a boolean)
    public boolean isUsed(int bit){
     
        List<Integer> signature = new ArrayList<Integer>();
        signature.addAll(higherSign);
        signature.addAll(lowerSign);
        
        if (signature.get(bit) == 1)
            return true;
        else
            return false;
    }
}
