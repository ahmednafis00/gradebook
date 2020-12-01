package com.example.gradebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class CreateCourseActivity extends AppCompatActivity {
    private static String token;
    private static String components;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_course);

        Intent thisIntent = getIntent();
        token = thisIntent.getStringExtra("token");
        Log.i("gradebook", "Instructor creating a course");
    }

    public void createNewCourse(View view) {
        EditText course_Name = findViewById(R.id.new_course_name);
        String courseName = course_Name.getText().toString().trim();
        EditText course_Institution = findViewById(R.id.new_course_institution);
        String courseInstitution = course_Institution.getText().toString().trim();
        EditText course_Credits = findViewById(R.id.new_course_credits);
        String courseCredits = course_Credits.getText().toString().trim();
        components = "";
        EditText component1 = findViewById(R.id.component_1);
        parseComponentStr(component1.getText().toString().replaceAll("\\s", ""));
        EditText component2 = findViewById(R.id.component_2);
        parseComponentStr(component2.getText().toString().replaceAll("\\s", ""));
        EditText component3 = findViewById(R.id.component_3);
        parseComponentStr(component3.getText().toString().replaceAll("\\s", ""));

        ServerCommunicator message = new ServerCommunicator();
        String response = "";
        try {
            response = message.execute(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN +
                    GradebookProtocol.ADD_COURSE_INSTRUCTOR + GradebookProtocol.PARAM_SEPERATOR +
                    GradebookProtocol.COURSE_NAME + GradebookProtocol.PARAM_ASSIGN + courseName +
                    GradebookProtocol.PARAM_SEPERATOR + GradebookProtocol.COURSE_INSTITUTION +
                    GradebookProtocol.PARAM_ASSIGN + courseInstitution + GradebookProtocol.PARAM_SEPERATOR
                    + GradebookProtocol.COURSE_CREDITS + GradebookProtocol.PARAM_ASSIGN + courseCredits +
                    GradebookProtocol.PARAM_SEPERATOR + GradebookProtocol.COMPONENTS +
                    GradebookProtocol.PARAM_ASSIGN + components + GradebookProtocol.PARAM_SEPERATOR
                    + GradebookProtocol.TOKEN + GradebookProtocol.PARAM_ASSIGN + token).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<String, Object> chatRoomMap = new HashMap<>();
        chatRoomMap.put(courseName + " - " + courseInstitution, "");
        root.updateChildren(chatRoomMap);

        Intent intent = new Intent(this, InstructorWelcomeActivity.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }

    public void parseComponentStr(String componentStr) {
        String[] componentArr = componentStr.split(";", 0);
        components += GradebookProtocol.COMPONENT_NAME + GradebookProtocol.COMPONENT_CONTENT + componentArr[0]
                + GradebookProtocol.SEPARATOR + GradebookProtocol.COMPONENT_QUANTITY + GradebookProtocol.COMPONENT_CONTENT
                + componentArr[1] + GradebookProtocol.SEPARATOR + GradebookProtocol.COMPONENT_WEIGHT
                + GradebookProtocol.COMPONENT_CONTENT + componentArr[2] + GradebookProtocol.END;
    }
}
