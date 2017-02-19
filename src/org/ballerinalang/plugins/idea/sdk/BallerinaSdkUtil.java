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

package org.ballerinalang.plugins.idea.sdk;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.Function;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.ballerinalang.plugins.idea.BallerinaConstants;
import org.ballerinalang.plugins.idea.project.BallerinaLibraryService;
import org.ballerinalang.plugins.idea.project.GoApplicationLibrariesService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.util.containers.ContainerUtil.newLinkedHashSet;

public class BallerinaSdkUtil {

    private static final Pattern BALLERINA_VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+.\\d+)");
    private static final Key<String> VERSION_DATA_KEY = Key.create("BALLERINA_VERSION_KEY");

    @Nullable
    public static VirtualFile suggestSdkDirectory() {
        if (SystemInfo.isWindows) {
            return ObjectUtils.chooseNotNull(LocalFileSystem.getInstance().findFileByPath("C:\\Ballerina"), null);
        }
        if (SystemInfo.isMac || SystemInfo.isLinux) {
            String fromEnv = suggestSdkDirectoryPathFromEnv();
            if (fromEnv != null) {
                return LocalFileSystem.getInstance().findFileByPath(fromEnv);
            }
            VirtualFile usrLocal = LocalFileSystem.getInstance().findFileByPath("/usr/local/ballerina");
            if (usrLocal != null) return usrLocal;
        }
        if (SystemInfo.isMac) {
            String macPorts = "/opt/local/lib/ballerina";
            String homeBrew = "/usr/local/Cellar/ballerina";
            File file = FileUtil.findFirstThatExist(macPorts, homeBrew);
            if (file != null) {
                return LocalFileSystem.getInstance().findFileByIoFile(file);
            }
        }
        return null;
    }

    @Nullable
    private static String suggestSdkDirectoryPathFromEnv() {
        File fileFromPath = PathEnvironmentVariableUtil.findInPath("ballerina");
        if (fileFromPath != null) {
            File canonicalFile;
            try {
                canonicalFile = fileFromPath.getCanonicalFile();
                String path = canonicalFile.getPath();
                if (path.endsWith("bin/ballerina")) {
                    return StringUtil.trimEnd(path, "bin/ballerina");
                }
            } catch (IOException ignore) {
            }
        }
        return null;
    }

    @Nullable
    public static String retrieveBallerinaVersion(@NotNull String sdkPath) {
        try {
            VirtualFile sdkRoot = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(sdkPath));
            if (sdkRoot != null) {
                String cachedVersion = sdkRoot.getUserData(VERSION_DATA_KEY);
                if (cachedVersion != null) {
                    return !cachedVersion.isEmpty() ? cachedVersion : null;
                }

                VirtualFile versionFile = sdkRoot.findFileByRelativePath(
                        BallerinaConstants.BALLERINA_VERSION_FILE_PATH);
                // Please note that if the above versionFile is null, we can check on other locations as well.
                if (versionFile != null) {
                    String text = VfsUtilCore.loadText(versionFile);
                    String version = parseBallerinaVersion(text);
                    if (version == null) {
                        BallerinaSdkService.LOG.debug("Cannot retrieve Ballerina version from version file: " + text);
                    }
                    sdkRoot.putUserData(VERSION_DATA_KEY, StringUtil.notNullize(version));
                    return version;
                } else {
                    BallerinaSdkService.LOG.debug("Cannot find Ballerina version file in sdk path: " + sdkPath);
                }
            }
        } catch (IOException e) {
            BallerinaSdkService.LOG.debug("Cannot retrieve Ballerina version from sdk path: " + sdkPath, e);
        }
        return null;
    }

    @Nullable
    public static String parseBallerinaVersion(@NotNull String text) {
        Matcher matcher = BALLERINA_VERSION_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @NotNull
    public static Collection<VirtualFile> getSdkDirectoriesToAttach(@NotNull String sdkPath, @NotNull String
            versionString) {
        return ContainerUtil.createMaybeSingletonList(getSdkSrcDir(sdkPath, versionString));
    }

    @Nullable
    private static VirtualFile getSdkSrcDir(@NotNull String sdkPath, @NotNull String sdkVersion) {
        String srcPath = getSrcLocation(sdkVersion);
        VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(
                VfsUtilCore.pathToUrl(FileUtil.join(sdkPath, srcPath)));
        return file != null && file.isDirectory() ? file : null;
    }

    @NotNull
    private static String getSrcLocation(@NotNull String version) {
        return "src";
    }

    public static String getSdkHome(Project project) {
        Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (sdk == null || sdk.getSdkType() != BallerinaSdkType.getInstance()) {
            return "";
        }
        return sdk.getHomePath();
    }

    public static String getBallerinaExecutablePath(Project project) {
        String sdkHome = getSdkHome(project);
        if (!sdkHome.isEmpty()) {
            return sdkHome + File.separator + "bin/ballerina";
        }
        return "";
    }

    @NotNull
    public static Collection<VirtualFile> getGoPathsRootsFromEnvironment() {
        return BallerinaPathModificationTracker.getGoEnvironmentGoPathRoots();
    }

    @NotNull
    private static List<VirtualFile> getInnerGoPathSources(@NotNull Project project, @Nullable Module module) {
        return ContainerUtil.mapNotNull(getGoPathRoots(project, module), new RetrieveSubDirectoryOrSelfFunction("src"));
    }

    @NotNull
    public static Collection<VirtualFile> getGoPathRoots(@NotNull Project project, @Nullable Module module) {
        Collection<VirtualFile> roots = ContainerUtil.newArrayList();
        if (GoApplicationLibrariesService.getInstance().isUseGoPathFromSystemEnvironment()) {
            roots.addAll(getGoPathsRootsFromEnvironment());
        }
        roots.addAll(module != null ? BallerinaLibraryService.getUserDefinedLibraries(module) :
                BallerinaLibraryService.getUserDefinedLibraries(project));
        return roots;
    }
//
//    @NotNull
//    public static Collection<VirtualFile> getGoPathSources(@NotNull Project project, @Nullable Module module) {
//        if (module != null) {
//            return CachedValuesManager.getManager(project).getCachedValue(module, () -> {
//                Collection<VirtualFile> result = newLinkedHashSet();
//                Project project1 = module.getProject();
//                GoSdkService sdkService = GoSdkService.getInstance(project1);
//                if (sdkService.isAppEngineSdk(module)) {
//                    ContainerUtil.addAllNotNull(result, ContainerUtil.mapNotNull(YamlFilesModificationTracker.getYamlFiles(project1, module),
//                            VirtualFile::getParent));
//                }
//                result.addAll(getInnerGoPathSources(project1, module));
//                return CachedValueProvider.Result
//                        .create(result, getSdkAndLibrariesCacheDependencies(project1, module, YamlFilesModificationTracker.getInstance(project1)));
//            });
//        }
//        return CachedValuesManager.getManager(project).getCachedValue(project,
//                (CachedValueProvider<Collection<VirtualFile>>)() -> CachedValueProvider.Result
//                        .create(getInnerGoPathSources(project, null),
//                                getSdkAndLibrariesCacheDependencies(project, null, YamlFilesModificationTracker.getInstance(project))));
//    }

    private static class RetrieveSubDirectoryOrSelfFunction implements Function<VirtualFile, VirtualFile> {
        @NotNull private final String mySubdirName;

        public RetrieveSubDirectoryOrSelfFunction(@NotNull String subdirName) {
            mySubdirName = subdirName;
        }

        @Override
        public VirtualFile fun(VirtualFile file) {
            return file == null || FileUtil.namesEqual(mySubdirName, file.getName()) ? file : file.findChild(mySubdirName);
        }
    }

//    @NotNull
//    private static Collection<Object> getSdkAndLibrariesCacheDependencies(@NotNull Project project,
//                                                                          @Nullable Module module,
//                                                                          Object... extra) {
//        Collection<Object> dependencies = ContainerUtil.newArrayList((Object[])BallerinaSdkService.getModificationTrackers(project,
//                module));
//        ContainerUtil.addAllNotNull(dependencies, GoSdkService.getInstance(project));
//        ContainerUtil.addAllNotNull(dependencies, extra);
//        return dependencies;
//    }

}
