package org.example;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "ccwc",
    mixinStandardHelpOptions = true,
    version = "ccwc 1.0",
    description = "word, line, character and byte count")
public class CCWC implements Callable<Integer> {

  @Parameters(index = "0", description = "target file", arity = "0..1")
  private File file;

  @Option(
      names = {"-c"},
      description = "show byte count")
  private boolean showByteCount;

  @Option(
      names = {"-l"},
      description = "show line count")
  private boolean showLineCount;

  @Option(
      names = {"-w"},
      description = "show word count")
  private boolean showWordCount;

  @Option(
      names = {"-m"},
      description = "show character count")
  private boolean showCharacterCount;

  public static void main(String[] args) {
    int exitCode = new CommandLine(new CCWC()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() {
    boolean tmpFile = false;
    try {
      if (!showLineCount && !showWordCount && !showByteCount && !showCharacterCount) {
        showLineCount = true;
        showWordCount = true;
        showByteCount = true;
      }

      if (file == null) {
        tmpFile = true;
        // Read from standard input
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = System.in.read(buffer)) != -1) {
          baos.write(buffer, 0, bytesRead);
        }
        file = Files.createTempFile("stdin", ".tmp").toFile();
        try (OutputStream fileOutputStream = new FileOutputStream(file)) {
          baos.writeTo(fileOutputStream);
        }
      }

      StringBuffer sb = new StringBuffer();
      if (showLineCount) {
        sb.append(countLines(file));
        sb.append(" ");
      }
      if (showWordCount) {
        sb.append(countWords(file));
        sb.append(" ");
      }
      if (showByteCount && !showCharacterCount) {
        sb.append(countBytes(file));
        sb.append(" ");
      }
      if (showCharacterCount) {
        sb.append(countCharacter(file));
        sb.append(" ");
      }
      sb.append(file.getName());
      System.out.println(sb);
      return 0;
    } catch (IOException e) {
      System.out.println(e);
      return 1;
    } finally {
      if (tmpFile && file != null && file.exists()) {
        file.delete();
      }
    }
  }

  private long countBytes(File file) throws IOException {
    return Files.size(file.toPath());
  }

  private long countLines(File file) throws IOException {
    Stream<String> lines = Files.lines(file.toPath());
    return lines.count();
  }

  private long countWords(File file) throws IOException {
    Stream<String> lines = Files.lines(file.toPath());
    return lines.flatMap(Pattern.compile("\\s+")::splitAsStream).filter(s -> !s.isEmpty()).count();
  }

  private long countCharacter(File file) throws IOException {
    long noOfCharacters = 0;
    try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
      while (reader.read() != -1) {
        noOfCharacters++;
      }
    }
    return noOfCharacters;
  }
}
