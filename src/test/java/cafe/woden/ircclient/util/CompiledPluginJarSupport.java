package cafe.woden.ircclient.util;

import cafe.woden.ircclient.plugin.spi.IrcafePluginManifest;
import cafe.woden.ircclient.plugin.spi.IrcafePluginServiceDescriptors;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public final class CompiledPluginJarSupport {

  private CompiledPluginJarSupport() {}

  public static Map<String, String> compatibleManifest(String pluginId, String pluginVersion) {
    return IrcafePluginManifest.compatibleManifestAttributes(pluginId, pluginVersion);
  }

  public static Map<String, String> compatibleManifestUsingImplementationVersion(
      String pluginId, String pluginVersion) {
    return IrcafePluginManifest.compatibleImplementationVersionManifestAttributes(
        pluginId, pluginVersion);
  }

  public static Path writeLibraryJar(Path jarPath, Map<String, String> sourcesByClassName)
      throws IOException {
    Path normalizedJarPath = jarPath.toAbsolutePath().normalize();
    Path baseDirectory =
        Files.createDirectories(
            Objects.requireNonNull(normalizedJarPath.getParent(), "jarPath.parent"));
    Path workRoot = Files.createTempDirectory(baseDirectory, ".compiled-plugin-");
    Path sourceRoot = Files.createDirectories(workRoot.resolve("src"));
    Path classesRoot = Files.createDirectories(workRoot.resolve("classes"));

    compileSources(writeSources(sourceRoot, sourcesByClassName), classesRoot, List.of());

    try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(normalizedJarPath))) {
      writeCompiledClassEntries(out, classesRoot);
    }

    return normalizedJarPath;
  }

  public static Path writePluginJar(
      Path jarPath,
      String providerClassName,
      String providerSource,
      String serviceTypeName,
      Map<String, String> manifestAttributes)
      throws IOException {
    return writePluginJar(
        jarPath,
        Map.of(providerClassName, providerSource),
        Map.of(serviceTypeName, List.of(providerClassName)),
        manifestAttributes);
  }

  public static Path writePluginJar(
      Path jarPath,
      Map<String, String> sourcesByClassName,
      Map<String, List<String>> serviceProvidersByServiceType,
      Map<String, String> manifestAttributes)
      throws IOException {
    return writePluginJar(
        jarPath, sourcesByClassName, serviceProvidersByServiceType, manifestAttributes, List.of());
  }

  public static Path writePluginJar(
      Path jarPath,
      Map<String, String> sourcesByClassName,
      Map<String, List<String>> serviceProvidersByServiceType,
      Map<String, String> manifestAttributes,
      List<Path> additionalClasspathEntries)
      throws IOException {
    Path normalizedJarPath = jarPath.toAbsolutePath().normalize();
    Path baseDirectory =
        Files.createDirectories(
            Objects.requireNonNull(normalizedJarPath.getParent(), "jarPath.parent"));
    Path workRoot = Files.createTempDirectory(baseDirectory, ".compiled-plugin-");
    Path sourceRoot = Files.createDirectories(workRoot.resolve("src"));
    Path classesRoot = Files.createDirectories(workRoot.resolve("classes"));

    compileSources(
        writeSources(sourceRoot, sourcesByClassName), classesRoot, additionalClasspathEntries);

    Manifest manifest = new Manifest();
    Attributes attributes = manifest.getMainAttributes();
    attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
    for (Map.Entry<String, String> entry :
        Objects.requireNonNullElse(manifestAttributes, Map.<String, String>of()).entrySet()) {
      if (entry.getKey() == null || entry.getValue() == null) {
        continue;
      }
      attributes.putValue(entry.getKey(), entry.getValue());
    }

    try (JarOutputStream out =
        new JarOutputStream(Files.newOutputStream(normalizedJarPath), manifest)) {
      for (Map.Entry<String, List<String>> serviceEntry :
          Objects.requireNonNullElse(serviceProvidersByServiceType, Map.<String, List<String>>of())
              .entrySet()) {
        out.putNextEntry(
            new JarEntry(
                IrcafePluginServiceDescriptors.serviceDescriptorPath(serviceEntry.getKey())));
        out.write(
            IrcafePluginServiceDescriptors.serviceDescriptorContent(
                    Objects.requireNonNullElse(serviceEntry.getValue(), List.<String>of()))
                .getBytes(StandardCharsets.UTF_8));
        out.closeEntry();
      }
      writeCompiledClassEntries(out, classesRoot);
    }

    return normalizedJarPath;
  }

  private static List<Path> writeSources(Path sourceRoot, Map<String, String> sourcesByClassName)
      throws IOException {
    ArrayList<Path> sourcePaths = new ArrayList<>();
    for (Map.Entry<String, String> sourceEntry :
        Objects.requireNonNullElse(sourcesByClassName, Map.<String, String>of()).entrySet()) {
      Path sourcePath = sourceRoot.resolve(sourceEntry.getKey().replace('.', '/') + ".java");
      Files.createDirectories(Objects.requireNonNull(sourcePath.getParent()));
      Files.writeString(sourcePath, sourceEntry.getValue(), StandardCharsets.UTF_8);
      sourcePaths.add(sourcePath);
    }
    return List.copyOf(sourcePaths);
  }

  private static void compileSources(
      List<Path> sourcePaths, Path classesRoot, List<Path> additionalClasspathEntries)
      throws IOException {
    if (sourcePaths == null || sourcePaths.isEmpty()) {
      throw new IllegalArgumentException("[ircafe] plugin test source set must not be empty");
    }
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      throw new IllegalStateException("[ircafe] JDK compiler is not available for plugin tests");
    }

    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
    StringWriter compilerOutput = new StringWriter();
    try (StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
      Iterable<? extends JavaFileObject> compilationUnits =
          fileManager.getJavaFileObjectsFromPaths(sourcePaths);
      List<String> options =
          List.of(
              "--release",
              Integer.toString(IrcafePluginManifest.REQUIRED_JAVA_RELEASE),
              "-classpath",
              compilerClasspath(additionalClasspathEntries),
              "-d",
              classesRoot.toString());
      Boolean success =
          compiler
              .getTask(compilerOutput, fileManager, diagnostics, options, null, compilationUnits)
              .call();
      if (Boolean.TRUE.equals(success)) {
        return;
      }
    }

    StringBuilder message = new StringBuilder("[ircafe] failed to compile test plugin source");
    if (compilerOutput.getBuffer().length() > 0) {
      message.append(": ").append(compilerOutput);
    }
    diagnostics
        .getDiagnostics()
        .forEach(diagnostic -> message.append(System.lineSeparator()).append(diagnostic));
    throw new IllegalStateException(message.toString());
  }

  private static String compilerClasspath(List<Path> additionalClasspathEntries) {
    ArrayList<String> entries = new ArrayList<>();
    for (Path entry : Objects.requireNonNullElse(additionalClasspathEntries, List.<Path>of())) {
      if (entry != null) {
        entries.add(entry.toString());
      }
    }
    String javaClassPath = Objects.toString(System.getProperty("java.class.path"), "").trim();
    if (!javaClassPath.isEmpty()) {
      entries.add(javaClassPath);
    }
    return String.join(File.pathSeparator, entries);
  }

  private static void writeCompiledClassEntries(JarOutputStream out, Path classesRoot)
      throws IOException {
    try (var stream = Files.walk(classesRoot)) {
      stream
          .filter(Files::isRegularFile)
          .forEach(compiledClass -> writeCompiledClassEntry(out, classesRoot, compiledClass));
    }
  }

  private static void writeCompiledClassEntry(
      JarOutputStream out, Path classesRoot, Path compiledClassPath) {
    Path relativePath = classesRoot.relativize(compiledClassPath);
    try {
      out.putNextEntry(new JarEntry(relativePath.toString().replace('\\', '/')));
      out.write(Files.readAllBytes(compiledClassPath));
      out.closeEntry();
    } catch (IOException e) {
      throw new IllegalStateException(
          "[ircafe] failed to write compiled plugin class " + compiledClassPath, e);
    }
  }
}
