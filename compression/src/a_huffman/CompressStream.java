package a_huffman;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CompressStream { // I did not "extends OutputStrem" because I did not want to implement "write(int)"
    String            zipName; 
    ZipOutputStream    outAll  = null;
    ZipEntry         entryAll  = null; 
    int          iCountRecord  =    0;

    CompressStream(String outdir, String zipName, String embeddedFilename ) {
        try {
            this.zipName  = zipName;
            entryAll = new ZipEntry(embeddedFilename);
            outAll   = new ZipOutputStream(new BufferedOutputStream( new FileOutputStream(outdir + "/"+zipName+".zip")));
            outAll.putNextEntry(entryAll); 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    void writeRecord(byte[] bytes, int offset, int nBytes) {
        try {
            outAll.write(bytes, offset, nBytes);
            iCountRecord++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    void closeResources() {
        if(     outAll   !=null) {
            try {
                outAll   .close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
