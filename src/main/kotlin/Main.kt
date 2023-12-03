import com.google.gson.Gson
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.abs

data class Resource(val type: String, val amount: Int)
data class Tile(val road: Int, val resource: Resource?)
data class CityCell(val x: Int, val y: Int, val cooldown: Int)
data class City(val cityCells: List<CityCell>)
data class Data(val map: List<List<Tile>>, val cities: Map<String, City>)

fun main() {
    val file = File("data.json")
    val data = Gson().fromJson(file.readText(), Data::class.java)

    println("Map size: ${data.map.size}x${data.map[0].size}")

    val attraction = mutableMapOf<Pair<Int, Int>, Float>()

    val fuelSavedCoef = 5f
    val woodCoeff = 30f
    val coalCoeff = 10f
    val uraniumCoeff = 10f

    data.map.forEachIndexed { y, row ->
        row.forEachIndexed { x, _ ->
            val coordinates = Pair(x, y)
            var attractionValue = 0f

            val fuelSaved = data.cities.values.count {
                // adjacent tiles
                it.cityCells.any { cityCell ->
                    cityCell.x == x - 1 && cityCell.y == y
                } || it.cityCells.any { cityCell ->
                    cityCell.x == x + 1 && cityCell.y == y
                } || it.cityCells.any { cityCell ->
                    cityCell.x == x && cityCell.y == y - 1
                } || it.cityCells.any { cityCell ->
                    cityCell.x == x && cityCell.y == y + 1
                }
            } * 5f / 20f * fuelSavedCoef

            var resourceAttraction = 0f
            var totalCount = 0
            var presenceWood = 0f
            var presenceCoal = 0f
            var presenceUranium = 0f

            data.map.forEachIndexed { rowY, row ->
                row.forEachIndexed { tileX, tile ->
                    if (tileX == x && rowY == y) {
                        return@forEachIndexed
                    }

                    if (tile.resource != null) {
                        totalCount++

                        val distance = abs(x - tileX) + abs(y - rowY)

                        when (tile.resource.type) {
                            "wood" -> presenceWood += 1f / distance
                            "coal" -> presenceCoal += 1f / distance
                            "uranium" -> presenceUranium += 1f / distance
                        }
                    }
                }
            }

            //resourceAttraction += presenceWood / totalCount * woodCoeff
            //resourceAttraction += presenceCoal / totalCount * coalCoeff
            resourceAttraction += presenceUranium / totalCount * uraniumCoeff

            attractionValue += /*fuelSaved +*/ resourceAttraction

            attraction[coordinates] = attractionValue
        }
    }

    println(attraction)

    // Write to image
    val imageFile = File("image.jpg")

    val imageWidth = data.map.size * 11
    val imageHeight = data.map[0].size * 11

    val image = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)
    val graphics = image.createGraphics()

    graphics.color = Color.BLACK
    graphics.fillRect(0, 0, imageWidth, imageHeight)

    val maxAttraction = attraction.values.maxOrNull() ?: 0f
    val minAttraction = attraction.values.minOrNull() ?: 0f

    println("Max attraction: $maxAttraction")
    println("Min attraction: $minAttraction")

    attraction.forEach { (coordinates, value) ->
        val (x, y) = coordinates

        var g = 0f
        /*val r = if (data.map[x][y].resource != null || data.cities.values.any { city -> city.cityCells.any { cityCell -> cityCell.x == x && cityCell.y == y } }) {
            1f
        } else {
            g = (value - minAttraction) / (maxAttraction - minAttraction)

            0f
        }*/ val r = 0f
        g = (value - minAttraction) / (maxAttraction - minAttraction)

        println("x: $x, y: $y, value: $value, g: $g")

        graphics.color = Color(r, g, 0f)
        graphics.fillRect(x * 11, y * 11, 10, 10)
    }

    graphics.dispose()

    imageFile.outputStream().use {
        javax.imageio.ImageIO.write(image, "jpg", it)
    }
}