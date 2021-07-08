package downloader.helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarFile;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import downloader.Log;

public class ArchiveHelper {
	private ArchiveHelper(){}

    public static File decompressBz2(File inputFile, String outputFile) throws IOException {
		BZip2CompressorInputStream input = new BZip2CompressorInputStream(
				new BufferedInputStream(new FileInputStream(inputFile)));
		File decompressedFile = new File(outputFile);
		FileOutputStream output = new FileOutputStream(decompressedFile);
		IOUtils.copy(input, output);
		return decompressedFile;
	}

	/**
	 * @param mod
	 * @return true = fichier bon
	 */
	public static boolean checkJarIntegrity(File mod) {
        try {
            //si sa balance une exception sa veut dire que le jar n'est pas lisible
            new JarFile(mod).close();
        } catch (IOException e) {
            Log.i("integrityChecker","Corruption detected redownloading");
            mod.delete();
            return false;
        }
        return true;
    }
}
