import play.api.libs.json.JsValue
import zio.{Tag => _}

import java.time.LocalDate

object Model {

  object StreamingService extends Enumeration {
    type Provider = Value
    val Netflix     = Value("Netflix")
    val AmazonPrime = Value("AmazonPrime")
    val Hulu        = Value("Hulu")
    val DisneyPlus  = Value("DisneyPlus")
    val HBO         = Value("HBO")
  }

  case class Movie(id: Long, name: String, releaseDate: LocalDate, lengthInMin: Int)
  case class Actor(id: Long, name: String)
  case class MovieActorMapping(id: Long, movieId: Long, actorId: Long)
  case class MovieStreamingProviderMapping(
      id: Long,
      movieId: Long,
      streamingService: StreamingService.Provider
  )

  case class MovieLocations(id: Long, movieId: Long, locations: List[String])
  case class MovieProperties(id: Long, movieId: Long, properties: Map[String, String])
  case class ActorDetails(id: Long, actorId: Long, details: JsValue)

}
