package menu;

import javax.swing.JRadioButton;
import javax.swing.JMenu;

import api.ViewType;
import controller.PlanetViewController;

public final class ViewsMenu extends JMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -470772476656995980L;

	private static JRadioButton[] buttons;

	static {
		buttons = new JRadioButton[] { //
				new JRadioButton("Topography", true), //
				new JRadioButton("Vegetation", false), //
				new JRadioButton("Temperature", false), //
				new JRadioButton("Aridity", false), //
				new JRadioButton("Humidity", false), //
				new JRadioButton("Precipitation", false) //
		};
	}

	/*
	 * CONSTRUCTORS
	 */
	public ViewsMenu() {
		super("View");

		for (JRadioButton el : buttons)
			add(el);

		buttons[0].addActionListener(e -> changeView(buttons[0], ViewType.TOPOGRAPHY));
		buttons[1].addActionListener(e -> changeView(buttons[1], ViewType.VEGETATION));
		buttons[2].addActionListener(e -> changeView(buttons[2], ViewType.TEMPERATURE));
		buttons[3].addActionListener(e -> changeView(buttons[3], ViewType.ARIDITY));
		buttons[4].addActionListener(e -> changeView(buttons[4], ViewType.HUMIDITY));
		buttons[5].addActionListener(e -> changeView(buttons[5], ViewType.PRECIPITATION));
	}

	/*
	 * PRIVATE METHODS
	 */
	private <F> void changeView(JRadioButton button, ViewType type) {
		PlanetViewController.setViewType(type);

		for (JRadioButton el : buttons)
			el.setSelected(false);

		button.setSelected(true);
	}

}
