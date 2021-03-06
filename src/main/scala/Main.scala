import slick.jdbc.PostgresProfile.profile.api._
import zio.{Tag => _, _}

import java.time.LocalDate
import slick.jdbc.GetResult

object Connection {
  val db = Database.forConfig("postgres")
}

object Model {
  case class Movie(id: Long, name: String, releaseDate: LocalDate, lengthInMin: Int)
  case class Actor(id: Long, name: String)
  case class MovieActorMapping(movieId: Long, actorId: Long)
}

object Tables {

  import Model._

  class MovieTable(tag: Tag) extends Table[Movie](tag, Some("movies"), "Movie") {
    def id          = column[Long]("movie_id", O.PrimaryKey)
    def name        = column[String]("name")
    def releaseDate = column[LocalDate]("release_date")
    def lengthInMin = column[Int]("length_in_min")

    def * = (id, name, releaseDate, lengthInMin) <> (Movie.tupled, Movie.unapply)
  }
  class ActorTable(tag: Tag) extends Table[Actor](tag, Some("movies"), "Actor") {
    def id   = column[Long]("actor_id", O.PrimaryKey)
    def name = column[String]("name")

    def * = (id, name) <> (Actor.tupled, Actor.unapply)
  }
  case class MovieActorMappingTable(tag: Tag)
      extends Table[MovieActorMapping](tag, Some("movies"), "MovieActorMapping") {
    def movieId = column[Long]("movie_id")
    def actorId = column[Long]("actor_id")

    def * = (movieId, actorId) <> (MovieActorMapping.tupled, MovieActorMapping.unapply)
  }

  lazy val movieTable             = TableQuery[MovieTable]
  lazy val actorTable             = TableQuery[ActorTable]
  lazy val movieActorMappingTable = TableQuery[MovieActorMappingTable]
}

object Repo {

  import Model._
  import Tables._

  def removeAll =
    ZIO.fromFuture { implicit ec =>
      Connection.db.run(
        DBIO.seq(
          Tables.movieActorMappingTable.delete,
          Tables.movieTable.delete,
          Tables.actorTable.delete
        )
      )
    }

  def insertMovies(movies: Movie*) =
    ZIO.fromFuture(implicit ec => Connection.db.run(Tables.movieTable ++= movies))

  def insertMovie(movie: Movie) =
    ZIO.fromFuture(implicit ec => Connection.db.run(Tables.movieTable += movie))

  def insertActors(actors: Actor*) =
    ZIO.fromFuture(implicit ec => Connection.db.run(Tables.actorTable ++= actors))

  def insertActor(actor: Actor) =
    ZIO.fromFuture(implicit ec => Connection.db.run(Tables.actorTable += actor))

  def insertMovieAndActors(movie: Movie, actors: Actor*) =
    ZIO.fromFuture { implicit ec =>
      Connection.db.run(
        DBIO.seq(
          Tables.movieTable += movie,
          Tables.actorTable ++= actors,
          Tables.movieActorMappingTable ++= actors.map(actor =>
            MovieActorMapping(movie.id, actor.id)
          )
        )
      )
    }

  def getAllMovies =
    ZIO.fromFuture(implicit ec => Connection.db.run(Tables.movieTable.result))

  def getAllActors =
    ZIO.fromFuture(implicit ec => Connection.db.run(Tables.actorTable.result))

  def getMovieById(id: Long) =
    ZIO.fromFuture(implicit ec => Connection.db.run(Tables.movieTable.filter(_.id === id).result))

  def getActorById(id: Long) =
    ZIO.fromFuture(implicit ec => Connection.db.run(Tables.actorTable.filter(_.id === id).result))

  def findMovieByTitle(title: String) =
    ZIO.fromFuture(implicit ec =>
      Connection.db.run(Tables.movieTable.filter(_.name.like(s"%$title%")).result)
    )

  def findMoviesByPlainQuery = {
    implicit val getResultMovie: GetResult[Movie] =
      GetResult(r => Movie(r.<<, r.<<, LocalDate.parse(r.nextString()), r.<<))

    val query = sql"""SELECT * FROM movies."Movie"""".as[Movie]

    ZIO.fromFuture(implicit ec => Connection.db.run(query))
  }

  def updateLength(id: Long, length: Int) =
    ZIO.fromFuture(implicit ec =>
      Connection.db.run(
        Tables.movieTable
          .filter(_.id === id)
          .map(_.lengthInMin)
          .update(length)
      )
    )

  def findActorsByMovieId(movieId: Long) =
    ZIO.fromFuture { implicit ec =>
      Connection.db.run(
        Tables.movieActorMappingTable
          .filter(_.movieId === movieId)
          .join(Tables.actorTable)
          .on(_.actorId === _.id)
          .map(_._2)
          .result
      )
    }

}

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

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    for {
      _ <- removeAll

      _ <- insertMovieAndActors(shawshank, morganFreeman, timRobbins)
      _ <- insertMovieAndActors(matrix, liamNeeson)
      _ <- insertMovieAndActors(inception, leoDiCaprio)

      movies <- getAllMovies
      _      <- Console.printLine(s"Movies:\n${movies.mkString("\n - ", "\n - ", "")}")

      actors <- getAllActors
      _      <- Console.printLine(s"Actors:${actors.mkString("\n - ", "\n - ", "")}")

      // movie   <- findMovieByTitle("Matrix")
      // _       <- Console.printLine(s"Found by title:\n${movie.headOption}")
      // _       <- updateLength(movies.head.id, movies.head.lengthInMin * 2)
      // updated <- getMovieById(movies.head.id)
      // _       <- Console.printLine(s"Found updated:\n${updated.headOption}")
      // movies <- findMoviesByPlainQuery
      // _      <- Console.printLine(s"Found by plain query:${movies.mkString("\n - ", "\n - ", "")}")

      movie = movies.head

      _ <- Console.printLine(s"Looking for actors who played in the $movie...")

      actors <- findActorsByMovieId(movie.id)
      _      <- Console.printLine(s"Found ${actors.mkString(", ")}")
    } yield ()

}
