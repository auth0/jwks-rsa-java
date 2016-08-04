package com.auth0;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.apache.commons.io.IOUtils;
import sun.nio.ch.IOUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Created by auth0 on 8/4/16.
 */
public class FileBasedJwksReader implements JwksReader {
    private HttpJwksReader serverReader;
    private String path;

    public FileBasedJwksReader(String jwksUri, String path) {
        this.serverReader = new HttpJwksReader(jwksUri);
        this.path = path;
    }

    public InputStream getJwks() throws JwksException {
        File f = new File(this.path);

        if(f.exists() && !f.isDirectory()) {
            try {
                return new FileInputStream(f);
            } catch (FileNotFoundException e) {
                // We just ignore this condition and move on
            }
        }

        try {
            InputStream is = serverReader.getJwks();
            byte[] bytes = IOUtils.toByteArray(is);

            Files.copy(new ByteInputStream(bytes, bytes.length),
                    Paths.get(this.path), StandardCopyOption.REPLACE_EXISTING);

            return new ByteInputStream(bytes, bytes.length);
        } catch (IOException e) {
            throw new JwksException("Cannot write cache file", e);
        }
    }
}
