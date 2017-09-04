package loader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LoadProperties {

    private final String PROP_FILE_NAME = "config.properties";
    private InputStream inputStream;
    private Properties props;

    public LoadProperties() {
        props = new Properties();
    }

    public void load() throws IOException {

        Properties prop = new Properties();
        inputStream = getClass().getClassLoader().getResourceAsStream(PROP_FILE_NAME);

        try {
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + PROP_FILE_NAME + "' not found in the classpath");
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            inputStream.close();
        }

        props = prop;
    }

    public String getPropertyValue(String propName) {
        String propVal = null;

        if (props != null) {
            if (props.containsKey(propName)) {
                propVal = props.getProperty(propName);
            }
        }

        return propVal;
    }
}
