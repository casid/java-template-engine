package org.jusecase.kte;

import java.util.List;

public interface CodeResolver {
    String resolve(String name);

    default List<String> resolveAllTemplateNames() {
        throw new UnsupportedOperationException("This code resolver does not support finding all template names!");
    }
}
