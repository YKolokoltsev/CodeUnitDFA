package org.ykolokoltsev.codeunitdfa.core.examples;

public class FieldTargetExample {

  private int x;

  public void explicitSetterSameName(int x) {
    this.x = x;
  }

  public void explicitSetterAnyName(int y) {
    this.x = y;
  }
}
