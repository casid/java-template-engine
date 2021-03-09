package gg.jte.resolve;

import gg.jte.CodeResolver;
import gg.jte.TemplateEngine;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DirectoryCodeResolver implements CodeResolver {
    private final Path root;
    private final ConcurrentMap<String, Long> modificationTimes = new ConcurrentHashMap<>();
    private Thread reloadThread;

    public DirectoryCodeResolver(Path root) {
        this.root = root;
    }

    @Override
    public String resolve(String name) {
        try {
            Path file = root.resolve(name);
            byte[] bytes = Files.readAllBytes(file);
            modificationTimes.put(name, getLastModified(file));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean exists(String name) {
        Path file = root.resolve(name);
        return Files.exists(file);
    }

    @Override
    public boolean hasChanged(String name) {
        Long lastResolveTime = modificationTimes.get(name);
        if (lastResolveTime == null) {
            return true;
        }

        long lastModified = getLastModified(root.resolve(name));

        return lastModified != lastResolveTime;
    }

    private long getLastModified(Path file) {
        return file.toFile().lastModified();
    }

    @Override
    public List<String> resolveAllTemplateNames() {
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> root.relativize(p).toString().replace('\\', '/'))
                    .filter(this::isTemplateFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to resolve all templates in " + root, e);
        }
    }

    public void startTemplateFilesListener(TemplateEngine templateEngine, Consumer<List<String>> onTemplatesChanged) {
        reloadThread = new Thread(() -> startTemplateFilesListenerBlocking(templateEngine, onTemplatesChanged));
        reloadThread.setName("jte-reloader");
        reloadThread.setDaemon(true);
        reloadThread.start();
    }

    public void stopTemplateFilesListener() {
        reloadThread.interrupt();
        reloadThread = null;
    }

    public void startTemplateFilesListenerBlocking(TemplateEngine templateEngine, Consumer<List<String>> onTemplatesChanged) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

            Files.walk(root).filter(Files::isDirectory).forEach(p -> {
                try {
                    p.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to register watch service for hot reload!", e);
                }
            });

            WatchKey watchKey;
            while ((watchKey = watchService.take()) != null) {
                try {
                    List<WatchEvent<?>> events = watchKey.pollEvents();
                    for (WatchEvent<?> event : events) {
                        String eventContext = event.context().toString();
                        if (!isTemplateFile(eventContext)) {
                            continue;
                        }

                        Path file = root.relativize((Path) watchKey.watchable()).resolve(eventContext);

                        Path absoluteFile = root.resolve(file);
                        if (absoluteFile.toFile().length() <= 0) {
                            continue;
                        }

                        String name = file.toString().replace('\\', '/');

                        List<String> changedTemplates = templateEngine.getTemplatesUsing(name);
                        if (onTemplatesChanged != null) {
                            onTemplatesChanged.accept(changedTemplates);
                        }
                    }
                } finally {
                    watchKey.reset();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to watch page content", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isTemplateFile(String name) {
        return name.endsWith(".jte") || name.endsWith(".kte");
    }
}
