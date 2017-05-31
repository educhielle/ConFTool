/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cft.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Eduardo
 */
public class Util
{
    public static final long BITS_WORD_LENGTH = 32;
    public static final long BYTES_WORD_LENGTH = 4;
    public static final long HEX_WORD_LENGTH = 8;
    public static final long HEX_BASIS = 16;
    public static final long BIN_BASIS = 2;
    public static final long DEC_BASIS = 10;
    
    /*
     * Conversão de Base
     */

    public static String conversion(String n, long basisIn, long basisOut)
    {
        /* basisIn to dec */
        long dec = 0;
        for (int i = 0; i < n.length(); i++)
        {
            dec += (long)(Math.pow(basisIn, n.length() - 1 - i)) * digitValue(n.charAt(i));
        }

        /* dec to basisOut */
        String nOut = new String();
        long r;
        do
        {
            r = dec % basisOut;
            dec /= basisOut;
            nOut = toDigit(r) + nOut;
        } while (dec > 0);

        return nOut;
    }

    public static String conversion(String n, long basisIn, long basisOut, long length)
    {
        return format(conversion(n, basisIn, basisOut), length);
    }

    public static long binToDec(String bin)
    {
        long dec = 0;
        for (int i = 0; i < bin.length(); i++)
        {
            dec += (long)(Math.pow(2,bin.length() - i - 1)) * digitValue(bin.charAt(i));
        }

        return dec;
    }

    public static String decToBin(long dec)
    {
        String bin = new String();
        long r;
        do
        {
            r = dec % 2;
            dec /= 2;
            bin = toDigit(r) + bin;
        } while (dec > 0);

        return bin;
    }

    public static long hexToDec(String hex)
    {
        long dec = 0;
        for (int i = 0; i < hex.length(); i++)
        {
            dec += (long)(Math.pow(16,hex.length() - i - 1)) * digitValue(hex.charAt(i));
        }

        return dec;
    }

    public static String decToHex(long dec)
    {
        String hex = new String();
        long r;
        do
        {
            r = dec % 16;
            dec /= 16;
            hex = toDigit(r) + hex;
        } while (dec > 0);

        return hex;
    }

    public static String binToHex(String bin)
    {
        return (decToHex(binToDec(bin)));
    }

    public static String hexToBin(String hex)
    {
        return (decToBin(hexToDec(hex)));
    }

    public static String format(String str, long length)
    {
        for (int i = str.length(); i < length; i++)
        {
            str = "0" + str;
        }

        return str;
    }

    public static long digitValue(char d)
    {
        if ((d >= '0') && (d <= '9')) return (d - '0');
        else if ((d >= 'A') && (d <= 'Z')) return (d - 'A' + 10);
        else if ((d >= 'a') && (d <= 'z')) return (d - 'a' + 10);
        else return 0;
    }

    public static char toDigit(long n)
    {
        if ((n >= 0) && (n <= 9)) return (char)('0' + n);
        else if (n > 9) return (char)('A' - 10 + n);
        else return '0';
    }

    public static long twoComplement(String bin)
    {
        return (-1)*(binToDec(complement(bin))+1);
    }

    public static String complement(String bin)
    {
        String complement = new String();

        for (int i = 0; i < bin.length(); i++)
        {
            complement += (bin.charAt(i)=='1'?'0':'1');
        }

        return complement;
    }
    
    /*
     * Manipulação de Listas
     */
    
    public static List complement(List list1, List list2)
    {
        List complement = new ArrayList(list1);
        
        for (int i = complement.size()-1; i >= 0; i--)
        {
            if (list2.contains(complement.get(i))) complement.remove(i);
        }
        
        return complement;
    }

    public static boolean containsAtLeastOne(List list, List sublist)
    {
        for (int i = 0; i < sublist.size(); i++)
        {
            if (list.contains(sublist.get(i))) return true;
        }

        return false;
    }
    
    public static List createList(Object obj)
    {
        List list = new ArrayList();
        list.add(obj);
        return list;
    }
    
    public static List createList(Object obj1, Object obj2)
    {
        List list = new ArrayList();
        list.add(obj1);
        list.add(obj2);
        return list;
    }

