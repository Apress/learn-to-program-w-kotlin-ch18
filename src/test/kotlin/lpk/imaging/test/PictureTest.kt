package lpk.imaging.test

import org.junit.Assert
import org.junit.Test
import lpk.imaging.Picture
import lpk.imaging.loadPictureFromFile
import java.awt.Color
import java.nio.file.Paths

val IMAGES = "src/test/resources/images/"

fun load(name: String): Picture {
    val file = Paths.get(IMAGES + name).toFile()
    val loaded = loadPictureFromFile(file)
    return loaded
}

class PictureTest {
    @Test
    fun loadPictureFromFileTest() {
        val file = Paths.get(IMAGES + "green_h50_w100.png").toFile()
        val loaded = loadPictureFromFile(file)
        Assert.assertEquals(loaded.height(), 50)
        Assert.assertEquals(loaded.width(), 100)
        val green = Color(0, 255, 0)
        for (row in 0..49) {
            for (column in 0..99) {
                Assert.assertEquals(loaded.pixelByRowColumn(row, column), green)
            }
        }
    }

    @Test
    fun loadYellowPicture() {
        val file = Paths.get(IMAGES + "yellow_h80_w30.png").toFile()
        val loaded = loadPictureFromFile(file)
        Assert.assertEquals(loaded.height(), 80)
        Assert.assertEquals(loaded.width(), 30)
        val yellow = Color(255, 255, 0)
        for (row in 0..79) {
            for (column in 0..29) {
                Assert.assertEquals(loaded.pixelByRowColumn(row, column), yellow)
            }
        }
    }

    @Test
    fun chopIntoSquaresTest() {
        val original = load("red_blue_green.png")
        val blocks = original.chopIntoSquares(10)
        Assert.assertEquals(10, blocks.size)//10 rows
        Assert.assertEquals(10, blocks[0].size)//10 columns
        val red = Color(255, 0, 0)
        val blue = Color(0, 0, 255)
        for (row in 0..4) {
            for (column in 0..4) {
                checkSingleColorPicture(blocks[row][column], red, 10, 10)
            }
            for (column in 5..9) {
                checkSingleColorPicture(blocks[row][column], blue, 10, 10)
            }
        }
        for (row in 5..9) {
            for (column in 0..4) {
                checkSingleColorPicture(blocks[row][column], blue, 10, 10)
            }
            for (column in 5..9) {
                checkSingleColorPicture(blocks[row][column], red, 10, 10)
            }
        }
    }

    @Test
    fun averageColorTest() {
        val red10 = load("red10.png")
        Assert.assertEquals(Color(255, 0, 0), red10.averageColor())

        val green10 = load("green10.png")
        Assert.assertEquals(Color(0, 255, 0), green10.averageColor())

        val blue10 = load("blue10.png")
        Assert.assertEquals(Color(0, 0, 255), blue10.averageColor())

        val redblue = load("red_blue_tiles_50.png")
        Assert.assertEquals(Color(127, 0, 127), redblue.averageColor())
    }

    @Test
    fun scaleDownTest() {
        val image1 = load("red_blue_green.png")
        val scaled1 = image1.scaleDown(10)
        val expected1 = load("red_blue_tiles_5.png")
        checkPicture(scaled1, expected1)

        val image2 = load("green_black_large.png")
        val scaled2 = image2.scaleDown(3)
        val expected2 = load("green_black_small.png")
        checkPicture(scaled2, expected2)
    }

    @Test
    fun saveToTest() {
        val picture = load("green_black_small.png")
        val temp = Paths.get("temp.png").toFile()
        picture.saveTo(temp)
        val reloaded = loadPictureFromFile(temp)
        Assert.assertEquals(20, reloaded.height())
        Assert.assertEquals(40, reloaded.width())
        val green = Color(0, 255, 0)
        val black = Color(0, 0, 0)
        for (row in 0..9) {
            for (column in 0..39) {
                val pixel = reloaded.pixelByRowColumn(row, column)
                Assert.assertEquals(green, pixel)
            }
        }
        for (row in 10..19) {
            for (column in 0..39) {
                val pixel = reloaded.pixelByRowColumn(row, column)
                Assert.assertEquals(black, pixel)
            }
        }
    }

    @Test
    fun cropToRedSquare() {
        val tiles100 = load("red_blue_tiles_50.png")
        val cropped = tiles100.cropTo(0, 0, 50, 50)
        val expectedColor = Color(255, 0, 0)
        checkSingleColorPicture(cropped, expectedColor, 50, 50)
    }

    @Test
    fun cropToBlueRectangle() {
        val tiles100 = load("red_blue_tiles_50.png")
        val cropped = tiles100.cropTo(50, 10, 10, 20)
        val expectedColor = Color(0, 0, 255)
        checkSingleColorPicture(cropped, expectedColor, 10, 20)
    }

    @Test
    fun cropCentre() {
        val tiles100 = load("red_blue_tiles_50.png")
        val cropped = tiles100.cropTo(25, 25, 50, 50)
        val expected = load("red_blue_tiles_25.png")
        checkPicture(expected, cropped)
    }

    @Test
    fun transformTest() {
        //Start with an image in which all pixels are green.
        val file = Paths.get(IMAGES + "green_h50_w100.png").toFile()
        val loaded = loadPictureFromFile(file)
        //Create a transformation
        //that turns each pixel red.
        val red = Color(255, 0, 0)
        val toRed = { it: Color -> red }
        //Call the transform function
        //using the red transformation.
        val changed = loaded.transform(toRed)

        //For each row in the result...
        for (row in 0..49) {
            //for each pixel in the row...
            for (column in 0..99) {
                Assert.assertEquals(changed.pixelByRowColumn(row, column), red)
            }
        }
    }

    fun checkPicture(picture: Picture, expected: Picture) {
        Assert.assertEquals(picture.height(), expected.height())
        Assert.assertEquals(picture.width(), expected.width())
        for (row in 0..picture.height() - 1) {
            for (column in 0..picture.width() - 1) {
                val actualPixel = picture.pixelByRowColumn(row, column)
                val expectedPixel = expected.pixelByRowColumn(row, column)
                Assert.assertEquals(actualPixel, expectedPixel)
            }
        }
    }

    fun checkSingleColorPicture(picture: Picture, expectedColor: Color, expectedHeight: Int, expectedWidth: Int) {
        Assert.assertEquals(picture.height(), expectedHeight)
        Assert.assertEquals(picture.width(), expectedWidth)
        for (row in 0..expectedHeight - 1) {
            for (column in 0..expectedWidth - 1) {
                Assert.assertEquals(picture.pixelByRowColumn(row, column), expectedColor)
            }
        }
    }
}