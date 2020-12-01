package com.example.gradebook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutionException;

public class GetProgressActivity extends AppCompatActivity {
    private static String token;
    private static String courseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_progress);

        Intent thisIntent = getIntent();
        token = thisIntent.getStringExtra("token");
        courseName = thisIntent.getStringExtra("courseName");
        Log.i("gradebook", "Student checking progress");

        ServerCommunicator message = new ServerCommunicator();
        String response = "";
        try {
            response = message.execute(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN +
                    GradebookProtocol.GET_COMPONENTS + GradebookProtocol.PARAM_SEPERATOR + GradebookProtocol.COURSE_NAME
                    + GradebookProtocol.PARAM_ASSIGN + courseName + GradebookProtocol.PARAM_SEPERATOR +
                    GradebookProtocol.TOKEN + GradebookProtocol.PARAM_ASSIGN + token).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String componentNames = response.substring(response.indexOf(GradebookProtocol.COMPONENT_NAMES + GradebookProtocol.PARAM_ASSIGN)
                + GradebookProtocol.COMPONENT_NAMES.length() + GradebookProtocol.PARAM_ASSIGN.length());
        String[] componentsArr = componentNames.split(GradebookProtocol.END, 0);
        String components = "";
        for (int i = 0; i < componentsArr.length; ++i) {
            components += "Component " + (i + 1) + ": " + componentsArr[i] + "\n";
        }
        System.out.println(components);
        TextView componentList = (TextView) findViewById(R.id.component_list_server);
        components = courseName + " Grading Components: " + components;
        componentList.setText(components);
    }

    public void getProgress(View view) {
        String availableScores = "";
        EditText scores1 = findViewById(R.id.component_1_scores);
        availableScores += scores1.getText().toString().replaceAll("\\s", "") + GradebookProtocol.END;
        EditText scores2 = findViewById(R.id.component_2_scores);
        availableScores += scores2.getText().toString().replaceAll("\\s", "") + GradebookProtocol.END;
        EditText scores3 = findViewById(R.id.component_3_scores);
        availableScores += scores3.getText().toString().replaceAll("\\s", "") + GradebookProtocol.END;
        EditText desired_score = findViewById(R.id.desired_score);
        String desiredScore = desired_score.getText().toString().trim();

        ServerCommunicator message = new ServerCommunicator();
        String response = "";
        try {
            response = message.execute(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN +
                    GradebookProtocol.CHECK_PROGRESS_CMD + GradebookProtocol.PARAM_SEPERATOR + GradebookProtocol.COURSE_NAME +
                    GradebookProtocol.PARAM_ASSIGN + courseName + GradebookProtocol.PARAM_SEPERATOR +
                    GradebookProtocol.COMPONENT_SCORES + GradebookProtocol.PARAM_ASSIGN + availableScores
                    + GradebookProtocol.PARAM_SEPERATOR + GradebookProtocol.DESIRED_SCORE + GradebookProtocol.PARAM_ASSIGN
                    + desiredScore + GradebookProtocol.PARAM_SEPERATOR + GradebookProtocol.TOKEN +
                    GradebookProtocol.PARAM_ASSIGN + token).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String resultStr = response.substring(response.indexOf(GradebookProtocol.RESULT + GradebookProtocol.PARAM_ASSIGN) +
                GradebookProtocol.RESULT.length() + GradebookProtocol.PARAM_ASSIGN.length());
        resultStr = resultStr.replace(GradebookProtocol.END, "\n");
        TextView progressResult = (TextView) findViewById(R.id.progress_result);
        progressResult.setText(resultStr);
    }

    public void chat(View view) {
        ServerCommunicator message = new ServerCommunicator();
        String response = "";
        try {
            response = message.execute(GradebookProtocol.TYPE_KEY + GradebookProtocol.PARAM_ASSIGN +
                    GradebookProtocol.CHAT_COMMAND + GradebookProtocol.PARAM_SEPERATOR +
                    GradebookProtocol.TOKEN + GradebookProtocol.PARAM_ASSIGN + token).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GradebookProtocol chat_info = new GradebookProtocol(response);
        Intent intent = new Intent(this, ChatroomActivity.class);
        intent.putExtra("room_name", courseName + " - " + chat_info.getParam(GradebookProtocol.COURSE_INSTITUTION));
        intent.putExtra("user_name", chat_info.getParam(GradebookProtocol.USER_NAME));
        startActivity(intent);
    }
}
