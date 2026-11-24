package com.example.groupproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button playButton = (Button) findViewById(R.id.play_button);
        Button settingsButton = (Button) findViewById(R.id.settings_button);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDifficultyDialog();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, R.string.settings_soon,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    //choose difficulty
    private void showDifficultyDialog() {
        String[] items = {
                getString(R.string.easy),
                getString(R.string.medium),
                getString(R.string.hard)
        };

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.select_difficulty);
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                Intent intent = new Intent(MainActivity.this, MinesweeperActivity.class);
                intent.putExtra(MinesweeperActivity.EXTRA_DIFFICULTY, which);
                startActivity(intent);
            }
        });
        dialog.show();
    }
}
