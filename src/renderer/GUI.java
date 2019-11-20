package renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A simple GUI, similar to the one in assignments 1 and 2, that you can base
 * your renderer off. It is abstract, and there are three methods you need to
 * implement: onLoad, onKeyPress, and render. There is a method to get the
 * ambient light level set by the sliders. You are free to use this class as-is,
 * modify it, or ignore it completely.
 *
 * @author tony
 */
public abstract class GUI {

	/**
	 * Is called when the user has successfully selected a model file to load, and
	 * is passed a File representing that file.
	 */
	protected abstract void onLoad(File file);

	/**
	 * Is called every time the user presses a key. This can be used for moving the
	 * camera around. It is passed a KeyEvent object, whose methods of interest are
	 * getKeyChar() and getKeyCode().
	 */
	protected abstract void onKeyPress(KeyEvent ev);

	protected abstract void Scroll(MouseWheelEvent e);

	protected abstract void Released(MouseEvent e);

	protected abstract void Pressed(MouseEvent e);

	protected abstract void switchMoveRotate();

	protected abstract void Default();

	/**
	 * Is called every time the drawing canvas is drawn. This should return a
	 * BufferedImage that is your render of the scene.
	 */
	protected abstract BufferedImage render();

	/**
	 * Forces a redraw of the drawing canvas. This is called for you, so you don't
	 * need to call this unless you modify this GUI.
	 */
	public void redraw() {
		frame.repaint();
	}

	/**
	 * Returns the values of the three sliders used for setting the ambient light of
	 * the scene. The returned array in the form [R, G, B] where each value is
	 * between 0 and 255.
	 */
	public Color getAmbientLight() {
		return new Color(red.getValue(), green.getValue(), blue.getValue());
	}

	public Color getAddedLight() {
		return new Color(redAddedLight.getValue(), greenAddedLight.getValue(), blueAddedLight.getValue());
	}

	public Dimension getDrawingSize() {
		return DRAWING_SIZE;
	}

	public static final int CANVAS_WIDTH = 600;
	public static final int CANVAS_HEIGHT = 600;

	// --------------------------------------------------------------------
	// Everything below here is Swing-related and, while it's worth
	// understanding, you don't need to look any further to finish the
	// assignment up to and including completion.
	// --------------------------------------------------------------------

