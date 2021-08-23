package com.twodevsstudio.simplejsonconfig.def.scanner;

import java.util.List;
import org.reflections.Store;
import org.reflections.scanners.AbstractScanner;


@SuppressWarnings({"unchecked"})
public class SkipRecordsAnnotationScanner extends AbstractScanner {


  /**
   * scans for field's annotations
   */

  public void scan(final Object cls, Store store) {

    /*if (cls.getClass().isRecord()) {
      return;
    }*/

    final String className = getMetadataAdapter().getClassName(cls);
    List<Object> fields = getMetadataAdapter().getFields(cls);

    for (final Object field : fields) {
      List<String> fieldAnnotations = getMetadataAdapter().getFieldAnnotationNames(field);
      for (String fieldAnnotation : fieldAnnotations) {

        if (acceptResult(fieldAnnotation)) {
          String fieldName = getMetadataAdapter().getFieldName(field);
          put(store, fieldAnnotation, String.format("%s.%s", className, fieldName));
        }
      }
    }
  }
}