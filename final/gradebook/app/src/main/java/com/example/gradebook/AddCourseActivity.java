package com.example.gradebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutionException;

public class AddCourseActivity extends AppCompatActivity {
    private static String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        Intent thisIntent = getIntent();
        token = thisIntent.getStringExtra("token");
        Log.i("gradebook", "Student adding a course");
    }

    public void addCourse(View view) {
        EditText course_name = findViewById(R.id.add_course_name);
        String courseName = course_name.getText().toString().trim();

        ServerCommunicator message = new ServerCommunicator();
        String response = "";
        try {
            response = message.execute(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN +
                    GradebookProtocol.ADD_COURSE_STUDENT + GradebookProtocol.PARAM_SEPERATOR +
                    GradebookProtocol.COURSE_NAME + GradebookProtocol.PARAM_ASSIGN + courseName +
                    GradebookProtocol.PARAM_SEPERATOR + GradebookProtocol.TOKEN
                    + GradebookProtocol.PARAM_ASSIGN + token).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TextView add_course_ack = findViewById(R.id.add_course_ack);
        GradebookProtocol server_ack = new GradebookProtocol(response);
        if (server_ack.getParam(GradebookProtocol.STATUS).equals(GradebookProtocol.ACK)) {
            add_course_ack.setText(R.string.add_course_success);
            Intent intent = new Intent(this, StudentWelcomeActivity.class);
            intent.putExtra("token", token);
            startActivity(intent);
        } else {
            add_course_ack.setText(R.string.add_course_fail);
        }
    }
}
