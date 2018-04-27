package empty.barkerandburdock;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Created by user-pc on 06.03.2018.
 *
 * Настройки матчей
 */

public class SettingsActivity extends AppCompatActivity {

    Spinner spModeBarker;
    Spinner spModeBurdock;
    EditText edMatchesCount;

    int modeBarker = 1;
    int modeBurdock = 1;
    boolean isPlayerBurdock = true;
    int matchesCount = 0;

    DbController dbController = new DbController(this);
    WriteReader writeReader = new WriteReader();

    String[] modes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initialization();

        Intent intent = getIntent();
        if (intent != null) {
            modeBarker = intent.getIntExtra("modeBarker", modeBarker);
            modeBurdock = intent.getIntExtra("modeBurdock", modeBurdock);
        }
    }

    void sendSettings() {
        Intent intent = new Intent( SettingsActivity.this, MainActivity.class);
        intent.putExtra("modeBarker", modeBarker);
        intent.putExtra("modeBurdock", modeBurdock);
        intent.putExtra("isPlayerBurdock", isPlayerBurdock);
        intent.putExtra("matchesCount", matchesCount);
        startActivity(intent);
    }

    void initialization() {

        modes = new String[] {
                getResources().getString(R.string.sp_mode_random),
                getResources().getString(R.string.sp_mode_optimal),
                getResources().getString(R.string.sp_mode_human)
        };

        spModeBarker = findViewById(R.id.spModeBarker);
        spModeBurdock = findViewById(R.id.spModeBurdock);
        edMatchesCount = findViewById(R.id.edMatchesCount);

        Button btnApply = findViewById(R.id.btnApply);
        Button btnRefresh = findViewById(R.id.btnRefresh);

        final GridView gvExpValues = findViewById(R.id.gvExpValues);

        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, modes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spModeBarker.setAdapter(adapter);
        spModeBarker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                modeBarker = (int)spModeBarker.getSelectedItemId();
                if (modeBarker == 2) {
                    if (modeBurdock == 2) {
                        modeBurdock = 1;
                        spModeBurdock.setSelection(modeBurdock);
                        Toast.makeText(SettingsActivity.this,
                                getResources().getString(R.string.toast_burdock_changed), Toast.LENGTH_SHORT).show();
                    }
                    isPlayerBurdock = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                modeBarker = 0;
                spModeBarker.setSelection(modeBarker);
            }
        });
        spModeBarker.setSelection(modeBarker);

        spModeBurdock.setAdapter(adapter);
        spModeBurdock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                modeBurdock = (int)spModeBurdock.getSelectedItemId();
                if (modeBurdock == 2) {
                    if (modeBarker == 2) {
                        modeBarker = 1;
                        spModeBarker.setSelection(modeBarker);
                        Toast.makeText(SettingsActivity.this,
                                getResources().getString(R.string.toast_barker_changed), Toast.LENGTH_SHORT).show();
                    }
                    isPlayerBurdock = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                modeBurdock = 2;
                spModeBurdock.setSelection(modeBurdock);
            }
        });
        spModeBurdock.setSelection(modeBurdock);

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                matchesCount = Integer.parseInt(edMatchesCount.getText().toString());
                sendSettings();
                finish();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshStatistic();
                printStatistic(gvExpValues);
            }
        });

        printStatistic(gvExpValues);
    }

    void printStatistic(GridView gvExpValues) {
        DataAdapter.SetArray(loadStatistic());

        gvExpValues.setNumColumns(4);
        gvExpValues.setAdapter(new DataAdapter(this, android.R.layout.simple_list_item_1));
    }

    String[] loadStatistic() {
        String[][] strArr = writeReader.getResult();

        for (int i = 0; i < 8; i++) {
            if (strArr[1][i] == null)
                strArr[1][i] = "0";
            if (strArr[1][i].length() < 6)
                strArr[1][i] += "      ";
            strArr[1][i] = strArr[1][i].substring(0, 5);
        }

        return new String[] {
                getResources().getString(R.string.gv_title_empty),
                getResources().getString(R.string.gv_title_random),
                getResources().getString(R.string.gv_title_optimal),
                getResources().getString(R.string.gv_title_human),
                getResources().getString(R.string.gv_title_random), strArr[1][0], strArr[1][1], strArr[1][2],
                getResources().getString(R.string.gv_title_optimal), strArr[1][3], strArr[1][4], strArr[1][5],
                getResources().getString(R.string.gv_title_human), strArr[1][6], strArr[1][7],
                getResources().getString(R.string.gv_title_empty)
        };
    }

    void refreshStatistic() {
        long[][] statistic = dbController.refreshStatistic();

        long[] counts = new long[8];
        float[] expValues = new float[8];

        for (int i = 0; i < 8; i++) {
            counts[i] = statistic[0][i];
            if (counts[i] > 0)
                expValues[i] = (float) ((double) statistic[1][i] / (double) statistic[0][i]);
        }

        writeReader.writeFile(counts, expValues);
    }
}

class DataAdapter extends ArrayAdapter<String> {

    private static String[] arr;

    DataAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId, arr);
    }

    static void SetArray(String[] array) {
        arr = array;
    }
}

