
package a_huffman;
import java.util.Arrays;
import java.nio.ByteBuffer; //for byte-ops
import java.nio.charset.Charset; //for ASCII conversion from bytes
import java.nio.BufferOverflowException;
public class TaqQuoteCompressor
{
	private static String[] compressFileNames = {"time-exchange.cmp","security.cmp","bid-price-int.cmp","bid-price-float.cmp",
		"bid-size.cmp","ask-price-int.cmp","ask-price-float.cmp","ask-size.cmp","quote-cond.cmp","spaces.cmp","quote-codes.cmp","seq-number.cmp","misc-indexes.cmp","newlines.cmp"};
	//this String[] is used to make the file names for the compression of a zipped file, and for decompressing from our format.
	private static int[] compressByteAmounts = {9,17,7,4,7,7,4,7,1,4,2,16,11,2};
	//this one has the number of bytes we're eating for each of the files in compressFileNames
	//first 9 bytes are time plus the exchange symbol
	//next 17 bytes are the security symbol
	//next 18 bytes are the bid price and bid size
	// - 7 bytes bid price (int part)
	// - 4 bytes bid price (float part)
	// - 7 bytes bid size (you can't buy fractional shares)
	//next 18 bytes are ask price and ask size
	// - 7 bytes ask price (int part)
	// - 4 bytes ask price (float part)
	// - 7 bytes ask size (you can't buy fractional shares)
	//next 1 byte is quote conditions
	//next 4 bytes are empty space always according to the spec - this will save us a lot!
	//next 2 bytes are all the letters describing which exchange is asking and buying
	//next 16 bytes are the sequence number
	//last 13 bytes are miscellaneous info and a bunch of spaces, plus 2 bytes worth of newline - it may be worth chunking up newline further as the newline char is ASCII 12, which only requires 4 bits.
	
	
	private static byte[] intToByteArray(int i,int numBytes){
		ByteBuffer dbuf = ByteBuffer.allocate(numBytes);
		dbuf.putInt(i);
		return dbuf.array();
	}
	
