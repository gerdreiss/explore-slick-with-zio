scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "dev.zio"             %% "zio"                % "2.0.0",
  "com.typesafe.slick"  %% "slick"              % "3.4.0-M1",
  "com.typesafe.slick"  %% "slick-hikaricp"     % "3.4.0-M1",
  "com.github.tminglei" %% "slick-pg"           % "0.21.0-M1",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.21.0-M1",
  "org.postgresql"       % "postgresql"         % "42.4.0"
)
