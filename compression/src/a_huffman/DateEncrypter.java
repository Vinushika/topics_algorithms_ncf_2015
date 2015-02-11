package a_huffman;

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
	Integer currentDate = null;
	DecompressStream inputStreamDecode = null;
	int len;
	CompressStream outputStreamEncode = null;

	public DateEncrypter (DecompressStream inputStreamDecode){
		len = inputStreamDecode.readRecord(buffer1, 0,9);
		if (len !=9){
			throw new IllegalArgumentException("Error: Input DecompressStream must be pointing to file of at least 9 bytes");
		} else{
			this.inputStreamDecode = inputStreamDecode;
			firstDate = ByteBuffer.wrap(buffer1).getInt(); 
		}
	}
	
	public Integer getFirstDate(){
		return firstDate;
	}

	public Integer getNextDate(){
		len = inputStreamDecode.readRecord(buffer1,numEncrypted*98,9);
		if (len != 9){
			throw new IllegalArgumentException("Error: Input DecompressStream must be pointing to file of at least 9 bytes");
		} else{
			currentDate = ByteBuffer.wrap(buffer1).getInt() - firstDate;
			return currentDate;
		}

	}
}
