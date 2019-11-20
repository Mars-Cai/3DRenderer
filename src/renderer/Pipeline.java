package renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import renderer.Scene.Polygon;

/**
 * The Pipeline class has method stubs for all the major components of the
 * rendering pipeline, for you to fill in.
 *
 * Some of these methods can get quite long, in which case you should strongly
 * consider moving them out into their own file. You'll need to update the
 * imports in the test suite if you do.
 */
public class Pipeline {

	/**
	 * Returns true if the given polygon is facing away from the camera (and so
	 * should be hidden), and false otherwise.
	 */
	public static boolean isHidden(Polygon poly) {
		return getNormal(poly).z > 0;
	}

	public static Vector3D getNormal(Polygon poly) {
		Vector3D edge1 = poly.getVertices()[1].minus(poly.getVertices()[0]);
		Vector3D edge2 = poly.getVertices()[2].minus(poly.getVertices()[1]);
		return edge1.crossProduct(edge2);
	}

	private static Scene compute(Scene scene, Transform matrix) {
		List<Polygon> newPolygons = new ArrayList<Polygon>();
		for (Polygon p : scene.getPolygons()) {
			Vector3D[] vectors = new Vector3D[3];
			for (int i = 0; i < vectors.length; i++)
				vectors[i] = matrix.multiply(p.vertices[i]);
			newPolygons.add(new Polygon(vectors[0], vectors[1], vectors[2], p.reflectance));
		}
		return new Scene(newPolygons, matrix.multiply(scene.getLight()));
	}

	/**
	 * Computes the colour of a polygon on the screen, once the lights, their angles
	 * relative to the polygon's face, and the reflectance of the polygon have been
	 * accounted for.
	 *
	 * @param lightDirection
	 *            The Vector3D pointing to the directional light read in from the
	 *            file.
	 * @param lightColor
	 *            The color of that directional light.
	 * @param ambientLight
	 *            The ambient light in the scene, i.e. light that doesn't depend on
	 *            the direction.
	 */
	public static Color getShading(Polygon poly, Vector3D lightDirection, Color lightColor, Color ambientLight) {
		int r, g, b;
		Vector3D normal = getNormal(poly);
		double cos = normal.cosTheta(lightDirection);
		if (cos > 0) {
			r = (int) (poly.reflectance.getRed() / 255.0f * (ambientLight.getRed() + lightColor.getRed() * cos));
			g = (int) (poly.reflectance.getGreen() / 255.0f * (ambientLight.getGreen() + lightColor.getGreen() * cos));
			b = (int) (poly.reflectance.getBlue() / 255.0f * (ambientLight.getBlue() + lightColor.getBlue() * cos));
		} else {
			r = (int) (poly.reflectance.getRed() / 255.0f * ambientLight.getRed());
			g = (int) (poly.reflectance.getGreen() / 255.0f * ambientLight.getGreen());
			b = (int) (poly.reflectance.getBlue() / 255.0f * ambientLight.getBlue());
		}
		r = r > 255 ? 255 : r;
		g = g > 255 ? 255 : g;
		b = b > 255 ? 255 : b;
		return new Color(r, g, b);
	}

	/**
	 * This method should rotate the polygons and light such that the viewer is
	 * looking down the Z-axis. The idea is that it returns an entirely new Scene
	 * object, filled with new Polygons, that have been rotated.
	 *
	 * @param scene
	 *            The original Scene.
	 * @param xRot
	 *            An angle describing the viewer's rotation in the YZ-plane (i.e
	 *            around the X-axis).
	 * @param yRot
	 *            An angle describing the viewer's rotation in the XZ-plane (i.e
	 *            around the Y-axis).
	 * @return A new Scene where all the polygons and the light source have been
	 *         rotated accordingly.
	 */
	public static Scene rotateScene(Scene scene, float xRot, float yRot) {
		Vector3D newLightPos =  Transform.newXRotation(xRot).multiply(scene.getLight());
		newLightPos =  Transform.newYRotation(yRot).multiply(newLightPos);
		return new Scene(compute(scene, Transform.newXRotation(xRot).compose(Transform.newYRotation(yRot))).getPolygons(),newLightPos);
	}

	/**
	 * This should translate the scene by the appropriate amount.
	 *
	 * @param scene
	 * @return
	 */
	public static Scene translateScene(Scene scene, float x, float y, float z) {
		return compute(scene, Transform.newTranslation(x, y, z));
	}

	/**
	 * This should scale the scene.
	 *
	 * @param scene
	 * @return
	 */
	public static Scene scaleScene(Scene scene, float x, float y, float z) {
		return compute(scene, Transform.newScale(x, y, z));
	}


