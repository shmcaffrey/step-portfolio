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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    //command to get parameter 
    int numComments = getCommentInput(request);

    if (numComments == -1) {
        response.setContentType("text/html");
        response.getWriter().println("Please enter an integer greater than 0");
        return;
    }

    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ArrayList<String> arr = new ArrayList<>();

    int count = 0;
    for (Entity entity : results.asIterable()) {  
        if (numComments <= count) {
            break;
        }
        count++;
        String commentEntry = (String) entity.getProperty("userInput");
        arr.add(commentEntry);
    }

    String json = convertJsonUsingGson(arr);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String userComment = request.getParameter("text-input");
    long timestamp = System.currentTimeMillis();
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("userInput", userComment);
    commentEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    response.sendRedirect("/index.html");
  }

  private String convertJsonUsingGson(ArrayList<String> comments) {
    String json = new Gson().toJson(comments);
    return json;
  }

  private int getCommentInput(HttpServletRequest request) {
      String commentInputStr = request.getParameter("num-comments");
      int defaultVal = 5;
      if (commentInputStr == null) {
          return defaultVal;
      }
      int commentInput;
      try {
          commentInput = Integer.parseInt(commentInputStr);
      } catch (NumberFormatException e) {
          System.err.println("Could not conver to int: " + commentInputStr);
          return -1;
      }

      if (commentInput < 0) {
          System.err.println("Comment Input less than 0: " + commentInput);
          return -1;
      }

      return commentInput;
  }
}

