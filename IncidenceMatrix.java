import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Incidence matrix implementation for the GraphInterface interface.
 *
 * Your task is to complete the implementation of this class.  You may add methods, but ensure your modified class compiles and runs.
 *
 * @author Jeffrey Chan, 2021.
 */
public class IncidenceMatrix extends AbstractGraph
{

    private boolean[][] matrix;
    private HashMap<String, Integer> edgeIndices;

	/**
	 * Construct empty graph.
	 */
    public IncidenceMatrix() {
        matrix = null;
        edgeIndices = new HashMap<String, Integer>();
    } // end of IncidenceMatrix()


    public void addVertex(String vertLabel) {
        // Check duplicate vertex
        if (getIndices().containsKey(vertLabel))
            System.err.println("Found duplicate vertex");
        else {
            // Map the new vertex to its index and SIR state
            getIndices().put(vertLabel, getIndices().size());
            getSirStates().put(vertLabel, SIRState.S);

            // Insert the row of the new vertex into the matrix
            if (matrix != null) {
                boolean[][] tempMatrix = new boolean[matrix.length + 1][matrix[0].length];
                for (int i = 0; i < matrix.length; i++) {
                    for (int j = 0; j < matrix[0].length; j++) {
                        tempMatrix[i][j] = matrix[i][j];
                    }
                }
                matrix = tempMatrix;
            }
        }
    } // end of addVertex()


