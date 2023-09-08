/*
 * SonarQube Scanner for Jenkins
 * Copyright (C) 2007-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.scanner.jenkins.pipeline.model;

import java.util.Map;

/**
 * @author caozhensheng
 * @date 2023/9/5
 */

public class SonarScannerResult {
    private Long buildId;

    private String projectKey;

    private String projectName;

    private String gitRepository;

    private String branch;

    private String sonarHostUrl;

    private String sources;

    private String exclusions;

    private Map<String,Integer> bugCount;

    private Map<String,Integer> vulCount;
   /* private Integer bugMajorCount;

    private Integer bugMinorCount;

    private Integer bugCriticalCount;

    private Integer bugInfoCount;

    private Integer bugBlockerCount;

    private Integer vulMajorCount;

    private Integer vulMinorCount;

    private Integer vulCriticalCount;

    private Integer vulInfoCount;

    private Integer vulBlockerCount;*/

    public SonarScannerResult() {
    }

    public Long getBuildId() {
        return buildId;
    }

    public void setBuildId(Long buildId) {
        this.buildId = buildId;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getGitRepository() {
        return gitRepository;
    }

    public void setGitRepository(String gitRepository) {
        this.gitRepository = gitRepository;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getSonarHostUrl() {
        return sonarHostUrl;
    }

    public void setSonarHostUrl(String sonarHostUrl) {
        this.sonarHostUrl = sonarHostUrl;
    }

    public String getSources() {
        return sources;
    }

    public void setSources(String sources) {
        this.sources = sources;
    }

    public String getExclusions() {
        return exclusions;
    }

    public void setExclusions(String exclusions) {
        this.exclusions = exclusions;
    }

    public Map<String, Integer> getBugCount() {
        return bugCount;
    }

    public void setBugCount(Map<String, Integer> bugCount) {
        this.bugCount = bugCount;
    }

    public Map<String, Integer> getVulCount() {
        return vulCount;
    }

    public void setVulCount(Map<String, Integer> vulCount) {
        this.vulCount = vulCount;
    }
}
