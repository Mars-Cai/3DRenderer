package renderer;

/**
 * EdgeList should store the data for the edge list of a single polygon in your
 * scene. A few method stubs have been provided so that it can be tested, but
 * you'll need to fill in all the details.
 *
 * You'll probably want to add some setters as well as getters or, for example,
 * an addRow(y, xLeft, xRight, zLeft, zRight) method.
 */
public class EdgeList {

	private int startY, endY;
	private float[] leftX, rightX, leftZ, rightZ;
	public int dy;

	public EdgeList(int startY, int endY) {
		this.startY = startY;
		this.endY = endY;
		dy = endY - startY +1;
		leftX = new float[dy];
		rightX = new float[dy];
		leftZ = new float[dy];
		rightZ = new float[dy];

		for (int i = 0; i < dy; i++) {
			leftX[i] = Float.POSITIVE_INFINITY;
			rightX[i] = Float.NEGATIVE_INFINITY;
			leftZ[i] = Float.POSITIVE_INFINITY;
			rightZ[i] = Float.POSITIVE_INFINITY;
		}
	}

	public int getStartY() {
		return this.startY;
	}

	public int getEndY() {
		return this.endY;
	}

	public float getLeftX(int y) {
		return this.leftX[y];
	}

	public float getRightX(int y) {
		return this.rightX[y];
	}

	public float getLeftZ(int y) {
		return this.leftZ[y];
	}

	public float getRightZ(int y) {
		return this.rightZ[y];
	}

	public void addRow(int y, float x, float z) {
		if (x <= this.leftX[y]) {
			this.leftX[y] = x;
			this.leftZ[y] = z;
		}

		if (x >= this.rightX[y]) {
			this.rightX[y] = x;
			this.rightZ[y] = z;
		}
	}
}

// code for comp261 assignments