	private JFrame frame;
	private final JSlider red = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);
	private final JSlider green = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);
	private final JSlider blue = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);

	private final JSlider redAddedLight = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);
	private final JSlider greenAddedLight = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);
	private final JSlider blueAddedLight = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);

	private static final Dimension DRAWING_SIZE = new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT);
	private static final Dimension CONTROLS_SIZE = new Dimension(150, 600);

	private static final Font FONT = new Font("Courier", Font.BOLD, 36);

	public GUI() {
		initialise();
	}

	@SuppressWarnings("serial")
	private void initialise() {
		// make the frame
		frame = new JFrame();
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.LINE_AXIS));
		frame.setSize(new Dimension(DRAWING_SIZE.width + CONTROLS_SIZE.width, DRAWING_SIZE.height));
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set up the drawing canvas, hook it into the render() method, and give
		// it a nice default if render() returns null.
		JComponent drawing = new JComponent() {
			protected void paintComponent(Graphics g) {
				BufferedImage image = render();
				if (image == null) {
					g.setColor(Color.WHITE);
					g.fillRect(0, 0, DRAWING_SIZE.width, DRAWING_SIZE.height);
					g.setColor(Color.BLACK);
					g.setFont(FONT);
					g.drawString("IMAGE IS NULL", 50, DRAWING_SIZE.height - 50);
				} else {
					g.drawImage(image, 0, 0, null);
				}
			}
		};

		drawing.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Pressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				Released(e);
				redraw();
			}

		});

		drawing.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				Scroll(e);
				redraw();
			}
		});

		// fix its size
		drawing.setPreferredSize(DRAWING_SIZE);
		drawing.setMinimumSize(DRAWING_SIZE);
		drawing.setMaximumSize(DRAWING_SIZE);
		drawing.setVisible(true);

		// set up the load button
		final JFileChooser fileChooser = new JFileChooser();
		JButton load = new JButton("Load");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				// set up the file chooser
				fileChooser.setCurrentDirectory(new File("."));
				fileChooser.setDialogTitle("Select input file");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				// run the file chooser and check the user didn't hit cancel
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					onLoad(file);
					redraw();
				}
			}
		});

		JButton moveOrRotate = new JButton("Move");
		moveOrRotate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (moveOrRotate.getText().equals("Move"))
					moveOrRotate.setText("Rotate");
				else
					moveOrRotate.setText("Move");
				switchMoveRotate();
			}
		});
		JButton defaultButton = new JButton("Default");
		defaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				red.setValue(128);
				green.setValue(128);
				blue.setValue(128);
				redAddedLight.setValue(128);
				greenAddedLight.setValue(128);
				blueAddedLight.setValue(128);
				Default();
				redraw();
			}
		});

		// we have to put the button in its own panel to ensure it fills the
		// full width of the control bar.
		JPanel loadpanel = new JPanel(new GridLayout(3, 1));
		loadpanel.setMaximumSize(new Dimension(1000, 50));
		loadpanel.setPreferredSize(new Dimension(1000, 100));
		loadpanel.add(load);
		loadpanel.add(moveOrRotate);
		loadpanel.add(defaultButton);

		// set up the sliders for ambient light. they were instantiated in
		// the field definition, as for some reason they need to be final to
		// pull the set background trick.
		red.setBackground(new Color(230, 50, 50));
		green.setBackground(new Color(50, 230, 50));
		blue.setBackground(new Color(50, 50, 230));

		JPanel sliderparty = new JPanel();
		sliderparty.setLayout(new BoxLayout(sliderparty, BoxLayout.PAGE_AXIS));
		sliderparty.setBorder(BorderFactory.createTitledBorder("Ambient Light"));

		JComponent ambientColor = new JComponent() {
			@Override
			protected void paintComponent(Graphics g) {
				g.setColor(new Color(red.getValue(), green.getValue(), blue.getValue()));
				g.fillRect(0, 0, CONTROLS_SIZE.width, 30);
			}
		};
		Dimension reviewDimension = new Dimension(150, 15);
		ambientColor.setPreferredSize(reviewDimension);
		ambientColor.setMinimumSize(reviewDimension);
		ambientColor.setMaximumSize(reviewDimension);
		ambientColor.setVisible(true);

		ChangeListener listenerAmbient = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ambientColor.repaint();
				redraw();
			}
		};
		red.addChangeListener(listenerAmbient);
		green.addChangeListener(listenerAmbient);
		blue.addChangeListener(listenerAmbient);

		sliderparty.add(red);
		sliderparty.add(green);
		sliderparty.add(blue);

		// for the added light
		redAddedLight.setBackground(new Color(230, 50, 50));
		greenAddedLight.setBackground(new Color(50, 230, 50));
		blueAddedLight.setBackground(new Color(50, 50, 230));

		JPanel sliderpartyAddedLight = new JPanel();
		sliderpartyAddedLight.setLayout(new BoxLayout(sliderpartyAddedLight, BoxLayout.PAGE_AXIS));
		sliderpartyAddedLight.setBorder(BorderFactory.createTitledBorder("Added Light"));

		JComponent directColor = new JComponent() {
			@Override
			protected void paintComponent(Graphics g) {
				g.setColor(new Color(redAddedLight.getValue(), greenAddedLight.getValue(), blueAddedLight.getValue()));
				g.fillRect(0, 0, CONTROLS_SIZE.width, 30);
			}
		};

		directColor.setPreferredSize(reviewDimension);
		directColor.setMinimumSize(reviewDimension);
		directColor.setMaximumSize(reviewDimension);
		directColor.setVisible(true);

		ChangeListener listenerAddedLight = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				directColor.repaint();
				redraw();
			}
		};

		redAddedLight.addChangeListener(listenerAddedLight);
		greenAddedLight.addChangeListener(listenerAddedLight);
		blueAddedLight.addChangeListener(listenerAddedLight);

		sliderpartyAddedLight.add(redAddedLight);
		sliderpartyAddedLight.add(greenAddedLight);
		sliderpartyAddedLight.add(blueAddedLight);
		sliderpartyAddedLight.add(directColor);

		// this is not a best-practices way of doing key listening; instead you
		// should use either a KeyListener or an InputMap/ActionMap combo. but
		// this method neatly avoids any focus issues (KeyListener) and requires
		// less effort on your part (ActionMap).
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent ev) {
				if (ev.getID() == KeyEvent.KEY_PRESSED) {
					onKeyPress(ev);
					redraw();
				}
				return true;
			}
		});

		// make the panel on the right, fix its size, give it a border!
		JPanel controls = new JPanel();
		controls.setPreferredSize(CONTROLS_SIZE);
		controls.setMinimumSize(CONTROLS_SIZE);
		controls.setMaximumSize(CONTROLS_SIZE);
		controls.setLayout(new BoxLayout(controls, BoxLayout.PAGE_AXIS));
		Border edge = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		controls.setBorder(edge);

		controls.add(loadpanel);
		controls.add(Box.createRigidArea(new Dimension(0, 15)));
		controls.add(sliderparty);
		controls.add(Box.createRigidArea(new Dimension(0, 15)));
		controls.add(sliderpartyAddedLight);
		// if i were going to add more GUI components, i'd do it here.
		controls.add(Box.createVerticalGlue());

		// put it all together.
		frame.add(drawing);
		frame.add(controls);

		frame.pack();
		frame.setVisible(true);
	}
}

// code for comp261 assignments
