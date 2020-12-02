package TeamPacific;

import TeamPacific.Robot;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Refinery extends Robot {

    public Refinery(RobotController rc) throws GameActionException {
        super(rc);
    }

    @Override
    public void run(int turnCount) throws GameActionException {
        //System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }
}
