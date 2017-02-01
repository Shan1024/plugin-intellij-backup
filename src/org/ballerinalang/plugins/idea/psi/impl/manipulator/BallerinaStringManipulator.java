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

package org.ballerinalang.plugins.idea.psi.impl.manipulator;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaFunctionNameImpl;
import org.jetbrains.annotations.NotNull;

public class BallerinaStringManipulator extends AbstractElementManipulator<BallerinaFunctionNameImpl> {
    @Override
    public BallerinaFunctionNameImpl handleContentChange(@NotNull BallerinaFunctionNameImpl element,
                                                         @NotNull TextRange range, String newContent)
            throws IncorrectOperationException {
        return null;
    }
}
