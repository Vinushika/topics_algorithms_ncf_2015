package a_huffman;

import java.nio.ByteBuffer;

/*
 Expected Use:
 Encrypting:
 create DateEncrypter. de = new DateEncrypter(inputStreamDecode)
  write the first date. whatEverYouDoToWrite(de.getFirstDate())
  then when the time comes, write the next date. whatEverYouDoToWrite(d.getNextDate())
 */

public class DateEncrypter {
	private Integer firstDate = null; 
	private Integer numEncrypted = 0;
	private byte[] buffer1 = new byte[9];
	private Integer currentDate = null;
	private DecompressStream inputStreamDecode = null;
	private int len;
	private CompressStream outputStreamEncode = null;

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
	
	public int getNumEncrypted(){
		return numEncrypted;
	}

	public Integer getNextDate(){
		len = inputStreamDecode.readRecord(buffer1,numEncrypted*98,9);
		if (len != 9){
			throw new IllegalArgumentException("Error: Input DecompressStream must be pointing to file of at least 9 bytes");
		} else{
			currentDate = ByteBuffer.wrap(buffer1).getInt() - firstDate;
			numEncrypted+=1;
			return currentDate;
		}

	}
}
