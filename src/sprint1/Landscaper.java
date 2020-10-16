package sprint1;
import battlecode.common.*;

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
    static int MaxSenseRadius = 20;
    static Team myTeam;


    // functions
    /*
     - rc.digDirt() / canDigDirt
     - rc.depositDirt() / canDepositDirt
     - canSenseRobot(id)
     - senseNearbyRobots()
     - canBuildRobot
     - buildRobot
     - disintegrate
     - resign
     - submitTransaction / canSubmitTransaction
     - RobotInfo -dirtCarrying()
     - MapLocation
     - team
     - robotType
     - move


     */
    /*
    actions:
     - sense flooded tiles in its vicinity
     - destroy enemy buildings
     - send message
     - if close to enemy building, deposit dirt

     */
}
// public int dig(int units) {
//
//}