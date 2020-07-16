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

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.lang.*;
import java.util.Comparator;

public final class FindMeetingQuery {
  private static final int DAY_IN_MINS = (60 * 24);

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    System.out.println("new request");
    Collection<TimeRange> collection = new ArrayList<TimeRange>();

    // EDGE CASES: request time over/under minutes in a day number of attendees is 0
    if (request.getDuration() > DAY_IN_MINS || request.getDuration() < 0) {
      return Arrays.asList();
    } else if (request.getAttendees() == null) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    int currStart = TimeRange.START_OF_DAY;
    int currEnd = TimeRange.END_OF_DAY;
    
    // get every attendee's blocked off time into a cohesive arrayList to sort
    // don't add duplicate events
    ArrayList<TimeRange> allEventTimes = new ArrayList<>();
    Set<String> eventTitles = new HashSet<>();


    for (String person : request.getAttendees()) {
      System.out.println("Requested Attendee: " + person);
      for (Event event : events) {

        //TO DELETE
        System.out.println("Event in Event list: " + event.getTitle());
        System.out.println("List of attendees in event: ");
        for (String attendee : event.getAttendees()) {
          System.out.println(attendee);
        }
        // END DELETE

        if (event.getAttendees().contains(person)) {
          if (!eventTitles.contains(event.getTitle())) {
            allEventTimes.add(event.getWhen());
            System.out.println("add event: START: " + event.getWhen().start() + " END: " + event.getWhen().end());
          }
        }
      }
    }

    // no attendees match any events
    if (allEventTimes.isEmpty()) {
      System.out.println("no attendees match events");
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    Collections.sort(allEventTimes, TimeRange.ORDER_BY_END); //TO DELETE


    System.out.println("sorting complete"); // TO DELETE
    for (TimeRange aEvent : allEventTimes) {
      System.out.println("postsort. START: " + aEvent.start() + " END: " + aEvent.end());
    }

    // Create new time which starts at first opening and ends at  of request

    newTime: 
    for(TimeRange eventTime : allEventTimes) { 
      currEnd = eventTime.start();
      System.out.println("current start: " + currStart + " current end: " + currEnd);
      TimeRange proposeTime = TimeRange.fromStartEnd(currStart, currEnd, false);

      // check that proposed time is longer than request dureation. Also ensures
      // that the propsed duration is not negtive
      if (checkDuration(proposeTime, request.getDuration())) {
        System.out.println("durration large enough");
        for (TimeRange anEvent : allEventTimes) {
          if (anEvent.equals(proposeTime)) {
            continue;
          } else if (anEvent.overlaps(proposeTime)) {
            proposeTime = fixRange(anEvent, proposeTime);
            if (!checkDuration(proposeTime, request.getDuration())) {
              currStart = eventTime.end();
              continue newTime;
            }
          }
        }

        // if the proposed time is already within the collection don't add
        for (TimeRange RequestOptions : collection) {
          if (RequestOptions.equals(proposeTime)) {
            currStart = eventTime.end();
            continue;
          }
        }

        collection.add(proposeTime);
      }
      else {
        System.out.println("duration too short: " + proposeTime.duration());
      }
      currStart = eventTime.end();
    }
    //  add time gap for end of day, current start is on last 
    TimeRange proposeTime = TimeRange.fromStartEnd(currStart, DAY_IN_MINS, false);
    if (proposeTime.duration() > request.getDuration()) {
      collection.add(proposeTime);
    }

    return collection;
    
  }
  
  private boolean checkDuration(TimeRange proposed, long duration) {
    return proposed.duration() >= duration;
  }

  private TimeRange fixRange(TimeRange event, TimeRange proposed) {
    if (event.start() < proposed.start()) {
      System.out.println("proposed start is within another events start, adusting end");
      return TimeRange.fromStartEnd(proposed.start(), event.start(), false);
    }
    else if (event.start() < proposed.end()) {
      System.out.println("proposed end is within another events start. adjusting end");
      return TimeRange.fromStartEnd(proposed.start(), event.start(), false);
    }
    //should not reach here
    System.out.println(", returning null");
    return null;
  }
}
