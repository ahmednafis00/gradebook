package com.example.gradebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class InstructorWelcomeActivity extends AppCompatActivity {
    private static String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_welcome);

        Intent thisIntent = getIntent();
        token = thisIntent.getStringExtra("token");
        Log.i("gradebook", "Instructor viewing courses");

        ListView coursesListView = findViewById(R.id.course_list);
        ServerCommunicator message = new ServerCommunicator();
        String response = "";
        try {
            response = message.execute(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN +
                    GradebookProtocol.VIEW_COURSES_CMD + GradebookProtocol.PARAM_SEPERATOR +
                    GradebookProtocol.TOKEN + GradebookProtocol.PARAM_ASSIGN + token).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String courseStr = response.substring(response.indexOf(GradebookProtocol.COURSES + GradebookProtocol.PARAM_ASSIGN)
                + GradebookProtocol.COURSES.length() + GradebookProtocol.PARAM_ASSIGN.length());
        System.out.println("Viewing Courses: " + courseStr);
        String[] courseArr = courseStr.split(GradebookProtocol.END, 0);

        if (courseStr.equals(GradebookProtocol.END)) {
            courseArr = new String[]{"There are no courses in your gradebook."};
        }

        final ArrayList<String> courseArrayList = new ArrayList<>(Arrays.asList(courseArr)); // add course names to list
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, courseArrayList);
        coursesListView.setAdapter(arrayAdapter);

        //add listener to coursesListView
        final String[] chat_server_response = {""};
        coursesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!((TextView) view).getText().toString().equals("There are no courses in your gradebook.")) {
                    ServerCommunicator message = new ServerCommunicator();
                    try {
                        chat_server_response[0] = message.execute(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN +
                                GradebookProtocol.CHAT_COMMAND + GradebookProtocol.PARAM_SEPERATOR +
                                GradebookProtocol.TOKEN + GradebookProtocol.PARAM_ASSIGN + token).get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    GradebookProtocol chat_info = new GradebookProtocol(chat_server_response[0]);
                    Intent intent = new Intent(getApplicationContext(), ChatroomActivity.class);
                    intent.putExtra("room_name", ((TextView) view).getText().toString() + " - " +
                            chat_info.getParam(GradebookProtocol.COURSE_INSTITUTION));
                    intent.putExtra("user_name", chat_info.getParam(GradebookProtocol.USER_NAME));
                    startActivity(intent);
                }
            }
        });
    }

    public void createCourse(View view) {
        Intent intent = new Intent(this, CreateCourseActivity.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }

    public void instructorLogOut(View view) {
        Log.i("Gradebook", "Instructor logging out");
        ServerCommunicator message = new ServerCommunicator();
        String response = "";

        try {
            response = message.execute(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN +
                    GradebookProtocol.LOGOUT_COMMAND + GradebookProtocol.PARAM_SEPERATOR +
                    GradebookProtocol.TOKEN + GradebookProtocol.PARAM_ASSIGN + token).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GradebookProtocol logout_ack = new GradebookProtocol(response);
        if (logout_ack.getParam(GradebookProtocol.STATUS).equals(GradebookProtocol.ACK)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            System.out.println("Token: " + token + " has successfully logged out.");
        } else {
            System.out.println("Log out unsuccessful.");
        }
    }
}
