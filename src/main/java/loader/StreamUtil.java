package loader;


import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {
    public static final String SUFFIX = ".csv";

    public static File stream2file (InputStream in,String filename) throws IOException {
        //TODO set normal filename
        final File tempFile = File.createTempFile(filename, SUFFIX);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }
        return tempFile;
    }
}
