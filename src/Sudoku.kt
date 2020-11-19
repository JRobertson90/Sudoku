import java.io.File
import java.io.PrintWriter
import java.util.*

class Sudoku {

    companion object {
        const val MAX_ITERATIONS = 1000
        const val PUZZLE_FILE = "puzzle_4.txt"

        @JvmStatic
        fun main(args: Array<String>) {
            Sudoku().solve()
        }
    }

    private val rows: Array<Array<Cell>>  = Array(9) { Array(9) { Cell() } }
    private val cols: Array<Array<Cell>>  = Array(9) { Array(9) { Cell() } }
    private val squares: Array<Array<Cell>> = Array(9) { Array(9) { Cell() } }
    private val cells = ArrayList<Cell>(9*9)

    fun solve() {
        init()
        readFile()
        var counter = 0
        while(!isPuzzleSolved() && counter < MAX_ITERATIONS) {
            removeSingles()
            removeHiddenSingles()
            counter++
        }
        writeToFile()
    }

    private fun init() {
        (0..8).forEach { row ->
            (0..8).forEach { col ->
                val cell = Cell(row, col)
                rows[row][col] = cell
                cols[col][row] = cell
                cells.add(cell)
            }
        }
        initSquares()
    }

    private fun initSquares() {
        var square = 0
        (0..2).forEach { rowStart ->
            (0..2).forEach { colStart ->
                initSquare(square, rowStart * 3, colStart * 3)
                square++
            }
        }
    }

    private fun initSquare(square: Int, rowStart: Int, colStart: Int) {
        var element = 0
        (0..2).forEach { rowOffset ->
            (0..2).forEach { colOffset ->
                val row = rowOffset + rowStart
                val col = colOffset + colStart
                val cell = rows[row][col]
                squares[square][element] = cell
                cell.square = square
                element++
            }
        }
    }

    private fun readFile() {
        val file = File("./puzzles/$PUZZLE_FILE")
        val scanner = Scanner(file)
        var lineCount = 0
        while(scanner.hasNextLine()) {
            val line = scanner.nextLine()
            var cell = lineCount * 9
            line.forEach { c ->
                if (c != ' ') {
                    cells[cell].setSolvedNumber(Character.getNumericValue(c))
                }
                cell++
            }
            lineCount++
        }
    }

    private fun isPuzzleSolved() = cells.all { it.isSolved() }

    private fun removeSingles() {
        for(row in rows) {
            removeSingles(row)
        }
        for(col in cols) {
            removeSingles(col)
        }
        for(square in squares) {
            removeSingles(square)
        }
    }

    private fun removeSingles(group: Array<Cell>) {
        val solvedNumbers = group.mapNotNull { cell -> cell.getSolvedNumber() }
        group.filter{ !it.isSolved() }.forEach { cell -> cell.removeAll(solvedNumbers) }
    }

    private fun removeHiddenSingles() {
        for(row in rows) {
            removeHiddenSingles(row)
        }
        for(col in cols) {
            removeHiddenSingles(col)
        }
        for(square in squares) {
            removeHiddenSingles(square)
        }
    }

    private fun removeHiddenSingles(group: Array<Cell>) {
        val counters = arrayOf(0,0,0,0,0,0,0,0,0)
        for(item in group) {
            for (candidate in item.candidates) {
                counters[candidate - 1]++
            }
        }
        val hiddenSingles = arrayListOf<Int>()
        counters.forEachIndexed { i, counter ->
            if (counter == 1) {
                hiddenSingles.add(i + 1)
            }
        }
        for (single in hiddenSingles) {
            for(item in group) {
                if (item.candidates.contains(single)) {
                    item.setSolvedNumber(single)
                }
            }
        }
    }

    private fun writeToFile() {
        PrintWriter(File("./solutions/$PUZZLE_FILE")).use { pw ->
            rows.forEach { row ->
                pw.println("+---+---+---+---+---+---+---+---+---+")
                row.forEach {
                    pw.print("| ")
                    pw.print(it.getSolvedNumber() ?: " ")
                    pw.print(" ")
                }
                pw.print("|")
                pw.println()
            }
            pw.println("+---+---+---+---+---+---+---+---+---+")
        }
    }

    inner class Cell() {
        var candidates = arrayListOf(1,2,3,4,5,6,7,8,9)
        var row: Int = -1
        var col: Int = -1
        var square: Int = -1

        constructor(row: Int, col: Int) {
            this.row = row
            this.col = col
        }

        fun isSolved(): Boolean = candidates.size == 1

        fun getSolvedNumber(): Int? = if(isSolved()) candidates.first() else null

        fun setSolvedNumber(num: Int) {
            candidates = arrayListOf(num)
        }

        fun removeAll(num: List<Int>) {
            candidates.removeAll(num)
        }

        fun getRow(): Array<Cell> {
            return rows[row]
        }

        fun getCol(): Array<Cell> {
            return cols[col]
        }

        fun getSquare(): Array<Cell> {
            return squares[square]
        }
    }
}