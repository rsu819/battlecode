package TeamPacific;
import TeamPacific.Robot;
import battlecode.common.*;


public class NetGun extends Robot {

    public static int shot;

    public  NetGun(RobotController rc) throws GameActionException {
        // TODO Auto-generated constructor stub
        super(rc);
        shot = 0;
    }

    @Override
    public void run(int turnCount) throws GameActionException {
        RobotInfo[] kills = findOpponents(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED);
        if(kills != null) {
            for (RobotInfo kill : kills) {
                if (kill.getType() == RobotType.DELIVERY_DRONE) {
                    shot = shoot(kill);
                }
            }
        }
    }

    public RobotInfo[] findOpponents(int sensorRadius){
        return rc.senseNearbyRobots(sensorRadius, rc.getTeam().opponent());
    }

    public int shoot(RobotInfo kill) throws GameActionException {
        int shot = 0;
        if (rc.canShootUnit(kill.ID)) {
            rc.shootUnit(kill.ID);
            ++shot;
        }
        return shot;
    }

}
