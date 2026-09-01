# Contributing Guide

We’d love for you to contribute to our source code and to make app even better.
While we try to keep requirements for contributing to a minimum,
there are a few guidelines we’d like you to follow:

* [Contributor License Agreement](#contributor-license-agreement)
* [Reporting Issues](#reporting-issues)
* [Submitting](#submitting)
* [Overriding a class from another module](#overriding-a-class-from-another-module)
* [Code of Conduct](#code-of-conduct)

## Contributor License Agreement

By submitting code as an individual you agree to the [individual contributor license agreement][individual-cla].
By submitting code as an entity you agree to the [corporate contributor license agreement][corporate-cla].

## Security issues

If you believe you've found a security vulnerability, please read our [security policy](SECURITY.md) for more details.

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
* If your change overrides a service, a controller or a repository of another module,
follow [Overriding a class from another module](#overriding-a-class-from-another-module).
* Push your changes to the topic branch in your fork of the repository.

* Initiate a [pull request](http://help.github.com/send-pull-requests/) on the development branch
that has the issue. For example, if the issue appears in `master` branch then choose `dev` branch,
 if the issue appears in `5.3` branch choose `5.3-dev`, etc...

## Overriding a class from another module

A module overrides a service, a controller or a repository of another module through a Guice
binding declared in its `*Module.java`:

```java
bind(SaleOrderComputeServiceImpl.class).to(SaleOrderComputeServiceSupplychainImpl.class);
```

Such a binding is active **as soon as the module is deployed**, whatever the apps the customer
actually installed. A customer running Sales without the Supply Chain app would still go through
the Supply Chain implementation.

**Rule: an override must not change the behaviour of the overridden module when its own app is
not installed.** Guard the overriding methods on the app and delegate to `super` otherwise:

```java
@Override
public SaleOrder computeSaleOrder(SaleOrder saleOrder) throws AxelorException {
  if (!appSupplychainService.isApp("supplychain")) {
    return super.computeSaleOrder(saleOrder);
  }
  // Supply Chain specific behaviour
  return saleOrder;
}
```

The `isApp(String)` method is available on `AppService`, `AppBaseService` and on every
`App<Module>Service`. The rule applies to repositories and web controllers too, not only to
services.

### Accepted exceptions

Two cases do not need a guard:

* the override is behaviour-neutral when the app is off, meaning the overriding implementation
produces the same result as the overridden one for every input;
* the overridden code path is unreachable unless the app of the *overriding* module is installed,
so the guard could never evaluate to false.

Note that the reverse is **not** an accepted exception: the fact that the overriding module depends
on the overridden module and on its app is true of every cross-module override, and says nothing
about what happens when the app of the overriding module is off. That is precisely the case the
rule is about.

In both accepted cases the class must be added to `config/app-override-guard-allowlist.txt` with a
comment explaining why, and the justification must appear in the merge request.

### Automated check

The `checkAppOverrideGuard` gradle task reports every cross-module override whose implementation
contains no `isApp(...)` call and is not allowlisted. It can be run locally:

```
./gradlew checkAppOverrideGuard
```

**What the check does not catch.** It only looks for a single `isApp(...)` occurrence anywhere in
the overriding class. A class that already guards one method therefore passes, even if a new
unguarded `@Override` is added to it later. A green check is not a proof that the rule is
respected: reviewing an override is still a reviewer's job.

The allowlist also carries the overrides that predate the check. They are grouped by module and
are being removed one module at a time; do not add new entries to those sections.

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
