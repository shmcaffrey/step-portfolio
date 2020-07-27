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

    //compile list of attendees
    Collection<String> allAttendees = new ArrayList<String>();
    for (String attendee : request.getAttendees()) {
      allAttendees.add(attendee);
    } for (String attendee : request.getOptionalAttendees()) {
      allAttendees.add(attendee);
    }

    // returns list of events all attendees have scheduled.
    ArrayList<TimeRange> allEventTimes = getAllEventTimes(allAttendees, events);
    if(allEventTimes.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    Collections.sort(allEventTimes, TimeRange.ORDER_BY_END);

    // Returns a list of all possible requested meeting times for all attendees
    Collection<TimeRange> meetingOptions = getMeetingRequest(allEventTimes, request.getDuration());
    if (!meetingOptions.isEmpty()) {
      return meetingOptions;
    } else if (request.getAttendees().isEmpty()) {
      return Arrays.asList();
    }

    //  If meetingOptions was empty, then only run with required attendees 
    allEventTimes = getAllEventTimes(request.getAttendees(), events);
    if (allEventTimes.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    Collections.sort(allEventTimes, TimeRange.ORDER_BY_END);
    return getMeetingRequest(allEventTimes, request.getDuration());
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

        //  In the event that two events end at the same time events: |--|---]
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

  // ASSUMES:  Duration of proposeTime is longer than or equal to requestDuration
  // MODIFIES: proposedTime [if any events in allEventTimes overlap, fixes the time]
  // EFFECTS:  Returns original proposedTime if it does not overlap with any other Events in allEventTimes
  //           Returns fixed proposedTime that does not overlap with any events in allEventTimes
  //           Returns null if fixRange results in a null event
  private TimeRange checkProposedTime(TimeRange proposeTime, long requestDuration, ArrayList<TimeRange> allEventTimes) {
    for (TimeRange anEvent : allEventTimes) {
      if (anEvent.overlaps(proposeTime)) {
        proposeTime = fixRange(anEvent, proposeTime, requestDuration);
      }
    }
    return proposeTime;
  }
  
  //  ASSUMES: event and proposed overlap
  //           proposed.end is never >= an overlappingEvent.end 
  //               Event:       [-----]  or [----]   or [----]
  //               Proposed:  [---------]     [----]      [--]
  //             in the cases above the proposed time end would stop at the 
  //             events start, it would never skip an event who's end time intersects it
  //             because the events are ordered in ascending order by end time
  //  EFFECTS: 
  //         1)  Event:       |--------|
  //             Proposed:       |--|
  //             RETURNS: null::new duration is negative
  //         2)  Event:           [----------]
  //             Proposed:  [---------]
  //             new proposed time becomes proposed start and event start
  //             RETURNS:  shortened proposed time || null::new duration is too short for request 
  private TimeRange fixRange(TimeRange overlappingEvent, TimeRange proposed, long requestDuration) {
    if (proposed.end() > overlappingEvent.start()) {
      proposed = TimeRange.fromStartEnd(proposed.start(), overlappingEvent.start(), false);
      if (!checkDuration(proposed.duration(), requestDuration)) {
          return null;
      }
      return proposed;
    }
    // should no reach here, the case that they don't overlap.
    return null;
  }
 
  //  EFFECTS: Returns if the proposed event time duration is longer than the requestDuration
  private boolean checkDuration(long proposedDuration, long requestDuration) {
    return proposedDuration >= requestDuration;
  }

}
