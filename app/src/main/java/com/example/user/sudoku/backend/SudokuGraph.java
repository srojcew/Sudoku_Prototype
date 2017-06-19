package com.example.user.sudoku.backend;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

/*
 * Stephen Rojcewicz
 *
 * Graph representation of a Sudoku board
 *
 */

public class SudokuGraph {
    private Vector<QueueNode> blankNodes;
    /**
     * Queue of nodes to be removed. Does not necessarily contain all of the assigned nodes in this SudokuGraph.
     */
    private Vector<QueueNode> assignedNodes;
    /**
     * used to break ties when dequeuing nodes
     */
    private Random rand;

    /**
     * Stores a reference to every node. Array index is node number
     */
    private QueueNode[] allNodes;


    public SudokuGraph(Vector<Vector<Integer>> candidates) {
        initSudokuGraph();

        // initialize assignments; nodes will notify one another
        for (int i = 0; i < 81; i++) {
            if (candidates.elementAt(i).size() == 1) {
                allNodes[i].assignValue(candidates.elementAt(i).elementAt(0));
            }
        }
        // add the blank nodes to blankNodes Vector and the assigned nodes to the assignedNodes Vector
        initQueues();
    }

    public SudokuGraph(int[] board) {
        initSudokuGraph();

        // initialize assignments; nodes will notify one another
        for (int i = 0; i < 81; i++) {
            if (board[i] != TypeConstants.BLANK) {
                allNodes[i].assignValue(board[i]);
            }
        }
        // add the blank nodes to blankNodes Vector and the assigned nodes to the assignedNodes Vector
        initQueues();
    }

    private void initSudokuGraph() {
        allNodes = new QueueNode[81];
        blankNodes = new Vector<QueueNode>();
        assignedNodes = new Vector<QueueNode>();
        rand = new Random(new Date().getTime());
        for (int i = 0; i < 81; i++) {
            allNodes[i] = new QueueNode(i, TypeConstants.BLANK);
        }

        // associate neighbors for each node
        for (int i = 0; i < 81; i++) {
            int row = i / 9;
            int col = i % 9;
            // add nodes that are in the same row as node i
            for (int j = row * 9; j < (row * 9) + 9; j++) {
                if (j != i) {
                    allNodes[i].addNeighbor(allNodes[j]);
                }
            }
            // add nodes that are in the same column as node i
            for (int j = col; j < col + 73; j += 9) {
                if (j != i) {
                    allNodes[i].addNeighbor(allNodes[j]);
                }
            }
            // add nodes that are in the same region as node i
            for (int c = (col / 3) * 3; c < (col / 3) * 3 + 3; c++) {
                for (int r = (row / 3) * 3; r < (row / 3) * 3 + 3; r++) {
                    if (r != row && c != col && (9 * r + c) != i) {
                        allNodes[i].addNeighbor(allNodes[9 * r + c]);
                    }
                }
            }
        }
    }
    private void initQueues() {
        for (int i = 0; i < 81; i++) {
            if (allNodes[i].getAssignment() == TypeConstants.BLANK) {
                blankNodes.add(allNodes[i]);
            }
            else {
                assignedNodes.add(allNodes[i]);
            }
        }
    }

    public int dequeueBlankNode() {
        int bestPriority = 0;
        Vector<Integer> bestNodeLocs = new Vector<Integer>();
        for (int i = 0; i < blankNodes.size(); i++) {
            if (blankNodes.elementAt(i).getPriority() == bestPriority) {
                bestNodeLocs.add(i);
            }
            else if (blankNodes.elementAt(i).getPriority() > bestPriority) {
                bestPriority = blankNodes.elementAt(i).getPriority();
                bestNodeLocs.clear();
                bestNodeLocs.add(i);
            }
        }
        int bestLoc = bestNodeLocs.elementAt(rand.nextInt(bestNodeLocs.size()));
        int bestNodeNum = blankNodes.elementAt(bestLoc).getNodeNumber();
        blankNodes.removeElementAt(bestLoc);
        return bestNodeNum;
    }

    public int dequeueBlankNodeRandom() {
        int index = rand.nextInt(blankNodes.size());
        int node = blankNodes.elementAt(index).getNodeNumber();
        blankNodes.removeElementAt(index);
        return node;
    }



    public void enqueueBlankNode(int nodeNum) {
        blankNodes.add(allNodes[nodeNum]);
    }

