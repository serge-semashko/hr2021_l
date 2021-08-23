package dubna.walt.util;

import dubna.walt.SimpleQueryThread;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.util.Locale;

import javax.servlet.ServletInputStream;

/**
 *
 * @author serg
 */
public class FileContent extends Object {

    byte[] content = null;
    public ResourceManager rm = null;
    long fileSize = 0;
    long bytesRead = 0;
    String fileName = "";
    String tmpFileName = "";
    String filePath = "";
    String contentType = "";
    final String CT_KEY = "Content-Type:";

    /**
     *
     * @param content
     * @throws Exception
     */
    public FileContent(byte[] content) throws Exception {
        this.content = content;
        this.fileSize = content.length;
    }

    /**
     *
     * @param content
     * @param contentType
     * @throws Exception
     */
    public FileContent(byte[] content, String contentType) throws Exception {
        this.contentType = contentType;
        this.content = content;
        this.fileSize = content.length;
    }

    /**
     *
     * @param filePath
     * @param contentType
     * @throws Exception
     */
    public FileContent(String filePath, String contentType) throws Exception {
        fileName = FileContent.parseFilePath(filePath);
        System.out.print("********* FileContent.1: READING " + filePath + " CONTENT...");
        File f = new File(filePath);
        if (!f.exists() || !f.canRead()) {
            throw new Exception("Could not read file " + filePath);
        }

        FileInputStream fi = new FileInputStream(f);
        fileSize = (int) f.length();
        content = new byte[(int)fileSize];
        bytesRead = fi.read(content);
        fi.close();
        this.contentType = contentType;
        if (bytesRead != fileSize) {
            System.out.println("!!!!!!!!!! " + filePath + " length=" + fileSize + "; read:" + bytesRead);
            throw new Exception("filePath: length=" + fileSize + "; read:" + bytesRead);
        }
    }

