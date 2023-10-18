package tentsandtrees.backtracker;

import java.io.*;
import java.util.*;

/**
 *  The full representation of a configuration in the TentsAndTrees puzzle.
 *  It can read an initial configuration from a file, and supports the
 *  Configuration methods necessary for the Backtracker solver.
 *
 *  @author Jack Robbins
 */
public class TentConfig implements Configuration {
    // INPUT CONSTANTS
    /** An empty cell */
    public final static char EMPTY = '.';
    /** A cell occupied with grass */
    public final static char GRASS = '-';
    /** A cell occupied with a tent */
    public final static char TENT = '^';
    /** A cell occupied with a tree */
    public final static char TREE = '%';

    private int[] rowLook;
    private int[] colLook;
    private char[][] field;
    private int DIM;
    private int row;
    private int column;
    // OUTPUT CONSTANTS
    /** A horizontal divider */
    public final static char HORI_DIVIDE = '-';
    /** A vertical divider */
    public final static char VERT_DIVIDE = '|';
     ;

    /**
     * Construct the initial configuration from an input file whose contents
     * are, for example:<br>
     * <tt><br>
     * 3        # square dimension of field<br>
     * 2 0 1    # row looking values, top to bottom<br>
     * 2 0 1    # column looking values, left to right<br>
     * . % .    # row 1, .=empty, %=tree<br>
     * % . .    # row 2<br>
     * . % .    # row 3<br>
     * </tt><br>
     * @param filename the name of the file to read from
     * @throws IOException if the file is not found or there are errors reading
     */
    public TentConfig(String filename) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        DIM = Integer.parseInt(in.readLine());
        this.rowLook= new int[DIM];
        this.colLook = new int[DIM];
        this.field = new char[DIM][DIM];
        this.row = 0;
        this.column = -1;


        String[] rowFields = in.readLine().split("\\s+");
        for (int row=0; row < DIM; row++){
            this.rowLook[row] = Integer.parseInt(rowFields[row]);
        }

        String[] colFields = in.readLine().split("\\s+");
        for (int col=0; col < DIM; col++){
            this.colLook[col] = Integer.parseInt(colFields[col]);
        }

        for (int row = 0; row < DIM; row++){
            String[] fields = in.readLine().split("\\s+");
            for (int col = 0; col < DIM; col++){
                this.field[row][col] = fields[col].charAt(0);
            }
        }

