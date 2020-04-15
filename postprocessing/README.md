# MATSim output post-processing :

## Converting events.xml to csv

`event2csv.sh` converts the MATSim `output_events.xml.gz` file into four CSVs based on MATSim's main events:
- Person/agent departure and Arrival events: `person_trip.csv.gz`,
- Vehicle-traffic interaction (vehicle leaves/enters traffic) events: `vehicle_traffic.csv.gz`,
- vehicle left/entered the link events: `vehicle_link.csv.gz`,
- Activity-start/end (actstart and actend) events: `person_act.csv.gz`.

To do the conversion, either put the converter and output in a same folder and run:
```
./event2csv.sh
```
or optionally you can specify path to the output file for converter as follows:
```
./event2csv.sh <path-to-your-events-file>
```

**NOTE:** Selecting values for the attribute of each event is based on assuming the order is fixed and won't change. For example, I have assumed for person departure events, order of the attributes is time->type->person->link->legMode. For now, **make sure the orders are similar in your event file as well**, I will make this more flexible later.

**NOTE:** The converter cuts the strings by double quotation mark, `"`, so if you have `"` in the name of your agents or vehicles or link ids, it is very likely that the output won't be what you want.
