package gg.jte.compiler.kotlin;

import gg.jte.TemplateException;
import gg.jte.runtime.ClassInfo;
import gg.jte.runtime.Constants;
import org.jetbrains.kotlin.cli.common.ExitCode;
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler;
import org.jetbrains.kotlin.config.Services;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class KotlinClassFilesCompiler {
    public static void compile(Path classDirectory, String[] files, Map<String, ClassInfo> templateByClassName) {
        K2JVMCompilerArguments compilerArguments = new K2JVMCompilerArguments();
        //compilerArguments.setJvmTarget("");
        compilerArguments.setJavaParameters(true);
        compilerArguments.setNoStdlib(true);
        compilerArguments.setDestination(classDirectory.toFile().getAbsolutePath());

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        StringBuilder classpath = new StringBuilder();
        String separator = System.getProperty("path.separator");
        String prop = System.getProperty("java.class.path");

        if (prop != null && !"".equals(prop)) {
            classpath.append(prop);
        }

        if (classLoader instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                if (classpath.length() > 0) {
                    classpath.append(separator);
                }

                if ("file".equals(url.getProtocol())) {
                    try {
                        classpath.append(new File(url.toURI()));
                    } catch (URISyntaxException e) {
                        e.printStackTrace(); // TODO
                    }
                }
            }
        }

        compilerArguments.setFreeArgs(Arrays.asList(files));

        compilerArguments.setClasspath(classpath.toString());

        K2JVMCompiler compiler = new K2JVMCompiler();

        SimpleKotlinCompilerMessageCollector messageCollector = new SimpleKotlinCompilerMessageCollector(templateByClassName);
        ExitCode exitCode = compiler.exec(messageCollector, new Services.Builder().build(), compilerArguments);

        if (exitCode != ExitCode.OK && exitCode != ExitCode.COMPILATION_ERROR) {
            throw new TemplateException(messageCollector.getErrorMessage());
        }

        if (messageCollector.hasErrors()) {
            throw new TemplateException(messageCollector.getErrorMessage());
        }
    }

    private static class SimpleKotlinCompilerMessageCollector implements MessageCollector {

        private final Map<String, ClassInfo> templateByClassName;
        private final List<String> errors = new ArrayList<>();

        private String className;
        private int line;

        private SimpleKotlinCompilerMessageCollector(Map<String, ClassInfo> templateByClassName) {
            this.templateByClassName = templateByClassName;
        }

        @Override
        public void clear() {
        }

        @Override
        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        @Override
        public void report(CompilerMessageSeverity severity, String s, CompilerMessageLocation location) {
            if (severity.isError()) {
                if ((location != null) && (location.getLineContent() != null)) {
                    if (className == null) {
                        className = extractClassName(location);
                        line = location.getLine();
                    }

                    errors.add(String.format("%s%n%s:%d:%d%nReason: %s", location.getLineContent(), location.getPath(),
                            location.getLine(),
                            location.getColumn(), s));
                } else {
                    errors.add(s);
                }
            }
        }

        private String extractClassName(CompilerMessageLocation location) {
            String path = location.getPath();
            path = path.replace('/', '.').replace('\\', '.');
            int packageIndex = path.indexOf(Constants.PACKAGE_NAME);

            path = path.substring(packageIndex);

            // Remove .kt extension
            path = path.substring(0, path.length() - 3);

            return path;
        }

        public String getErrorMessage() {
            String allErrors = String.join("\n", errors);

            if (className != null) {
                ClassInfo templateInfo = templateByClassName.get(className);
                int templateLine = templateInfo.lineInfo[line - 1] + 1;

                return "Failed to compile template, error at " + templateInfo.name + ":" + templateLine + "\n" + allErrors;
            } else {
                return "Failed to compile template, error at\n" + errors;
            }
        }
    }
}
