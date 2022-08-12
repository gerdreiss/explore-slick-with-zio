import slick.jdbc.PostgresProfile.api._

import java.time.LocalDate

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

  class MovieLocationsTable(tag: Tag)
      extends Table[MovieLocations](tag, Some("movies"), "MovieLocations") {
    import CustomPostgresProfile.api._

    def id        = column[Long]("movie_location_id", O.PrimaryKey)
    def movieId   = column[Long]("movie_id")
    def locations = column[List[String]]("locations")

    def * = (id, movieId, locations) <> (MovieLocations.tupled, MovieLocations.unapply)
  }

  class MoviePropertiesTable(tag: Tag)
      extends Table[MovieProperties](tag, Some("movies"), "MovieProperties") {
    import CustomPostgresProfile.api._

    def id         = column[Long]("id", O.PrimaryKey)
    def movieId    = column[Long]("movie_id")
    def properties = column[Map[String, String]]("properties")

    def * = (id, movieId, properties) <> (MovieProperties.tupled, MovieProperties.unapply)
  }

  class ActorDetailsTable(tag: Tag)
      extends Table[ActorDetails](tag, Some("movies"), "ActorDetails") {
    import CustomPostgresProfile.api._

    def id      = column[Long]("id", O.PrimaryKey)
    def actorId = column[Long]("actor_id")
    def details = column[play.api.libs.json.JsValue]("personal_info")

    def * = (id, actorId, details) <> (ActorDetails.tupled, ActorDetails.unapply)
  }

  lazy val movieTable                         = TableQuery[MovieTable]
  lazy val actorTable                         = TableQuery[ActorTable]
  lazy val movieActorMappingTable             = TableQuery[MovieActorMappingTable]
  lazy val movieStreamingProviderMappingTable = TableQuery[MovieStreamingProviderMappingTable]
  lazy val movieLocationsTable                = TableQuery[MovieLocationsTable]
  lazy val moviePropertiesTable               = TableQuery[MoviePropertiesTable]
  lazy val actorDetailsTable                  = TableQuery[ActorDetailsTable]

  lazy val tables = List(
    movieTable,
    actorTable,
    movieActorMappingTable,
    movieStreamingProviderMappingTable,
    movieLocationsTable,
    moviePropertiesTable,
    actorDetailsTable
  )

  lazy val ddl = tables.map(_.schema).reduce(_ ++ _)
}
