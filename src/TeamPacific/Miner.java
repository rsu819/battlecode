package TeamPacific;
import java.util.Random;

import TeamPacific.Robot;
import battlecode.common.*;
import static TeamPacific.Blockchain.*;
public class Miner extends Robot {

	public static enum States {
		WANDER, MINE, REFINE, MOVE, BUILD;
	}

	static MapLocation closestRefinery;
	static boolean designSchoolCount;

	static Random randomGen = new Random();
	public static States currentState = null;
	public static States prevState = null;
	public static Direction tempDir = null;
	public static Direction lastDir = randomDirection();
	public static Direction currDir = lastDir;
	public static MapLocation[] nearbySoup;
	public static MapLocation currentTargetLoc = new MapLocation( -1, -1);
	public static MapLocation lastTargetLoc;
	static Direction[] dirList;
	public static int buildCount;
	static RobotType buildType;

	
	public Miner(RobotController rc) throws GameActionException {
		super(rc);
		designSchoolCount = true;
		buildCount = 0;

		setState(States.WANDER);

		//Find the location of the HQ
		RobotType[] tempArray = {RobotType.HQ};
		closestRefinery = teamHqLoc;;
	}
	
	/* Used for testing
	public Miner(RobotController rc) {
		super(rc);
		designSchoolCount = true;
		buildCount = 0;

		setState(States.WANDER);

		//Find the location of the HQ
		//RobotType[] tempArray = {RobotType.HQ};
		//teamHqLoc; = findNearest(rc.getLocation(), rc.senseNearbyRobots(), tempArray, rc.getTeam());
		//closestRefinery = teamHqLoc;;
	}*/

	public static States getState() {
		return currentState;
	}

	static void setState(States aState) {
		currentState = aState;
	}

