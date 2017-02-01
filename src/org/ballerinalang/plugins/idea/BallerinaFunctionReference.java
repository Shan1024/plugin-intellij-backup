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

package org.ballerinalang.plugins.idea;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import org.ballerinalang.plugins.idea.psi.BallerinaFunctionName;
import org.ballerinalang.plugins.idea.util.BallerinaUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BallerinaFunctionReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    public BallerinaFunctionReference(PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Project project = myElement.getProject();
        final List<BallerinaFunctionName> functionNames = BallerinaUtil.findFunctionName(project,
                getElement().getText());
        List<ResolveResult> results = new ArrayList<ResolveResult>();
        for (BallerinaFunctionName functionName : functionNames) {
            results.add(new PsiElementResolveResult(functionName));
        }
        return results.toArray(new ResolveResult[results.size()]);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    //    @Override
    //    public boolean isReferenceTo(PsiElement element) {
    //        if (element == BallerinaTypes.FUNCTION_NAME) {
    //            return Objects.equals(element.getNode().getText(), getElement().getText());
    //        }
    //        return super.isReferenceTo(element);
    //    }
}
