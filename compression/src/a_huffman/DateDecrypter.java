package a_huffman;

import java.nio.ByteBuffer;

/*
Expected Use:
Decrypting:
create DateDecrypter. dd = new DateDecrypter(datesStream)
 write the first date. whatEverYouDoToWrite(dd.getFirstDate())
 then when the time comes, write the next date. whatEverYouDoToWrite(dh.getNextDate())
*/

public class DateDecrypter {
	private Integer firstDate = null; 
	private Integer numDecrypted = 0;
	private byte[] buffer1 = new byte[9];
	private byte[] buffer2 = new byte[4];

	private int len;
	private DecompressStream datesStream = null;
	
	public DateDecrypter (DecompressStream datesStream){
//		len = datesStream.readRecord(buffer1, 0,9);
//		if (len !=9){
//			throw new IllegalArgumentException("Error: Input DecompressStream must be pointing to file of at least 9 bytes");
//		} else{
			this.datesStream = datesStream;
			firstDate = ByteBuffer.wrap(buffer1).getInt(); 
//		}
	}
	public Integer getFirstDate(){
		return firstDate;
	}
	
	public Integer getNextDate(){
		// +3 for the first date
		len = datesStream.readRecord(buffer2, numDecrypted+3,1);
		if (len!=1){
			throw new IllegalArgumentException("Error: Could not read in another byte.");
		}
		else{
			numDecrypted+=1;
			return firstDate + ByteBuffer.wrap(buffer2).getInt(); 
		}
	}
	
}