	public static Scene autoScaleAndTranslate(Scene scene, float[] boundary, Dimension dimension) {

	    float left = boundary[0];
	    float right = boundary[1];
	    float up = boundary[2];
	    float down = boundary[3];
	    float close = boundary[4];
        float far = boundary[5];

	    float objectWidth = right - left;
	    float objectHeight = down - up;
	    float objectdepth = far - close;
	    int canvasWidth = dimension.width;
	    int canvasHeight = dimension.height;

	    //scale
        float ratioHorizontal = canvasWidth / 2 / objectWidth;
        float ratioVertical = canvasHeight / 2 / objectHeight;
        float ratioDepth = Math.min(canvasWidth, canvasHeight) / 2 / objectdepth;

        float scale = Math.min(Math.min(ratioHorizontal, ratioVertical), ratioDepth);
        Transform scaleMatrix = Transform.newScale(scale, scale, scale);
        Scene scaledScene = compute(scene, scaleMatrix);

        //translate
        float scaledLeft = left * scale;
        float scaledUp = up * scale;

        // work out how much to shift horizontally
        float scaledObjectWidth = objectWidth * scale;
        float centralPosX = (canvasWidth - scaledObjectWidth) / 2;
        float horizontalShift = centralPosX - scaledLeft;

        // work out how much to shift vertically
        float scaledObjectHeight = objectHeight * scale;
        float centralPosY = (canvasHeight - scaledObjectHeight) / 2;
        float verticalShift = centralPosY - scaledUp;

        return compute(scaledScene, Transform.newTranslation(horizontalShift, verticalShift, 0f));
    }

	public static Scene autoTranslate(Scene scene, float[] boundary, Dimension dimension) {

        float left = boundary[0];
        float right = boundary[1];
        float up = boundary[2];
        float down = boundary[3];

        float objectWidth = right - left;
        float objectHeight = down - up;
        int canvasWidth = dimension.width;
        int canvasHeight = dimension.height;

        // work out how much to shift horizontally
        float centralPosX = (canvasWidth - objectWidth) / 2;
        float horizontalShift = centralPosX - left;

        // work out how much to shift vertically
        float centralPosY = (canvasHeight - objectHeight) / 2;
        float verticalShift = centralPosY - up;

        return compute(scene, Transform.newTranslation(horizontalShift, verticalShift, 0f));
    }


	/**
	 * Computes the edgelist of a single provided polygon, as per the lecture
	 * slides.
	 */
	public static EdgeList computeEdgeList(Polygon poly) {
		int minY = (int) Math.min(Math.min(poly.vertices[0].y, poly.vertices[1].y), poly.vertices[2].y);
		int maxY = (int) Math.max(Math.max(poly.vertices[0].y, poly.vertices[1].y), poly.vertices[2].y);
		EdgeList edgeList = new EdgeList(minY, maxY);
		for (int i = 0; i < 3; i++) {
			Vector3D up, down;
			int j = i + 1;
			j = j == 3 ? 0 : j;
			if (poly.vertices[i].y == poly.vertices[j].y)
				continue;
			else {
				up = poly.vertices[i].y > poly.vertices[j].y ? poly.vertices[j] : poly.vertices[i];
				down = poly.vertices[i].y < poly.vertices[j].y ? poly.vertices[j] : poly.vertices[i];
			}
			float x = up.x;
			float z = up.z;
			float mX = (down.x - x) / (down.y - up.y);
			float mZ = (down.z - z) / (down.y - up.y);
			for (int y = (int) up.y; y < (int) down.y; y++, x += mX, z += mZ)
				edgeList.addRow(y - minY, x, z);
		}
		return edgeList;
	}

	/**
	 * Fills a zbuffer with the contents of a single edge list according to the
	 * lecture slides.
	 *
	 * The idea here is to make zbuffer and zdepth arrays in your main loop, and
	 * pass them into the method to be modified.
	 *
	 * @param zbuffer
	 *            A double array of colours representing the Color at each pixel so
	 *            far.
	 * @param zdepth
	 *            A double array of floats storing the z-value of each pixel that
	 *            has been coloured in so far.
	 * @param polyEdgeList
	 *            The edgelist of the polygon to add into the zbuffer.
	 * @param polyColor
	 *            The colour of the polygon to add into the zbuffer.
	 */
	public static void computeZBuffer(Color[][] zbuffer, float[][] zdepth, EdgeList polyEdgeList, Color polyColor) {
		int startY = polyEdgeList.getStartY();
		int endY = polyEdgeList.getEndY();
		int dy = endY - startY;
		for (int y = 0; y < dy; y++) {
			if (y + startY < 0 || y + startY >= zbuffer[0].length)
				continue;
			float z = polyEdgeList.getLeftZ(y);
			float mZ = (polyEdgeList.getRightZ(y) - z) / (polyEdgeList.getRightX(y) - polyEdgeList.getLeftX(y));
			for (int x = (int) polyEdgeList.getLeftX(y); x < (int) polyEdgeList.getRightX(y); x++,z += mZ) {
				if (x < 0 || x >= zbuffer.length)
					continue;
				if (z < zdepth[x][y + startY]) {
					zdepth[x][y + startY] = z;
					zbuffer[x][y + startY] = polyColor;
				}
			}
		}
	}
}

// code for comp261 assignments
