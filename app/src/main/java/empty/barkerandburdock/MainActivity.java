package empty.barkerandburdock;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    RelativeLayout relativeLayout;
    TextView tvRes;
    Button btnAceDiamonds;
    Button btnAceClubs;
    Button btnTwo;
    Button btnSettings;
    ProgressBar progressBar;
    Button btnPlaying;

    AiController aiController = new AiController();
    DbController dbController = new DbController(this);
    WriteReader writeReader = new WriteReader();

    int modeBarker = 0;
    int modeBurdock = 2;
    boolean isPlayerBurdock = true;
    int matchesCount = 0;
    float tmpExpValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialization();
    }

    @Override
    public void onStart(){
        super.onStart();
        applySettings();
    }

    void initialization() {

        relativeLayout = findViewById(R.id.relativeLayoutMain);
        relativeLayout.setBackgroundResource(R.color.colorDefaultBackground);

        tvRes = findViewById(R.id.tvRes);
        btnAceDiamonds = findViewById(R.id.btnAceDiamonds);
        btnAceClubs = findViewById(R.id.btnAceClubs);
        btnTwo = findViewById(R.id.btnTwo);
        btnSettings = findViewById(R.id.btnSettings);
        progressBar = findViewById(R.id.progressBar);
        btnPlaying = findViewById(R.id.btnPlaying);

        btnAceDiamonds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                playingHumanVsAi(0);
            }
        });
        btnAceClubs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playingHumanVsAi(1);
            }
        });
        btnTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                playingHumanVsAi(2);
            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( MainActivity.this, SettingsActivity.class);
                intent.putExtra("modeBarker", modeBarker);
                intent.putExtra("modeBurdock", modeBurdock);
                startActivity(intent);
            }
        });
        btnPlaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPlaying.setEnabled(false);
                btnSettings.setEnabled(false);
                playingAiVsAi();
            }
        });
    }

    void playingHumanVsAi(int card) {

        int barker = -1;
        int burdock = -1;
        int result;

        if (isPlayerBurdock) {

            burdock = card;
            if (modeBarker == 0) barker = aiController.randomSolution();
            else if (modeBarker == 1) barker = aiController.optimalSolutionBarker();
            result = aiController.getResult(barker, burdock);

            tmpExpValue = (tmpExpValue * matchesCount + (float) result) / ((float) matchesCount + 1);
            matchesCount++;

            if (result > 0) {
                tvRes.setText(String.format(getResources().getString(R.string.txt_lose),
                        result, matchesCount, Float.toString(tmpExpValue)));
                relativeLayout.setBackgroundResource(R.color.colorLose);
            }
            else if (result < 0) {
                tvRes.setText(String.format(getResources().getString(R.string.txt_win),
                        result, matchesCount, Float.toString(tmpExpValue)));
                relativeLayout.setBackgroundResource(R.color.colorWin);
            }
            else if (result == 0) {
                tvRes.setText(String.format(getResources().getString(R.string.txt_dead_heat),
                        result, matchesCount, Float.toString(tmpExpValue)));
                relativeLayout.setBackgroundResource(R.color.colorDeadHeat);
            }

        } else {

            barker = card;
            if (modeBurdock == 0) burdock = aiController.randomSolution();
            else if (modeBurdock == 1) burdock = aiController.optimalSolutionBurdock();
            result = aiController.getResult(barker, burdock);

            tmpExpValue = (tmpExpValue * matchesCount + (float) result) / ((float) matchesCount + 1);
            matchesCount++;

            if (result > 0) {
                tvRes.setText(String.format(getResources().getString(R.string.txt_win),
                        result, matchesCount, Float.toString(tmpExpValue)));
                relativeLayout.setBackgroundResource(R.color.colorWin);
            }
            else if (result < 0) {
                tvRes.setText(String.format(getResources().getString(R.string.txt_lose),
                        result, matchesCount, Float.toString(tmpExpValue)));
                relativeLayout.setBackgroundResource(R.color.colorLose);
            }
            else if (result == 0) {
                tvRes.setText(String.format(getResources().getString(R.string.txt_dead_heat),
                        result, matchesCount, Float.toString(tmpExpValue)));
                relativeLayout.setBackgroundResource(R.color.colorDeadHeat);
            }
        }

        dbController.dbAddRow(barker, burdock, modeBarker, modeBurdock, result);

        writeReader.applyRow(modeBarker, modeBurdock, result);
    }

    void playingAiVsAi() {

        Thread playingThread = new Thread(
                new Runnable() {
                    public void run() {

                        int result = 0;
                        progressBar.setMax(matchesCount);

                        for (int i = 0; i < matchesCount; i++) {

                            int barker = -1;
                            int burdock = -1;


                            if (modeBarker == 0)
                                barker = aiController.randomSolution();
                            else if (modeBarker == 1)
                                barker = aiController.optimalSolutionBarker();

                            if (modeBurdock == 0)
                                burdock = aiController.randomSolution();
                            else if (modeBurdock == 1)
                                burdock = aiController.optimalSolutionBurdock();

                            int res = aiController.getResult(barker, burdock);
                            dbController.dbAddRow(barker, burdock, modeBarker, modeBurdock, res);

                            int progress = i + 1;
                            progressBar.setProgress(progress);

                            Message message = pHandler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putInt("progress", progress);
                            message.setData(bundle);
                            pHandler.sendMessage(message);

                            result += res;
                        }

                        tmpExpValue = ((float) result) / ((float) matchesCount);

                        Message message = eHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("end", true);
                        message.setData(bundle);
                        eHandler.sendMessage(message);

                        writeReader.applyRows(matchesCount, modeBarker, modeBurdock, result);
                    }
                }
        );
        playingThread.setDaemon(true);
        playingThread.start();
    }

    Handler pHandler = new progressHandler();
    Handler eHandler = new endHandler();

    private class progressHandler extends Handler {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            int progress = bundle.getInt("progress");
            tvRes.setText(String.format(
                    getResources().getString(R.string.txt_progress),
                    progress, matchesCount)
            );
        }
    }

    private class endHandler extends Handler {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            if (bundle.getBoolean("end")) {
                tvRes.setText(String.format(getResources().getString(R.string.txt_added_rows),
                        matchesCount, matchesCount, Float.toString(tmpExpValue)));
                btnPlaying.setEnabled(true);
                btnSettings.setEnabled(true);
            }
        }
    }

    public void applySettings() {
        relativeLayout.setBackgroundResource(R.color.colorDefaultBackground);
        Intent intent = getIntent();
        if (intent != null) {
            modeBarker = intent.getIntExtra("modeBarker", modeBarker);
            modeBurdock = intent.getIntExtra("modeBurdock", modeBurdock);
            matchesCount = intent.getIntExtra("matchesCount", matchesCount);

            isPlayerBurdock = intent.getBooleanExtra("isPlayerBurdock", isPlayerBurdock);
            if (isPlayerBurdock) btnTwo.setText(R.string.btn_two_diamonds);
            else btnTwo.setText(R.string.btn_two_clubs);

            if (modeBarker == 2 || modeBurdock == 2) {
                btnAceDiamonds.setVisibility(Button.VISIBLE);
                btnAceClubs.setVisibility(Button.VISIBLE);
                btnTwo.setVisibility(Button.VISIBLE);

                progressBar.setVisibility(ProgressBar.INVISIBLE);
                btnPlaying.setVisibility(Button.INVISIBLE);

                matchesCount = 0;
            } else {
                btnAceDiamonds.setVisibility(Button.INVISIBLE);
                btnAceClubs.setVisibility(Button.INVISIBLE);
                btnTwo.setVisibility(Button.INVISIBLE);

                progressBar.setVisibility(ProgressBar.VISIBLE);
                btnPlaying.setVisibility(Button.VISIBLE);
            }
        }
    }
}