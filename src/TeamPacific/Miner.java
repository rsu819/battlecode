package TeamPacific;
import java.util.Random;

import battlecode.common.*;
import static TeamPacific.Blockchain.*;

public class Miner extends Robot {
	
	enum States {
	        WANDER, MINE, REFINE, MOVE, BUILD;
	}

    static MapLocation locationHQ;

    static MapLocation closestRefinery;
    static boolean designSchoolCount;

    static Random randomGen = new Random();
    public static States currentState = null;
    public static States prevState = null;
    public static Direction tempDir = null;
    public static Direction lastDir = randomDirection();
    public static Direction currDir = lastDir;
    public static MapLocation[] nearbySoup;
    public static MapLocation currentTargetLoc;
    public static MapLocation lastTargetLoc;
    static Direction[] dirList;
    static int buildCount;
    
    Miner(RobotController rc) {
        super(rc);
        designSchoolCount = true;
        buildCount = 0;
        
        setState(States.WANDER);

        //Find the location of the HQ
        RobotType[] tempArray = {RobotType.HQ};
        locationHQ = findNearest(rc.getLocation(), rc.senseNearbyRobots(), tempArray, rc.getTeam());
    }
    
    static States getState() {
        return currentState;
    }

    static void setState(States aState) {
        currentState = aState;
    }
		
	 void run(int turnCount) throws GameActionException {

	        System.out.println(currentState);
	        //System.out.println("My current state is: " + currentState);

	        if (rc.isReady()) {

	            if ( (turnCount > 200 && turnCount < 300) && (buildCount < 1) ) {
	                buildCount++;
	                prevState = getState();
	                setState(States.BUILD);
	                //tryBuild(RobotType.DESIGN_SCHOOL, randomDirection());
	            }

	            switch( getState() ) {
	                // Wander around in a (general) straight line until nearby soup is found
	                case WANDER:
	                	// check blockchain for soup messages
						int round = rc.getRoundNum();
						MapLocation remoteSoup = null;
						if (round > 50) {
							for (int i = round - 1; i > round-50 ; i--) {
								remoteSoup = getRemoteSoupLoc(i);
								if (remoteSoup != null) {
									break;
								}
							}
						}
	                    // Check if there is any soup nearby first
	                    nearbySoup = rc.senseNearbySoup();
	                    //System.out.println(nearbySoup[0]);
	                    if (nearbySoup.length > 0) {
	                        currentTargetLoc = findNearest(rc.getLocation(), nearbySoup);
	                        tempDir = rc.getLocation().directionTo(currentTargetLoc);
	                        // Switch to mining state
	                        prevState = States.MINE;
	                        setState(States.MOVE);
	                        System.out.println("I'm gonna mine soup: " + tempDir);
	                    } else if (remoteSoup != null) {
							System.out.println("located remote soup at: " + remoteSoup);
							currentTargetLoc = remoteSoup;
							tempDir = rc.getLocation().directionTo(remoteSoup);
	                    	prevState = States.MINE;
	                    	setState(States.MOVE);
						} else {
	                        // Used to prevent robots from shuffling back and forth
	                        if (tryMove(currDir)) {
	                            System.out.println("I moved!");
	                        } else {
	                            //Get a list of all "frontal" directions
	                            Direction[] dirList = getFrontDirections(currDir);
	                            while( !tryMove(currDir) ) {
	                                currDir = dirList[randomGen.nextInt(dirList.length)];
	                                while ( currDir == lastDir ) {
	                                    currDir = randomDirection();
	                                }
	                                lastDir = currDir;
	                            }
	                        }
	                    }

	                    break;

	                //Stay put and mine nearby soup
	                case MINE:

	                    if (rc.getSoupCarrying() >= 100) {
	                        lastTargetLoc = currentTargetLoc;
	                        setState(States.REFINE);
	                    } else {
	                        if (tryMine(tempDir)) {
	                        		MapLocation myLoc = rc.getLocation();
									// emit soup location if it is outside of miner sense radius from HQ
									if (myLoc.distanceSquaredTo(locationHQ) > RobotType.MINER.sensorRadiusSquared) {
										boolean mined = emitSoupLoc(myLoc);
										System.out.println("Emitted soup location: " + mined);
									}
	                            //System.out.println("I mined soup! " + tempDir + " " + rc.getSoupCarrying());
	                        } else { //Check if there is any other soup around to mine
	                            nearbySoup = rc.senseNearbySoup(2);
	                            if(nearbySoup.length > 0) {
	                                tempDir = rc.getLocation().directionTo(findNearest(rc.getLocation(), nearbySoup));
	                                //System.out.println("I'm gonna mine soup: " + tempDir);
	                            } else {
	                                setState(States.WANDER);
	                            }
	                        }
	                    }

	                    break;
	                    
	                    
	                //Move towards currTargetLoc naively
	                case MOVE:
	                	System.out.println("tempDir: " + tempDir);
	                    //System.out.println("I'm moving towards: " + currentTargetLoc);
	                    if(rc.adjacentLocation(tempDir).equals(currentTargetLoc)) {
	                        setState(prevState);
	                    } else {
	                        // Try the "correct" direction first"
	                        if(!tryMove(tempDir)) {
	                            //Get a list of all "frontal" directions
	                            dirList = getFrontDirections(tempDir);
	                            //Otherwise continue on whatever path
	                            while( !tryMove(lastDir) ) {
	                                lastDir = dirList[randomGen.nextInt(dirList.length)];
	                            }
	                        } else {
	                            lastDir = tempDir;
	                        }
	                        tempDir = rc.getLocation().directionTo(currentTargetLoc);
	                    }
	                	break;

	                case REFINE:
	             
	                    if(turnCount < 200) {
	                        RobotType[] tempArray = {RobotType.HQ, RobotType.REFINERY};
	                        closestRefinery = findNearest(rc.getLocation(), rc.senseNearbyRobots(), tempArray, rc.getTeam());
	                    } else {
	                        RobotType[] tempArray = {RobotType.REFINERY};
	                        closestRefinery = findNearest(rc.getLocation(), rc.senseNearbyRobots(), tempArray, rc.getTeam());
	                    }


	                    //There was a refinery found!
	                    if (rc.onTheMap(closestRefinery)) {

	                        tempDir = rc.getLocation().directionTo(closestRefinery);
	                        if (tryRefine(tempDir)) {
	                            System.out.println("I refined some soup! " + rc.getSoupCarrying());
	                            setState(States.WANDER);
	                            System.out.println("I'm gonna wander around!");
	                        } else { // Gotta walk
	                            currentTargetLoc = closestRefinery;
	                            prevState = getState();
	                            setState(States.MOVE);
	                        }

	                    } else {

	                        tempDir = randomDirection();
	                        //Try and build in a random location
	                        if(tryBuild(RobotType.REFINERY, tempDir)) {
	                            //System.out.println("I built a refinery!");
	                        }
	                        if (tryRefine(tempDir)) {
	                            //System.out.println("I refined some soup! " + rc.getSoupCarrying());
	                            currentTargetLoc = lastTargetLoc;
	                        	prevState = States.MINE;
	                            setState(States.MOVE);
	                            //System.out.println("I'm gonna move back to mine more soup!");
	                        } // Shouldn't be here
	                    }

	                    break;

	                // Used to exclusively build design schools for now
	                case BUILD:
	                	
	                    tempDir = rc.getLocation().directionTo(locationHQ);

	                    if ((locationHQ.distanceSquaredTo(rc.adjacentLocation(tempDir.opposite())) >= 4) && (locationHQ.distanceSquaredTo(rc.adjacentLocation(tempDir)) < 16) ){
	                        if (tryBuild(RobotType.DESIGN_SCHOOL, tempDir.opposite())) {
	                            buildCount++;
	                            setState(prevState);
	                        } else {
	                            setState(prevState);
	                        }
	                    } else {
	                        // Try the "correct" direction first"
	                        if(!tryMove(tempDir)) {
	                            //Get a list of all "frontal" directions
	                            Direction[] dirList = getFrontDirections(tempDir);
	                            //Otherwise continue on whatever path
	                            while( !tryMove(lastDir) ) {
	                                lastDir = dirList[randomGen.nextInt(dirList.length)];
	                            }
	                        } else {
	                            lastDir = tempDir;
	                        }
	                        tempDir = rc.getLocation().directionTo(locationHQ);
	                    }

	                    break;
	            }
	        }
	    }
	 
