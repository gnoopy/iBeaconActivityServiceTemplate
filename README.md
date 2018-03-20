# Activity and Service template for iBeacon application

To make android android application work with iBeacon, there are a few things to consider as foolows
* Gatt Service generating data stream should be parsed as iBeacon data structure 
* A Service should be able to scan iBeacons broadcast data in background.
* This service needs to communicate with activity.

Once these three features are ready it is easy to implement whatever your idea require. For better usage of this development patter, I would like to share my template.
Most of sources and logic are from respectful guru on the internet.

I refered to a few links like below.
* https://github.com/Vinayrraj/Android-iBeacon-Demo
* https://stackoverflow.com/questions/33513416/does-android-beacon-library-really-support-background-scanning
* https://github.com/AltBeacon/android-beacon-library-reference/tree/master/app/src/main/java/org/altbeacon/beaconreference
* https://stackoverflow.com/questions/4300291/example-communication-between-activity-and-service-using-messaging

And also I left TODOs what you have to do when you play around with the codes which can be a clue to check whether this template is working or not and how you can modify for your own projects.

Hope this is helpful for your development life.