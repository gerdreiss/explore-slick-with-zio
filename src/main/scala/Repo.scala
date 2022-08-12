import slick.jdbc.GetResult
import slick.jdbc.PostgresProfile.api._
import zio.{Tag => _, _}

import java.time.LocalDate

object Repo {

  import Model._

  object Connection {
    val db = Database.forConfig("postgres")
  }

  /*
   * delete all data from table
   */

  def removeAll(): Task[Unit] =
    ZIO.fromFuture { implicit ec =>
      Connection.db.run(
        DBIO.seq(
          ORM.movieTable.delete,
          ORM.actorTable.delete,
          ORM.movieActorMappingTable.delete,
          ORM.movieStreamingProviderMappingTable.delete,
          ORM.movieLocationsTable.delete,
          ORM.moviePropertiesTable.delete,
          ORM.actorDetailsTable.delete
        )
      )
    }

  /*
   * INSERTS
   */

  def insertMovies(movies: Movie*): Task[Option[Int]] =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.movieTable ++= movies))

  def insertActors(actors: Actor*): Task[Option[Int]] =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.actorTable ++= actors))

  def insertMovieActorMappings(mappings: MovieActorMapping*): Task[Option[Int]] =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.movieActorMappingTable ++= mappings))

  def insertStreamProviders(streamingProviders: MovieStreamingProviderMapping*): Task[Option[Int]] =
    ZIO.fromFuture(implicit ec =>
      Connection.db.run(ORM.movieStreamingProviderMappingTable ++= streamingProviders)
    )

  def insertMovieLocations(movieLocations: MovieLocations*): Task[Option[Int]] =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.movieLocationsTable ++= movieLocations))

  def insertMovieProperties(movieProperties: MovieProperties*): Task[Option[Int]] =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.moviePropertiesTable ++= movieProperties))

  def insertActorDetails(actorDetails: ActorDetails*): Task[Option[Int]] =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.actorDetailsTable ++= actorDetails))

  /*
   * UPDATES
   */

  def updateLength(id: Long, length: Int): Task[Int] =
    ZIO.fromFuture(implicit ec =>
      Connection.db.run(
        ORM.movieTable
          .filter(_.id === id)
          .map(_.lengthInMin)
          .update(length)
      )
    )

  /*
   * QUERIES
   */

  def getAllMovies: Task[Seq[Movie]] =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.movieTable.result))

  def getAllActors: Task[Seq[Actor]] =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.actorTable.result))

  def findMovieByTitle(title: String): Task[Seq[Movie]] =
    ZIO.fromFuture(implicit ec =>
      Connection.db.run(ORM.movieTable.filter(_.name.like(s"%$title%")).result)
    )

  def findMoviesByPlainQuery: Task[Vector[Movie]] = {
    implicit val GRM: GetResult[Movie] =
      GetResult(r => Movie(r.<<, r.<<, LocalDate.parse(r.nextString()), r.<<))

    ZIO.fromFuture(implicit ec =>
      Connection.db.run(
        sql"""SELECT * FROM movies."Movie"""".as[Movie]
      )
    )
  }

  def findActorsByMovie(movie: Movie): Task[Seq[Actor]] =
    ZIO.fromFuture { implicit ec =>
      Connection.db.run(
        ORM.movieActorMappingTable
          .filter(_.movieId === movie.id)
          .join(ORM.actorTable)
          .on(_.actorId === _.id)
          .map(_._2)
          .result
      )
    }

  def findStreamingProvidersByMovie(movie: Movie): Task[Seq[MovieStreamingProviderMapping]] =
    ZIO.fromFuture { implicit ec =>
      Connection.db.run(
        ORM.movieStreamingProviderMappingTable
          .filter(_.movieId === movie.id)
          // TODO: .map(_.streamingProvider)
          .result
      )
    }

  def findLocationsByMovie(movie: Movie): Task[Seq[MovieLocations]] =
    ZIO.fromFuture { implicit ec =>
      Connection.db.run(
        ORM.movieLocationsTable
          .filter(_.movieId === movie.id)
          .result
      )
    }

  def findActorDetails(actor: Actor): Task[Seq[ActorDetails]] =
    ZIO.fromFuture { implicit ec =>
      Connection.db.run(
        ORM.actorDetailsTable
          .filter(_.actorId === actor.id)
          .result
      )
    }
}
