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

package org.ballerinalang.plugins.idea.project;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import org.ballerinalang.plugins.idea.BallerinaConstants;
import org.ballerinalang.plugins.idea.sdk.BallerinaSdkUtil;
import org.jetbrains.annotations.NotNull;

@State(
        name = BallerinaConstants.GO_LIBRARIES_SERVICE_NAME,
        storages = @Storage(file = StoragePathMacros.APP_CONFIG + "/" + BallerinaConstants.GO_LIBRARIES_CONFIG_FILE)
)
public class GoApplicationLibrariesService extends BallerinaLibraryService<GoApplicationLibrariesService.GoApplicationLibrariesState> {
    @NotNull
    @Override
    protected GoApplicationLibrariesState createState() {
        return new GoApplicationLibrariesState();
    }

    public static GoApplicationLibrariesService getInstance() {
        return ServiceManager.getService(GoApplicationLibrariesService.class);
    }

    public boolean isUseGoPathFromSystemEnvironment() {
        return myState.isUseGoPathFromSystemEnvironment();
    }

    public void setUseGoPathFromSystemEnvironment(boolean useGoPathFromSystemEnvironment) {
        if (myState.isUseGoPathFromSystemEnvironment() != useGoPathFromSystemEnvironment) {
            myState.setUseGoPathFromSystemEnvironment(useGoPathFromSystemEnvironment);
            if (!BallerinaSdkUtil.getGoPathsRootsFromEnvironment().isEmpty()) {
                incModificationCount();
                ApplicationManager.getApplication().getMessageBus().syncPublisher(LIBRARIES_TOPIC).librariesChanged(getLibraryRootUrls());
            }
        }
    }

    public static class GoApplicationLibrariesState extends BallerinaLibraryState {
        private boolean myUseGoPathFromSystemEnvironment = true;

        public boolean isUseGoPathFromSystemEnvironment() {
            return myUseGoPathFromSystemEnvironment;
        }

        public void setUseGoPathFromSystemEnvironment(boolean useGoPathFromSystemEnvironment) {
            myUseGoPathFromSystemEnvironment = useGoPathFromSystemEnvironment;
        }
    }
}
