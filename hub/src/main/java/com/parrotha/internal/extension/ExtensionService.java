/**
 * Copyright (c) 2021-2022 by the respective copyright holders.
 * All rights reserved.
 * <p>
 * This file is part of Parrot Home Automation Hub.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.parrotha.internal.extension;

import com.parrotha.internal.common.FileSystemUtils;
import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtensionService {
    private ExtensionDataStore extensionDataStore;

    public ExtensionService() {
        this.extensionDataStore = new ExtensionYamlDataStore();
    }

    public ExtensionService(ExtensionDataStore extensionDataStore) {
        this.extensionDataStore = extensionDataStore;
    }

    public void clearExtensions() {
        this.extensions.clear();
        this.extensions = null;
    }

    private Map<String, Map> extensions;

    public Map getExtension(String id) {
        return getExtensions().get(id);
    }

    public Collection<Map> getExtensionList() {
        return getExtensions().values();
    }

    public synchronized Map<String, Map> getExtensions() {
        if (extensions == null) {
            extensions = loadExtensions();
        }
        return extensions;
    }

    public List getExtensionSettings() {
        return extensionDataStore.getExtensionSettings();
    }

    public String addSetting(String name, String type, String location) {
        return extensionDataStore.addSetting(name, type, location);
    }

    public boolean updateSetting(String id, String name, String type, String location) {
        return extensionDataStore.updateSetting(id, name, type, location);
    }

    public boolean downloadExtension(String id) {
        Map extension = getExtension(id);
        for (String key : ((Map<String, Object>) extension).keySet()) {
            System.out.println("key: [" + key + "] value: [" + extension.get(key) + "]");
        }
        //TODO: handle other extension types
        File file = new File("./extensions/.extensions/" + id + "/githubReleaseInformation.json");
        if (file.exists()) {
            Object githubInfoObject = new JsonSlurper().parse(file);
            if (githubInfoObject instanceof Map) {
                Map githubInfo = (Map) githubInfoObject;
                List<Map> assetList = (List<Map>) githubInfo.get("assets");

                FileSystemUtils.createDirectory("./extensions/" + id);

                for (Map asset : assetList) {
                    String assetName = (String) asset.get("name");
                    if (!"extensionInformation.yaml".equals(assetName) && !"integrationInformation.yaml".equals(assetName)) {
                        String extFileUrlStr = (String) asset.get("browser_download_url");
                        try {
                            FileUtils.copyURLToFile(new URL(extFileUrlStr), new File("./extensions/" + id + "/" + assetName));

                            // extract file if zip
                            if (assetName.endsWith(".zip")) {
                                FileSystemUtils.unzipFile("./extensions/" + id + "/" + assetName, "./extensions/" + id);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return false;
    }

    public List refreshExtensionList() {
        FileSystemUtils.createDirectory("./extensions");
        FileSystemUtils.createDirectory("./extensions/.extensions");

        List<Map> extLocs = getExtensionSettings();

        for (Map extLoc : extLocs) {
            String type = (String) extLoc.get("type");
            String location = (String) extLoc.get("location");

            if (type.equalsIgnoreCase("GithubRelease")) {
                try {
                    String url = "https://api.github.com/repos/" + location + "/releases/latest";
                    URL github = new URL(url);
                    String githubResponse = IOUtils.toString(github, "UTF8");

                    Map parsedData = (Map) new JsonSlurper().parseText(githubResponse);
                    List<Map> assetList = (List<Map>) parsedData.get("assets");

                    for (Map asset : assetList) {
                        String assetName = (String) asset.get("name");
                        if ("extensionInformation.yaml".equals(assetName)) {
                            String extInfUrlStr = (String) asset.get("browser_download_url");
                            String extInfStr = IOUtils.toString(new URL(extInfUrlStr), "UTF8");
                            Yaml yaml = new Yaml();
                            Map extensionInformation = yaml.load(extInfStr);
                            String extensionId = (String) extensionInformation.get("id");

                            FileSystemUtils.createDirectory("./extensions/.extensions/" + extensionId);

                            File file = new File("./extensions/.extensions/" + extensionId + "/extensionInformation.yaml");
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(extInfStr.getBytes(StandardCharsets.UTF_8));
                            fos.close();

                            file = new File("./extensions/.extensions/" + extensionId + "/githubReleaseInformation.json");
                            fos = new FileOutputStream(file);
                            fos.write(new JsonBuilder(parsedData).toPrettyString().getBytes(StandardCharsets.UTF_8));
                            fos.close();
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }


    private synchronized Map<String, Map> loadExtensions() {
        // load extensions from file system
        File extensionDirectory = new File("./extensions");
        if (!extensionDirectory.exists()) {
            extensionDirectory.mkdir();
        }
        File extDirs[] = extensionDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });

        Map<String, Map> tmpExtensions = new HashMap<>();
        for (File extDir : extDirs) {
            tmpExtensions.putAll(loadJarFiles(extDir));
        }

        // load extensions from configuration directory
        File availableExtensionDirectory = new File("./extensions/.extensions");
        if (availableExtensionDirectory.isDirectory()) {
            File avExtDirs[] = availableExtensionDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });

            for (File extDir : avExtDirs) {
                File[] extInfFiles = extDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        return "extensionInformation.yaml".equals(s);
                    }
                });

                if (extInfFiles.length > 0) {
                    File extInfFile = extInfFiles[0];
                    Yaml yaml = new Yaml();
                    try {
                        Map extInf = yaml.load(new FileInputStream(extInfFile));
                        String extInfId = (String) extInf.get("id");
                        if (tmpExtensions.containsKey(extInfId)) {
                            Map extension = tmpExtensions.get(extInfId);
                            if (!StringUtils.equals((String) extension.get("version"), (String) extInf.get("version"))) {
                                extension.put("updateAvailable", true);
                                extension.put("updateInfo", extInf);
                            } else {
                                extension.put("updateAvailable", false);
                            }
                        } else {
                            extInf.put("installed", false);
                            tmpExtensions.put((String) extInf.get("id"), extInf);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return tmpExtensions;
    }

    public Map<String, Map> loadJarFiles(File extDir) {
        Map<String, Map> extensions = new HashMap<>();

        File additionalDirectories[] = extDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        // recurse through directories
        for (File addDir : additionalDirectories) {
            extensions.putAll(loadJarFiles(addDir));
        }

        ClassLoader myClassLoader = FileSystemUtils.getClassloaderForJarFiles(extDir);
        if (myClassLoader != null) {
            extensions.putAll(getExtensionFromClassloader(myClassLoader, extDir.getPath()));
        }

        return extensions;
    }

    private Map<String, Map> getExtensionFromClassloader(ClassLoader classLoader, String extensionDirectory) {
        Map<String, Map> extensions = new HashMap<>();
        try {
            Enumeration<URL> resources = classLoader.getResources("extensionInformation.yaml");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Yaml yaml = new Yaml();
                Map extensionInformation = yaml.load(url.openStream());
                String id = (String) extensionInformation.get("id");
                String name = (String) extensionInformation.get("name");
                String description = (String) extensionInformation.get("description");
                String version = (String) extensionInformation.get("version");
                // create mutable map
                extensions.put(id,
                        new HashMap<>(Map.of("id", id, "name", name, "description", description, "location", extensionDirectory, "version", version,
                                "installed", true)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extensions;
    }
}
