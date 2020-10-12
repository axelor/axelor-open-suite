# Contributing Guide

We’d love for you to contribute to our source code and to make app even better.
While we try to keep requirements for contributing to a minimum,
there are a few guidelines we’d like you to follow:

* [Contributor License Agreement](#contributor-license-agreement)
* [Reporting Issues](#reporting-issues)
* [Submitting](#submitting)
* [Code of Conduct](#code-of-conduct)

## Contributor License Agreement

By submitting code as an individual you agree to the [individual contributor license agreement][individual-cla].
By submitting code as an entity you agree to the [corporate contributor license agreement][corporate-cla].

## Reporting Issues

Before you submit your issue search the archive, maybe your question was already answered.

If your issue appears to be a bug, and hasn't been reported, open a new issue.
Help us to maximize the effort we can spend fixing issues and adding new features,
by not reporting duplicate issues. Providing the following information will increase
the chances of your issue being dealt with quickly:

* **Use Case** – explain your use case
* **Overview of the issue** – include stacktrace
* **Version** – which version you are using?
* **Browsers and Operating System** – is this a problem with all browsers?
* **Reproduce the Error** – provide a patch against app to reproduce the error
* **Related Issues** – has a similar issue been reported before?
* **Suggest a Fix** – you can point to what might be causing the problem (line of code or commit)

Please make sure you don’t post any sensitive information while reporting issues.

## Submitting

* [Fork](https://help.github.com/articles/fork-a-repo/) the repo.
* Code!
* Format the java code to follow Google Code Format. Tools:
  * Gradle Task: `./gradlew spotlessApply`
  * IDE plugin
    * [Eclipse](https://github.com/google/google-java-format#eclipse)
    * [IntelliJ](https://github.com/google/google-java-format#intellij)
* You must create a changelog entry to describe the change.
See [the README in changelogs folder](https://github.com/axelor/axelor-open-suite/blob/master/changelogs/README.md)
and follow the instructions.
* Push your changes to the topic branch in your fork of the repository.

* Initiate a [pull request](http://help.github.com/send-pull-requests/) on the development branch
that has the issue. For example, if the issue appears in `master` branch then choose `dev` branch,
 if the issue appears in `5.3` branch choose `5.3-dev`, etc...

## Code of Conduct

As contributors and maintainers of this project, and in the interest of
fostering an open and welcoming community, we pledge to respect all people who
contribute through reporting issues, posting feature requests, updating
documentation, submitting pull requests or patches, and other activities.

We are committed to making participation in this project a harassment-free
experience for everyone, regardless of level of experience, gender, gender
identity and expression, sexual orientation, disability, personal appearance,
body size, race, ethnicity, age, religion, or nationality.

Examples of unacceptable behavior by participants include:

* The use of sexualized language or imagery
* Personal attacks
* Trolling or insulting/derogatory comments
* Public or private harassment
* Publishing other's private information, such as physical or electronic
  addresses, without explicit permission
* Other unethical or unprofessional conduct

Project maintainers have the right and responsibility to remove, edit, or
reject comments, commits, code, wiki edits, issues, and other contributions
that are not aligned to this Code of Conduct, or to ban temporarily or
permanently any contributor for other behaviors that they deem inappropriate,
threatening, offensive, or harmful.

By adopting this Code of Conduct, project maintainers commit themselves to
fairly and consistently applying these principles to every aspect of managing
this project. Project maintainers who do not follow or enforce the Code of
Conduct may be permanently removed from the project team.

This Code of Conduct applies both within project spaces and in public spaces
when an individual is representing the project or its community.

Instances of abusive, harassing, or otherwise unacceptable behavior may be
reported by contacting a project maintainer at [conduct@axelor.com][mail]. All
complaints will be reviewed and investigated and will result in a response that
is deemed necessary and appropriate to the circumstances. Maintainers are
obligated to maintain confidentiality with regard to the reporter of an
incident.

This Code of Conduct is adapted from the [Contributor Covenant][homepage],
version 1.3.0, available at
[http://contributor-covenant.org/version/1/3/0/][version]

[mail]: mailto:conduct@axelor.com
[homepage]: http://contributor-covenant.org
[version]: http://contributor-covenant.org/version/1/3/0/
[individual-cla]: http://axelor.com/cla/individuel-cla/
[corporate-cla]: http://axelor.com/cla/corporate-cla/
