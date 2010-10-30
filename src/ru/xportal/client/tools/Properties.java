
package ru.xportal.client.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import javax.microedition.midlet.MIDlet;

/**
 * Облегченный класс для работы с файлом свойств.
 * Автоматически загружает свойства из указанного файла. Для инициализации испоьзуются методы:
 * {@link #setApplicationClass(MIDlet) setApplicationClass},
 * и {@link #setApplicationClass(MIDlet) setPropertiesFile}.
 * @author Airat Khasianov
 */
public class Properties {
    private final static int[] SPACE_CHARS = {' ', '\t'};
    private final static int[] LINE_BREAK_CHARS = { '\r', '\n', ';'};
    private final static int[] COMMENT_CHARS = {'#'};
    private final static int[] DELIMITER_CHARS = {'=', ':'};
    private final static int DEFAULT_BUFFER_SIZE = 32;
    private Hashtable properties = null;
    private MIDlet app = null;
    private String propertiesFile = null;
    private StringBuffer commonBuffer = new StringBuffer(DEFAULT_BUFFER_SIZE);

 /**
  * Единственный конструктор
  * @param midlet - мидлет, использующий свойства,
  * {@link #javax.microedition.midlet.MIDlet MIDlet};
  * @param fileName - имя файла конфигурации
  * {@link String String}
  */
    public Properties(MIDlet midlet, String fileName){
         app = midlet;
         propertiesFile = fileName;
    };

 /**
  * Возвращает значение мидлета, использующего конфигурационный файл
  * @return midlet, {@link javax.microedition.midlet.MIDlet MIDlet}
  */
    public MIDlet getApplicationClass(){
        return app;
    }

 /**
 * Возвращает имя конфигурационного файла
  * @return
  */
    public String getPropertiesFile(){
        return propertiesFile;
    }

 /**
 * Возвращает значение свойства по значению ключа
 * @param name - имя требуемого свойства,
 * {@link #String String}.
  * @return {@link String String}
  * @throws PropertyException
 */
    public String getProperty(String name) throws PropertyException{
        lazyInitProperties();
        Object value = properties.get(name);
        if (value == null){
            throw new PropertyException();
        }
        return value.toString();
    }

 /**
 * Возвращает коллекцию пар свойств в формате (ключ, значение)
 * @return {@link java.util.Hashtable Hashtable}
 */
    public Hashtable getPropertiesHashtable(){
        lazyInitProperties();
        return properties;
    }

    private void loadProperties() throws UnsupportedEncodingException, IOException, PropertyException {
        InputStream is = (app == null?MIDlet.class:app.getClass()).getResourceAsStream(propertiesFile);
        if (is == null){
            throw new PropertyException();
        }
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        String name;
        String value;
        int chars;
        while ((chars = isr.read()) != -1){
            chars = removeComments(isr, chars);
            chars = skipNewLines(isr, chars);
            chars = readName(isr, chars);
            name = commonBuffer.toString();
            chars = skipDelimiters(isr, chars);
            chars = readValue(isr, chars);
            value = commonBuffer.toString();
            if(name.length()>0&&value.length()>0){
                properties.put(name, value);
            }
        }
        isr.close();
    }

    private static int removeComments(InputStreamReader isr, int chars) throws IOException{
        while (isComment(chars)){
            chars = skipLine(isr, chars);
            chars = skipNewLines(isr, chars);
        }
        return chars;
    }

    private static boolean otherChar(int chars, int[] skips) {
        boolean t = true;
        int i=0;
        while(i<skips.length&&t){
            t = skips[i++] != chars;
        }
        return t;
    }

    private static void crop(StringBuffer buf) {
        int i = buf.length();
        while (--i>0 && !otherChar(buf.charAt(i), SPACE_CHARS)){
            buf.deleteCharAt(i);
        }
    }

   private static int skipChars(InputStreamReader r, int[] skipChars, boolean positive, int chars) throws IOException {
        boolean t;
        while(chars !=-1&&(!(t = otherChar(chars, skipChars))&&positive||t&&!positive)){
            chars = r.read();
        }
        return chars;
    }

    private static int skipLine(InputStreamReader r, int chars) throws IOException {
        //System.out.println("Skipping comment");
        return skipChars(r, LINE_BREAK_CHARS, false, chars);
    }

    private static int skipNewLines(InputStreamReader r, int chars) throws IOException {
        //System.out.println("Skipping newlines");
        return skipChars(r, LINE_BREAK_CHARS, true, chars);
    }

    private static int skipSpaces(InputStreamReader r, int chars) throws IOException {
        //System.out.println("Skipping Spaces");
        return skipChars(r, SPACE_CHARS, true, chars);
    }

    private static int skipDelimiters(InputStreamReader r, int chars) throws IOException {
        //System.out.println("Skipping Delimiters");
        return skipChars(r, DELIMITER_CHARS, true, chars);
    }

    private static boolean isLineBreak(int chars) {
        return !otherChar(chars, LINE_BREAK_CHARS);
    }

    private static boolean isComment(int chars) {
        return !otherChar(chars, COMMENT_CHARS);
    }

    private static boolean isDelimiter(int chars) {
        return !otherChar(chars, DELIMITER_CHARS);
    }

    private void lazyInitProperties() {
        if (properties == null) {
            try {
                properties = new Hashtable(2);
                loadProperties();
            } catch (UnsupportedEncodingException ex) {
                Logger.log("Properties load error (unicode is not supported): "+ex.getMessage());
            } catch (IOException ex) {
                Logger.log("Properties load error: "+ex.getMessage());
            } catch (PropertyException ex){
                Logger.log("Properties load error: "+ex.getMessage());
            }
        }
    }

    private static void clearBuffer(StringBuffer buffer) {
        buffer.delete(0, buffer.length());
    }

    private int readName(InputStreamReader r, int chars) throws IOException {
        //System.out.println("Reading name");
        clearBuffer(commonBuffer);
        chars = skipSpaces(r, chars);
        //System.out.println("Done skipping Spaces");
        while (chars !=-1 && !isDelimiter(chars)&&!isLineBreak(chars)){
            commonBuffer.append((char)chars);
            chars = r.read();
        }
        crop(commonBuffer);
        return chars;
    }

    private int readValue(InputStreamReader r, int chars) throws IOException {
        //System.out.println("Reading value");
        clearBuffer(commonBuffer);
        chars = skipSpaces(r, chars);
        //System.out.println("Done skipping Spaces");
        while (chars !=-1 && !isLineBreak(chars)){
                commonBuffer.append((char)chars);
                chars = r.read();
        }
        crop(commonBuffer);
        return chars;
    }
}
