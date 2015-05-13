/**
 * 
 */
package de.unibonn.iai.eis.irap.helper;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kemele M. Endris
 *
 */
public class Utilities {

	 private static final Logger logger = LoggerFactory.getLogger(Utilities.class);
	 
	/**
	 * deletes a file identified by the its name
	 * 
	 * @param filename
	 * @return
	 */
	public static boolean deleteFile(String filename) {
		try {
			return new File(filename).delete();
		} catch (Exception e) {
			logger.error("Cannot delete file: " + filename, e);
			return false;
		}
	}
	
    /**
     *  Decompresses the passed GZip file, and returns the filename of the decompressed file
     *
     * @param filename The filename of compressed file
     * @return The filename of the output file, or empty string if a problem occurs
     */
    public static String decompressGZipFile(String filename) {

        String outFilename;
        //The output filename is the same as input filename without last .gz
        int lastDotPosition = filename.lastIndexOf('.');
        outFilename = filename.substring(0, lastDotPosition);

        try (
                FileInputStream fis = new FileInputStream(filename);
                GZIPInputStream gis = new GZIPInputStream(fis);
                InputStreamReader isr = new InputStreamReader(gis, "UTF8");
                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outFilename), "UTF8")
        ) {
            int character;
            while ((character = isr.read()) != -1) {
                out.write(character);
            }

            logger.debug("File : " + filename + " decompressed successfully to " + outFilename);
        } catch (EOFException e) {
            // probably Wrong compression, out stream will close and existing contents will remain
            // but might leave incomplete triples
            logger.error("EOFException in compressed file: " + filename + " - Trying to recover");
        } catch (IOException ioe) {
            logger.warn("File " + filename + " cannot be decompressed due to " + ioe.getMessage(), ioe);
            outFilename = "";
        } finally {
        	Utilities.deleteFile(filename);
        }
        return outFilename;
    }
    
    /**
     * Downloads the file with passed URL to the passed folder
     * http://stackoverflow.com/a/921400/318221
     *
     * @param fileURL    URL of the file that should be downloaded
     * @param folderPath The path to which this file should be saved
     * @return The local full path of the downloaded file, empty string is returned if a problem occurs
     */
    public static String downloadFile(String fileURL, String folderPath) {

        //Extract filename only without full path
        int lastSlashPos = fileURL.lastIndexOf('/');
        if (lastSlashPos < 0) {
            return null;
        }

        String fullFileName = folderPath + fileURL.substring(lastSlashPos + 1);

        //Create parent folder if it does not already exist
        File file = new File(fullFileName);
        file.getParentFile().mkdirs();

        URL url;

        try {
            url = new URL(fileURL);
        } catch (MalformedURLException e) {
        	logger.info(fullFileName, e);
            return null;
        }

        try (
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(file);
        ) {

            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        } catch (FileNotFoundException e) {
        	logger.info(fullFileName, e);
            return null;
        } catch (IOException e) {
        	logger.info(fullFileName, e);
            return null;
        }

        return fullFileName;
    }

    public static String getTimestamp() {
        return new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
    }
    
    public static String getFileAsString(String filename) {
        StringBuilder str = new StringBuilder();

        try (InputStreamReader in = new InputStreamReader(new FileInputStream(filename), "UTF-8")) {
            int ch;
            while ((ch = in.read()) != -1) {
                str.append((char) ch);
            }
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File " + filename + " not found!", e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UnsupportedEncodingException: ", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("IOException in file " + filename, e);
        }

        return str.toString();
    }
}
