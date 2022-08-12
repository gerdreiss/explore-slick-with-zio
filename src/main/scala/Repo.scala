import slick.jdbc.PostgresProfile.profile.api._
import zio.{Tag => _, _}
import java.time.LocalDate
import slick.jdbc.GetResult
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import java.util.UUID

object Repo {

  import Model._
  import ORM._

  object Connection {
    val db = Database.forConfig("postgres")
  }

  def removeAll =
    ZIO.fromFuture { implicit ec =>
      Connection.db.run(
        DBIO.seq(
          ORM.movieStreamingProviderMappingTable.delete,
          ORM.movieActorMappingTable.delete,
          ORM.movieTable.delete,
          ORM.actorTable.delete
        )
      )
    }

  def insertMovies(movies: Movie*) =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.movieTable ++= movies))

  def insertMovie(movie: Movie) =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.movieTable += movie))

  def insertActors(actors: Actor*) =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.actorTable ++= actors))

  def insertActor(actor: Actor) =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.actorTable += actor))

  def insertStreamProvider(streamingProvider: MovieStreamingProviderMapping) =
    ZIO.fromFuture(implicit ec =>
      Connection.db.run(ORM.movieStreamingProviderMappingTable += streamingProvider)
    )

  def insertStreamProviders(streamingProviders: MovieStreamingProviderMapping*) =
    ZIO.fromFuture(implicit ec =>
      Connection.db.run(ORM.movieStreamingProviderMappingTable ++= streamingProviders)
    )

  def insertMovieAndActors(movie: Movie, actors: Actor*) =
    ZIO.fromFuture { implicit ec =>
      Connection.db.run(
        DBIO.seq(
          ORM.movieTable += movie,
          ORM.actorTable ++= actors,
          ORM.movieActorMappingTable ++= actors.map(actor =>
            MovieActorMapping(
              UUID.randomUUID().hashCode(), // should be hopefully mostly unique
              movie.id,
              actor.id
            )
          )
        )
      )
    }

  def insertMovieAndStreamingProvider(
      movie: Movie,
      streamingProviders: StreamingService.Provider*
  ) =
    ZIO.fromFuture { implicit ec =>
      Connection.db.run(
        DBIO.seq(
          ORM.movieTable += movie,
          ORM.movieStreamingProviderMappingTable ++= streamingProviders.map(streamingProvider =>
            MovieStreamingProviderMapping(
              UUID.randomUUID().hashCode(), // should be hopefully mostly unique
              movie.id,
              streamingProvider
            )
          )
        )
      )
    }

  def getAllMovies: Task[Seq[Movie]] =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.movieTable.result))

  def getAllActors: Task[Seq[Actor]] =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.actorTable.result))

  def getMovieById(id: Long): Task[Seq[Movie]] =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.movieTable.filter(_.id === id).result))

  def getActorById(id: Long): Task[Seq[Actor]] =
    ZIO.fromFuture(implicit ec => Connection.db.run(ORM.actorTable.filter(_.id === id).result))

  def findMovieByTitle(title: String) =
    ZIO.fromFuture(implicit ec =>
      Connection.db.run(ORM.movieTable.filter(_.name.like(s"%$title%")).result)
    )

  def findMoviesByPlainQuery = {
    implicit val GRM: GetResult[Movie] =
      GetResult(r => Movie(r.<<, r.<<, LocalDate.parse(r.nextString()), r.<<))

    ZIO.fromFuture(implicit ec =>
      Connection.db.run(
        sql"""SELECT * FROM movies."Movie"""".as[Movie]
      )
    )
  }

  def updateLength(id: Long, length: Int) =
    ZIO.fromFuture(implicit ec =>
      Connection.db.run(
        ORM.movieTable
          .filter(_.id === id)
          .map(_.lengthInMin)
          .update(length)
      )
    )

  def findActorsByMovie(movie: Movie) =
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

  def findStreamingProvidersByMovie(movie: Movie) =
    ZIO.fromFuture { implicit ec =>
      Connection.db.run(
        ORM.movieStreamingProviderMappingTable
          .filter(_.movieId === movie.id)
          .result
      )
    }
}