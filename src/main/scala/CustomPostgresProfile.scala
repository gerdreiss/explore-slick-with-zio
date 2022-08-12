import com.github.tminglei.slickpg._
import play.api.libs.json.{JsValue, Json}

trait CustomPostgresProfile
    extends ExPostgresProfile
    with PgArraySupport
    with PgHStoreSupport
    with PgJsonSupport
    with PgPlayJsonSupport {

  override val api = CustomPostgresApi

  override def pgjson: String = "jsonb"

  object CustomPostgresApi extends API with ArrayImplicits with HStoreImplicits with JsonImplicits {

    implicit val stringListTypeMapper: DriverJdbcType[List[String]] =
      new SimpleArrayJdbcType[String]("text").to(_.toList)

    implicit val playJsonArrayTypeMapper: DriverJdbcType[List[JsValue]] =
      new AdvancedArrayJdbcType[JsValue](
        pgjson,
        string => utils.SimpleArrayUtils.fromString(Json.parse)(string).orNull,
        value => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(value)
      ).to(_.toList)

  }
}

object CustomPostgresProfile extends CustomPostgresProfile
