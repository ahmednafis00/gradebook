import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private ServerSocket mServerSocket;
    private HashMap<String, String> mPasswordMap;
    private HashMap<String, TcpUser> mTcpUsers;
    private HashMap<String, TcpUser> mUserIndex;
    private HashMap<String, Course> mCourses;

    public Server(int port) throws IOException {
        mServerSocket = new ServerSocket(port);
        mPasswordMap = new HashMap<String, String>(); // key: email, value: password
        mTcpUsers = new HashMap<String, TcpUser>(); // key: token, value: TcpUser
        mUserIndex = new HashMap<String, TcpUser>(); // key: email, value: TcpUser
        mCourses = new HashMap<String, Course>(); // key: course name - institution, value: Course
    }

    public void listen() throws IOException {
        boolean quit = false;
        TcpConnectionListenerThread listenerThread = new TcpConnectionListenerThread(mServerSocket, mTcpUsers);
        listenerThread.start();

        while (!quit) {
            synchronized (mTcpUsers) {
                String[] tokens = mTcpUsers.keySet().toArray(new String[0]);
                for (String token : tokens) {
                    TcpUser user = mTcpUsers.get(token);

                    if (user.hasMessage()) {
                        String rawMessage = user.readMessage();
                        if (rawMessage == null || !rawMessage.contains("type=")) {
                            rawMessage = "";
                        }

                        GradebookProtocol message = new GradebookProtocol(rawMessage);
                        switch (message.getParam(GradebookProtocol.TYPE_KEY)) {
                            case (GradebookProtocol.LOGIN_COMMAND):
                                user.setToken(token);
                                if (logIn(user, message) != 0) {
                                    System.out.println("Failed login attempt by "
                                        + message.getParam(GradebookProtocol.LOGIN_EMAIL));
                                    user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                                        + GradebookProtocol.LOGIN_ACK + GradebookProtocol.PARAM_SEPERATOR
                                        + GradebookProtocol.STATUS + GradebookProtocol.PARAM_ASSIGN
                                        + GradebookProtocol.INVALID_TOKEN);
                                    mTcpUsers.remove(token);
                                } else {
                                    System.out.println(message.getParam(GradebookProtocol.LOGIN_EMAIL) + " logged in");
                                }
                                break;
                            case (GradebookProtocol.SIGNUP_COMMAND):
                                if (signUp(user, message) != 0) {
                                    System.out.println("Duplicate sign up attempt by "
                                            + message.getParam(GradebookProtocol.LOGIN_EMAIL));
                                    user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                                        + GradebookProtocol.SIGNUP_ACK + GradebookProtocol.PARAM_SEPERATOR
                                        + GradebookProtocol.STATUS + GradebookProtocol.PARAM_ASSIGN
                                        + GradebookProtocol.INVALID_TOKEN);
                                    mTcpUsers.remove(token);
                                } else {
                                    System.out.println(user.getEmail() + " signed up");
                                }
                                break;
                            case (GradebookProtocol.VIEW_COURSES_CMD):
                                if (userViewCourses(user, message) != 0) {
                                    user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                                        + GradebookProtocol.VIEW_COURSES_ACK + GradebookProtocol.PARAM_SEPERATOR
                                        + GradebookProtocol.STATUS + GradebookProtocol.PARAM_ASSIGN + 
                                        GradebookProtocol.INVALID_TOKEN);
                                }
                                break;
                            case (GradebookProtocol.ADD_COURSE_INSTRUCTOR):
                                if (createCourse(user, message) != 0) {
                                    user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                                        + GradebookProtocol.ADD_COURSE_ACK + GradebookProtocol.PARAM_SEPERATOR
                                        + GradebookProtocol.STATUS + GradebookProtocol.PARAM_ASSIGN + 
                                        GradebookProtocol.INVALID_TOKEN);
                                }
                                break;
                            case (GradebookProtocol.ADD_COURSE_STUDENT): 
                                if (addCourse(user, message) != 0) {
                                    user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                                        + GradebookProtocol.ADD_COURSE_ACK + GradebookProtocol.PARAM_SEPERATOR
                                        + GradebookProtocol.STATUS + GradebookProtocol.PARAM_ASSIGN + 
                                        GradebookProtocol.INVALID_TOKEN);
                                }
                                break;
                            case (GradebookProtocol.GET_COMPONENTS):
                                getComponents(user, message); // course and user have already been verified
                                break;
                            case (GradebookProtocol.CHECK_PROGRESS_CMD):
                                if (getProgress(user, message) != 0) {
                                    user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                                        + GradebookProtocol.ADD_COURSE_ACK + GradebookProtocol.PARAM_SEPERATOR
                                        + GradebookProtocol.STATUS + GradebookProtocol.PARAM_ASSIGN + 
                                        GradebookProtocol.INVALID_TOKEN);
                                } 
                                break;
                            case (GradebookProtocol.CHAT_COMMAND):
                                chat(user, message); // course and user have already been verified
                            case (GradebookProtocol.LOGOUT_COMMAND):
                                if (logOut(user, message) != 0) {
                                    user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                                        + GradebookProtocol.LOGOUT_ACK + GradebookProtocol.PARAM_SEPERATOR
                                        + GradebookProtocol.STATUS + GradebookProtocol.PARAM_ASSIGN 
                                        + GradebookProtocol.INVALID_TOKEN);
                                }
                                mTcpUsers.remove(token);
                                break;
                            default:
                                mTcpUsers.remove(token);
                        }
                    }
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            listenerThread.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private int logIn(TcpUser user, GradebookProtocol message) {
        TcpUser actUser = mUserIndex.get(message.getParam(GradebookProtocol.LOGIN_EMAIL));
        if (actUser != null) {
            actUser.setToken(user.getToken());
        }

        if ((!message.getParam(GradebookProtocol.LOGIN_PASSWORD)
                .equals(mPasswordMap.get(message.getParam(GradebookProtocol.LOGIN_EMAIL)))) || 
                (message.getParam(GradebookProtocol.LOGIN_EMAIL) == null)) {
                return -1;
        }

        try {
            user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                    + GradebookProtocol.LOGIN_ACK + GradebookProtocol.PARAM_SEPERATOR
                    + GradebookProtocol.STATUS + GradebookProtocol.PARAM_ASSIGN 
                    + GradebookProtocol.ACK + GradebookProtocol.PARAM_SEPERATOR + 
                    GradebookProtocol.TOKEN + GradebookProtocol.PARAM_ASSIGN + actUser.getToken() 
                    + GradebookProtocol.PARAM_SEPERATOR + GradebookProtocol.USER_TYPE
                    + GradebookProtocol.PARAM_ASSIGN + actUser.getType() );
        } catch (IOException e) {
            return -1;
        }

        mTcpUsers.put(actUser.getToken(), actUser); 
        return 0;
    }

    private int signUp(TcpUser user, GradebookProtocol message) {
        if (message.getParam(GradebookProtocol.NAME) == null || 
            message.getParam(GradebookProtocol.LOGIN_EMAIL) == null ||
            message.getParam(GradebookProtocol.INSTITUTION) == null) {
                return -1;
            }

        if (mPasswordMap.get(message.getParam(GradebookProtocol.LOGIN_EMAIL)) == null) {
            mPasswordMap.put(message.getParam(GradebookProtocol.LOGIN_EMAIL),
                message.getParam(GradebookProtocol.LOGIN_PASSWORD));
            user.setName(message.getParam(GradebookProtocol.NAME));    
            user.setEmail(message.getParam(GradebookProtocol.LOGIN_EMAIL));
            user.setInstitution(message.getParam(GradebookProtocol.INSTITUTION));
            user.setType(message.getParam(GradebookProtocol.USER_TYPE));
        } else {
            return -1;
        }

        try {
            user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                    + GradebookProtocol.SIGNUP_ACK + GradebookProtocol.PARAM_SEPERATOR
                    + GradebookProtocol.STATUS + GradebookProtocol.PARAM_ASSIGN 
                    + GradebookProtocol.ACK);
        } catch (IOException e) {
            return -1;
        }

        mUserIndex.put(user.getEmail(), user);
        return 0;
    }

    private int createCourse(TcpUser user, GradebookProtocol message) {
        String token = message.getParam(GradebookProtocol.TOKEN);
        TcpUser instructor = mTcpUsers.get(token);

        if (token == null || instructor == null || instructor.getEmail() == null) {
            return -1;
        }

        Course newCourse = new Course(message.getParam(GradebookProtocol.COURSE_NAME), message.getParam(GradebookProtocol.COURSE_INSTITUTION), 
                            Integer.parseInt(message.getParam(GradebookProtocol.COURSE_CREDITS)));
        String components = message.getParam(GradebookProtocol.COMPONENTS);
        String[] componentsArr = components.split(GradebookProtocol.END, 0);

        String name = "";
        int qty = 0, weight = 0;
        for (int i = 0; i < componentsArr.length; ++i) {
            String comp = componentsArr[i];
            String[] compArr = comp.split(GradebookProtocol.SEPARATOR, 0);
            for (int j = 0; j < compArr.length; ++j) {
                if (compArr[j].substring(0, compArr[j].indexOf(GradebookProtocol.COMPONENT_CONTENT)).equals(GradebookProtocol.COMPONENT_NAME)) {
                    name = compArr[j].substring(compArr[j].indexOf(GradebookProtocol.COMPONENT_CONTENT) + GradebookProtocol.COMPONENT_CONTENT.length());
                } else if (compArr[j].substring(0, compArr[j].indexOf(GradebookProtocol.COMPONENT_CONTENT)).equals(GradebookProtocol.COMPONENT_QUANTITY)) {
                    qty = Integer.parseInt(compArr[j].substring(compArr[j].indexOf(GradebookProtocol.COMPONENT_CONTENT) + GradebookProtocol.COMPONENT_CONTENT.length()));
                } else if (compArr[j].substring(0, compArr[j].indexOf(GradebookProtocol.COMPONENT_CONTENT)).equals(GradebookProtocol.COMPONENT_WEIGHT)) {
                    weight = Integer.parseInt(compArr[j].substring(compArr[j].indexOf(GradebookProtocol.COMPONENT_CONTENT) + GradebookProtocol.COMPONENT_CONTENT.length()));
                }
            }
            System.out.println("New component (" + newCourse.getName() + "): " + name + " " + qty + " " + weight);
            newCourse.addComponent(name, qty, weight);
        }
        instructor.addCourse(newCourse);
        mCourses.put(message.getParam(GradebookProtocol.COURSE_NAME) + " - " + message.getParam(GradebookProtocol.COURSE_INSTITUTION), newCourse);

        try {
            user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                    + GradebookProtocol.ADD_COURSE_ACK + GradebookProtocol.PARAM_SEPERATOR
                    + GradebookProtocol.STATUS + GradebookProtocol.PARAM_ASSIGN 
                    + GradebookProtocol.ACK);
        } catch (IOException e) {
            return -1;
        }

        return 0;
    }

    private int userViewCourses(TcpUser user, GradebookProtocol message) {
        String token = message.getParam(GradebookProtocol.TOKEN);
        TcpUser origUser = mTcpUsers.get(token);

        if (token == null || origUser == null || origUser.getEmail() == null) {
            return -1;
        }

        Vector<Course> userCourses = origUser.getCourses();
        String courseList = "";

        if (userCourses.isEmpty()) {
            courseList = GradebookProtocol.COURSE_END;
        } else {
            for (int i = 0; i < userCourses.size(); ++i) {
                courseList += userCourses.elementAt(i).getName() + GradebookProtocol.COURSE_END;
            }
        }

        try {
            user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                    + GradebookProtocol.COURSES + GradebookProtocol.PARAM_SEPERATOR
                    + GradebookProtocol.COURSES + GradebookProtocol.PARAM_ASSIGN 
                    + courseList);
        } catch (IOException e) {
            return -1;
        }

        return 0;
    }

    private int addCourse(TcpUser user, GradebookProtocol message) {
        String token = message.getParam(GradebookProtocol.TOKEN);
        TcpUser student = mTcpUsers.get(token);

        if (token == null || student == null || student.getEmail() == null) {
            return -1;
        }

        if (mCourses.get(message.getParam(GradebookProtocol.COURSE_NAME) + " - " + student.getInstitution()) != null) {
            student.addCourse(mCourses.get(message.getParam(GradebookProtocol.COURSE_NAME) + " - " + student.getInstitution()));
        } else {
            return -1;
        }

        // UNCOMMENT FOR FUTURE IMPROVEMENTS
        // mCourses.get(message.getParam(GradebookProtocol.COURSE_NAME) + " - " + student.getInstitution()).addStudent(student);

        try {
            user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                    + GradebookProtocol.ADD_COURSE_ACK + GradebookProtocol.PARAM_SEPERATOR
                    + GradebookProtocol.STATUS + GradebookProtocol.PARAM_ASSIGN 
                    + GradebookProtocol.ACK);
        } catch (IOException e) {
            return -1;
        }

        return 0;
    }

    private int getComponents(TcpUser user, GradebookProtocol message) {
        String token = message.getParam(GradebookProtocol.TOKEN);
        TcpUser student = mTcpUsers.get(token);

        if (token == null || student == null || student.getEmail() == null) {
            return -1;
        }

        Course course = mCourses.get(message.getParam(GradebookProtocol.COURSE_NAME) + " - " + student.getInstitution());
        Vector<GradingComponent> components = course.getComponents();
        String compList = "";

        for (int i = 0; i < components.size(); ++i) {
            compList += components.elementAt(i).getName() + " - " + components.elementAt(i).getQuantity() 
                        + GradebookProtocol.COURSE_END;
        }

        
        try {
            user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                    + GradebookProtocol.COMPONENT_NAMES + GradebookProtocol.PARAM_SEPERATOR
                    + GradebookProtocol.COMPONENT_NAMES + GradebookProtocol.PARAM_ASSIGN 
                    + compList);
        } catch (IOException e) {
            return -1;
        }

        return 0;
    }

    private int getProgress(TcpUser user, GradebookProtocol message) {
        String token = message.getParam(GradebookProtocol.TOKEN);
        TcpUser student = mTcpUsers.get(token);

        if (token == null || student == null || student.getEmail() == null) {
            return -1;
        }

        Course course = mCourses.get(message.getParam(GradebookProtocol.COURSE_NAME) + " - " + student.getInstitution());
        int desiredGrade = Integer.parseInt(message.getParam(GradebookProtocol.DESIRED_SCORE));
        Vector<GradingComponent> components = course.getComponents();
        String scores = message.getParam(GradebookProtocol.COMPONENT_SCORES);
        String[] scoresArr = scores.split(GradebookProtocol.END, 0);

        // add scores to respective components, get each component info
        String[] component_1_scores = scoresArr[0].split(GradebookProtocol.SEPARATOR, 0);
        boolean noScores1 = false;
        int component_1_rem, component_1_avg, component_1_sum = 0;
        if (!component_1_scores[0].isEmpty()) {
            component_1_rem = components.elementAt(0).getQuantity() - component_1_scores.length;
            for (int i = 0; i < component_1_scores.length; ++i) {
                components.elementAt(0).addGrades(i, Integer.parseInt(component_1_scores[i]));
                component_1_sum += Integer.parseInt(component_1_scores[i]);
            }
            component_1_avg = component_1_sum / component_1_scores.length;
        } else {
            component_1_rem = components.elementAt(0).getQuantity();
            component_1_sum = desiredGrade * component_1_rem;
            component_1_avg = desiredGrade;
            noScores1 = true;
        }
    
       
        String[] component_2_scores = scoresArr[1].split(GradebookProtocol.SEPARATOR, 0);
        boolean noScores2 = false;
        int component_2_rem, component_2_avg, component_2_sum = 0;
        if (!component_2_scores[0].isEmpty()) {
            component_2_rem = components.elementAt(1).getQuantity() - component_2_scores.length;
            for (int i = 0; i < component_2_scores.length; ++i) {
                components.elementAt(1).addGrades(i, Integer.parseInt(component_2_scores[i]));
                component_2_sum += Integer.parseInt(component_2_scores[i]);
            }
            component_2_avg = component_2_sum / component_2_scores.length;
        } else {
            component_2_rem = components.elementAt(1).getQuantity();
            component_2_sum = desiredGrade * component_2_rem;
            component_2_avg = desiredGrade;
            noScores2 = true;
        }

        String[] component_3_scores = scoresArr[2].split(GradebookProtocol.SEPARATOR, 0);
        boolean noScores3 = false;
        int component_3_rem, component_3_avg, component_3_sum = 0;
        if (!component_3_scores[0].isEmpty()) {
            component_3_rem = components.elementAt(2).getQuantity() - component_3_scores.length;
            for (int i = 0; i < component_3_scores.length; ++i) {
                components.elementAt(2).addGrades(i, Integer.parseInt(component_3_scores[i]));
                component_3_sum += Integer.parseInt(component_3_scores[i]);
            }
            component_3_avg = component_3_sum / component_3_scores.length;
        } else {
            component_3_rem = components.elementAt(2).getQuantity();
            component_3_sum = desiredGrade * component_3_rem;
            component_3_avg = desiredGrade;
            noScores3 = true;
        }

        String result = "";
        int overallAvg = ((component_1_avg*(components.elementAt(0).getWeight())) + (component_2_avg*(components.elementAt(1).getWeight()))
                            + (component_3_avg*(components.elementAt(2).getWeight())))/100;
        if (overallAvg >= desiredGrade) {
            result += "Overall: On Track - Your average is " + overallAvg + GradebookProtocol.END;
        } else {
            result += "Overall: Below Par - Your average is " + overallAvg + GradebookProtocol.END;
        }

        if (noScores1) {
            result += components.elementAt(0).getName() + ": No scores added (Average = Desired Score)"
                        + GradebookProtocol.END;
        } else { 
            if (component_1_avg >= desiredGrade) {
                result += components.elementAt(0).getName() + ": On Track";
            } else {
                result += components.elementAt(0).getName() + ": Below Par";
            }

            if (component_1_rem != 0) {
                int futureScore = ((desiredGrade * components.elementAt(0).getQuantity()) - component_1_sum)/component_1_rem;
                result += " - You need to score " + futureScore + " in the remaining units." + GradebookProtocol.END;
            } else {
                result += " - All units are graded (average: " + component_1_avg + ")." + GradebookProtocol.END;;
            }
        }

        if (noScores2) {
            result += components.elementAt(1).getName() + ": No scores added (Average = Desired Score)"
                        + GradebookProtocol.END;
        } else { 
            if (component_2_avg >= desiredGrade) {
                result += components.elementAt(1).getName() + ": On Track";
            } else {
                result += components.elementAt(1).getName() + ": Below Par";
            }

            if (component_2_rem != 0) {
                int futureScore = ((desiredGrade * components.elementAt(1).getQuantity()) - component_2_sum)/component_2_rem;
                result += " - You need to score " + futureScore + " in the remaining units." + GradebookProtocol.END;
            } else {
                result += " - All units are graded (average: " + component_2_avg + ")." + GradebookProtocol.END;;
            }
        }

        if (noScores3) {
            result += components.elementAt(2).getName() + ": No scores added (Average = Desired Score)"
                        + GradebookProtocol.END; 
        } else {
            if (component_3_avg >= desiredGrade) {
                result += components.elementAt(2).getName() + ": On Track";
            } else {
                result += components.elementAt(2).getName() + ": Below Par";
            }

            if (component_3_rem != 0) {
                int futureScore = ((desiredGrade * components.elementAt(2).getQuantity()) - component_3_sum)/component_3_rem;
                result += " - You need to score " + futureScore + " in the remaining units." + GradebookProtocol.END;
            } else {
                result += " - All units are graded (average: " + component_3_avg + ")." + GradebookProtocol.END;;
            }
        }

        try {
            user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                    + GradebookProtocol.PROGRESS_RESULT + GradebookProtocol.PARAM_SEPERATOR
                    + GradebookProtocol.RESULT + GradebookProtocol.PARAM_ASSIGN + result);
        } catch (IOException e) {
            return -1;
        }

        return 0;
    }

    private int chat(TcpUser user, GradebookProtocol message) {
        String token = message.getParam(GradebookProtocol.TOKEN);
        TcpUser origUser = mTcpUsers.get(token);

        if (token == null || origUser == null || origUser.getEmail() == null) {
            return -1;
        }

        try {
            user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                    + GradebookProtocol.CHAT_COMMAND + GradebookProtocol.PARAM_SEPERATOR
                    + GradebookProtocol.USER_NAME + GradebookProtocol.PARAM_ASSIGN 
                    + origUser.getName() + GradebookProtocol.PARAM_SEPERATOR
                    + GradebookProtocol.COURSE_INSTITUTION + GradebookProtocol.PARAM_ASSIGN 
                    + origUser.getInstitution());
        } catch (IOException e) {
            return -1;
        }

        return 0;
    }
    
    private int logOut(TcpUser user, GradebookProtocol message) {
        String token = message.getParam(GradebookProtocol.TOKEN);
        TcpUser destUser = mTcpUsers.get(token);

        if (token == null || destUser == null || destUser.getEmail() == null) {
            return -1;
        }
        
        try {
            user.sendMessage(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN
                    + GradebookProtocol.LOGOUT_ACK + GradebookProtocol.PARAM_SEPERATOR
                    + GradebookProtocol.STATUS + GradebookProtocol.PARAM_ASSIGN 
                    + GradebookProtocol.ACK);
        } catch (IOException e) {
            return -1;
        }

        System.out.println(destUser.getEmail() + " logged out" + "\n");
        return 0;
    }
}
