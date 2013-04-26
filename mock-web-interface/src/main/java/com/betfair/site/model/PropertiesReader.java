package com.betfair.site.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: BrumfittC
 * Date: 04/04/13
 * Time: 12:55
 * To change this template use File | Settings | File Templates.
 */
public class PropertiesReader {

    String path;

    public PropertiesReader() throws IOException{
        Properties props = new Properties();
        InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream("/resources/paths.properties");
        props.load(fis);
        path = props.getProperty("root");
    }

    public String getPath() {
        return path;
    }

}
