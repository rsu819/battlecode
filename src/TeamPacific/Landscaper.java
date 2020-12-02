/* TODO:
 - refine the flood patrol landscaper
 */

package TeamPacific;

import TeamPacific.Robot;
import battlecode.common.*;
import static TeamPacific.Blockchain.*;
import static TeamPacific.RobotPlayer.*;

public class Landscaper extends Robot {
	/* Variable(s) from robot:
	 * - MapLocation teamHqLoc
	 */
	
    // data members
    public static LandscaperTask currentState = null;

    public enum LandscaperTask{
        WALL_BUILDER,
        OFFENSE_UNIT,
        FLOOD_PATROL
    }

    public Landscaper(RobotController rc) throws GameActionException {
        super(rc);
        currentState = LandscaperTask.WALL_BUILDER;
    }

    @Override
    public void run(int turnCount) throws GameActionException {
        // TODO Auto-generated method stub
        int round = rc.getRoundNum();
        if (round % 10 == 1) {
            Transaction[] block = rc.getBlock(rc.getRoundNum() - 1);
            currentState = checkBlockForState(block);
        }
        if (round > 450) {
            currentState = LandscaperTask.OFFENSE_UNIT;
        }
        switch(currentState) {
            case OFFENSE_UNIT:
                runOffenseUnit(turnCount);
                break;
            default:
                runWallBuilder(turnCount);
        }
    }

    public static Direction[] findDigDirs(Direction bldgDir) {
        Direction[] awayFromBldg = {bldgDir.opposite(), bldgDir.opposite().rotateLeft(), bldgDir.opposite().rotateRight()};
        return awayFromBldg;
    }

    public static Direction[] findAdjacentDirs(Direction dir) {
        Direction[] aroundBldg = new Direction[6];
        int index = 0;
        for (Direction d : Direction.allDirections()) {
            if ((Math.abs(d.dx - dir.dx) <= 1 && Math.abs(d.dy - dir.dy) <= 1) && d != dir) {
                aroundBldg[index] = d;
                ++index;
            }
        }
        return aroundBldg;
    }

    public static boolean tryDig(Direction d) throws GameActionException {

        if (rc.isReady() && rc.canDigDirt(d)) {
            rc.digDirt(d);
            return true;
        }
        return false;
    }

    public static boolean buildWall(MapLocation building, int turn) throws GameActionException {
        System.out.println("building wall");
        MapLocation curr = rc.getLocation();
        Direction bldg = curr.directionTo(building);
        Direction[] surrounding = findAdjacentDirs(bldg);
        Direction toBuild = surrounding[turn % 5];
        return tryDepositDirt(toBuild);
    }

    public static boolean tryDepositDirt(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositDirt(dir)) {
            rc.depositDirt(dir);
            System.out.println("depositing dirt " + dir);
            return true;
        }
        return false;
    }

    // build general tasks / skills for the landscaper
    public static void runWallBuilder(int turnCount)  throws GameActionException {

        MapLocation curr = rc.getLocation();
        Direction dirToHq = curr.directionTo(teamHqLoc);
        Direction[] awayFromHq;

        if (teamHqLoc != null) {
            if (!curr.isAdjacentTo(teamHqLoc)) {
                if (tryMove(dirToHq) == false) {
                    tryMove(dirToHq.rotateLeft());
                    tryMove(dirToHq.rotateRight());
                }
            } else {
                // if elevation at hq is getting buried, dig it out!
                if (checkForEnemies(8, RobotType.LANDSCAPER) != null) {
                    System.out.println("unbury hq");
                    tryDig(curr.directionTo(teamHqLoc));
                }
                if (rc.getDirtCarrying() == 0) {
                    awayFromHq = Landscaper.findDigDirs(dirToHq);
                    tryDig(awayFromHq[turnCount % 3]);
                }
                else if (turnCount > 50 &&
                        dirToHq == Direction.EAST || dirToHq == Direction.WEST || dirToHq == Direction.NORTH || dirToHq == Direction.SOUTH) {
                    if (compareElevation(rc.getLocation(), teamHqLoc) >= 3 && rc.getRoundNum() < 275) {
                        System.out.println("waiting to build higher...");
                    }
                    else { buildWall(teamHqLoc, turnCount); }
                }
                else {
                    tryMove(randomDirection());
                }
            }
        }
    }

    public static void runOffenseUnit(int turnCount) throws GameActionException {
        MapLocation curr = rc.getLocation();

        if (enemyHq == null){
            RobotInfo[] robots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
            if (robots.length > 0) {
                for (RobotInfo bot : robots) {
                    if (bot.getType() == RobotType.HQ) {
                        enemyHq = bot.location;
                        return;
                    }
                }
            }
        }
        else if(!curr.isAdjacentTo(enemyHq)) {
            tryMove(curr.directionTo(enemyHq));
        }
        // TODO: adjust logic for when to dig and when to bury enemy HQ
        else if (curr.isAdjacentTo(enemyHq) && (rc.getDirtCarrying() == RobotType.LANDSCAPER.dirtLimit)){
            tryDepositDirt(curr.directionTo(enemyHq));
        } else {
            tryMove(curr.directionTo(enemyHq));
        }

        tryDig(randomDirection());
    }

    public static LandscaperTask checkBlockForState(Transaction[] txns) throws GameActionException {

        for (Transaction txn : txns) {
            int[] message = txn.getMessage();
            if (message[0] == TeamId && message[1] == MessageTo.Landscaper && message[5] == 1) {
                System.out.println("Attack EnemyHq!");
                return LandscaperTask.OFFENSE_UNIT;
            }
        }
        return LandscaperTask.WALL_BUILDER;
    }

    public static int compareElevation(MapLocation one, MapLocation two) throws GameActionException {
        if (rc.canSenseLocation(one) && rc.canSenseLocation(two)) {
            return Math.abs(rc.senseElevation(one) - rc.senseElevation(two));
        }
        return -1;
    }
}