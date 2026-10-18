/*
ParameterNumber
max = (default)7
ignoreAnnotatedBy = (default)
ignoreOverriddenMethods = (default)false
tokens = (default)METHOD_DEF, CTOR_DEF, COMPACT_CTOR_DEF, RECORD_DEF


*/

package com.puppycrawl.tools.checkstyle.checks.sizes.parameternumber;

public class InputParameterNumberRecords {

    // violation below 'More than 7 parameters (found 8).'
    record ImplicitMany(int a, int b, int c, int d, int e, int f, int g, int h) {
    }

    record ImplicitFew(int a, int b, int c) {
        int sum() {
            return a + b + c;
        }
    }

    record CompactMany(int a, int b, int c, int d, int e, int f, int g, int h) {
        // violation below 'More than 7 parameters (found 8).'
        public CompactMany {
        }
    }

    record CompactFew(int a, int b, int c) {
        public CompactFew {
        }
    }

    record LongFormMany(int a, int b, int c, int d, int e, int f, int g, int h) {
        // violation below 'More than 7 parameters (found 8).'
        public LongFormMany(int a, int b, int c, int d, int e, int f, int g, int h) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.e = e;
            this.f = f;
            this.g = g;
            this.h = h;
        }
    }

    // violation below 'More than 7 parameters (found 8).'
    record AdditionalCtorMany(int a, int b, int c, int d, int e, int f, int g, int h) {
        AdditionalCtorMany(int a) {
            this(a, 0, 0, 0, 0, 0, 0, 0);
        }
    }

    record AdditionalCtorFew(int a, int b, int c) {
        AdditionalCtorFew(int a) {
            this(a, 0, 0);
        }
    }
}
