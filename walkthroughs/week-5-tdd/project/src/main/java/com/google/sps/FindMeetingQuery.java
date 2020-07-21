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

  //  Requires an array of TimeRanges sorted by acending order by end time
  //  Returns a collection of all open times between events that are longer than the requested duration
  private Collection<TimeRange> getMeetingRequest(ArrayList<TimeRange> eventTimes, Long duration) {
    Collection<TimeRange> meetingTimes = new ArrayList<TimeRange>();
    int currStart = TimeRange.START_OF_DAY;
    int currEnd = TimeRange.END_OF_DAY;

    // Create new time which starts at end of prev event and ends at beginning of next request
    newTime: 
    for (TimeRange eventTime : eventTimes) {
      currEnd = eventTime.start();
      TimeRange proposeTime = TimeRange.fromStartEnd(currStart, currEnd, false);

      // check that proposed time is longer than request dureation. Also ensures
      // that the propsed duration is not negtive
      if (checkDuration(proposeTime, duration)) {
        // checks if proposed time overlaps other times and makes adjustments
        // if duration is too short with new time, proposed time is nullified
        proposeTime = checkProposedTime(proposeTime, duration, eventTimes);
        if (proposeTime == null) {
          currStart = eventTime.end();
          continue newTime;
        }

        // if the proposed time is already within the collection don't add
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
    if (proposeTime.duration() > duration) {
      meetingTimes.add(proposeTime);
    }
    return meetingTimes;
  }

  // returns a list of all event times based on attendee list, no duplicates.
  private ArrayList<TimeRange> getAllEventTimes(Collection<String> attendees, Collection<Event> events) {
    ArrayList<TimeRange> allEventTimes = new ArrayList<>();
    Set<String> eventTitles = new HashSet<>();

    for (String attendee : attendees) {
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

  //  Requires a proposed requestedMeeting time based off allEventTimes that has an initial duration >= requested duration
  //  Checks if proposed time based on attendence overlaps any other attendee events
  //  If there are overlaps then it returns the new proposed time 
  private TimeRange checkProposedTime(TimeRange proposeTime, long duration, ArrayList<TimeRange> allEventTimes) {
    for (TimeRange anEvent : allEventTimes) {
      if (anEvent.overlaps(proposeTime)) {
        proposeTime = fixRange(anEvent, proposeTime);
        if (!checkDuration(proposeTime, duration)) {
          return null;
        }
      }
    }
    return proposeTime;
  }

  // Ensures proposed time has long enough duration
  private boolean checkDuration(TimeRange proposed, long duration) {
    return proposed.duration() >= duration;
  }

  // If proposed meeting time overlaps with another event, adjust start or end of proposed
  private TimeRange fixRange(TimeRange event, TimeRange proposed) {
    if (proposed.start() > event.start()) {
      return TimeRange.fromStartEnd(proposed.start(), event.start(), false);
    }
    else if (proposed.end() > event.start()) {
      return TimeRange.fromStartEnd(proposed.start(), event.start(), false);
    }
    //should not reach here
    System.out.println("returning null");
    return null;
  }

}
