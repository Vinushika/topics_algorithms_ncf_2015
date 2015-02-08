package a_huffman;

import java.util.Arrays;

public class TaqQuoteCompressor 
{

	private static String[] compressFileNames   = {"time-exchange.cmp","security.cmp","bid-price-size.cmp","ask-price.cmp","quote-cond.cmp","spaces.cmp","quote-codes.cmp","seq-number.cmp","misc-indexes.cmp","newlines.cmp"};
	//this String[] is used to make the file names for the compression of a zipped file, and for decompressing from our format.
	private static int[]    compressByteAmounts = {10,16,18,18,1,4,2,16,11,2};
	//this one has the number of bytes we're eating for each of the files in compressFileNames
	//first 10 bytes are time plus the exchange symbol
	//next 16 bytes are the security symbol
	//next 18 bytes are the bid price and bid size
	//next 18 bytes are ask price and ask size
	//next 1 byte is quote conditions
	//next 4 bytes are empty space always according to the spec - this will save us a lot!
	//next 2 bytes are all the letters describing which exchange is asking and buying
	//next 16 bytes are the sequence number
	//last 13 bytes are miscellaneous info and a bunch of spaces, plus 2 bytes worth of newline - it may be worth chunking up newline further as the newline char is ASCII 12, which only requires 4 bits.

	private static void compress(String outdir, String path,String outfile) throws Exception{
		//outdir is the directory to which we're reading/writing
		//path is the path to the NORMAL ZIP FILE with taqmaster20131218
		//outfile is the filename for the compressed file
		//to compress, we need one DecompressStream, and a series of CompressStreams
		//the filenames are declared in the arrays above this function, as are the number of bytes needed for each chunk
		byte[] buffer = new byte[128];
		//first, let's open up the file we want to compress more
		DecompressStream  inputStreamDecode = new DecompressStream(outdir,path,"taqmaster20131218");
		//now we have the file we're looking into, so let's make CompressStreams for the fields
		CompressStream[] used_streams = new CompressStream[compressFileNames.length];
		try{
			for(int i = 0; i < compressFileNames.length;i++){
				used_streams[i] = new CompressStream(outdir,outfile+"_"+compressFileNames[i],compressFileNames[i]); //we have to write multiple ZIP to preserve efficiency, since you can't write to ZipEntry
			}
			//now iterate through the records
			int len = inputStreamDecode.readRecord(buffer, 0,98); //each record is 98 bytes, so read 98
			while(len > 0){
				int current_offset = 0; //start at 0, increase by each of the byte amounts in the array
				for(int j = 0;j<used_streams.length;j++){
					used_streams[j].writeRecord(buffer, current_offset, compressByteAmounts[j]); //write to the file stream
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
		CompressStream outputStreamDecode = new   CompressStream(outdir,extractedFile,"taqmaster20131218");
		//do the exact same thing, but instead of opening a [] of CompressStreams, open up a [] of DecompressStreams
		DecompressStream[] used_streams   = new DecompressStream[compressFileNames.length];
		try{
			for(int i = 0; i < compressFileNames.length;i++){
				used_streams[i] = new DecompressStream(outdir,path+"_"+compressFileNames[i],compressFileNames[i]); //open each of the files we used for compression
			}
			//now iterate
			int len = used_streams[0].readRecord(buffer, 0, compressByteAmounts[0]); //read the first record from the first file
			//they all have the same number of records, but they do NOT each have the same length. Thus the while loop below
			//should still stop in time
			while(len > 0){
				//we can use len to keep track of this, however I'll set it to a different variable so we don't get confused
				int bytesReadIn = len;
				for(int j = 1;j<compressFileNames.length;j++){
					//go through each of the files we compressed
					bytesReadIn += used_streams[j].readRecord(buffer, bytesReadIn, compressByteAmounts[j]);
				}
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
		//DecompressStream  inputStreamDecode = new DecompressStream(outdir,                         args[0],"taqmaster20131218");
		//CompressStream   outputStreamEncode = new   CompressStream(outdir,"EQY_US_ALL_BBO_20131218_fields","taqmaster20131218");
		//nnot needed for us
		DecompressStream  inputStreamCheck  = null;
		DecompressStream inputStreamDecode  = null;
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
				inputStreamDecode = new DecompressStream(outdir,                         args[0],"taqmaster20131218");
				inputStreamCheck  = new DecompressStream(outdir,"EQY_US_ALL_BBO_20131218_restored","taqmaster20131218");

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
				                  inputStreamDecode = new DecompressStream(outdir,                         args[0],"taqmaster20131218");
				CompressStream   outputStreamEncode = new   CompressStream(outdir,"EQY_US_ALL_BBO_20131218_normalzip","taqmaster20131218");
				int len = inputStreamDecode.readRecord(buffer1, 0, 98); // 
	            while (len > 0) { //  
	                assert(len == 98); // Should be 98 every time until it is 0
	        	    outputStreamEncode     .writeRecord(buffer1, 0, 98);
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
				inputStreamCheck  .closeResources();
			}

		}

		long endTime  = System.nanoTime();
		long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
		long durSec   =  duration/1000000000L;
		long durMilli = (duration-durSec*1000000000L)/1000000;
		System.out.println("This "+args[2]+" operation took "+duration+" nanoseconds. ("+durSec+"."+durMilli+" seconds)");
	}
}