    /**
     * DETECTION and PROTECTION FOR BIG FILE SIZES NEEDED !!!
     * Сейчас макс. размер файла ограничен максимальным значением int и памятью
     *
     * @param inpStream
     * @param filePath
     * @param contentType
     * @param maxSize - макс.размер файла. 
     * inpStream копируется во временный буфер из которого данные копируются в content[fileSize]
     */
    public FileContent(InputStream inpStream, String filePath, String contentType, int maxSize) {
        fileName = FileContent.parseFilePath(filePath);
        this.contentType = contentType;
        int bufLen = maxSize;
	  System.out.println("********* FileContent.2: READING " + filePath + " CONTENT... bufLen=" + bufLen);
//	  System.out.println("********* inpStream:" + inpStream);
        byte[] buf = new byte[bufLen];
        fileSize = 0;
        try {
            int bufPos = 0;
            int numBytes = 1;
            while (numBytes > 0) {
                while (bufPos < bufLen && numBytes > 0) {
                    numBytes = inpStream.read(buf, bufPos, bufLen - bufPos);
//				  System.out.println("++++++++++++++++++++++++++ numBytes=" + numBytes + ";");
                    if (numBytes > 0) {
                        fileSize += numBytes;
                        bufPos += numBytes;
                    }
                }
            System.out.println("++++++++++++ fileSize=" + fileSize + ";");
            content = new byte[(int)fileSize];
            System.arraycopy(buf, 0,  content, 0, (int)fileSize ); // Копировать ЛУЧШЕ ТАК (НЕ ПРОТЕСТИРОВАНО)
            }
/*          System.out.println("\n\r************************");
            System.out.println(fileSize + ":'" + new String(buf, 0, fileSize) + "'");
            System.out.println("************************");
*/
            buf = null;
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Чтение файла из ServletInputStream и запись во временный файл filePath + tmpFileName
     * Буфер содержимого content не создается!
     * Используется для загрузки больших файлов.
     * 
     * @param inpStream - входной поток
     * @param filePath - путь к временному файлу
     * @param fileName - оригинальное имя файла (не используется, только запоминается)
     * @param tmpFileName - имя временного файла
     * @param boundary - разделитель параметров во входном потоке
     * @param maxSize - максимальный допустимый размер файла. 
     */
    public FileContent(ServletInputStream inpStream, String filePath, String fileName, String tmpFileName, byte[] boundary, long maxSize)
    {        
        System.out.println("********* FileContent.3: COPY ServletInputStream: fileName="+ fileName + " => "  + filePath + " " + tmpFileName + "; maxSize=" + Fmt.ftm_delim(maxSize));
        this.filePath = filePath;
        this.fileName = fileName;
        this.tmpFileName = tmpFileName;
        int lb = boundary.length;
//        byte b[] = new byte[lb + 2];
//        b[0]='\r';
//        b[1]='\n';
//        System.arraycopy(boundary, 0,  b, 2, lb );
//        lb += 2;
        byte b[] = new byte[lb];
        System.arraycopy(boundary, 0,  b, 0, lb );
//        byte[] buf = new byte[lb];
        int bufSize = 1024 * 8 ;
        byte[] buf = new byte[bufSize + lb + 2];
//        System.out.println("b: '" + new String(b) + "', " + lb + "bytes");
        int r;

        try {       
            File f = null;
//            BufferedOutputStream lf = null;
            
            f = new File(sFilePath(filePath)); // + tmpFileName));
            if (!f.exists()) {
                if (!f.mkdirs()) {
                    throw new Exception("Could not create destination directory");
                }
            }
            FileOutputStream lf = new FileOutputStream(filePath + tmpFileName, false); 
//            lf = new BufferedOutputStream( new FileOutputStream(filePath + tmpFileName, false), 8*1024);  //8КБ
//            lf = new BufferedOutputStream( new FileOutputStream(filePath + tmpFileName, false), 512*1024*1024);  //512MB
            if (lf == null) {
                throw new Exception("Could not write output file");
            }

            int n = SimpleQueryThread.readLine(inpStream, buf, 0, maxSize);
            contentType = new String(buf, 0, n);
            if (contentType.indexOf(CT_KEY) == 0) {
                contentType = contentType.substring(CT_KEY.length()).trim();
            } else {
                contentType = "";
            }
            n += SimpleQueryThread.readLine(inpStream, buf, 0, maxSize);
            fileSize = 0;
//2088272 
            int boundBytesFound=0;
            int bufPos=0;
            while (fileSize < maxSize) {
                r = inpStream.read();
//                if(fileSize > 1220) {
////                if(boundBytesFound > 5  || fileSize > 2088214) {
//                    boolean c1 = ((byte) r == b[boundBytesFound]);
//                    boolean c2 = ((char) r == (char) b[boundBytesFound]);
//                    System.out.println("  " + fileSize  + " f:" + boundBytesFound + "/ char='" + (char) r + "' " + (char) b[boundBytesFound] + ": " + c1 + "/" + c2);
//                }
                if (r == -1) {
                    lf.write(buf, 0, bufPos - 2 - boundBytesFound);
                    break;
                }
                if((byte) r == b[boundBytesFound]){
                    if(bufPos >= bufSize){
                        lf.write(buf, 0, bufPos);
                        bufPos = 0;
                    }
                    buf[bufPos++] = (byte) r;
                    boundBytesFound++;
//                    buf[boundBytesFound++] = (byte) r;
                    if(boundBytesFound == lb){
                        System.out.println("********* Boundary found! File Size=" + fileSize);
                        if(bufPos > boundBytesFound + 2){
                            lf.write(buf, 0, bufPos - 2 - boundBytesFound);
                        }
                        break;
                    }
                } else {
                    if(bufPos >= bufSize){
                        lf.write(buf, 0, bufPos);
                        bufPos = 0;
                    }
                    buf[bufPos++] = (byte)r;
//                    if(boundBytesFound > 0)
//                        lf.write(buf, 0, boundBytesFound);
//                    
//                    lf.write(r);
                    fileSize += boundBytesFound + 1;
                    boundBytesFound = 0;
                }   
            }
            
            lf.close();                      
            buf = null;
            bytesRead = n + fileSize + lb;
            System.out.println("********* OUT FILE:" + filePath + tmpFileName + "; fileSize=" + Fmt.ftm_delim(fileSize) + " bytes; bytesRead=" + Fmt.ftm_delim(bytesRead));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    /**
     * Копирует inpStream в буфер содержимого content.
     * Временный буфер не создается.
     * Размер буфера content = maxSize (недозаполняется).
     * Реальный размер файла ставится в fileSize!
     * Полное кол-во прочитанных из inpStream байт вплоть до boundary ставится в bytesRead.
     * 
     * @param inpStream
     * @param filePath
     * @param boundary
     * @param maxSize
     */
    public FileContent(ServletInputStream inpStream, String filePath, byte[] boundary, long maxSize)
    {        
        fileName = FileContent.parseFilePath(filePath);
//        if (bufLen > maxSize) 
        int bufLen = (int)maxSize;
        System.out.println("********* FileContent.4: BUFFERING " + filePath + " CONTENT... bufLen=" + Fmt.ftm_delim(bufLen));
        byte[] buf = new byte[bufLen];
        fileSize = 0;
        //  byte[] bb = boundary.getBytes();
        int lb = boundary.length;
        int r;
        try {
//            int n = SimpleQueryThread.readLine(inpStream, buf, 0, maxSize);
//            contentType = new String(buf, 0, n);
            int n = SimpleQueryThread.readLine(inpStream, buf, 0, maxSize);
            System.out.println(n);
            contentType = new String(buf, 0, n);
            if (contentType.indexOf(CT_KEY) == 0) {
                contentType = contentType.substring(CT_KEY.length()).trim();
            } else {
                contentType = "";
            }
//            n += SimpleQueryThread.readLine(inpStream, buf, 0, maxSize);
            n += SimpleQueryThread.readLine(inpStream, buf, 0, maxSize);
            bytesRead = 0;

            while (bytesRead < bufLen - n) {
                r = inpStream.read();
                //      System.out.print((char) r + "/");
                if (r == -1) {
                    break;
                }
//                buf[(int)bytesRead++] = (byte) r;
                buf[(int)bytesRead++] = (byte) r;
                if (bytesRead >= lb) {
                    int i = lb - 1;
                    int j = (int)bytesRead - 1;
                    while (i >= 0) {
//                        if (boundary[i--] != buf[j--]) {
                        if (boundary[i--] != buf[j--]) {
                            break;
                        }
                    }
                    if (i <= 0) {
                        fileSize = bytesRead - lb - 2;  // отрезаем cr/lf  перед boundary
                        System.out.println("********* Boundary found! fileSize=" + fileSize);
                        break;
                    }
                }
            }
            bytesRead += n;
            content = new byte[(int)fileSize];
            System.arraycopy(buf, 0,  content, 0, (int)fileSize );
            buf = null;
//                System.out.println("********* fileSize=" + Fmt.ftm_delim(fileSize) + "; bytesRead=" + Fmt.ftm_delim(bytesRead));
//                System.out.println("************************");
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    /**
     *
     * @param filePath
     * @return
     */
    public static String parseFilePath(String filePath) {
        int i = filePath.lastIndexOf("\\");
        if (i < 0) {
            i = filePath.lastIndexOf("/");
        }
        return filePath.substring(i + 1);
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public byte[] getBytes() throws Exception {
        return content;
    }

    /**
     *
     * @param pieceLength
     * @return
     * @throws Exception
     */
    public int getNumPieces(int pieceLength) throws Exception {
        return (int)((getFileSize() - 1) / pieceLength + 1);
    }

    /**
     *
     * @param pieceNr
     * @param pieceLength
     * @return
     * @throws Exception
     */
    public byte[] getPiece(int pieceNr, int pieceLength) throws Exception {
        int startPos = pieceNr * pieceLength;
        int length = pieceLength;
        if (startPos + length > getFileSize()) {
            length = (int)getFileSize() - startPos;
        }
        byte[] a = new byte[length];
        System.arraycopy(content, startPos, a, 0, length);
        return a;
    }

    /**
     *
     * @param path
     * @param name
     * @throws Exception
     */
    public void storeToDisk(String path, String name) throws Exception {
        System.out.println( "==> FileContent.storeToDisk(): path=" + path + "; name=" + name + "; " + Fmt.ftm_delim(fileSize) + "bytes; ");
        File f = null;
        FileOutputStream lf = null;
        long tm = System.currentTimeMillis();
        f = new File(path);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new Exception("Could not create upload directory");
            }
        }
        if(content != null) {
            lf = new FileOutputStream(path + name, false);
            if (lf == null) {
                throw new Exception("Could not store file");
            }
            lf.write(content, 0, (int)fileSize);
            lf.close();
        } else {
//            FileContent.copyFile(filePath + tmpFileName, path, name);
            FileContent.moveFile(filePath + tmpFileName, path + name);
        }
        if(rm != null) {
            tm = System.currentTimeMillis() - tm;
            IOUtil.writeLogLn(3,"==> FileContent.storeToDisk(): path=" + path + "; name=" + name + "; " + Fmt.ftm_delim(fileSize) + "bytes; " + tm + "ms.", rm);
        }
        System.out.println( "==> FileContent.storeToDisk() DONE: " + tm + "ms.");
    }

    /**
     *
     * @param inp
     * @param path
     * @param name
     * @throws Exception
     */
    public static void storeToDisk(InputStream inp, String path, String name) throws Exception {
        File f = null;
        FileOutputStream lf = null;
        f = new File(path);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new Exception("Could not create upload directory");
            }
        }
        lf = new FileOutputStream(path + name, false);
        if (lf == null) {
            throw new Exception("Could not store file");
        }
        IOUtil.copyStream(inp, lf);
        lf.close();
    }

    /**
     *
     * @param srcFilePath
     * @return
     */
    public static long getFileSize(String srcFilePath) {
//        System.out.println("getFileSize: " + srcFilePath);
        File f = new File(srcFilePath);
        if (!f.isFile()) {
            System.out.println("getFileSize: " + srcFilePath + "; =-1 NOT FOUND!");
            return -1;
        }
        if (!f.canRead()) {
            System.out.println("getFileSize: " + srcFilePath + "; =-2 CAN NOT READ!");
            return -2;
        }
//        System.out.println("getFileSize: " + srcFilePath + "; size=" + f.length());
        return f.length();
    }

    /**
     *
     * @param srcPath
     * @return
     */
    public static String sFilePath(String srcPath) {
        return (srcPath.replace('/', File.separatorChar)).replace('\\', File.separatorChar);
    }

    /**
     *
     * @param srcFilePath
     * @param destPath
     * @param destFileName
     * @throws Exception
     */
    public static void copyFile(String srcFilePath, String destPath, String destFileName) throws Exception {
        System.out.println("+++ COPY FILE: " + srcFilePath + " ==> " + destPath + " filename=" + destFileName);
        File f = null;
        FileOutputStream lf = null;
        f = new File(sFilePath(destPath));
        if (!f.exists()) {
            if (!f.mkdirs()) {
                throw new Exception("Could not create destination directory");
            }
        }
        lf = new FileOutputStream(destPath + destFileName, false);
        if (lf == null) {
            throw new Exception("Could not write output file");
        }
        FileContent.copyFileData(srcFilePath, lf);
        System.out.println("... COPY FILE - OK! ");
    }

    /**
 * 
 * @param srcFilePath
 * @param destFilePath
 * @throws Exception 
 */
    public static void copyFile(String srcFilePath, String destFilePath) throws Exception {
        String src = sFilePath(srcFilePath.trim());
        String dest = sFilePath(destFilePath.trim());
        String srcPath = src;
        String destPath = dest;
        String destFileName = dest;
//    System.out.println("+++ COPY FILE: "+ src + " ==> "+ dest);
//    System.out.println("+++ File.separator: '"+ File.separator + "'");
        int i = destPath.lastIndexOf(File.separator);
        if (i > 0) {
            destPath = dest.substring(0, i + 1);
            destFileName = dest.substring(i + 1);
        } else {
            i = srcPath.lastIndexOf(File.separator);
            destPath = srcPath.substring(0, i + 1);
        }
//   System.out.println("+++ COPY FILE: "+ src + " ==> "+ destPath + "; filename=" + destFileName);

        FileContent.copyFile(srcPath, destPath, destFileName);
    }

/**
 * 
 * @param srcFilePath
 * @param destFilePath
 * @throws Exception 
 */
    public static void moveFile(String srcFilePath, String destFilePath) throws Exception {
        String src = sFilePath(srcFilePath.trim());
        String dest = sFilePath(destFilePath.trim());
       System.out.println("+++ MOVE FILE: "+ src + " ==> "+ dest + "; ");
        
        File f = new File(dest);
        if (!f.exists()) {
          if (!f.mkdirs()) {
            throw new Exception("Could not create destination directory");
          }
        }
        Path s = FileSystems.getDefault().getPath(src);
        Path d = FileSystems.getDefault().getPath(dest);
        Files.move(s, d, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     *
     * @param filePath
     * @param out
     * @throws Exception
     * https://docs.oracle.com/javase/7/docs/api/java/io/FileInputStream.html#read(byte[])
     */
    public static void copyFileData(String filePath, OutputStream out) throws Exception {
        System.out.println("+++ copyFileData: '" + filePath);
        FileInputStream lf = new FileInputStream(sFilePath(filePath));
//        int maxLen = 500000000;  // Limit - 500MB
        long maxLen = 5000000000L;  // Limit - 5GB
        int bufLen = 1000000;
        byte[] buf = new byte[bufLen];
        int numBytes = 1;
        long totNumBytes = 0;
//        int avail=lf.available();
        while (numBytes > 0 
//               && avail > 0 
             && totNumBytes<maxLen
        ) {
//            numBytes = lf.read(buf, 0, bufLen);
            numBytes = lf.read(buf);
//            System.out.println("++++++++ available=" + avail + "; numBytes=" + numBytes + "; totNumBytes=" + totNumBytes);
            if (numBytes > 0) {
                totNumBytes += numBytes;
                out.write(buf, 0, numBytes);
            }
//            avail=lf.available();
        }
        out.flush();
        lf.close();
        System.out.println("======== " + totNumBytes + " bytes copied");
    }

    /**
     *
     * @param filePath
     * @throws Exception
     */
    public static void deleteFile(String filePath) throws Exception {
        (new File(sFilePath(filePath))).delete();
    }

    /**
     *
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public static String getFileType(String fileName) {
        int i = fileName.lastIndexOf(".");
        if (i < 0 || i == fileName.length()) {
            return "";
        }
        return fileName.substring(i + 1);
    }

    /**
     *
     * @return
     */
    public String getFileType() {
        return FileContent.getFileType(fileName);
    }

    /**
     *
     * @return
     */
    public String getContentType() {
        return contentType;
    }

    /**
     *
     * @return
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     *
     * @return
     */
    public long getBytesRead() {
        return bytesRead;
    }

}
