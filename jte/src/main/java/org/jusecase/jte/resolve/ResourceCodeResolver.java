package org.jusecase.jte.resolve;

import org.jusecase.jte.CodeResolver;
import org.jusecase.jte.compile.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class ResourceCodeResolver implements CodeResolver {
    private final String root;

    public ResourceCodeResolver(String root) {
        this.root = root + "/";
    }

    @Override
    public String resolve(String name) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(root + name)) {
            if (is == null) {
                return null;
            }
            return IoUtils.toString(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean hasChanged(String name) {
        return false;
    }
}