    /**
     * NOTE: Dequeuing an assigned node removes the node from the queue of assigned nodes but does not
     * automatically remove the node's assignment. Thus, it is possible for a SudokuGraph to have assigned
     * nodes at the same time that the assigned nodes queue is empty. This behavior is desirable for the
     * removeNodes() function in Backend.java because it prevents removeNodes() from attempting to remove
     * a node that it has already attempted to remove
     */
    public int dequeueAssignedNode() {
        int bestPriority = 0;
        Vector<Integer> bestNodeLocs = new Vector<Integer>();
        for (int i = 0; i < assignedNodes.size(); i++) {
            if (assignedNodes.elementAt(i).getPriority() == bestPriority) {
                bestNodeLocs.add(i);
            }
            else if (assignedNodes.elementAt(i).getPriority() > bestPriority) {
                bestPriority = assignedNodes.elementAt(i).getPriority();
                bestNodeLocs.clear();
                bestNodeLocs.add(i);
            }
        }
        int bestLoc = bestNodeLocs.elementAt(rand.nextInt(bestNodeLocs.size()));
        int bestNodeNum = assignedNodes.elementAt(bestLoc).getNodeNumber();
        assignedNodes.removeElementAt(bestLoc);
        return bestNodeNum;
    }

    public boolean hasBlankNodes() {
        return (!blankNodes.isEmpty());
    }

    public boolean hasAssignedNodes() {
        return (!assignedNodes.isEmpty());
    }

    public void putValueAt(int nodeNum, int color) {
        if (color == TypeConstants.BLANK && (allNodes[nodeNum].getAssignment() != TypeConstants.BLANK)) {
            blankNodes.add(allNodes[nodeNum]);
        }
        else if (color != TypeConstants.BLANK && (allNodes[nodeNum].getAssignment() == TypeConstants.BLANK)) {
            blankNodes.remove(allNodes[nodeNum]);
        }
        allNodes[nodeNum].assignValue(color);
    }

    public int getValueAt(int nodeNum) {
        return allNodes[nodeNum].getAssignment();
    }

    public boolean hasValidConfiguration() {
        for (int i = 0; i < 81; i++) {
            if (!allNodes[i].hasValidAssignment()) {
                return false;
            }
        }
        return true;
    }

    public boolean isValidAt(int node) {
        return allNodes[node].hasValidAssignment();
    }

    public int[] valuesArray() {
        int[] boardArray = new int[81];
        for (int i = 0; i < 81; i++) {
            boardArray[i] = allNodes[i].getAssignment();
        }
        return boardArray;
    }

    public Vector<Vector<Integer>> committedNumberes() {
        Vector<Vector<Integer>> committedNums = new Vector<Vector<Integer>>();
        for (int i = 0; i < 81; i++) {
            committedNums.add(new Vector<Integer>());
            int assignment = allNodes[i].getAssignment();
            if (assignment != TypeConstants.BLANK) {
                committedNums.elementAt(i).add(allNodes[i].getAssignment());
            }
        }
        return committedNums;
    }

    public Vector<Vector<Integer>> findAllCandidates() {
        Vector<Vector<Integer>> candidates = new Vector<Vector<Integer>>();
        for (int i = 0; i < 81; i++) {
            candidates.add(allNodes[i].findCandidates());
        }
        return candidates;
    }


    /**
     *
     * @param oldCandidates assumes that the candidate lists contain the solution number
     * @return
     */
    public Vector<Vector<Integer>> updateAllCandidates(Vector<Vector<Integer>> oldCandidates) {
        Vector<Vector<Integer>> updatedCands = new Vector<Vector<Integer>>();
        for (int i = 0; i < 81; i++) {
            Vector<Integer> calcCands = allNodes[i].findCandidates();
            Vector<Integer> oldCands = oldCandidates.elementAt(i);
            updatedCands.add(new Vector<Integer>());
            for (int n = 0; n < oldCands.size(); n++) {
                if (calcCands.contains(oldCands.elementAt(n))) {
                    updatedCands.elementAt(i).add(oldCands.elementAt(n));
                }
            }
        }
        return updatedCands;
    }

    public Vector<Vector<Integer>> updateAllCandidatesBlanks(Vector<Vector<Integer>> oldCandidates) {
        Vector<Vector<Integer>> updatedCands = new Vector<Vector<Integer>>();
        for (int i = 0; i < 81; i++) {
            Vector<Integer> calcCands = allNodes[i].findCandidates();
            Vector<Integer> oldCands = oldCandidates.elementAt(i);
            if (oldCands.isEmpty()) {
                updatedCands.add(calcCands);
            }
            else {
                updatedCands.add(new Vector<Integer>());
                for (int n = 0; n < oldCands.size(); n++) {
                    if (calcCands.contains(oldCands.elementAt(n))) {
                        updatedCands.elementAt(i).add(oldCands.elementAt(n));
                    }
                }
            }
        }
        return updatedCands;
    }
    public Vector<Integer> findCandidatesAt(int loc) {
        return allNodes[loc].findCandidates();

    }

    // for testing purposes
    public String toString() {
        String str = "";
        int node = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                str += allNodes[node].getAssignment() + "\t";
                node++;
            }
            str += "\n";
        }
        return str + "\n----------------------------------------------------------------------------";
    }
}
