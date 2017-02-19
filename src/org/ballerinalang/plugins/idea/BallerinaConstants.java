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

import com.intellij.notification.NotificationGroup;
import org.jetbrains.annotations.NonNls;

public class BallerinaConstants {

    private BallerinaConstants() {

    }

    public static final String MODULE_TYPE_ID = "BALLERINA_MODULE";

    @NonNls
    public static final String BALLERINA_EXECUTABLE_NAME = "ballerina";

    @NonNls
    public static final String BALLERINA_VERSION_FILE_PATH = "bin/version.txt";

    public static final NotificationGroup BALLERINA_NOTIFICATION_GROUP =
            NotificationGroup.balloonGroup("Ballerina plugin notifications");

    public static final String BALLERINA_REPOSITORY = "GOPATH";
    public static final String GO_LIBRARIES_SERVICE_NAME = "GoLibraries";
    public static final String GO_LIBRARIES_CONFIG_FILE = "goLibraries.xml";
    public static final String GO_MODULE_SESTTINGS_SERVICE_NAME = "Go";
}
