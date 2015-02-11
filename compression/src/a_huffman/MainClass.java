package a_huffman;

public class MainClass {

	public static void main(String[] args) {
		TaqQuoteCompressor reader = new TaqQuoteCompressor();
		String outfile = "EQY_US_ALL_BBO_20131218_100000"; // default filename
		if(args != null){
			if(args.length > 1){
				System.out.println("Too many arguments. This only takes the filename to read from.");
				System.exit(1);
			}else if(args.length == 1){
					outfile = args[0]; //only takes 1 arg
			}
			//if 0 args, take the default filename

		}
		System.out.println("Outfile is: " + outfile);
		String [] arguments = new String[3];
		arguments[0] = outfile;
		arguments[1] = "./";
		arguments[2] = "compress";
		//first we compress to show our smaller file
		read(reader, arguments);
		arguments[0] = "EQY_US_ALL_BBO_20131218_restored";
		arguments[2] = "extract";
		//then decompress to show we can restore the original file
		read(reader, arguments);
		arguments[0] = outfile;
		arguments[2] = "check";
		read(reader,arguments);
		arguments[0] = outfile;
		arguments[2] = "check_zip"; //this performs regular zip to check how much longer we take doing the fancier procedure
		read(reader,arguments);
	}
	
	public static void read(TaqQuoteCompressor reader, String[] arguments){
		try{
			reader.read(arguments);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
