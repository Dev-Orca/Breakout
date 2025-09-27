import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class BreakoutWithExtentions extends GraphicsProgram {

/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH-2;
	private static final int HEIGHT = APPLICATION_HEIGHT-27;

/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

/** Separation between bricks */
	private static final int BRICK_SEP = 4;

/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

/** Number of turns */
	private static final int NTURNS = 3;

/** random number/boolean generator */
	private RandomGenerator rgen = RandomGenerator.getInstance();

/** posibility of ball being stuck in paddle */
	private boolean ballMightBeStuck = false;
	
/** state of the game */
	private boolean gameFinished = false;	
	
/** direction of ball's movement */
	private double vx;
	private double vy;
	
/** remaining turns a player has left */
	private int remainingTurns = NTURNS;
	
/** remaining bricks on the screen */
	private int remainingBricks = NBRICK_ROWS * NBRICKS_PER_ROW;
	
/** border lines */	
	private GLine leftLine = new GLine(1,1,1,(double)HEIGHT);
	private GLine rightLine = new GLine((double)WIDTH,1,(double)WIDTH,(double)HEIGHT);
	private GLine topLine = new GLine(1,1,(double)WIDTH,1);
	private GLine bottomLine = new GLine(1,(double)HEIGHT,(double)WIDTH,(double)HEIGHT);
	
/** the ball */	
	private GOval ball =new GOval(2 * BALL_RADIUS, 2 * BALL_RADIUS);
	
/** the paddle */
	private GRect paddle = new GRect(WIDTH / 2 - PADDLE_WIDTH / 2, 
			HEIGHT - PADDLE_Y_OFFSET - PADDLE_HEIGHT, 
			PADDLE_WIDTH, PADDLE_HEIGHT);

/** creates an audio clip for a ball hit */
	private AudioClip bounceClip = MediaTools.loadAudioClip("bounce.au");
	private AudioClip music = MediaTools.loadAudioClip("music.mp3");
	
/** ball's speed */
	private int ballSpeedOnHighHeight = 5;
	private int ballSpeedOnLowHeight = 8;
	
/** ball's speeds levels */
	private boolean isOnSpeedLevel2 = false;
	private boolean isOnSpeedLevel3 = false;
	private boolean isOnSpeedLevel4 = false;
	
/** player's points */
	private int points = 0;
	
/** the score of the player */
	private GLabel score = new GLabel("" + points + "",WIDTH / 2 - 3, 26);
	
/** part of UI , which keeps track of players lives */
	private GLabel lives = new GLabel("" + remainingTurns + "",WIDTH - 64, 18);
	
/** color of the score */
	private Color scoreColor = Color.ORANGE;
	
/* Method: run() */
/** Runs the Breakout program. */
	public void run() {
		setup();
		startGame();
	}

	/* while the game is running, this method runs everything */
	private void startGame() {
		while(true && gameFinished == false){
			ball.move(vx, vy);	
			
			controllAllSpeed();
			bounceOnHit();
			checkForGameOver();
			printUI();
		}	 
	
	}

	/* prints all UI */
	private void printUI() {
		printScore();
		printLives();
		
	}

	/* prints the remaining lives of the player */
	private void printLives() {
		remove(lives);
		lives = new GLabel("Lives: " + remainingTurns + "",WIDTH - 64, 18);
		lives.setFont(new Font( "SansSerif", Font.PLAIN, 18));
		lives.setColor(Color.RED);
		add(lives);
	}

	/* controll's the speed of the ball everywhere, any time */
	private void controllAllSpeed() {
		controllSpeed();
		pauseDependingOnHeight();
	}

	/* controls the speed of the ball depending on the score*/
	private void controllSpeed() {
		if(points >= 50 && isOnSpeedLevel2 == false){
			isOnSpeedLevel2 = true;
			increaseBallSpeed();
			scoreColor =Color.GREEN;
		}
		if(points >= 100 && isOnSpeedLevel3 == false){
			isOnSpeedLevel3 = true;
			increaseBallSpeed();
			scoreColor =Color.BLUE;
		}
		if(points >= 150 && isOnSpeedLevel4 == false){
			isOnSpeedLevel4 = true;
			increaseBallSpeed();
			scoreColor =Color.PINK;
		}
		
	}

	/* increases ball speed */
	private void increaseBallSpeed() {
		ballSpeedOnHighHeight--;
		ballSpeedOnLowHeight--;
		
	}

	/* prints player's score */
	private void printScore() {
		remove(score);
		score = new GLabel("" + points + "",WIDTH / 2 - 3, 26);
		score.setFont(new Font( "SansSerif", Font.PLAIN, 26));
		score.setColor(scoreColor);
		add(score);
	}

	/* briefly changes speed according to games screen's height */
	private void pauseDependingOnHeight() {
		if(HEIGHT > 750){
			pause(ballSpeedOnHighHeight);
		}
		else{
			pause(ballSpeedOnLowHeight);
		}
		
	}

	/* bounces a ball if it hits something */
	private void bounceOnHit() {
		bounceFromWall();
		bounceFromObjects();
		
	}

	/* checks if any game-ending conditions are true */
	private void checkForGameOver() {
		ifWin();
		ifLose();
		
	}
	/* if a player runs out of turns, game stops and a losing text gets printed */
	private void ifLose() {
		if(remainingTurns == 0){
		   gameFinished = true;
		   printLoseText();
		}
		
	}

	/* prints losing text */
	private void printLoseText() {
		GLabel loseText = new GLabel("YOU LOSE!", WIDTH / 2 - 90, HEIGHT / 2);
		   loseText.setFont(new Font( "SansSerif", Font.PLAIN, 40 ));
		   add(loseText);
		
	}

	/* if a player won, game stops and a win text gets printed */
	private void ifWin() {
		if(remainingBricks == 0)
		{
			gameFinished = true;
			printsWiningText();
		}
	}

	/* prints wining text */
	private void printsWiningText() {
		GLabel winText = new GLabel("YOU WIN!", WIDTH / 2 - 12, HEIGHT / 2);
		winText.setFont(new Font( "SansSerif", Font.PLAIN, 40 ));
		add(winText);
		
	}

	/* bounces a ball if it hit anything */
	private void bounceFromObjects() {
		if(getCollidingObject() != null)
		{
			GObject collider = getCollidingObject();
			
			checkIfBallIsStuck();
			ballHitSomething(collider);
		}
		
	}

	/* checks what a ball is hitting and acts according to it */
	private void ballHitSomething(GObject collider) {
		if(collider == paddle){
			bounceClip.play();
			ballHitPaddle();
		}
		else if (collider != leftLine && collider != rightLine && collider != topLine && collider != bottomLine){
			ballHitBrick(collider);
			bounceClip.play();
		}
		
	}

	/* then a ball hits a brick, brick gets destroyed and ball gets reflected */
	private void ballHitBrick(GObject collider) {
		vy = -vy;
		
		addPointsBasedOnBrickColor(collider);
		remove(collider);
		ballMightBeStuck = false;
		remainingBricks--;
	}

	/* adds points based on brick color */
	private void addPointsBasedOnBrickColor(GObject collider) {
		if(collider.getColor() == Color.CYAN){
			points = points + 1;
		}
		if(collider.getColor() == Color.GREEN){
			points = points + 2;
		}
		if(collider.getColor() == Color.YELLOW){
			points = points + 3;
		}
		if(collider.getColor() == Color.ORANGE){
			points = points + 4;
		}
		if(collider.getColor() == Color.RED){
			points = points + 5;
		}
		
	}

	/* reflects the ball, if it hits the paddle */
	private void ballHitPaddle() {
		ballHitRightSideOfPaddle();
		ballHitLeftSideOfPaddle();
		vy = -vy;
		ballMightBeStuck = true;
		
	}

	/* if ball hit the right side of the paddle, ball acts accordingly */
	private void ballHitRightSideOfPaddle() {
		if(ball.getX() + BALL_RADIUS > paddle.getX() + PADDLE_WIDTH / 2){
			if(vx < 0){
				vx = - vx;
			}
		}
		
	}
	
	/* if ball hit the left side of the paddle, ball acts accordingly */
	private void ballHitLeftSideOfPaddle() {
		if(ball.getX() + BALL_RADIUS < paddle.getX() + PADDLE_WIDTH / 2){
			if(vx > 0){
				vx = - vx;
			}
		}
		
	}

	/* if the ball is Stuck It helps it get out */
	private void checkIfBallIsStuck() {
		if(ballMightBeStuck == true){
			ball.move(0, 3);
		}
		
	}

	/* returns an object, which a ball is colliding with, if ball isn't colliding with anything , it returns null */
	private GObject getCollidingObject() {
		if(getElementAt(ball.getX(), ball.getY()) != null){
			return getElementAt(ball.getX(), ball.getY());
		}
		if(getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY()) != null){
			return getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY());
		}
		if(getElementAt(ball.getX(), ball.getY() + 2 * BALL_RADIUS) != null){
			return getElementAt(ball.getX(), ball.getY() + 2 * BALL_RADIUS);
		}
		if(getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY() + 2 * BALL_RADIUS) != null){
			return getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY() + 2 * BALL_RADIUS);
		}
		return null;
	}

	/* bounces from walls if hit */
	private void bounceFromWall() {
		checkForLeftWall();
		checkForRightWall();
		checkForTopWall();
		checkForBottomWall();	
	}

	/* checks if the ball hit the top wall and reflects the ball */
	private void checkForTopWall() {
		if(ball.getY() <= 0){
			vy = -vy;
			ballMightBeStuck = false;
			
		}
		
	}

	/* checks if the ball hit the bottom wall and reflects the ball while the player loses 1 turn*/
	private void checkForBottomWall() {
		if(ball.getY() + 2 * BALL_RADIUS >= HEIGHT){
			ballMightBeStuck = false;
			remainingTurns--;
			resetBall();
			
		}
		
	}

	/* this makes a dead ball disappear and a new one appear */
	private void resetBall() {
		remove(ball);
		if(remainingTurns != 0)
		{
			addBall();
		}
		resetSpeed();
	}

	/* resets balls speed to 0 */
	private void resetSpeed() {
		vx = 0;
		vy = 0;
		
	}

	/* checks if the ball hit the right wall and reflects the ball */
	private void checkForRightWall() {
		if(ball.getX() + 2 * BALL_RADIUS >= WIDTH){
			vx = -vx;
			ballMightBeStuck = false;
		}	
		
	}

	/* checks if the ball hit the left wall and reflects the ball */
	private void checkForLeftWall() {
		if(ball.getX() <= 0){
			vx = -vx;
			ballMightBeStuck = false;
		}
	}

	/* adds a ball with speed */
	private void addBall() {
		placeBall();
	}

	/* makes a ball appear */
	private void placeBall() {
		ball.setFilled(true);
		add(ball, WIDTH / 2 - BALL_RADIUS, HEIGHT / 2 - BALL_RADIUS);
		
	}

	/* sets up everything before the game starts */
	private void setup() {
		music.play();
		addPaddle();
		addBricks();
		addBall();
		addAllLines();
	}

	/* adds all the border lines */
	private void addAllLines() {
		add(leftLine);
		add(rightLine);
		add(topLine);
		add(bottomLine);
	}

	/* adds all the bricks */
	private void addBricks() {
		for(int i = 1; i <= NBRICK_ROWS; i++){
			putRow(i);
		}
		
	}

	/* puts a row of bricks down with colors*/
	private void putRow(int indexOfRow) {
		Color brickColor = getRowColor(indexOfRow);
		putBricks(brickColor, indexOfRow);
	}
	
	/* puts a row of bricks */
	private void putBricks(Color brickColor, int indexOfRow) {
		for(int i = 1; i <= NBRICKS_PER_ROW; i++){
			GRect brick = new GRect((i - 1) * (BRICK_WIDTH + BRICK_SEP) + BRICK_SEP / 2, 
					(indexOfRow - 1) * (BRICK_HEIGHT + BRICK_SEP) + BRICK_Y_OFFSET,
					BRICK_WIDTH, BRICK_HEIGHT);
			brick.setFilled(true);
			brick.setColor(brickColor);
			add(brick);
			
		}
		
	}
	
	/* returns the color of row */
	private Color getRowColor(int indexOfRow) {
		int n = indexOfRow % 10;
		
		if(n == 1 || n == 2){
			return Color.RED;
		}
		if(n == 3 || n == 4){
			return Color.ORANGE;
		}
		if(n == 5 || n == 6){
			return Color.YELLOW;
		}
		if(n == 7 || n == 8){
			return Color.GREEN;
		}
		else{
			return Color.CYAN;
		}
		
	}
	
	/* adds paddle */
	private void addPaddle() {
		paddle.setFilled(true);
		add(paddle);
		addMouseListeners();
	}
	
	/* moves paddle when mouse moves */
	public void mouseMoved(MouseEvent e){
		if( !(paddle.getX() <= 5 && e.getX() - paddle.getX() - PADDLE_WIDTH / 2 < 0) &&
				!(paddle.getX() + PADDLE_WIDTH >= WIDTH - 5 && e.getX() - paddle.getX() - PADDLE_WIDTH / 2 > 0)){
			paddle.move(e.getX() - paddle.getX() - PADDLE_WIDTH / 2, 0);
		}
		
	}
	
	/* starts moving a ball on click */
	public void mousePressed(MouseEvent e){
		if(vx == 0 && vy == 0){
			giveSpeedToBall();
		}
	}

	/* gives the ball speed */
	private void giveSpeedToBall() {
		vy = 3.0;
		vx = rgen.nextDouble(1.0, 3.0);
		if (rgen.nextBoolean(0.5)) {
			vx = -vx;
		}
		
	}
	

}
