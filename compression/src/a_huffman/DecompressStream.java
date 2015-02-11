package a_huffman;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DecompressStream {
    String            zipName; 
    ZipInputStream   inStream  = null;
    int          iCountRecord  =    0;

    DecompressStream(String inDir, String zipName, String embeddedFilename ) {
        try {
            this.zipName  = zipName;
            inStream = new ZipInputStream(new BufferedInputStream( new FileInputStream(inDir + "/"+zipName+".zip")));
            ZipEntry entry;
            while((entry = inStream.getNextEntry())!=null)
            {
                if(entry.getName().equals(embeddedFilename)) break;
            }
            assert(entry != null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    int readRecord(byte[] bytes, int offset, int nBytes) {
        int bytesReadIn = 0;
        try {
            bytesReadIn          = inStream.read(bytes, offset            , nBytes            );
            if (    bytesReadIn >=      0) { // it is -1 when at the end of a file
                if (bytesReadIn  < nBytes) {
                    bytesReadIn += inStream.read(bytes, offset+bytesReadIn, nBytes-bytesReadIn);
                }
                iCountRecord++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytesReadIn;
    }
    
    void closeResources() {
        if(     inStream   !=null) {
            try {
                inStream   .close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
       System.out.println("File: "+zipName+" ; # of records: "+iCountRecord); //make it easier to read times
    }

}
