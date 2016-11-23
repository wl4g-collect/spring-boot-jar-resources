package com.github.ulisesbocchio.jar.resources;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ulises Bocchio
 */
@Slf4j
public class JarResource implements Resource {

    private final Resource delegate;
    private final String extractPath;
    private static final Map<URL, File> FILE_REGISTRY = new ConcurrentHashMap<>();

    public JarResource(Resource resource) {
        this(resource, null);

    }

    public JarResource(Resource resource, String extractPath) {
        this.delegate = resource;
        this.extractPath = extractPath;
    }

    @Override
    public boolean exists() {
        return delegate.exists();
    }

    @Override
    public boolean isReadable() {
        return delegate.isReadable();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public URL getURL() throws IOException {
        return delegate.getURL();
    }

    @Override
    public URI getURI() throws IOException {
        return delegate.getURI();
    }

    @Override
    public File getFile() throws IOException {
        URL resourceUrl = getURL();
        if(ResourceUtils.isJarURL(resourceUrl)) {
            File tempFile = FILE_REGISTRY.get(resourceUrl);
            if(tempFile == null || !tempFile.exists()) {
                log.debug("Extracting File for URL: {}", resourceUrl);
                tempFile = JarUtils.getFile(delegate, extractPath);
                FILE_REGISTRY.put(resourceUrl, tempFile);
            } else {
                log.debug("File found in registry for URL: {}", resourceUrl);
            }
            return new File(tempFile.toURI());
        }
        return delegate.getFile();
    }

    @Override
    public long contentLength() throws IOException {
        return getFile().length();
    }

    @Override
    public long lastModified() throws IOException {
        return getFile().lastModified();
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        return new JarResource(delegate.createRelative(relativePath), extractPath);
    }

    @Override
    public String getFilename() {
        return delegate.getFilename();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    /**
     * This implementation returns the description of this resource.
     * @see #getDescription()
     */
    @Override
    public String toString() {
        return getDescription();
    }

    /**
     * This implementation compares description strings.
     * @see #getDescription()
     */
    @Override
    public boolean equals(Object obj) {
        return (obj == this ||
                (obj instanceof Resource && ((Resource) obj).getDescription().equals(getDescription())));
    }

    /**
     * This implementation returns the description's hash code.
     * @see #getDescription()
     */
    @Override
    public int hashCode() {
        return getDescription().hashCode();
    }

}
