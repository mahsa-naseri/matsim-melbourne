#!/bin/bash
if [[ $# -eq 0 ]]; then
  events_file=output_events.xml.gz;
else
  events_file=$1
fi

# Departure and Arrival events
zcat $events_file | tail -n +3 | head -n -2 |
grep "departure\|arrival" |
# makes sure these locations are correct before running
cut -d "\"" -f 2,4,6,8,10 |
sed -e "s/\"/,/g"|
sed -e '1i\'$'\n''time,type,person,link,legMode' | gzip >> person_trip.csv.gz

# vehicle-traffic interaction (vehicle leaves/enters traffic) events
zcat $events_file | tail -n +3 | head -n -2 |
grep "vehicle leaves traffic\|vehicle enters traffic" |
sed -e "s/vehicle leaves traffic/vehicle_leaves_traffic/g;" |
sed -e "s/vehicle enters traffic/vehicle_enters_traffic/g;" |
# makes sure these locations are correct before running
cut -d "\"" -f 2,4,6,8,10,12 |
sed -e "s/\"/,/g"|
sed -e '1i\'$'\n''time,type,person,link,vehicle,networkMode' | gzip >> vehicle_traffic.csv.gz

# vehicle left/entered the link events
zcat $events_file | tail -n +3 | head -n -2 |
grep "left link\|entered link" |
sed -e "s/left link/left_link/g; s/entered link/entered_link/g " |
# makes sure these locations are correct before running
cut -d "\"" -f 2,4,6,8 |
sed -e "s/\"/,/g"|
sed -e '1i\'$'\n''time,type,vehicle,link' | gzip >>  vehicle_link.csv.gz


# Activity-start/end (actstart and actend) events
zcat  $events_file | tail -n +3 | head -n -2 |
grep "actstart\|actend" |
# makes sure these locations are correct before running
cut -d "\"" -f 2,4,6,8,10 |
sed -e "s/\"/,/g"|
sed -e '1i\'$'\n''time,type,person,link,actType' | gzip >> person_act.csv.gz
