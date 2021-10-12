package com.example.gradebook;

import java.util.HashMap;

public class GradebookProtocol {
    // String constants
    public static final String PARAM_SEPERATOR = "&";
    public static final String PARAM_ASSIGN = "=";
    public static final String TYPE_KEY = "type";
    public static final String LOGIN_COMMAND = "log_in";
    public static final String LOGIN_EMAIL = "email";
    public static final String LOGIN_PASSWORD = "password";
    public static final String LOGIN_ACK = "log_in_ack";
    public static final String STATUS = "status";
    public static final String ACK = "ok";
    public static final String SIGNUP_COMMAND = "sign_up";
    public static final String SIGNUP_ACK = "sign_up_ack";
    public static final String NAME = "name";
    public static final String INSTITUTION = "institution";
    public static final String USER_TYPE = "user_type";
    public static final String STUDENT = "student";
    public static final String INSTRUCTOR = "instructor";
    public static final String VIEW_COURSES_CMD = "view_courses";
    public static final String VIEW_COURSES_ACK = "view_courses_ack";
    public static final String ADD_COURSE_INSTRUCTOR = "add_course_instructor";
    public static final String ADD_COURSE_STUDENT = "add_course_student";
    public static final String ADD_COURSE_ACK = "add_course_ack";
    public static final String COURSE_NAME = "course_name";
    public static final String COURSE_INSTITUTION = "course_institution";
    public static final String COURSE_CREDITS = "course_credits";
    public static final String COMPONENTS = "components";
    public static final String COMPONENT_NAME = "component_name";
    public static final String COMPONENT_NAMES = "component_names";
    public static final String COMPONENT_SCORES = "component_scores";
    public static final String COMPONENT_QUANTITY = "component_quantity";
    public static final String COMPONENT_WEIGHT = "component_weight";
    public static final String INVALID_TOKEN = "failed";
    public static final String SEND_EMAIL_COMMAND = "send_email";
    public static final String SEND_EMAIL_ACK = "send_email_ack";
    public static final String RETRIEVE_EMAIL_COMMAND = "retrieve_emails";
    public static final String COURSES = "courses";
    public static final String EMAILS_INBOX = "emails";
    public static final String EMAIL_SENDER = "from";
    public static final String EMAIL_RECEIVER = "to";
    public static final String EMAIL_BODY = "body";
    public static final String SEPARATOR = ";";
    public static final String COMPONENT_CONTENT = ">";
    public static final String END = "ZZZ";
    public static final String LOGOUT_COMMAND = "log_out";
    public static final String LOGOUT_ACK = "log_out_ack";
    public static final String TOKEN = "token";
    public static final String GET_COMPONENTS = "get_components";
    public static final String CHECK_PROGRESS_CMD = "check_progress";
    public static final String PROGRESS_RESULT = "progress_result";
    public static final String RESULT = "result";
    public static final String DESIRED_SCORE = "desired_score";
    public static final String CHAT_COMMAND = "chat_info";
    public static final String USER_NAME = "user_name";

    private static HashMap<String, String> protocolMap = new HashMap<String, String>();

    public GradebookProtocol(String clientMessage) {
        parseStr(clientMessage);
    }

    public void putParam(String key, String val) {
        protocolMap.put(key, val);
    }

    public String getParam(String key) {
        return protocolMap.get(key);
    }

    public void parseStr(String clientMessage) {
        String[] arr = clientMessage.split(PARAM_SEPERATOR, 0);
        for (int i = 0; i < arr.length; ++i) {
            protocolMap.put(arr[i].substring(0, arr[i].indexOf(PARAM_ASSIGN)),
                    arr[i].substring(arr[i].indexOf(PARAM_ASSIGN) + PARAM_ASSIGN.length()));
        }
    }
}
