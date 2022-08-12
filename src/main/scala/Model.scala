import slick.jdbc.PostgresProfile.profile.api._
import zio.{Tag => _, _}
import java.time.LocalDate
import slick.jdbc.GetResult
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import java.util.UUID

object Model {

  object StreamingService extends Enumeration {
    type Provider = Value
    val Netflix     = Value("Netflix")
    val AmazonPrime = Value("AmazonPrime")
    val Hulu        = Value("Hulu")
    val DisneyPlus  = Value("DisneyPlus")
  }

  case class Movie(id: Long, name: String, releaseDate: LocalDate, lengthInMin: Int)
  case class Actor(id: Long, name: String)
  case class MovieActorMapping(id: Long, movieId: Long, actorId: Long)
  case class MovieStreamingProviderMapping(
      id: Long,
      movieId: Long,
      streamingService: StreamingService.Provider
  )

}
