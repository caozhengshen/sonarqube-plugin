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
package hudson.plugins.sonar;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.google.common.annotations.VisibleForTesting;
import hudson.*;
import hudson.model.*;
import hudson.plugins.sonar.action.SonarMarkerAction;
import hudson.plugins.sonar.utils.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

/**
 * @since 1.7
 */

public class SonarRunnerBuilder extends Builder implements SimpleBuildStep {

  private Long buildNumber;
  private String exclusions;
  private String scannerDir;
  private String hostUrl;
  private String credentialsId;
  private String projectKey;
  private String sources;
  private String gitRepoUrl;
  private String branch;
  @DataBoundConstructor
  public SonarRunnerBuilder() {
    // all fields are optional
  }

  /**
   * @deprecated We're moving to using @DataBoundSetter instead and a much leaner @DataBoundConstructor
   */
  @Deprecated
  public SonarRunnerBuilder(String scannerDir, String exclusions, String hostUrl, String credentialsId, Long buildNumber) {
    this.scannerDir = scannerDir;
    this.exclusions = exclusions;
    this.hostUrl = hostUrl;
    this.credentialsId = credentialsId;
    this.buildNumber = buildNumber;
  }

  public String getExclusions() {
    return exclusions;
  }
  @DataBoundSetter
  public void setExclusions(String exclusions) {
    this.exclusions = exclusions;
  }

  public String getScannerDir() {
    return scannerDir;
  }
  @DataBoundSetter
  public void setScannerDir(String scannerDir) {
    this.scannerDir = scannerDir;
  }

  public String getHostUrl() {
    return hostUrl;
  }
  @DataBoundSetter
  public void setHostUrl(String hostUrl) {
    this.hostUrl = hostUrl;
  }

  public String getCredentialsId() {
    return credentialsId;
  }
  @DataBoundSetter
  public void setCredentialsId(String credentialsId) {
    this.credentialsId = credentialsId;
  }


  public Long getBuildNumber() {
    return buildNumber;
  }
  @DataBoundSetter
  public void setBuildNumber(Long buildNumber) {
    this.buildNumber = buildNumber;
  }

  public String getProjectKey() {
    return projectKey;
  }

  public void setProjectKey(String projectKey) {
    this.projectKey = projectKey;
  }

  public String getSources() {
    return sources;
  }

  public void setSources(String sources) {
    this.sources = sources;
  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  public SonarRunnerInstallation getSonarRunnerInstallation() {
    // If no installation match then take the first one
    if (getDescriptor().getSonarRunnerInstallations().length > 0) {
      return getDescriptor().getSonarRunnerInstallations()[0];
    }
    return null;
  }

  @Override
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
    FilePath workspace = build.getWorkspace();
    if (workspace == null) {
      throw new AbortException("no workspace for " + build);
    }
    perform(build, workspace, launcher, listener);
    return true;
  }

  @Override
  public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
    if(!isVaild(run,listener)){
      throw new AbortException("Invalid property ");
    }
    ArgumentListBuilder args = new ArgumentListBuilder();

    EnvVars env = BuilderUtils.getEnvAndBuildVars(run, listener);

    SonarRunnerInstallation sri = getSonarRunnerInstallation();
    if (sri == null) {
      // No idea if the path contains old sonar-runner or new sonar-scanner, so prefer the new one
      args.add(launcher.isUnix() ? "sonar-scanner" : "sonar-scanner.bat");
    } else {
      sri = BuilderUtils.getBuildTool(sri, env, listener, workspace);
      String exe = sri.getExecutable(launcher);
      if (exe == null) {
        Logger.printFailureMessage(listener);
        String msg = Messages.SonarScanner_ExecutableNotFound(sri.getName());
        listener.fatalError(msg);
        throw new AbortException(msg);
      }
      args.add(exe);
    }

    ExtendedArgumentListBuilder argsBuilder = new ExtendedArgumentListBuilder(args, launcher.isUnix());
    populateConfiguration(argsBuilder, run, workspace, listener, env);

    long startTime = System.currentTimeMillis();
    int exitCode;
    try {
      exitCode = executeSonarQubeScanner(run, workspace, launcher, listener, args, env);
    } catch (IOException e) {
      handleErrors(listener, sri, startTime, e);
      exitCode = -1;
    }

    // with workflows, we don't have realtime access to build logs, so url might be null
    // if the analyis doesn't succeed, it will also be null
    SonarInstallation sonarInst = new SonarInstallation("sonarqube",StringUtils.removeEnd(hostUrl,"/"),credentialsId);

