package teampacifictest;

import battlecode.common.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static TeamPacific.Blockchain.TeamId;
import static TeamPacific.HQ.emitAttackMode;
import static org.mockito.Mockito.*;
import TeamPacific.*;

import java.util.Map;

public class landscaperTest {

    static MapLocation expectedHq;
    static Transaction[] block;
    static Transaction[] block1;
    static Landscaper testLandscaper;
    static RobotController mockRC1;


    @BeforeClass
    public static void setupLandscaperTest() {

        // instantiate mock RobotController for testing only
        mockRC1 = mock(RobotController.class);
        testLandscaper = new Landscaper(mockRC1);

        /**** mock Blockchain ****/

        expectedHq = new MapLocation(10, 25);
        int[] goodMessage = {603720, 2, 2, 10, 25, 0, 0};
        int[] badMessage = {123, 123, 123, 123, 123, 123, 123};

        // set up expected message
        block = new Transaction[10];
        Transaction teamTransaction = new Transaction(0, goodMessage, 0);
        Transaction notOurTransaction = new Transaction(0, badMessage, 10);
        for (int i = 0; i < 10; i++) {
            if (i == 4) {
                block[i] = teamTransaction;
            } else {
                block[i] = notOurTransaction;
            }
        }

        int[] attackMessage = {603720, 2, 2, 33, 33, 1, 0};
        int[] randMessage = {123, 123, 123, 123, 123, 123, 123};

        // set up expected message
        block1 = new Transaction[10];
        Transaction attackTransaction = new Transaction(0, attackMessage, 0);
        Transaction randTransaction = new Transaction(0, randMessage, 10);
        for (int i = 0; i < 10; i++) {
            if (i == 6) {
                block1[i] = attackTransaction;
            } else {
                block1[i] = randTransaction;
            }
        }

    }

    @Test
     public void testGetHqLoc() throws GameActionException {
        MapLocation actualHq = Landscaper.getHqLoc(block);
        Assert.assertEquals(expectedHq, actualHq);
    }

    @Test
    public void testFindDigDirs() {
        Direction hqDirection = Direction.NORTH;
        Direction[] expectedDirs = {Direction.SOUTH, Direction.SOUTHEAST, Direction.SOUTHWEST};
        Direction[] actualDirs = Landscaper.findDigDirs(hqDirection);
        for (int i = 0; i < 2; i++) {
            Assert.assertEquals(expectedDirs[i], actualDirs[i]);
        }
    }


    @Test
    public void testTryDepositDirt() throws GameActionException{
        when(mockRC1.isReady()).thenReturn(false);
        Assert.assertEquals(false, Landscaper.tryDepositDirt(Direction.EAST));
    }

    @Test
    public void testBuildWall() throws GameActionException {
        // hard code the directions where the landscaper should be depositing dirt
        Direction[] buildingDirs = {Direction.NORTH, Direction.SOUTH, Direction.SOUTHWEST, Direction.NORTHWEST, Direction.CENTER};
        // hard code landscaper location
        MapLocation currentLoc = new MapLocation(11,25);
        // mock landscaper ability to perform action this turn
        when(mockRC1.isReady()).thenReturn(true);
        when(mockRC1.getLocation()).thenReturn(currentLoc);

        for (int i = 0; i < 5; i++ ) {
            when(mockRC1.canDepositDirt(buildingDirs[i])).thenReturn(true);
            Assert.assertEquals(true, Landscaper.buildWall(expectedHq, (i)));
        }
    }

    @Test
    public void testCheckMessage() throws GameActionException {
        when(mockRC1.getBlock(10)).thenReturn(block1);
        Landscaper.LandscaperTask actualState = Landscaper.checkMessage(block1);

        Assert.assertEquals(Landscaper.LandscaperTask.OFFENSE_UNIT, actualState);
    }


    /* things to test:
    X getHqLoc func
    X digAway
    around bldg
    tryDig
    X tryDeposit
    buildWall
    what are the edge cases?...where should i be concerned for bugs?
     - maybe when it gets later in the game, and there is a lot of flooding
     surrounded by too-high elevation on all sides
     surrounded by water
     */
}
