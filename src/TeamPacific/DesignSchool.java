package TeamPacific;

import battlecode.common.*;
import static TeamPacific.Blockchain.*;
import static TeamPacific.RobotPlayer.*;

public class DesignSchool extends Robot{

    static int buildCountD = 0;

    public DesignSchool(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    void run(int turnCount) throws GameActionException {

        if (buildCountD < 1) {
            for (Direction dir : directions) {
                if(tryBuild(RobotType.LANDSCAPER, dir)) {
                    buildCountD++;
                }
            }
        }
    }
}
