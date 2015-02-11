package a_huffman;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class CompressStream { // I did not "extends OutputStrem" because I did not want to implement "write(int)"
    String            zipName; 
    // FileOutputStream   outHdr  = null;  // For student convenience, I removed the header from the files
    ZipOutputStream    outAll  = null;
    ZipEntry         entryAll  = null; 
    int          iCountRecord  =    0;

    CompressStream(String outdir, String zipName, String embeddedFilename ) {
        try {
            this.zipName  = zipName;
            entryAll = new ZipEntry(embeddedFilename);
            outAll   = new ZipOutputStream(new BufferedOutputStream( new FileOutputStream(outdir + "/"+zipName+".zip")));
            outAll.putNextEntry(entryAll); 
            // outHdr   = new FileOutputStream(outdir + "/"+zipName+"_header.txt"); 
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    void writeRecord(byte[] bytes, int offset, int nBytes) {
        try {
            //assert( nBytes == 98 ); // We are only doing 98 byte chunks
            outAll.write(bytes, offset, nBytes);
            iCountRecord++;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    void closeResources() {
        if(     outAll   !=null) {
            try {
                outAll   .close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
//        if(     outHdr   !=null) {
//            try {
//                outHdr   .close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
        //System.out.println("File: "+zipName+" ; # of records: "+iCountRecord);
    }
//     // For student convenience, I removed the header from the files
//    void writeHeader(byte[] bytes, int offset, int nBytes) {
//        try {
//            assert( nBytes == 98 ); // We are only doing 98 byte chunks
//            outHdr.write(bytes, offset, nBytes);
//            iCountRecord++;
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
}
