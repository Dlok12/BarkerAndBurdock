package empty.barkerandburdock;

import java.util.Random;

/**
 * Created by user-pc on 21.02.2018.
 *
 * Принятие решений бота
 */

public class AiController {

    Random rnd = new Random();

    // Итог матча
    public int getResult(int barker, int burdock) {
        int[][] res = {
                { 1, -1, 1 },
                { -1, 1, -2 },
                { -1, 2, 0 }
        };
        return res[barker][burdock];
    }

    public int optimalSolutionBarker() {
        int r = rnd.nextInt(5);
        if (r < 3) return 0;
        else return 2;
    }

    public int optimalSolutionBurdock() {
        int r = rnd.nextInt(5);
        if (r < 3) return 0;
        else return 1;
    }

    public int randomSolution() {
        return rnd.nextInt(3);
    }
}
