package menu;

import javax.swing.JRadioButton;
import javax.swing.JMenu;

import api.ViewType;
import controller.ZGlobeViewController;

public final class ViewsMenu extends JMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -470772476656995980L;

	private static JRadioButton[] buttons;

	static {
		buttons = new JRadioButton[] { //
				new JRadioButton("Elevation", true), //
				new JRadioButton("Vegetation", false), //
				new JRadioButton("Temperature", false), //
				new JRadioButton("Aridity", false), //
				new JRadioButton("Humidity", false), //
				new JRadioButton("Precipitation", false), //
				new JRadioButton("Regions", false), //
				new JRadioButton("Latitude", false) //
		};
	}

	/*
	 * CONSTRUCTORS
	 */
	public ViewsMenu() {
		super("View");

		for (int i = 0; i < buttons.length; ++i) {
			add(buttons[i]);

			if (i == 5)
				this.addSeparator();
		}

		buttons[0].addActionListener(e -> changeView(buttons[0], ViewType.ELEVATION));
		buttons[1].addActionListener(e -> changeView(buttons[1], ViewType.VEGETATION));
		buttons[2].addActionListener(e -> changeView(buttons[2], ViewType.TEMPERATURE));
		buttons[3].addActionListener(e -> changeView(buttons[3], ViewType.ARIDITY));
		buttons[4].addActionListener(e -> changeView(buttons[4], ViewType.HUMIDITY));
		buttons[5].addActionListener(e -> changeView(buttons[5], ViewType.PRECIPITATION));
		//
		buttons[6].addActionListener(e -> changeView(buttons[6], ViewType.REGION));
		buttons[7].addActionListener(e -> changeView(buttons[7], ViewType.LATITUDE));
	}

	/*
	 * PRIVATE METHODS
	 */
	private <F> void changeView(JRadioButton button, ViewType type) {
		ZGlobeViewController.setViewType(type);

		for (JRadioButton el : buttons)
			el.setSelected(false);

		button.setSelected(true);
	}

}