    public static int getMaxPosition(List<Integer> list)
    {
        if (list.isEmpty()) return -1;

        int pos = 0;
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) > list.get(pos)) pos = i;
        }

        return pos;
    }
    
    public static List intercalate(List list1, List list2)
    {
        List intercalate = new ArrayList();
        
        for (int i = 0; (i < list1.size()) || (i < list2.size()); i++)
        {
            if (i < list1.size()) intercalate.add(list1.get(i));
            if (i < list2.size()) intercalate.add(list2.get(i));
        }
        
        return intercalate;
    }
    
    public static List intersection(List list1, List list2)
    {
        List intersect = new ArrayList<String>();
        
        for (int i = 0; i < list1.size(); i++)
        {
            if (list1.indexOf(list2.get(i)) != -1) intersect.add(list1.get(i));
        }
        
        return intersect;
    }
    
    public static List<List> splitIntercalate(List list1, List list2, int numberOfElements)
    {
        List<List> list = new ArrayList<List>();
        List intercalate = intercalate(list1, list2);
        
        for (int i = 0; i < (intercalate.size() / numberOfElements); i++)
        {
            list.add(new ArrayList());
            for (int j = 0; j < numberOfElements; j++)
            {
                list.get(i).add(intercalate.get(numberOfElements*i+j));
            }
        }
        
        return list;
    }
    
    public static List symmetricDifference(List listIn1, List listIn2)
    {
        List list1 = new ArrayList(listIn1);
        List list2 = new ArrayList(listIn2);
        
        for (int i = 0; i < list1.size(); i++)
        {
            int pos;
            boolean found = false;
            while ((pos = list2.indexOf(list1.get(i))) != -1)
            {
                list2.remove(pos);
                found = true;
            }
            if (found)
            {
                list1.remove(i);
                i--;
            }
        }
        list1.addAll(list2);
        
        return list1;
    }
    
    /*
     * Tratamento de String
     */

    public static String formatToRegex(String line)
    {
        String str = new String();

        for (int i = 0; i < line.length(); i++)
        {
            if (isRegexSpecialCharacter(line.charAt(i)))
            {
                str += "\\";
            }
            str += line.charAt(i);
        }

        return str;
    }
    
    public static String getFileExtension(String fileName)
    {
        int pos = fileName.length()-1;
        for (int i = 0; i < fileName.length(); i++)
        {
            if (fileName.charAt(i) == '.') pos = i;
        }
        
        return fileName.substring(pos+1, fileName.length());
    }
    
    public static String getFileTitle(String fileName)
    {
        int pos = fileName.length();
        for (int i = 0; i < fileName.length(); i++)
        {
            if (fileName.charAt(i) == '.') pos = i;
        }
        
        return fileName.substring(0, pos);
    }

    public static boolean isRegexSpecialCharacter(char c)
    {
        //Verificar caracteres especiais das expressões regulares do java
        if ((c == '(') || (c == ')') || (c == '$') || (c == '\\') || (c == '.') || (c == '-')
         || (c == '^') || (c == '+') || (c == '*') || (c == '?') || (c == '[') || (c == ']')
         || (c == '{') || (c == '}'))
        {
            return true;
        }

        return false;
    }
    
    public static int numberOfOccurrences(String line, char item)
    {
        int occurrences = 0;
        
        for (int pos = 0; pos < line.length(); pos++)
        {
            if (line.charAt(pos) == item) occurrences++;
        }
        
        return occurrences;
    }
    
    public static String replaceAll(String line, String item, String replacement)
    {
        while(line.contains(item))
        {
            line = replaceFirst(line, item, replacement);
        }
        
        return line;
    }
    
    public static String replaceFirst(String line, String item, String replacement)
    {
        String str = new String();
        
        for (int i = 0, j = 0, mark = 0; i < line.length(); i++)
        {
            if (line.charAt(i) == item.charAt(j))
            {
                j++;
                if (j == item.length())
                {
                    str += replacement + line.substring(i+1);
                    break;
                }
            }
            else
            {
                str += line.substring(mark, i+1);
                mark = i + 1;
                j = 0;
            }
        }
        
        return str;
    }
    
    public static String removeExtraWhitespaces(String line)
    {
        String str = new String();
        line = line.trim();
        boolean whitespace = false;
        
        for (int i = 0; i < line.length(); i++)
        {
            if ((line.charAt(i) == ' ') || (line.charAt(i) == '\t')) whitespace = true;
            else
            {
                if (whitespace) str += " ";
                whitespace = false;
                str += line.charAt(i);
            }
        }
        
        return str;
    }
    
    public static String removeWhitespaces(String line)
    {
        String str = new String();
        
        for (int i = 0; i < line.length(); i++)
        {
            if ((line.charAt(i) != '\t') && (line.charAt(i) != ' '))
            {
                str += line.charAt(i);
            }
        }
        
        return str;
    }
    
    public static String removeWhitespacesFromEnd(String line)
    {
        int i;        
        for (i = line.length()-1; i >= 0; i--)
        {
            if ((line.charAt(i) != '\t') && (line.charAt(i) != ' '))
            {
                break;
            }
        }
        line = line.substring(0,i+1);
        
        return line;
    }
    
    public static String[] splitTrim(String line, String split)
    {
        String vector[] = line.split(split);

        for (int i = 0; i < vector.length; i++)
        {
            vector[i] = vector[i].trim();
        }
        
        return vector;
    }
       
    /*
     * Padrões
     */
    
    public static String getBefore(String line, String item)
    {
        Pattern pattern = Pattern.compile(item);
        Matcher matcher = pattern.matcher(line);
                
        if ((item.length() > 0) && matcher.find())
        {
            line = line.substring(0,matcher.start());
        }
        
        return line;
    }
    
    public static String getBetween(String line, String start, String end)
    {
        int posStart = line.indexOf(start);
        int posEnd = line.indexOf(end, (posStart>=0?posStart:0));
        String between = new String();
        
        if ((posStart >= 0) && (posEnd >= 0) && (posStart < line.length()))
        {
            between = line.substring(posStart+1, posEnd);
        }
        
        return between;
    }

    public static String generateCommand(String instruction, List<String> rd, List<String> rs, List<String> imm, List<String> offset, List<String> target, String format)
    {
        for (int i = 0; i < rd.size(); i++) format = replaceFirst(format, "rd", rd.get(i));
        for (int i = 0; i < rs.size(); i++) format = replaceFirst(format, "rs", rs.get(i));
        for (int i = 0; i < imm.size(); i++) format = replaceFirst(format, "imm", imm.get(i));
        for (int i = 0; i < offset.size(); i++) format = replaceFirst(format, "offset", offset.get(i));
        for (int i = 0; i < target.size(); i++) format = replaceFirst(format, "target", target.get(i));
        format = replaceFirst(format, "ins", instruction);
        
        return format;
    }
    
    public static List<String> getIt(String line, String format, String item, String[] base)
    {
        List list = new ArrayList<String>();
        line = line.trim();
        
        format = formatToRegex(format.trim());
        format = format.replaceAll(item, "(?:\\\\s*)(\\\\S+)(?:\\\\s*)");
        
        for (int i = 0; i < base.length; i++)
        {
            format = format.replaceAll(base[i], "(?:\\\\s*)(?:\\\\S+)(?:\\\\s*)");
        }
        format = removeExtraWhitespaces(format);
        format = format.replaceAll(" ", "(?:\\\\s+)");
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(line);
        
        if (matcher.matches())
        {
            for (int i = 0; i < matcher.groupCount(); i++)
            {
                list.add(matcher.group(i+1).trim());
            }
        }
        
        return list;
    }

    public static boolean isIt(String line, String format, String[] base)
    {
        line = line.trim();
        format = formatToRegex(format.trim());
        
        for (int i = 0; i < base.length; i++) 
        {
            format = format.replaceAll(base[i], "(?:\\\\s*)(?:\\\\S+)(?:\\\\s*)");
        }
        format = removeExtraWhitespaces(format);
        format = format.replaceAll(" ", "(?:\\\\s+)");
        
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(line);
                
        return matcher.matches();
    }
    
    /*public static void setIt(String format, String item)
    {
        Util.setIt(format, item, 0);
    }
    
    public static void setIt(String format, String item, int pos)
    {
        
    }*/
    
    /*
     * Arquivos
     */
    
    public static void concatFiles(List<String> filesNamesIn, String fileNameOut) throws IOException
    {
        BufferedReader in;
        BufferedWriter out = new BufferedWriter(new FileWriter(fileNameOut));
        
        for (int i = 0; i < filesNamesIn.size(); i++)
        {
            in = new BufferedReader(new FileReader(filesNamesIn.get(i)));
            for (String line; (line = in.readLine()) != null;)
            {
                out.write(line+"\n");
            }
            in.close();
        }
        out.close();
    }
    
    public static String findLineThatContains(String filename, String text) throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        
        for (String line; (line = in.readLine()) != null; )
        {
            if (line.contains(text)) return line;
        }
        
        return new String();
    }
    
    public static void fileRename(String currentName, String newName)
    {
        File existingFile = new File(newName);
        if (existingFile.exists()) existingFile.delete();
        File file = new File(currentName);
        file.renameTo(new File(newName));
    }
    
    public static void fileRemove(String fileName)
    {
        File file = new File(fileName);
        file.delete();
    }
    
    public static List<String> getCode(String fileName, String commentTag) throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(fileName));
        List<String> list = new ArrayList<String>();
        
        for (String line; (line = in.readLine()) != null; )
        {
            //Formatar linha
            line = Util.getBefore(line, commentTag);
            line = Util.removeWhitespacesFromEnd(line);
            
            //Adicionar linha
            list.add(line);
        }
        
        return list;
    }
    
    public static List<String> readFile(String filename) throws IOException
    {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        List<String> list = new ArrayList<String>();
        
        for (String line; (line = in.readLine()) != null; )
        {
            list.add(line);
        }
        
        return list;
    }
    
    public static void removeTempFiles()
    {
        File dir = new File("./");
        String format = ".*tmp\\d+\\.s";
        Pattern pattern = Pattern.compile(format);
        
        for (int pos = (dir.listFiles().length - 1); pos >= 0; pos--)
        {
            String fileName = dir.listFiles()[pos].toString();
            Matcher matcher = pattern.matcher(fileName);
            
            if (matcher.matches())
            {
                (new File(fileName)).delete();
            }
        }
    }
    
    public static void write(List list, String fileName) throws IOException
    {
        BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
        
        for (int i = 0; i < list.size(); i++)
        {
            out.write(String.valueOf(list.get(i)) + "\n");
        }
        
        out.close();
    }
}