        in.close();
    }

    /**
     * Makes a full copy of the other TentConfig, then advances the
     * cursor and places a tent or grass, based on input.
     * @param other the config to be copied
     * @param tent a boolean value that when true, will have the
     *             constructor place a tent on the cursor. When
     *             false, grass will be placed instead, given
     *             a tree is not already at that spot
     */
    private TentConfig(TentConfig other, boolean tent) {
        this.row = other.row;
        this.column = other.column;
        this.rowLook = other.rowLook;
        this.colLook = other.colLook;
        this.DIM = other.DIM;

        this.column += 1;
        if (this.column == this.DIM){
            this.row += 1;
            this.column = 0;
        }

        this.field = new char[DIM][DIM];
        for (int row = 0; row < this.DIM; ++row){
            System.arraycopy(other.field[row], 0, this.field[row], 0, this.DIM);
        }

        //Taking care not to overwrite trees, checks if the cursor is on a tree first
        if (this.field[this.row][this.column] != TREE){
            if (tent){
                this.field[this.row][this.column] = TENT;
            } else {
                this.field[this.row][this.column] = GRASS;
            }
        }
    }

    /**
     * Makes two copies using the copy constructor, one with grass at the cursor,
     * and one with a tent there, given the cursor is not on a tree. Then, adds them
     * both to a linked list and returns it.
     * @return returns a linked list with two successors
     */
    @Override
    public Collection<Configuration> getSuccessors() {
        List<Configuration> successors = new LinkedList<>();

        TentConfig grassSuccessor = new TentConfig(this, false);
        TentConfig tentSuccessor = new TentConfig(this, true);

        successors.add(tentSuccessor);
        successors.add(grassSuccessor);

        return successors;
    }

    /**
     * runs multiple tests on the config, checking for validity and pruning out configs
     * that may have too many trees, not enough trees, tents without trees, etc.
     * @return returns true if the config is valid, false if it isn't
     */
    @Override
    public boolean isValid() {
        char cursorVal = this.field[this.row][this.column];

        //Do all checks for tent placement
        if (cursorVal == TENT){
            //Check for adjacent tents
            if (!noAdjacentTents()){
                return false;
            }
            //Check for diagonal tents
            if (!noDiagonalTents()){
                return false;
            }
            //check to see if theres a tree
            if (!nextToTree()){
                return false;
            }
            //if at any point, the number of tents in a row exceeds its rowlook value, the config is  automatically invalid
            int tentCounter3 = 0;
            for (int c = 0; c<=this.DIM-1; c++){
                if (this.field[this.row][c] == TENT){
                    tentCounter3 += 1;
                }
            }
            if (tentCounter3 > this.rowLook[row]){
                return false;
            }
            //if at any point, the number of tents in a column exceeds its colLook value, the config is automatically invalid
            int tentCounter4 = 0;
            for (int r =0; r <= DIM-1; r++){
                if (this.field[r][this.column] == TENT){
                    tentCounter4 += 1;
                }
            }
            if (tentCounter4 > this.colLook[column]){
                return false;
            }
        }

        //When the end of a row is reached, check to see if the number of tents is right
        if (this.column == this.DIM -1){
            int tentCounter1 = 0;
            for (int c = 0; c <= this.DIM-1; c++){
                if (this.field[this.row][c] == TENT){
                    tentCounter1 += 1;
                }
            }
            if (tentCounter1 != this.rowLook[row]){
                return false;
            }
        }

        //When the end of a column is reached, check to see if the number of tents is right
        if (this.row == this.DIM -1) {
            int tentCounter2 = 0;
            for (int r = 0; r <= this.DIM-1; r++){
                if (this.field[r][this.column] == TENT){
                    tentCounter2 += 1;
                }
            }
            if (tentCounter2 != this.colLook[column]){
                return false;
            }
        }

        //When the very end of the board is reached, ensure every tree has a tent
        if (this.row == this.DIM -1 && this.column == this.DIM -1){
            for (int row = 0; row < this.DIM; row++){
                for (int col = 0; col < this.DIM; col++){
                    if (this.field[row][col] == TREE){
                        if (!hasTent(row, col)){
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Checks above, below, left and right of the cursor for other tents,
     * which would break the rules.
     * @return true if there are no adjacent tents, false if there are
     * adjacent tents
     */
    private boolean noAdjacentTents(){
        int r = this.row;
        int c = this.column;

        //check to the left
        if (c-1 >= 0){
            if (this.field[r][c-1] == TENT){
                return false;
            }
        }

        //check to the right
        if (c+1 <= this.DIM-1){
            if (this.field[r][c+1] == TENT){
                return false;
            }
        }

        //check below
        if (r+1 <= this.DIM-1){
            if (this.field[r+1][c] == TENT){
                return false;
            }
        }

        //check above
        if (r-1 >= 0){
            if (this.field[r-1][c] == TENT){
                return false;
            }
        }

        return true;
    }

    /**
     * checks all diagonals from the cursor to check if there is a tent
     * on those diagonals, which would violate the rules.
     * @return returns true only if there are no tents diagonal to the
     * tent on the cursor
     */
    private boolean noDiagonalTents(){
        int r = this.row;
        int c = this.column;

        //bottom right diagonal
        if (r+1 <= this.DIM-1 && c+1 <= this.DIM-1) {
            if (this.field[r+1][c+1] == TENT){
                return false;
            }
        }

        //bottom left diagonal
        if (r+1 <= this.DIM-1 && c-1 >= 0){
            if (this.field[r+1][c-1] == TENT){
                return false;
            }
        }

        //top right diagonal
        if (r-1 >= 0 && c+1 <= this.DIM-1){
            if (this.field[r-1][c+1] == TENT){
                return false;
            }
        }

        //top left diagonal
        if (r-1 >= 0 && c-1 >= 0){
            if (this.field[r-1][c-1] == TENT){
                return false;
            }
        }

        return true;
    }

    /**
     * checks to see if the tent has a neighboring tree to the right, left
     * top or bottom of it.
     * @return returns true if there is a tree in a bordering cell
     */
    private boolean nextToTree(){
        int r = this.row;
        int c = this.column;

        //Check to the left
        if (c-1 >= 0){
            if (this.field[r][c-1] == TREE){
                return true;
            }
        }

        //Check to the right
        if (c+1 <= this.DIM-1){
            if (this.field[r][c+1] == TREE){
                return true;
            }
        }

        //Check below
        if (r+1 <= this.DIM-1){
            if (this.field[r+1][c] == TREE) {
                return true;
            }
        }

        //Check above
        if (r-1 >= 0) {
            if (this.field[r-1][c] == TREE){
                return true;
            }
        }

        return false;
    }

    /**
     * Checks left, right, above and below the tree to see if the
     * tree has a tent next to it.
     * @param r the row position of the tree
     * @param c the column position of the tree
     * @return true if there is at least one adjacent tent
     */
    private boolean hasTent(int r, int c){
        //Check left
        if (c-1 >= 0){
            if (this.field[r][c-1] == TENT){
                return true;
            }
        }

        //Check right
        if (c+1 <= this.DIM-1){
            if (this.field[r][c+1] == TENT){
                return true;
            }
        }

        //Check below
        if (r+1 <= this.DIM-1){
            if (this.field[r+1][c] == TENT){
                return true;
            }
        }

        //Check above
        if (r-1 >= 0){
            if (this.field[r-1][c] == TENT){
                return true;
            }
        }

        return false;
    }

    /**
     * The solution is a goal when the cursor has successfully reached the last
     * square on the board, noted by the last row and the last column
     * @return true if cursor is on end of board, false if it
     * is not
     */
    @Override
    public boolean isGoal() {
        return (this.row == this.DIM -1) && (this.column == this.DIM-1);
    }

    /**
     * Goes through the char array and adds the string representation
     * to the gridString along with the proper formatting.
     * @return returns the string representation of the TentConfig
     */
    @Override
    public String toString() {
        String gridString = "";
        gridString += " ";
        int i = 0;
        while (i < colLook.length*2-1){
            gridString += HORI_DIVIDE;
            i++;
        }

        gridString += " ";
        gridString += "\n";

        int j = 0;
        for (char[] rowChar: this.field){
            gridString += VERT_DIVIDE;
            for (char rowVal: rowChar){
                String rowString = Character.toString(rowVal);
                gridString = gridString  + rowString + " ";
            }
            gridString += VERT_DIVIDE;
            gridString += rowLook[j];
            gridString += "\n";
            j ++;
        }

        gridString += " ";

        int k = 0;
        while (k < colLook.length*2-1){
            gridString += HORI_DIVIDE;
            k++;
        }

        gridString += "\n";
        gridString += " ";

        for (int colVal: colLook){
            gridString = gridString + colVal + " ";
        }
        return gridString;
    }
}
