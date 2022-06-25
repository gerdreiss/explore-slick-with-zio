scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "dev.zio"             %% "zio"                % "2.0.0",
  "com.typesafe.slick"  %% "slick"              % "3.3.3",
  "com.typesafe.slick"  %% "slick-hikaricp"     % "3.3.3",
  "com.github.tminglei" %% "slick-pg"           % "0.20.3",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.20.3",
  "org.postgresql"       % "postgresql"         % "42.4.0"
)
