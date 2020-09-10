# hwk-1
The lifecycle methods that are called when the screen is rotated 90 degrees are:
* onPause()
* onStop()
* onDestroy()
* onCreate()
* onStart()
* onResume()

The orientation the screen is in when the user enters and uses the app is considered as one activity (Activity A) and the rotation of the screen triggers a different activity (Activity B). As a result, the lifecycle methods follow the order as seen above. The system calls onPause() as the first indication that the user is leaving Activity A. It indicates that the activity is no longer in the foreground. Then the system invokes the onStop() callback as Activity A is no longer visible to the user. This is followed by invoking onDestroy() as the system is temporarily destroying Activity A due to a configuration change (device rotation in this case). 

Afterwards, the onCreate() callback is invoked as the system creates Activity B. Then the system invokes onStart() to make Activity B visible to the user, as the app prepares for the activity to enter the foreground and become interactive. Finally, Activity B comes to the foreground, and the system invokes the onResume() callback. This is the state in which the app interacts with the user. 
