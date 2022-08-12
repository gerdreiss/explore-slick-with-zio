import slick.jdbc.PostgresProfile.profile.api._
import zio.{Tag => _, _}

import java.time.LocalDate
import slick.jdbc.GetResult
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import java.util.UUID

object Main extends ZIOAppDefault {

  import Model._
  import Repo._

  val shawshank = Movie(1, "The Shawshank Redemption", LocalDate.parse("1994-10-14"), 142)
  val matrix    = Movie(2, "The Matrix", LocalDate.parse("1999-03-31"), 136)
  val inception = Movie(3, "Inception", LocalDate.parse("2010-07-16"), 148)

  val morganFreeman = Actor(1, "Morgan Freeman")
  val timRobbins    = Actor(2, "Tim Robbins")
  val liamNeeson    = Actor(3, "Liam Neeson")
  val leoDiCaprio   = Actor(4, "Leonardo DiCaprio")

  val providers = List(
    MovieStreamingProviderMapping(1, 1, StreamingService.Netflix),
    MovieStreamingProviderMapping(2, 2, StreamingService.AmazonPrime),
    MovieStreamingProviderMapping(3, 3, StreamingService.DisneyPlus),
    MovieStreamingProviderMapping(4, 3, StreamingService.Hulu)
  )

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    for {
      _ <- removeAll

      _ <- insertMovieAndActors(shawshank, morganFreeman, timRobbins)
      _ <- insertMovieAndActors(matrix, liamNeeson)
      _ <- insertMovieAndActors(inception, leoDiCaprio)
      _ <- insertStreamProviders(providers: _*)

      movies <- getAllMovies
      _      <- Console.printLine(s"Movies:\n${movies.mkString("\n - ", "\n - ", "")}")

      actors <- getAllActors
      _      <- Console.printLine(s"Actors:${actors.mkString("\n - ", "\n - ", "")}")

      movie   <- findMovieByTitle("Matrix")
      _       <- Console.printLine(s"Found by title:\n - ${movie.headOption.getOrElse("")}")
      _       <- updateLength(movies.head.id, movies.head.lengthInMin * 2)
      updated <- getMovieById(movies.head.id)
      _       <- Console.printLine(s"Found updated:\n - ${updated.headOption.getOrElse("")}")
      movies  <- findMoviesByPlainQuery
      _       <- Console.printLine(s"Found by plain query:${movies.mkString("\n - ", "\n - ", "")}")

      movie = movies.last

      _      <- Console.printLine(s"Looking for actors who played in the $movie...")
      actors <- findActorsByMovie(movie)
      _      <- Console.printLine(s"Found ${actors.mkString("\n - ", "\n - ", "")}")

      _         <- Console.printLine(s"Looking for service providers for $movie...")
      providers <- findStreamingProvidersByMovie(movie)
      _         <- Console.printLine(s"Found ${providers.mkString("\n - ", "\n - ", "")}")

      _ <- Console.printLine(s"Printing the schema:")
      _ <- Console.printLine(ORM.ddl.createIfNotExistsStatements.mkString(";\n"))
    } yield ()

}
