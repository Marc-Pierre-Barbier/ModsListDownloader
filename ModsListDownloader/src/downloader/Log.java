package downloader;

import java.io.PrintStream;

public class Log {
	private Log() {}
	private static String staticLine = "";
	private static PrintStream lastStream = System.out;

	public static void e(String who, String msg) {
		printNonStatic("[ERROR]" + who + "  :  " + msg, System.err);
	}

	public static void w(String who, String msg) {
		printNonStatic("[WARN]" + who + "  :  " + msg, System.out);
    }
	
	public static void i(String who, String msg) {
		if(Main.verbose) printNonStatic("[INFO]" + who + "  :  " + msg, System.out);
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
