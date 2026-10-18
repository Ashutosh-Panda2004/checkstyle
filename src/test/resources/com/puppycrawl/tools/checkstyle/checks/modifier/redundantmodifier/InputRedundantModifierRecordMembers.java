/*
RedundantModifier
jdkVersion = (default)22
tokens = (default)METHOD_DEF, VARIABLE_DEF, ANNOTATION_FIELD_DEF, INTERFACE_DEF, \
         CTOR_DEF, CLASS_DEF, ENUM_DEF, RESOURCE, ANNOTATION_DEF, RECORD_DEF, \
         PATTERN_VARIABLE_DEF, LITERAL_CATCH, LAMBDA


*/

package com.puppycrawl.tools.checkstyle.checks.modifier.redundantmodifier;

// Java17
public class InputRedundantModifierRecordMembers {

    public record RecordWithFinalMethods(int x) {

        public final int doubled() { // violation 'Redundant 'final' modifier.'
            return x * 2;
        }

        @Override
        public final String toString() { // violation 'Redundant 'final' modifier.'
            return "R";
        }

        public int tripled() {
            return x * 3;
        }
    }

    public record RecordWithNestedClasses(int x) {

        static class Nested { // violation 'Redundant 'static' modifier.'
        }

        public static class PublicNested { // violation 'Redundant 'static' modifier.'
        }

        class PlainNested {
        }

        class Inner {

            public final void method() {
            }
        }
    }

    public interface InterfaceWithNestedClass {

        static class InInterface { // violation 'Redundant 'static' modifier.'
        }
    }

    public static class PlainClass {

        static class NestedInClass {
        }

        class Inner {

            public final void method() {
            }
        }
    }
}
