// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String pageOne = "Homepage with pictures. U/C";
    String pageTwo = "List of projects in a resume way. U/C";
    String pageThree = "Hidden Talents. U/C";


    List<String> arr = new ArrayList<String>();
    arr.add(pageOne);
    arr.add(pageTwo);
    arr.add(pageThree);

    String json = convertJsonUsingGson(arr);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

//  Manual array to Json conversion
 private String convertToJson(ArrayList<String> comments) {
    String json = "{";
    json += "\"pageOne\": ";
    json += "\"" + comments.get(0) + "\"";
    json += ", ";
    json += "\"pageTwo\": ";
    json += "\"" + comments.get(1) + "\"";
    json += ", ";
    json += "\"pageThree\": ";
    json += comments.get(2);
    json += "}";
    return json;
  }

  private String convertJsonUsingGson(ArrayList<String> comments) {
      String json = new Gson().toJson(comments);
      return json;
  }
}
