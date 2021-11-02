package com.twodevsstudio.simplejsonconfig.api;

import com.twodevsstudio.simplejsonconfig.interfaces.Comment;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;

public class CommentProcessor {
  private static final String COMMENT_PREFIX = "//";

  @SneakyThrows
  public void includeComments(File file, Map<String, Comment> comments) {

    List<String> lines = Files.readAllLines(Paths.get(file.getPath()));

    for (Map.Entry<String, Comment> entry : comments.entrySet()) {
      String fieldName = entry.getKey();
      Comment comment = entry.getValue();

      String line =
          lines.stream()
              .filter(searchLine -> searchLine.contains("\"" + fieldName + "\"" + ":"))
              .findFirst()
              .orElse(null);

      if (line == null) {
        continue;
      }

      int index = lines.indexOf(line);

      lines.set(index, line + " " + COMMENT_PREFIX + comment.value());
    }

    Files.write(Paths.get(file.getPath()), lines, StandardOpenOption.WRITE);
  }

  @SneakyThrows
  public void includeComments(File file, Object object) {

    Map<String, Comment> comments = AnnotationProcessor.getFieldsComments(object);
    includeComments(file, comments);
  }

  @SneakyThrows
  public File getFileWithoutComments(File file) {

    List<String> lines = Files.readAllLines(Paths.get(file.getPath()));
    List<String> linesWithoutComments = new ArrayList<>();

    Path tempFile = Files.createTempFile("nocomment-", "-" + file.getName());

    for (String line : lines) {

      if (line.contains(COMMENT_PREFIX)) {

        int commentStartIndex = line.lastIndexOf(COMMENT_PREFIX);
        int endStringIndex = line.lastIndexOf("\"");

        if (commentStartIndex > endStringIndex) {

          String comment = line.substring(commentStartIndex);

          String lineWithoutComment = line.replace(comment, "");
          linesWithoutComments.add(lineWithoutComment);
          continue;
        }
      }

      linesWithoutComments.add(line);
    }

    Files.write(tempFile, linesWithoutComments, StandardOpenOption.WRITE);

    File temp = tempFile.toFile();
    temp.deleteOnExit();

    return temp;
  }
}
