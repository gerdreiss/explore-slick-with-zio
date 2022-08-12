import play.api.libs.json.Json
import zio._

import java.time.LocalDate

object Main extends ZIOAppDefault {

  import Model._
  import Repo._

  private val shawshank = Movie(1, "The Shawshank Redemption", LocalDate.parse("1994-10-14"), 142)
  private val matrix    = Movie(2, "The Matrix", LocalDate.parse("1999-03-31"), 136)
  private val inception = Movie(3, "Inception", LocalDate.parse("2010-07-16"), 148)
  private val phantomMenace   = Movie(4, "The Phantom Menace", LocalDate.parse("1999-05-19"), 136)

  private val morganFreeman = Actor(1, "Morgan Freeman")
  private val timRobbins    = Actor(2, "Tim Robbins")
  private val liamNeeson    = Actor(3, "Liam Neeson")
  private val leoDiCaprio   = Actor(4, "Leonardo DiCaprio")
  private val keanuReeves   = Actor(5, "Keanu Reeves")

  private val movieActorMappings = List(
    MovieActorMapping(1, shawshank.id, morganFreeman.id),
    MovieActorMapping(2, shawshank.id, timRobbins.id),
    MovieActorMapping(3, matrix.id, keanuReeves.id),
    MovieActorMapping(4, inception.id, leoDiCaprio.id),
    MovieActorMapping(5, phantomMenace.id, liamNeeson.id)
  )

  private val providers = List(
    MovieStreamingProviderMapping(1, shawshank.id, StreamingService.Netflix),
    MovieStreamingProviderMapping(2, matrix.id, StreamingService.AmazonPrime),
    MovieStreamingProviderMapping(3, inception.id, StreamingService.DisneyPlus),
    MovieStreamingProviderMapping(4, phantomMenace.id, StreamingService.Hulu),
    MovieStreamingProviderMapping(5, phantomMenace.id, StreamingService.HBO)
  )

  private val starwarsLocations = MovieLocations(1, phantomMenace.id, List("Englang", "Tunisia", "Italy"))
  private val starwarsProperties =
    MovieProperties(1, phantomMenace.id, Map("Genre" -> "Sci-Fi", "Features" -> "Light sabers"))

  private val liamNeesonDetails =
    ActorDetails(1, liamNeeson.id, Json.parse("""{"born": 1952, "height": 193}"""))

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    for {
      _ <- removeAll()

      _ <- insertMovies(shawshank, matrix, inception, phantomMenace)
      _ <- insertActors(morganFreeman, timRobbins, liamNeeson, leoDiCaprio, keanuReeves)
      _ <- insertMovieActorMappings(movieActorMappings: _*)
      _ <- insertStreamProviders(providers: _*)
      _ <- insertMovieLocations(starwarsLocations)
      _ <- insertMovieProperties(starwarsProperties)
      _ <- insertActorDetails(liamNeesonDetails)

      movies <- getAllMovies
      _      <- Console.printLine(s"Movies:${movies.mkString("\n - ", "\n - ", "")}")

      actors <- getAllActors
      _      <- Console.printLine(s"Actors:${actors.mkString("\n - ", "\n - ", "")}")

      movies <- findMovieByTitle("Matrix")
      _      <- Console.printLine(s"Found by title:${movies.mkString("\n - ", "\n - ", "")}")

      _       <- updateLength(movies.head.id, movies.head.lengthInMin * 2)
      updated <- findMovieByTitle("Matrix")
      _       <- Console.printLine(s"Found updated:${updated.mkString("\n - ", "\n - ", "")}")

      _      <- Console.printLine(s"Looking for actors by plain query...")
      movies <- findMoviesByPlainQuery
      _      <- Console.printLine(s"Found by plain query:${movies.mkString("\n - ", "\n - ", "")}")

      _      <- Console.printLine(s"Looking for actors who played in the ${movies.head}...")
      actors <- findActorsByMovie(movies.head)
      _      <- Console.printLine(s"Found ${actors.mkString("\n - ", "\n - ", "")}")

      _         <- Console.printLine(s"Looking for service providers for ${movies.head}...")
      providers <- findStreamingProvidersByMovie(movies.head)
      _         <- Console.printLine(s"Found ${providers.map(_.streamingService).mkString("\n - ", "\n - ", "")}")

      _         <- Console.printLine(s"Looking for locations for ${movies.head}...")
      locations <- findLocationsByMovie(phantomMenace)
      _         <- Console.printLine(s"Found ${locations.flatMap(_.locations).mkString("\n - ", "\n - ", "")}")

      _       <- Console.printLine(s"Looking for details for $liamNeeson...")
      details <- findActorDetails(liamNeeson)
      _       <- Console.printLine(s"Found ${details.head.details}")

      _ <- Console.printLine(s"Printing the schema:")
      _ <- Console.printLine(ORM.ddl.createIfNotExistsStatements.mkString(";\n"))
    } yield ()

}