	    static Direction randomDirection() {
	        return directions[(int) (Math.random() * directions.length)];
	    }
	    
	    /**
	     * Attempts to mine soup in a given direction.
	     *
	     * @param dir The intended direction of mining
	     * @return true if a move was performed
	     * @throws GameActionException
	     */
	    static boolean tryMine(Direction dir) throws GameActionException {
	        if (rc.isReady() && rc.canMineSoup(dir)) {
	            rc.mineSoup(dir);
	            return true;
	        } else return false;
	    }
	    
	    /**
	     * Attempts to refine soup in a given direction.
	     *
	     * @param dir The intended direction of refining
	     * @return true if a move was performed
	     * @throws GameActionException
	     */
	    static boolean tryRefine(Direction dir) throws GameActionException {
	        if (rc.isReady() && rc.canDepositSoup(dir)) {
	            rc.depositSoup(dir, rc.getSoupCarrying());
	            return true;
	        } else return false;
	    }

	    static boolean emitSoupLoc(MapLocation soupLoc) throws GameActionException {
			int[] message;
			int cost = 10;
			message = foundResourceMessage(soupLoc, MessageTo.Miner, Resource.Soup);
			if (rc.canSubmitTransaction(message, cost)){
				rc.submitTransaction(message, cost);
				return true;
			}
			return false;
		}

		static MapLocation getRemoteSoupLoc(int roundNum) throws GameActionException {
			Transaction[] txns = rc.getBlock(roundNum);
			for (Transaction txn : txns) {
				int[] message = txn.getMessage();
				if (message[0] == TeamId &&
						message[1] == MessageTo.Miner &&
						message[2] == Resource.Soup) {
					MapLocation soupLocation = new MapLocation(message[3], message[4]);
					System.out.println("Found soup: " + soupLocation.x + ", " + soupLocation.y);
					return soupLocation;
				}
			}
			return null;
		}

}
