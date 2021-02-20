package gg.jte.kotlin;

import gg.jte.CodeResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DummyCodeResolver implements CodeResolver {
    private final Map<String, String> codeLookup = new HashMap<>();

    @Override
    public String resolve(String name) {
        return codeLookup.get(name);
    }

    @Override
    public boolean hasChanged(String name) {
        return false;
    }

    public void givenCode(String name, String code) {
        codeLookup.put(name, code);
    }

    @Override
    public List<String> resolveAllTemplateNames() {
        return new ArrayList<>(codeLookup.keySet());
    }
}
