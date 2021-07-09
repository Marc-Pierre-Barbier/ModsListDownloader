package downloader;

import java.io.PrintStream;

public class Log {
	private Log() {}
	private static String staticLine = "";
	private static PrintStream lastStream = System.out;

	public static void e(String who, String msg) {
		printNonStatic(String.valueOf(who) + "  :  " + msg, System.err);
	}
	
	public static void i(String who, String msg) {
		if(Main.verbose) printNonStatic(String.valueOf(who) + "  :  " + msg, System.out);
	}

	private static void printNonStatic(String line, PrintStream stream) {
		//erase last stream since we used \r
		lastStream.print("");
		stream.println(line);
		stream.print(staticLine + "                 \r");
		lastStream = stream;
	}

	public static void setStaticPrint(String staticString) {
		staticLine = staticString;
		System.out.print(staticString + "                    \r");
	}
}
