package com.byteshaft.foodtruck;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SupportActivity extends AppCompatActivity {

    TextView textView;
    EditText mName;
    EditText mSubject;
    EditText mDetails;
    Button mSendButton;


    String mNameString;
    String mSubjectString;
    String mDetailsString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);
        setTitle("Support");
        textView = (TextView) findViewById(R.id.description_text);
        mSubject = (EditText) findViewById(R.id.subject_et);
        mName = (EditText) findViewById(R.id.name_et);
        mSendButton = (Button) findViewById(R.id.submit_button);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "mubangak3@gmail.com", null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, mSubjectString);
                    emailIntent.putExtra(Intent.EXTRA_TEXT, mNameString + "\n" + mDetailsString);
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                }
            }
        });
    }

    public boolean validate() {
        boolean valid = true;

        mSubjectString = mSubject.getText().toString();
        mNameString = mName.getText().toString();

        if (mNameString.isEmpty()) {
            mName.setError("name is required");
            valid = false;
        } else {
            mName.setError(null);
        }

        if (mSubjectString.trim().isEmpty()) {
            mSubject.setError("enter a valid email address");
            valid = false;
        } else {
            mSubject.setError(null);
        }

        if (mDetailsString.trim().isEmpty()) {
            mDetails.setError("please provide details");
        } else {
            mDetails.setError(null);
        }

        return valid;
    }
}
