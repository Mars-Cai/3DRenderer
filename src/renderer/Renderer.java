package renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import renderer.Scene.Polygon;

public class Renderer extends GUI {

	private Scene scene;
	private Scene centralisedScene;
	private float xRot = 0f, yRot = 0f;
	private Vector3D viewer;
	private float currentScale = 1.0f;
	private static final float MIN_ZOOM = 0.5f, MAX_ZOOM = 5.0f;
	private boolean isRotating = true;
	private Point dragStart;

	@Override
	protected void onLoad(File file) {
		xRot = 0f;
		yRot = 0f;
		viewer = new Vector3D(0f, 0f, 0f);
		currentScale = 1.0f;

		List<Polygon> polygons = new ArrayList<Polygon>();
		Vector3D lightPos;
		try {
			BufferedReader Reader = new BufferedReader(new FileReader(file));
			String line = Reader.readLine();

			if (line == null) {
				System.out.println("Nothing found in the file.");
				Reader.close();
				return;
			}

			String[] values = line.split(" ");

			// the light
			lightPos = new Vector3D(Float.parseFloat(values[0]), Float.parseFloat(values[1]),Float.parseFloat(values[2]));

			line = Reader.readLine();

			while (line != null) {
				values = line.split(" ");
				// for the polygon
				float ax = Float.parseFloat(values[0]);
				float ay = Float.parseFloat(values[1]);
				float az = Float.parseFloat(values[2]);
				float bx = Float.parseFloat(values[3]);
				float by = Float.parseFloat(values[4]);
				float bz = Float.parseFloat(values[5]);
				float cx = Float.parseFloat(values[6]);
				float cy = Float.parseFloat(values[7]);
				float cz = Float.parseFloat(values[8]);
				float[] points = new float[] { ax, ay, az, bx, by, bz, cx, cy, cz };

				// for the color of the polygon
				int r = Integer.parseInt(values[9]);
				int g = Integer.parseInt(values[10]);
				int b = Integer.parseInt(values[11]);
				int[] color = new int[] { r, g, b };

				polygons.add(new Polygon(points, color));

				line = Reader.readLine();
			}
			Reader.close();
			this.scene = new Scene(polygons, lightPos);
		} catch (IOException e) {
			System.err.println("IOException");
		}
	}

	@Override
	protected void onKeyPress(KeyEvent ev) {
		char c = ev.getKeyChar();
		// Rotate
		if (ev.getKeyCode() == KeyEvent.VK_LEFT)
			yRot = 0.1f;
		else if (ev.getKeyCode() == KeyEvent.VK_RIGHT)
			yRot = -0.1f;
		else if (ev.getKeyCode() == KeyEvent.VK_UP)
			xRot = -0.1f;
		else if (ev.getKeyCode() == KeyEvent.VK_DOWN)
			xRot = 0.1f;
		else {
			// Translate
			xRot = 0f; yRot =0f;currentScale = 1.0f;
			if (c == 'w' || c == 'W')
				viewer = viewer.plus(new Vector3D(0f, -2f, 0f));
			else if (c == 's' || c == 'S')
				viewer = viewer.plus(new Vector3D(0f, 2f, 0f));
			else if (c == 'a' || c == 'A')
				viewer = viewer.plus(new Vector3D(-2f, 0f, 0f));
			else if (c == 'd' || c == 'D')
				viewer = viewer.plus(new Vector3D(2f, 0f, 0f));
			// Zoom "L" for LARGER and "M" for MINI
			else if (c == 'l' || c == 'L') {
				currentScale *= 1.2f;
				if (currentScale > MAX_ZOOM)
					currentScale = MAX_ZOOM;
			} else if (c == 'm' || c == 'M') {
				currentScale *= 0.8f;
				if (currentScale < MIN_ZOOM)
					currentScale = MIN_ZOOM;
			}
		}
	}

	protected void Scroll(MouseWheelEvent e) {
		int i = e.getWheelRotation();
		if (i > 0) {
			currentScale *= 0.8f;
			if (currentScale > MAX_ZOOM)
				currentScale = MAX_ZOOM;
		} else {
			currentScale *= 1.2f;
			if (currentScale < MIN_ZOOM)
				currentScale = MIN_ZOOM;
		}
	}

	protected void Pressed(MouseEvent e) {
		dragStart = e.getPoint();
	}

	protected void Released(MouseEvent e) {
		Point dragEnd = e.getPoint();
		int mx = dragEnd.x - dragStart.x;
		int my = dragEnd.y - dragStart.y;

		if (isRotating) {
			yRot -= (mx / 100.0f);
			xRot += (my / 100.0f);
		} else {
			viewer = viewer.plus(new Vector3D(mx, 0f, 0f));
			viewer = viewer.plus(new Vector3D(0f, my, 0f));
		}
	}

	protected void switchMoveRotate() {
		isRotating = !isRotating;
	}

	protected void Default() {
		xRot = 0f;
		yRot = 0f;
		viewer = new Vector3D(0f, 0f, 0f);
		currentScale = 1.0f;
	}

	@Override
	protected BufferedImage render() {
		Color[][] zbuffer = new Color[CANVAS_WIDTH][CANVAS_HEIGHT];
		float[][] zdepth = new float[CANVAS_WIDTH][CANVAS_HEIGHT];
		// initialize all light grey
		for (int i = 0; i < zbuffer.length; i++) {
			for (int j = 0; j < zbuffer[i].length; j++) {
				zbuffer[i][j] = new Color(200, 200, 200);
				zdepth[i][j] = Float.POSITIVE_INFINITY;
			}
		}
		if (this.scene == null)
			return null;
		Dimension dimension = getDrawingSize();

		if (centralisedScene == null)
			centralisedScene = Pipeline.autoScaleAndTranslate(scene, scene.getBound(), dimension);
		//make sure the model will always be in the center
		Scene rotatedScene = Pipeline.rotateScene(centralisedScene, xRot, yRot);
		xRot = 0f; yRot = 0f;
		Scene scaledScene = Pipeline.scaleScene(rotatedScene, currentScale, currentScale, currentScale);
		currentScale = 1.0f;
		Scene reCenteredScene = Pipeline.autoTranslate(scaledScene, scaledScene.getBound(), dimension);
		Scene translatedScene = Pipeline.translateScene(reCenteredScene, viewer.x, viewer.y, viewer.z);

		// update colors in zbuffer
		Color lightColor = getAddedLight();
		Color ambientColor = getAmbientLight();
		Vector3D lightVector = translatedScene.getLight();
		List<Polygon> polygons = translatedScene.getPolygons();
		for (Polygon p : polygons) {
			if (Pipeline.isHidden(p)) {
				p.isHadden = true;
				continue;
			}
			Color polyColor = Pipeline.getShading(p, lightVector, lightColor, ambientColor);
			EdgeList edgeList = Pipeline.computeEdgeList(p);
			Pipeline.computeZBuffer(zbuffer, zdepth, edgeList, polyColor);
		}

		return convertBitmapToImage(zbuffer);
	}

	/**
	 * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
	 * indexed by column then row and has imageHeight rows and imageWidth columns.
	 * Note that image.setRGB requires x (col) and y (row) are given in that order.
	 */
	private BufferedImage convertBitmapToImage(Color[][] bitmap) {
		BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				image.setRGB(x, y, bitmap[x][y].getRGB());
			}
		}
		return image;
	}

	public static void main(String[] args) {
		new Renderer();
	}
}

// code for comp261 assignments
