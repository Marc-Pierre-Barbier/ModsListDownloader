package downloader;

public class Log {
	private Log() {}

	public static void e(String who, String msg) {
		System.out.println(String.valueOf(who) + "  :  " + msg);
	}
	
	public static void i(String who, String msg) {
		if(Main.verbose)System.out.println(String.valueOf(who) + "  :  " + msg);
	}
}