    SonarUtils.addBuildInfoTo(run, listener, workspace, sonarInst);
    SonarUtils.addScannerParams(run,hostUrl,branch,gitRepoUrl,exclusions,sources,projectKey,buildNumber);
    if (exitCode != 0) {
      throw new AbortException("SonarQube scanner exited with non-zero code: " + exitCode);
    }
  }

  private boolean isVaild(Run<?, ?> build,TaskListener listener) {
    if(StringUtils.isBlank(this.getHostUrl())){
      listener.fatalError("HostUrl property is empty ");
      return false;
    }
    if(StringUtils.isBlank(this.getCredentialsId()) || build == null){
      listener.fatalError("CredentialsId property is empty ");
      return false;
    }
    StandardUsernamePasswordCredentials credential = CredentialsProvider.findCredentialById(this.getCredentialsId(), StandardUsernamePasswordCredentials.class, build);
    if(credential == null){
      listener.fatalError(Messages.Credentials_NoFound(this.getCredentialsId()));
      return false;
    }
    String username = credential.getUsername();
    String password = credential.getPassword().getPlainText();
    if(StringUtils.isBlank(username) || StringUtils.isBlank(password)){
      listener.fatalError("The account or password of the credential with id '" + this.getCredentialsId() + "'"+" is empty");
    }
    return true;
  }

  private void handleErrors(TaskListener listener, @Nullable SonarRunnerInstallation sri, long startTime, IOException e) {
    Logger.printFailureMessage(listener);
    Util.displayIOException(e, listener);

    String errorMessage = Messages.SonarScanner_ExecFailed();
    if (sri == null && (System.currentTimeMillis() - startTime) < 1000 && getDescriptor().getSonarRunnerInstallations() == null) {
      // looks like the user didn't configure any SonarQube Scanner installation
      errorMessage += Messages.SonarScanner_GlobalConfigNeeded();
    }
    e.printStackTrace(listener.fatalError(errorMessage));
  }

  private static int executeSonarQubeScanner(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener, ArgumentListBuilder args, EnvVars env)
    throws IOException, InterruptedException {
    return launcher.launch().cmds(args).envs(env).stdout(listener).pwd(BuilderUtils.getModuleRoot(build, workspace)).join();
  }


  @VisibleForTesting
  void populateConfiguration(ExtendedArgumentListBuilder args, Run<?, ?> build, FilePath workspace,
    TaskListener listener, EnvVars env) throws IOException, InterruptedException {
    FilePath moduleRoot = BuilderUtils.getModuleRoot(build, workspace);
    args.append("sonar.host.url", hostUrl);
    UsernamePasswordCredentials credential = getUsernamePassword(build);

    if (credential != null) {
      args.append("sonar.login", credential.getUsername());
      args.appendMasked("sonar.password", credential.getPassword().getPlainText());
    }

    projectKey = getProjectKey(moduleRoot.getRemote());

    args.append("sonar.projectKey", projectKey);
    args.append("sonar.projectName", projectKey);
    args.append("sonar.projectVersion", "1.0.1");
    args.append("sonar.scm.revision", "2.0.2");
    args.append("sonar.projectBaseDir", moduleRoot.getRemote());

    sources = moduleRoot.getRemote() + File.separator + scannerDir.trim().replace("./","");
    args.append("sonar.sources", sources);
    args.append("sonar.exclusions", exclusions);
    args.append("sonar.java.binaries", sources);
  }


  private String getProjectKey(String path) {
    if(!JGitUtil.openGit(path)){
      return "";
    }
    gitRepoUrl = JGitUtil.getGitRepoUrl();
    branch = JGitUtil.getCurrentBranch();
    if(gitRepoUrl.equals("") || branch.equals("")){
      return "";
    }

    String dir = scannerDir.trim().replace("./","").replace("/","-");
    if(StringUtils.isBlank(dir)){
      return new StringBuilder().append(gitRepoUrl.trim().replace("/",".")
              .replace("@","_")).append(".").append(branch).toString();
    }else {
      return new StringBuilder().append(gitRepoUrl.trim().replace("/",".")
              .replace("@","_")).append(".").append(branch).append(".")
              .append(dir).toString();
    }
  }

  private UsernamePasswordCredentials getUsernamePassword(Run<?, ?> build) {
    if (StringUtils.isBlank(credentialsId)  || build == null) {
      return null;
    }
    StandardUsernamePasswordCredentials credential = CredentialsProvider.findCredentialById(credentialsId, StandardUsernamePasswordCredentials.class, build);
    if(credential == null){
      return null;
    }
    return credential;
  }
  public String getInstallationName() {
    return "sonarqube";
  }

  public void setInstallationName(String installationName) {
  }

  public String getSonarScannerName() {
    return "scanner";
  }


  public void setSonarScannerName(String sonarScannerName) {
  }
  public String getJdk() {
    return  "(Inherit From Job)";
  }

  public void setJdk(String jdk) {
  }

  @Override
  public Action getProjectAction(AbstractProject<?, ?> project) {
    return new SonarMarkerAction();
  }


  @Symbol("sonarScanner")
  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
    // Used in jelly configuration for conditional display of the UI
    public static final boolean BEFORE_V2 = JenkinsRouter.BEFORE_V2;

    public String getGlobalToolConfigUrl() {
      return JenkinsRouter.getGlobalToolConfigUrl();
    }


    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return Messages.SonarScannerBuilder_DisplayName();
    }

    public SonarRunnerInstallation[] getSonarRunnerInstallations() {
      return Jenkins.getInstance().getDescriptorByType(SonarRunnerInstallation.DescriptorImpl.class).getInstallations();
    }

  }

}
