package org.ykolokoltsev.codeunitdfa.core.examples;

public class JavaParameterLocal {
  int x;
  int y;

  void callSetX(int a) {
    setX(a);
  }

  void callSetXY(int a, int b) {
    setXY(a, b);
  }

  void callSetXYComplexArgument(int a, int b, int c) {
    setXY(a + b, b + c);
  }

  void callTwoMethods(int a, int b) {
    setX(a);
    setXY(a, b + 1);
  }

  void setX(int x) {
    this.x = x;
  }

  void setXY(int x, int y) {
    this.x = x;
  }
}
