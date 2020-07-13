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
import java.util.*;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/map-marker")
public class MapMarkerServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Query query = new Query("map-marker").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    ArrayList<Map<String, Double>> pairList = new ArrayList<Map<String, Double>>();

    for (Entity entity : results.asIterable()) {  
        Map<String, Double> latLngPair = new HashMap<>();
        Double latEntry = (Double) entity.getProperty("lat");
        Double lngEntry = (Double) entity.getProperty("lng");
        latLngPair.put("lat", latEntry);
        latLngPair.put("lng", lngEntry);
        pairList.add(latLngPair);
    }

    String json = convertJsonUsingGson(pairList);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    Map<String, Double> pairLatLng = getCoords(request);
    Long timestamp = System.currentTimeMillis();
    Entity markerEntity = new Entity("map-marker");
    markerEntity.setProperty("lat", pairLatLng.get("lat"));
    markerEntity.setProperty("lng", pairLatLng.get("lng"));
    markerEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(markerEntity);
  }

  private String convertJsonUsingGson(ArrayList<Map<String, Double>> latLngPair) {
    String json = new Gson().toJson(latLngPair);
    return json;
  }

  private Map<String, Double> getCoords(HttpServletRequest request) {

      String latInputStr = request.getParameter("lat");
      String lngInputStr = request.getParameter("lng");
      Map<String, Double> pair= new HashMap<String, Double>();

      try {
          pair.put("lat", Double.parseDouble(latInputStr));
          pair.put("lng", Double.parseDouble(lngInputStr));
      } catch (NumberFormatException e) {
          System.err.println("Could not convert to Double: " + latInputStr + "or " + lngInputStr);
      }

      log("pair requested: " + pair.get("lat") + " " + pair.get("lng"));
      return pair;
  }
}

