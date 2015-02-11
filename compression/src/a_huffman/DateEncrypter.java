package a_huffman;

import java.io.IOException;
import java.nio.ByteBuffer;

/*
 Expected Use:
 Encrypting:
 create DateEncrypter. de = DateEncrypter(inputStreamDecode)
  write the first date. whatEverYouDoToWrite(dh.getFirstDate())
  then when the time comes, write the next date. whatEverYouDoToWrite(dh.getNextDate())

 */
public class DateEncrypter {
	public Integer firstDate = null; 
	public Integer numEncrypted = 0;
	byte[] buffer1 = new byte[9];
	byte[] dump = new byte[128];
	Integer currentDate = null;
	DecompressStream inputStreamDecode = null;
	int len;
    boolean once = true;
	CompressStream outputStreamEncode = null;

	public DateEncrypter (DecompressStream inputStreamDecode) throws IOException{
		len = inputStreamDecode.readRecord(buffer1, 0,9);
		if (len !=9){
			throw new IllegalArgumentException("Error: Input DecompressStream must be pointing to file of at least 9 bytes");
		} else{
			this.inputStreamDecode = inputStreamDecode;
            firstDate = Integer.parseInt(new String(buffer1, "UTF-8"));

			//firstDate = ByteBuffer.wrap(buffer1).getInt();
			inputStreamDecode.readRecord(dump, 0, 89);
		}
	}
	
	public Integer getFirstDate(){
		return firstDate;
	}

	public Integer getNextDate() throws IOException {
		//len = inputStreamDecode.readRecord(buffer1,numEncrypted*98,9);
        len = inputStreamDecode.readRecord(buffer1,0,9);

        if (len != 9){
			throw new IllegalArgumentException("Error: Input DecompressStream must be pointing to file of at least 9 bytes");
		} else{
            currentDate = Integer.parseInt(new String(buffer1, "UTF-8")) - firstDate;
            if (once){
             System.out.println("currentDate");
            System.out.println(currentDate);
            once = false;
            }
			//currentDate = ByteBuffer.wrap(buffer1).getInt() - firstDate;
			inputStreamDecode.readRecord(dump, 0, 89);
			return currentDate;
		}

	}
}
