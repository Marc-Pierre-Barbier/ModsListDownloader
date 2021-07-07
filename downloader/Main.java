package downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
	private static File f = null;
	
	private static boolean verbose = false;
	
	private static boolean checkingonly = false;
	
	public static final AtomicInteger changes = new AtomicInteger(0);
	
	private static Integer threadNb = null;
	
	public static void main(String[] args) throws FileNotFoundException, IOException, URISyntaxException {
		try {
			Files.createDirectories(Paths.get("mods", new String[0]), (FileAttribute<?>[])new FileAttribute[0]);
		} catch (IOException e) {
			Log.e("HTTPHelper", "could not create folder");
			System.exit(0);
		} 
		interpretArgs(args);
		if (f == null)
		f = new File("modpack.txt"); 
		if (!f.exists()) {
			System.out.println("file not found");
			System.exit(1);
		} 
		IntegrityChecker updateManager = new IntegrityChecker();
		Database db = new Database();
		db.updateDatabase();
		BufferedReader in = new BufferedReader(new FileReader(f));
		if (threadNb == null)
		threadNb = Integer.valueOf(4); 
		List<Thread> threads = new ArrayList<>(threadNb.intValue());
		while (in.ready()) {
			String line = in.readLine();
			if (!isAComment(line)) {
				checkThreadState(threads);
				Thread newLineTerpreter = new NewLineInterpreter(line, updateManager, db);
				newLineTerpreter.start();
				threads.add(newLineTerpreter);
			} 
		} 
		checkThreadState(threads);
		in.close();
		while (threads.size() != 0) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException interruptedException) {}
			checkThreadState(threads);
		} 
		if (changes.get() != 0 && verbose)
		Log.i("Main", "changed " + changes.get() + " mod(s)"); 
		Log.i("Main", "finished");
	}
	
	private static void checkThreadState(List<Thread> threads) {
		do {
			if (threads.size() >= threadNb.intValue())
			try {
				Thread.sleep(100L);
			} catch (InterruptedException interruptedException) {} 
			Iterator<Thread> it = threads.iterator();
			while (it.hasNext()) {
				Thread t = it.next();
				if (!t.isAlive())
				it.remove(); 
			} 
		} while (threads.size() >= threadNb.intValue());
	}
	
	private static void interpretArgs(String[] args) {
		Iterator<String> it = Arrays.<String>asList(args).iterator();
		while (it.hasNext()) {
			String cmd = it.next();
			if (cmd.startsWith("-")) {
				String nbt;
				int cores;
				String str1;
				switch ((str1 = cmd).hashCode()) {
					case -1629068440:
					if (!str1.equals("--check"))
					break; 
					checkingonly = true;
					continue;
					case 1511:
					if (!str1.equals("-t"))
					break; 
					if (threadNb != null) {
						System.out.println("error conflicting arguments --thread and -t");
						System.exit(1);
					} 
					cores = Runtime.getRuntime().availableProcessors();
					if (cores <= 0) {
						System.out.println("java think you have no cpu core... sooo lets go for 4");
						threadNb = Integer.valueOf(4);
						continue;
					} 
					System.out.println("running on " + cores + " thread");
					threadNb = Integer.valueOf(cores);
					continue;
					case 1513:
					if (!str1.equals("-v"))
					break; 
					verbose = true;
					continue;
					case 48044553:
					if (!str1.equals("--threads"))
					break; 
					if (threadNb != null) {
						System.out.println("error conflicting arguments --thread and -t");
						System.exit(1);
					} 
					if (!it.hasNext()) {
						System.err.println("arguments invalid please refer to --help");
						System.exit(1);
					} 
					nbt = it.next();
					if (nbt.matches("[0-9]*")) {
						threadNb = Integer.valueOf(Integer.parseInt(nbt));
						continue;
					} 
					System.err.println("arguments invalid please refer to --help");
					System.exit(1);
					continue;
					case 1333013276:
					if (!str1.equals("--file"))
					break; 
					if (!it.hasNext()) {
						System.err.println("arguments invalid please refer to --help");
						System.exit(1);
					} 
					f = new File(it.next());
					continue;
					case 1333069025:
					if (!str1.equals("--help"))
					break; 
					System.out.println("use --file to specify the modfile\nuse --check to check without updating\nuse --help to see this help\nuse -v to see verbose\nuse --threads to specify the number of threads\nuse -t to set the number of thread automaticaly");
					System.exit(0);
					continue;
				} 
				System.err.print("unknown args " + cmd + "\n" + 
				"see --help for help");
				System.exit(1);
			} 
		} 
	}
	
	public static boolean isAComment(String line) {
		return line.startsWith("//");
	}
	
	public static boolean isVersbose() {
		return verbose;
	}
	
	public static boolean isCheckingOnly() {
		return checkingonly;
	}
}
