package TeamPacific;
import battlecode.common.*;

public class  Landscaper {
    // data members
    static int MaxSenseRadius;
    static Team myTeam;
    RobotController rc;
    MapLocation locationHQ;


    Landscaper(RobotController r, MapLocation hq) {
        rc = r;
        locationHQ = hq;
    }

    static Direction[] digAwayFromBldg(Direction bldgDir) {
        Direction[] awayFromBldg = {bldgDir.opposite(), bldgDir.opposite().rotateLeft(), bldgDir.opposite().rotateRight()};
        return awayFromBldg;
    }

    static Direction[] aroundBldg(Direction dir) {
        Direction[] aroundBldg = new Direction[5];
        int index = 0;
        for (Direction d : Direction.allDirections()) {
            if ((Math.abs(d.dx - dir.dx) <= 1 && Math.abs(d.dy - dir.dy) <= 1) && d != dir) {
                System.out.println("surrounding loc: " + d);
                aroundBldg[index] = d;
                ++index;
            }
        }
        return aroundBldg;
    }

    public boolean tryDig(Direction d) throws GameActionException {

        if (rc.isReady() && rc.canDigDirt(d)) {
            System.out.println("digging dirt");
            rc.digDirt(d);
            return true;
        }
        return false;
    }

    public void buildWall(MapLocation building, int turn) throws GameActionException {
        System.out.println("building wall");
        MapLocation curr = rc.getLocation();
        Direction bldg = curr.directionTo(building);
        Direction[] surround = aroundBldg(bldg);
        tryDepositDirt(surround[turn % 4]);
    }


    public boolean tryDepositDirt(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositDirt(dir)) {
            rc.depositDirt(dir);
            System.out.println("depositing dirt " + dir);
            return true;
        }
        return false;
    }

}
