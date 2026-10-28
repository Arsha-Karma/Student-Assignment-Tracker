
package com.example.studentassignmenttracker;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText title, subject, dueDate;
    private Button b1, b2;
    private LinearLayout assignmentsContainer;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = findViewById(R.id.title);
        subject = findViewById(R.id.subject);
        dueDate = findViewById(R.id.dueDate);
        b1 = findViewById(R.id.b1);
        b2 = findViewById(R.id.b2);
        assignmentsContainer = findViewById(R.id.assignmentsContainer);

        dbHelper = new DatabaseHelper(this);

        b1.setOnClickListener(v -> addAssignment());
        b2.setOnClickListener(v -> showAssignments());
    }

    private void addAssignment() {
        String t = title.getText().toString().trim();
        String s = subject.getText().toString().trim();
        String d = dueDate.getText().toString().trim();

        if (t.isEmpty()) {
            title.setError(getString(R.string.enter_title_error));
            return;
        }

        if (s.isEmpty()) {
            subject.setError(getString(R.string.enter_subject_error));
            return;
        }

        if (d.isEmpty()) {
            dueDate.setError(getString(R.string.enter_date_error));
            return;
        }

        boolean inserted = dbHelper.addAssignment(t, s, d);

        if (inserted) {
            Toast.makeText(
                    this,
                    R.string.assignment_added_success,
                    Toast.LENGTH_SHORT
            ).show();

            title.setText("");
            subject.setText("");
            dueDate.setText("");

            // Keep assignment details hidden after adding.
            assignmentsContainer.removeAllViews();

        } else {
            Toast.makeText(
                    this,
                    R.string.add_failed,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void showAssignments() {
        assignmentsContainer.removeAllViews();

        Cursor cursor = dbHelper.getAllAssignments();
        int count = 0;

        try {
            while (cursor.moveToNext()) {
                count++;

                long id = cursor.getLong(0);
                String assignmentTitle = cursor.getString(1);
                String assignmentSubject = cursor.getString(2);
                String assignmentDueDate = cursor.getString(3);
                String status = cursor.getString(4);

                addAssignmentCard(
                        id,
                        assignmentTitle,
                        assignmentSubject,
                        assignmentDueDate,
                        status
                );
            }
        } finally {
            cursor.close();
        }

        if (count == 0) {
            TextView emptyMessage = new TextView(this);
            emptyMessage.setText(R.string.no_assignments_message);
            emptyMessage.setTextSize(16);
            emptyMessage.setTextColor(Color.GRAY);
            emptyMessage.setGravity(Gravity.CENTER);
            emptyMessage.setPadding(16, 28, 16, 28);

            assignmentsContainer.addView(emptyMessage);
        }
    }

    private void addAssignmentCard(
            long id,
            String assignmentTitle,
            String assignmentSubject,
            String assignmentDueDate,
            String status) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 16, 16, 16);

        GradientDrawable cardBackground = new GradientDrawable();
        cardBackground.setColor(Color.WHITE);
        cardBackground.setCornerRadius(20);
        cardBackground.setStroke(1, Color.rgb(225, 218, 240));

        card.setBackground(cardBackground);
        card.setElevation(4);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);

        TextView titleView = new TextView(this);
        titleView.setText(assignmentTitle);
        titleView.setTextSize(19);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setTextColor(Color.rgb(81, 45, 168));
        titleView.setPadding(0, 0, 0, 10);

        card.addView(titleView);

        TextView detailsView = new TextView(this);
        detailsView.setText(getString(
                R.string.assignment_details,
                assignmentSubject,
                assignmentDueDate,
                status
        ));
        detailsView.setTextSize(15);
        detailsView.setTextColor(Color.rgb(70, 70, 70));
        detailsView.setLineSpacing(6, 1);

        card.addView(detailsView);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        rowParams.setMargins(0, 14, 0, 0);
        buttonRow.setLayoutParams(rowParams);

        Button updateButton = new Button(this);
        updateButton.setText(R.string.update);
        updateButton.setAllCaps(false);
        updateButton.setTextColor(Color.WHITE);
        updateButton.setBackgroundTintList(
                ColorStateList.valueOf(Color.rgb(103, 58, 183))
        );

        Button deleteButton = new Button(this);
        deleteButton.setText(R.string.delete);
        deleteButton.setAllCaps(false);
        deleteButton.setTextColor(Color.WHITE);
        deleteButton.setBackgroundTintList(
                ColorStateList.valueOf(Color.rgb(211, 47, 47))
        );

        LinearLayout.LayoutParams updateParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                );

        updateParams.setMargins(0, 0, 8, 0);
        buttonRow.addView(updateButton, updateParams);

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                );

        deleteParams.setMargins(8, 0, 0, 0);
        buttonRow.addView(deleteButton, deleteParams);

        card.addView(buttonRow);
        assignmentsContainer.addView(card);

        updateButton.setOnClickListener(v ->
                showUpdateDialog(
                        id,
                        assignmentTitle,
                        assignmentSubject,
                        assignmentDueDate
                )
        );

        deleteButton.setOnClickListener(v -> confirmDelete(id));
    }

    private void showUpdateDialog(
            long id,
            String oldTitle,
            String oldSubject,
            String oldDueDate) {

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(24, 8, 24, 0);

        EditText titleInput = new EditText(this);
        titleInput.setHint(R.string.enter_assignment_title);
        titleInput.setText(oldTitle);
        titleInput.setSingleLine(true);
        form.addView(titleInput);

        EditText subjectInput = new EditText(this);
        subjectInput.setHint(R.string.enter_subject);
        subjectInput.setText(oldSubject);
        subjectInput.setSingleLine(true);
        form.addView(subjectInput);

        EditText dateInput = new EditText(this);
        dateInput.setHint(R.string.date_format_hint);
        dateInput.setText(oldDueDate);
        dateInput.setSingleLine(true);
        form.addView(dateInput);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.update_assignment)
                .setView(form)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
        ).setOnClickListener(v -> {

            String newTitle = titleInput.getText().toString().trim();
            String newSubject = subjectInput.getText().toString().trim();
            String newDueDate = dateInput.getText().toString().trim();

            if (newTitle.isEmpty()) {
                titleInput.setError(
                        getString(R.string.enter_title_error)
                );
                return;
            }

            if (newSubject.isEmpty()) {
                subjectInput.setError(
                        getString(R.string.enter_subject_error)
                );
                return;
            }

            if (newDueDate.isEmpty()) {
                dateInput.setError(
                        getString(R.string.enter_date_error)
                );
                return;
            }

            boolean updated = dbHelper.updateAssignment(
                    id, newTitle, newSubject, newDueDate
            );

            if (updated) {
                Toast.makeText(
                        this,
                        R.string.assignment_updated_success,
                        Toast.LENGTH_SHORT
                ).show();

                dialog.dismiss();
                showAssignments();
            } else {
                Toast.makeText(
                        this,
                        R.string.update_failed,
                        Toast.LENGTH_SHORT
                ).show();
            }
        }));

        dialog.show();
    }

    private void confirmDelete(long id) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_assignment)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.delete, (dialog, which) -> {

                    boolean deleted = dbHelper.deleteAssignment(id);

                    if (deleted) {
                        Toast.makeText(
                                this,
                                R.string.assignment_deleted_success,
                                Toast.LENGTH_SHORT
                        ).show();

                        // Reload the list from the database.
                        showAssignments();
                    } else {
                        Toast.makeText(
                                this,
                                R.string.delete_failed,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
