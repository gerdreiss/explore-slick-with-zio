import slick.jdbc.PostgresProfile.profile.api._
import zio.{Tag => _, _}
import java.time.LocalDate
import slick.jdbc.GetResult
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import java.util.UUID

object ORM {

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
    def id      = column[Long]("movie_actor_id")
    def movieId = column[Long]("movie_id")
    def actorId = column[Long]("actor_id")

    def * = (id, movieId, actorId) <> (MovieActorMapping.tupled, MovieActorMapping.unapply)
  }
  class MovieStreamingProviderMappingTable(tag: Tag)
      extends Table[MovieStreamingProviderMapping](
        tag,
        Some("movies"),
        "StreamingProviderMapping"
      ) {

    implicit val BCT: BaseColumnType[StreamingService.Provider] =
      MappedColumnType.base[StreamingService.Provider, String](
        _.toString,
        StreamingService.withName
      )

    def id                = column[Long]("id", O.PrimaryKey)
    def movieId           = column[Long]("movie_id")
    def streamingProvider = column[StreamingService.Provider]("streaming_provider")

    def * = (
      id,
      movieId,
      streamingProvider
    ) <> (MovieStreamingProviderMapping.tupled, MovieStreamingProviderMapping.unapply)
  }

  lazy val movieTable                         = TableQuery[MovieTable]
  lazy val actorTable                         = TableQuery[ActorTable]
  lazy val movieActorMappingTable             = TableQuery[MovieActorMappingTable]
  lazy val movieStreamingProviderMappingTable = TableQuery[MovieStreamingProviderMappingTable]

  lazy val tables =
    List(movieTable, actorTable, movieActorMappingTable, movieStreamingProviderMappingTable)

  lazy val ddl = tables.map(_.schema).reduce(_ ++ _)
}
