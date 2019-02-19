# Fight For Subs PubSub Sources

These are the FFS PubSub server sources.

The FFS Project is accessible from [this address](https://ffs-events.zerator.com).

Theses sources will allow you to build the PubSub's binaries.

By contributing or using this project, you agree to abide by the [Code of Conduct](/CODE_OF_CONDUCT.md) when interracting with a community member.

## Documentation

The PubSub documentation can be found [here](https://github.com/AlexMog/FFS-Documentation/blob/master/Doc/PubSub/)

## Build and run

First of all clone and build the project's Framework

```bash
git clone https://github.com/AlexMog/ApiLib.git
cd ApiLib && mvn install
```

Then, clone and build the project

```bash
git clone https://github.com/AlexMog/FFS-PubSub.git
cd FFS-PubSub && mvn compile assembly:single
```

Then, copy and edit the project's settings in the build directory

```bash
cp configs.properties.example target/configs.properties
cp databases.json.example target/databases.json.example
```

You can edit thoes files depending on your configurations

Finally, you can run the project! :)

```bash
cd target && java -jar ffs-pubsub-{VERSION}-jar-with-dependencies.jar
```

## Bug Report

If you find any bugs, please report it on the [Issues](https://github.com/AlexMog/FFS-PubSub/issues) page of the project

## Deployment

TODO

## Built with

* [Maven](https://maven.apache.org/)

## Contributing

Please read [CONTRIBUTING.md](/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning.

## Authors

* **Alexandre Moghrabi** - *Initial work* - [GitHub](https://github.com/AlexMog) [GitLab](https://gitlab.com/AlexMog)

## License

This project is under GNU GPLv3 License - see the [LICENSE](/LICENSE) file for details.