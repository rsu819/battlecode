/* TODO:
 - refine the flood patrol landscaper
 */

package TeamPacific;

import battlecode.common.*;
import static TeamPacific.Blockchain.*;
import static TeamPacific.RobotPlayer.*;

public class Landscaper extends Robot {
	/* Variable(s) from robot:
	 * - MapLocation teamHqLoc
	 */
	
    // data members
    static LandscaperTask currentState = null;

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
    void run(int turnCount) throws GameActionException {
        // TODO Auto-generated method stub

        if (rc.getRoundNum() % 10 == 1) {
            Transaction[] block = rc.getBlock(rc.getRoundNum() - 1);
            currentState = checkState(block);
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

    static Direction[] findAdjacentDirs(Direction dir) {
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
    static void runWallBuilder(int turnCount)  throws GameActionException {

        MapLocation curr = rc.getLocation();
        Direction[] awayFromHq;

        if (teamHqLoc != null) {
            Direction dirToHq = curr.directionTo(teamHqLoc);
            if (!curr.isAdjacentTo(teamHqLoc)) {
                if (tryMove(dirToHq) == false) {
                    tryMove(dirToHq.rotateLeft());
                    tryMove(dirToHq.rotateRight());
                }
            } else {
                // if elevation at hq is getting buried, dig it out!
                if (rc.senseNearbyRobots(8, rc.getTeam().opponent()).length > 0) {
                    System.out.println("unbury hq");
                    tryDig(curr.directionTo(teamHqLoc));
                }
                if (rc.getDirtCarrying() == 0) {
                    awayFromHq = Landscaper.findDigDirs(dirToHq);
                    tryDig(awayFromHq[turnCount % 3]);
                }
                else if (turnCount > 50 &&
                        curr.directionTo(teamHqLoc) == Direction.EAST || curr.directionTo(teamHqLoc) == Direction.WEST) {
                    buildWall(teamHqLoc, turnCount);
                }
            }
        }
        tryMove(randomDirection());
    }

    static void runOffenseUnit(int turnCount) throws GameActionException {
        MapLocation curr = rc.getLocation();

        if (enemyHq == null){
            RobotInfo[] robots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
            if (robots.length > 0) {
                for (RobotInfo bot : robots) {
                    if (bot.ID < 2) {
                        enemyHq = bot.location;
                        break;
                    }
                }
            }
        }

        if(!curr.isAdjacentTo(enemyHq)) {
            tryMove(curr.directionTo(enemyHq));
        }
        // TODO: adjust logic for when to dig and when to bury enemy HQ
        if (curr.isAdjacentTo(enemyHq) && (rc.getDirtCarrying() == RobotType.LANDSCAPER.dirtLimit)){
            tryDepositDirt(curr.directionTo(enemyHq));
        } else {
            tryMove(curr.directionTo(enemyHq));
        }

        tryDig(randomDirection());
    }

    // TODO: activate other modes of landscaper action
    static void runFloodPatrol(Landscaper patrol) throws GameActionException {

        MapLocation curr = rc.getLocation();
        Direction d = randomDirection();

        if (!rc.senseFlooding(rc.adjacentLocation(d)) && rc.getDirtCarrying() != RobotType.LANDSCAPER.dirtLimit) {
            patrol.tryDig(d);
        }

        else if (rc.senseFlooding(rc.adjacentLocation(d)) && curr.isWithinDistanceSquared(teamHqLoc, 18)) {
            if (rc.getDirtCarrying() > 0) {
                patrol.tryDepositDirt(d);
            }
            else
                patrol.tryDig(randomDirection());
        }

        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.HQ) {
                tryMove(curr.directionTo(robot.location));
            }
        }
    }




    public static LandscaperTask checkState(Transaction[] txns) throws GameActionException {

        for (Transaction txn : txns) {
            int[] message = txn.getMessage();
            if (message[0] == TeamId && message[1] == MessageTo.Landscaper && message[5] == 1) {
                System.out.println("Attack EnemyHq!");
                return LandscaperTask.OFFENSE_UNIT;
            }
        }
        return LandscaperTask.WALL_BUILDER;
    }
}