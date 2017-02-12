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

package org.ballerinalang.plugins.idea.codeinsight.smartenter;

import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.codeInsight.editorActions.smartEnter.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiForStatement;
import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class BallerinaSmartEnterProcessor extends SmartEnterProcessor {

    private static final EnterProcessor[] ourAfterCompletionEnterProcessors = {
            new EnterProcessor() {
                @Override
                public boolean doEnter(Editor editor, PsiElement psiElement, boolean isModified) {
                    return PlainEnterProcessor.expandCodeBlock(editor, psiElement);
                }
            }
    };

    private int myFirstErrorOffset = Integer.MAX_VALUE;
    private boolean mySkipEnter;
    private static final int MAX_ATTEMPTS = 20;
    private static final Key<Long> SMART_ENTER_TIMESTAMP = Key.create("smartEnterOriginalTimestamp");

    @Override
    public boolean process(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
        return invokeProcessor(editor, psiFile, false);
    }

    @Override
    public boolean processAfterCompletion(@NotNull Editor editor, @NotNull PsiFile psiFile) {
        return invokeProcessor(editor, psiFile, true);
    }

    private boolean invokeProcessor(Editor editor, PsiFile psiFile, boolean afterCompletion) {
        final Document document = editor.getDocument();
        final CharSequence textForRollback = document.getImmutableCharSequence();
        try {
            editor.putUserData(SMART_ENTER_TIMESTAMP, editor.getDocument().getModificationStamp());
            myFirstErrorOffset = Integer.MAX_VALUE;
            mySkipEnter = false;
            process(editor, psiFile, 0, afterCompletion);
        }
        catch (JavaSmartEnterProcessor.TooManyAttemptsException e) {
            document.replaceString(0, document.getTextLength(), textForRollback);
        } finally {
            editor.putUserData(SMART_ENTER_TIMESTAMP, null);
        }
        return true;
    }

    private void process(@NotNull final Editor editor, @NotNull final PsiFile file, final int attempt, boolean afterCompletion) throws JavaSmartEnterProcessor.TooManyAttemptsException {
        if (attempt > MAX_ATTEMPTS) throw new JavaSmartEnterProcessor.TooManyAttemptsException();

        try {
            commit(editor);
            if (myFirstErrorOffset != Integer.MAX_VALUE) {
                editor.getCaretModel().moveToOffset(myFirstErrorOffset);
            }

            myFirstErrorOffset = Integer.MAX_VALUE;

            PsiElement atCaret = getStatementAtCaret(editor, file);
            if (atCaret == null) {
//                if (myJavadocFixer.process(editor, file)) {
//                    return;
//                }

//                if (!new CommentBreakerEnterProcessor().doEnter(editor, file, false)) {
//                    plainEnter(editor);
//                }
                return;
            }

//            List<PsiElement> queue = new ArrayList<>();
//            collectAllElements(atCaret, queue, true);
//            queue.add(atCaret);

//            for (PsiElement psiElement : queue) {
//                for (Fixer fixer : ourFixers) {
//                    fixer.apply(editor, this, psiElement);
//                    if (LookupManager.getInstance(file.getProject()).getActiveLookup() != null) {
//                        return;
//                    }
//                    if (isUncommited(file.getProject()) || !psiElement.isValid()) {
//                        moveCaretInsideBracesIfAny(editor, file);
//                        process(editor, file, attempt + 1, afterCompletion);
//                        return;
//                    }
//                }
//            }

            doEnter(atCaret, editor, afterCompletion);
        }
        catch (IncorrectOperationException e) {
//            LOG.error(e);
        }
    }

    @Override
    protected void reformat(PsiElement atCaret) throws IncorrectOperationException {
        if (atCaret == null) {
            return;
        }
        PsiElement parent = atCaret.getParent();
        if (parent instanceof PsiForStatement) {
            atCaret = parent;
        }

        if (parent instanceof PsiIfStatement && atCaret == ((PsiIfStatement)parent).getElseBranch()) {
            PsiFile file = atCaret.getContainingFile();
            Document document = file.getViewProvider().getDocument();
            if (document != null) {
                TextRange elseIfRange = atCaret.getTextRange();
                int lineStart = document.getLineStartOffset(document.getLineNumber(elseIfRange.getStartOffset()));
                CodeStyleManager.getInstance(atCaret.getProject()).reformatText(file, lineStart, elseIfRange.getEndOffset());
                return;
            }
        }

        super.reformat(atCaret);
    }

    private void doEnter(PsiElement atCaret, Editor editor, boolean afterCompletion) throws
            IncorrectOperationException {
        final PsiFile psiFile = atCaret.getContainingFile();

        if (myFirstErrorOffset != Integer.MAX_VALUE) {
            editor.getCaretModel().moveToOffset(myFirstErrorOffset);
            reformat(atCaret);
            return;
        }

        final RangeMarker rangeMarker = createRangeMarker(atCaret);
        reformat(atCaret);
        commit(editor);

        if (!mySkipEnter) {
            atCaret = CodeInsightUtil.findElementInRange(psiFile, rangeMarker.getStartOffset(), rangeMarker
                    .getEndOffset(), atCaret.getClass());
            //            for (EnterProcessor processor : afterCompletion ? ourAfterCompletionEnterProcessors :
            // ourEnterProcessors) {
            for (EnterProcessor processor : ourAfterCompletionEnterProcessors) {
                if (atCaret == null) {
                    // Can't restore element at caret after enter processor execution!
                    break;
                }

                //                if (processor.doEnter(editor, atCaret, isModified(editor))) {
                //                    rangeMarker.dispose();
                //                    return;
                //                }
            }

            //            if (!isModified(editor) && !afterCompletion) {
            //                plainEnter(editor);
            //            } else {
            if (myFirstErrorOffset == Integer.MAX_VALUE) {
                editor.getCaretModel().moveToOffset(rangeMarker.getEndOffset());
            } else {
                editor.getCaretModel().moveToOffset(myFirstErrorOffset);
            }
            //            }
        }
        rangeMarker.dispose();
    }
}
