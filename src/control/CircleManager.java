package control;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import model.Circle;

public class CircleManager {

	private int rows, cols;
	private Circle circles[][];
	private List<Circle> selected = new LinkedList<Circle>();
	private CircleFactory factory;

	public CircleManager(int rows, int columns) {
		this.rows = rows;
		this.cols = columns;
		circles = new Circle[rows][columns];
		factory = CircleFactory.instance();

		for (int row = 0; row < rows; row ++)
			for (int col = 0; col < cols; col++)
				circles[row][col] = factory.newCircle(row, col);
	}

	public void select(int row, int col) {
		if(inValidRowAndCol(row, col))
			return;
		
		Circle c = circles[row][col];
		if(!hasSelection()) {
			c.select();
			selected.add(c);
		} else {
			Circle last = lastSelectedCircle();
			if (!c.isSelected() && areNeighbors(c, last) 
					&& c.getFill() == last.getFill()) {
				c.select();
				selected.add(c);
			}
		}
	}
	
	public void flush() {
		/* 
		 * Only one selected circle is not enough to be flushed, so unselect it
		 * and return.
		 */
		if (selected.size() == 1) {
			selected.get(0).unselect();
			selected.clear();
			return;
		}

		/* Clear those circles that have been selected */
		for (Circle b: selected) {
			int col = b.getColumn();
			int row = b.getRow();
			circles[row][col] = null;
		}

		/* Dropping circles */
		for (int col = 0; col < cols; col++) {
			/* Collect every non-null circle from the current column */
			List<Circle> column = new ArrayList<Circle>(rows);
			for (int j = rows - 1; j >= 0; j--) {
				if (circles[j][col] != null)
					column.add(circles[j][col]);
			}
			
			/* And drop them */
			int currentRow = rows - 1;
			for (int j = 0; j < column.size(); j++) {
				circles[currentRow][col] = column.get(j);
				circles[currentRow][col].setRow(currentRow); 
				currentRow--;
			}
			
			/* Create new circles to go above the ones that were dropped */
			while (currentRow >= 0) {
				circles[currentRow][col] = factory.newCircle(currentRow, col);
				currentRow--;
			}
		}
		selected.clear();
	}

	public boolean hasSelection() { return !selected.isEmpty(); }
	
	public Circle getCircle(int row, int col) {
		if (inValidRowAndCol(row, col))
			return null;
		return circles[row][col];
	}
	
	public List<Circle> getSelected() { return selected; }
	

	public Circle lastSelectedCircle() {
		if (selected.isEmpty())
			return null;
		return selected.get(selected.size() - 1);
	}
	
	private boolean inValidRowAndCol(int row, int col) {
		return (col >= cols || row >= rows || row < 0 || col < 0);
	}
	
	private boolean areNeighbors(Circle a, Circle b) {
		int myCol = a.getColumn();
		int myRow = a.getRow();
		int bCol = b.getColumn();
		int bRow = b.getRow();

		return xor(myCol == bCol, myRow == bRow) &&
				xor(Math.abs(myCol - bCol) == 1, Math.abs(myRow - bRow) == 1);
	}
	
	private boolean xor(boolean a, boolean b) {
		return (a && !b) || (!a && b);
	}

}