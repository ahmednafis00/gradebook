# gradebook

## What does the project do?
This project (titled ‘gradebook’) aims to create a productivity/planning tool that will allow students to keep track of their progress in a semester. The ideal use case for gradebook is as follows:
* In the beginning of the semester, instructor registers the course in *gradebook* and adds all the grading components and their respective weights
*	Student can then add the course, and check her/his progress at any time of the semester. The student enters the scores s/he received, and *gradebook* immediately notifies if s/he is on track to receive a desired score, and what s/he has to score in the remaining grading components’ units to receive the desired score. 
*	As soon as the instructor registers the course in *gradebook*, a real-time chatroom is created for the course. Instructor can post notes/announcements there, and students can ask/discuss course-related questions and concerns. 

## Why did I choose this project?
Every semester since freshman year, I felt the need of a *gradebook*-like application. Specially towards the end of the semester when there’s increased work load from all courses, it’s extremely useful to prioritize all the course-related tasks. By checking progress on *gradebook*, students can have a clear idea on their current standing for each course. As a result, they can make data-oriented decisions on which courses they should put in some extra effort and on which courses they can afford to slack off a little. 

Moreover, I believe *gradebook* can improve student-instructor communication. On top of being occupied with various commitments, instructors usually receive a bunch of different emails from different people and organizations. As a result, it becomes quite difficult to reply to students over email. Moreover, students often feel hesitant to reach out to instructors via email as they fear their question might not be “important/urgent enough”. Using the course chat room in *gradebook*, instructors can have all their student queries in one place. Furthermore, any student questions can be answered by other students as well.

## Technical Challenges & Solutions
*	**Protocol for client/server communication:** *took inspiration from hwk-2 and hwk-3*
*	**Lots of different types of data to be stored (each user has a set of courses – each course has different grading components – each grading components have different number of items and grades):** *created different classes (TcpUser.java, Course.java, GradingComponent.java, etc.) and used different types of data structures (Maps, Vectors, and Arrays) depending on the use case*
*	**Multiple users logging in at the same time:** *Similar multi-threading and “token for every session” concepts like hwk-3*
*	**Ensuring students can’t enroll in courses from any university other than their own:** *Every time an instructor creates a course, it is added as a value in a map with key “Course Name – Institution”. So, when a student submits a request to add a course using its name, “ – Institution” is appended to ensure a course with the same name from a different university is not added. The chatrooms are also named in this format for the same reason.*
*	**Real-time chat:** *This has been implemented using Firebase Realtime Database by Google. The database is created as a tree. Each course chatroom acts as a parent node, and all the messages in that chatroom are its children. All user messages have a unique identifier to allow duplicate messages. All messages are added as a Name-Value pair.*
* **AWS deployment:** *recalled what we did during an "hands-on" in class for the same purpose and followed documentation and resources available online - Public IP: 54.160.212.155* 

## Limitations
The program could be made more robust. Current limitations include:
*	Only courses with 3 grading components can be added.
*	User has to enter scores and create courses in a specific format (instructions are given in their respective layouts)
* Students need to enter at least one score for every grading component to receive their progress results 
* Most common reason for server crash: no input when the program expects one

## Compatibility
The program should run on most Android devices. If you're running it on an emulator in Android Studio, I'd recommend using Nexus 5 API 30. 

## Future Steps
I will definitely be working this winter to make improvements in this project. Most of them can be done by making myself more skilled in layout design in Android Studio.  Some improvements I currently have in mind:
*	Allow instructors to add grades and view class average for each grading component’s items
*	Allow students to add grades and store them in a layout similar to the one used by instructors in Brightspace
*	Work on the current limitations of the programs
