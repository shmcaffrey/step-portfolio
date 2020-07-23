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
  private static final int MINS_IN_DAY = (60 * 24);

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // EDGE CASES: request time over/under minutes in a day number of attendees is 0
    if (request.getDuration() > MINS_IN_DAY || request.getDuration() < 0) {
      return Arrays.asList();
    } else if (request.getAttendees().isEmpty() && request.getOptionalAttendees().isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // Returns list of needed attendees event times nased on requested members in a single array
    ArrayList<TimeRange> allEventTimes = getAllEventTimes(request.getAttendees(), events);

    // No attendees match any events
    if (allEventTimes.isEmpty() && request.getOptionalAttendees().isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    Collections.sort(allEventTimes, TimeRange.ORDER_BY_END);

    // Returns a list of all optional attendee event times on requested members in a single array. 
    Collection<TimeRange> meetingOptions = getMeetingRequest(allEventTimes, request.getDuration());
    Collection<TimeRange> optionalMeetingOptions = new ArrayList<TimeRange>();
    // Get optional attendees open times, if open times overlap with set open times 
    //     then temp. adjust duration and continue
    if (!request.getOptionalAttendees().isEmpty()) {
      ArrayList<TimeRange> optionalAttendeeTimes = getAllEventTimes(request.getOptionalAttendees(), events);
      Collections.sort(optionalAttendeeTimes, TimeRange.ORDER_BY_END);
      optionalMeetingOptions = getMeetingRequest(optionalAttendeeTimes, request.getDuration());

      if (optionalMeetingOptions.isEmpty() && request.getAttendees().isEmpty()) {
        return Arrays.asList();
      } else if (allEventTimes.isEmpty()) {
        return optionalMeetingOptions;
      }

      // combine event time lists and return optional times if they exist
      for (TimeRange optional : optionalAttendeeTimes) {
        allEventTimes.add(optional);
      }

      Collections.sort(allEventTimes, TimeRange.ORDER_BY_END);
      optionalMeetingOptions = getMeetingRequest(allEventTimes, request.getDuration());
      
      if (!optionalMeetingOptions.isEmpty()) {
        return optionalMeetingOptions;
      }
    }
    return meetingOptions;
  }

  //  ASSUMES: eventTimes is not empty and is sorted in acending order by end time, duration is greater than 0. 
  //  EFFECTS: Returns a list of TimeRanges with durations longer than requestDuration that do not overlap with any events in eventTimes.
  private Collection<TimeRange> getMeetingRequest(ArrayList<TimeRange> eventTimes, Long requestDuration) {
    Collection<TimeRange> meetingTimes = new ArrayList<TimeRange>();
    int currStart = TimeRange.START_OF_DAY;
    int currEnd = TimeRange.END_OF_DAY;

    for (TimeRange eventTime : eventTimes) {
      currEnd = eventTime.start();
      //  proposeTime finds the gap between eventA and eventB 
      TimeRange proposeTime = TimeRange.fromStartEnd(currStart, currEnd, false);

      //  Ensures no proposedTimes that are negative
      if (checkDuration(proposeTime.duration(), requestDuration)) {

        // returns null if the proposedTime's duration is not longer than the request duration
        //    and skips to next loop with current eventTime.end() as the start
        proposeTime = checkProposedTime(proposeTime, requestDuration, eventTimes);
        if (proposeTime == null) {
          currStart = eventTime.end();
          continue;
        }

        //  In the event thattwo events end at the same time events: |--|---]
        //      the proposed time would be aducted for the inner event and be equivalent to the outer event
        //      skips to next loop with current eventTime.end() as the start
        for (TimeRange requestOption : meetingTimes) {
          if (proposeTime.equals(requestOption)) {
            currStart = eventTime.end();
            continue;
          }
        }
        meetingTimes.add(proposeTime);
      }
      currStart = eventTime.end();
    }
    //  add time gap for end of day, current start is on last 
    TimeRange proposeTime = TimeRange.fromStartEnd(currStart, MINS_IN_DAY, false);
    if (checkDuration(proposeTime.duration(), requestDuration)) {
      meetingTimes.add(proposeTime);
    }
    return meetingTimes;
  }

  // EFFECTS: Returns a cumulative list of all requestAttendees's events from the event list without duplicates
  private ArrayList<TimeRange> getAllEventTimes(Collection<String> requestAttendees, Collection<Event> events) {
    ArrayList<TimeRange> allEventTimes = new ArrayList<>();
    //  To ensure no duplicates.
    Set<String> eventTitles = new HashSet<>();

    for (String attendee : requestAttendees) {
      for (Event event : events) {
        if (event.getAttendees().contains(attendee)) {
          if (!eventTitles.contains(event.getTitle())) {
            eventTitles.add(event.getTitle());
            allEventTimes.add(event.getWhen());
          }
        }
      }
    }
    return allEventTimes;
  }


  // ASSUMES:  Duration of proposeTime is longer than or equal to requestDuration
  // MODIFIES: proposedTime [if any events in allEventTimes overlap, fixes the time]
  // EFFECTS:  Returns original proposedTime if it does not overlap with any other Events in allEventTimes
  //           Returns fixed proposedTime that does not overlap with any events in allEventTimes
  //           Returns null if the proposedTime overlaps an event and the fixed proposed time has too small of a duration. 
  private TimeRange checkProposedTime(TimeRange proposeTime, long requestDuration, ArrayList<TimeRange> allEventTimes) {
    for (TimeRange anEvent : allEventTimes) {
      if (anEvent.overlaps(proposeTime)) {
        proposeTime = fixRange(anEvent, proposeTime);
        if (!checkDuration(proposeTime.duration(), requestDuration)) {
          return null;
        }
      }
    }
    return proposeTime;
  }

 
  //  EFFECTS: Returns if the proposed event time duration is longer than the requestDuration
  private boolean checkDuration(long proposedDuration, long requestDuration) {
    return proposedDuration >= requestDuration;
  }

  
  //   ASSUMES: event overlaps proposed
  private TimeRange fixRange(TimeRange event, TimeRange proposed) {
    // if (proposed.start() > event.start()) {
    //   return TimeRange.fromStartEnd(event.end(), proposed.end(), false);
    // } Works without this function
     if (proposed.end() > event.start()) {
      return TimeRange.fromStartEnd(proposed.start(), event.start(), false);
    }
    return null;
  }

}
