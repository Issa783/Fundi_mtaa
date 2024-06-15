package com.example.fundimtaa;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class ReportDetailsDialog extends Dialog {
    private ReportDialog.Report report;

    public ReportDetailsDialog(@NonNull Context context, ReportDialog.Report report) {
        super(context);
        this.report = report;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details_dialog);

        TextView textViewJobId = findViewById(R.id.textViewJobId);
        TextView textViewJobTitle = findViewById(R.id.textViewJobTitle);
        TextView textViewJobDescription = findViewById(R.id.textViewJobDescription);
        TextView textViewClientName = findViewById(R.id.textViewClientName);
        TextView textViewClientEmail = findViewById(R.id.textViewClientEmail);
        TextView textViewClientPhone = findViewById(R.id.textViewClientPhone);

        textViewJobId.setText("Job ID: " + report.getJobId());
        textViewJobTitle.setText("Job Title: " + report.getJobName());
        textViewJobDescription.setText("Job Description: " + report.getJobDescription());
        textViewClientName.setText("Client Name: " + report.getClientName());
        textViewClientEmail.setText("Client Email: " + report.getClientEmail());
        textViewClientPhone.setText("Client Phone: " + report.getClientPhone());
    }
}
