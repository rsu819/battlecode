package teampacifictest;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import TeamPacific.Blockchain;
import TeamPacific.Miner;
import TeamPacific.Robot;
import battlecode.common.*;
import org.junit.Assert;
import org.junit.Before;

import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatcher;


/* Test list:
 * -
 * 
 * 
 * 
 */

public class minerTest {
	
	static RobotController mockRC;
	static Miner testMiner;
	static MapLocation mockHqLoc;
	static MapLocation mockRcLoc;
	
	@Before
	public void setupMinerTest() throws GameActionException {
		// Create a location for the mock hq
		mockHqLoc = new MapLocation(10, 10);
		mockRcLoc = new MapLocation(15, 15);
		
		// Create a mock RobotController for testing
		mockRC = mock(RobotController.class);
		testMiner = new Miner(mockRC) {};
	}
	
	@Test
	public void testFindNearestSoupSuccess() throws GameActionException {
		//Create mock locations for testing
		MapLocation currentLoc = new MapLocation(15, 15);
		MapLocation[] soupLoc = new MapLocation[] {new MapLocation (20,20), new MapLocation (21, 21)};
		
		// Mock miner functions 
		when(mockRC.isReady()).thenReturn(true);
		when(mockRC.getLocation()).thenReturn(currentLoc);
		when(mockRC.senseElevation(soupLoc[0])).thenReturn(3);
		when(mockRC.senseElevation(mockRC.getLocation())).thenReturn(5);
		when(mockRC.canSenseLocation(any(MapLocation.class))).thenReturn(true);
		
		Assert.assertEquals(soupLoc[0], Miner.findNearestSoup(currentLoc, soupLoc));
		
	}
	
	/*
	 * Test that isAccessible returns false if the given soup location is flooded (inaccessible)
	 */
	@Test
	public void testFindNearestSoupFail() throws GameActionException {
		//Create mock locations for testing
		MapLocation currentLoc = new MapLocation(15, 15);
		MapLocation[] soupLoc = new MapLocation[] {new MapLocation (20,20), new MapLocation (21, 21)};
		MapLocation tempLocation = new MapLocation( -1, -1);
		
		// Mock miner functions 
		when(mockRC.isReady()).thenReturn(true);
		when(mockRC.getLocation()).thenReturn(currentLoc);
		when(mockRC.senseElevation(soupLoc[0])).thenReturn(-1000);
		when(mockRC.senseElevation(mockRC.getLocation())).thenReturn(5);
		when(mockRC.senseFlooding(any(MapLocation.class))).thenReturn(true);
		
		Assert.assertEquals(tempLocation, Miner.findNearestSoup(currentLoc, soupLoc));
		
	}
	
	/*
	 * Testing the wander state for wandering
	 */ 
	@Test
	public void testRunWanderWandering() throws GameActionException {
		
		//Mock locations 
		MapLocation[] emptyLoc = new MapLocation[] {};
		
		//Mock miner functions
		when(mockRC.isReady()).thenReturn(true);
		when(mockRC.getTeamSoup()).thenReturn(0);
		when(mockRC.senseNearbySoup()).thenReturn(emptyLoc);
		//when(Miner.getState()).thenReturn(Miner.States.WANDER);
		/*
		 * Robot tryMove uses two canMoves, so this skips the first try move and loops twice
		 */
		when(mockRC.canMove(any())).thenReturn(false, false, false, false, true, true);
		testMiner.run(0);
		
		Assert.assertEquals(Miner.States.WANDER, Miner.getState());
	}
	
	
	/*
	 * Testing soup being found in the wander state (not through blockchain
	 */
	@Test
	public void testRunWanderFoundSoup() throws GameActionException {
		
		//Mock locations 
		MapLocation currentLoc = new MapLocation(15, 15);
		MapLocation[] soupLoc = new MapLocation[] {new MapLocation (11, 11)};
		
		//Mock miner functions
		when(mockRC.isReady()).thenReturn(true);
		when(mockRC.getTeamSoup()).thenReturn(0);
		when(mockRC.senseNearbySoup()).thenReturn(soupLoc);
		when(mockRC.senseSoup(any())).thenReturn(100);
		when(mockRC.getLocation()).thenReturn(currentLoc);
		when(mockRC.senseElevation(soupLoc[0])).thenReturn(3);
		when(mockRC.senseElevation(mockRC.getLocation())).thenReturn(5);
		when(mockRC.canSenseLocation(any(MapLocation.class))).thenReturn(true);
		
		
		testMiner.run(0);
		
		Assert.assertEquals(Miner.States.MOVE, Miner.getState());
	}
	
