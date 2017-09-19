package loader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    private final String PROP_FILE_NAME = "config.properties";
    private InputStream inputStream;
    private Properties props;

    public PropertiesLoader()  {
        props = new Properties();
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
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
