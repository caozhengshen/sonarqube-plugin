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
package hudson.plugins.sonar.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RemoteConfig;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * @author caozhensheng
 * @date 2023/9/4
 */
public class JGitUtil {
    private static Git git ;

    public static boolean openGit(String path){
        try {
            git =Git.open(Paths.get(path, ".git").toFile());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static String getGitRepoUrl(){
        try {
            List<RemoteConfig> remoteConfigList = git.remoteList().call();
            if(remoteConfigList.size()>0){
                String url = remoteConfigList.get(0).getURIs().toString();
                return url.substring(1,url.length()-1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getCurrentBranch(){
        String branch = "";
        try {
            Repository repository = git.getRepository();
            Ref head = repository.findRef("HEAD");
            Map<String, Ref> allRefs = repository.getAllRefs();
            for(Map.Entry<String,Ref> entry:allRefs.entrySet()){
                String key = entry.getKey();
                Ref value = entry.getValue();
                if(!key.equals("HEAD") && value.getObjectId().equals(head.getObjectId())){
                    branch = key;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return branch.replace("refs/remotes/origin/","");
    }
}
