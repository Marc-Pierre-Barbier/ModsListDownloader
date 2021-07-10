package downloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

public class Main {	

	public static boolean verbose;
	public static Integer threadNb;
    public static String mcVersion;

	public static void main(String[] args) {
		mcVersion = "1.12.2";

		checkModsDirectory();
		new DatabaseVersionManager().updateIfNeeded();
		interpretArgs(args);
		//no threading option called => only one thread will be used
		if( threadNb == null)threadNb = 1;
		Log.i("Main", "Running on " + threadNb + " thread");
		new ModUpdater().update();
	}

	public static void checkModsDirectory() {
		File modDir = new File("mods");
		if(!modDir.exists()) {
			try {
				Files.createDirectories(Paths.get("mods"));
			} catch (IOException e) {
				Log.e("Main","could not create folder");
				System.exit(0);
			}
		}
	}

	private static void interpretArgs(String[] args) {
		Iterator<String> it = Arrays.asList(args).iterator();
		final int cores = Runtime.getRuntime().availableProcessors();

		while (it.hasNext()) {
			String cmd = it.next();
			switch (cmd) {
				case "-t":
					Log.w("main", "Warning using -t option can lead to corruption during download if you have a lot of cores or a slow internet");

					if (threadNb != null) {
						Log.e("main", "error conflicting arguments --thread and -t");
						System.exit(1);
					}

					if (cores <= 0) {
						Log.i("main","java think you have no cpu core... sooo lets go for 2");
						threadNb = Integer.valueOf(2);
						break;
					} 
					Log.i("main", "running on " + cores + " thread");
					threadNb = Integer.valueOf(cores);
					break;

				case "-v":
					verbose = true;
					break;
					
				case "--thread":
					if (threadNb != null) {
						Log.e("main", "error conflicting arguments --thread and -t");
						System.exit(1);
					} 
					if (!it.hasNext()) {
						Log.e("main", "arguments invalid please refer to --help");
						System.exit(1);
					} 
					String threadNumber = it.next();
					if (threadNumber.matches("[0-9]*")) {
						threadNb = Integer.valueOf(Integer.parseInt(threadNumber));
						break;
					} 
					Log.e("main","arguments invalid please refer to --help");
					System.exit(1);
					break;

				case "--help":
					Log.i("main", "use --help to see this help\nuse -v to see verbose\nuse --threads to specify the number of threads\nuse -t to set the number of thread automaticaly");
					System.exit(0);
					break;
				default:
					Log.e("main", "unknown args " + cmd + "\nsee --help for help");
					System.exit(1);
			} 
		} 
	}
}
