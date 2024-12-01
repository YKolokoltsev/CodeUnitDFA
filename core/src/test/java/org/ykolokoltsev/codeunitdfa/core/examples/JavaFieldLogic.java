package org.ykolokoltsev.codeunitdfa.core.examples;

public class JavaFieldLogic {
  int x;

  void mergeIfThenBranches(int a, int b, boolean c) {
    if (c) {
      x = a;
    } else {
      x = b;
    }
  }

  void fromGreaterThen(int a, int b) {
    if (a > b) {
      x = 1;
    } else {
      x = 2;
    }
  }

  void fromAssignment(boolean a, boolean b) {
    if (a = b) {
      x = 1;
    } else {
      x = 2;
    }
  }

  void branchesDoNotAffectField(boolean c, int a) {
    int y = 0;
    if (c) {
      y = 1;
    } else {
      y = 2;
    }
    x = a;
  }

  void branchesEquivalent(boolean c) {
    if (c) {
      x = 1;
    } else {
      x = 1;
    }
  }

  void fromComplexCondition(int a, int b, boolean c) {
    if (a > b && c) {
      x = 1;
    } else {
      x = 2;
    }
  }

  void elseIfBranch(boolean a, boolean b) {
    if (a) {
      x = 1;
    } else if (b) {
      x = 2;
    } else {
      x = 3;
    }
  }

  void orBranch(boolean a, boolean b) {
    if (a || b) {
      x = 1;
    } else {
      x = 2;
    }
  }

  void switchBranchWithBreak(int a) {
    switch (a) {
      case 1:
        x = 1;
        break;
      default:
        x = 2;
    }
  }

  void switchWithoutBreak(int a) {
    switch (a) {
      case 1:
        x = 1;
      default:
        x = 2;
    }
  }

  void switchWithReturn(int a) {
    switch (a) {
      case 1:
        x = 1;
        return;
      default:
        x = 2;
    }
  }

}
