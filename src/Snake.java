
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

public class Snake {
	// dimensions of window
	static final int windowX = 1200;
	static final int windowY = 600;

	// dimensions of playing field
	static final int X = 100;
	static final int Y = 50;

	// delay (in milliseconds) between each tick
	static int delay = 40;

	// border width between every square
	static final int border = 2;

	// objects to display board
	static BufferedImage image = new BufferedImage(windowX, windowY, BufferedImage.TYPE_INT_RGB);
	static JFrame frame = new JFrame("Snake");
	static Canvas canvas = new Canvas();
	static GraphicsEnvironment ge;
	static GraphicsConfiguration gc;
	static GraphicsDevice gd;
	static Graphics graphics;
	static Graphics2D g2d;
	static BufferStrategy buffer;

	// object to generate random food positions
	static final Random rand = new Random();

	// fixed definitions of every possible direction
	static final Point right = new Point(0, 1);
	static final Point left = new Point(0, -1);
	static final Point up = new Point(-1, 0);
	static final Point down = new Point(1, 0);

	// set of positions of foods
	static HashSet<Point> foods = new HashSet<Point>();

	// head of snake
	static Point head;
	// body of snake
	static Queue<Point> body = new LinkedList<Point>();

	// timer to execute ticks
	static Timer timer = new Timer();

	// queue to store moves for better smoothness
	static Queue<Point> moves = new LinkedList<Point>();

	// different conditions on 1st
	static boolean first;

	public static void main(String[] args) {
		initFrame();
		start();
	}

	// draws foods and the snake
	private static void draw() {
		g2d = image.createGraphics();
		for (int i = 0; i < Y; i++) {
			for (int j = 0; j < X; j++) {
				// if none, black
				g2d.setColor(Color.black);
				// if food, red
				for (Point food : foods) {
					if (food.equals(new Point(i, j)))
						g2d.setColor(Color.red);
				}
				// if snake, white
				if (body.contains(new Point(i, j)))
					g2d.setColor(Color.white);

				// draw
				g2d.fillRect(j * windowY / Y + border, i * windowX / X + border, windowX / X - border,
						windowY / Y - border);
			}
		}
		// update buffer
		graphics = buffer.getDrawGraphics();
		graphics.drawImage(image, 0, 0, null);
		g2d.dispose();
		graphics.dispose();
		buffer.show();
	}

	private static void start() {
		// reset timer
		timer.purge();
		// reset snake
		body.clear();
		// reset starting position & add to body
		head = new Point(0, 0);
		body.add(head);

		// reset foods
		foods.clear();
		for (int i = 0; i < 5; i++) {
			Point food = new Point(0, 0);
			do {
				food.x = rand.nextInt(Y);
				food.y = rand.nextInt(X);
			} while (body.contains(food) || foods.contains(food));

			foods.add(food);
		}

		// default start moving right
		moves.add(right);

		first = true;

		// start timer
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				// update position based on move direction
				Point move = moves.remove();
				Point newHead = new Point(head.x + move.x, head.y + move.y);

				// if no inputed moves then continue last move
				if (moves.isEmpty())
					moves.add(move);

				// if head is in body, lose
				if (body.contains(newHead) && !first) {
					start();
					this.cancel();
					return;
				}

				// wrap edges
				if (newHead.x >= Y)
					newHead.x = 0;
				if (newHead.y >= X)
					newHead.y = 0;
				if (newHead.x < 0)
					newHead.x = Y - 1;
				if (newHead.y < 0)
					newHead.y = X - 1;

				// add new head
				body.add(newHead);

				boolean remove = true;

				// relocate food
				for (Point food : foods) {
					if (food.equals(newHead)) {
						do {
							food.x = rand.nextInt(Y);
							food.y = rand.nextInt(X);
						} while (body.contains(food) || foods.contains(food));
						remove = false;
					}
				}

				// if food is picked up this turn, don't move tail
				// also, speed up each turn by 1 ms
				if (remove)
					body.remove();
				else
					delay--;

				first = false;
				head = newHead;

				draw();
			}
		}, 0, delay);
	}

	private static void initFrame() {
		frame.setIgnoreRepaint(true);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);

		canvas.setFocusable(false);
		canvas.setSize(windowX, windowY);
		canvas.createBufferStrategy(2);
		
		buffer = canvas.getBufferStrategy();
		// Get graphics configuration...
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();
		// Create off-screen drawing surface
		image = gc.createCompatibleImage(windowX, windowY);
		// Objects needed for rendering...
		graphics = null;
		g2d = null;

		// controls
		frame.addKeyListener(new java.awt.event.KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					if (!moves.peek().equals(left))
						moves.add(right);
				} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					if (!moves.peek().equals(right))
						moves.add(left);
				} else if (e.getKeyCode() == KeyEvent.VK_UP) {
					if (!moves.peek().equals(down))
						moves.add(up);
				} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					if (!moves.peek().equals(up))
						moves.add(down);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
		
		frame.add(canvas);
		frame.pack();
	}
}
