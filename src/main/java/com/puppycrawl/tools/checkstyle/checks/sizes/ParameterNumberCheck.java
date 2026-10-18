///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2026 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
///////////////////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.checks.sizes;

import java.util.Set;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * <div>
 * Checks the number of parameters of a method or constructor.
 * </div>
 *
 * @since 3.0
 */
@StatelessCheck
public class ParameterNumberCheck extends AbstractCheck {

    /**
     * A key is pointing to the warning message text in "messages.properties" file.
     */
    public static final String MSG_KEY = "maxParam";

    /** Default maximum number of allowed parameters. */
    private static final int DEFAULT_MAX_PARAMETERS = 7;

    /** Specify the maximum number of parameters allowed. */
    private int max = DEFAULT_MAX_PARAMETERS;

    /** Ignore number of parameters for methods with {@code @Override} annotation. */
    private boolean ignoreOverriddenMethods;

    /**
     * Ignore methods and constructors annotated with the specified annotation(s).
     */
    private Set<String> ignoreAnnotatedBy = Set.of();

    /**
     * Creates a new {@code ParameterNumberCheck} instance.
     */
    public ParameterNumberCheck() {
        // no code by default
    }

    /**
     * Setter to specify the maximum number of parameters allowed.
     *
     * @param max the max allowed parameters
     * @since 3.0
     */
    public void setMax(int max) {
        this.max = max;
    }

    /**
     * Setter to ignore number of parameters for methods with {@code @Override} annotation.
     *
     * @param ignoreOverriddenMethods set ignore overridden methods
     * @since 6.2
     */
    public void setIgnoreOverriddenMethods(boolean ignoreOverriddenMethods) {
        this.ignoreOverriddenMethods = ignoreOverriddenMethods;
    }

    /**
     * Setter to ignore methods and constructors annotated with the specified annotation(s).
     *
     * @param annotationNames specified annotation(s)
     * @since 10.15.0
     */
    public void setIgnoreAnnotatedBy(String... annotationNames) {
        ignoreAnnotatedBy = Set.of(annotationNames);
    }

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[] {
            TokenTypes.METHOD_DEF,
            TokenTypes.CTOR_DEF,
            TokenTypes.COMPACT_CTOR_DEF,
            TokenTypes.RECORD_DEF,
        };
    }

    @Override
    public int[] getRequiredTokens() {
        return CommonUtil.EMPTY_INT_ARRAY;
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (ast.getType() == TokenTypes.RECORD_DEF) {
            visitRecordDef(ast);
        }
        else if (ast.getType() == TokenTypes.COMPACT_CTOR_DEF) {
            final DetailAST recordDef = ast.getParent().getParent();
            logRecordComponentCount(ast, recordDef);
        }
        else {
            final DetailAST params = ast.findFirstToken(TokenTypes.PARAMETERS);
            final int count = params.getChildCount(TokenTypes.PARAMETER_DEF);
            if (count > max && !shouldIgnoreNumberOfParameters(ast)) {
                final DetailAST name = ast.findFirstToken(TokenTypes.IDENT);
                log(name, MSG_KEY, max, count);
            }
        }
    }

    /**
     * Processes a record definition. A record whose canonical constructor is not
     * declared explicitly, neither in long form nor as a compact constructor, has
     * no constructor node to visit, so the number of its components, which is the
     * parameter count of the implicit canonical constructor, is checked here.
     *
     * @param recordDef the record definition to process
     */
    private void visitRecordDef(DetailAST recordDef) {
        final DetailAST components = recordDef.findFirstToken(TokenTypes.RECORD_COMPONENTS);
        final int count = components.getChildCount(TokenTypes.RECORD_COMPONENT_DEF);
        final DetailAST objBlock = recordDef.findFirstToken(TokenTypes.OBJBLOCK);
        if (!hasExplicitCanonicalConstructor(objBlock, count)) {
            logRecordComponentCount(recordDef, recordDef);
        }
    }

    /**
     * Checks whether a record declares its canonical constructor explicitly, either
     * as a compact constructor or in long form. A long form canonical constructor
     * is recognised by its parameter count, which is always equal to the number of
     * record components; any other constructor is an additional constructor and
     * leaves the canonical constructor implicit.
     *
     * @param objBlock the record body to inspect
     * @param componentCount the number of record components
     * @return true if the canonical constructor is declared explicitly
     */
    private static boolean hasExplicitCanonicalConstructor(DetailAST objBlock,
            int componentCount) {
        boolean result = objBlock.getChildCount(TokenTypes.COMPACT_CTOR_DEF) > 0;
        DetailAST child = objBlock.getFirstChild();
        while (!result && child != null) {
            if (child.getType() == TokenTypes.CTOR_DEF) {
                final DetailAST params = child.findFirstToken(TokenTypes.PARAMETERS);
                result = params.getChildCount(TokenTypes.PARAMETER_DEF) == componentCount;
            }
            child = child.getNextSibling();
        }
        return result;
    }

    /**
     * Logs a violation if the number of components of a record exceeds the maximum.
     * The component count is the parameter count of the record's canonical
     * constructor, so it is checked against the same limit.
     *
     * @param ast the node to log the violation at, a compact constructor definition
     *        or the record definition itself
     * @param recordDef the record definition whose components are counted
     */
    private void logRecordComponentCount(DetailAST ast, DetailAST recordDef) {
        final DetailAST components = recordDef.findFirstToken(TokenTypes.RECORD_COMPONENTS);
        final int count = components.getChildCount(TokenTypes.RECORD_COMPONENT_DEF);
        if (count > max && !shouldIgnoreNumberOfParameters(ast)) {
            final DetailAST name = ast.findFirstToken(TokenTypes.IDENT);
            log(name, MSG_KEY, max, count);
        }
    }

    /**
     * Determine whether to ignore number of parameters.
     *
     * @param ast the token to process
     * @return true if number of parameters should be ignored.
     */
    private boolean shouldIgnoreNumberOfParameters(DetailAST ast) {
        return isIgnoredOverriddenMethod(ast) || isAnnotatedByIgnoredAnnotations(ast);
    }

    /**
     * Checks if method is overridden and should be ignored.
     *
     * @param ast method definition to check
     * @return true if method is overridden and should be ignored.
     */
    private boolean isIgnoredOverriddenMethod(DetailAST ast) {
        return ignoreOverriddenMethods && AnnotationUtil.hasOverrideAnnotation(ast);
    }

    /**
     * Checks if method or constructor is annotated by ignored annotation(s).
     *
     * @param ast method or constructor definition to check
     * @return true if annotated by ignored annotation(s).
     */
    private boolean isAnnotatedByIgnoredAnnotations(DetailAST ast) {
        return AnnotationUtil.containsAnnotation(ast, ignoreAnnotatedBy);
    }

}