	public void run(int turnCount) throws GameActionException {

		System.out.println(currentState);
		//System.out.println("My current state is: " + currentState);

		if (rc.isReady()) {
			//Check if there are any build orders
			
			//Build Design schools!
			if ( rc.getTeamSoup() > 150 ) {
				if ( buildCount < 1 ) {
					buildCount += 1;
					if(getState() != States.MOVE) {
						prevState = getState();
					}
					buildType = RobotType.DESIGN_SCHOOL;
					setState(States.BUILD);
					//tryBuild(RobotType.DESIGN_SCHOOL, randomDirection());
				} else if ( buildCount <= 2 )  {
					buildCount += 1;
					if(getState() != States.MOVE) {
						prevState = getState();
					}
					//prevState = getState();
					buildType = RobotType.FULFILLMENT_CENTER;
					setState(States.BUILD);
				}
			}

			switch( getState() ) {
				// Wander around in a (general) straight line until nearby soup is found
				case WANDER:

					// Check if there is any soup nearby first
					nearbySoup = rc.senseNearbySoup();
					//System.out.println(nearbySoup[0]);
					
					if (nearbySoup.length > 0) {
						currentTargetLoc = findNearestSoup(rc.getLocation(), nearbySoup);
					} else { // Check the block chain for potential soup locations
						if( (rc.getRoundNum() % 10) == 1 ) {
							currentTargetLoc = getRemoteSoupLoc(rc.getRoundNum() - 1);
							if(currentTargetLoc != null) {
								tempDir = rc.getLocation().directionTo(currentTargetLoc);
								prevState = States.MINE;
								setState(States.MOVE);
								System.out.println("I'm gonna mine soup: " + currentTargetLoc);
								break;
							}
						}
					}
					
					// Check if the location is on the map
					if( currentTargetLoc != null ) {
						// Check if the location is on the map
						if ( rc.canSenseLocation(currentTargetLoc) ) {
							if( rc.senseSoup(currentTargetLoc) > 0) {
								// Post that there is soup at this loc
								emitSoupLoc(currentTargetLoc);
								tempDir = rc.getLocation().directionTo(currentTargetLoc);
								prevState = States.MINE;
								setState(States.MOVE);
								System.out.println("I'm gonna mine soup: " + currentTargetLoc);
								break;
							}
						}
					}
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
					

					break;

				//Stay put and mine nearby soup
				case MINE:

					if (rc.getSoupCarrying() >= RobotType.MINER.soupLimit) {
						lastTargetLoc = currentTargetLoc;
						setState(States.REFINE);
					} else {
						if (tryMine(tempDir)) {
							MapLocation myLoc = rc.getLocation();
							// emit soup location if round number is divisible by 10
							if ((rc.getRoundNum() % 10) == 0) {
								boolean mined = emitSoupLoc(myLoc);
								System.out.println("Emitted soup location: " + mined);
							}
							//System.out.println("I mined soup! " + tempDir + " " + rc.getSoupCarrying());
						} else { //Check if there is any other soup around to mine
							nearbySoup = rc.senseNearbySoup(2);
							if(nearbySoup.length > 0) {
								tempDir = rc.getLocation().directionTo(findNearest(rc.getLocation(), nearbySoup));
								currentTargetLoc = findNearest(rc.getLocation(), nearbySoup);
								prevState = States.MINE;
								setState(States.MOVE);
								//System.out.println("I'm gonna mine soup: " + tempDir);
							} else {
								setState(States.WANDER);
							}
						}
					}

					break;


				//Move towards currTargetLoc naively
				case MOVE:
					
					//If moving towards soup
					if(prevState == States.MINE) {
						//Recheck if this soup is still accessible
						if(!isAccessible(currentTargetLoc)) {
							setState(States.WANDER);
						}
					}
					
					//If there is a closer refinery while moving
					if(prevState == States.REFINE) {
						RobotType[] tempArray = {RobotType.REFINERY};
						MapLocation tempLoc = findNearest(rc.getLocation(), rc.senseNearbyRobots(), tempArray, rc.getTeam()); 
						if( tempLoc.distanceSquaredTo(rc.getLocation()) < currentTargetLoc.distanceSquaredTo(rc.getLocation()) ) {
							currentTargetLoc = tempLoc;
							closestRefinery = currentTargetLoc;
							tempDir = rc.getLocation().directionTo(currentTargetLoc);
						} 
					}
					
					//System.out.println("I'm moving towards: " + currentTargetLoc);
					//System.out.println("I'm moving towards: " + tempDir);
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
					
					// Check if the nearest refinery is within an acceptable walking distance OR there isn't enough soup to make a new refinery
					if( closestRefinery.isWithinDistanceSquared(rc.getLocation(), 50) || rc.getTeamSoup() < 150) {
	
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
					} 
					
					// If it isn't, just build a closer one if possible (mainly if there is enough soup)
					else {
						
						//Check if there is a closer refinery before building a new one
						RobotType[] tempArray = {RobotType.REFINERY};
						MapLocation tempLoc = findNearest(rc.getLocation(), rc.senseNearbyRobots(), tempArray, rc.getTeam()); 
						if( rc.onTheMap(tempLoc) ) {
							currentTargetLoc = tempLoc;
							closestRefinery = currentTargetLoc;
							tempDir = rc.getLocation().directionTo(currentTargetLoc);
							prevState = getState();
							setState(States.MOVE);
						} 
						else {
						
							tempDir = randomDirection();
							//Try and build in a random location
							if(tryBuild(RobotType.REFINERY, tempDir)) {
								//System.out.println("I built a refinery!");
								closestRefinery = rc.adjacentLocation(tempDir); // Remember where this is built to prevent too much soup being wasted
							}
							if (tryRefine(tempDir)) {
								//System.out.println("I refined some soup! " + rc.getSoupCarrying());
								currentTargetLoc = lastTargetLoc;
								prevState = States.MINE;
								setState(States.MOVE);
								//System.out.println("I'm gonna move back to mine more soup!");
							}
						}
					}

					break;

				// Used to exclusively build design schools for now
				case BUILD:

					tempDir = rc.getLocation().directionTo(teamHqLoc);

					if ((teamHqLoc.distanceSquaredTo(rc.adjacentLocation(tempDir.opposite())) >= 4) && (teamHqLoc.distanceSquaredTo(rc.adjacentLocation(tempDir)) < 16) ){
						if (tryBuild(buildType, tempDir.opposite())) {
							buildCount += 1;
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
						tempDir = rc.getLocation().directionTo(teamHqLoc);
					}

					break;
			}
		}
		//return;
	}
	
    // Will give the nearest soup, filters out soup that is inaccessible
    public static MapLocation findNearestSoup(MapLocation currLoc, MapLocation[] locations) throws GameActionException {

        MapLocation tempLocation = new MapLocation( -1, -1);
        int nearest = -1;
        int tempInt = Integer.MAX_VALUE;
        for(int i = 0; i < locations.length; i++) {
            if ((currLoc.distanceSquaredTo(locations[i]) < tempInt) && (isAccessible(locations[i]))) {
                tempInt = currLoc.distanceSquaredTo(locations[i]);
                nearest = i;
            }
        }

        if (nearest != -1) {
            tempLocation = locations[nearest];
        }
        return tempLocation;
    }
    
    // Checks if it is possible to access the soup in a 90 degree angle towards the miner
    static boolean isAccessible(MapLocation aLocation) throws GameActionException {
    	
    	Direction tempDir = aLocation.directionTo(rc.getLocation()).rotateLeft();
    	MapLocation tempLoc = aLocation;
    	int distanceRoot  = (int) Math.round(Math.sqrt(aLocation.distanceSquaredTo(rc.getLocation())));
    	
    	for(int i = 0; i < 2; i++) {
    		// Check if the location can even be sensed first
    		if(rc.canSenseLocation(tempLoc.translate(tempDir.dx, tempDir.dy))) {
    			
    		// Check if not flooded
    		if(!rc.senseFlooding(tempLoc.translate(tempDir.dx, tempDir.dy))) {
    			//Check if the soup is actually accessible height-wise 
    			if( ((rc.senseElevation(rc.getLocation()) - distanceRoot*3) < rc.senseElevation(tempLoc.translate(tempDir.dx, tempDir.dy)) && 
    					((rc.senseElevation(rc.getLocation()) + distanceRoot*3) > rc.senseElevation(tempLoc.translate(tempDir.dx, tempDir.dy))))) {
    				return true;
    			}
    		}
    		}
    		tempDir = tempDir.rotateRight();
    	}
    	
    	return false;
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
		message = foundResourceMessage(soupLoc, MessageTo.Miner, Resource.Soup, false);
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
