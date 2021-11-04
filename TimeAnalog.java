import java.awt.*;
import javax.swing.*;

/**
 * creates the analog clock
 * @author group2 
 *
 */
public class TimeAnalog extends JPanel {
	
	private Font clockFont; // Font for number display on clock
	private Color handColour; // Colour of ticking hands
	private Color numberColour; // Colour of numbers
	private Color clockColour; //colour for outer circle

	private int lastxo, lastyo, lastxt, lastyt;
	private int x=90,y=100;
	private int xo,yo,xt,yt;
 
	public TimeAnalog() { 
		super();
		lastxo=lastyo=lastxt=lastyt=0;
		xo=yo=xt=yt=0;
		clockFont = new Font("Serif", Font.BOLD, 15);//15 for big
		clockColour = Color.BLUE;
		handColour = Color.RED;
		numberColour = Color.BLACK;
		this.setSize(150,150);
	}

	/**
	 * updates the clock ticking hands
	 * @param h hours
	 * @param m minutes
	 */
	public void updateAnalog(int h, int m) {
		// Set position of the ends of the hands
		xo = (int) (Math.cos(m * Math.PI / 30 - Math.PI / 2) * 60 + x); 
		yo = (int) (Math.sin(m * Math.PI / 30 - Math.PI / 2) * 60 + y);
		xt = (int) (Math.cos((h * 30 + m / 2) * Math.PI / 180 - Math.PI / 2) * 40 + x);
		yt = (int) (Math.sin((h * 30 + m / 2) * Math.PI / 180 - Math.PI / 2) * 40 + y);
		repaint();
	}
	
	/**
	 * sets start and end positions of clock components
	 * paints components accordingly
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(clockFont);
		this.setOpaque(false);
		g.setColor(getBackground());

		if (xo != lastxo || yo != lastyo) {
			g.drawLine(x, y-1, lastxo, lastyo);
			g.drawLine(x-1, y, lastxo, lastyo);
		}
		if (xt != lastxt || yt != lastyt) {
			g.drawLine(x, y-1, lastxt, lastyt);
			g.drawLine(x-1, y, lastxt, lastyt);
		}
		
		// Draw the circle and numbers
		g.setColor(clockColour);
		g.drawOval(15,20,150,150);
		g.setColor(numberColour);
		g.drawString("1",x+32,y-54);
		g.drawString("2",x+53,y-31);
		g.drawString("3",x+65,y);
		g.drawString("4",x+53,y+34);
		g.drawString("5",x+30,y+57);
		g.drawString("6",x-3,y+65);
		g.drawString("7",x-33,y+57);
		g.drawString("8",x-57,y+34);
		g.drawString("9",x-70,y);
		g.drawString("10",x-60,y-30);
		g.drawString("11",x-35,y-55);
		g.drawString("12",x-5,y-65);
		
		//hand lines
		g.setColor(handColour);
		g.drawLine(x, y - 1, xo, yo);
		g.drawLine(x - 1, y, xo, yo);
		g.drawLine(x, y - 1, xt, yt);
		g.drawLine(x - 1, y, xt, yt);
		lastxo = xo;
		lastyo = yo;
		lastxt = xt;
		lastyt = yt;
	
	}
}
