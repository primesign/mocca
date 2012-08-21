import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class JettyTempCleaner extends TimerTask {

	private static final long CLEAN_INTERVAL = 60000; // 1 minute

	private File tmpDir;

	public JettyTempCleaner(File tmpDir) {
		this.tmpDir = tmpDir;
	}

	private static boolean deleteRecursive(File f) {
		if (f.isDirectory()) {
			String[] children = f.list();
			for (String child : children) {
				if (!deleteRecursive(new File(f, child)))
					return false;
			}
		}
		return f.delete();
	}

	private static void clean(File tmpDir) {
		System.out.println("Trying to remove " + tmpDir);
		if (deleteRecursive(tmpDir)) {
			System.out.println("Successfully removed temporary directory");
			System.exit(0);
		}
	}

	private void schedule() {
		System.out.println("Scheduling cleaner for directory " + tmpDir);
		Timer t = new Timer();
		t.scheduleAtFixedRate(this, 1000, CLEAN_INTERVAL);
	}

	@Override
	public void run() {
		clean(tmpDir);
	}

	public static void main(String[] args) {
		if (args.length != 1)
			System.exit(1);
		File tmpDir = new File(args[0]);
		if (!tmpDir.exists())
		{
			System.err.println("Directory " + args[0] + " doesn't exist");
			System.exit(2);
		}
		JettyTempCleaner cleaner = new JettyTempCleaner(tmpDir);
		cleaner.schedule();
	}
}
