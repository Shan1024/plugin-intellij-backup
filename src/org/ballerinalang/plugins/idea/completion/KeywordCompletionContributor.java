/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.completion;

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.DumbAware;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.ProcessingContext;
import org.ballerinalang.plugins.idea.psi.CompilationUnitNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.LiteralValueNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.SimpleTypeNode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class KeywordCompletionContributor extends CompletionContributor implements DumbAware {

    static final LookupElement PACKAGE;
    static final LookupElement IMPORT;
    static final LookupElement CONST;
    static final LookupElement SERVICE;
    static final LookupElement FUNCTION;
    static final LookupElement CONNECTOR;
    static final LookupElement STRUCT;
    static final LookupElement TYPECONVERTER;

    static final LookupElement BOOLEAN;
    static final LookupElement INT;
    static final LookupElement LONG;
    static final LookupElement FLOAT;
    static final LookupElement DOUBLE;
    static final LookupElement STRING;
    static final LookupElement MESSAGE;
    static final LookupElement MAP;
    static final LookupElement EXCEPTION;

    static {
        PACKAGE = createKeywordLookupElement("package", true, AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        IMPORT = createKeywordLookupElement("import", true, AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        CONST = createKeywordLookupElement("const", true, AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        SERVICE = createKeywordLookupElement("service", true, AddSpaceInsertHandler.INSTANCE);
        FUNCTION = createKeywordLookupElement("function", true, AddSpaceInsertHandler.INSTANCE);
        CONNECTOR = createKeywordLookupElement("connector", true, AddSpaceInsertHandler.INSTANCE);
        STRUCT = createKeywordLookupElement("struct", true, AddSpaceInsertHandler.INSTANCE);
        TYPECONVERTER = createKeywordLookupElement("typeconverter", true, AddSpaceInsertHandler.INSTANCE);

        BOOLEAN = createLookupElement("boolean", true, AddSpaceInsertHandler.INSTANCE);
        INT = createLookupElement("int", true, AddSpaceInsertHandler.INSTANCE);
        LONG = createLookupElement("long", true, AddSpaceInsertHandler.INSTANCE);
        FLOAT = createLookupElement("float", true, AddSpaceInsertHandler.INSTANCE);
        DOUBLE = createLookupElement("double", true, AddSpaceInsertHandler.INSTANCE);
        STRING = createLookupElement("string", true, AddSpaceInsertHandler.INSTANCE);
        MESSAGE = createLookupElement("message", true, AddSpaceInsertHandler.INSTANCE);
        MAP = createLookupElement("map", true, AddSpaceInsertHandler.INSTANCE);
        EXCEPTION = createLookupElement("exception", true, AddSpaceInsertHandler.INSTANCE);
    }

    private static LookupElementBuilder createLookupElement(String name, boolean withBoldness,
                                                            InsertHandler insertHandler) {
        return LookupElementBuilder.create(name).withBoldness(withBoldness).withInsertHandler(insertHandler);
    }

    private static LookupElement createKeywordLookupElement(String name, boolean withBoldness,
                                                            InsertHandler insertHandler) {
        return createLookupElement(name, withBoldness, insertHandler).withTypeText("Keyword");
    }

    public KeywordCompletionContributor() {
        //        extend(CompletionType.BASIC,
        //                PlatformPatterns.psiElement().withLanguage(BallerinaLanguage.INSTANCE),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("ZZZZZZ"));
        //                    }
        //                }
        //        );
        //        extend(CompletionType.BASIC,
        //                PlatformPatterns.psiElement().withParent(CompilationUnitNode.class).withLanguage
        // (BallerinaLanguage
        //                        .INSTANCE),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("YYYYYY"));
        //                    }
        //                }
        //        );
        //        extend(CompletionType.BASIC,
        //                PlatformPatterns.psiElement().withParent(FunctionDefinitionNode.class).withLanguage
        // (BallerinaLanguage
        //                        .INSTANCE),
        //                new CompletionProvider<CompletionParameters>() {
        //                    public void addCompletions(@NotNull CompletionParameters parameters,
        //                                               ProcessingContext context,
        //                                               @NotNull CompletionResultSet resultSet) {
        //                        resultSet.addElement(LookupElementBuilder.create("XXXXXXX"));
        //                    }
        //                }
        //        );

        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new CompletionProvider<CompletionParameters>() {
                    public void addCompletions(@NotNull CompletionParameters parameters,
                                               ProcessingContext context,
                                               @NotNull CompletionResultSet resultSet) {

                        //                        PsiElement originalPosition = parameters.getOriginalPosition();
                        //
                        //                        if (originalPosition == null) {
                        setKeywordSuggestions(parameters.getPosition(), resultSet);
                        //                        } else {
                        //                            setKeywordSuggestions(originalPosition, resultSet);
                        //                        }


                        //                        if (originalPosition != null) {
                        //                            PsiElement parent = originalPosition.getParent();
                        //                            if (parent instanceof PsiFile) {
                        //                                String[] keywords = new String[]{"package", "import",
                        // "service", "function",
                        //                                        "connector", "struct", "typeconverter", "const"};
                        //                                for (String keyword : keywords) {
                        //                                    resultSet.addElement(LookupElementBuilder.create
                        // (keyword));
                        //                                }
                        //                                resultSet.addElement(LookupElementBuilder.create("XX file"));
                        //                            } else if (parent instanceof CompilationUnitNode) {
                        //                                resultSet.addElement(LookupElementBuilder.create("XX comp"));
                        //                            } else if (parent instanceof PsiErrorElement) {
                        //
                        //                                String[] keywords = new String[]{"package", "import",
                        // "service", "function",
                        //                                        "connector", "struct", "typeconverter", "const"};
                        //                                for (String keyword : keywords) {
                        //                                    resultSet.addElement(LookupElementBuilder.create
                        // (keyword).withBoldness(true)
                        //                                            .withInsertHandler(AddSpaceInsertHandler
                        // .INSTANCE_WITH_AUTO_POPUP));
                        //                                }
                        //
                        //                                resultSet.addElement(LookupElementBuilder.create("XX error"));
                        //                                PsiElement prevSibling = originalPosition.getPrevSibling();
                        //                                //                                if (prevSibling.getText()
                        // .equals("import")) {
                        //                                //                                    Editor editor =
                        // context.getEditor();
                        //                                //
                        // EditorModificationUtil.insertStringAtCaret
                        //                                // (editor, " ");
                        //                                //                                    PsiDocumentManager
                        // .getInstance(project)
                        //                                // .commitDocument(editor.getDocument());
                        //                                //                                }
                        //                            } else {
                        //                                resultSet.addElement(LookupElementBuilder.create("XX other"));
                        //                            }
                        //                        } else {
                        //                            PsiElement parent = parameters.getPosition().getParent();
                        //                            if (parent instanceof PsiFile) {
                        //                                resultSet.addElement(LookupElementBuilder.create("YYY file"));
                        //                            } else if (parent instanceof CompilationUnitNode) {
                        //                                resultSet.addElement(LookupElementBuilder.create("YYY comp"));
                        //                            } else if (parent instanceof PsiErrorElement) {
                        //
                        //                                PsiElement prevSibling = parameters.getPosition().getParent
                        // ().getPrevSibling();
                        //
                        //                                if (prevSibling != null) {
                        //
                        //                                    if (prevSibling instanceof ImportDeclarationNode
                        //                                            || prevSibling instanceof
                        // PackageDeclarationNode) {
                        //                                        String[] keywords = new String[]{"import", "const",
                        // "service",
                        //                                                "function", "connector", "struct",
                        // "typeconverter"};
                        //                                        for (String keyword : keywords) {
                        //                                            resultSet.addElement(LookupElementBuilder
                        // .create(keyword).withBoldness(true)
                        //                                                    .withInsertHandler
                        // (AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP));
                        //                                        }
                        //                                        resultSet.addElement(LookupElementBuilder.create
                        // ("YYY error 1.1"));
                        //                                    } else {
                        //                                        String[] keywords = new String[]{"const", "service",
                        //                                                "function", "connector", "struct",
                        // "typeconverter"};
                        //                                        for (String keyword : keywords) {
                        //                                            resultSet.addElement(LookupElementBuilder
                        // .create(keyword).withBoldness(true)
                        //                                                    .withInsertHandler
                        // (AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP));
                        //                                        }
                        //                                        resultSet.addElement(LookupElementBuilder.create
                        // ("YYY error 1.2"));
                        //                                    }
                        //
                        //                                } else {
                        //                                    String[] keywords = new String[]{"package", "import",
                        // "const", "service",
                        //                                            "function", "connector", "struct",
                        // "typeconverter"};
                        //                                    for (String keyword : keywords) {
                        //                                        resultSet.addElement(LookupElementBuilder.create
                        // (keyword).withBoldness(true)
                        //                                                .withInsertHandler(AddSpaceInsertHandler
                        // .INSTANCE_WITH_AUTO_POPUP));
                        //                                    }
                        //                                    resultSet.addElement(LookupElementBuilder.create("YYY
                        // error 2"));
                        //                                }
                        //
                        //
                        //                            } else {
                        //                                resultSet.addElement(LookupElementBuilder.create("YYY
                        // other"));
                        //                            }
                        //                        }

                    }
                }
        );
    }


    private void setKeywordSuggestions(PsiElement element, CompletionResultSet resultSet) {

        PsiElement parent = element.getParent();
        PsiElement parentPrevSibling = parent.getPrevSibling();
        PsiElement prevSibling = element.getPrevSibling();

        if (parent instanceof LiteralValueNode) {
            return;
        }
        //Todo - Add literal value node, service definition
        if (parent instanceof PsiFile) {
            resultSet.addElement(PACKAGE);
            resultSet.addElement(IMPORT);
            resultSet.addElement(CONST);
            resultSet.addElement(SERVICE);
            resultSet.addElement(FUNCTION);
            resultSet.addElement(CONNECTOR);
            resultSet.addElement(STRUCT);
            resultSet.addElement(TYPECONVERTER);
        } else if (parentPrevSibling instanceof ImportDeclarationNode || parentPrevSibling instanceof
                PackageDeclarationNode) {
            resultSet.addElement(IMPORT);
            resultSet.addElement(CONST);
            resultSet.addElement(SERVICE);
            resultSet.addElement(FUNCTION);
            resultSet.addElement(CONNECTOR);
            resultSet.addElement(STRUCT);
            resultSet.addElement(TYPECONVERTER);
            resultSet.addElement(LookupElementBuilder.create("YYY error 1.1"));
        } else {

            if (parent instanceof PackageNameNode) {
                //Todo - Suggest current file path
                PsiDirectory[] psiDirectories = BallerinaPsiImplUtil.suggestPackages(element);
                for (PsiDirectory directory : psiDirectories) {
                    InsertHandler insertHandler;
                    if (BallerinaPsiImplUtil.hasSubdirectories(directory)) {
                        insertHandler = ImportCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP;
                    } else {
                        insertHandler = StatementCompletionInsertHandler.INSTANCE;
                    }
                    resultSet.addElement(LookupElementBuilder.create(directory).withInsertHandler(insertHandler));
                }
            } else if (parent instanceof ImportDeclarationNode) {
                PsiDirectory[] psiDirectories = BallerinaPsiImplUtil.suggestPackages(element);
                for (PsiDirectory directory : psiDirectories) {
                    InsertHandler insertHandler;
                    if (BallerinaPsiImplUtil.hasSubdirectories(directory)) {
                        insertHandler = ImportCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP;
                    } else {
                        insertHandler = StatementCompletionInsertHandler.INSTANCE;
                    }
                    resultSet.addElement(LookupElementBuilder.create(directory).withInsertHandler(insertHandler));
                }
            } else if (parent instanceof PsiErrorElement) {

                PsiElement superParent = parent.getParent();
                //Todo - add throws keyword

                if (superParent instanceof StatementNode) {
                    resultSet.addElement(LookupElementBuilder.create("YYY error 2.1"));
                } else if (superParent instanceof CompilationUnitNode) {
                    if (parentPrevSibling == null) {
                        resultSet.addElement(PACKAGE);
                    }

                    //Todo - move to util
                    PsiElement nonWhitespaceElement = parent.getPrevSibling();
                    while (nonWhitespaceElement != null && nonWhitespaceElement instanceof PsiWhiteSpace) {
                        nonWhitespaceElement = nonWhitespaceElement.getPrevSibling();
                    }
                    if (nonWhitespaceElement != null) {

                        if (nonWhitespaceElement instanceof ImportDeclarationNode
                                || nonWhitespaceElement instanceof PackageDeclarationNode) {
                            resultSet.addElement(IMPORT);
                        }
                    } else {
                        resultSet.addElement(IMPORT);
                    }

                    resultSet.addElement(CONST);
                    resultSet.addElement(SERVICE);
                    resultSet.addElement(FUNCTION);
                    resultSet.addElement(CONNECTOR);
                    resultSet.addElement(STRUCT);
                    resultSet.addElement(TYPECONVERTER);
                } else {
                    if (parentPrevSibling == null) {
                        resultSet.addElement(PACKAGE);
                    }
                    resultSet.addElement(IMPORT);

                    resultSet.addElement(CONST);
                    resultSet.addElement(SERVICE);
                    resultSet.addElement(FUNCTION);
                    resultSet.addElement(CONNECTOR);
                    resultSet.addElement(STRUCT);
                    resultSet.addElement(TYPECONVERTER);
                    resultSet.addElement(LookupElementBuilder.create("YYY error 2.2"));
                }

            } else if (parent instanceof FunctionDefinitionNode || parent instanceof ParameterNode) {

                if (prevSibling != null) {
                    if ("(".equals(prevSibling.getText())) {
                        resultSet.addElement(LookupElementBuilder.create("YYY error 3.1"));
                    } else {
                        //Todo - check type
                        //                        if(){
                        //
                        //                        }else{
                        //
                        //                        }
                        resultSet.addElement(LookupElementBuilder.create("YYY error 3.2"));
                    }
                } else {
                    resultSet.addElement(LookupElementBuilder.create("YYY error 3.3"));
                }
            } else if (parent instanceof SimpleTypeNode) {
                //                resultSet.addElement(LookupElementBuilder.create("YYY error 1.3"));

                resultSet.addElement(BOOLEAN);
                resultSet.addElement(INT);
                resultSet.addElement(LONG);
                resultSet.addElement(FLOAT);
                resultSet.addElement(DOUBLE);
                resultSet.addElement(STRING);
                resultSet.addElement(MESSAGE);
                resultSet.addElement(MAP);
                resultSet.addElement(EXCEPTION);

            } else {

                if (element instanceof IdentifierPSINode) {
                    return;
                }

                if (parentPrevSibling == null) {
                    resultSet.addElement(PACKAGE);
                }
                resultSet.addElement(IMPORT);

                resultSet.addElement(CONST);
                resultSet.addElement(SERVICE);
                resultSet.addElement(FUNCTION);
                resultSet.addElement(CONNECTOR);
                resultSet.addElement(STRUCT);
                resultSet.addElement(TYPECONVERTER);
                resultSet.addElement(LookupElementBuilder.create("YYY error 1.2"));
            }
        }
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return typeChar == ':';
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        super.fillCompletionVariants(parameters, result);
    }
}
