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
package hudson.plugins.sonar.client;

import com.google.common.base.Strings;
import okhttp3.*;
import org.sonarqube.ws.client.HttpException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpClient {
  private final OkHttpClient okHttpClient;

  public HttpClient(OkHttpClient okHttpClient) {
    this.okHttpClient = okHttpClient;
  }

  public String getHttp(String url, @Nullable String token) {
    Request request = newRequest(url, token);
    Response response = httpCall(request);
    String content = getContent(response);

    if (isSuccessful(response)) {
      return content;
    } else {
      throw new HttpException(url, response.code(), content);
    }
  }

  public String postHttp(String url, @Nullable String token, Map<String, String> param) {
    Request request = newPostRequest(url,token,param);
    Response response = httpCall(request);
    String content = getContent(response);

    if (isSuccessful(response)) {
      return content;
    } else {
      throw new HttpException(url, response.code(), content);
    }
  }

  private static Request newRequest(String url, @Nullable String token) {
    Request.Builder builder = new Request.Builder().url(url);
    if (!Strings.isNullOrEmpty(token)) {
      if(token.contains(":")){
        String[] split = token.split(":");
        builder.addHeader("Authorization", Credentials.basic(split[0], split[1], StandardCharsets.UTF_8));
      }else {
        builder.addHeader("Authorization", Credentials.basic(token, "", StandardCharsets.UTF_8));
      }

    }
    return builder.build();
  }

  private static Request newPostRequest(String url, @Nullable String token,Map<String, String> param){
    FormBody.Builder formBodyBuilder = new FormBody.Builder();
    for (Map.Entry<String, String> entry : param.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      formBodyBuilder.add(key,value);
    }
    Request.Builder builder = new Request.Builder().post(formBodyBuilder.build()).url(url);
    if (!Strings.isNullOrEmpty(token)) {
      if(token.contains(":")){
        String[] split = token.split(":");
        builder.addHeader("Authorization", Credentials.basic(split[0], split[1], StandardCharsets.UTF_8));
      }else {
        builder.addHeader("Authorization", Credentials.basic(token, "", StandardCharsets.UTF_8));
      }
    }
    return builder.build();
  }

  private static String getContent(Response response) {
    try (ResponseBody body = response.body()) {
      return body.string();
    } catch (IOException e) {
      throw new IllegalStateException("Fail to read response of " + response.request().url(), e);
    }
  }

  private Response httpCall(Request request) {
    try {
      return okHttpClient.newCall(request).execute();
    } catch (IOException e) {
      throw new IllegalStateException("Fail to request " + request.url(), e);
    }
  }

  private static boolean isSuccessful(Response response) {
    return response.code() >= 200 && response.code() < 300;
  }

}
