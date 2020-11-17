package TeamPacific;
import battlecode.common.*;


public class NetGun extends Robot {

    public  NetGun(RobotController rc) throws GameActionException {
        // TODO Auto-generated constructor stub
        super(rc);
    }

    @Override
    void run(int turnCount) throws GameActionException {
        RobotInfo[] kills = findOpponents();
        for (RobotInfo kill : kills) {
            if (kill.getType() == RobotType.DELIVERY_DRONE) {
                shoot(kill);
            }
        }
    }

    public RobotInfo[] findOpponents(){
        return rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, rc.getTeam().opponent());
    }

    public int shoot(RobotInfo kill) throws GameActionException {
        int shot = 0;
        if (rc.canShootUnit(kill.ID)) {
            rc.shootUnit(kill.ID);
        }
        return shot;
    }

}
