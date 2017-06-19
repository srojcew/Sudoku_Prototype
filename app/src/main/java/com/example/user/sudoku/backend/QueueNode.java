package com.example.user.sudoku.backend;

import java.util.Vector;

/*
 * Stephen Rojcewicz
 *
 * Node used by SudokuGraph
 * Maintains a count of the numbers assigned among the node's neighbors
 *
 */

public class QueueNode implements Comparable {
    private int assignment;
    private int[] colorCounts;
    private Vector<QueueNode> neighbors;
    private int nodeNumber;
    private int priority;

    public QueueNode(int num, int assign) {
        nodeNumber = num;
        assignment = assign;
        colorCounts = new int[9];
        for (int i = 0; i < 9; i++) {
            colorCounts[i] = 0;
        }
        neighbors = new Vector<QueueNode>();
        priority = 0;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public int getAssignment() {
        return assignment;
    }
    public int getPriority() {
        return priority;
    }

    public void addNeighbor(QueueNode neighbor) {
        neighbors.add(neighbor);
    }

    public boolean hasValidAssignment() {
        if (assignment == TypeConstants.BLANK) {
            return true;
        }
        return (colorCounts[assignment] == 0);
    }

    public void assignValue(int color) {
        for (int i = 0; i < neighbors.size(); i++) {
            neighbors.elementAt(i).notifyAssignment(color, assignment);
        }
        assignment = color;
    }

    /**
     * Update the appropriate numbers count, as a neighbor's assignment has changed
     * @param color
     * @param oldColor
     */
    private void notifyAssignment(int color, int oldColor) {
        if (color == oldColor) {
            System.err.println("tried to replace a node's color with the same color it already has " + color);
            System.exit(1);
        }
        if (color == TypeConstants.BLANK) {
            if (oldColor != TypeConstants.BLANK) {
                colorCounts[oldColor]--;
                if (colorCounts[oldColor] == 0) {
                    priority--;
                }
            }
        } else {
            if (colorCounts[color] == 0) {
                priority++;
            }
            colorCounts[color]++;
            if (oldColor != TypeConstants.BLANK) {
                colorCounts[oldColor]--;
                if (colorCounts[oldColor] == 0) {
                    priority--;
                }
            }
        }
    }

    public Vector<Integer> findCandidates() {
        Vector<Integer> candidates = new Vector<Integer>();
        if (assignment != TypeConstants.BLANK && this.hasValidAssignment()) {
            candidates.add(assignment);
        }
        else {
            for (int i = 0; i < 9; i++) {
                if (colorCounts[i] == 0) {
                    candidates.add(i);
                }
            }
        }
        return candidates;

    }

    public int compareTo(Object node) {
        if (this.priority < ((QueueNode) node).getPriority()) {
            return 1;
        }
        if (this.priority > ((QueueNode) node).getPriority()) {
            return -1;
        }
        else {
            return 0;
        }
    }
}
