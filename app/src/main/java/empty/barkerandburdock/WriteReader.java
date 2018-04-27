package empty.barkerandburdock;

import android.app.Activity;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by user-pc on 21.02.2018.
 *
 * Запись и чтение полученных результатов, сбор статистики матчей
 */

public class WriteReader extends Activity {

    /// Структура файла:
    /// count ; r r /
    /// count ; r o /
    /// ... /
    /// count ; h o
    ///
    /// count - количество матчей
    /// r - random
    /// o - optimal
    /// h - human
    ///
    /// 1 - barker
    /// 2 - burdock

    final String fileName = "Statistic.txt";
    final String filePath = Environment.getExternalStorageDirectory().toString() + "/BarkerAndBurdock/" + fileName;
    final String modeSplitter = "/";
    final String countSplitter = ";";

    public void writeFile(String string) {
        try {
            File file = new File(filePath);
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeFile(long[] counts, float[] expValues) {
        String s = "";
        if (counts.length == expValues.length) {
            for (int i = 0; i < counts.length; i++) {
                s += Long.toString(counts[i]) + countSplitter + Float.toString(expValues[i]) + modeSplitter;
            }

            writeFile(s);
        }
    }

    public void writeFile(String[][] strArr) {
        String s = "";
        for (int i = 0; i < strArr[0].length; i++) {
            s += strArr[0][i] + countSplitter + strArr[1][i] + modeSplitter;
        }

        writeFile(s);
    }

    public void writeFile(long count, float expValue, int index) {
        if (index < 8) {
            String[][] strArr = getResult();
            strArr[0][index] = Long.toString(count);
            strArr[1][index] = Float.toString(expValue);
            writeFile(strArr);
        }
    }

    public void writeNewFile() {
        String s = "";
        for (int i = 0; i < 8; i++) {
            s += "0" + countSplitter + "0" + modeSplitter;
        }

        writeFile(s);
    }

    public StringBuilder readFile() {
        StringBuilder stringBuilder = new StringBuilder();
        File myFile = new File(filePath);
        if (!myFile.exists())
            writeNewFile();
        try {
            FileInputStream inputStream = new FileInputStream(myFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null){
                    stringBuilder.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stringBuilder;
    }

    public String[][] getResult() {

            String[][] result = new String[2][8];
            String[] countsAndExpVal = readFile().toString().split(modeSplitter);
            for (int i = 0; i < countsAndExpVal.length; i++) {
                String[] tmp = countsAndExpVal[i].split(countSplitter);
                if (tmp.length == 2) {
                    result[0][i] = tmp[0];  // count
                    result[1][i] = tmp[1];  // expValues
                } else {
                    result[0][i] = "0";     // count
                    result[1][i] = "0";     // expValues
                }
            }

        return result;
    }

    public int getIndex(int modeBarker, int modeBurdock) {
        int[][] res = {
                {0, 1, 2},
                {3, 4, 5},
                {6, 7, 8}
        };

        return res[modeBarker][modeBurdock];
    }

    public void applyRow(int modeBarker, int modeBurdock, int result) {

        String strArr[][] = getResult();
        int index = getIndex(modeBarker, modeBurdock);

        long count = 0;
        float expValue = 0;

        if (strArr[0][index] != null)
            count = Long.parseLong(strArr[0][index]);
        if (strArr[1][index] != null)
            expValue = (((Float.parseFloat(strArr[1][index]) * count) + result) / (count + 1));

        writeFile(count + 1, expValue, index);
    }

    public void applyRows(long count, int modeBarker, int modeBurdock, int sumResult) {

        String strArr[][] = getResult();
        int index = getIndex(modeBarker, modeBurdock);

        long c = 0;
        float expValue = 0;

        if (strArr[0][index] != null)
            c = Long.parseLong(strArr[0][index]);
        if (strArr[1][index] != null)
            expValue = (((Float.parseFloat(strArr[1][index]) * count) + sumResult) / (count + c));

        writeFile(count + c, expValue, index);
    }
}
