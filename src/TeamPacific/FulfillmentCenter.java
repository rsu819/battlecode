package TeamPacific;

import battlecode.common.*;
import static TeamPacific.Blockchain.*;
import static TeamPacific.RobotPlayer.*;

public class FulfillmentCenter extends Robot{

    static int buildCountF = 0;

    public FulfillmentCenter(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    void run(int turnCount) throws GameActionException {
        if (buildCountF < 1) {
            for (Direction dir : directions) {
                if(tryBuild(RobotType.DELIVERY_DRONE, dir)) {
                    buildCountF++;
                }
            }
        }
    }
}
