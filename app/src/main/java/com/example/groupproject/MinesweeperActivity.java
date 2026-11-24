package com.example.groupproject;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;


public class MinesweeperActivity extends AppCompatActivity {

    //getting the difficulty
    public static final String EXTRA_DIFFICULTY = "difficulty";

    // how many rows/colums/mines on each difficulty
    private static final int[][] LEVELS = {
            {9, 9, 10},
            {16, 12, 30},
            {20, 12, 50}
    };

    private Game game;
    private BoardView boardView;
    private TextView minesText;
    private TextView timerText;

    private int difficulty;
    private int seconds;
    private boolean timerRunning = false;

    // keep running the runnable every second
    private final Handler handler = new Handler();
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            seconds++;
            updateDisplays();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minesweeper);

        boardView = (BoardView) findViewById(R.id.board_view);
        minesText = (TextView) findViewById(R.id.mines_text);
        timerText = (TextView) findViewById(R.id.timer_text);
        Button restartButton = (Button) findViewById(R.id.restart_button);

        difficulty = getIntent().getIntExtra(EXTRA_DIFFICULTY, 0);
        if (difficulty < 0 || difficulty >= LEVELS.length) {
            difficulty = 0;
        }

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame();
            }
        });

        boardView.setOnCellActionListener(new BoardView.OnCellActionListener() {
            @Override
            public void onReveal(int row, int col) {
                game.reveal(row, col);
                afterMove();
            }

            @Override
            public void onFlag(int row, int col) {
                game.toggleFlag(row, col);
                afterMove();
            }
        });

        startNewGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);   // stop the timer when not on game
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timerRunning) {
            handler.postDelayed(tick, 1000);
        }
    }

    // making new game
    private void startNewGame() {
        stopTimer();
        seconds = 0;
        int[] level = LEVELS[difficulty];
        game = new Game(level[0], level[1], level[2]);
        boardView.setGame(game);
        updateDisplays();
    }

    //called whenever the player does something
    private void afterMove() {
        if (game.isStarted()) {
            startTimer();   // does nothing if it is already running
        }
        boardView.invalidate();   // redraw board
        updateDisplays();

        if (game.isWon() || game.isLost()) {
            stopTimer();
            showEndDialog();
        }
    }

    private void startTimer() {
        if (!timerRunning) {
            timerRunning = true;
            handler.postDelayed(tick, 1000);
        }
    }

    private void stopTimer() {
        timerRunning = false;
        handler.removeCallbacks(tick);
    }

    private void updateDisplays() {
        minesText.setText(String.format(Locale.getDefault(), "%03d", game.getMinesLeft()));
        timerText.setText(String.format(Locale.getDefault(), "%03d", seconds));
    }

    //win/lose with quit or start again
    private void showEndDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        if (game.isWon()) {
            dialog.setTitle(R.string.win_title);
            dialog.setMessage(getString(R.string.win_message, seconds));
        } else {
            dialog.setTitle(R.string.lose_title);
            dialog.setMessage(getString(R.string.lose_message));
        }
        dialog.setCancelable(false);
        dialog.setPositiveButton(R.string.play_again,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int buttonId) {
                        startNewGame();
                    }
                });
        dialog.setNegativeButton(R.string.menu,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int buttonId) {
                        finish();   // return to menu
                    }
                });
        dialog.show();
    }
}
