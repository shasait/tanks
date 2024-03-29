tanks
=====

[![Maven Central](https://img.shields.io/maven-central/v/de.hasait.tanks/tanks.app.pc.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22de.hasait.tanks%22%20AND%20a%3A%22tanks.app.pc%22)

A small but funny Java game.

* 2D multiplayer tank battle
* Ad hoc networking (no server)
* Supports one or two players on one seat, optionally connected to others via network
    * Keys for player 1
        * WSAD: Move and rotate tank
        * QE: Rotate turrent
        * SPACE: Fire
    * Keys for player 2
        * Numpad 5213: Move and rotate tank
        * Numpad 46: Rotate turret
        * Numpad 0: Fire
* Uses [libGDX](https://libgdx.com/) for graphics
* Uses [JGroups](http://www.jgroups.org/) for networking

## Try it

### Launch via maven (wo checking out)

    mvn de.hasait.majala:majala-maven-plugin:majala \
    -Dmajala.coords=de.hasait.tanks:tanks.app.pc:1.0.0.RELEASE \
    -Dmajala.mainClass=de.hasait.tanks.app.pc.Main

### Download and launch

* Download [tanks.app.pc-1.0.0.RELEASE-shaded.jar](https://repo1.maven.org/maven2/de/hasait/tanks/tanks.app.pc/1.0.0.RELEASE/tanks.app.pc-1.0.0.RELEASE-shaded.jar
)
* Launch with: `java -jar tanks.app.pc-1.0.0.RELEASE-shaded.jar`

## License
tanks is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
