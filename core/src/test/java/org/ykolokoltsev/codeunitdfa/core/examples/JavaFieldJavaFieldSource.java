package org.ykolokoltsev.codeunitdfa.core.examples;

public class JavaFieldJavaFieldSource {
  int x;
  int y;

  void setFieldFromAnotherField() {
    x = y;
  }

  void notAFieldTarget() {
    int x = y;
  }

  void setUsingPreviousValue() {
    x = x + 1;
  }

  void useOtherFieldAsVariable() {
    y = x + y;
    x = y;
  }
}