	/*
	 * Testing soup location being given through blockchain
	 */
	@Test
	public void testRunWanderBlockChainSoup() throws GameActionException {
		
		//Mock locations 
		MapLocation currentLoc = new MapLocation(15, 15);
		MapLocation[] emptyLoc = new MapLocation[] {};;
		
		//Mock miner functions
		when(mockRC.isReady()).thenReturn(true);
		when(mockRC.getTeamSoup()).thenReturn(0);
		when(mockRC.senseNearbySoup()).thenReturn(emptyLoc);
		when(mockRC.getRoundNum()).thenReturn(11);;
		when(mockRC.getLocation()).thenReturn(currentLoc);
		
		//Mock Blockchain
		int[] mockMessage = {603720, 1, 1, 11, 11, 0, 0};
		int[] emptyMessage = {0, 0, 0, 0, 0, 0, 0 ,0};
		Transaction[] block = new Transaction[11];
		Transaction mockTransaction = new Transaction(0, mockMessage, 0);
		Transaction emptyTransaction = new Transaction(0, emptyMessage, 1);
		for(int i = 0; i < 11; i++) {
			if(i == 10) {
				block[i] = mockTransaction;
			} else {
				block[i] = emptyTransaction;
			}
		}
		
		when(mockRC.getBlock(anyInt())).thenReturn(block);
		
		testMiner.run(0);
		
		Assert.assertEquals(Miner.States.MOVE, Miner.getState());
	}
	
	/*
	 * Testing going from the MOVE state to the MINE state without any movement
	 * (Miner is already adjacent to soup)
	 */
	@Test
	public void testRunMoveSoupAdjacent() throws GameActionException {
		
		/*-- Setting up test --*/
		/*-- WANDER setup --*/
		//Mock locations 
		MapLocation currentLoc = new MapLocation(15, 15);
		MapLocation[] soupLoc = new MapLocation[] {new MapLocation (14, 15)};
		
		//Mock miner functions
		when(mockRC.isReady()).thenReturn(true);
		when(mockRC.getTeamSoup()).thenReturn(0);
		when(mockRC.senseNearbySoup()).thenReturn(soupLoc);
		when(mockRC.senseSoup(any())).thenReturn(100);
		when(mockRC.getLocation()).thenReturn(currentLoc);
		when(mockRC.senseElevation(soupLoc[0])).thenReturn(3);
		when(mockRC.senseElevation(mockRC.getLocation())).thenReturn(5);
		when(mockRC.canSenseLocation(any(MapLocation.class))).thenReturn(true);
		
		testMiner.run(0);
		
		/*-- Actual test here --*/
		//Mock functions
		when(mockRC.isReady()).thenReturn(true);
		when(mockRC.getTeamSoup()).thenReturn(0);
		when(mockRC.adjacentLocation(any(Direction.class))).thenReturn(soupLoc[0]);
		//when(MapLocation.equals(any(MapLocation.class))).thenReturn(true);
		
		testMiner.run(1);
		Assert.assertEquals(Miner.States.MINE, Miner.getState());
	}
	
	/*
	 * Testing mining in the MINE state (goes to REFINE State)
	 */
	@Test
	public void testRunMine() throws GameActionException {
		
		/*-- Setting up test --*/
		/*-- WANDER setup --*/
		//Mock locations 
		MapLocation currentLoc = new MapLocation(15, 15);
		MapLocation[] soupLoc = new MapLocation[] {new MapLocation (14, 15)};
		
		//Mock miner functions
		when(mockRC.isReady()).thenReturn(true);
		when(mockRC.getTeamSoup()).thenReturn(0);
		when(mockRC.senseNearbySoup()).thenReturn(soupLoc);
		when(mockRC.senseSoup(any())).thenReturn(100);
		when(mockRC.getLocation()).thenReturn(currentLoc);
		when(mockRC.senseElevation(soupLoc[0])).thenReturn(3);
		when(mockRC.senseElevation(mockRC.getLocation())).thenReturn(5);
		when(mockRC.canSenseLocation(any(MapLocation.class))).thenReturn(true);
		
		testMiner.run(0);
		
		/*-- MOVE setup --*/
		//Mock functions
		when(mockRC.isReady()).thenReturn(true);
		when(mockRC.getTeamSoup()).thenReturn(0);
		when(mockRC.adjacentLocation(any(Direction.class))).thenReturn(soupLoc[0]);
		
		testMiner.run(1);
		
		/*-- Actual test here --*/
		int soupCount = 21;
		
		//Mock Locations
		MapLocation[] emptyLoc = new MapLocation[] {};
		
		//Mock functions
		when(mockRC.isReady()).thenReturn(true, true);
		when(mockRC.getTeamSoup()).thenReturn(0);
		when(mockRC.getSoupCarrying()).thenReturn(0, 500);
		when(mockRC.senseNearbySoup(anyInt())).thenReturn(emptyLoc);
		when(mockRC.canMineSoup(any(Direction.class))).thenReturn(true);
		
		testMiner.run(2);
		testMiner.run(3);
		Assert.assertEquals(Miner.States.REFINE, Miner.getState());
	}
	
	//Test Building Design school
//	@Test
//	public void testBuild() throws GameActionException {
//		//Mock functions
//		when(mockRC.isReady()).thenReturn(true);
//		when(mockRC.getTeamSoup()).thenReturn(1000);
//		when(mockRC.getRoundNum()).thenReturn(1000);
//
//		testMiner.run(0);
//
//		Assert.assertEquals(Miner.States.BUILD, Miner.getState());
//	}
}
