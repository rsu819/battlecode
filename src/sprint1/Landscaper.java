package sprint1;
import battlecode.common.*;

import java.util.ArrayList;

//
//    Landscaper: moves dirt around the map to adjust elevation and destroy buildings.
//
//    Produced by the design school.
//    Can perform the action rc.digDirt() to remove one unit of dirt from an adjacent tile or its current tile, increasing the landscaper’s stored dirt by 1 up to a max of RobotType.LANDSCAPER.dirtLimit (currently set to 25). If the tile is empty, flooded, or contains another unit, this reduces the tile’s elevation by 1. If the tile contains a building, it removes one unit of dirt from the building, or if the building is not buried, has no effect.
//    Can perform the action rc.depositDirt() to reduce its stored dirt by one and place one unit of dirt onto an adjacent tile or its current tile. If the tile contains a building, the dirt partially buries it–the health of a building is how much dirt can be placed on it before it is destroyed. If the tile is empty, flooded, or contains another unit, the only effect is that the elevation of that tile increases by 1.
//    Note: all this means that buildings may never change elevation, so be careful to contain that water level.
//    When a landscaper dies, the dirt it’s carrying is dropped on the current tile.
//    If enough dirt is placed on a flooded tile to raise its elevation above the water level, it becomes not flooded.


public class Landscaper {
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
            if (Math.abs(d.dx - dir.dx) <= 1 && Math.abs(d.dy - dir.dy) <= 1 && d != dir) {
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
//        for (Direction d : Direction.values()) {
//            // if can deposit and elevation comparison is lower
//                if (rc.canDepositDirt(d.rotateRight()) && d == curr.directionTo(building)) {
//                    tryDepositDirt(d.rotateRight());
//                } else if (rc.canDepositDirt(d.rotateLeft()) && d == curr.directionTo(building)) {
//                    tryDepositDirt(d.rotateLeft());
//                }
//        }
//    }

    public boolean tryDepositDirt(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositDirt(dir)) {
            rc.depositDirt(dir);
            System.out.println("depositing dirt " + dir);
            return true;
        }
        return false;
    }

}