package cafe.woden.ircclient.release;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public final class PluginApiBaselineTool {

  private static final String MANIFEST_CLASS =
      "cafe.woden.ircclient.plugin.spi.IrcafePluginManifest";
  private static final String API_VERSION_FIELD = "SUPPORTED_PLUGIN_API_VERSION";

  private PluginApiBaselineTool() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 4) {
      throw new IllegalArgumentException(
          "Usage: PluginApiBaselineTool <write|verify> <classes-dir> <baseline-file> <api-version>");
    }

    String mode = args[0].toLowerCase(Locale.ROOT);
    Path classesDirectory = Path.of(args[1]).toAbsolutePath().normalize();
    Path baselineFile = Path.of(args[2]).toAbsolutePath().normalize();
    int expectedApiVersion = Integer.parseInt(args[3]);

    if (!Files.isDirectory(classesDirectory)) {
      throw new IllegalStateException(
          "Plugin API classes directory does not exist: " + classesDirectory);
    }

    try (URLClassLoader loader =
        new URLClassLoader(
            new URL[] {classesDirectory.toUri().toURL()}, ClassLoader.getPlatformClassLoader())) {
      int actualApiVersion = readApiVersion(loader);
      if (actualApiVersion != expectedApiVersion) {
        throw new IllegalStateException(
            "Plugin API baseline task expects version "
                + expectedApiVersion
                + " but IrcafePluginManifest declares "
                + actualApiVersion
                + ". Add a versioned baseline and verification task before changing the manifest version.");
      }

      List<String> currentSignature = collectSignature(classesDirectory, loader);
      switch (mode) {
        case "write" -> writeBaseline(baselineFile, expectedApiVersion, currentSignature);
        case "verify" -> verifyBaseline(baselineFile, expectedApiVersion, currentSignature);
        default -> throw new IllegalArgumentException("Unsupported mode: " + mode);
      }
    }
  }

  private static int readApiVersion(ClassLoader loader) throws Exception {
    Class<?> manifestClass = Class.forName(MANIFEST_CLASS, false, loader);
    return manifestClass.getField(API_VERSION_FIELD).getInt(null);
  }

  private static List<String> collectSignature(Path classesDirectory, ClassLoader loader)
      throws Exception {
    List<String> classNames;
    try (Stream<Path> files = Files.walk(classesDirectory)) {
      classNames =
          files
              .filter(Files::isRegularFile)
              .map(classesDirectory::relativize)
              .map(Path::toString)
              .filter(name -> name.endsWith(".class"))
              .map(name -> name.substring(0, name.length() - ".class".length()))
              .map(name -> name.replace('/', '.').replace('\\', '.'))
              .filter(name -> !name.endsWith("module-info") && !name.endsWith("package-info"))
              .sorted()
              .toList();
    }

    Set<String> entries = new TreeSet<>();
    for (String className : classNames) {
      Class<?> type = Class.forName(className, false, loader);
      if (!isPublished(type.getModifiers())) {
        continue;
      }
      addType(entries, type);
      addFields(entries, type);
      addConstructors(entries, type);
      addMethods(entries, type);
      addRecordComponents(entries, type);
    }
    return List.copyOf(entries);
  }

  private static boolean isPublished(int modifiers) {
    return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
  }

  private static void addType(Set<String> entries, Class<?> type) {
    String kind;
    if (type.isAnnotation()) {
      kind = "annotation";
    } else if (type.isEnum()) {
      kind = "enum";
    } else if (type.isRecord()) {
      kind = "record";
    } else if (type.isInterface()) {
      kind = "interface";
    } else {
      kind = "class";
    }

    List<String> interfaces =
        Arrays.stream(type.getGenericInterfaces())
            .map(java.lang.reflect.Type::getTypeName)
            .sorted()
            .toList();
    List<String> permitted =
        type.isSealed()
            ? Arrays.stream(type.getPermittedSubclasses()).map(Class::getName).sorted().toList()
            : List.of();
    String superType =
        type.getGenericSuperclass() == null ? "<none>" : type.getGenericSuperclass().getTypeName();

    entries.add(
        "TYPE|"
            + type.getName()
            + "|modifiers="
            + Modifier.toString(type.getModifiers())
            + "|kind="
            + kind
            + "|super="
            + superType
            + "|interfaces="
            + interfaces
            + "|permitted="
            + permitted
            + "|annotations="
            + annotationNames(type.getDeclaredAnnotations()));
  }

  private static void addFields(Set<String> entries, Class<?> type) {
    Arrays.stream(type.getDeclaredFields())
        .filter(field -> isPublished(field.getModifiers()))
        .sorted(Comparator.comparing(Field::toGenericString))
        .forEach(
            field ->
                entries.add(
                    "FIELD|"
                        + type.getName()
                        + "|"
                        + field.toGenericString()
                        + "|constant="
                        + constantValue(field)
                        + "|synthetic="
                        + field.isSynthetic()
                        + "|annotations="
                        + annotationNames(field.getDeclaredAnnotations())));
  }

  private static void addConstructors(Set<String> entries, Class<?> type) {
    Arrays.stream(type.getDeclaredConstructors())
        .filter(constructor -> isPublished(constructor.getModifiers()))
        .sorted(Comparator.comparing(Constructor::toGenericString))
        .forEach(
            constructor ->
                entries.add(
                    "CONSTRUCTOR|"
                        + type.getName()
                        + "|"
                        + constructor.toGenericString()
                        + "|varargs="
                        + constructor.isVarArgs()
                        + "|synthetic="
                        + constructor.isSynthetic()
                        + "|annotations="
                        + annotationNames(constructor.getDeclaredAnnotations())
                        + "|parameterAnnotations="
                        + parameterAnnotationNames(constructor.getParameterAnnotations())));
  }

  private static void addMethods(Set<String> entries, Class<?> type) {
    Arrays.stream(type.getDeclaredMethods())
        .filter(method -> isPublished(method.getModifiers()))
        .sorted(Comparator.comparing(Method::toGenericString))
        .forEach(
            method ->
                entries.add(
                    "METHOD|"
                        + type.getName()
                        + "|"
                        + method.toGenericString()
                        + "|default="
                        + method.isDefault()
                        + "|bridge="
                        + method.isBridge()
                        + "|varargs="
                        + method.isVarArgs()
                        + "|synthetic="
                        + method.isSynthetic()
                        + "|annotationDefault="
                        + String.valueOf(method.getDefaultValue())
                        + "|annotations="
                        + annotationNames(method.getDeclaredAnnotations())
                        + "|parameterAnnotations="
                        + parameterAnnotationNames(method.getParameterAnnotations())));
  }

  private static void addRecordComponents(Set<String> entries, Class<?> type) {
    if (!type.isRecord()) {
      return;
    }
    Arrays.stream(type.getRecordComponents())
        .sorted(Comparator.comparing(RecordComponent::getName))
        .forEach(
            component ->
                entries.add(
                    "RECORD_COMPONENT|"
                        + type.getName()
                        + "|"
                        + component.getGenericType().getTypeName()
                        + " "
                        + component.getName()
                        + "|annotations="
                        + annotationNames(component.getDeclaredAnnotations())));
  }

  private static String constantValue(Field field) {
    int modifiers = field.getModifiers();
    Class<?> fieldType = field.getType();
    if (!Modifier.isStatic(modifiers)
        || !Modifier.isFinal(modifiers)
        || !(fieldType.isPrimitive() || fieldType == String.class)) {
      return "<not-constant>";
    }
    try {
      if (!field.canAccess(null) && !field.trySetAccessible()) {
        return "<inaccessible>";
      }
      Object value = field.get(null);
      return fieldType.getTypeName() + ":" + escape(String.valueOf(value));
    } catch (ReflectiveOperationException | RuntimeException exception) {
      return "<unreadable:" + exception.getClass().getSimpleName() + ">";
    }
  }

  private static String annotationNames(Annotation[] annotations) {
    return Arrays.stream(annotations)
        .map(annotation -> annotation.annotationType().getName())
        .sorted()
        .toList()
        .toString();
  }

  private static String parameterAnnotationNames(Annotation[][] parameterAnnotations) {
    List<String> parameters = new ArrayList<>(parameterAnnotations.length);
    for (Annotation[] annotations : parameterAnnotations) {
      parameters.add(annotationNames(annotations));
    }
    return parameters.toString();
  }

  private static String escape(String value) {
    return value.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r");
  }

  private static void writeBaseline(Path baselineFile, int apiVersion, List<String> signature)
      throws IOException {
    Files.createDirectories(baselineFile.getParent());
    Path temporary = baselineFile.resolveSibling(baselineFile.getFileName() + ".tmp");
    String content =
        "# IRCafe plugin API binary signature baseline\n"
            + "# API version: "
            + apiVersion
            + "\n"
            + "# Regenerate only after deliberate compatibility review: "
            + "./gradlew generatePluginApiV1Baseline\n\n"
            + String.join("\n", signature)
            + "\n";
    Files.writeString(temporary, content);
    try {
      Files.move(
          temporary,
          baselineFile,
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException exception) {
      Files.move(temporary, baselineFile, StandardCopyOption.REPLACE_EXISTING);
    }
    System.out.println("Wrote " + signature.size() + " plugin API entries to " + baselineFile);
  }

  private static void verifyBaseline(
      Path baselineFile, int apiVersion, List<String> currentSignature) throws IOException {
    if (!Files.isRegularFile(baselineFile)) {
      throw new IllegalStateException(
          "Missing plugin API v" + apiVersion + " baseline: " + baselineFile);
    }

    Set<String> baseline = readSignatureEntries(baselineFile);
    Set<String> current = new TreeSet<>(currentSignature);
    if (baseline.equals(current)) {
      System.out.println(
          "Plugin API v"
              + apiVersion
              + " binary baseline verified ("
              + current.size()
              + " entries).");
      return;
    }

    Set<String> removedOrChanged = new TreeSet<>(baseline);
    removedOrChanged.removeAll(current);
    Set<String> addedOrChanged = new TreeSet<>(current);
    addedOrChanged.removeAll(baseline);

    StringBuilder message = new StringBuilder();
    message
        .append("Plugin API v")
        .append(apiVersion)
        .append(" binary baseline changed.\n")
        .append("Removed or changed baseline entries:\n")
        .append(formatEntries(removedOrChanged))
        .append("\nAdded or changed entries:\n")
        .append(formatEntries(addedOrChanged))
        .append(
            "\nReview source and binary compatibility deliberately. "
                + "If the change is approved, run ")
        .append("./gradlew generatePluginApiV1Baseline and commit the reviewed baseline update. ")
        .append("An incompatible contract change requires a new plugin API version and baseline.");
    throw new IllegalStateException(message.toString());
  }

  private static Set<String> readSignatureEntries(Path baselineFile) throws IOException {
    Set<String> entries = new TreeSet<>();
    for (String line : Files.readAllLines(baselineFile)) {
      String trimmed = line.trim();
      if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
        entries.add(trimmed);
      }
    }
    return entries;
  }

  private static String formatEntries(Set<String> entries) {
    if (entries.isEmpty()) {
      return "  <none>";
    }
    int limit = 200;
    StringBuilder out = new StringBuilder();
    int count = 0;
    for (String entry : entries) {
      if (count == limit) {
        out.append("  ... and ").append(entries.size() - limit).append(" more\n");
        break;
      }
      out.append("  ").append(entry).append('\n');
      count++;
    }
    return out.toString().stripTrailing();
  }
}
