import java.util.Random;

public class SatogaeriCell{
    private static Random random = new Random();

    // location:
    public int row;
    public int col;
    public int region;

    /** If the cell is a circle cell, this boolean becomes true and 
     * the circleNum indicates how many spaces it will be moved: */
    public boolean circle = false;
    public int circleNum;
    // location circle is to be moved to
    public int cRow;
    public int cCol;


    /**
     * constructor
     */
    public SatogaeriCell(int r, int c, int region){
        this.row = r; this.col = c; this.region = region;
    }

    /**
     * Checks neighboring cells for one assigned to a region, then creates a new cell of the same region for the 
     * specified coordinates
     * @param board         a 2d array representing the game board with existing cells
     * @param row           row of cell to be created
     * @param col           col of cell to be created
     * @return              a new cell - or null if no neighbors were found
     */
    public static SatogaeriCell findMatchNeighbor(SatogaeriCell[][] board, int row, int col){
        int[] indexes = {-1, 1};

        // check vertical and horizontal neighbors, making a new cell of the same region of a neighboring region cell is found:
        for (int i : indexes){
            if ((col + i) >= 0 && (col + i) < SatogaeriGen.cols){
                if (board[row][col + i] != null) { return new SatogaeriCell(row, col, board[row][col + i].region); } }
            if ((row + i) >= 0 && (row + i) < SatogaeriGen.rows){ 
                if (board[row + i][col] != null) { return new SatogaeriCell(row, col, board[row + i][col].region); } }
        }
        return null;
    }


    /**
     * Creates a new cell neighboring the current one
     * 
     * @param r             maximum acceptable row value
     * @param c             maximum acceptable column value
     * @param regionBoard   board of SatogaeriCells to verify a cell region is not already assigned
     * @return
     */
    public SatogaeriCell createNeighborCell(int r, int c, SatogaeriCell[][] regionBoard){
        int nRow;
        int nCol;
        int itr = 0; 
        int case_int = 0;

        while (itr < 10){

            nRow = this.row + (random.nextBoolean() ? 1 : 0);       // generate coordinates +- 1 from this cell
            nCol = this.col + (random.nextBoolean() ? 1 : 0);

            // avoid out of bounds indexes:
            if (nRow < 0 || nCol < 0 || nRow >= r || nCol >= c || (nRow == this.row && nCol == this.col)) { 
                itr += 1;
                continue;
            }

            // if the cell is empty, make a new cell of the same region as this:
            case_int = random.nextBoolean() ? 1 : 0;                        // https://stackoverflow.com/questions/3793650/convert-boolean-to-int-in-java
            switch (case_int) {
                case 0:
                    if (regionBoard[this.row][nCol] == null){ return new SatogaeriCell(this.row, nCol, this.region); }
                    break;
                case 1:
                    if (regionBoard[nRow][this.col] == null){ return new SatogaeriCell(nRow, this.col, this.region); }
                    break;
                default:
                    System.out.println("wuh oh, case:  " + case_int);
            }
            itr += 1;
        }
        return null;
    }


    public boolean validateMovement(char[][] board){
        // pick a random direction to try and traverse first:
        int directoion = SatogaeriGen.randNum(3);
        int itr = 0;

        // loop all four directions til one is a legal move
        while(itr < 4){
            switch (directoion){
                case 0:                     // move up
                    if (this.isLegalMove(board, (this.row - this.circleNum), this.col )){
                        this.updateBoard(board, (this.row - this.circleNum), this.col );
                        return true;
                    }
                    break;
                case 1:                     // move right
                    if (this.isLegalMove(board, this.row, (this.col + this.circleNum))){
                        this.updateBoard(board, this.row, (this.col + this.circleNum));
                        return true;
                    }
                    break;
                case 2:                     // move down
                    if (this.isLegalMove(board, (this.row + this.circleNum), this.col )){
                        this.updateBoard(board, (this.row + this.circleNum), this.col );
                        return true;
                    }
                    break;
                case 3:                     // move left
                    if (this.isLegalMove(board, this.row, (this.col - this.circleNum))){
                        this.updateBoard(board, this.row, (this.col - this.circleNum));
                        return true;
                    }
                    break;
            }
            itr += 1;
            directoion += 1;
            if (directoion == 4) { directoion = 0;}
        }
        // if none of the directions are a legal move
        // TODO decrease circleNum by one and retry?
        return false;
    }


    private boolean isLegalMove(char[][] board, int row, int col){
        int r = board.length; int c = board[0].length;
        int max; int min;

        if (row < 0 || col < 0 || row >= r || col >= c) { return false; }
        // the move is horizontal:
        if (this.row == row){
            if (this.col > col) { max = this.col; min = col; }
            else { max = col; min = this.col; }
            // verify nothing blocks this movement:
            for (int i = min; i < max; i++ ){
                if (board[this.row][i] != '.') { return false; }
            }
        }
        // the move is vertical:
        else {
            if (this.row > row){ max = this.row; min = row; }
            else { max = row; min = this.row; }
            // verify nothing blocks this movement:
            for (int i = min; i < max; i++ ){
                if (board[i][this.col] != '.') { return false; }
            }
        }
        return true;

    }


    private void updateBoard(char[][] board, int row, int col){
        int max; int min;
        this.cRow = row; this.cCol = col;

        // the move is horizontal:       
        if (this.row == row){
            if (this.col > col) { max = this.col; min = col; }
            else { max = col; min = this.col; }
            for (int i = min; i <= max; i++ ){
                board[this.row][i] = '-';
            }
        }
        // the move is vertical:
        else {
            if (this.row > row){ max = this.row; min = row; }
            else { max = row; min = this.row; }
            for (int i = min; i <= max; i++ ){
                board[i][this.col] = '|';
            }
        }
        board[this.row][this.col] = 'o';
        board[row][col] = Character.forDigit(this.circleNum, 10);
    }


    /**
     * Designates the cell as a circle cell and assigns its movement value
     * @param circleNum
     */
    public void makeCircle(int circleNum){
        this.circle = true;
        this.circleNum = circleNum;
    }
}