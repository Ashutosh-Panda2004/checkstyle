/*
ParameterNumber
max = 2
ignoreAnnotatedBy = MyAnno
ignoreOverriddenMethods = (default)false
tokens = (default)METHOD_DEF, CTOR_DEF, COMPACT_CTOR_DEF, RECORD_DEF


*/

package com.puppycrawl.tools.checkstyle.checks.sizes.parameternumber;

public class InputParameterNumberRecordsIgnoreAnnotatedBy {

    record CompactIgnored(int a, int b, int c) {
        @MyAnno
        public CompactIgnored {
        }
    }

    record CompactLogged(int a, int b, int c) {
        // violation below 'More than 2 parameters (found 3).'
        public CompactLogged {
        }
    }

    @MyAnno
    record ImplicitIgnored(int a, int b, int c) {
    }

    // violation below 'More than 2 parameters (found 3).'
    record ImplicitLogged(int a, int b, int c) {
    }
}

@interface MyAnno {
}