    public void addEdge(String srcLabel, String tarLabel) {
        // Check duplicate edge
        if (edgeIndices.containsKey(srcLabel + " " + tarLabel)
                || edgeIndices.containsKey(tarLabel + " " + srcLabel)) {
            System.err.println("Found duplicate edge");

            return;
        }

        // Check if both vertices are present
        if (!getIndices().containsKey(srcLabel)
                || !getIndices().containsKey(tarLabel)) {
            System.err.println("At least one vertex is not present");
            return;
        }

        // Map the edge to its index
        edgeIndices.put(srcLabel + " " + tarLabel, edgeIndices.size());

        // Instantiate the matrix if this is the first edge added into the graph
        if (matrix == null) {
            matrix = new boolean[getIndices().size()][1];
        }
        else {
            // Insert the column of the new edge into the matrix
            boolean[][] tempMatrix = new boolean[matrix.length][matrix[0].length + 1];
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[0].length; j++) {
                    tempMatrix[i][j] = matrix[i][j];
                }
            }
            matrix = tempMatrix;
        }

        // Reflect presence of the edge
        int srcIndex = getIndices().get(srcLabel);
        matrix[srcIndex][matrix[0].length - 1] = true;
        int tarIndex = getIndices().get(tarLabel);
        matrix[tarIndex][matrix[0].length - 1] = true;
    } // end of addEdge()


    public void toggleVertexState(String vertLabel) {
        // Check if the vertex is present
        if (!getIndices().containsKey(vertLabel)
                || !getSirStates().containsKey(vertLabel)) {
            System.err.println("The vertex is not present in the graph");
            return;
        }

        if (getSirStates().get(vertLabel) == SIRState.S)
            getSirStates().replace(vertLabel, SIRState.I);
        else
            getSirStates().replace(vertLabel, SIRState.R);
    } // end of toggleVertexState()


    public void deleteEdge(String srcLabel, String tarLabel) {
        // Check if both vertices are present
        if (getIndices().get(srcLabel) == null
                || getIndices().get(tarLabel) == null) {
            System.err.println("At least one vertex is not present");
            return;
        }

        // Check if the edge is present
        // Remove the edge from the map if yes
        int edgeIndex = -1;
        if (edgeIndices.containsKey(srcLabel + " " + tarLabel)) {
            edgeIndex = edgeIndices.get(srcLabel + " " + tarLabel);
            edgeIndices.remove(srcLabel + " " + tarLabel);
        }
        else if (edgeIndices.containsKey(tarLabel + " " + srcLabel)) {
            edgeIndex = edgeIndices.get(tarLabel + " " + srcLabel);
            edgeIndices.remove(tarLabel + " " + srcLabel);
        }
        else {
            System.err.println("The edge is not present in the graph");
            return;
        }

        // Remove the column of the edge from the matrix
        if (matrix[0].length == 1)
            matrix = null;
        else {
            boolean[][] tempMatrix = new boolean[matrix.length][matrix[0].length - 1];
            for (int i = 0; i < tempMatrix.length; i++) {
                for (int j = 0; j < tempMatrix[0].length; j++) {
                    // Copy the columns as is until reach the column to be deleted
                    if (j < edgeIndex)
                        tempMatrix[i][j] = matrix[i][j];
                        // Skip the column to be deleted and move the following columns to the left
                    else
                        tempMatrix[i][j] = matrix[i][j + 1];
                }
            }
            matrix = tempMatrix;
        }

        // Move the indices of behind edges forward
        for (Map.Entry<String, Integer> entry : edgeIndices.entrySet()) {
            if (entry.getValue() > edgeIndex)
                entry.setValue(entry.getValue() - 1);
        }
    } // end of deleteEdge()


    public void deleteVertex(String vertLabel) {
        // Check if the vertex is present
        if (!getIndices().containsKey(vertLabel)
                || !getSirStates().containsKey(vertLabel)) {
            System.err.println("The vertex is not present in the graph");
            return;
        }

        // Remove all edges of the vertex
        for (String edgeLabel : edgeIndices.keySet()) {
            String[] srcAndTar = edgeLabel.split(" ", 2);
            if (srcAndTar[0].equals(vertLabel) || srcAndTar[1].equals(vertLabel)) {
                deleteEdge(srcAndTar[0], srcAndTar[1]);
            }
        }

        // Remove the row of the vertex from the matrix
        if (matrix.length == 1)
            matrix = null;
        else {
            boolean[][] tempMatrix = new boolean[matrix.length - 1][matrix[0].length];
            for (int j = 0; j < tempMatrix[0].length; j++) {
                for (int i = 0; i < tempMatrix.length; i++) {
                    // Copy the rows as is until reach the row to be deleted
                    if (i < getIndices().get(vertLabel))
                        tempMatrix[i][j] = matrix[i][j];
                        // Skip the row to be deleted and move the following rows upwards
                    else
                        tempMatrix[i][j] = matrix[i + 1][j];
                }
            }
            matrix = tempMatrix;
        }

        // Move the indices of behind vertices forward
        for (Map.Entry<String, Integer> entry : getIndices().entrySet()) {
            if (entry.getValue() > getIndices().get(vertLabel))
                entry.setValue(entry.getValue() - 1);
        }

        // Delete the vertex in the maps
        getIndices().remove(vertLabel);
        getSirStates().remove(vertLabel);
    } // end of deleteVertex()


    public String[] kHopNeighbours(int k, String vertLabel) {
        if (k == 0)
            return null;

        DynamicArray<String> neighbours = new DynamicArray<String>();

        // BFS
        // Define "depth" as the number of hops away from the initial vertex
        int depth = 0;
        // Store the vertices visited in the last depth
        DynamicArray<String> lastVisited = new DynamicArray<String>();
        lastVisited.add(vertLabel);
        while (depth < k) {
            // Terminate BFS when no more vertex is visited in the last depth
            if (lastVisited.getSize() == 0) {
                String[] neighbours2 = new String[neighbours.getSize()];
                return neighbours.toArray(neighbours2);
            }

            DynamicArray<String> currVisited = new DynamicArray<String>();

            // Locate the vertex visited in the last depth (source vertex)
            for (int m = 0; m < lastVisited.getSize(); m++) {
                String srcVertex = lastVisited.get(m);
                int rowIndex = getIndices().get(srcVertex);
                // Find the edge associated with the source vertex
                for (int j = 0; j < matrix[0].length; j++) {
                    if (matrix[rowIndex][j]) {
                        // Retrieve the edge from the map
                        for (Map.Entry<String, Integer> entry : edgeIndices.entrySet()) {
                            if (entry.getValue() == j) {
                                // Find the target vertex
                                String[] srcAndTar = entry.getKey().split(" ", 2);
                                String tarVert = null;
                                if (srcAndTar[0].equals(srcVertex))
                                    tarVert = srcAndTar[1];
                                else
                                    tarVert = srcAndTar[0];
                                // Check if the target vertex has already been visited
                                boolean isVisited = false;
                                if (neighbours.getSize() != 0) {
                                    for (int n = 0; n < neighbours.getSize(); n++) {
                                        if (neighbours.get(n).equals(tarVert) || vertLabel.equals(tarVert)) {
                                            isVisited = true;
                                            break;
                                        }
                                    }
                                }
                                if (!isVisited) {
                                    // Add the target vertex to the array of neighbours
                                    neighbours.add(tarVert);
                                    // Record the target vertex visited in the current depth
                                    currVisited.add(tarVert);
                                }
                                break;
                            }
                        }
                    }
                }
            }
            depth++;
            lastVisited = currVisited;
        }

        String[] neighbours2 = new String[neighbours.getSize()];
        return neighbours.toArray(neighbours2);
    } // end of kHopNeighbours()


    public void printVertices(PrintWriter os) {
        for (Map.Entry<String, SIRState> entry : getSirStates().entrySet()) {
            os.print("(" + entry.getKey() + ", " + entry.getValue() + ") ");
        }
        os.println();
    } // end of printVertices()


    public void printEdges(PrintWriter os) {
        for (Map.Entry<String, Integer> entry : edgeIndices.entrySet()) {
            String[] srcAndTar = entry.getKey().split(" ", 2);
            os.println(srcAndTar[0] + " " + srcAndTar[1]);
            os.println(srcAndTar[1] + " " + srcAndTar[0]);
        }
    } // end of printEdges()
} // end of class IncidenceMatrix
