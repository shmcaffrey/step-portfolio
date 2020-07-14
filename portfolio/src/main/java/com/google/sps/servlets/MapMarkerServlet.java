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

class Location {
  public Double lat;
  public Double lng;

  public Location(Double latIn, Double lngIn) {
    this.lat = latIn;
    this.lng = lngIn;
  }
  public Location() {}
}

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/map-marker")
public class MapMarkerServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Query query = new Query("map-marker").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    ArrayList<Location> locations = new ArrayList<Location>();

    for (Entity entity : results.asIterable()) {  
      Double latEntry = (Double) entity.getProperty("lat");
      Double lngEntry = (Double) entity.getProperty("lng");
      Location locality = new Location(latEntry, lngEntry);
      locations.add(locality);
    }

    String json = convertJsonUsingGson(locations);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    Location locality = getCoords(request);
    Long timestamp = System.currentTimeMillis();
    Entity markerEntity = new Entity("map-marker");
    markerEntity.setProperty("lat", locality.lat);
    markerEntity.setProperty("lng", locality.lng);
    markerEntity.setProperty("timestamp", timestamp);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(markerEntity);
  }

  private String convertJsonUsingGson(ArrayList<Location> locality) {
    String json = new Gson().toJson(locality);
    return json;
  }

  private Location getCoords(HttpServletRequest request) {

    String latInputStr = request.getParameter("lat");
    String lngInputStr = request.getParameter("lng");

    Location locality = new Location();

    try {
      locality.lat = Double.parseDouble(latInputStr);
      locality.lng = Double.parseDouble(lngInputStr);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to Double: " + latInputStr + "or " + lngInputStr);
    }

    log("pair requested: " + locality.lat + " " + locality.lng);
    return locality;
  }
}

