package TeamPacific;

import TeamPacific.Robot;
import battlecode.common.*;
import static TeamPacific.Blockchain.*;
import static TeamPacific.RobotPlayer.*;

public class FulfillmentCenter extends Robot {

    public static int buildCountF = 0;

    public FulfillmentCenter(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    public void run(int turnCount) throws GameActionException {
        if (buildCountF < 4) {
            for (Direction dir : directions) {
                if(tryBuild(RobotType.DELIVERY_DRONE, dir)) {
                    ++buildCountF;
                }
            }
        }
    }
}