	private static void compress(String outdir, String path,String outfile) throws Exception{
		//outdir is the directory to which we're reading/writing
		//path is the path to the NORMAL ZIP FILE with taqmaster20131218
		//outfile is the filename for the compressed file
		//to compress, we need one DecompressStream, and a series of CompressStreams
		//the filenames are declared in the arrays above this function, as are the number of bytes needed for each chunk
		byte[] buffer = new byte[128];
		//first, let's open up the file we want to compress more
		DecompressStream inputStreamDecode = new DecompressStream(outdir,path,"taqmaster20131218");
		//now we have the file we're looking into, so let's make CompressStreams for the fields
		CompressStream[] used_streams = new CompressStream[compressFileNames.length];
		int count = 0;
		try{
			for(int i = 0; i < compressFileNames.length;i++){
				used_streams[i] = new CompressStream(outdir,outfile+"_"+compressFileNames[i],compressFileNames[i]); //we have to write multiple ZIP to preserve efficiency, since you can't write to ZipEntry
			}
			//now iterate through the records
			int len = inputStreamDecode.readRecord(buffer, 0,98); //each record is 98 bytes, so read 98
//			DateEncrypter de = new DateEncrypter(inputStreamDecode);
//			boolean writtenFirstDate = false;
			while(len > 0){
				int current_offset = 0; //start at 0, increase by each of the byte amounts in the array
				for(int j = 0;j<used_streams.length;j++){
					//here we branch out depending on whether we're in ask-price or bid-price
					//I don't like hardcoding indices into my program, but it sure makes it faster when we iterate several thousand times.
					boolean special_write = false; //make sure we don't double-write when we're doing special stuff to the data first
					
//					if (j==0){
//						// write the next date difference
//						if (writtenFirstDate){
//							//+3 for the first date
//							used_streams[0].writeRecord(intToByteArray(de.getNextDate(),1),de.getNumEncrypted()+3,1);
//						}else{
//							// write the first date
//							used_streams[0].writeRecord(intToByteArray(de.getFirstDate(),3),0,1);
//							writtenFirstDate = true;
//						}
//					}
					if(j> 1 && j < 8){
						//right here is where we do things. Special things.
						//we know our current offset, and how much compressByteAmounts tell us to read, so let's take those in and make an int
						//out of the bytes
						//first read in the digits
						int iDigits = 0;
						for (int digits_read=0;digits_read < compressByteAmounts[j];digits_read++){
							iDigits = 10 *iDigits + (buffer[current_offset+digits_read]-48); //read in a certain amount of bytes
						}
						count++;
						if(count == 29){
							System.out.println(iDigits);
						}
						//ok, theoretically now we have our int. So we know for a fact that the highest possible
						//number we can have here is 9,999,999 < 16,777,216 = 2^24 = 3 bytes
						//And for the 4-byte fields we have
						///9,999 < 2^16 = 2 bytes. Yes we can use less than 2 bytes, but we won't concern ourselves
						//with that level of optimization. So allocate either 3 or 4
						int amount_to_write = compressByteAmounts[j] == 7 ? 3 : 2;
						byte[] stuff_to_write = new byte[amount_to_write];
						for(int k =0;k<amount_to_write;k++){
							//we have to write byte-by-byte into an array we allocate instead of doing things more easily
							//I wanted to use ByteBuffer.putInt, but that writes 4 byte chunks, which makes our buffer overflow
							//this code makes sure that the int value is 0 everywhere but at the last two positions
							//then casts that to a byte, because at that point we're fine
							//technically this is little-endian! What fun.
							stuff_to_write[k] = (byte)((iDigits & 0x000000ff << 8*k) >> 8*k);
							if(count == 29){
								System.out.println("stuff_to_write[" + k + "] is: " +stuff_to_write[k]);
							}
						}
						used_streams[j].writeRecord(stuff_to_write,0,amount_to_write);//write the entire "new buffer" to the file
						special_write = true; //make sure we don't double-write
					}
					if(!(special_write)){
						used_streams[j].writeRecord(buffer, current_offset, compressByteAmounts[j]); //write to the file stream
					}
					current_offset += compressByteAmounts[j]; //
				}
				len = inputStreamDecode.readRecord(buffer,0,98);
			}
		}
		finally{
			//make sure we close all our streams
			inputStreamDecode.closeResources();
			for(int k=0;k<used_streams.length;k++){
				if(used_streams[k] != null){
					used_streams[k].closeResources();
				}
			}
		}
	}
	private static void extract(String outdir,String path,String extractedFile) throws Exception{
		//same as compress, but the other way around - use ONLY for our compression format!
		//outdir is the directory to which we're writing
		//path is the path to the file that was COMPRESSED BY THIS PROGRAM
		//extractedFile is the path to a NORMAL ZIP FILE that we're writing with CompressStream
		byte[] buffer = new byte[128];
		int count=0;
		CompressStream outputStreamDecode = new CompressStream(outdir,extractedFile,"taqmaster20131218");
		//do the exact same thing, but instead of opening a [] of CompressStreams, open up a [] of DecompressStreams
		DecompressStream[] used_streams = new DecompressStream[compressFileNames.length];
		try{
			for(int i = 0; i < compressFileNames.length;i++){
				used_streams[i] = new DecompressStream(outdir,path+"_"+compressFileNames[i],compressFileNames[i]); //open each of the files we used for compression
			}
			//now iterate
			int len = used_streams[0].readRecord(buffer, 0, compressByteAmounts[0]); //read the first record from the first file
			//they all have the same number of records, but they do NOT each have the same length. Thus the while loop below
			//should still stop in time
//			DateEncrypter de = new DateEncrypter(inputStreamDecode);
//			boolean writtenFirstDate = false;
			while(len > 0){
				//we can use len to keep track of this, however I'll set it to a different variable so we don't get confused
				int bytesReadIn = len;
				for(int j = 1;j<compressFileNames.length;j++){
					//go through each of the files we compressed
					boolean special_read = false;
					
//					if (j==0){
//						// write the next date difference
//						if (writtenFirstDate){
//							//+3 for the first date
//							used_streams[0].writeRecord(intToByteArray(de.getNextDate(),1),de.getNumEncrypted()+3,1);
//						}else{
//							// write the first date
//							used_streams[0].writeRecord(intToByteArray(de.getFirstDate(),3),0,1);
//							writtenFirstDate = true;
//						}
//					}
					if(j > 1 && j < 8){
						//so we aren't reading in the same amounts! We have to be careful
						int bytes_to_read = (compressByteAmounts[j] == 7) ? 3 : 2;
						byte[] coded_number = new byte[bytes_to_read]; //we need to make sure we can read this correctly
						used_streams[j].readRecord(coded_number, 0, bytes_to_read); //read the amount we need to decompress - note we don't return to anywhere because these records are not at the end
						//therefore we don't need a bytesReadIn == 0 check
						int number_read_in = 0;
						for(int l=0; l < bytes_to_read;l++){

							 number_read_in |= (((int)coded_number[l])&0xff) << (8*l);//shift by one byte so that we're in the right spot
							if(count == 28){
								System.out.println("Read in at index " + l + " the byte " + coded_number[l]);
								System.out.println("number_read_in is: " + Integer.toBinaryString(number_read_in));
							}
							//we need to do this to avoid BufferUnderflowException from ByteBuffer because it's dumb
						}
						//System.out.println(number_read_in);
						// if(count < 100){
						// System.out.println(number_read_in);
						count++;
						// }
						if(count == 29){
							System.out.println(number_read_in);
							System.out.println("Number read in shifted a byte: " + (number_read_in >> 8));
						}
						//ok, now we have an int. So make it a string.
						String number_string = Integer.valueOf(number_read_in).toString();
						char[] number_chars = number_string.toCharArray();
						if(count == 29){
							System.out.println(number_chars);
						}
						int number_length = number_string.length();
						byte[] decompressed_bytes = new byte[compressByteAmounts[j]];
						for(int m=0;m<compressByteAmounts[j] - number_length;m++){ //middle argument should always be >= 0!!
							decompressed_bytes[m] = 48; //make it "0"
						}
						for(int n=compressByteAmounts[j] - number_length;n<compressByteAmounts[j];n++){
							decompressed_bytes[n] = (byte)number_chars[n - (compressByteAmounts[j] - number_length)];
							if(count == 29){
								System.out.println("Decompressed bytes " + n + " is: " + decompressed_bytes[n]);
								System.out.println("CBA[n] - n - 1 is:" + (compressByteAmounts[j] - n - 1));
							}
						}
						//the line above is complex, see comments below
						//now we have our bytes back in normal form, so let's shove them in to our buffer
						for(int k = 0; k < decompressed_bytes.length;k++){
							buffer[bytesReadIn+k] = decompressed_bytes[k]; //make sure you read in at the proper offset
						}
						bytesReadIn += compressByteAmounts[j]; //we read correctly! I don't think we absolutely need to do this, but I'm doing it for safety
						special_read = true;
					}
					if(!(special_read)){
						bytesReadIn += used_streams[j].readRecord(buffer, bytesReadIn, compressByteAmounts[j]); //make sure we don't double-read
					}
				}
				/*
				 * To convert back from a byte array of x (in our case 3 or 2) bytes
				 * byte[] decompressed_bytes = Integer.parseInt(ByteBuffer.wrap(bytes).getInt()).toString().getBytes(Charset.forName("US-ASCII"))
				 * toString makes string
				 * getBytes makes it into a byte array and we need to specify the charset so it doesn't go UTF-8 and blow up the amount of
				 * stuff we have in there because each UTF-8 char is 8 bytes and that's huge for our purposes
				 */
				//we're done reading in all the compressed files, so let's write the record
				outputStreamDecode.writeRecord(buffer,0,98); //write 98-byte record
				len = used_streams[0].readRecord(buffer, 0, compressByteAmounts[0]); //start over with the next record
			}
		}finally{
			//close everything
			outputStreamDecode.closeResources();
			for(int k = 0;k<used_streams.length;k++){
				used_streams[k].closeResources();
			}
		}
	}
	public void read(String args[]) throws Exception
	{
		if(args.length != 3)
		{
			System.err.println("zipreader zipfile outputdir");
			return;
		}
		// create a buffer to improve copy performance later.
		byte[] buffer1 = new byte[128];
		byte[] buffer2 = new byte[128];
		// open the zip file stream
		String outdir = args[1];
		// Start the timing
		long startTime = System.nanoTime();
		//TODO: Modify this to use the stuff at the top of the file for compression/decompression
		//DecompressStream inputStreamDecode = new DecompressStream(outdir, args[0],"taqmaster20131218");
		//CompressStream outputStreamEncode = new CompressStream(outdir,"EQY_US_ALL_BBO_20131218_fields","taqmaster20131218");
		//nnot needed for us
		DecompressStream inputStreamCheck = null;
		DecompressStream inputStreamDecode = null;
		try
		{
			if(args[2].equals("compress")){
				//call our compressor method
				System.out.println("Compressing...");
				compress(outdir,args[0],"EQY_US_ALL_BBO_20131218_fields");
			}else if(args[2].equals("extract")){
				System.out.println("Decompressing...");
				extract(outdir,"EQY_US_ALL_BBO_20131218_fields",args[0]);
			}else if(args[2].equals("check")){
				System.out.println("Opening files to check");
				System.out.println(args[0]);
				inputStreamDecode = new DecompressStream(outdir, args[0],"taqmaster20131218");
				inputStreamCheck = new DecompressStream(outdir,"EQY_US_ALL_BBO_20131218_restored","taqmaster20131218");
				int len1 = 1; //
				int len2;
				while (len1 > 0) { //
					len1 = inputStreamDecode.readRecord(buffer1, 0, 98); //
					len2 = inputStreamCheck .readRecord(buffer2, 0, 98); //
					assert(len1 == len2);
					assert(Arrays.equals(buffer1, buffer2));
				}
			}else if (args[2].equals("check_zip")){
				System.out.println("Performing regular zip...");
				//this just unzips the file, then zips it again
				inputStreamDecode = new DecompressStream(outdir, args[0],"taqmaster20131218");
				CompressStream outputStreamEncode = new CompressStream(outdir,"EQY_US_ALL_BBO_20131218_normalzip","taqmaster20131218");
				int len = inputStreamDecode.readRecord(buffer1, 0, 98); //
				while (len > 0) { //
					assert(len == 98); // Should be 98 every time until it is 0
					outputStreamEncode .writeRecord(buffer1, 0, 98);
					len = inputStreamDecode. readRecord(buffer1, 0, 98); //
				}
				outputStreamEncode.closeResources();
			}else{
				System.err.println(args[2]+" is not a function this program can perform");
				return;
			}
		}finally
		{
			// we must always close the zip file.
			//outputStreamEncode.closeResources();
			if(inputStreamDecode != null && inputStreamCheck != null){
				//no reason to close them if we never opened them, and we only open them to do the check
				inputStreamDecode .closeResources();
				inputStreamCheck .closeResources();
			}
		}
		long endTime = System.nanoTime();
		long duration = (endTime - startTime); //divide by 1000000 to get milliseconds.
		long durSec = duration/1000000000L;
		long durMilli = (duration-durSec*1000000000L)/1000000;
		System.out.println("This "+args[2]+" operation took "+duration+" nanoseconds. ("+durSec+"."+durMilli+" seconds)");
	}
}
