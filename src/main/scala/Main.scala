import slick.jdbc.PostgresProfile.profile.api._
import zio.Console
import zio.Scope
import zio.URIO
import zio.ZIO
import zio.ZIOAppArgs
import zio.ZIOAppDefault

import java.time.LocalDate

object Connection {
  val db = Database.forConfig("postgres")
}

object Model {
  case class Movie(id: Long, name: String, releaseDate: LocalDate, lengthInMin: Int)
}

object Tables {
  class MovieTable(tag: Tag) extends Table[Model.Movie](tag, Some("movies"), "Movie") {
    def id          = column[Long]("movie_id", O.PrimaryKey, O.AutoInc)
    def name        = column[String]("name")
    def releaseDate = column[LocalDate]("release_date")
    def lengthInMin = column[Int]("length_in_min")
    def * = (id, name, releaseDate, lengthInMin) <> (Model.Movie.tupled, Model.Movie.unapply)
  }

  lazy val movieTable = TableQuery[MovieTable]
}

object Main extends ZIOAppDefault {

  val shawshank = Model.Movie(1, "The Shawshank Redemption", LocalDate.parse("1994-10-14"), 142)
  val matrix    = Model.Movie(2, "The Matrix", LocalDate.parse("1999-03-31"), 136)
  val inception = Model.Movie(3, "Inception", LocalDate.parse("2010-07-16"), 148)

  def removeAllMovies =
    ZIO.fromFuture(implicit ec => Connection.db.run(Tables.movieTable.delete))

  def insertMovie(movie: Model.Movie) =
    ZIO.fromFuture(implicit ec => Connection.db.run(Tables.movieTable += movie))

  def getAllMovies =
    ZIO.fromFuture(implicit ec => Connection.db.run(Tables.movieTable.result))

  def getMovieById(id: Long) =
    ZIO.fromFuture(implicit ec => Connection.db.run(Tables.movieTable.filter(_.id === id).result))

  def findMovieByTitle(title: String) =
    ZIO.fromFuture(implicit ec =>
      Connection.db.run(Tables.movieTable.filter(_.name.like(s"%$title%")).result)
    )

  def updateLength(id: Long, length: Int) =
    ZIO.fromFuture(implicit ec =>
      Connection.db.run(Tables.movieTable.filter(_.id === id).map(_.lengthInMin).update(length))
    )

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    for {
      _       <- removeAllMovies
      _       <- insertMovie(shawshank)
      _       <- insertMovie(matrix)
      _       <- insertMovie(inception)
      movies  <- getAllMovies
      _       <- Console.printLine(s"Movies:\n${movies.mkString("\n")}")
      movie   <- findMovieByTitle("Matrix")
      _       <- Console.printLine(s"Found by title:\n${movie.headOption}")
      _       <- updateLength(movies.head.id, movies.head.lengthInMin * 2)
      updated <- getMovieById(movies.head.id)
      _       <- Console.printLine(s"Found updated:\n${updated.headOption}")
    } yield ()

}
