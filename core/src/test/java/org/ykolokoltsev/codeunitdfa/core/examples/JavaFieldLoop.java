package org.ykolokoltsev.codeunitdfa.core.examples;

public class JavaFieldLoop {
  int x;

  void forLoop() {
    for (int i = 0; i < 3; i++) {
      x = i;
    }
  }

  void forLoopIterative(int a) {
    for (int i = 0; i < 3; i++) {
      x = a * x;
    }
  }

  void whileLoop() {
    int i = 0;
    while (i < 3) {
      x = i;
      i = i + 1;
    }
  }

  void doWhileLoop() {
    int i = 0;
    do {
      x = i;
      i = i + 1;
    } while (i < 3);
  }

}
