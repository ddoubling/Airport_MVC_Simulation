import java.awt.*;
import javax.swing.*;

/**
 * creates the digital clock display
 * @author group2 
 *
 */
public class TimeDigital extends JPanel{
	
	private JTextField timeText = new JTextField(10); // the clock display

	public TimeDigital() {
		this.add(timeText);
		timeText.setEditable(false);
		timeText.setHorizontalAlignment(JTextField.CENTER);
		Font timeFont = new Font("SansSerif", Font.BOLD, 20);
		timeText.setFont(timeFont);
	}
	
	/**
	 * updates the textfield with corresponding String to display time
	 * @param s
	 */
	public void updateDigital(String s) {
		timeText.setText(s);
	}
}
